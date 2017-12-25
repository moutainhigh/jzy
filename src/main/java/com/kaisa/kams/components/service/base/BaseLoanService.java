package com.kaisa.kams.components.service.base;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.LoanRepayService;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.DecimalFormatUtils;
import com.kaisa.kams.components.utils.DecimalUtils;
import com.kaisa.kams.enums.LoanRepayStatus;
import com.kaisa.kams.enums.LoanStatus;
import com.kaisa.kams.models.Channel;
import com.kaisa.kams.models.Loan;
import com.kaisa.kams.models.LoanFee;
import com.kaisa.kams.models.LoanFeeRecord;
import com.kaisa.kams.models.LoanRepay;
import com.kaisa.kams.models.LoanRepayRecord;
import com.kaisa.kams.models.User;
import com.kaisa.kams.models.flow.LoanedResult;

import org.apache.commons.lang.StringUtils;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.service.Service;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sunwanchao on 2017/3/2.
 */
@IocBean
public abstract class BaseLoanService extends Service {

    @Inject
    private LoanRepayService loanRepayService;
    public int fetchMaxPeriodByLoanId(String loanId) {
        Sql sql = Sqls.create("select max(period) as maxPeriod from sl_loan_repay l where l.loanId=@loanId");
        sql.setParam("loanId", loanId);
        sql.setCallback(Sqls.callback.integer());
        sql = dao().execute(sql);
        return sql.getInt();
    }

    public boolean clearLoanRepay(String repayId,String remark) {
        LoanRepay loanRepay = dao().fetch(LoanRepay.class, Cnd.where("id", "=", repayId));
        if (loanRepay == null) {
            return false;
        }
        Trans.exec((Atom) () -> {
            loanRepay.setRepayDate(getRepayDateByRepayId(repayId));
            updateLoanRepay(remark, loanRepay);
            updateCommonLoan(loanRepay);
        });
        return true;
    }

    public String checkClearLoanRepay(String repayId){
        LoanRepay loanRepay = dao().fetch(LoanRepay.class, Cnd.where("id", "=", repayId));
        if(loanRepay==null){
            return "还款计划不存在";
        }
        String msg = "";
        //如果所还本金等于已还本金
        BigDecimal residualAmount = DecimalFormatUtils.getNotNull(loanRepay.getAmount()).subtract(DecimalFormatUtils.getNotNull(loanRepay.getRepayAmount()));
        BigDecimal residualInterest = DecimalFormatUtils.getNotNull(loanRepay.getInterest()).subtract(DecimalFormatUtils.getNotNull(loanRepay.getRepayInterest()));
        if(residualAmount.doubleValue()>0){
            msg += "本金应还"+loanRepay.getAmount()+"，实还"+DecimalFormatUtils.getNotNull(loanRepay.getRepayAmount())+"。不允许结清！";
            return  msg;
        }
       /* if(residualInterest.doubleValue()>0){
            msg += "利息应还"+loanRepay.getInterest()+"，实还"+DecimalFormatUtils.getNotNull(loanRepay.getRepayInterest())+"。";
        }*/
        Map map = loanRepayService.fetchLoanRepayFeeByRepayId(repayId);
        Map cnMap = getFeeCN();
        for(Object key  :map.keySet()){
            //if("interest".equalsIgnoreCase(key.toString()))continue;
            Object value = map.get(key);
            if(Double.parseDouble(value.toString())>0L){
                msg += cnMap.get(key)+"还剩"+value+"未结清。";
            }
        }
        if(StringUtils.isNotEmpty(msg)){
            msg += "确定要结清？";
        }
        return msg;
    }

    private Map<String,String> getFeeCN(){
        Map<String,String> cnMap = new HashMap<String,String>();
        cnMap.put("interest","利息");
        cnMap.put("prepaymentFee","提前结清罚息");
        cnMap.put("prepaymentFeeRate","一次性服务费");
        cnMap.put("overdueFee","逾期罚息");
        cnMap.put("manageFee","资金管理费");
        cnMap.put("guaranteeFee","借款担保费");
        cnMap.put("serviceFee","借款服务费");
        return cnMap;
    }
    public Date getRepayDateByRepayId(String repayId) {
        String querySql = "select max(repayDate) maxRepayDate from sl_loan_repay_record where repayId=@repayId;";

        Sql sql = Sqls.create(querySql);
        sql.setParam("repayId",repayId);
        sql.setCallback(Sqls.callback.timestamp());
        dao().execute(sql);
        Date date = sql.getObject(Date.class);
        if (null != date) {
            return date;
        }
        return new Date();
    }

    private void updateCommonLoan(LoanRepay loanRepay) {
        User user = ShiroSession.getLoginUser();
        int maxPeriod = fetchMaxPeriodByLoanId(loanRepay.getLoanId());
        Loan loan = dao().fetch(Loan.class, loanRepay.getLoanId());
        //下一期的还款计划状态作为标的的状态
        LoanStatus loanStatus = loan.getLoanStatus();
        LoanRepay nextLoanRepay = dao().fetch(LoanRepay.class, Cnd.where("loanId", "=", loanRepay.getLoanId()).and("period", "=", loanRepay.getPeriod() + 1));
        if (nextLoanRepay != null) {
            LoanRepayStatus status = nextLoanRepay.getStatus();
            if (LoanRepayStatus.LOANED.equals(status)) {
                loan.setLoanStatus(LoanStatus.LOANED);
            } else if (LoanRepayStatus.OVERDUE.equals(status)) {
                loan.setLoanStatus(LoanStatus.OVERDUE);
            }
        }
        if (maxPeriod == loanRepay.getPeriod()) {
            loan.setLoanStatus(LoanStatus.CLEARED);
            loan.setClearDate(loanRepay.getRepayDate());
            addLoanRecode(loan,loan.getLoanTime());
            addClearLoanRecode(loan.getId());
        }
        if (!loanStatus.equals(loan.getLoanStatus())) {
            loan.setUpdateBy(user.getName());
            loan.setUpdateTime(new Date());
            dao().update(loan);
        }
    }

    private void updateLoanRepay(String remark, LoanRepay loanRepay) {
        User user = ShiroSession.getLoginUser();
        if (LoanRepayStatus.OVERDUE.equals(loanRepay.getStatus())) {
            loanRepay.setStatus(LoanRepayStatus.OVERDUE_CLEARED);
        } else if (LoanRepayStatus.AHEAD_CLEARED.equals(loanRepay.getStatus())) {
            loanRepay.setStatus(LoanRepayStatus.AHEAD_CLEARED);
        } else {
            loanRepay.setStatus(LoanRepayStatus.CLEARED);
        }
        loanRepay.setRemark(remark);
        loanRepay.setUpdateBy(user.getName());
        loanRepay.setUpdateTime(new Date());
        dao().update(loanRepay);
    }

    protected void addClearLoanRecode(String loanId) {
        //结清后就么有结束时间了，所以一并算好。
        Loan clearLoan = new Loan();
        clearLoan.setId(loanId);
        clearLoan.setLoanStatus(null);
        addLoanRecode(clearLoan,new Date());
    }

    public LoanedResult addLoanRecode(Loan loan, Date lastUpdateTime){

        Date now  = new Date();
        User user = ShiroSession.getLoginUser();
        LoanedResult approvalResult =  new LoanedResult();
        approvalResult.setLoanId(loan.getId());
        approvalResult.setApprovalTime(now);
        approvalResult.setNodeName(LoanStatus.LOANED.equals(loan.getLoanStatus())?"待放款":(LoanStatus.CLEARED.equals(loan.getLoanStatus())?"还款中":"已结清"));
        approvalResult.setNodeCode(LoanStatus.LOANED.equals(loan.getLoanStatus())?"D1-贷后":(LoanStatus.CLEARED.equals(loan.getLoanStatus())?"D2-贷后":"D3-贷后"));
        approvalResult.setUserId(user.getId());
        approvalResult.setUserName(user.getName());
        approvalResult.setCreateBy(user.getName());
        approvalResult.setCreateTime(now);
        approvalResult.setUpdateBy(user.getName());
        approvalResult.setUpdateTime(now);
        approvalResult.setStartTime(lastUpdateTime);
        approvalResult.setDuration(DateUtil.daysBetweenTowDate(approvalResult.getStartTime(),approvalResult.getCreateTime()));
        return dao().insert(approvalResult);
    }

    /**
     * loanRepayRecord表数据累加同步到Loanrepay
     * @param repayId
     * @return
     */
    @Aop(TransAop.READ_COMMITTED)
    public void syncToLoanRepay(String repayId,boolean isBill){
        LoanRepay loanRepay = this.dao().fetch(LoanRepay.class,Cnd.where("id","=",repayId));
        if(loanRepay == null){
            return;
        }

        loanRepay = initRepayInfo(loanRepay);
        loanRepay = repayLoanFee(loanRepay);
        loanRepay = repayLoan(loanRepay);

        loanRepay.setRepayTotalAmount(DecimalUtils.sum(loanRepay.getRepayTotalAmount(),loanRepay.getRepayFeeAmount()));
        if (isBill) {
            loanRepay.setTotalAmount(DecimalUtils.sumArr(loanRepay.getAmount(), loanRepay.getFeeAmount()));
        } else {
            loanRepay.setTotalAmount(DecimalUtils.sumArr(loanRepay.getAmount(), loanRepay.getInterest(), loanRepay.getFeeAmount()));
        }
        dao().update(loanRepay);
        Loan loan = dao().fetch(Loan.class,loanRepay.getLoanId());
        if(loan != null){
            syncToChannel(loan.getChannelId());
        }
    }

    private LoanRepay repayLoan(LoanRepay loanRepay) {
        //2.loanRepayRecord汇总到loanRepay
        List<LoanRepayRecord> loanRepayRecordList = this.dao().query(LoanRepayRecord.class, Cnd.where("repayId","=",loanRepay.getId()));
        for(LoanRepayRecord record:loanRepayRecordList){
            record.setRepayTotalAmount(DecimalUtils.sumArr(record.getRepayAmount(),record.getRepayInterest()));
            loanRepay.setRepayAmount(DecimalUtils.sum(loanRepay.getRepayAmount(),record.getRepayAmount()));
            loanRepay.setRepayInterest(DecimalUtils.sum(loanRepay.getRepayInterest(),record.getRepayInterest()));
            loanRepay.setRepayTotalAmount(DecimalUtils.sum(loanRepay.getRepayTotalAmount(),record.getRepayTotalAmount()));
        }
        dao().update(loanRepayRecordList);
        return loanRepay;
    }

    private LoanRepay repayLoanFee(LoanRepay loanRepay) {
        String repayId = loanRepay.getId();
        List<LoanFeeRecord> loanFeeRecordList = dao().query(LoanFeeRecord.class, Cnd.where("repayId","=",repayId));
        List<LoanFee> loanFeeList = dao().query(LoanFee.class,Cnd.where("repayId","=",repayId));
        Map<String,List<LoanFeeRecord>> feeRecordId2Map = loanFeeRecordList.stream().collect(Collectors.groupingBy(l->l.getLoanFeeId()));
        for(LoanFee loanFee:loanFeeList){
            loanFee.setRepayFeeAmount(null);
            if(feeRecordId2Map.containsKey(loanFee.getId())){
                List<LoanFeeRecord> records = feeRecordId2Map.get(loanFee.getId());
                records.stream().forEach(l->{
                    loanFee.setRepayFeeAmount(DecimalUtils.sum(loanFee.getRepayFeeAmount(),l.getRepayFeeAmount()));
                });
            }
            loanRepay.setRepayFeeAmount(DecimalUtils.sum(loanRepay.getRepayFeeAmount(),loanFee.getRepayFeeAmount()));
            loanRepay.setFeeAmount(DecimalUtils.sumArr(loanRepay.getFeeAmount(),loanFee.getFeeAmount()));
        }
        dao().update(loanFeeList);
        return loanRepay;
    }

    private LoanRepay initRepayInfo(LoanRepay loanRepay) {
        loanRepay.setRepayAmount(null);
        loanRepay.setRepayInterest(null);
        loanRepay.setRepayFeeAmount(null);
        loanRepay.setRepayOtherFee(null);
        loanRepay.setRepayTotalAmount(null);
        loanRepay.setFeeAmount(null);
        loanRepay.setTotalAmount(null);
        return loanRepay;
    }

    /**
     * 更新渠道在库余额
     * 在库余额为该渠道剩余未还本金
     * @param channelId
     */
    @Async
    public void syncToChannel(String channelId){
        if(StringUtils.isEmpty(channelId)){
            return;
        }
        Channel channel = this.dao().fetch(Channel.class,channelId);
        if(channel==null){
            return ;
        }
        String sqlStr = "select sum(v.amount) amount from sl_channel c," +
                "sl_loan l," +
                "(select sum(amount-IFNULL(repayAmount,0)) amount,loanId from sl_loan_repay where loanId is not null and status in ('LOANED','OVERDUE') group by loanId) v " +
                "where c.id=l.channelId " +
                "and l.id=v.loanId " +
                "and l.channelId=@channelId";

        Sql sql = Sqls.create(sqlStr);
        sql.setParam("channelId",channelId);
        sql.setCallback(Sqls.callback.doubleValue());
        dao().execute(sql);
        double remainAmount = sql.getDouble();
        channel.setResidualAmount(BigDecimal.valueOf(remainAmount));
        this.dao().update(channel);
    }
}

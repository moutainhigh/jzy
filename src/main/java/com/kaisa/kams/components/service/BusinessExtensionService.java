package com.kaisa.kams.components.service;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.utils.DecimalUtils;
import com.kaisa.kams.components.utils.LoanCalculator;
import com.kaisa.kams.components.utils.SqlUtil;
import com.kaisa.kams.components.utils.TimeUtils;
import com.kaisa.kams.components.view.loan.LoanRepayPlan;
import com.kaisa.kams.components.view.loan.Repayment;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.sql.SqlCallback;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.service.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by sunwanchao on 2017/4/11.
 */
@IocBean(fields = "dao")
public class BusinessExtensionService extends Service {
    @Inject
    private LoanService loanService;

    @Inject
    private LoanRepayService loanRepayService;

    @Inject
    private LoanFeeTempService loanFeeTempService;

    @Inject
    private ExtensionService extensionService;


    public  List<OldLoanRepay>  getOldLoanRepayRecode(String loanId){
        List<LoanRepay> sourceLoanRepayList = dao().query(LoanRepay.class, Cnd.where("loanId", "=", loanId).orderBy("period", "asc"));
        List<OldLoanRepay> oldLoanRepayList = copyLoanRepay(loanId,sourceLoanRepayList);
        return oldLoanRepayList;
    }

    public List<Extension> getExtensionListByReject(String loanId,String extensionCode){
        List<Extension> extensionList = dao().query(Extension.class,Cnd.where("loanId","=",loanId).and("extensionCode","<=",extensionCode).and("approvalStatusType","=",ApprovalStatusType.REJECT).asc("extensionCode"));
        for(Extension extension : extensionList){
            extension.setExtensionId(extension.getId());
        }
        return extensionList;
    }
    public LoanExtension getLoanExtensionByExtension(Extension extension){
        LoanExtension loanExtension  = new LoanExtension();
        loanExtension.setLoanId(extension.getLoanId());
        loanExtension.setTerm(extension.getTerm());
        loanExtension.setTermType(extension.getTermType());
        loanExtension.setLoanLimitType(extension.getLoanLimitType());
        loanExtension.setInterestAmount(extension.getInterestAmount());
        loanExtension.setInterestRate(extension.getInterestRate());
        loanExtension.setEnterpriseExplain(extension.getEnterpriseExplain());
        loanExtension.setEnterpriseAgreement(extension.getEnterpriseAgreement());
        loanExtension.setPosition(extension.getPosition());
        loanExtension.setRepayMethod(extension.getRepayMethod());
        loanExtension.setCalculationMethod(extension.getCalculationMethod());
        loanExtension.setRepayDateType(extension.getRepayDateType());
        loanExtension.setExtensionId(extension.getId());
        loanExtension.setApprovalStatusType(extension.getApprovalStatusType());
        loanExtension.setExtensionCode(extension.getExtensionCode());
        loanExtension.updateOperator();
        return loanExtension;
    }
    @Aop(TransAop.READ_COMMITTED)
    public NutMap enableExtension(Extension extension){
        LoanExtension loanExtension =  getLoanExtensionByExtension(extension);
        NutMap nutMap = generateLoanRepay(loanExtension.getLoanId(),loanExtension.getTermType(),loanExtension.getTerm(),loanExtension.getLoanLimitType(),
                LoanLimitType.FIX_AMOUNT.equals(loanExtension.getLoanLimitType())?loanExtension.getInterestAmount():loanExtension.getInterestRate(),
                loanExtension.getRepayMethod(),loanExtension.getCalculationMethod(),loanExtension.getRepayDateType());
        if ((Boolean) nutMap.get("ok") == false) {
            return nutMap;
        }
        List<LoanRepay> loanRepayList = (List<LoanRepay>) nutMap.get("data");
        List<LoanRepay> updateRepayList = new ArrayList<>();
        List<LoanRepay> insertRepayList = new ArrayList<>();
        loanRepayList.stream().forEach(r -> {
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(r.getId())) {
                updateRepayList.add(r);
            } else {
                insertRepayList.add(r);
            }
        });
        boolean isOverDue = isOverDue(updateRepayList);
        List<LoanRepay> sourceLoanRepayList = dao().query(LoanRepay.class, Cnd.where("loanId", "=", loanExtension.getLoanId()).orderBy("period", "asc"));
        List<OldLoanRepay> oldLoanRepayList = copyLoanRepay(loanExtension.getLoanId(),sourceLoanRepayList);
        List<LoanFee> feeList = (List<LoanFee>) nutMap.get("feeList");
        dao().insert(oldLoanRepayList);

        if (sourceLoanRepayList.size() > updateRepayList.size()) {
            LoanRepay oldLastLoanRepay = sourceLoanRepayList.get(sourceLoanRepayList.size() - 1);
            dao().delete(LoanRepay.class, oldLastLoanRepay.getId());
        } else {
            LoanRepay updateLastLoanRepay = updateRepayList.get(updateRepayList.size() - 1);
            dao().update(updateLastLoanRepay);
        }
        List<LoanRepay> insertedRepayList = dao().insert(insertRepayList);
        //修改loanExtension的状态为启用，因为展期到这里已经审批通过了，这里做一系列操作
        extensionService.updateLoanExtension(loanExtension,null);
        if (CollectionUtils.isNotEmpty(feeList) && CollectionUtils.isNotEmpty(insertedRepayList)) {
            feeList.forEach(fee -> fee.setRepayId(getRepayId(fee.getPeriod(), insertedRepayList)));
            dao().insert(feeList);
        }
        if (!isOverDue && org.apache.commons.lang3.StringUtils.isNotEmpty(loanExtension.getLoanId())) {
            dao().update(Loan.class, Chain.make("loanStatus",LoanStatus.LOANED), Cnd.where("id","=",loanExtension.getLoanId()));
        }
        return new NutMap().setv("ok", true);
    }


    @Aop(TransAop.READ_COMMITTED)
    public NutMap insert(String loanId,
                         LoanTermType termType,
                         String term,
                         LoanLimitType loanLimitType,
                         BigDecimal interest,
                         String repayMethod, String enterpriseExplain
            , String enterpriseAgreement, String calculationMethod, String repayDateType) {
        NutMap nutMap = generateLoanRepay(loanId, termType, term, loanLimitType, interest, repayMethod, calculationMethod, repayDateType);
        if ((Boolean) nutMap.get("ok") == false) {
            return nutMap;
        }
        List<LoanRepay> loanRepayList = (List<LoanRepay>) nutMap.get("data");
        List<LoanRepay> updateRepayList = new ArrayList<>();
        List<LoanRepay> insertRepayList = new ArrayList<>();
        loanRepayList.stream().forEach(r -> {
            if (StringUtils.isNotEmpty(r.getId())) {
                updateRepayList.add(r);
            } else {
                insertRepayList.add(r);
            }
        });
        boolean isOverDue = isOverDue(updateRepayList);
        List<LoanRepay> sourceLoanRepayList = dao().query(LoanRepay.class, Cnd.where("loanId", "=", loanId).orderBy("period", "asc"));
        List<OldLoanRepay> oldLoanRepayList = copyLoanRepay(loanId,sourceLoanRepayList);
        LoanExtension loanExtension = getLoanExtension(loanId, termType, term, loanLimitType, interest, enterpriseExplain, enterpriseAgreement, repayMethod, calculationMethod, repayDateType);
        List<LoanFee> feeList = (List<LoanFee>) nutMap.get("feeList");
        dao().insert(oldLoanRepayList);

        if (sourceLoanRepayList.size() > updateRepayList.size()) {
            LoanRepay oldLastLoanRepay = sourceLoanRepayList.get(sourceLoanRepayList.size() - 1);
            dao().delete(LoanRepay.class, oldLastLoanRepay.getId());
        } else {
            LoanRepay updateLastLoanRepay = updateRepayList.get(updateRepayList.size() - 1);
            dao().update(updateLastLoanRepay);
        }
        //dao().update(updateRepayList);
        List<LoanRepay> insertedRepayList = dao().insert(insertRepayList);
        dao().insert(loanExtension);
        if (CollectionUtils.isNotEmpty(feeList) && CollectionUtils.isNotEmpty(insertedRepayList)) {
            feeList.forEach(fee -> fee.setRepayId(getRepayId(fee.getPeriod(), insertedRepayList)));
            dao().insert(feeList);
        }
        if (!isOverDue && StringUtils.isNotEmpty(loanId)) {
            dao().update(Loan.class, Chain.make("loanStatus",LoanStatus.LOANED), Cnd.where("id","=",loanId));
        }
        return new NutMap().setv("ok", true);
    }

    private boolean isOverDue(List<LoanRepay> updateRepayList) {
        boolean overDueFlag = false;
        for (LoanRepay lr : updateRepayList) {
            if (LoanRepayStatus.OVERDUE.equals(lr.getStatus())) {
                overDueFlag = true;
                break;
            }
        }
        return overDueFlag;
    }

    private String getRepayId(int period, List<LoanRepay> insertedRepayList) {
        for (LoanRepay loanRepay : insertedRepayList) {
            if (loanRepay.getPeriod() == period) {
                return loanRepay.getId();
            }
        }
        return "";
    }

    private List<OldLoanRepay> copyLoanRepay(String loanId, List<LoanRepay> loanRepayList) {
        int position = getExtensionCount(loanId);
        List<OldLoanRepay> oldLoanRepayList = new ArrayList<>();
        for (LoanRepay loanRepay : loanRepayList) {
            OldLoanRepay oldLoanRepay = new OldLoanRepay();
            oldLoanRepay.setRepayId(loanRepay.getId());
            oldLoanRepay.setLoanId(loanRepay.getLoanId());
            oldLoanRepay.setPeriod(loanRepay.getPeriod());
            oldLoanRepay.setTotalAmount(loanRepay.getTotalAmount());
            oldLoanRepay.setAmount(loanRepay.getAmount());
            oldLoanRepay.setInterest(loanRepay.getInterest());
            oldLoanRepay.setRepayInterest(loanRepay.getRepayInterest());
            oldLoanRepay.setRepayFeeAmount(loanRepay.getRepayFeeAmount());
            oldLoanRepay.setRepayAmount(loanRepay.getRepayAmount());
            oldLoanRepay.setRepayTotalAmount(loanRepay.getRepayTotalAmount());
            oldLoanRepay.setFeeAmount(loanRepay.getFeeAmount());
            oldLoanRepay.setDueDate(loanRepay.getDueDate());
            oldLoanRepay.setOutstanding(loanRepay.getOutstanding());
            oldLoanRepay.setStatus(loanRepay.getStatus());
            oldLoanRepay.setRemark(loanRepay.getRemark());
            oldLoanRepay.setPosition(position);
            oldLoanRepayList.add(oldLoanRepay);
        }
        return oldLoanRepayList;
    }

    private int getExtensionCount(String loanId) {
        Sql sql = Sqls.fetchInt("select count(id) from sl_loan_extension where loanId=@loanId");
        sql.setParam("loanId", loanId);
        dao().execute(sql);
        return sql.getInt();
    }

    /**
     * 生成展期还款计划
     */
    public NutMap generateLoanRepay(String loanId,
                                    LoanTermType termType,
                                    String term,
                                    LoanLimitType loanLimitType,
                                    BigDecimal interest,
                                    String repayMethod, String calculationMethod, String repayDateType) {
        Loan loan = loanService.fetchById(loanId);
        if (loan == null) {
            return new NutMap().setv("ok", false).setv("msg", "标的不存在");
        }
        List<LoanRepay> oldLoanRepayList = loanRepayService.queryLoanRepayByLoanId(loanId);
        if (oldLoanRepayList == null || oldLoanRepayList.isEmpty()) {
            return new NutMap().setv("ok", false).setv("msg", "请先生成还款计划");
        }
        LoanRepayMethod useRepayMethod = LoanRepayMethod.valueOf(repayMethod);
        LoanRepayDateType loanRepayDateType = getLoanRepayDateType(repayDateType);
        // 获取展期的loanTime
        Date newLoanTime = getNewLoanTime(termType, oldLoanRepayList);
        CalculateMethodAboutDay calculateMethodAboutDay = getCalculateMethodAboutDay(calculationMethod);
        // 获取剩余本金
        BigDecimal remainAmount = getRemainAmount(oldLoanRepayList);
        LoanRepayPlan newLoanRepayPlan = LoanCalculator.calcuate(remainAmount, termType, term, useRepayMethod, loanLimitType, interest, loanRepayDateType, newLoanTime, null, calculateMethodAboutDay);
        if (newLoanRepayPlan == null) {
            return new NutMap().setv("ok", false).setv("msg", "参数有误");
        }
        loan.setAmount(remainAmount);
        loan.setTermType(termType);
        loan.setRepayMethod(useRepayMethod);
        LoanRepayDateType oldLoanRepayDateType = getLastRepayDateType(loan);
        int transType = getTransRepayDateType(oldLoanRepayDateType,loanRepayDateType);
        List<LoanFee> loanRepayFeeList = updateLoanRepayAndGetFees(loan, oldLoanRepayList, newLoanRepayPlan, transType);
        int position = getExtensionCount(loanId);
        oldLoanRepayList.stream().forEach(r->{
            r.setPosition( position);
            r.setRepayId(r.getId());
        });
        return new NutMap().setv("ok", true).setv("data", oldLoanRepayList).setv("feeList", loanRepayFeeList);
    }

    private LoanRepayDateType getLastRepayDateType(Loan loan) {
        Sql sql = Sqls.create("select repayDateType from sl_loan_extension where loanId=@loanId order by position desc limit 0,1");
        sql.params().set("loanId", loan.getId());
        sql.setCallback(Sqls.callback.str());
        dao().execute(sql);
        String repayDateType = sql.getString();
        if (StringUtils.isNotEmpty(repayDateType)) {
            return LoanRepayDateType.valueOf(repayDateType);
        }
        return loan.getRepayDateType();
    }

    /**
     * 获取转换类型
     * @return
     */
    private int getTransRepayDateType(LoanRepayDateType old,LoanRepayDateType newer) {
        //期初->期初
        //期初->期末
        if (LoanRepayDateType.REPAY_PRE.equals(old)) {
            if (LoanRepayDateType.REPAY_PRE.equals(newer)) {
                return 1;
            }
            if (LoanRepayDateType.REPAY_SUF.equals(newer)) {
                return 2;
            }
        }
        //期末->期初
        //期末->期末
        if (LoanRepayDateType.REPAY_SUF.equals(old)) {
            if (LoanRepayDateType.REPAY_PRE.equals(newer)) {
                return 3;
            }
            if (LoanRepayDateType.REPAY_SUF.equals(newer)) {
                return 4;
            }
        }
        return 0;
    }

    private List<LoanFee> updateLoanRepayAndGetFees(Loan loan, List<LoanRepay> oldLoanRepayList, LoanRepayPlan newLoanRepayPlan, int transType) {
        User user = ShiroSession.getLoginUser();
        String name = user.getName();
        String loanId = loan.getId();
        String remark = getRemark(oldLoanRepayList);
        List<Repayment> repaymentList = newLoanRepayPlan.getRepayments();
        List<LoanFeeTemp> loanFeeTempList = loanFeeTempService.queryByLoanIdAndChargeNode(loanId, FeeChargeNode.REPAY_NODE);
        loanRepayService.filterLoanFeeTemp(loanFeeTempList, loan);
        List<LoanFee> loanRepayFeeList = new ArrayList<>();
        int period = oldLoanRepayList.size();
        List<LoanRepay> newLoanRepays = new ArrayList();
        LoanRepay oldLastLoanRepay = oldLoanRepayList.get(oldLoanRepayList.size() - 1);
        boolean isDayEndToPre = isDayEndToPre(loan.getTermType(), transType, oldLastLoanRepay);
        if (isDayEndToPre) {
            period++;
        }
        if (4 == transType && isInterestRepayMethod(loan.getRepayMethod())) {
            newLoanRepays.add(convertOldLastLoanRepayToNew(oldLastLoanRepay,remark,name));
            period++;
        }
        Date date = new Date();
        for (int i=0; i < repaymentList.size(); i++) {
            Repayment repayment = repaymentList.get(i);
            LoanRepay loanRepay = new LoanRepay();
            loanRepay.setLoanId(loanId);
            loanRepay.setPeriod(period++);
            loanRepay.setAmount(repayment.getPrincipal());
            loanRepay.setInterest(repayment.getInterest());
            loanRepay.setOutstanding(repayment.getOutstanding());
            BigDecimal feeAmount = BigDecimal.ZERO;
            if (1 != transType || i != 0) {
                feeAmount = loanRepayService.createFee(loan, user, loanFeeTempList, loanRepayFeeList, repayment, loanRepay, feeAmount);
            }
            loanRepay.setFeeAmount(feeAmount);
            loanRepay.setTotalAmount(DecimalUtils.sumArr(loanRepay.getAmount(), loanRepay.getInterest(), loanRepay.getFeeAmount()));
            loanRepay.setDueDate(repayment.getDueDate());
            loanRepay.setStatus(LoanRepayStatus.LOANED);
            loanRepay.setRemark(remark);
            loanRepay.setCreateBy(name);
            loanRepay.setCreateTime(date);
            loanRepay.setUpdateBy(name);
            loanRepay.setUpdateTime(date);
            newLoanRepays.add(loanRepay);
        }
        if (isDayEndToPre) {
            oldLastLoanRepay.setAmount(BigDecimal.ZERO);
            oldLastLoanRepay.setOutstanding(loan.getAmount());
            oldLoanRepayList.addAll(oldLoanRepayList.size(), newLoanRepays);
            return loanRepayFeeList;
        }

        List<LoanFee> oldFeeList = dao().query(LoanFee.class, Cnd.where("repayId", "=", oldLastLoanRepay.getId()).and("chargeNode", "=", FeeChargeNode.REPAY_NODE));
        LoanRepay newFirstRepay = newLoanRepays.get(0);
        newFirstRepay.setInterest(getRemainInterest(oldLastLoanRepay,newFirstRepay));
        loanRepayFeeList = getRepayLoanFeeList(oldFeeList, newFirstRepay, loanRepayFeeList);
        newFirstRepay.setFeeAmount(getLastPeriodFeeAmount(loanRepayFeeList, newFirstRepay.getPeriod()));
        newFirstRepay.setTotalAmount(DecimalUtils.sumArr(newFirstRepay.getAmount(), newFirstRepay.getInterest(), newFirstRepay.getFeeAmount()));
        oldLoanRepayList.remove(oldLoanRepayList.size() - 1);
        oldLoanRepayList.addAll(oldLoanRepayList.size(), newLoanRepays);
        return loanRepayFeeList;
    }

    private boolean isInterestRepayMethod(LoanRepayMethod repayMethod) {
        return LoanRepayMethod.INTEREST_DAYS == repayMethod || LoanRepayMethod.INTEREST_MONTHS == repayMethod;
    }

    private boolean isDayEndToPre(LoanTermType termType, int transType, LoanRepay oldLastLoanRepay) {
        if (3 != transType) {
            return false;
        }
        if (LoanTermType.DAYS != termType && LoanTermType.FIXED_DATE != termType) {
            return false;
        }
        if (DecimalUtils.sub(oldLastLoanRepay.getInterest(),oldLastLoanRepay.getRepayInterest()).compareTo(BigDecimal.ZERO)<=0) {
            return false;
        }
        return true;
    }

    private LoanRepay convertOldLastLoanRepayToNew(LoanRepay oldLastLoanRepay,String remark,String name) {
        Date date = new Date();
        LoanRepay loanRepay = new LoanRepay();
        loanRepay.setLoanId(oldLastLoanRepay.getLoanId());
        loanRepay.setPeriod(oldLastLoanRepay.getPeriod());
        loanRepay.setAmount(BigDecimal.ZERO);
        loanRepay.setInterest(BigDecimal.ZERO);
        loanRepay.setOutstanding(DecimalUtils.sub(oldLastLoanRepay.getAmount(),oldLastLoanRepay.getRepayAmount()));
        loanRepay.setFeeAmount(BigDecimal.ZERO);
        loanRepay.setTotalAmount(BigDecimal.ZERO);
        loanRepay.setDueDate(oldLastLoanRepay.getDueDate());
        loanRepay.setStatus(LoanRepayStatus.LOANED);
        loanRepay.setRemark(remark);
        loanRepay.setCreateBy(name);
        loanRepay.setCreateTime(date);
        loanRepay.setUpdateBy(name);
        loanRepay.setUpdateTime(date);
        return loanRepay;
    }

//    private List<LoanFee> updateLoanRepayAndGetLoanFees(Loan loan, List<LoanRepay> oldLoanRepayList, LoanRepayPlan loanRepayPlan) {
//        LoanRepay lastLoanRepay = oldLoanRepayList.get(oldLoanRepayList.size() - 1);
//        User user = ShiroSession.getLoginUser();
//        String loanId = loan.getId();
//        String remark = getRemark(oldLoanRepayList);
//        List<Repayment> repaymentList = loanRepayPlan.getRepayments();
//        List<LoanFeeTemp> loanFeeTempList = loanFeeTempService.queryByLoanIdAndChargeNode(loanId, FeeChargeNode.REPAY_NODE);
//        loanRepayService.filterLoanFeeTemp(loanFeeTempList, loan);
//        List<LoanFee> loanRepayFeeList = new ArrayList<>();
//        int period = oldLoanRepayList.size();
//        List<LoanRepay> loanRepays = new ArrayList();
//        Date date = new Date();
//        String name = user.getName();
//        for (Repayment repayment : repaymentList) {
//            LoanRepay loanRepay = new LoanRepay();
//            loanRepay.setLoanId(loanId);
//            loanRepay.setPeriod(period++);
//            loanRepay.setAmount(repayment.getPrincipal());
//            loanRepay.setInterest(repayment.getInterest());
//            loanRepay.setOutstanding(repayment.getOutstanding());
//            BigDecimal feeAmount = BigDecimal.ZERO;
//            feeAmount = loanRepayService.createFee(loan, user, loanFeeTempList, loanRepayFeeList, repayment, loanRepay, feeAmount);
//            loanRepay.setFeeAmount(feeAmount);
//            loanRepay.setTotalAmount(DecimalUtils.sumArr(loanRepay.getAmount(), loanRepay.getInterest(), loanRepay.getFeeAmount()));
//            loanRepay.setDueDate(repayment.getDueDate());
//            loanRepay.setStatus(LoanRepayStatus.LOANED);
//            loanRepay.setRemark(remark);
//            loanRepay.setCreateBy(name);
//            loanRepay.setCreateTime(date);
//            loanRepay.setUpdateBy(name);
//            loanRepay.setUpdateTime(date);
//            loanRepays.add(loanRepay);
//        }
//        int lastPeriod = oldLoanRepayList.size() + loanRepays.size() - 1;
//        List<LoanFee> oldFeeList = dao().query(LoanFee.class, Cnd.where("repayId", "=", lastLoanRepay.getId()).and("chargeNode", "=", FeeChargeNode.REPAY_NODE));
//        LoanRepay lastRepayment = loanRepays.remove(loanRepays.size() - 1);
//        lastLoanRepay.setStatus(LoanRepayStatus.LOANED);
//        lastLoanRepay.setPeriod(lastPeriod);
//        lastLoanRepay.setDueDate(lastRepayment.getDueDate());
//        lastLoanRepay.setAmount(lastRepayment.getAmount());
//        lastLoanRepay.setRepayAmount(null);
//        lastLoanRepay.setInterest(getRemainInterest(lastLoanRepay, lastRepayment));
//        lastLoanRepay.setRepayInterest(null);
//        loanRepayFeeList = getRepayLoanFeeList(oldFeeList, lastRepayment, loanRepayFeeList);
//        lastLoanRepay.setFeeAmount(getLastPeriodFeeAmount(loanRepayFeeList, lastPeriod));
//        lastLoanRepay.setRepayFeeAmount(null);
//        lastLoanRepay.setTotalAmount(DecimalUtils.sumArr(lastLoanRepay.getAmount(), lastLoanRepay.getInterest(), lastLoanRepay.getFeeAmount()));
//        lastLoanRepay.setRepayTotalAmount(null);
//        lastLoanRepay.setRemark(remark);
//        lastLoanRepay.setOverdueDays(0);
//        lastLoanRepay.setId(null);//原还款计划最后一期ID置空需要生产新的
//        oldLoanRepayList.addAll(oldLoanRepayList.size() - 1, loanRepays);
//        return loanRepayFeeList;
//    }

    private String getRemark(List<LoanRepay> loanRepayList) {
        String remark = loanRepayList.get(loanRepayList.size() - 1).getRemark();
        if (StringUtils.isNotEmpty(remark)) {
            remark = remark + "-展期";
        } else {
            remark = "展期";
        }
        return remark;
    }

    private CalculateMethodAboutDay getCalculateMethodAboutDay(String calculationMethod) {
        CalculateMethodAboutDay calculateMethodAboutDay = null;
        if (StringUtils.isNotEmpty(calculationMethod)) {
            calculateMethodAboutDay = CalculateMethodAboutDay.valueOf(calculationMethod);
        }
        return calculateMethodAboutDay;
    }

    private LoanRepayDateType getLoanRepayDateType(String repayDateType) {
        LoanRepayDateType loanRepayDateType = null;
        if (StringUtils.isNotEmpty(repayDateType) && LoanRepayDateType.isInLoanRepayDateType(repayDateType)) {
            loanRepayDateType = LoanRepayDateType.valueOf(repayDateType);
        }
        return loanRepayDateType;
    }

    private Date getNewLoanTime(LoanTermType termType, List<LoanRepay> loanRepayList) {
        LoanRepay lastLoanRepay = loanRepayList.get(loanRepayList.size() - 1);
        Date newLoanTime = lastLoanRepay.getDueDate();
        Calendar calendar = new GregorianCalendar();
        if (LoanTermType.DAYS.equals(termType) || LoanTermType.FIXED_DATE.equals(termType)) {
            calendar.setTime(newLoanTime);
            calendar.add(calendar.DATE, 1);
            newLoanTime = calendar.getTime();
        }
        return newLoanTime;
    }

    private BigDecimal getLastPeriodFeeAmount(List<LoanFee> loanRepayFeeList, int period) {
        BigDecimal feeAmount = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(loanRepayFeeList)) {
            for (LoanFee fee : loanRepayFeeList) {
                if (period == fee.getPeriod()) {
                    feeAmount = DecimalUtils.sum(feeAmount, fee.getFeeAmount());
                }
            }
        }
        return feeAmount;
    }

    private BigDecimal getRemainAmount(List<LoanRepay> loanRepayList) {
        BigDecimal remainAmount = BigDecimal.ZERO;
        for (LoanRepay loanRepay : loanRepayList) {
            BigDecimal tmp = DecimalUtils.sub(loanRepay.getAmount(), loanRepay.getRepayAmount());
            remainAmount = DecimalUtils.sum(remainAmount, tmp);
        }
        return remainAmount;
    }

    private BigDecimal getRemainInterest(LoanRepay old, LoanRepay newer) {
        BigDecimal remainInterest = DecimalUtils.sum(old.getInterest(), newer.getInterest());
        remainInterest = DecimalUtils.sub(remainInterest, old.getRepayInterest());
        return remainInterest;
    }

    private List<LoanFee> getRepayLoanFeeList(List<LoanFee> oldFeeList, LoanRepay newLoanRepay, List<LoanFee> newFeeList) {
        int period = newLoanRepay.getPeriod();
        if (CollectionUtils.isNotEmpty(oldFeeList) && CollectionUtils.isNotEmpty(newFeeList)) {
            newFeeList.forEach(fee -> {
                if (period == fee.getPeriod() && !FeeType.OVERDUE_FEE.equals(fee.getFeeType())) {
                    BigDecimal oldRemainFeeAmount = getOldRemainFeeAmount(oldFeeList, fee.getFeeType());
                    fee.setFeeAmount(DecimalUtils.sum(fee.getFeeAmount(), oldRemainFeeAmount));
                }
            });
            iteratorDelete(newFeeList.iterator(), FeeType.OVERDUE_FEE);
        }
        if (CollectionUtils.isNotEmpty(oldFeeList)) {
            oldFeeList.forEach(fee -> {
                if (FeeType.OVERDUE_FEE.equals(fee.getFeeType())) {
                    Date now = new Date();
                    LoanFee newFee = new LoanFee();
                    newFee.setId(null);
                    newFee.setRepayDate(null);
                    newFee.setDueDate(newLoanRepay.getDueDate());
                    newFee.setUpdateTime(now);
                    newFee.setFeeAmount(DecimalUtils.sub(fee.getFeeAmount(), fee.getRepayFeeAmount()));
                    newFee.setHistoryFeeAmount(newFee.getFeeAmount());
                    newFee.setPeriod(period);
                    newFee.setCreateTime(now);
                    newFee.setRepayFeeAmount(BigDecimal.ZERO);
                    newFee.setRepayId(null);
                    newFee.setFeeName(fee.getFeeName());
                    newFee.setFeeType(fee.getFeeType());
                    newFee.setLoanId(fee.getLoanId());
                    newFee.setStatus(LoanRepayStatus.LOANED);
                    newFee.setChargeNode(fee.getChargeNode());
                    newFeeList.add(newFee);
                }
            });
        }
        return newFeeList;
    }


    private void iteratorDelete(Iterator<LoanFee> it, FeeType feeType) {
        while (it.hasNext()) {
            LoanFee loanFee = it.next();
            if (feeType.equals(loanFee.getFeeType())) {
                it.remove();
            }
        }
    }

    private BigDecimal getOldRemainFeeAmount(List<LoanFee> oldFeeList, FeeType feeType) {
        BigDecimal oldRemainFeeAmount = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(oldFeeList)) {
            for (LoanFee fee : oldFeeList) {
                if (feeType.equals(fee.getFeeType())) {
                    oldRemainFeeAmount = DecimalUtils.sub(fee.getFeeAmount(), fee.getRepayFeeAmount());
                    break;
                }
            }
        }
        return oldRemainFeeAmount;
    }

    public Object queryLoanList(DataTableParam param) {
        String borrower = "";
        String dueDate = "";
        String isExtension = "";
        if (null != param.getSearchKeys()) {
            Map<String, String> keys = param.getSearchKeys();
            borrower = keys.get("borrserName");
            dueDate = keys.get("dueDate");
            isExtension = keys.get("isExtension");
        }
        Date beginDate = null;
        Date endDate = null;
        if (StringUtils.isNotEmpty(dueDate)) {
            String date[] = dueDate.split("to");
            if (null != date && date.length > 1) {
                beginDate = TimeUtils.formatDate("yyyy-MM-dd", date[0]);
                endDate = TimeUtils.formatDate("yyyy-MM-dd", date[1]);
            }
        }
        String sqlStr = "select distinct IFNULL(l.id,'--') AS 'id'," +
                "IFNULL(l.`code`,'--') AS 'code'," +
                "IFNULL(pt.name,'--') AS 'productTypeName'," +
                "IFNULL(u.name,'--') AS 'saleName'," +
                "(select lb.name from sl_loan_borrower lb where lb.loanId=l.id and lb.master=1) AS 'borrserName'," +
                "IFNULL(l.submitTime,'--') AS 'submitTime'," +
                "IFNULL(l.amount,'--') AS 'amount'," +
                "IFNULL(l.actualAmount,'--') AS 'actualAmount'," +
                "IFNULL(l.loanTime,'--') AS 'loanTime'," +
                "IFNULL(o.businessLine,'--') AS 'businessLine'," +
                "IFNULL(l.term,'--') AS 'term'," +
                "IFNULL(l.approveStatusDesc,'--') AS 'approveStatus'," +
                "IFNULL(o.name,'--') AS 'organizeName'," +
                "IFNULL(l.termType,'--') AS 'termType'," +
                "IFNULL(l.loanStatus,'--') AS 'loanStatus'," +
                "IFNULL(a.name,'--') AS 'agencyName'," +
                "IFNULL(t.productTempType,'--') AS 'productTempType'," +
                "IFNULL(lr.dueDate,'--') AS 'dueDate'," +
                " IFNULL(l.channelId,'--') AS 'channelId'," +
                " IFNULL(o.businessLine,'--') AS 'businessLine'," +
                " IFNULL(o.code,'--') AS 'orgCode'," +
                "IFNULL(le.loanId,'--') AS 'extensionLoanId'," +
                "IFNULL(extension.approvalStatusType,'--') AS 'approvalStatusType',"+
                "IFNULL(extension.id,'--') AS 'extensionId' "+
                "from sl_loan l " +
                "left join (select loanId,max(dueDate) dueDate from sl_loan_repay where status in ('LOANED','OVERDUE') GROUP by loanId) lr on l.id=lr.loanId " +
                "left join sl_business_user u on l.saleId=u.id " +
                "left join sl_product_type pt on l.productTypeId=pt.id " +
                "left join sl_business_organize o on u.organizeId=o.id " +
                "left join sl_business_agency a on o.agencyId = a.id " +
                "left join sl_product p on l.productId=p.id " +
                "left join sl_product_info_tmpl t on p.infoTmpId=t.id " +
                "left join sl_loan_extension le on l.id=le.loanId " +
                "left join (select * from (select se.extensionCode,se.id ,se.loanId,se.approvalStatusType from sl_extension se order by se.extensionCode desc)temp group by loanId)  extension on  l.id = extension.loanId "+
                "where 1=1 " +
                "and l.status='ABLE' " +
                "and t.productTempType in ('SHULOU','HONGBEN','GERENDAI','CHEDAI','RRC','SHULOUPLAT') " +
                "and CASE WHEN l.repayMethod in('BULLET_REPAYMENT','INTEREST_DAYS') THEN l.loanStatus IN ('LOANED', 'OVERDUE') WHEN l.repayMethod IN ('INTEREST_MONTHS') THEN l.loanStatus = 'LOANED' END ";
                //"and l.id not in (select se.loanId from sl_extension se where se.approvalStatusType in ('IN_EDIT','IN_APPROVAL'))  ";


        String countSqlStr = " SELECT  COUNT(DISTINCT l.id) AS 'number'  " +
                "from sl_loan l " +
                "left join (select loanId,max(dueDate) dueDate from sl_loan_repay where status in ('LOANED','OVERDUE') GROUP by loanId) lr on l.id=lr.loanId " +
                "left join sl_business_user u on l.saleId=u.id " +
                "left join sl_product_type pt on l.productTypeId=pt.id " +
                "left join sl_business_organize o on u.organizeId=o.id " +
                "left join sl_business_agency a on o.agencyId = a.id " +
                "left join sl_product p on l.productId=p.id " +
                "left join sl_product_info_tmpl t on p.infoTmpId=t.id " +
                "left join sl_loan_extension le on l.id=le.loanId " +
                "where 1=1 " +
                "and l.status='ABLE' " +
                "and t.productTempType in ('SHULOU','HONGBEN','GERENDAI','CHEDAI','RRC','SHULOUPLAT') " +
                "and CASE WHEN l.repayMethod in('BULLET_REPAYMENT','INTEREST_DAYS') THEN l.loanStatus IN ('LOANED', 'OVERDUE') WHEN l.repayMethod IN ('INTEREST_MONTHS') THEN l.loanStatus = 'LOANED' END ";
                //"and l.id not in (select se.loanId from sl_extension se  where se.approvalStatusType in ('IN_EDIT','IN_APPROVAL'))  ";

        if (org.apache.commons.lang.StringUtils.isNotEmpty(borrower)) {
            sqlStr += " AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
            countSqlStr += " AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
        }
        if (beginDate != null && endDate != null) {
            sqlStr += "and lr.dueDate between @beginDate and @endDate ";
            countSqlStr += " and lr.dueDate between @beginDate and @endDate ";
        }
        if ("Y".equals(isExtension)) {
            sqlStr += " and le.loanId is not null ";
            countSqlStr += " and le.loanId is not null";
        } else if ("N".equals(isExtension)) {
            sqlStr += " and le.loanId is null ";
            countSqlStr += " and le.loanId is null";
        }
        sqlStr += " order by lr.dueDate asc limit @start,@length ";
        Sql sql = Sqls.create(sqlStr);
        sql.setParam("borrower", '%' + borrower + '%');
        sql.setParam("beginDate", beginDate);
        sql.setParam("endDate", endDate);
        sql.setParam("start", param.getStart());
        sql.setParam("length", param.getLength());

        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("borrower", '%' + borrower + '%');
        countSql.setParam("beginDate", beginDate);
        countSql.setParam("endDate", endDate);

        sql.setCallback(new SqlCallback() {
            public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
                List<Map> list = new LinkedList<Map>();
                while (rs.next()) {
                    Map tmp = new HashMap();
                    tmp.put("id", rs.getString("id"));
                    tmp.put("code", rs.getString("code"));
                    tmp.put("productTypeName", rs.getString("productTypeName"));
                    tmp.put("saleName", loanService.getBusinessSource(rs.getString("channelId"), rs.getString("businessLine"), rs.getString("orgCode"), rs.getString("saleName")));
                    tmp.put("borrserName", rs.getString("borrserName"));
                    tmp.put("submitTime", rs.getString("submitTime"));
                    tmp.put("amount", rs.getString("amount"));
                    tmp.put("actualAmount", rs.getString("actualAmount"));
                    tmp.put("loanTime", rs.getString("loanTime"));
                    tmp.put("businessLine", rs.getString("businessLine"));
                    tmp.put("term", rs.getString("term"));
                    tmp.put("loanStatus", rs.getString("loanStatus"));
                    tmp.put("termType", rs.getString("termType"));
                    tmp.put("approveStatus", rs.getString("approveStatus"));
                    tmp.put("organizeName", rs.getString("organizeName"));
                    tmp.put("agencyName", rs.getString("agencyName"));
                    tmp.put("productTempType", rs.getString("productTempType"));
                    tmp.put("dueDate", rs.getString("dueDate"));
                    tmp.put("extensionLoanId", rs.getString("extensionLoanId"));
                    tmp.put("approvalStatusType",rs.getString("approvalStatusType"));
                    tmp.put("extensionId",rs.getString("extensionId"));
                    list.add(tmp);
                }
                return list;
            }
        });
        dao().execute(sql);
        List<Map> list = sql.getList(Map.class);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        if (null == list) {
            list = new ArrayList<>();
        }
        if (count < 1) {
            return new DataTables(param.getDraw(), count, count, list);
        }
        return new DataTables(param.getDraw(), count, count, list);
    }

    private List<List<OldLoanRepay>> queryOldLoanRepayListByExtension(Extension extension){
        List<OldLoanRepay> oldLoanRepayList = this.dao().query(OldLoanRepay.class, Cnd.where("loanId", "=", extension.getLoanId()).and("position","<=",extension.getPosition()).orderBy("position,period", "asc"));
        oldLoanRepayList.stream().forEach(r -> r.setId(r.getRepayId()));
        List<List<OldLoanRepay>> oldrepaylist =  groupByPosition(oldLoanRepayList);
//        if(CollectionUtils.isEmpty(oldrepaylist)){
        if(!ApprovalStatusType.APPROVED.equals(extension.getApprovalStatusType())){
            List<OldLoanRepay> currentLoanRepays = getOldLoanRepayRecode(extension.getLoanId());
            oldrepaylist.add(currentLoanRepays);
        }
        return oldrepaylist;
    }
    private List<LoanRepay> queryCurrentLoanRepayListByExtension(Extension extension){
        List<OldLoanRepay> oldLoanRepayList = this.dao().query(OldLoanRepay.class, Cnd.where("loanId", "=", extension.getLoanId()).and("position","=",extension.getPosition()+1).orderBy("position,period", "asc"));
        List<LoanRepay> loanRepays = new ArrayList<LoanRepay>();
        if(CollectionUtils.isEmpty(oldLoanRepayList)){
            loanRepays =  loanRepayService.queryLoanRepayByLoanId(extension.getLoanId());
        }else{
            for(OldLoanRepay oldLoanRepay : oldLoanRepayList){
                loanRepays.add(getLoanRepayByOldLoanRepay(oldLoanRepay));
            }
        }
        int position = getExtensionCount(extension.getLoanId())-1;
        loanRepays.stream().forEach(r->{
            r.setPosition( position);
            r.setRepayId(r.getId());
        });
        return loanRepays;
    }
    private List<LoanExtension> getLoanExtensionListByExtension(Extension extension){
        List<LoanExtension> loanExtensions =  this.dao().query(LoanExtension.class, Cnd.where("loanId", "=", extension.getLoanId()).and("position","<=",extension.getPosition()).orderBy("position", "asc"));
        if(CollectionUtils.isEmpty(loanExtensions)){
            loanExtensions = new ArrayList<LoanExtension>();
        }
        if(!ApprovalStatusType.APPROVED.equals(extension.getApprovalStatusType())){
            loanExtensions.add(extension.getLoanExtension());
        }
        List<Extension> extensionList = this.dao().query(Extension.class,Cnd.where("loanId","=",extension.getLoanId()).asc("extensionCode"));
        for(LoanExtension loanExtension1: loanExtensions){
            if(StringUtils.isNotEmpty(loanExtension1.getExtensionId())){
                for(Extension extension1 : extensionList){
                    if(loanExtension1.getExtensionId().equalsIgnoreCase(extension1.getId())){
                        loanExtension1.setExtensionCode(extension1.getExtensionCode());
                        break;
                    }
                }
            }
        }
        return loanExtensions;
    }

    public NutMap queryRepayByExtension(Extension extension){
        NutMap result = new NutMap();
        List<Extension> rejecExtensionList = getExtensionListByReject(extension.getLoanId(),extension.getExtensionCode());
        if(ApprovalStatusType.REJECT.equals(extension.getApprovalStatusType())){
            rejecExtensionList.remove(rejecExtensionList.size()-1);
        }
        result.setv("rejectExtensionList",rejecExtensionList);
        result.addv("extension",extension);
        result.setv("oldRepayList", queryOldLoanRepayListByExtension(extension));
        result.setv("loanExtensionList", getLoanExtensionListByExtension(extension));
        result.setv("loanExtension", extension.getLoanExtension());
        if(ApprovalStatusType.APPROVED.equals(extension.getApprovalStatusType())){
            NutMap nutMap = new NutMap();
            nutMap.setv("data",queryCurrentLoanRepayListByExtension(extension));
            result.setv("newRepayList",nutMap);
        }else {
            result.setv("newRepayList", generateLoanRepay(extension.getLoanId(),extension.getTermType(),extension.getTerm(),extension.getLoanLimitType(),
                    LoanLimitType.FIX_AMOUNT.equals(extension.getLoanLimitType())?extension.getInterestAmount():extension.getInterestRate(),
                    extension.getRepayMethod(),extension.getCalculationMethod(),extension.getRepayDateType()));
        }
        return result;
    }
    public NutMap queryRepayByLoanId(String loanId) {
        LoanExtension loanExtension = this.dao().fetch(LoanExtension.class, Cnd.where("loanId", "=", loanId));
        NutMap resultMap = new NutMap();
        List<LoanRepay> newLoanRepayList = loanRepayService.queryLoanRepayByLoanId(loanId);
        newLoanRepayList.stream().forEach(r->r.setPosition(getExtensionCount(loanId)));
        Extension extension  = extensionService.getCurrentExtensionByLoanId(loanId);
        if(extension!=null){
            if(ApprovalStatusType.IN_EDIT.equals(extension.getApprovalStatusType())) {
                resultMap.setv("extension", extension);
                resultMap.setv("status","IN_EDIT");
            }
            /*else if(ApprovalStatusType.REJECT.equals(extension.getApprovalStatusType())){
                resultMap.setv("status","REJECT");
                resultMap.setv("rejectExtensionList",getExtensionList(extension.getLoanId()));
                *//*List<LoanRepay> extensionLoanRepayList  = (List<LoanRepay>)generateLoanRepay(extension.getLoanId(),extension.getTermType(),extension.getTerm(),extension.getLoanLimitType(),
                        LoanLimitType.FIX_AMOUNT.equals(extension.getLoanLimitType())?extension.getInterestAmount():extension.getInterestRate(),
                        extension.getRepayMethod(),extension.getCalculationMethod(),extension.getRepayDateType()).get("data");
                resultMap.setv("rejectRepayList",copyLoanRepay(loanId,extensionLoanRepayList));*//*
            }*/
            resultMap.setv("rejectExtensionList",getExtensionListByReject(extension.getLoanId(),"YWZQ999999"));
        }
        if (loanExtension == null) {
            return resultMap.setv("ok", true).setv("oldRepayList", groupByLoanRepays(newLoanRepayList));
        }
        if (loanExtension.getInterestRate() != null) {
            loanExtension.setInterestRate(loanExtension.getInterestRate());
        }
        //List<LoanExtension> loanExtensionList = this.dao().query(LoanExtension.class, Cnd.where("loanId", "=", loanId).orderBy("position", "asc"));
        List<LoanExtension> loanExtensionList = queryLoanExtension(loanId);
        List<OldLoanRepay> oldLoanRepayList = this.dao().query(OldLoanRepay.class, Cnd.where("loanId", "=", loanId).orderBy("position,period", "asc"));
        oldLoanRepayList.stream().forEach(r -> r.setId(r.getRepayId()));
        List<List<OldLoanRepay>> oldRepayList = groupByPosition(oldLoanRepayList);
        //如果是正在编辑中，则需要为编辑状态
        resultMap.setv("ok", true);
        resultMap.setv("oldRepayList", oldRepayList);
        resultMap.setv("newRepayList", newLoanRepayList);
        resultMap.setv("loanExtension", loanExtension);
        resultMap.setv("loanExtensionList", loanExtensionList);

        return resultMap;
    }

    private List<LoanExtension> queryLoanExtension(String loanId){
        List<LoanExtension> loanExtensionList = this.dao().query(LoanExtension.class, Cnd.where("loanId", "=", loanId).orderBy("position", "asc"));
        List<Extension> extensionList = this.dao().query(Extension.class,Cnd.where("loanId","=",loanId).asc("extensionCode"));
        for(LoanExtension loanExtension1: loanExtensionList){
            if(StringUtils.isNotEmpty(loanExtension1.getExtensionId())){
                for(Extension extension1 : extensionList){
                    if(loanExtension1.getExtensionId().equalsIgnoreCase(extension1.getId())){
                        loanExtension1.setExtensionCode(extension1.getExtensionCode());
                        break;
                    }
                }
            }
        }
        return loanExtensionList;
    }
    public LoanExtension getLoanExtension(String loanId,
                                           LoanTermType termType,
                                           String term,
                                           LoanLimitType loanLimitType,
                                           BigDecimal interest,
                                           String enterpriseExplain,
                                           String enterpriseAgreement, String repayMethod, String calculationMethod, String repayDateType) {
        User user = ShiroSession.getLoginUser();
        int position = getExtensionCount(loanId);
        LoanExtension loanExtension = new LoanExtension();
        Date nowDate = new Date();
        loanExtension.setLoanId(loanId);
        loanExtension.setTermType(termType);
        loanExtension.setTerm(term);
        loanExtension.setLoanLimitType(loanLimitType);
        if (LoanLimitType.FIX_AMOUNT.equals(loanLimitType)) {
            loanExtension.setInterestAmount(interest);
        } else {
            loanExtension.setInterestRate(interest);
        }
        loanExtension.setUpdateBy(user.getName());
        loanExtension.setUpdateTime(nowDate);
        loanExtension.setCreateBy(user.getName());
        loanExtension.setCreateTime(nowDate);
        loanExtension.setEnterpriseExplain(enterpriseExplain);
        loanExtension.setEnterpriseAgreement(enterpriseAgreement);
        loanExtension.setPosition(position);
        loanExtension.setRepayMethod(repayMethod);
        loanExtension.setCalculationMethod(calculationMethod);
        loanExtension.setRepayDateType(repayDateType);
        return loanExtension;
    }

    private List<List<OldLoanRepay>> groupByPosition(List<OldLoanRepay> oldLoanRepays) {
        List<List<OldLoanRepay>> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(oldLoanRepays)) {
            int position = 0;
            List<OldLoanRepay> groupList = new ArrayList<>();
            groupList.add(oldLoanRepays.get(0));
            for (int i = 1; i < oldLoanRepays.size(); i++) {
                if (position != oldLoanRepays.get(i).getPosition()) {
                    list.add(groupList);
                    groupList = new ArrayList<>();
                    position = oldLoanRepays.get(i).getPosition();
                }
                groupList.add(oldLoanRepays.get(i));
            }
            list.add(groupList);
        }
        return list;
    }

    private List<List<LoanRepay>> groupByLoanRepays(List<LoanRepay> loanRepays) {
        List<List<LoanRepay>> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(loanRepays)) {
            list.add(loanRepays);
        }
        return list;
    }
    private LoanRepay getLoanRepayByOldLoanRepay(OldLoanRepay oldLoanRepay){
        LoanRepay loanRepay = new LoanRepay();
        loanRepay.setFeeAmount(oldLoanRepay.getFeeAmount());
        loanRepay.setStatus(oldLoanRepay.getStatus());
        loanRepay.setInterest(oldLoanRepay.getInterest());
        loanRepay.setRepayInterest(oldLoanRepay.getRepayInterest());
        loanRepay.setTotalAmount(oldLoanRepay.getTotalAmount());
        loanRepay.setRepayDate(oldLoanRepay.getRepayDate());
        loanRepay.setOutstanding(oldLoanRepay.getOutstanding());
        loanRepay.setAmount(oldLoanRepay.getAmount());
        loanRepay.setRepayTotalAmount(oldLoanRepay.getRepayTotalAmount());
        loanRepay.setDueDate(oldLoanRepay.getDueDate());
        loanRepay.setPeriod(oldLoanRepay.getPeriod());
        loanRepay.setRemark(oldLoanRepay.getRemark());
        loanRepay.setRepayAmount(oldLoanRepay.getRepayAmount());
        loanRepay.setRepayFeeAmount(oldLoanRepay.getRepayFeeAmount());
        loanRepay.setLoanId(oldLoanRepay.getLoanId());
        loanRepay.setOverdueDays(oldLoanRepay.getOverdueDays());
        loanRepay.setRepayOtherFee(oldLoanRepay.getRepayOtherFee());
        loanRepay.setOtherFee(oldLoanRepay.getOtherFee());
        loanRepay.setUpdateBy(oldLoanRepay.getUpdateBy());
        loanRepay.setUpdateTime(oldLoanRepay.getUpdateTime());
        loanRepay.setCreateBy(oldLoanRepay.getCreateBy());
        loanRepay.setCreateTime(oldLoanRepay.getCreateTime());
        return loanRepay;
    }

}

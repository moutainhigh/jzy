package com.kaisa.kams.components.service.base;

import com.kaisa.kams.components.service.ChannelService;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.DecimalUtils;
import com.kaisa.kams.components.view.report.*;
import com.kaisa.kams.enums.LoanStatus;
import com.kaisa.kams.models.Channel;
import com.kaisa.kams.models.LoanFeeRecord;
import com.kaisa.kams.models.LoanRepayRecord;

import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.service.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by wangqx on 2017/8/7.
 */
@IocBean(fields = "dao")
public class ReportUtilsService extends Service {

    @Inject
    private ChannelService channelService;

    public String getLoanActualClearedDate(String status, String loanId) {
        if (status.equals(LoanStatus.CLEARED.toString())) {
            String query = "select max(repayDate) repayDate from sl_loan_repay where loanId=@loanId";
            Sql sql = Sqls.create(query);
            sql.setParam("loanId", loanId);
            sql.setCallback(Sqls.callback.timestamp());
            dao().execute(sql);
            Date repayDate = sql.getObject(Date.class);
            if (null != repayDate) {
                return DateUtil.formatDateToString(repayDate);
            }
        }
        return "--";
    }

    public Date getLoanActualClearedDate(String loanId) {
        String query = "select max(repayDate) repayDate from sl_loan_repay where loanId=@loanId";
        Sql sql = Sqls.create(query);
        sql.setParam("loanId", loanId);
        sql.setCallback(Sqls.callback.timestamp());
        dao().execute(sql);
        return sql.getObject(Date.class);
    }

    public BusinessReportCommonData getCommonInfo(String loanId) {
        BusinessReportCommonData data = BusinessReportCommonData.create();
        BusinessReportFeeData feeData = getFeeInfo(loanId);
        BusinessReportRepayData repayData = getRepayInfo(loanId);
        data.setFeeData(feeData);
        data.setRepayData(repayData);
        data.setActualTotalFeeAmount(feeData.getActualServiceTotalFeeAmount().add(repayData.getActualInterest()));
        data.setActualTotalAmount(repayData.getActualAmount().add(repayData.getActualInterest()));
        return data;
    }

    public BusinessReportFeeData getFeeInfo(String loanId) {
        BusinessReportFeeData data = BusinessReportFeeData.create();
        String query = "SELECT" +
                " IFNULL(sum(IF(feeType='PREPAYMENT_FEE_RATE',feeAmount,0)), 0) prePaymentFeeAmount," +
                " IFNULL(sum(IF(feeType='PREPAYMENT_FEE_RATE',repayFeeAmount,0)), 0) actualPrePaymentFeeAmount," +
                " IFNULL(sum(IF(feeType='OVERDUE_FEE',repayFeeAmount,0)), 0) actualOverdueFeeAmount," +
                " IFNULL(sum(feeAmount), 0) totalServiceFeeAmount," +
                " IFNULL(sum(repayFeeAmount), 0) actualServiceTotalFeeAmount," +
                " IFNULL(sum(IF(feeType='SERVICE_FEE',feeAmount,0)), 0) serviceFeeAmount," +
                " IFNULL(sum(IF(feeType='SERVICE_FEE',repayFeeAmount,0)), 0) actualServiceFeeAmount" +
                " FROM sl_loan_fee WHERE loanId = @loanId";
        Sql sql = Sqls.create(query);
        sql.setParam("loanId", loanId);
        sql.setCallback(Sqls.callback.entity());
        sql.setEntity(dao().getEntity(BusinessReportFeeData.class));
        dao().execute(sql);
        BusinessReportFeeData queryObj = sql.getObject(BusinessReportFeeData.class);
        if (null != queryObj) {
            return queryObj;
        }
        return data;
    }

    private BusinessReportRepayData getRepayInfo(String loanId) {
        BusinessReportRepayData data = BusinessReportRepayData.create();
        String query = "SELECT IFNULL(sum(repayAmount), 0.00) actualAmount," +
                " IFNULL(sum(repayInterest), 0.00) actualInterest" +
                " FROM sl_loan_repay WHERE loanId = @loanId";
        Sql sql = Sqls.create(query);
        sql.setParam("loanId", loanId);
        sql.setCallback(Sqls.callback.entity());
        sql.setEntity(dao().getEntity(BusinessReportRepayData.class));
        dao().execute(sql);
        BusinessReportRepayData queryObj = sql.getObject(BusinessReportRepayData.class);
        if (null != queryObj) {
            return queryObj;
        }
        return data;
    }

    public static String getInterestMode(String loanLimitType,String loanTermType){
        if("FIX_AMOUNT".equals(loanLimitType)){
            if("DAYS".equals(loanTermType) || "FIXED_DATE".equals(loanTermType)){
                return "金额计息(元)/天";
            }
            if("MOTHS".equals(loanTermType)){
                return "金额计息(元)/月";
            }

        }else if("FIX_RATE".equals(loanLimitType)){
            if("DAYS".equals(loanTermType) || "FIXED_DATE".equals(loanTermType)){
                return "比例计息(%)/天";
            }
            if("MOTHS".equals(loanTermType)){
                return "比例计息(%)/月";
            }
        }
        return null;
    }

    public BusinessReportLastRepayData getLastRepayData(String repayId) {
        BusinessReportLastRepayData data = BusinessReportLastRepayData.create();
        LoanRepayRecord record = getLastRepayRecord(repayId);
        if (null != record) {
            data.setLastRepayDate(DateUtil.formatDateToString(record.getRepayDate()));
            data.setLastRepayAmount(getLastRepayAmount(record));
        }
        return data;
    }

    private BigDecimal getLastRepayAmount(LoanRepayRecord record) {
        BigDecimal feeAmount = (BigDecimal)dao().func2(LoanFeeRecord.class, "sum",
                "repayFeeAmount", Cnd.where("repayRecordId","=",record.getId()));
        return DecimalUtils.sum(feeAmount,record.getRepayInterest());
    }

    private LoanRepayRecord getLastRepayRecord(String repayId) {
        if (StringUtils.isEmpty(repayId)) {
            return null;
        }
        String query = "select * from sl_loan_repay_record where repayId=@repayId " +
                "order by repayDate desc limit 0,1";
        Sql sql = Sqls.create(query);
        sql.setParam("repayId", repayId);
        sql.setCallback(Sqls.callback.entity());
        sql.setEntity(dao().getEntity(LoanRepayRecord.class));
        dao().execute(sql);
        return sql.getObject(LoanRepayRecord.class);
    }

    public Date getLastRepayDate(String repayId) {
        LoanRepayRecord record = getLastRepayRecord(repayId);
        if (null != record) {
            return record.getRepayDate();
        }
        return null;
    }

    public String[] getChannelInfo(String channelId) {
        String result[] = null;
        if("--".equals(channelId)|| Strings.isEmpty(channelId)) {
            result = new String[2];
            result[0] = "自营";
            result[1] = "--";
            return result;
        }
        Channel channel = channelService.fetch(channelId);
        if(channel != null){
            if(channel.getChannelType().equals("0")){
                result = new String[2];
                result[0] = "自营";
                result[1] = channel.getName();
            }
        }
        return result;
    }

    public FinanceReportRepayData getFinanceRepayInfo(String loanId) {
        FinanceReportRepayData data = new FinanceReportRepayData();
        String query = "SELECT IFNULL(sum(amount), 0.00) receivablePrincipal, " +
                " IFNULL(sum(lr.repayAmount), 0.00) repayAmount, " +
                " IFNULL(sum(lr.interest), 0.00) receivableInterest, " +
                " IFNULL(sum(lr.repayInterest), 0.00) repayInterest, " +
                " IFNULL(sum(IF(lf.repayDate IS NULL,lf.feeAmount,lf.repayFeeAmount)), 0.00) receivableFee, " +
                " IFNULL(sum(lf.repayFeeAmount), 0.00) repayFee " +
                " FROM sl_loan_repay lr LEFT JOIN sl_loan_fee lf ON lf.loanId = lr.loanId WHERE lr.loanId = @loanId";
        Sql sql = Sqls.create(query);
        sql.setParam("loanId", loanId);
        sql.setCallback(Sqls.callback.entity());
        sql.setEntity(dao().getEntity(FinanceReportRepayData.class));
        dao().execute(sql);
        FinanceReportRepayData queryObj = sql.getObject(FinanceReportRepayData.class);
        if (null != queryObj) {
            queryObj.setOherVal();
            return queryObj;
        }
        return data;
    }


}

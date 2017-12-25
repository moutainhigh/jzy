package com.kaisa.kams.components.service.report;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.params.report.DataTableBusinessReportParam;
import com.kaisa.kams.components.params.report.DataTableFinanceReportParam;
import com.kaisa.kams.components.service.LoanService;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.ParamUtil;
import com.kaisa.kams.components.utils.ProductTypesUtils;
import com.kaisa.kams.components.utils.TimeUtils;
import com.kaisa.kams.components.utils.report.ReportUtils;
import com.kaisa.kams.enums.LoanStatus;
import com.kaisa.kams.models.report.BillBusinessReport;
import com.kaisa.kams.models.BillReport;
import com.kaisa.kams.models.report.ComprehensiveReport;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.ProductRate;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangqx on 2017/10/17.
 */
@IocBean(fields = "dao")
public class BillLoanReportService extends Service {

    @Inject
    private LoanService loanService;

    public DataTables billReportPage(DataTableFinanceReportParam param){

        List<ProductRate> productRateList = dao().query(ProductRate.class,null);
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String[] loanStatus = {"LOANED", "OVERDUE","CLEARED"};
        if (org.apache.commons.lang.StringUtils.isNotEmpty(param.getStatus()) && ArrayUtils.isNotEmpty(param.getLoanedStatus())) {
            loanStatus =  param.getLoanedStatus();
        }
        String sqlStr = "SELECT " +
                "  slr.amount AS 'parValue', " +
                "  slr.interest AS 'billInterest', " +
                "  sbl.discountTime  AS 'businessDate', " +
                "  sbl.accountName, " +
                "  sbl.accountBank, " +
                "  sbl.accountNo, " +
                "  sbl.totalAmount AS 'totalAmount' , " +
                "  sbl.intermediaryName AS 'intermediaryName' , " +
                "  sl.CODE  AS 'businessOrderNo', " +
                "  sl.loanTime , " +
                "  sblr.loanId, " +
                "  sblr.repayId, " +
                "  sblr.billNo AS 'billCode', " +
                "  sblr.drawTime AS 'billingDate', " +
                "  sblr.payer AS 'drawer', " +
                "  sblr.riskRank, " +
                "  sblr.payee, " +
                "  sblr.bankName, " +
                "  sblr.bankAddress, " +
                "  sblr.overdueDays AS 'adjustedDay', " +
                "  sblr.position, " +
                "  sblr.disDate, " +
                "  sblr.disDays, " +
                "  sl.loanStatus AS 'status', " +
                "  ls.name AS 'loanSubject', "+
                "  slr.dueDate AS 'expireDate', " +
                "  sblr.actualDueDate AS 'actualExpireDate', " +
                "  slr.repayDate AS 'billReceivablesDate' , " +
                " TIMESTAMPDIFF( " +
                "    DAY, " +
                "    CURDATE(), " +
                "    slr.dueDate " +
                " ) AS 'distanceExpireDay' , " +
                " sblr.costRate, " +
                " sblr.intermediaryFee AS 'intermediaryTotalFee', " +
                " sp.NAME AS 'billType' , " +
                " sbu.NAME AS 'operator'," +
                " IFNULL(slr.feeAmount,0.0 ) AS 'otherFee' ," +
                " slb.name AS 'discountProposer' , " +
                " IFNULL(slr.amount,0.00) AS 'receivablePrincipal', " +
                " IFNULL(slr.repayAmount,0.00) AS 'repayAmount', " +
                " IFNULL(slr.repayInterest,0.00) AS 'repayInterest', " +
                " IFNULL(slr.repayFeeAmount,0.00) AS 'repayFee' , " +
                " IFNULL(slr.feeAmount,0.00) AS 'receivableFee' " +
                " FROM sl_bill_loan sbl "+
                " LEFT JOIN sl_loan sl ON sbl.loanId = sl.id" +
                " LEFT JOIN sl_bill_loan_repay sblr on sblr.loanId = sl.id" +
                " LEFT JOIN sl_product_type spt on sl.productTypeId = spt.id" +
                " LEFT JOIN sl_product sp on sl.productId = sp.id " +
                " LEFT JOIN sl_business_user sbu on sl.saleId = sbu.id " +
                " LEFT JOIN sl_loan_repay slr on sblr.repayId = slr.id " +
                " LEFT JOIN sl_loan_borrower slb on slb.loanId = sl.id "+
                "  left join sl_loan_subject ls on ls.id = sl.loanSubjectId" +
                " where sl.status='ABLE' "+
                " AND sbl.discountTime is not null" +
                " AND slr.dueDate is not null  " +
                " AND sl.loanTime>=@loanBeginDate  AND sl.loanTime<=@loanEndDate " +
                " AND sl.loanStatus in(@loanStatus) "+
                " AND spt.name = @productTypeName ";
        String countSqlStr = "SELECT  COUNT( sl.id ) AS 'number' " +
                "  FROM " +
                "  sl_bill_loan sbl " +
                " LEFT JOIN sl_loan sl ON sbl.loanId = sl.id" +
                " LEFT JOIN sl_bill_loan_repay sblr on sblr.loanId = sl.id" +
                " LEFT JOIN sl_product_type spt on sl.productTypeId = spt.id"+
                " LEFT JOIN sl_product sp on sl.productId = sp.id " +
                " LEFT JOIN sl_business_user sbu on sl.saleId = sbu.id "+
                " LEFT JOIN sl_loan_repay slr on sblr.repayId = slr.id "+
                " LEFT JOIN sl_loan_borrower slb on slb.loanId = sl.id "+
                "  left join sl_loan_subject ls on ls.id = sl.loanSubjectId" +
                " where sl.status='ABLE'" +
                " AND sbl.discountTime is not null " +
                " AND slr.dueDate is not null  " +
                " AND sl.loanTime>=@loanBeginDate  AND sl.loanTime<=@loanEndDate "+
                " AND sl.loanStatus in(@loanStatus) "+
                " AND spt.name = @productTypeName ";
        String loanSubject = param.getLoanSubject();
        Date loanBeginDate = param.getBeginDateTime();
        String productTypeName = param.getProductTypeName();
        Date loanEndDate = param.getEndDateTime();
        if (StringUtils.isNotEmpty(loanSubject)) {
            sqlStr += " AND  ls.id=@loanSubject ";
            countSqlStr += " AND  ls.id=@loanSubject ";
        }
        sqlStr += " order by sl.loanTime,sl.code";

        Sql sql = Sqls.create(sqlStr);
        sql.setParam("loanStatus", loanStatus);
        sql.setParam("loanSubject", loanSubject);
        sql.setParam("loanBeginDate",loanBeginDate);
        sql.setParam("loanEndDate",loanEndDate);
        sql.setParam("productTypeName",productTypeName);
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(BillReport.class));
        dao().execute(sql);
        List<BillReport> list = sql.getList(BillReport.class);
        if (CollectionUtils.isNotEmpty(list)) {
            for (BillReport billReport:list) {
                billReport.dataConversion(productRateList);
                Integer distanceExpireDay = billReport.getDistanceExpireDay();
                if (null != billReport.getBillReceivablesDate()) {
                    billReport.setDistanceExpireDay(null);
                }
                if (billReport.getBillReceivablesDate() == null && distanceExpireDay != null && distanceExpireDay <= 0) {
                    Integer adjustedDay = billReport.getAdjustedDay();
                    if (null != adjustedDay) {
                        billReport.setDistanceExpireDay(adjustedDay+distanceExpireDay);
                    }
                }
                billReport.setRepayTotal((billReport.getRepayAmount() == null ? new BigDecimal(0):billReport.getRepayAmount()).add(billReport.getRepayInterest() == null ? new BigDecimal(0):billReport.getRepayInterest()).add(billReport.getRepayFee() == null ? new BigDecimal(0):billReport.getRepayFee()));
                billReport.setOtherVal();
                if(LoanStatus.CLEARED.name().equals(billReport.getStatus())){
                    billReport.setOutstandingAmount(ReportUtils.bigDecimalCompareClear(billReport.getOutstandingAmount()));
                    billReport.setOutstandingFee(ReportUtils.bigDecimalCompareClear(billReport.getOutstandingFee()));
                    billReport.setOutstandingTotal(ReportUtils.bigDecimalCompareClear(billReport.getOutstandingTotal()));
                }
            }
        }
        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("loanStatus", loanStatus);
        countSql.setParam("loanSubject", loanSubject);
        countSql.setParam("loanBeginDate",loanBeginDate);
        countSql.setParam("loanEndDate",loanEndDate);
        countSql.setParam("productTypeName",productTypeName);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();

        return new DataTables(param.getDraw(),count,count,list);
    }


    public DataTables billBusinessReportPage(DataTableBusinessReportParam param){
        Map<String,Object> addParam = new HashMap<>();
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
//        String billType;
//        String submitter;
//        String submitDate;
//        String status;
//        String channelId;
//        String expireDate;
//        String actualLoanDate;
//        String saleName;
//        String clearedDate;
//        String loanStatus;
//        if (null != param.getSearchKeys()) {
//            Map<String,String> keys = param.getSearchKeys();
//            billType = keys.get("billType");
//            submitter = keys.get("submitter");
//            submitDate =  keys.get("submitDate");
//            status =  keys.get("status");
//            channelId =  keys.get("channelId");
//            expireDate =  keys.get("expireDate");
//            actualLoanDate =  keys.get("actualLoanDate");
//            saleName =  keys.get("saleName");
//            clearedDate = keys.get("clearedDate");
//            loanStatus =  keys.get("loanStatus");
//        } else {
//            return null;
//        }

        String sqlStr = "SELECT " +
                " sl. CODE AS 'businessOrderNo',  " +
                " sp. NAME AS 'billType',  " +
                " sblr.billNo AS 'billCode',  " +
                " su.name AS 'submitter',  " +
                " sl.submitTime AS 'submitDate',  " +
                " slr.amount AS 'billAmount',  " +
                " sblr.drawTime AS 'billingDate',  " +
                " date_add( " +
                "    slr.dueDate, " +
                "    INTERVAL sblr.overdueDays DAY " +
                "  ) AS 'expireDate',  " +
                " sls. NAME AS 'loanSubject',  " +
                " sl.loanTime  AS 'actualLoanDate',   " +
                " slr.amount-slr.interest AS 'payAmount',  " +
                " slb. NAME AS 'discountPerson',  " +
                " sblr.costRate AS 'cost',  " +
                " sblr.disDays AS 'discountDays',  " +
                " slr.interest AS 'billInterest',  " +
                " sblr.intermediaryFee AS 'intermediaryTotalFee',  " +
                " sbl.intermediaryName AS 'intermediaryName',  " +
                " sbu. NAME AS 'saleName',  " +
                " slr.status AS 'status',  " +
                " sl.loanStatus AS 'loanStatus',  " +
                " sl.channelId AS 'channelId',  " +
                " o.businessLine AS 'businessLine',  " +
                " slp.profit as profit ,"+
                " sl.clearDate AS clearedDate, " +
                " o. CODE AS 'orgCode'  " +
                "FROM  " +
                "sl_bill_loan_repay sblr  " +
                "LEFT JOIN sl_loan sl ON  sl.id = sblr.loanId  " +
                "LEFT JOIN sl_bill_loan sbl ON sbl.loanId = sblr.loanId  " +
                "LEFT JOIN sl_product_type spt ON sl.productTypeId = spt.id  " +
                "LEFT JOIN sl_product sp ON sl.productId = sp.id  " +
                "LEFT JOIN sl_business_user sbu ON sl.saleId = sbu.id  " +
                "LEFT JOIN sl_loan_repay slr ON sblr.repayId = slr.id  " +
                "LEFT JOIN sl_loan_borrower slb ON slb.loanId = sl.id  " +
                "LEFT JOIN sl_user su ON su.id = sl.applyId  " +
                "LEFT JOIN sl_loan_subject sls ON sls.id = sl.loanSubjectId  " +
                "LEFT JOIN sl_business_organize o ON sbu.organizeId = o.id  " +
                "LEFT JOIN sl_loan_profit slp on sl.id = slp.loanId "+
                "WHERE  " +
                " sl. STATUS = 'ABLE'  " ;
        String countSqlStr = "SELECT  COUNT( sblr.id ) AS 'number' " +
                "  FROM " +
                "sl_bill_loan_repay sblr  " +
                "LEFT JOIN sl_loan sl ON  sl.id = sblr.loanId  " +
                "LEFT JOIN sl_bill_loan sbl ON sbl.loanId = sblr.loanId  " +
                "LEFT JOIN sl_product_type spt ON sl.productTypeId = spt.id  " +
                "LEFT JOIN sl_product sp ON sl.productId = sp.id  " +
                "LEFT JOIN sl_business_user sbu ON sl.saleId = sbu.id  " +
                "LEFT JOIN sl_loan_repay slr ON sblr.repayId = slr.id  " +
                "LEFT JOIN sl_loan_borrower slb ON slb.loanId = sl.id  " +
                "LEFT JOIN sl_user su ON su.id = sl.applyId  " +
                "LEFT JOIN sl_loan_subject sls ON sls.id = sl.loanSubjectId  " +
                "LEFT JOIN sl_business_organize o ON sbu.organizeId = o.id  " +
                " where sl.status='ABLE' " ;
        String billType = param.getBillType();
        if (StringUtils.isNotEmpty(billType)){
            sqlStr+=" AND sp. NAME = @billType ";
            countSqlStr+=" AND sp. NAME = @billType ";
        }else {
            List<Map<String,String>> pt = ProductTypesUtils.getProductTypeList();
            for(Map<String,String> m:pt){
                String codeVal = m.get("code");
                String nameVal = m.get("name");
                if(StringUtils.isNotEmpty(codeVal) && ("PIAOJU").equals(codeVal) && StringUtils.isNotEmpty(nameVal)){
                    sqlStr+=" AND spt. NAME = '"+nameVal+"' ";
                    countSqlStr+=" AND spt. NAME = '"+nameVal+"' ";
                }
            }
        }
        String submitter = param.getSubmitter();
        if (StringUtils.isNotEmpty(submitter)){
            sqlStr+=" AND su.name like @submitter ";
            countSqlStr+=" AND su.name like @submitter ";
        }
        Date beginSubmitDate = param.getBeginSubmitDate();
        Date endSubmitDate = param.getEndSubmitDate();
        if(null != beginSubmitDate){
            sqlStr+=" AND sl.submitTime >= @beginSubmitDate ";
            countSqlStr+=" AND sl.submitTime >= @beginSubmitDate ";
        }
        if(null != endSubmitDate){
            sqlStr+=" AND sl.submitTime <= @endSubmitDate ";
            countSqlStr+=" AND sl.submitTime <= @endSubmitDate ";
        }

        addParam.put("beginSubmitDate",beginSubmitDate);
        addParam.put("endSubmitDate",endSubmitDate);

        Date beginActualClearedDate = param.getBeginClearedDate();
        Date endActualClearedDate = param.getEndClearedDate();
        if(null != beginActualClearedDate){
            sqlStr+=" AND sl.clearDate >= @beginActualClearedDate ";
            countSqlStr+=" AND sl.clearDate >= @beginActualClearedDate ";
        }
        if(null != endActualClearedDate){
            sqlStr+=" AND sl.clearDate <= @endActualClearedDate ";
            countSqlStr+=" AND sl.clearDate <= @endActualClearedDate ";
        }

        addParam.put("beginActualClearedDate",beginActualClearedDate);
        addParam.put("endActualClearedDate",endActualClearedDate);

        String status =param.getStatus();
        if (StringUtils.isNotEmpty(status)){
            sqlStr+=" AND slr.status=@status ";
            countSqlStr+=" AND slr.status=@status ";
        }

        String loanStatus = param.getLoanStatus();
        if (StringUtils.isNotEmpty(loanStatus)){
            sqlStr+=" AND sl.loanStatus=@loanStatus ";
            countSqlStr+=" AND sl.loanStatus=@loanStatus ";
        }

        String channelId = param.getChannelId();
        if (StringUtils.isNotEmpty(channelId)) {
            sqlStr+=" AND  sl.channelId=@channelId ";
            countSqlStr+=" AND  sl.channelId=@channelId ";
        }
        Date beginExpireDate = param.getBeginExpireDate();
        Date endExpireDate = param.getEndExpireDate();
        if(null != beginExpireDate){
            sqlStr+=" AND slr.dueDate >= @beginExpireDate ";
            countSqlStr+=" AND slr.dueDate >= @beginExpireDate ";
        }
        if(null != endExpireDate){
            sqlStr+=" AND slr.dueDate <= @endExpireDate ";
            countSqlStr+=" AND slr.dueDate <= @endExpireDate ";
        }

        addParam.put("beginExpireDate",beginExpireDate);
        addParam.put("endExpireDate",endExpireDate);

        Date beginActualLoanDate = param.getBeginActualLoanDate();
        Date endActualLoanDate = param.getEndActualLoanDate();
        if(null != beginActualLoanDate){
            sqlStr+=" AND sl.loanTime >= @beginActualLoanDate ";
            countSqlStr+=" AND sl.loanTime >= @beginActualLoanDate ";
        }
        if(null != endActualLoanDate){
            sqlStr+=" AND sl.loanTime <= @endActualLoanDate ";
            countSqlStr+=" AND sl.loanTime <= @endActualLoanDate ";
        }

        addParam.put("beginActualLoanDate",beginActualLoanDate);
        addParam.put("endActualLoanDate",endActualLoanDate);

        String saleName = param.getSaleName();
        if (StringUtils.isNotEmpty(saleName)){
            sqlStr+=" AND sbu.name like @saleName ";
            countSqlStr+=" AND sbu.name like @saleName ";
        }
        sqlStr+=" ORDER BY sl. CODE, sblr.position asc";
        Sql sql = Sqls.create(sqlStr);
        addParam.put("saleName", '%'+saleName+'%');
        addParam.put("submitter", '%'+submitter+'%');
        //ParamUtil.matchParam(sql,param,addParam);
        sql.setParam("billType", billType);
        sql.setParam("submitter", '%'+submitter+'%');
        sql.setParam("beginSubmitDate",beginSubmitDate);
        sql.setParam("endSubmitDate",endSubmitDate);
        sql.setParam("beginActualClearedDate",beginActualClearedDate);
        sql.setParam("endActualClearedDate",endActualClearedDate);
        sql.setParam("status",status);
        sql.setParam("loanStatus",loanStatus);
        sql.setParam("channelId",channelId);
        sql.setParam("beginExpireDate",beginExpireDate);
        sql.setParam("endExpireDate",endExpireDate);
        sql.setParam("beginActualLoanDate",beginActualLoanDate);
        sql.setParam("endActualLoanDate",endActualLoanDate);
        sql.setParam("saleName",'%'+saleName+'%');
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(BillBusinessReport.class));
        dao().execute(sql);
        List<BillBusinessReport> list = sql.getList(BillBusinessReport.class);
        Map<String,String> businessSourcePool = new HashMap<>();
        if (CollectionUtils.isNotEmpty(list)) {
            for (BillBusinessReport billBusinessReport:list) {
                String BusinessSource =  getBusinessSource(businessSourcePool,billBusinessReport);
                billBusinessReport.setBusinessSource(BusinessSource);
            }
        }
        Sql countSql = Sqls.create(countSqlStr);
        //ParamUtil.matchParam(countSql,param,addParam);
        countSql.setParam("billType", billType);
        countSql.setParam("submitter", '%'+submitter+'%');
        countSql.setParam("beginSubmitDate",beginSubmitDate);
        countSql.setParam("endSubmitDate",endSubmitDate);
        countSql.setParam("beginActualClearedDate",beginActualClearedDate);
        countSql.setParam("endActualClearedDate",endActualClearedDate);
        countSql.setParam("status",status);
        countSql.setParam("loanStatus",loanStatus);
        countSql.setParam("channelId",channelId);
        countSql.setParam("beginExpireDate",beginExpireDate);
        countSql.setParam("endExpireDate",endExpireDate);
        countSql.setParam("beginActualLoanDate",beginActualLoanDate);
        countSql.setParam("endActualLoanDate",endActualLoanDate);
        countSql.setParam("saleName",'%'+saleName+'%');
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();

        return new DataTables(param.getDraw(),count,count,list);
    }

    private String getBusinessSource(Map<String,String> businessSourcePool,BillBusinessReport billBusinessReport){
        String source = businessSourcePool.get(""+billBusinessReport.getChannelId()+billBusinessReport.getBusinessLine()+billBusinessReport.getOrgCode()+billBusinessReport.getSaleName());
        if(StringUtils.isEmpty(source)){
            source = loanService.getBusinessSource(billBusinessReport.getChannelId(),billBusinessReport.getBusinessLine(),billBusinessReport.getOrgCode(),billBusinessReport.getSaleName());
            businessSourcePool.put(""+billBusinessReport.getChannelId()+billBusinessReport.getBusinessLine()+billBusinessReport.getOrgCode()+billBusinessReport.getSaleName(),source);
        }
        return source;
    }

    public DataTables comprehensiveReportPage(DataTableParam param){
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String loanBeginDate;
        String loanEndDate;
        String productType = ProductTypesUtils.getName("PIAOJU");
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            loanBeginDate = keys.get("loanBeginDate");
            loanEndDate = keys.get("loanEndDate");
        } else {
            return null;
        }

        String sqlStr = "SELECT " +
                " spt.id AS productTypeId ," +
                " spt. NAME AS productType, " +
                " count(DISTINCT case when sl.createTime<=@loanEndDate and sl.createTime>=@loanBeginDate then (case  when spt.name = @productType then slr.id else sl.id end ) else null end) AS businessSubmissionNumber, " +
                " count(DISTINCT case when sl.loanStatus = 'APPROVEREJECT' and EXISTS(select sar.loanId from  view_approval_result sar where  sar.loanId = sl.id and sar.approvalCode = 'DISAGREE' and sar.approvalTime<= @loanEndDate and sar.approvalTime>=@loanBeginDate )  then (case  when spt.name = @productType then slr.id else sl.id end ) else null end)AS approvalRejectNumber, " +
                " count(DISTINCT case when (sl.loanStatus = 'APPROVEEND' or sl.loanStatus = 'LOANED' or sl.loanStatus = 'CLEARED' or sl.loanStatus = 'OVERDUE' ) and EXISTS(select sar.loanId from  view_approval_result sar where sar.loanId = sl.id  and sar.approvalCode = 'AGREE' and sar.approvalTime<= @loanEndDate and sar.approvalTime>= @loanBeginDate )  then (case  when spt.name = @productType then slr.id else sl.id end ) else null end)AS approvalPassNumber, " +
                " count(DISTINCT case when sl.loanTime is not null and  sl.loanTime>= @loanBeginDate and sl.loanTime<= @loanEndDate  then (case  when spt.name = @productType then slr.id else sl.id end ) else null end)AS loanNumber, " +
                " count(DISTINCT case when  (case when sblr.actualDueDate is not null then sblr.actualDueDate>=@loanBeginDate and sblr.actualDueDate<= @loanEndDate  else  slr.dueDate>= @loanBeginDate and slr.dueDate<= @loanEndDate end) then (case  when spt.name = @productType then slr.id else sl.id end ) else null end)AS repaymentNumber, " +
                " sum( " +
                " " +
                "  IF (  " +
                "   case when sblr.actualDueDate is not null then sblr.actualDueDate>=@loanBeginDate and sblr.actualDueDate<=@loanEndDate  else   slr.dueDate>= @loanBeginDate and slr.dueDate<= @loanEndDate end , " +
                "   slr.totalAmount  , " +
                "   0 " +
                "  ) " +
                " ) AS repaymentMoney, " +
                " count(DISTINCT case when  slr.repayDate>= @loanBeginDate and slr.repayDate<=@loanEndDate then (case  when spt.name = @productType then slr.id else sl.id end ) else null end) AS actualRepaymentNumber, " +
                " sum( " +
                " " +
                "  IF ( " +
                "   slr.repayDate>= @loanBeginDate and slr.repayDate<=@loanEndDate , " +
                "   slr.repayTotalAmount, " +
                "   0 " +
                "  ) " +
                " ) AS actualRepaymentMoney " +
                " FROM " +
                " sl_loan sl " +
                " LEFT JOIN sl_loan_repay slr ON sl.id = slr.loanId " +
                " LEFT JOIN sl_bill_loan_repay sblr on sblr.repayId = slr.id"+
                " LEFT JOIN sl_product_type spt ON sl.productTypeId = spt.id " +
                " where sl.status='ABLE'  " +
                " GROUP BY " +
                " sl.productTypeId";

        String countSqlStr = "SELECT  COUNT(DISTINCT sl.productTypeId) AS 'number' " +
                "  FROM " +
                " sl_loan sl " +
                " LEFT JOIN sl_loan_repay slr ON sl.id = slr.loanId " +
                " LEFT JOIN sl_product_type spt ON sl.productTypeId = spt.id " +
                " where sl.status='ABLE' " ;

        String sqlStr1 = "SELECT " +
                " spt.id as productTypeId, " +
                " sum(IF ( " +
                "   sl.loanTime IS NOT NULL " +
                "   AND sl.loanTime >=@loanBeginDate " +
                "   AND sl.loanTime <=@loanEndDate, " +
                "   sl.actualAmount, " +
                "   0 " +
                "  )) as loanMoney," +
                " sum( " +
                " " +
                "  IF ( " +
                "   sl.loanTime IS NOT NULL " +
                "   AND sl.loanTime >=@loanBeginDate " +
                "   AND sl.loanTime <=@loanEndDate, " +
                "   ( " +
                "    CASE " +
                "    WHEN spt. NAME =  @productType  THEN " +
                "     ( " +
                "      SELECT " +
                "       sum(sblr.disDays) " +
                "      FROM " +
                "       sl_bill_loan_repay sblr " +
                "      WHERE " +
                "       sblr.loanId = sl.id " +
                "     ) " +
                "    ELSE " +
                "     ( " +
                "      CASE " +
                "      WHEN sl.termType = 'DAYS' THEN " +
                "       sl.term " +
                "      WHEN sl.termType = 'MOTHS' THEN " +
                "       sl.term * 30 " +
                "      ELSE " +
                "       TIMESTAMPDIFF(DAY, sl.loanTime, sl.term) + 1 " +
                "      END " +
                "     ) " +
                "    END " +
                "   ), " +
                "   0 " +
                "  ) " +
                " ) as totalDays " +
                "FROM " +
                " sl_loan sl " +
                "LEFT JOIN sl_product_type spt ON sl.productTypeId = spt.id " +
                "where sl. STATUS = 'ABLE' " +
                "GROUP BY " +
                " spt.id";
        Sql sql = Sqls.create(sqlStr);
        sql.setParam("loanBeginDate",loanBeginDate+" 00:00:00");
        sql.setParam("loanEndDate",loanEndDate+" 23:59:59");
        sql.setParam("productType",productType);
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(ComprehensiveReport.class));
        dao().execute(sql);
        List<ComprehensiveReport> list = sql.getList(ComprehensiveReport.class);

        Sql sql1 = Sqls.create(sqlStr1);
        sql1.setParam("loanBeginDate",loanBeginDate+" 00:00:00");
        sql1.setParam("loanEndDate",loanEndDate+" 23:59:59");
        sql1.setParam("productType",productType);
        sql1.setPager(pager);
        sql1.setCallback(Sqls.callback.entities());
        sql1.setEntity(dao().getEntity(ComprehensiveReport.class));
        dao().execute(sql1);
        List<ComprehensiveReport> list1 = sql1.getList(ComprehensiveReport.class);


        for(ComprehensiveReport comprehensiveReport : list){
            for(ComprehensiveReport comprehensiveReport1 : list1){
                if(comprehensiveReport.getProductTypeId().equals(comprehensiveReport1.getProductTypeId())){
                    comprehensiveReport.setLoanMoney(comprehensiveReport1.getLoanMoney());
                    if(comprehensiveReport.getLoanNumber()>0)
                        comprehensiveReport.setAverageLoan((int)(Math.round((double)comprehensiveReport1.getTotalDays()/(double)comprehensiveReport.getLoanNumber())));
                    else
                        comprehensiveReport.setAverageLoan(0);
                    break;
                }
            }
        }

        Sql countSql = Sqls.create(countSqlStr);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();

        return new DataTables(param.getDraw(),count,count,list);
    }
}

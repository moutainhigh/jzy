package com.kaisa.kams.components.service.report;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.utils.CoverUtil;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.ProductTypesUtils;
import com.kaisa.kams.enums.FinanceReportType;
import com.kaisa.kams.enums.LoanRepayStatus;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.report.BillFinanceDailyReport;
import com.kaisa.kams.models.report.CheDaiFinanceDailyReport;
import com.kaisa.kams.models.report.GeRenDaiFinanceDailyReport;
import com.kaisa.kams.models.report.HongBenFinanceDailyReport;
import com.kaisa.kams.models.report.ShuLouFinanceDailyReport;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.Dao;
import org.nutz.dao.QueryResult;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by pengyueyang on 2017/6/22.
 */
@IocBean
public class FinanceDailyReportService{

    private final String CHEDAI_PRODUCT_TYPE_NAME = ProductTypesUtils.getName("CHEDAI");
    private final String GERENDAI_PRODUCT_TYPE_NAME = ProductTypesUtils.getName("GERENDAI");
    private final String SHULOU_PRODUCT_TYPE_NAME = ProductTypesUtils.getName("SHULOU");
    private final String HONGBEN_PRODUCT_TYPE_NAME = ProductTypesUtils.getName("HONGBEN");


    @Inject
    private Dao dao;

    private void saveList(List list) {
        if (CollectionUtils.isNotEmpty(list)) {
            dao.insert(list);
        }
    }

    public DataTables getReportDataTables(DataTableParam param) {
        String reportName = param.getSearchKeys().get("report_name");
        String reportTime = param.getSearchKeys().get("report_time");
        Class className =  getClassName(reportName);
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        int count = 0;
        List list = new ArrayList();
        if (null == className || StringUtils.isEmpty(reportTime)) {
            return new DataTables(param.getDraw(),count,count,list);
        }
        Condition cnd = Cnd.where("recordDate","=",reportTime).asc("businessDate,businessOrderNo");
        list = dao.query(className, cnd, pager);
        list.forEach(report -> coverMessage(report,reportName));
        count = dao.count(className,cnd);
        return new DataTables(param.getDraw(),count,count,list);
    }

    private void coverMessage(Object obj,String reportName) {
        if (FinanceReportType.CHEDAI.name().equals(reportName)) {
            CheDaiFinanceDailyReport report = (CheDaiFinanceDailyReport) obj;
            report.setIdNumber(CoverUtil.coverIdNumber(report.getIdNumber()));
            //report.setBorrower(CoverUtil.coverName(report.getBorrower()));
            return;
        }
        if (FinanceReportType.GERENDAI.name().equals(reportName)) {
            GeRenDaiFinanceDailyReport report = (GeRenDaiFinanceDailyReport) obj;
            report.setIdNumber(CoverUtil.coverIdNumber(report.getIdNumber()));
            //report.setBorrower(CoverUtil.coverName(report.getBorrower()));
            return;
        }
        if (FinanceReportType.HONGBEN.name().equals(reportName)) {
            HongBenFinanceDailyReport report = (HongBenFinanceDailyReport) obj;
            report.setIdNumber(CoverUtil.coverIdNumber(report.getIdNumber()));
            //report.setBorrower(CoverUtil.coverName(report.getBorrower()));
            return;
        }
        if (FinanceReportType.SHULOU.name().equals(reportName)) {
            ShuLouFinanceDailyReport report = (ShuLouFinanceDailyReport) obj;
            report.setIdNumber(CoverUtil.coverIdNumber(report.getIdNumber()));
            //report.setBorrower(CoverUtil.coverName(report.getBorrower()));
            return;
        }
    }

    private Class getClassName(String reportName) {
        if (FinanceReportType.PIAOJU.name().equals(reportName)) {
            return BillFinanceDailyReport.class;
        }
        if (FinanceReportType.CHEDAI.name().equals(reportName)) {
            return CheDaiFinanceDailyReport.class;
        }
        if (FinanceReportType.GERENDAI.name().equals(reportName)) {
            return GeRenDaiFinanceDailyReport.class;
        }
        if (FinanceReportType.HONGBEN.name().equals(reportName)) {
            return HongBenFinanceDailyReport.class;
        }
        if (FinanceReportType.SHULOU.name().equals(reportName)) {
            return ShuLouFinanceDailyReport.class;
        }
        return null;
    }

    public void executeDailyJob(String preDayDate) {
        saveBill(preDayDate);
        saveCheDai(preDayDate);
        saveGeRenDai(preDayDate);
        saveShuLou(preDayDate);
        saveHongBen(preDayDate);
    }

    private void saveBill(String preDayDate) {
        saveList(getBillList(preDayDate));
    }

    private List<BillFinanceDailyReport> getBillList(String preDayDate) {
        Sql sql = Sqls.create(BILL_QUERY_SQL);
        sql.setParam("loanTime",preDayDate);
        List<Map> queryList = queryMapList(sql);
        List<BillFinanceDailyReport> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(queryList)) {
            Date recordDate = DateUtil.parseStringToDate(preDayDate);
            queryList.forEach(map -> {
                BillFinanceDailyReport item = Lang.map2Object(map,BillFinanceDailyReport.class);
                processBillReport(item,recordDate);
                result.add(item);
            });
        }
        return result;
    }

    private void processBillReport(BillFinanceDailyReport item,Date recordDate) {
        item.setOtherFee(BigDecimal.ZERO);
        if (isNotRepay(item.getStatus())) {
            item.setExpireDays(DateUtil.dayDiffToday(item.getActualDueDate()));
        }
        item.setRecordDate(recordDate);
    }

    private boolean isNotRepay (LoanRepayStatus status) {
        if (LoanRepayStatus.LOANED.equals(status)) {
            return true;
        }
        if (LoanRepayStatus.OVERDUE.equals(status)) {
            return true;
        }
        return false;
    }

    private List<Map> queryMapList(Sql sql) {
        sql.setCallback(Sqls.callback.maps());
        dao.execute(sql);
        return sql.getList(Map.class);
    }

    private void saveCheDai(String preDayDate) {
        saveList(getCheDaiList(preDayDate));
    }

    private List<CheDaiFinanceDailyReport> getCheDaiList(String preDayDate) {
        Sql sql = Sqls.create(CHEDAI_QUERY_SQL);
        sql.setParam("loanTime",preDayDate);
        sql.setParam("productTypeName",CHEDAI_PRODUCT_TYPE_NAME);
        List<Map> queryList = queryMapList(sql);
        List<CheDaiFinanceDailyReport> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(queryList)) {
            queryList.forEach(map -> {
                Date recordDate = DateUtil.parseStringToDate(preDayDate);
                CheDaiFinanceDailyReport item = Lang.map2Object(map,CheDaiFinanceDailyReport.class);
                processCheDaiReport(item,recordDate);
                result.add(item);
            });
        }
        return result;
    }

    private void processCheDaiReport(CheDaiFinanceDailyReport item,Date date) {
        item.setValues();
        item.setRecordDate(date);
    }

    private void saveGeRenDai(String preDayDate){
        saveList(getGeRenDaiList(preDayDate));
    }

    private List<GeRenDaiFinanceDailyReport> getGeRenDaiList(String preDayDate) {
        Sql sql = Sqls.create(GERENDAI_QUERY_SQL);
        sql.setParam("loanTime",preDayDate);
        sql.setParam("productTypeName",GERENDAI_PRODUCT_TYPE_NAME);
        List<Map> queryList = queryMapList(sql);
        List<GeRenDaiFinanceDailyReport> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(queryList)) {
            queryList.forEach(map -> {
                Date recordDate = DateUtil.parseStringToDate(preDayDate);
                GeRenDaiFinanceDailyReport item = Lang.map2Object(map,GeRenDaiFinanceDailyReport.class);
                processGeRenDaiReport(item,recordDate);
                result.add(item);
            });
        }
        return result;
    }

    private void processGeRenDaiReport(GeRenDaiFinanceDailyReport item,Date date) {
        item.setValues();
        item.setRecordDate(date);
    }
    private void saveShuLou(String preDayDate) {
        saveList(getShuLouList(preDayDate));
    }

    private List<ShuLouFinanceDailyReport> getShuLouList(String preDayDate) {
        Sql sql = Sqls.create(SHULOU_QUERY_SQL);
        sql.setParam("loanTime",preDayDate);
        sql.setParam("productTypeName",SHULOU_PRODUCT_TYPE_NAME);
        List<Map> queryList = queryMapList(sql);
        List<ShuLouFinanceDailyReport> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(queryList)) {
            queryList.forEach(map -> {
                Date recordDate = DateUtil.parseStringToDate(preDayDate);
                ShuLouFinanceDailyReport item = Lang.map2Object(map,ShuLouFinanceDailyReport.class);
                processShuLouReport(item,recordDate);
                result.add(item);
            });
        }
        return result;
    }

    private void processShuLouReport(ShuLouFinanceDailyReport item,Date date) {
        item.setValues();
        item.setRecordDate(date);
    }

    private void saveHongBen(String preDayDate) {
        saveList(qetHongBenList(preDayDate));
    }

    private List<HongBenFinanceDailyReport> qetHongBenList(String preDayDate) {
        Sql sql = Sqls.create(HONGBEN_QUERY_SQL);
        sql.setParam("productTypeName",HONGBEN_PRODUCT_TYPE_NAME);
        sql.setParam("loanTime",preDayDate);
        List<Map> queryList = queryMapList(sql);
        List<HongBenFinanceDailyReport> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(queryList)) {
            queryList.forEach(map -> {
                Date recordDate = DateUtil.parseStringToDate(preDayDate);
                HongBenFinanceDailyReport item = Lang.map2Object(map,HongBenFinanceDailyReport.class);
                processShuLouReport(item,recordDate);
                result.add(item);
            });
        }
        return result;
    }

    private void processShuLouReport(HongBenFinanceDailyReport item,Date date) {
        item.setValues();
        item.setRecordDate(date);
    }

    private final String BILL_QUERY_SQL = "select l.code businessOrderNo,blr.billNo billCode," +
            "blr.payer drawer,lb.name discounter,p.name billType," +
            "l.saleName businessUser,bl.discountTime businessDate," +
            "blr.drawTime billingDate,lr.dueDate dueDate," +
            "blr.actualDueDate actualDueDate,lr.repayDate billReceiveDate," +
            "blr.overdueDays adjustedDays,DATEDIFF(blr.actualDueDate,blr.disDate) totalDays," +
            "blr.intermediaryFee intermediaryFee,i.name intermediaryName," +
            "lr.amount billAmount,lr.interest billInterest,lr.status status " +
            "from sl_bill_loan_repay blr " +
            "left join sl_bill_loan bl on bl.loanId=blr.loanId " +
            "left join sl_intermediary i on i.id=bl.intermediaryId " +
            "left join sl_loan_repay lr on blr.repayId=lr.id " +
            "left join sl_loan l on l.id=blr.loanId " +
            "left join sl_loan_borrower lb on lb.loanId=l.id " +
            "left join sl_product p on p.id=l.productId " +
            "where DATE_FORMAT(l.loanTime,'%Y-%m-%d') = @loanTime " +
            "order by l.loanTime,l.code ";

    private final String CHEDAI_QUERY_SQL = "select l.code businessOrderNo,\n" +
            "p.name productType,l.saleName businessUser,\n" +
            "lb.name borrower,lb.certifNumber idNumber,\n" +
            "l.actualAmount loanAmount,l.term term,\n" +
            "l.termType termType,l.interestAmount interestAmount,\n" +
            "l.interestRate interestRate,l.loanLimitType loanLimitType,\n" +
            "l.loanTime businessDate,lr.dueDate dueDate,\n" +
            "lr.repayDate actualRepayDate,l.repayMethod repayMethod,\n" +
            "l.amount principal,lr.totalInterest interest,\n" +
            "lf.feeAmount otherFee,l.calculateMethodAboutDay calculateMethodAboutDay\n" +
            " from sl_loan l\n" +
            "left join sl_channel sc on l.channelId  = sc.id\n" +
            "left join sl_product p on p.id=l.productId\n" +
            "left join sl_product_type pt on pt.id=l.productTypeId\n" +
            "left join (select loanId,name,certifNumber from sl_loan_borrower where master=true) lb on lb.loanId=l.id\n" +
            "left join (select loanId,max(dueDate) dueDate,max(repayDate) repayDate,sum(interest) totalInterest  from sl_loan_repay group by loanId) lr on lr.loanId=l.id\n" +
            "left join (select loanId,sum(feeAmount) feeAmount from sl_loan_fee group by loanId) lf on lf.loanId=l.id\n" +
            "where DATE_FORMAT(l.loanTime,'%Y-%m-%d') = @loanTime and pt.name=@productTypeName\n" +
            "order by l.loanTime,l.code";

    private final String GERENDAI_QUERY_SQL = CHEDAI_QUERY_SQL;

    private final String SHULOU_QUERY_SQL = "select l.code businessOrderNo,sc.name source,\n" +
            "p.name productType,l.saleName businessUser,\n" +
            "lb.name borrower,lb.certifNumber idNumber,\n" +
            "l.actualAmount loanAmount,l.term term,\n" +
            "l.termType termType,l.interestAmount interestAmount,\n" +
            "l.interestRate interestRate,l.loanLimitType loanLimitType,\n" +
            "l.loanTime businessDate,lr.dueDate dueDate,\n" +
            "lr.repayDate actualRepayDate,l.amount principal,\n" +
            "lr.totalInterest interest,lf.feeAmount otherFee,\n" +
            " l.calculateMethodAboutDay calculateMethodAboutDay\n"+
            " from sl_loan l\n" +
            "left join sl_channel sc on l.channelId  = sc.id\n" +
            "left join sl_product p on p.id=l.productId\n" +
            "left join sl_product_type pt on pt.id=l.productTypeId\n" +
            "left join (select loanId,name,certifNumber from sl_loan_borrower where master=true) lb on lb.loanId=l.id\n" +
            "left join (select loanId,max(dueDate) dueDate,max(repayDate) repayDate,sum(interest) totalInterest  from sl_loan_repay group by loanId) lr on lr.loanId=l.id\n" +
            "left join (select loanId,sum(feeAmount) feeAmount from sl_loan_fee group by loanId) lf on lf.loanId=l.id\n" +
            "where  DATE_FORMAT(l.loanTime,'%Y-%m-%d') = @loanTime and  pt.name=@productTypeName\n" +
            "order by l.loanTime,l.code";

    private final String HONGBEN_QUERY_SQL = SHULOU_QUERY_SQL;


}

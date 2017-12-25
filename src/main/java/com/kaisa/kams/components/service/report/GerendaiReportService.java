package com.kaisa.kams.components.service.report;

import com.kaisa.kams.components.params.report.DataTableBusinessReportParam;
import com.kaisa.kams.components.params.report.DataTableFinanceReportParam;
import com.kaisa.kams.components.service.base.ReportUtilsService;
import com.kaisa.kams.components.utils.CoverUtil;
import com.kaisa.kams.components.utils.DecimalUtils;
import com.kaisa.kams.components.utils.ProductTypesUtils;
import com.kaisa.kams.components.utils.TimeUtils;
import com.kaisa.kams.components.utils.report.ReportUtils;
import com.kaisa.kams.components.view.loan.Duration;
import com.kaisa.kams.components.view.report.ChedaiBusinessReportView;
import com.kaisa.kams.components.view.report.GerendaiBusinessReportView;
import com.kaisa.kams.components.view.report.GerendaiReportView;
import com.kaisa.kams.enums.LoanRepayMethod;
import com.kaisa.kams.enums.LoanRepayStatus;
import com.kaisa.kams.enums.LoanStatus;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.Loan;
import com.kaisa.kams.models.LoanFee;
import com.kaisa.kams.models.LoanRepay;
import com.kaisa.kams.models.ProductInfoItem;
import com.kaisa.kams.models.ProductRate;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.IdNameEntityService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 个人贷、车贷报表服务层
 * Created by lw on 2017/5/17.
 */
@IocBean(fields = "dao")
public class GerendaiReportService extends IdNameEntityService<Loan> {
    @Inject
    private ReportUtilsService reportUtilsService;

    public DataTables queryGerendaiAndChedaiList(DataTableFinanceReportParam param) {
        String[] loanStatus = {"LOANED", "OVERDUE", "CLEARED"};
        if (StringUtils.isNotEmpty(param.getStatus()) && ArrayUtils.isNotEmpty(param.getLoanedStatus())) {
            loanStatus =  param.getLoanedStatus();
        }
        String sqlStr = "SELECT  " +
                "l.id AS 'loanId', " +
                "l. CODE AS 'businessNumber', " +
                "p. NAME AS 'productType', " +
                "l.saleName AS 'businesser', " +
                "sc.name AS 'businessSource', " +
                "lb.name AS 'borrower', " +
                "lb.certifNumber AS 'idNumber', " +
                "l.actualAmount AS 'loanAmount', " +
                "l.termType AS 'termType', " +
                "l.term AS 'loanTerm', " +
                "l.interestRate AS 'yearRate', " +
                "l.loanTime AS 'businessDate', " +
                "l.repayMethod AS 'repaymentMethods', " +
                "  ls.name AS 'loanSubject', " +
                "l.loanStatus AS 'status' " +
                "FROM  " +
                "sl_loan l " +
                "LEFT JOIN sl_loan_repay lr  ON lr.loanId = l.id " +
                "LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id  " +
                "LEFT JOIN sl_business_user u ON u.id = l.saleId  " +
                "LEFT JOIN sl_business_organize o ON u.organizeId = o.id  " +
                "LEFT JOIN sl_business_agency a ON o.agencyId = a.id  " +
                "LEFT JOIN sl_product p ON l.productId = p.id  " +
                "LEFT JOIN sl_channel sc ON l.channelId  = sc.id " +
                "LEFT JOIN sl_product_info_tmpl t ON p.infoTmpId = t.id  " +
                "LEFT JOIN sl_loan_borrower lb ON lb.loanId =l.id and lb.master=1 " +
                "  left join sl_loan_subject ls on ls.id = l.loanSubjectId " +
                " WHERE " +
                "l. STATUS = 'ABLE'  " +
                "AND pt.name = @productTypeName " +
                "AND l.loanStatus in(@loanStatus) ";

        String countSqlStr = "SELECT " +
                " COUNT(DISTINCT l.code) AS 'number' " +
                " FROM " +
                "sl_loan l " +
                "LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id  " +
                "LEFT JOIN sl_business_user u ON u.id = l.saleId  " +
                "LEFT JOIN sl_business_organize o ON u.organizeId = o.id  " +
                "LEFT JOIN sl_business_agency a ON o.agencyId = a.id  " +
                "LEFT JOIN sl_product p ON l.productId = p.id  " +
                "LEFT JOIN sl_channel sc ON l.channelId  = sc.id " +
                "LEFT JOIN sl_product_info_tmpl t ON p.infoTmpId = t.id  " +
                "LEFT JOIN sl_loan_borrower lb ON lb.loanId =l.id and lb.master=1 " +
                "  left join sl_loan_subject ls on ls.id = l.loanSubjectId " +
                " WHERE " +
                "l. STATUS = 'ABLE'  " +
                "AND pt.name = @productTypeName " +
                "AND l.loanStatus in(@loanStatus) ";
        Date beginDate = param.getBeginDateTime();
        Date endDate = param.getEndDateTime();
        if (null != beginDate && null != endDate) {
            sqlStr += " AND l.loanTime>=@beginDate  AND l.loanTime<=@endDate ";
            countSqlStr += " AND l.loanTime>=@beginDate  AND l.loanTime<=@endDate ";
        }

        String loanSubject = param.getLoanSubject();
        if (StringUtils.isNotEmpty(loanSubject)) {
            sqlStr += " AND  ls.id=@loanSubject ";
            countSqlStr += " AND  ls.id=@loanSubject ";
        }

        sqlStr += " GROUP BY l.code ORDER BY l.loanTime ASC" +
                " LIMIT @start,@length ";

        String productTypeName = param.getProductTypeName();
        Sql sql = Sqls.create(sqlStr);
        Sql countSql = Sqls.create(countSqlStr);
        sql.setParam("loanStatus", loanStatus);
        sql.setParam("loanSubject", loanSubject);
        sql.setParam("productTypeName", productTypeName);
        sql.setParam("beginDate", beginDate);
        sql.setParam("endDate", endDate);
        sql.setParam("start", param.getStart());
        sql.setParam("length", param.getLength());
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(GerendaiReportView.class));
        dao().execute(sql);
        List<GerendaiReportView> list = sql.getList(GerendaiReportView.class);
        countSql.setParam("loanStatus", loanStatus);
        countSql.setParam("loanSubject", loanSubject);
        countSql.setParam("productTypeName", productTypeName);
        countSql.setParam("beginDate", beginDate);
        countSql.setParam("endDate", endDate);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();

        return new DataTables(param.getDraw(), count, count, list);
    }

    public DataTables queryGerenDaiAndCheDaiListForBusiness(DataTableBusinessReportParam param, String type) {

        String sqlStr = "SELECT  " +
                "lr.id AS 'id', " +
                "l.id AS 'loanId', " +
                "l. CODE AS 'businessOrderNo', " +
                "p. NAME AS 'productName', " +
                " IFNULL(ls.name,'--') AS 'loanSubject', " +
                " IFNULL(USER.name,'--') AS 'submitter', " +
                " l.submitTime AS 'submitTime', " +
                "l.saleName AS 'businessSource', " +
                "lb.name AS 'borrower', " +
                "lb.certifNumber AS 'idNumber', " +
                " l.channelId AS 'channelId'," +
                " sc.name AS 'channel'," +
                " IFNULL(a.name,'--') AS 'agencyName'," +
                " IFNULL(o.name,'--') AS 'organizeName'," +
                " IFNULL(o.businessLine,'--') AS 'businessLine'," +
                " IFNULL(o.code,'--') AS 'orgCode'," +
                "l.actualAmount AS 'loanPrincipal', " +
                "l.termType AS 'termType', " +
                "l.loanLimitType AS 'loanLimitType', " +
                "l.term AS 'loanTerm', " +
                "l.interestRate AS 'borrowRate', " +
                "l.loanTime AS 'actualLoanDate', " +
                "lr.dueDate AS 'dueDate', " +
                "lr.repayDate AS 'actualClearedDate', " +
                "l.repayMethod AS 'repayMethod', " +
                "lr.amount AS 'receivableTotal', " +
                "lr.interest AS 'receivableInterest', " +
                "lr.otherFee AS 'serviceCharge', " +
                "slp.profit as profit , " +
                "l.clearDate AS clearedDate, " +
                "l.loanStatus AS 'status' " +
                "FROM  " +
                "sl_loan l " +
                "LEFT JOIN sl_loan_repay lr  ON lr.loanId = l.id " +
                "LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id  " +
                "LEFT JOIN sl_business_user u ON u.id = l.saleId  " +
                "LEFT JOIN sl_business_organize o ON u.organizeId = o.id  " +
                "LEFT JOIN sl_business_agency a ON o.agencyId = a.id  " +
                "LEFT JOIN sl_product p ON l.productId = p.id  " +
                "LEFT JOIN sl_product_info_tmpl t ON p.infoTmpId = t.id  " +
                "LEFT JOIN sl_loan_fee lf ON lf.loanId = l.id " +
                "LEFT JOIN sl_loan_borrower lb ON lb.loanId =l.id " +
                "LEFT JOIN sl_loan_profit slp on l.id = slp.loanId  " +
                "LEFT JOIN sl_loan_subject ls ON ls.id = l.loanSubjectId " +
                "LEFT JOIN sl_user USER ON USER.id = l.applyId " +
                "LEFT JOIN sl_channel sc ON sc.id = l.channelId " +
                "WHERE " +
                "l. STATUS = 'ABLE'  " +
                "AND pt.name = @type ";

        String countSqlStr = "SELECT " +
                " COUNT(DISTINCT l.code) AS 'number' " +
                " FROM " +
                "sl_loan l " +
                "LEFT JOIN sl_loan_repay lr  ON lr.loanId = l.id  " +
                "LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id  " +
                "LEFT JOIN sl_business_user u ON u.id = l.saleId  " +
                "LEFT JOIN sl_business_organize o ON u.organizeId = o.id  " +
                "LEFT JOIN sl_business_agency a ON o.agencyId = a.id  " +
                "LEFT JOIN sl_product p ON l.productId = p.id  " +
                "LEFT JOIN sl_product_info_tmpl t ON p.infoTmpId = t.id  " +
                "LEFT JOIN sl_loan_fee lf ON lf.loanId = l.id " +
                "LEFT JOIN sl_loan_borrower lb ON lb.loanId =l.id " +
                "LEFT JOIN sl_loan_subject ls ON ls.id = l.loanSubjectId " +
                "LEFT JOIN sl_user USER ON USER.id = l.applyId " +
                "LEFT JOIN sl_channel sc ON sc.id = l.channelId " +
                "WHERE " +
                "l. STATUS = 'ABLE'  " +
                "AND pt.name = @type ";

        String submitter = param.getSubmitter();
        if (StringUtils.isNotEmpty(submitter)) {
            sqlStr += " AND USER.name like @submitter ";
            countSqlStr += " AND USER.name like @submitter ";
        }

        Date beginSubmitDate = param.getBeginSubmitDate();
        Date endSubmitDate = param.getEndSubmitDate();
        if (null != beginSubmitDate) {
            sqlStr += " AND l.submitTime >= @beginSubmitDate ";
            countSqlStr += " AND l.submitTime >= @beginSubmitDate ";
        }
        if (null != endSubmitDate) {
            sqlStr += " AND l.submitTime <= @endSubmitDate ";
            countSqlStr += " AND l.submitTime <= @endSubmitDate ";
        }

        Date beginActualClearedDate = param.getBeginClearedDate();
        Date endActualClearedDate = param.getEndClearedDate();
        if (null != beginActualClearedDate) {
            sqlStr += " AND l.clearDate >= @beginActualClearedDate ";
            countSqlStr += " AND l.clearDate >= @beginActualClearedDate ";
        }
        if (null != endActualClearedDate) {
            sqlStr += " AND l.clearDate <= @endActualClearedDate ";
            countSqlStr += " AND l.clearDate <= @endActualClearedDate ";
        }


        String source = param.getSource();
        String channelId = param.getChannelId();
        if (StringUtils.isEmpty(source) || "1".equals(source)) {
            if (StringUtils.isNotEmpty(channelId)) {
                sqlStr += " AND  l.channelId=@channelId ";
                countSqlStr += " AND  l.channelId=@channelId ";
            }
        }
        if ("1".equals(source) && StringUtils.isEmpty(channelId)) {
            sqlStr += " AND  sc.channelType=1 ";
            countSqlStr += " AND  sc.channelType=1 ";
        }
        if ("0".equals(source)) {
            sqlStr += " AND  (sc.id is null or sc.channelType=0)";
            countSqlStr += " AND  (sc.id is null or sc.channelType=0) ";
        }

        String orgId = param.getOrganizeId();
        if (StringUtils.isNotEmpty(orgId)) {
            sqlStr += " AND  o.id=@orgId ";
            countSqlStr += " AND  o.id=@orgId ";
        }

        String agencyId = param.getAgencyId();
        if (StringUtils.isNotEmpty(agencyId)) {
            sqlStr += " AND  o.agencyId=@agencyId ";
            countSqlStr += " AND  o.agencyId=@agencyId ";
        }

        String loanSubject = param.getLoanSubjectId();
        if (StringUtils.isNotEmpty(loanSubject)) {
            sqlStr += " AND  ls.id=@loanSubject ";
            countSqlStr += " AND  ls.id=@loanSubject ";
        }

        String saleName = param.getSaleName();
        if (StringUtils.isNotEmpty(saleName)) {
            sqlStr += " AND  u.name like @saleName ";
            countSqlStr += " AND  u.name like @saleName ";
        }

        String status = param.getStatus();
        if (StringUtils.isNotEmpty(status)) {
            sqlStr += " AND  l.loanStatus=@status ";
            countSqlStr += " AND  l.loanStatus=@status ";
        }
        Date beginDate = param.getBeginDateTime();
        Date endDate = param.getEndDateTime();
        if (null != beginDate && null != endDate) {
            sqlStr += " AND l.loanTime>=@beginDate  AND l.loanTime<=@endDate ";
            countSqlStr += " AND l.loanTime>=@beginDate  AND l.loanTime<=@endDate ";
        }

        sqlStr += " GROUP BY l.code ORDER BY l.loanTime,l.createTime ASC" +
                " LIMIT @start,@length ";
        Sql sql = Sqls.create(sqlStr);
        Sql countSql = Sqls.create(countSqlStr);
        sql.setParam("submitter", '%' + submitter + '%');
        sql.setParam("beginSubmitDate", beginSubmitDate);
        sql.setParam("endSubmitDate", endSubmitDate);
        sql.setParam("channelId", channelId);
        sql.setParam("beginActualClearedDate", beginActualClearedDate);
        sql.setParam("endActualClearedDate", endActualClearedDate);
        sql.setParam("type", type);
        sql.setParam("orgId", orgId);
        sql.setParam("agencyId", agencyId);
        sql.setParam("loanSubject", loanSubject);
        sql.setParam("saleName", '%' + saleName + '%');
        sql.setParam("status", status);
        sql.setParam("beginDate", beginDate);
        sql.setParam("endDate", endDate);
        sql.setParam("start", param.getStart());
        sql.setParam("length", param.getLength());
        if ("CHEDAI".equals(getProductTypeByName(type))) {
            sql.setCallback(Sqls.callback.entities());
            sql.setEntity(dao().getEntity(ChedaiBusinessReportView.class));

        } else if ("GERENDAI".equals(getProductTypeByName(type))) {
            sql.setCallback(Sqls.callback.entities());
            sql.setEntity(dao().getEntity(GerendaiBusinessReportView.class));

        }
        dao().execute(sql);
        List<ChedaiBusinessReportView> listC = null;
        List<GerendaiBusinessReportView> listG = null;
        if ("CHEDAI".equals(getProductTypeByName(type))) {
            listC = sql.getList(ChedaiBusinessReportView.class);
        } else if ("GERENDAI".equals(getProductTypeByName(type))) {
            listG = sql.getList(GerendaiBusinessReportView.class);
        }
        countSql.setParam("submitter", '%' + submitter + '%');
        countSql.setParam("orgId", orgId);
        countSql.setParam("beginSubmitDate", beginSubmitDate);
        countSql.setParam("endSubmitDate", endSubmitDate);
        countSql.setParam("channelId", channelId);
        countSql.setParam("beginActualClearedDate", beginActualClearedDate);
        countSql.setParam("endActualClearedDate", endActualClearedDate);
        countSql.setParam("agencyId", agencyId);
        countSql.setParam("loanSubject", loanSubject);
        countSql.setParam("saleName", '%' + saleName + '%');
        countSql.setParam("status", status);
        countSql.setParam("type", type);
        countSql.setParam("beginDate", beginDate);
        countSql.setParam("endDate", endDate);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        DataTables dataTables = null;
        if ("CHEDAI".equals(getProductTypeByName(type))) {
            dataTables = new DataTables(param.getDraw(), count, count, listC);
        } else if ("GERENDAI".equals(getProductTypeByName(type))) {
            dataTables = new DataTables(param.getDraw(), count, count, listG);
        }
        return dataTables;
    }


    //车贷表格处理数据
    public DataTables queryChedaiListForTable(DataTableFinanceReportParam param) {
        List<ProductRate> productRateList = dao().query(ProductRate.class, null);
        DataTables dataTable = queryGerendaiAndChedaiList(param);
        if (dataTable.getRecordsTotal() < 1) {
            return dataTable;
        }
        List<GerendaiReportView> list = dataTable.getData();
        list.stream().forEach(m -> {
            String loanId = m.getLoanId();
            Date loanTime = m.getBusinessDate();
            String term = m.getLoanTerm();
            BigDecimal yearRate = m.getYearRate();
            String termType = m.getTermType();
            BigDecimal loanAmount = m.getLoanAmount();
            int totalDays = 0;
            BigDecimal receivablePrincipal = BigDecimal.ZERO;
            BigDecimal receivableInterest = BigDecimal.ZERO;
            BigDecimal otherCharges = BigDecimal.ZERO;
            BigDecimal financeCost = BigDecimal.ZERO;
            BigDecimal averageDailyIncome = BigDecimal.ZERO;
            BigDecimal averageDailyCost = BigDecimal.ZERO;
            BigDecimal monthlyIncome = BigDecimal.ZERO;
            BigDecimal monthlyCost = BigDecimal.ZERO;
            Date loanDueDate = null;
            Date clearedDate = null;

            //实还本金
            BigDecimal repayAmount = new BigDecimal(0);
            //实还利息
            BigDecimal repayInterest = new BigDecimal(0);
            //实还费用
            BigDecimal repayFee = new BigDecimal(0);
            switch (termType) {
                case "DAYS":
                    Duration duration_day = new Duration(0, 0, Integer.parseInt(term));
                    Date endDate_day = TimeUtils.offset(loanTime, duration_day);
                    totalDays = (int) ((endDate_day.getTime() - loanTime.getTime()) / (1000 * 24 * 3600));
                    m.setTotalDays(totalDays);
                    m.setYearRate(yearRate.multiply(new BigDecimal(365)));
                    break;
                case "MOTHS":
                    Duration duration_months = new Duration(0, Integer.parseInt(term), 0);
                    Date endDate_months = TimeUtils.offset(loanTime, duration_months);
                    totalDays = (int) ((endDate_months.getTime() - loanTime.getTime()) / (1000 * 24 * 3600));
                    m.setTotalDays(totalDays);
                    m.setYearRate(yearRate.multiply(new BigDecimal(12)));
                    m.setTotalDays(totalDays);
                    m.setYearRate(yearRate.multiply(new BigDecimal(12)));
                    break;
                case "YEAS":
                    Duration duration_yeas = new Duration(Integer.parseInt(term), 0, 0);
                    Date endDate_yeas = TimeUtils.offset(loanTime, duration_yeas);
                    totalDays = (int) ((endDate_yeas.getTime() - loanTime.getTime()) / (1000 * 24 * 3600));
                    m.setTotalDays(totalDays);
                    break;
                case "FIXED_DATE":
                    Date endDate_fixed = TimeUtils.formatDate("yyyy-MM-dd", term);
                    totalDays = (int) ((endDate_fixed.getTime() - loanTime.getTime()) / (1000 * 24 * 3600));
                    m.setTotalDays(totalDays);
                    break;
                default:
                    break;
            }
            m.dataConversion(productRateList, totalDays);
            List<LoanRepay> loanRepayList = this.dao().query(LoanRepay.class, Cnd.where("loanId", "=", loanId).orderBy("period", "asc"));
            //其他费用计算
            List<LoanFee> loanFeeList = this.dao().query(LoanFee.class, Cnd.where("loanId", "=", loanId).orderBy("period", "asc"));
            if (loanFeeList != null) {
                for (LoanFee loanFee : loanFeeList) {
                    if (null != loanFee.getRepayDate()) {
                        otherCharges = otherCharges.add(loanFee.getRepayFeeAmount() == null ? new BigDecimal(0) : loanFee.getRepayFeeAmount());
                    } else {
                        otherCharges = otherCharges.add(loanFee.getFeeAmount() == null ? new BigDecimal(0) : loanFee.getFeeAmount());
                    }
                    repayFee = repayFee.add(loanFee.getRepayFeeAmount() == null ? new BigDecimal(0) : loanFee.getRepayFeeAmount());
                }
            }
            for (LoanRepay loanRepay : loanRepayList) {
                receivablePrincipal = receivablePrincipal.add(loanRepay.getAmount() == null ? new BigDecimal(0) : loanRepay.getAmount());
                receivableInterest = receivableInterest.add(loanRepay.getInterest() == null ? new BigDecimal(0) : loanRepay.getInterest());
                repayAmount = repayAmount.add(loanRepay.getRepayAmount() == null ? new BigDecimal(0) : loanRepay.getRepayAmount());
                repayInterest = repayInterest.add(loanRepay.getRepayInterest() == null ? new BigDecimal(0) : loanRepay.getRepayInterest());
                loanDueDate = loanRepay.getDueDate();
                clearedDate = loanRepay.getRepayDate();
            }
            m.setReceivablePrincipal(receivablePrincipal);
            m.setRepayAmount(repayAmount);
            m.setOutstandingAmount(receivablePrincipal.subtract(repayAmount));
            m.setRepayInterest(repayInterest);
            m.setReceivableInterest(receivableInterest);
            m.setOutstandingInterest(ReportUtils.bigDecimalCompare(receivableInterest.subtract(repayInterest)));
            m.setOtherCharges(otherCharges);
            m.setRepayFee(repayFee);
            m.setOutstandingFee(otherCharges.subtract(repayFee));
            m.setTotal(receivablePrincipal.add(receivableInterest.add(otherCharges)));
            m.setRepayTotal(repayAmount.add(repayInterest.add(repayFee)));
            m.setOutstandingTotal(ReportUtils.bigDecimalCompare(m.getTotal().subtract(m.getRepayTotal())));
            //资金成本
            financeCost = loanAmount.multiply(new BigDecimal(totalDays)).divide(new BigDecimal(365), 10, BigDecimal.ROUND_HALF_EVEN).multiply(m.getFinanceRate());
            //日均收入
            averageDailyIncome = receivableInterest.divide(new BigDecimal(totalDays), 10, BigDecimal.ROUND_HALF_EVEN);
            averageDailyCost = financeCost.divide(new BigDecimal(totalDays), 10, BigDecimal.ROUND_HALF_EVEN);
            monthlyIncome = averageDailyIncome.multiply(new BigDecimal(TimeUtils.getDayOfMonth()));
            monthlyCost = averageDailyCost.multiply(new BigDecimal(TimeUtils.getDayOfMonth()));
            m.setFinanceCost(financeCost);
            m.setAverageDailyIncome(averageDailyIncome);
            m.setAverageDailyCost(averageDailyCost);
            m.setMonthlyIncome(monthlyIncome);
            m.setMonthlyCost(monthlyCost);
            m.setTermType(m.getTermType());
            m.setLoanDueDate(loanDueDate);
            if (LoanStatus.CLEARED.name().equals(m.getStatus())) {
                m.setClearedDate(clearedDate);
                m.setOutstandingAmount(ReportUtils.bigDecimalCompareClear(m.getOutstandingAmount()));
                m.setOutstandingInterest(ReportUtils.bigDecimalCompareClear(m.getOutstandingInterest()));
                m.setOutstandingFee(ReportUtils.bigDecimalCompareClear(m.getOutstandingFee()));
                m.setOutstandingTotal(ReportUtils.bigDecimalCompareClear(m.getOutstandingTotal()));
            }
        });
        return dataTable;
    }

    //业务车贷表格处理数据
    public DataTables queryBusinessCheDaiListForTable(DataTableBusinessReportParam param) {
        String type = getProductType("CHEDAI");
        DataTables dataTable = queryGerenDaiAndCheDaiListForBusiness(param,type);
        if (dataTable.getRecordsTotal() < 1) {
            return dataTable;
        }
        List<ChedaiBusinessReportView> list = dataTable.getData();
        list.stream().forEach(m -> {
            m.setIdNumber(CoverUtil.coverIdNumber(m.getIdNumber()));
            String loanId = m.getLoanId();
            BigDecimal receivableInterest = BigDecimal.ZERO;
            BigDecimal receivableAmount = BigDecimal.ZERO;
            BigDecimal receivableTotal = BigDecimal.ZERO;
            BigDecimal actualAmount = BigDecimal.ZERO;
            BigDecimal actualInterest = BigDecimal.ZERO;
            //获取应还日期,实际结清日期
            m.setDueDate(null);
            m.setActualClearedDate(null);
            int number = 0;
            String lastRepayId = null;
            int totalPeriod = 0;
            LoanRepay repay = null;
            if (LoanStatus.isLoaned(m.getStatus())) {
                List<LoanRepay> loanRepayList = this.dao().query(LoanRepay.class, Cnd.where("loanId", "=", loanId).orderBy("period", "asc"));
                if (CollectionUtils.isNotEmpty(loanRepayList)) {
                    for (LoanRepay loanRepay : loanRepayList) {
                        if (LoanRepayStatus.loanRepayIsCleared(loanRepay.getStatus())) {
                            number++;
                        }
                        if (null != loanRepay.getRepayAmount()) {
                            lastRepayId = loanRepay.getId();
                        }
                        receivableInterest = DecimalUtils.sum(receivableInterest, loanRepay.getInterest());
                        receivableAmount = DecimalUtils.sum(receivableAmount, loanRepay.getAmount());
                        actualAmount = DecimalUtils.sum(actualAmount, loanRepay.getRepayAmount());
                        actualInterest = DecimalUtils.sum(actualInterest, loanRepay.getRepayInterest());
                    }
                    if (number > 0) {
                        repay = loanRepayList.get(number - 1);
                    }
                    totalPeriod = loanRepayList.size();
                    m.setDueDate(getDueDate(loanRepayList.get(loanRepayList.size() - 1)));
                }
            }
            //实际结清日
            m.setActualClearedDate(getActualClearedDate(m.getStatus(), repay));
            //最近还款日
            m.setInterestBearingDate(reportUtilsService.getLastRepayDate(lastRepayId));
            m.setRepaymentNumber(String.valueOf(number) + "/" + totalPeriod);
            receivableTotal = receivableAmount.add(receivableInterest);
            m.setReceivableTotal(receivableTotal);
            m.setReceivableInterest(receivableInterest);
            m.setActualInterest(actualInterest);
            m.setActualAmount(actualAmount);
            m.setActualTotalAmount(actualInterest.add(actualAmount));
            m.setCommonInfo(reportUtilsService.getFeeInfo(m.getLoanId()));
            m.setActualTotalFeeAmount(actualInterest.add(m.getActualServiceTotalFeeAmount()));

            List<ProductInfoItem> productInfoItems = dao().query(ProductInfoItem.class, Cnd.where("loanId", "=", loanId));
            for (ProductInfoItem pt : productInfoItems) {
                if (("car_number").equals(pt.getKeyName())) {
                    m.setCarNumber(pt.getDataValue());
                }
                if (("car_type").equals(pt.getKeyName())) {
                    m.setCarBrand(pt.getDataValue());
                }
                if (("car_value").equals(pt.getKeyName())) {
                    m.setCarValuation(pt.getDataValue());
                }
            }
            //设置渠道和业务提供方
            String[] channelInfo = reportUtilsService.getChannelInfo(m.getChannelId());
            if (null != channelInfo) {
                m.setChannel(channelInfo[0]);
                m.setBusinessProvider(channelInfo[1]);
            }
            m.setTermType(m.getTermType());
            m.setInterestMode(ReportUtilsService.getInterestMode(m.getLoanLimitType(), m.getTermType()));
        });
        return dataTable;
    }

    //业务个人贷表格处理数据
    public DataTables queryBusinessGerendaiListForTable(DataTableBusinessReportParam param) {
//        String submitter = "";
//        String orgId = "";
//        String agencyId = "";
//        String loanSubjectId = "";
//        String status = "";
//        String type = getProductType("GERENDAI");
//        String repayDate = "";
//        String submitDate = "";
//        String clearedDate = "";
//        if (null != param.getSearchKeys()) {
//            Map<String, String> keys = param.getSearchKeys();
//            submitter = keys.get("submitter");
//            orgId = keys.get("orgId");
//            agencyId = keys.get("agencyId");
//            loanSubjectId = keys.get("loanSubjectId");
//            status = keys.get("status");
//            repayDate = keys.get("repayDate");
//            submitDate = keys.get("submitDate");
//            clearedDate = keys.get("clearedDate");
//
//        }
        String type = getProductType("GERENDAI");
        DataTables dataTable = (DataTables) this.queryGerenDaiAndCheDaiListForBusiness(param,type);
        if (dataTable.getRecordsTotal() < 1) {
            return dataTable;
        }
        List<GerendaiBusinessReportView> list = dataTable.getData();
        list.stream().forEach(m -> {
            m.setIdNumber(CoverUtil.coverIdNumber(m.getIdNumber()));
            String loanId = m.getLoanId();
            BigDecimal receivableInterest = BigDecimal.ZERO;
            BigDecimal receivableAmount = BigDecimal.ZERO;
            BigDecimal actualAmount = BigDecimal.ZERO;
            BigDecimal actualInterest = BigDecimal.ZERO;
            LoanRepay repay = null;
            int number = 0;
            int totalPeriod = 0;
            //应结清日
            m.setDueDate(null);
            //获取应还日期,实际结清日期
            m.setActualClearedDate(null);
            String lastRepayId = null;
            if (LoanStatus.isLoaned(m.getStatus())) {
                List<LoanRepay> loanRepayList = this.dao().query(LoanRepay.class, Cnd.where("loanId", "=", loanId).orderBy("period", "asc"));
                if (CollectionUtils.isNotEmpty(loanRepayList)) {
                    for (LoanRepay loanRepay : loanRepayList) {
                        if (LoanRepayStatus.loanRepayIsCleared(loanRepay.getStatus())) {
                            number++;
                        }
                        receivableInterest = DecimalUtils.sum(receivableInterest, loanRepay.getInterest());
                        receivableAmount = DecimalUtils.sum(receivableAmount, loanRepay.getAmount());
                        actualAmount = DecimalUtils.sum(actualAmount, loanRepay.getRepayAmount());
                        actualInterest = DecimalUtils.sum(actualInterest, loanRepay.getRepayInterest());
                        if (null != loanRepay.getRepayAmount()) {
                            lastRepayId = loanRepay.getId();
                        }
                    }
                    if (number > 0) {
                        repay = loanRepayList.get(number - 1);
                    }
                    totalPeriod = loanRepayList.size();
                    m.setDueDate(getDueDate(loanRepayList.get(loanRepayList.size() - 1)));
                }
            }

            m.setActualClearedDate(getActualClearedDate(m.getStatus(), repay));
            //最近还款日
            m.setInterestBearingDate(reportUtilsService.getLastRepayDate(lastRepayId));
            m.setRepaymentNumber(String.valueOf(number) + "/" + totalPeriod);
            m.setReceivableInterest(receivableInterest);
            m.setReceivableTotal(receivableAmount.add(receivableInterest));
            m.setActualInterest(actualInterest);
            m.setActualAmount(actualAmount);
            m.setActualTotalAmount(actualInterest.add(actualAmount));
            //TODO 设置费用及其其它信息
            m.setCommonInfo(reportUtilsService.getFeeInfo(loanId));
            m.setActualTotalFeeAmount(actualInterest.add(m.getActualServiceTotalFeeAmount()));
            //设置渠道和业务提供方
            String[] channelInfo = reportUtilsService.getChannelInfo(m.getChannelId());
            if (null != channelInfo) {
                m.setChannel(channelInfo[0]);
                m.setBusinessProvider(channelInfo[1]);
            }
            m.setTermType(m.getTermType());
            m.setInterestMode(ReportUtilsService.getInterestMode(m.getLoanLimitType(), m.getTermType()));
        });
        return dataTable;
    }

    private Date getDueDate(LoanRepay loanRepay) {
        if (null != loanRepay) {
            return loanRepay.getDueDate();
        }
        return null;
    }

    private Date getActualClearedDate(String status, LoanRepay loanRepay) {
        if (LoanStatus.CLEARED.name().equals(status) && null != loanRepay) {
            return loanRepay.getRepayDate();
        }
        return null;
    }

    public String getRepayMethod(String repayMethod) {
        if (StringUtils.isNotEmpty(repayMethod)) {
            for (LoanRepayMethod l : LoanRepayMethod.values()) {
                if (repayMethod.equals(l.toString())) {
                    return l.getDescription();
                }
            }
        }
        return null;
    }

    public String getTermType(String termType, String term) {
        if ("YEAS".equals(termType)) {
            return term + "年";
        } else if ("MOTHS".equals(termType)) {
            return term + "个月";
        } else if ("DAYS".equals(termType)) {
            return term + "天";
        } else if ("FIXED_DATE".equals(termType)) {
            return "至" + term;
        }
        return null;
    }

    public String getStatus(String status) {
        if (StringUtils.isNotEmpty(status)) {
            for (LoanStatus l : LoanStatus.values()) {
                if (status.equals(l.toString())) {
                    return l.getDescription();
                }
            }
        }
        return null;
    }

    public String getLoanRepayStatus(String status) {
        if (StringUtils.isNotEmpty(status)) {
            for (LoanRepayStatus l : LoanRepayStatus.values()) {
                if (status.equals(l.toString())) {
                    return l.getDescription();
                }
            }
        }
        return null;
    }

    public String getProductType(String key) {
        List<Map<String, String>> pt = ProductTypesUtils.getProductTypeList();
        for (Map<String, String> m : pt) {
            String codeVal = m.get("code");
            String nameVal = m.get("name");
            if (StringUtils.isNotEmpty(codeVal) && (key).equals(codeVal) && StringUtils.isNotEmpty(nameVal)) {
                return nameVal;
            }
        }
        return null;
    }

    public String getProductTypeByName(String name) {
        List<Map<String, String>> pt = ProductTypesUtils.getProductTypeList();
        for (Map<String, String> m : pt) {
            String codeVal = m.get("code");
            String nameVal = m.get("name");
            if (StringUtils.isNotEmpty(codeVal) && (name).equals(nameVal) && StringUtils.isNotEmpty(nameVal)) {
                return codeVal;
            }
        }
        return null;
    }

}

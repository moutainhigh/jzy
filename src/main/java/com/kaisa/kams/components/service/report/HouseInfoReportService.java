package com.kaisa.kams.components.service.report;

import com.kaisa.kams.components.params.report.DataTableBusinessReportParam;
import com.kaisa.kams.components.params.report.DataTableFinanceReportParam;
import com.kaisa.kams.components.service.ProductInfoItemService;
import com.kaisa.kams.components.service.base.ReportUtilsService;
import com.kaisa.kams.components.utils.CoverUtil;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.DecimalUtils;
import com.kaisa.kams.components.utils.TimeUtils;
import com.kaisa.kams.components.utils.report.ReportUtils;
import com.kaisa.kams.components.view.loan.Duration;
import com.kaisa.kams.components.view.report.BusinessReportLastRepayData;
import com.kaisa.kams.components.view.report.HouseBusinessHongBenReport;
import com.kaisa.kams.components.view.report.HouseBusinessShuLouReport;
import com.kaisa.kams.enums.FeeType;
import com.kaisa.kams.enums.LoanRepayMethod;
import com.kaisa.kams.enums.LoanRepayStatus;
import com.kaisa.kams.enums.LoanStatus;
import com.kaisa.kams.enums.LoanTermForRateType;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.HouseInfo;
import com.kaisa.kams.models.LoanExtension;
import com.kaisa.kams.models.LoanFee;
import com.kaisa.kams.models.LoanRepay;
import com.kaisa.kams.models.OldLoanRepay;
import com.kaisa.kams.models.ProductInfoItem;
import com.kaisa.kams.models.ProductRate;
import com.kaisa.kams.models.report.HouseReport;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by wangqx on 2017/10/17.
 */
@IocBean(fields = "dao")
public class HouseInfoReportService extends Service {

    @Inject
    private ProductInfoItemService productInfoItemService;
    @Inject
    private ReportUtilsService reportUtilsService;

    //红本赎楼表格处理数据
    public DataTables getFinanceReportForTable(DataTableFinanceReportParam param) {
        DataTables dataTable = houseReportPage(param);
        List<ProductRate> productRateList = dao().query(ProductRate.class, null);
        if (dataTable.getRecordsTotal() < 1) {
            return dataTable;
        }
        List<HouseReport> list = dataTable.getData();
        list.stream().forEach(m -> {
            String loanId = m.getLoanId();
            Date loanTime = m.getBusiness_date();
            String term = m.getLoan_term();
            BigDecimal yearRate = m.getYear_rate();
            String termType = m.getTermType();
            BigDecimal loanAmount = m.getLoan_amount();
            int totalDays = 0;
            BigDecimal receivablePrincipal = BigDecimal.ZERO;
            BigDecimal receivableInterest = BigDecimal.ZERO;
            BigDecimal otherCharges = BigDecimal.ZERO;
            BigDecimal financeCost = BigDecimal.ZERO;
            BigDecimal averageDailyIncome = BigDecimal.ZERO;
            BigDecimal averageDailyCost = BigDecimal.ZERO;
            BigDecimal monthlyIncome = BigDecimal.ZERO;
            BigDecimal monthlyCost = BigDecimal.ZERO;
            BigDecimal total = BigDecimal.ZERO;
            //实还本金
            BigDecimal repayAmount = BigDecimal.ZERO;
            //实还利息
            BigDecimal repayInterest = BigDecimal.ZERO;
            //实还费用
            BigDecimal repayFee = BigDecimal.ZERO;
            //实还总计
            BigDecimal repayTotal = BigDecimal.ZERO;

            switch (termType) {
                case "DAYS":
                    totalDays = Integer.parseInt(term);
                    m.setTotal_days(Integer.parseInt(term));
                    if (yearRate != null) {
                        m.setYear_rate(yearRate.multiply(new BigDecimal(365)));
                    }
                    break;
                case "MOTHS":
                    Duration duration_months = new Duration(0, Integer.parseInt(term), 0);
                    Date endDate_months = TimeUtils.offset(loanTime, duration_months);
                    totalDays = (int) ((endDate_months.getTime() - loanTime.getTime()) / (1000 * 24 * 3600));
                    m.setTotal_days(totalDays);
                    m.setYear_rate(yearRate.multiply(new BigDecimal(12)));
                    break;
                case "YEAS":
                    Duration duration_yeas = new Duration(Integer.parseInt(term), 0, 0);
                    Date endDate_yeas = TimeUtils.offset(loanTime, duration_yeas);
                    totalDays = (int) ((endDate_yeas.getTime() - loanTime.getTime()) / (1000 * 24 * 3600));
                    m.setTotal_days(totalDays);
                    break;
                case "FIXED_DATE":
                    Date endDate_fixed = TimeUtils.formatDate("yyyy-MM-dd", term);
                    totalDays = (int) ((endDate_fixed.getTime() - loanTime.getTime()) / (1000 * 24 * 3600));
                    m.setTotal_days(totalDays);
                    break;
                default:
                    break;
            }
            Date loanDueDate = null;
            Date clearedDate = null;
            BigDecimal rate = m.dataConversion(productRateList, totalDays);
            List<LoanRepay> loanRepayList = this.dao().query(LoanRepay.class, Cnd.where("loanId", "=", loanId).orderBy("period", "asc"));
            for (LoanRepay loanRepay : loanRepayList) {
                receivablePrincipal = receivablePrincipal.add(loanRepay.getAmount() == null ? new BigDecimal(0) : loanRepay.getAmount());
                receivableInterest = receivableInterest.add(loanRepay.getInterest() == null ? new BigDecimal(0) : loanRepay.getInterest());
                repayAmount = repayAmount.add(loanRepay.getRepayAmount() == null ? new BigDecimal(0) : loanRepay.getRepayAmount());
                repayInterest = repayInterest.add(loanRepay.getRepayInterest() == null ? new BigDecimal(0) : loanRepay.getRepayInterest());
                loanDueDate = loanRepay.getDueDate();
                clearedDate = loanRepay.getRepayDate();
            }
            m.setTermType(m.getTermType());
            m.setLoanDueDate(loanDueDate);

            //其他费用计算
            List<LoanFee> loanFeeList = this.dao().query(LoanFee.class, Cnd.where("loanId", "=", loanId).orderBy("period", "asc"));
            if (loanFeeList != null) {
                for (LoanFee loanFee : loanFeeList) {
                    if (loanFee.getRepayDate() != null) {
                        otherCharges = otherCharges.add(loanFee.getRepayFeeAmount() == null ? new BigDecimal(0) : loanFee.getRepayFeeAmount());
                    } else {
                        otherCharges = otherCharges.add(loanFee.getFeeAmount() == null ? new BigDecimal(0) : loanFee.getFeeAmount());
                    }
                    repayFee = repayFee.add(loanFee.getRepayFeeAmount() == null ? new BigDecimal(0) : loanFee.getRepayFeeAmount());
                }
            }
            total = receivablePrincipal.add(receivableInterest).add(otherCharges);
            repayTotal = repayAmount.add(repayInterest).add(repayFee);
            m.setReceivablePrincipal(receivablePrincipal);
            m.setRepayAmount(repayAmount);
            m.setOutstandingAmount(receivablePrincipal.subtract(repayAmount));
            m.setReceivableInterest(receivableInterest);
            m.setRepayInterest(repayInterest);
            m.setOutstandingInterest(ReportUtils.bigDecimalCompare(receivableInterest.subtract(repayInterest)));
            m.setOther_charges(otherCharges);
            m.setRepayFee(repayFee);
            m.setOutstandingFee(otherCharges.subtract(repayFee));
            m.setTotal(total);
            m.setRepayTotal(repayTotal);
            m.setOutstandingTotal(ReportUtils.bigDecimalCompare(total.subtract(repayTotal)));
            if (rate != null) {
                financeCost = loanAmount.multiply(new BigDecimal(totalDays)).divide(new BigDecimal(365), 10, BigDecimal.ROUND_HALF_EVEN).multiply(rate);
            }
            averageDailyIncome = total.divide(new BigDecimal(totalDays), 10, BigDecimal.ROUND_HALF_EVEN);
            averageDailyCost = financeCost.divide(new BigDecimal(totalDays), 10, BigDecimal.ROUND_HALF_EVEN);
            monthlyIncome = averageDailyIncome.multiply(new BigDecimal(TimeUtils.getDayOfMonth()));
            monthlyCost = averageDailyCost.multiply(new BigDecimal(TimeUtils.getDayOfMonth()));
            m.setAverage_daily_income(averageDailyIncome);
            m.setFinance_cost(financeCost);
            m.setAverage_daily_cost(averageDailyCost);
            m.setMonthly_income(monthlyIncome);
            m.setMonthly_cost(monthlyCost);
            m.setID_number(CoverUtil.coverIdNumber(m.getID_number()));
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

    /**
     * 财务报表-赎楼和红本
     */
    private DataTables houseReportPage(DataTableFinanceReportParam param) {
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());

        String[] loanStatus = {"LOANED", "OVERDUE", "CLEARED"};
        if (StringUtils.isNotEmpty(param.getStatus()) && ArrayUtils.isNotEmpty(param.getLoanedStatus())) {
            loanStatus =  param.getLoanedStatus();
        }
        String sqlStr = "SELECT  " +
                "l.id AS 'loanId', " +
                "l. CODE AS 'business_number', " +
                "p. NAME AS 'productType', " +
                "IFNULL(sc.name,'--') AS 'source', " +
                "l.saleName AS 'businesser', " +
                "lb.name AS 'borrower', " +
                "lb.certifNumber AS 'ID_number', " +
                "l.actualAmount AS 'loan_amount', " +
                "l.termType AS 'termType', " +
                "l.term AS 'loan_term', " +
                "l.interestRate AS 'year_rate', " +
                "l.loanTime AS 'business_date', " +
                "  ls.name AS 'loanSubject', " +
                "l.repayMethod AS 'repaymentMethods', " +
                "l.loanStatus AS 'status' " +
                "FROM  " +
                "sl_loan l " +
                "LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id  " +
                "LEFT JOIN sl_business_user u ON u.id = l.saleId  " +
                "LEFT JOIN sl_business_organize o ON u.organizeId = o.id  " +
                "LEFT JOIN sl_business_agency a ON o.agencyId = a.id  " +
                "LEFT JOIN sl_product p ON l.productId = p.id  " +
                "LEFT JOIN sl_product_type t ON p.typeId = t.id  " +
                "LEFT JOIN sl_channel sc ON l.channelId  = sc.id " +
                "LEFT JOIN sl_loan_borrower lb ON lb.loanId =l.id " +
                "  left join sl_loan_subject ls on ls.id = l.loanSubjectId " +
                " WHERE " +
                "l. STATUS = 'ABLE'  " +
                "AND t.name = @type " +
                "AND lb.master = '1' " +
                "AND l.loanTime is not null " +
                "AND l.loanTime>=@loanBeginDate  AND l.loanTime<=@loanEndDate " +
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
                "LEFT JOIN sl_product_type t ON p.typeId = t.id  " +
                "LEFT JOIN sl_channel sc ON l.channelId  =  sc.id " +
                "LEFT JOIN sl_loan_borrower lb ON lb.loanId =l.id " +
                "  left join sl_loan_subject ls on ls.id = l.loanSubjectId " +
                " WHERE " +
                "l. STATUS = 'ABLE'  " +
                "AND t.name = @type " +
                "AND lb.master = '1' " +
                "AND l.loanTime is not null " +
                "AND l.loanTime>=@loanBeginDate  AND l.loanTime<=@loanEndDate " +
                "AND l.loanStatus in(@loanStatus) ";
        String loanSubject = param.getLoanSubject();
        if (StringUtils.isNotEmpty(loanSubject)) {
            sqlStr += " AND  ls.id=@loanSubject ";
            countSqlStr += " AND  ls.id=@loanSubject ";
        }
        Date loanBeginDate = param.getBeginDateTime();
        Date loanEndDate = param.getEndDateTime();
        String type = param.getProductTypeName();
        sqlStr += " GROUP BY l.code ORDER BY l.loanTime ASC";
        Sql sql = Sqls.create(sqlStr);
        sql.setParam("loanStatus", loanStatus);
        sql.setParam("loanSubject", loanSubject);
        sql.setParam("loanBeginDate", loanBeginDate);
        sql.setParam("loanEndDate", loanEndDate);
        sql.setParam("type", type);
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(HouseReport.class));
        dao().execute(sql);
        List<HouseReport> list = sql.getList(HouseReport.class);
        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("loanStatus", loanStatus);
        countSql.setParam("loanSubject", loanSubject);
        countSql.setParam("loanBeginDate", loanBeginDate);
        countSql.setParam("loanEndDate", loanEndDate);
        countSql.setParam("type", type);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        return new DataTables(param.getDraw(), count, count, list);

    }


    // 业务报表查询-赎楼和红本

    private DataTables queryShuLouAndHongBenListForBusiness(DataTableBusinessReportParam param,String type) {

        List<HouseBusinessShuLouReport> listS = null;
        List<HouseBusinessHongBenReport> listH = null;
        String sqlStr = "SELECT  " +
                "lr.id AS 'id', " +
                "l.id AS 'loanId', " +
                "l. CODE AS 'businessOrderNo', " +
                "p. NAME AS 'productName', " +
                "IFNULL(ls.name,'--') AS 'loanSubject', " +
                "IFNULL(USER.name,'--') AS 'submitter', " +
                "l.submitTime AS 'submitTime', " +
                "l.saleName AS 'businessSource', " +
                "lb.name AS 'borrower', " +
                "lb.certifNumber AS 'idNumber', " +
                "IFNULL(l.channelId,'--') AS 'channelId'," +
                "sc.name AS 'channel'," +
                "IFNULL(a.name,'--') AS 'agencyName'," +
                "IFNULL(o.name,'--') AS 'organizeName'," +
                "IFNULL(o.businessLine,'--') AS 'businessLine'," +
                "IFNULL(o.code,'--') AS 'orgCode'," +
                "l.actualAmount AS 'loanPrincipal', " +
                "l.termType AS 'termType', " +
                "l.term AS 'loanTerm', " +
                "l.interestRate AS 'borrowRate', " +
                "l.loanTime AS 'loanTime', " +
                "lr.dueDate AS 'dueDate', " +
                "lr.repayDate AS 'actualClearedDate', " +
                "l.repayMethod AS 'repayMethod', " +
                "lr.amount AS 'receivableTotal', " +
                "lr.interest AS 'receivableInterest', " +
                "l.loanLimitType AS 'loanLimitType', " +
                "slp.profit as profit, " +
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
                "LEFT JOIN sl_loan_profit slp ON l.id = slp.loanId " +
                "LEFT JOIN sl_loan_subject ls ON ls.id = l.loanSubjectId " +
                "LEFT JOIN sl_user USER ON USER.id = l.applyId " +
                "LEFT JOIN sl_channel sc ON sc.id = l.channelId " +
                "WHERE " +
                "l. STATUS = 'ABLE'  " +
                "AND lb.master = '1' " +
                "AND t.productTempType = @type ";

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
                "AND lb.master = '1' " +
                "AND t.productTempType = @type ";

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
            sqlStr += " AND l.clearDate>= @beginActualClearedDate ";
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

        String productName = param.getProductName();
        if (StringUtils.isNotEmpty(productName)) {
            sqlStr += " AND  p.name like @productName ";
            countSqlStr += " AND  p.name like @productName ";
        }

        String loanStatus = param.getLoanStatus();
        if (StringUtils.isNotEmpty(loanStatus)) {
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
        sql.setParam("productName", '%' + productName + '%');
        sql.setParam("status", loanStatus);
        sql.setParam("beginDate", beginDate);
        sql.setParam("endDate", endDate);
        sql.setParam("start", param.getStart());
        sql.setParam("length", param.getLength());
        if ("SHULOU".equals(type)) {
            sql.setCallback(Sqls.callback.entities());
            sql.setEntity(dao().getEntity(HouseBusinessShuLouReport.class));
            dao().execute(sql);
            listS = sql.getList(HouseBusinessShuLouReport.class);
        } else if ("HONGBEN".equals(type)) {
            sql.setCallback(Sqls.callback.entities());
            sql.setEntity(dao().getEntity(HouseBusinessHongBenReport.class));
            dao().execute(sql);
            listH = sql.getList(HouseBusinessHongBenReport.class);
        }
        countSql.setParam("submitter", '%' + submitter + '%');
        countSql.setParam("beginSubmitDate", beginSubmitDate);
        countSql.setParam("endSubmitDate", endSubmitDate);
        countSql.setParam("channelId", channelId);
        countSql.setParam("beginActualClearedDate", beginActualClearedDate);
        countSql.setParam("endActualClearedDate", endActualClearedDate);
        countSql.setParam("orgId", orgId);
        countSql.setParam("agencyId", agencyId);
        countSql.setParam("loanSubject", loanSubject);
        countSql.setParam("productName", '%' + productName + '%');
        countSql.setParam("status", loanStatus);
        countSql.setParam("type", type);
        countSql.setParam("beginDate", beginDate);
        countSql.setParam("endDate", endDate);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        DataTables dataTables = null;
        if ("SHULOU".equals(type)) {
            dataTables = new DataTables(param.getDraw(), count, count, listS);
        } else if ("HONGBEN".equals(type)) {
            dataTables = new DataTables(param.getDraw(), count, count, listH);
        }
        return dataTables;
    }


    //业务红本表格处理数据
    public DataTables getBusinessHongBenReportForTable(DataTableBusinessReportParam param) {
//        String submitter = "";
//        String orgId = "";
//        String agencyId = "";
//        String loanSubjectId = "";
//        String productName = "";
//        String status = "";
//        String type = "HONGBEN";
//        String repayDate = "";
//        String submitDate = "";
//        String clearedDate = "";
//        String channelId = "";
//        if (null != param.getSearchKeys()) {
//            Map<String, String> keys = param.getSearchKeys();
//            submitter = keys.get("submitter");
//            orgId = keys.get("organizeId");
//            agencyId = keys.get("agencyId");
//            loanSubjectId = keys.get("loanSubjectId");
//            productName = keys.get("productName");
//            status = keys.get("loanStatus");
//            repayDate = keys.get("reportTime");
//            submitDate = keys.get("submitDate");
//            clearedDate = keys.get("clearedDate");
//            channelId = keys.get("channelId");
//
//        }
        String type = "HONGBEN";
        DataTables dataTable = queryShuLouAndHongBenListForBusiness(param,type);
        if (dataTable.getRecordsTotal() < 1) {
            return dataTable;
        }
        List<HouseBusinessHongBenReport> list = dataTable.getData();
        list.stream().forEach(m -> {
            m.setIdNumber(CoverUtil.coverIdNumber(m.getIdNumber()));
            String loanId = m.getLoanId();
            BigDecimal receivableInterest = BigDecimal.ZERO;
            BigDecimal receivableAmount = BigDecimal.ZERO;
            BigDecimal actualInterest = BigDecimal.ZERO;
            BigDecimal actualAmount = BigDecimal.ZERO;
            BigDecimal totalServiceFeeAmount = BigDecimal.ZERO;
            BigDecimal actualTotalServiceFeeAmount = BigDecimal.ZERO;
            BigDecimal actualOverdueFeeAmount = BigDecimal.ZERO;
            int number = 0;
            m.setNearestRepayDate("");
            //实际结清时间
            m.setActualClearedDate("");
            //应结清日
            m.setDueDate("");
            m.setNearestRepayDate("");
            m.setNearestRepayAmount(BigDecimal.ZERO);
            LoanRepay repay = null;
            int totalPeriod = 0;
            //
            String lastRepayId = null;
            if (LoanStatus.isLoaned(m.getStatus())) {
                //TODO 还款计划信息
                List<LoanRepay> loanRepayList = dao().query(LoanRepay.class,
                        Cnd.where("loanId", "=", loanId).orderBy("period", "asc"));
                if (CollectionUtils.isNotEmpty(loanRepayList)) {
                    for (LoanRepay loanRepay : loanRepayList) {
                        if (LoanRepayStatus.loanRepayIsCleared(loanRepay.getStatus())) {
                            number++;
                        }
                        receivableInterest = DecimalUtils.sum(receivableInterest, loanRepay.getInterest());
                        receivableAmount = DecimalUtils.sum(receivableAmount, loanRepay.getAmount());
                        actualInterest = DecimalUtils.sum(actualInterest, loanRepay.getRepayInterest());
                        actualAmount = DecimalUtils.sum(actualAmount, loanRepay.getRepayAmount());
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

                //TODO 费用信息
                List<LoanFee> loanFeeList = dao().query(LoanFee.class, Cnd.where("loanId", "=", loanId).orderBy("period", "asc"));
                if (CollectionUtils.isNotEmpty(loanFeeList)) {
                    for (LoanFee loanFee : loanFeeList) {
                        totalServiceFeeAmount = DecimalUtils.sum(totalServiceFeeAmount, loanFee.getFeeAmount());
                        actualTotalServiceFeeAmount = DecimalUtils.sum(actualTotalServiceFeeAmount, loanFee.getRepayFeeAmount());
                        actualOverdueFeeAmount = DecimalUtils.sum(actualOverdueFeeAmount, getActualOverDueFeeAmount(loanFee));
                    }
                }
            }
            BusinessReportLastRepayData data = reportUtilsService.getLastRepayData(lastRepayId);
            m.setNearestRepayDate(data.getLastRepayDate());
            m.setNearestRepayAmount(data.getLastRepayAmount());
            m.setActualClearedDate(getActualClearedDate(m.getStatus(), repay));
            m.setRepaymentNumber(String.valueOf(number) + "/" + totalPeriod);
            m.setReceivableTotal(receivableAmount.add(receivableInterest));
            m.setReceivableInterest(receivableInterest);
            m.setActualInterest(actualInterest);
            m.setActualAmount(actualAmount);
            m.setActualTotalAmount(actualAmount.add(actualInterest));
            m.setTotalServiceFeeAmount(totalServiceFeeAmount);
            m.setActualServiceTotalFeeAmount(actualTotalServiceFeeAmount);
            m.setActualOverdueFeeAmount(actualOverdueFeeAmount);
            m.setActualTotalFeeAmount(actualTotalServiceFeeAmount.add(actualInterest));
            //实际放款时间处理
            m.setActualLoanDate(DateUtil.formatDateToString(m.getLoanTime()));
            //设置渠道和业务提供方
            String[] channelInfo = reportUtilsService.getChannelInfo(m.getChannelId());
            if (null != channelInfo) {
                m.setChannel(channelInfo[0]);
                m.setBusinessProvide(channelInfo[1]);
            }
            //修改房产取数表
            List<HouseInfo> houseInfoList = queryByLoanId(loanId);
            if (CollectionUtils.isNotEmpty(houseInfoList)) {
                m.setPropertyRightNo(houseInfoList.get(0).getCode());
                m.setHouseAddress(houseInfoList.get(0).getAddress());
            }

            m.setLoanTerm(getLoanTerm(m.getTermType(), m.getLoanTerm()));
            //状态转换
            m.setStatus(LoanStatus.valueOf(m.getStatus()).getDescription());
            //还款方式转换
            m.setRepayMethod(getRepayMethodDescription(m.getRepayMethod()));
            //计息方式
            m.setInterestMode(ReportUtilsService.getInterestMode(m.getLoanLimitType(), m.getTermType()));
        });
        return dataTable;
    }

    //业务赎楼表格处理数据
    public DataTables getBusinessShuLouReportForTable(DataTableBusinessReportParam param) {
        String type = "SHULOU";
        DataTables dataTable = queryShuLouAndHongBenListForBusiness(param,type);
        if (dataTable.getRecordsTotal() < 1) {
            return dataTable;
        }
        List<HouseBusinessShuLouReport> list = dataTable.getData();
        list.stream().forEach(m -> {
            String loanId = m.getLoanId();
            BigDecimal receivableInterest = BigDecimal.ZERO;
            BigDecimal receivableAmount = BigDecimal.ZERO;
            BigDecimal repayInterest = BigDecimal.ZERO;
            BigDecimal actualAmount = BigDecimal.ZERO;
            LoanRepay repay = null;
            //应结清日
            m.setDueDate("");
            //实际结清时间
            m.setActualClearedDate("");
            int number = 0;
            m.setIdNumber(CoverUtil.coverIdNumber(m.getIdNumber()));
            if (LoanStatus.isLoaned(m.getStatus())) {
                List<LoanRepay> loanRepayList = dao().query(LoanRepay.class, Cnd.where("loanId", "=", loanId).orderBy("period", "asc"));
                if (CollectionUtils.isNotEmpty(loanRepayList)) {
                    for (LoanRepay loanRepay : loanRepayList) {
                        if (LoanRepayStatus.loanRepayIsCleared(loanRepay.getStatus())) {
                            number++;
                        }
                        if (number > 0) {
                            repay = loanRepayList.get(number - 1);
                        }
                        receivableInterest = DecimalUtils.sum(receivableInterest, loanRepay.getInterest());
                        receivableAmount = DecimalUtils.sum(receivableAmount, loanRepay.getAmount());
                        repayInterest = DecimalUtils.sum(repayInterest, loanRepay.getRepayInterest());
                        actualAmount = DecimalUtils.sum(actualAmount, loanRepay.getRepayAmount());
                    }
                    m.setDueDate(getDueDate(loanRepayList.get(loanRepayList.size() - 1)));

                }
            }
            m.setReceivableTotal(receivableAmount.add(receivableInterest));
            m.setReceivableInterest(receivableInterest);
            m.setActualInterest(repayInterest);
            m.setActualAmount(actualAmount);
            m.setActualTotalAmount(actualAmount.add(repayInterest));
            //获取实际结清时间
            m.setActualClearedDate(getActualClearedDate(m.getStatus(), repay));
            //实际放款时间处理
            m.setActualLoanDate(DateUtil.formatDateToString(m.getLoanTime()));
            //费用信息
            m.setFeeInfo(reportUtilsService.getFeeInfo(loanId));
            m.setActualTotalFeeAmount(DecimalUtils.sum(m.getActualServiceTotalFeeAmount(), repayInterest));

            //设置渠道和业务提供方
            String[] channelInfo = reportUtilsService.getChannelInfo(m.getChannelId());
            if (null != channelInfo) {
                m.setChannel(channelInfo[0]);
                m.setBusinessProvide(channelInfo[1]);
            }
            m.setTermType(m.getTermType());
            // 是否展期
            int extensionDays = 0;
            int actualExtensionDays = 0;
            List<LoanRepay> extensionList = null;
            m.setExtensionDueDate(null);
            boolean isExtension = dao().func(LoanExtension.class, "count", "id", Cnd.where("loanId", "=", loanId)) > 0;
            m.setIsExtension("否");
            m.setExtensionDueDate(null);
            if (isExtension) {
                List<OldLoanRepay> oldLoanRepayList = dao().query(OldLoanRepay.class, Cnd.where("loanId", "=", loanId).and("position", "=", 0).orderBy("period", "desc"));
                List<LoanRepay> nowList = dao().query(LoanRepay.class, Cnd.where("loanId", "=", loanId).orderBy("period", "desc"));
                m.setExtensionDueDate(DateUtil.formatDateToString(nowList.get(0).getDueDate()));
                if ((LoanStatus.CLEARED).equals(m.getStatus())) {
                    extensionList = this.dao().query(LoanRepay.class, Cnd.where("loanId", "=", loanId).and("status", "in", new LoanRepayStatus[]{LoanRepayStatus.CLEARED, LoanRepayStatus.OVERDUE_CLEARED, LoanRepayStatus.AHEAD_CLEARED}).orderBy("period", "desc"));
                    m.setExtensionDueDate(DateUtil.formatDateToString(nowList.get(0).getRepayDate()));
                }
                if (CollectionUtils.isNotEmpty(oldLoanRepayList)) {
                    extensionDays = (int) ((nowList.get(0).getDueDate().getTime() - oldLoanRepayList.get(0).getDueDate().getTime()) / (1000 * 24 * 3600));
                    m.setExtensionNumber(String.valueOf(extensionDays));
                    if (CollectionUtils.isNotEmpty(extensionList)) {
                        actualExtensionDays = (int) ((extensionList.get(0).getRepayDate().getTime() - oldLoanRepayList.get(0).getDueDate().getTime()) / (1000 * 24 * 3600));
                        m.setActualExtensionNumber(String.valueOf(actualExtensionDays));
                    } else {
                        m.setActualExtensionNumber(null);
                    }
                } else {
                    m.setExtensionNumber(null);
                }
                m.setIsExtension("是");
            }
            // 赎楼银行和审批银行
            List<ProductInfoItem> productInfoItemList = productInfoItemService.queryByLoanId(loanId);
            if (CollectionUtils.isNotEmpty(productInfoItemList)) {
                for (ProductInfoItem productInfoItem : productInfoItemList) {
                    if ("ransom_bank".equals(productInfoItem.getKeyName())) {
                        m.setRedeemFloorBank(productInfoItem.getDataValue());
                    }
                    if ("approval_bank".equals(productInfoItem.getKeyName())) {
                        m.setApprovalBank(productInfoItem.getDataValue());
                    }
                }
            }
            m.setLoanTerm(getLoanTerm(m.getTermType(), m.getLoanTerm()));
            //状态转换
            m.setStatus(LoanStatus.valueOf(m.getStatus()).getDescription());
            //还款方式转换
            m.setRepayMethod(getRepayMethodDescription(m.getRepayMethod()));
            //计息方式
            m.setInterestMode(ReportUtilsService.getInterestMode(m.getLoanLimitType(), m.getTermType()));
        });
        return dataTable;
    }


    private BigDecimal getActualOverDueFeeAmount(LoanFee loanFee) {
        if (FeeType.OVERDUE_FEE.equals(loanFee.getFeeType())) {
            return loanFee.getRepayFeeAmount();
        }
        return BigDecimal.ZERO;
    }

    private String getDueDate(LoanRepay loanRepay) {
        if (null != loanRepay && null != loanRepay.getDueDate()) {
            return DateUtil.formatDateToString(loanRepay.getDueDate());
        }
        return "";
    }

    private String getRepayMethodDescription(String repayMethod) {
        if (StringUtils.isEmpty(repayMethod)) {
            return "--";
        }
        return LoanRepayMethod.valueOf(repayMethod).getDescription();
    }

    private String getActualClearedDate(String status, LoanRepay loanRepay) {
        if (LoanStatus.CLEARED.name().equals(status) && null != loanRepay.getRepayDate()) {
            return DateUtil.formatDateToString(loanRepay.getRepayDate());
        }
        return "";
    }

    private String getLoanTerm(String termType, String term) {
        if (null != termType && "FIXED_DATE".equals(termType)) {
            return "至" + term;
        }
        if (null != termType) {
            return term + LoanTermForRateType.valueOf(termType).getDescription();
        }
        return "--";
    }

    private List<HouseInfo> queryByLoanId(String loanId) {
        return dao().query(HouseInfo.class, Cnd.where("loanId", "=", loanId).orderBy("position", "asc"));
    }

}

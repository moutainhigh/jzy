package com.kaisa.kams.components.service;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.PdfUtil;
import com.kaisa.kams.components.utils.ApiPropsUtils;
import com.kaisa.kams.components.utils.ApiRequestUtil;
import com.kaisa.kams.components.utils.TextFormatUtils;
import com.kaisa.kams.components.utils.TimeUtils;
import com.kaisa.kams.enums.FeeChargeNode;
import com.kaisa.kams.enums.FeeType;
import com.kaisa.kams.enums.LoanRepayStatus;
import com.kaisa.kams.enums.ProductTempType;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.business.BusinessOrganize;
import com.kaisa.kams.models.business.BusinessUser;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;


/**
 * 贷后管理服务层
 * Created by lw on 2016/12/12.
 */
@IocBean(fields = "dao")
public class PostLoanService extends IdNameEntityService<LoanRepay> {
    private static final Log log = Logs.get();

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String  localStartDate = sdf.format(new Date())+" 00:00:00";
    String  localEndDateForToday = sdf.format(new Date())+" 23:59:59";
    String localEndDate = sdf.format(DateUtil.getDateAfter(new Date(),7))+" 23:59:59";
    String localTodayDate = sdf.format(new Date())+" 23:59:59";
    String localBefore7Days = sdf.format(DateUtil.getDateBefore(new Date(),7))+" 00:00:00";

    @Inject
    private LoanService loanService;
    @Inject
    private LoanBorrowerService loanBorrowerService;
    @Inject
    private BusinessUserService businessUserService;
    @Inject
    private UserService userService;
    @Inject
    private BusinessOrganizeService businessOrganizeService;
    @Inject
    private BillLoanService billLoanService;
    @Inject
    private ChannelService channelService;


    /**
     * 查询最近3天内有效的待还款记录
     */
    public DataTables queryPostLoanList(DataTableParam param){
        String agencyId = "";
        String orgId = "";
        String businessLine = "";
        String borrower = "";
        String repayDate = "";
        String productTypeId = "";
        String channelId = "";
        String product = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            agencyId = keys.get("agencyId");
            orgId = keys.get("orgId");
            businessLine = keys.get("businessLine");
            borrower = keys.get("borrower");
            repayDate = keys.get("repayDate");
            productTypeId = keys.get("productType");
            channelId = keys.get("channelId");
            product = keys.get("product");

        }
        String sqlStr = "SELECT " +
                "lr.id AS 'id', " +
                "lr.loanId AS 'loanId', " +
                "blr.repayId AS 'billRepayId', " +
                "l. CODE AS 'code', " +
                "pt.NAME AS 'productTypeName', " +
                " IFNULL(p.name,'--') AS 'productName'," +
                "l.saleName AS 'saleName', " +
                "u.id AS 'businessUserId', " +
                "l.applyId AS 'applyer', " +
                "l.saleName AS 'buName', " +
                "lb.name AS 'borrserName', " +
                "IF(blr.actualDueDate IS NULL , lr.dueDate,blr.actualDueDate) AS 'dueDate', " +
                "lr.amount AS 'amount', " +
                "lr.interest AS 'interest', " +
                "lr.period AS 'period', " +
                "lr. STATUS AS 'status', " +
                "o.NAME AS 'organizeName', " +
                "a.NAME AS 'agencyName', " +
                "blr.overdueDays AS 'overdueDays', " +
                " l.channelId AS 'channelId',"+
                " o.businessLine AS 'businessLine',"+
                " o.code AS 'orgCode',"+
                "t.productTempType AS 'productTempType' " +
                "FROM " +
                "sl_loan_repay lr " +
                "LEFT JOIN sl_loan l ON lr.loanId = l.id " +
                "LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id " +
                "LEFT JOIN sl_business_user u ON u.id = l.saleId " +
                "LEFT JOIN sl_business_organize o ON u.organizeId = o.id " +
                "LEFT JOIN sl_business_agency a ON o.agencyId = a.id " +
                "LEFT JOIN sl_product p ON l.productId = p.id " +
                "LEFT JOIN sl_product_info_tmpl t ON p.infoTmpId = t.id " +
                "LEFT JOIN sl_bill_loan_repay blr ON lr.id = blr.repayId " +
                "LEFT JOIN (select name,loanId from sl_loan_borrower where master=1)lb ON lb.loanId=l.id " +
                "WHERE " +
                "l. STATUS = 'ABLE' " +
                "AND lr. STATUS = 'LOANED' " +
                "AND l.loanStatus IN ('LOANED', 'OVERDUE')";

        String countSqlStr = "SELECT "+
                " COUNT(DISTINCT lr.id) AS 'number' "+
                "FROM " +
                "sl_loan_repay lr " +
                "LEFT JOIN sl_loan l ON lr.loanId = l.id " +
                "LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id " +
                "LEFT JOIN sl_business_user u ON u.id = l.saleId " +
                "LEFT JOIN sl_business_organize o ON u.organizeId = o.id " +
                "LEFT JOIN sl_business_agency a ON o.agencyId = a.id " +
                "LEFT JOIN sl_product p ON l.productId = p.id " +
                "LEFT JOIN sl_product_info_tmpl t ON p.infoTmpId = t.id " +
                "LEFT JOIN sl_bill_loan_repay blr ON lr.id = blr.repayId " +
                "WHERE " +
                "l. STATUS = 'ABLE' " +
                "AND lr. STATUS = 'LOANED' " +
                "AND l.loanStatus IN ('LOANED', 'OVERDUE')";

        if (StringUtils.isNotEmpty(agencyId)){
            sqlStr+=" AND a.id=@agencyId ";
            countSqlStr+=" AND a.id=@agencyId ";
        }
        if (StringUtils.isNotEmpty(orgId)){
            sqlStr+=" AND o.id=@orgId ";
            countSqlStr+=" AND o.id=@orgId ";
        }

        if (StringUtils.isNotEmpty(businessLine)){
            sqlStr+=" AND  o.businessLine=@businessLine ";
            countSqlStr+=" AND  o.businessLine=@businessLine ";
        }

        if (StringUtils.isNotEmpty(productTypeId)){
            sqlStr+=" AND  l.productTypeId=@productTypeId ";
            countSqlStr+=" AND  l.productTypeId=@productTypeId ";
        }

        if (StringUtils.isNotEmpty(channelId)) {
            sqlStr+=" AND  l.channelId=@channelId ";
            countSqlStr+=" AND  l.channelId=@channelId ";
        }

        if (StringUtils.isNotEmpty(borrower)){
            sqlStr+=" AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
            countSqlStr+=" AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
        }
        if (StringUtils.isNotEmpty(product)) {
            sqlStr += " AND  l.productId=@productId ";
            countSqlStr += " AND  l.productId=@productId ";
        }

        Date beginDate = TimeUtils.getQueryStartDateTime(repayDate);
        Date endDate = TimeUtils.getQueryEndDateTime(repayDate);
        String queryRepayDateSql = getQueryRepayDateSql(beginDate, endDate);
        sqlStr += queryRepayDateSql;
        countSqlStr += queryRepayDateSql;

        sqlStr+=" ORDER BY lr.dueDate ASC "+
                " LIMIT @start,@length ";
        Sql sql = Sqls.create(sqlStr);
        Sql countSql = Sqls.create(countSqlStr);
        sql.setParam("agencyId",agencyId);
        sql.setParam("orgId",orgId);
        sql.setParam("businessLine",businessLine);
        sql.setParam("productTypeId",productTypeId);
        sql.setParam("borrower",'%'+borrower+'%');
        sql.setParam("beginDate",beginDate);
        sql.setParam("endDate",endDate);
        sql.setParam("start",param.getStart());
        sql.setParam("length",param.getLength());
        sql.setParam("channelId",channelId);
        sql.setParam("productId", product);
        sql.setCallback(Sqls.callback.maps());
        dao().execute(sql);
        List<Map> list = sql.getList(Map.class);
        if (CollectionUtils.isNotEmpty(list)) {
            for(Map<String,String> map:list) {
                map.put("saleName",loanService.getBusinessSource(map.get("channelId"), map.get("businessLine"),map.get("orgCode"), map.get("saleName")));
            }
        }
        countSql.setParam("agencyId",agencyId);
        countSql.setParam("orgId",orgId);
        countSql.setParam("businessLine",businessLine);
        countSql.setParam("productTypeId",productTypeId);
        countSql.setParam("borrower",'%'+borrower+'%');
        countSql.setParam("beginDate",beginDate);
        countSql.setParam("endDate",endDate);
        countSql.setParam("channelId",channelId);
        countSql.setParam("productId", product);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();

        return new DataTables(param.getDraw(),count,count,list);

    }

    private String getQueryRepayDateSql(Date beginDate, Date endDate) {
        if (null != beginDate && null != endDate){
            return "AND IF(blr.overdueDays IS NULL , lr.dueDate,blr.actualDueDate)>=@beginDate  AND IF(blr.overdueDays IS NULL , lr.dueDate, blr.actualDueDate)<=@endDate ";
        }
        return " AND IF(blr.overdueDays IS NULL , lr.dueDate, blr.actualDueDate)>='" + localStartDate + "'  AND IF(blr.overdueDays IS NULL , lr.dueDate, blr.actualDueDate)<='" + localEndDateForToday + "' ";
    }

    //快到期表格处理数据
    public Object queryPostLoanListForTable(DataTableParam param){

        DataTables dataTable = queryPostLoanList(param);
        if(dataTable.getRecordsTotal()<1){
            return dataTable;
        }
        List<Map> list = dataTable.getData();
        list.stream().forEach(m->{
            String repayId = (String)m.get("id");
            String loanId = (String)m.get("loanId");
            int number = this.dao().count(LoanRepay.class,Cnd.where("loanId","=",loanId).orderBy("period","asc"));
            List<LoanRepay> loanRepayList = this.dao().query(LoanRepay.class,Cnd.where("id","=",repayId).orderBy("period","asc"));
            LoanRepay nextLoanRepay = null;
            for(LoanRepay loanRepay:loanRepayList){
                if(LoanRepayStatus.LOANED.equals(loanRepay.getStatus())){
                    nextLoanRepay = loanRepay;
                    break;
                }
            }
            String productTempType = (String)m.get("productTempType");
            m.put("period",ProductTempType.isBill(ProductTempType.valueOf(productTempType))?"--": nextLoanRepay.getPeriod()+"/"+number);
        });
        return dataTable;
    }

    public DataTables queryOverdueList(DataTableParam param) {
        String agencyId = "";
        String orgId = "";
        String businessLine = "";
        String borrower = "";
        String beginDay = "";
        String endDay = "";
        String productType = "";
        String product = "";
        String channelId = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            agencyId = keys.get("agencyId");
            orgId = keys.get("orgId");
            businessLine = keys.get("businessLine");
            borrower = keys.get("borrower");
            beginDay = keys.get("beginDay");
            endDay = keys.get("endDay");
            productType = keys.get("productType");
            channelId = keys.get("channelId");
            product = keys.get("product");
        }
        String sqlStr = "SELECT  " +
                "lr.id AS 'id', " +
                "l.id AS 'loanId', " +
                "l.code AS 'code', " +
                "pt.name AS 'productTypeName', " +
                " IFNULL(p.name,'--') AS 'productName'," +
                "l.saleName AS 'saleName', " +
                "lb.name AS 'borrserName', " +
                "IF(blr.actualDueDate IS NULL , lr.dueDate,blr.actualDueDate) AS 'dueDate', " +
                "lr.overdueDays AS dueDays, " +
                " lr.amount AS 'amount', " +
                " lr.interest AS 'interest', " +
                " lr.period AS 'period', " +
                " l. loanStatus AS 'status', " +
                " o.name AS 'organizeName', " +
                " a.name AS 'agencyName', " +
                " IFNULL(l.channelId,'--') AS 'channelId',"+
                " IFNULL(o.businessLine,'--') AS 'businessLine',"+
                " IFNULL(o.code,'--') AS 'orgCode',"+
                " t.productTempType AS 'productTempType' " +
                "FROM  " +
                "sl_loan l " +
                "LEFT JOIN (select l1.* from sl_loan_repay l1,(select min(dueDate) minDueDate,loanId from sl_loan_repay where status='OVERDUE' GROUP BY loanId)l2 where l1.dueDate=l2.minDueDate and l1.loanId=l2.loanId) lr ON lr.loanId = l.id  " +
                "LEFT JOIN  sl_bill_loan_repay blr ON lr.id = blr.repayId " +
                "LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id " +
                "LEFT JOIN sl_business_user u ON u.id = l.saleId " +
                "LEFT JOIN sl_business_organize o ON u.organizeId = o.id " +
                "LEFT JOIN sl_business_agency a ON o.agencyId = a.id " +
                "LEFT JOIN sl_product p ON l.productId = p.id " +
                "LEFT JOIN sl_product_info_tmpl t ON p.infoTmpId = t.id " +
                "LEFT JOIN (SELECT name,loanId FROM sl_loan_borrower WHERE master = 1)lb ON lb.loanId =l.id "+
                "WHERE " +
                " l. loanStatus = 'OVERDUE' ";

        String countSqlStr = "SELECT " +
                " COUNT(DISTINCT l.code) AS 'number' " +
                " FROM " +
                "sl_loan l " +
                "LEFT JOIN sl_loan_repay lr ON lr.loanId = l.id " +
                "LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id " +
                "LEFT JOIN sl_business_user u ON u.id = l.saleId " +
                "LEFT JOIN sl_business_organize o ON u.organizeId = o.id " +
                "LEFT JOIN sl_business_agency a ON o.agencyId = a.id " +
                "LEFT JOIN sl_product p ON l.productId = p.id " +
                "LEFT JOIN (SELECT name,loanId FROM sl_loan_borrower WHERE master = 1)lb ON lb.loanId =l.id "+
                "WHERE " +
                " l. loanStatus = 'OVERDUE' ";

        if (StringUtils.isNotEmpty(agencyId)) {
            sqlStr += " AND a.id=@agencyId ";
            countSqlStr += " AND a.id=@agencyId ";
        }
        if (StringUtils.isNotEmpty(orgId)) {
            sqlStr += " AND o.id=@orgId ";
            countSqlStr += " AND o.id=@orgId ";
        }

        if (StringUtils.isNotEmpty(businessLine)) {
            sqlStr += " AND  o.businessLine=@businessLine ";
            countSqlStr += " AND  o.businessLine=@businessLine ";
        }

        if (StringUtils.isNotEmpty(productType) ) {
            sqlStr += " AND  l.productTypeId=@productTypeId ";
            countSqlStr += " AND  l.productTypeId=@productTypeId ";
        }

        if (StringUtils.isNotEmpty(borrower)) {
            sqlStr += " AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
            countSqlStr += " AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
        }

        if (StringUtils.isNotEmpty(beginDay)) {
                sqlStr += " AND  lr.overdueDays>=@beginDay  ";
                countSqlStr += " AND lr.overdueDays>=@beginDay  ";
        }
        if (StringUtils.isNotEmpty(endDay)) {
            sqlStr += "  AND  lr.overdueDays<=@endDay ";
            countSqlStr += "  AND  lr.overdueDays<=@endDay ";
        }
        if (StringUtils.isNotEmpty(channelId)) {
            sqlStr += " AND  l.channelId = @channelId ";
            countSqlStr += " AND  l.channelId = @channelId ";
        }
        if (StringUtils.isNotEmpty(product)) {
            sqlStr += " AND  l.productId=@productId ";
            countSqlStr += " AND  l.productId=@productId ";
        }
        sqlStr+=" GROUP BY l.code ORDER BY lr.dueDate ASC" +
                " LIMIT @start,@length ";

        Sql sql = Sqls.create(sqlStr);
        Sql countSql = Sqls.create(countSqlStr);
        sql.setParam("agencyId", agencyId);
        sql.setParam("orgId", orgId);
        sql.setParam("businessLine", businessLine);
        sql.setParam("productTypeId", productType);
        sql.setParam("borrower", '%'+borrower+'%');
        sql.setParam("beginDay", beginDay);
        sql.setParam("endDay", endDay);
        sql.setParam("start",param.getStart());
        sql.setParam("length",param.getLength());
        sql.setParam("channelId",channelId);
        sql.setParam("productId", product);
        sql.setCallback(Sqls.callback.maps());
        dao().execute(sql);
        List<Map> list = sql.getList(Map.class);

        if (CollectionUtils.isNotEmpty(list)) {
            for (Map<String,String> map:list) {
                map.put("saleName",loanService.getBusinessSource( map.get("channelId"), map.get("businessLine"), map.get("orgCode"), map.get("saleName")));
            }
        }
        countSql.setParam("agencyId", agencyId);
        countSql.setParam("orgId", orgId);
        countSql.setParam("businessLine", businessLine);
        countSql.setParam("productTypeId", productType);
        countSql.setParam("borrower", '%'+borrower+'%');
        countSql.setParam("beginDay", beginDay);
        countSql.setParam("endDay", endDay);
        countSql.setParam("channelId",channelId);
        countSql.setParam("productId", product);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();

        return new DataTables(param.getDraw(), count, count, list);
    }

    public DataTables queryClearedList(DataTableParam param) {

        String agencyId = "";
        String orgId = "";
        String businessLine = "";
        String borrower = "";
        String repayDate = "";
        String productType = "";
        String channelId = "";
        String product = "";

        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            agencyId = keys.get("agencyId");
            orgId = keys.get("orgId");
            businessLine = keys.get("businessLine");
            borrower = keys.get("borrower");
            repayDate = keys.get("repayDate");
            productType = keys.get("productType");
            channelId = keys.get("channelId");
            product = keys.get("product");
        }
        String sqlStr = "SELECT " +
                "  lr.id AS 'id', " +
                "  lr.loanId AS 'loanId', " +
                "  l. CODE AS 'code', " +
                "  pt. NAME AS 'productTypeName', " +
                " IFNULL(p.name,'--') AS 'productName'," +
                "  l.saleName AS 'saleName', " +
                "  lb.name AS 'borrserName', " +
                "  lr.repayDate AS 'repayDate', " +
                "  l.loanStatus AS 'loanStatus', " +
                "  o. NAME AS 'organizeName', " +
                "  lr.period AS 'period', " +
                " IFNULL(l.channelId,'--') AS 'channelId',"+
                " IFNULL(o.businessLine,'--') AS 'businessLine',"+
                " IFNULL(o.code,'--') AS 'orgCode',"+
                "  a. NAME AS 'agencyName' " +
                "FROM " +
                "   sl_loan l " +
                "LEFT JOIN sl_loan_repay lr ON lr.loanId = l.id AND lr.period = (SELECT MAX(period) FROM sl_loan_repay lr2 where lr2.loanId = l.id) " +
                "LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id " +
                "LEFT JOIN sl_business_user u ON u.id = l.saleId " +
                "LEFT JOIN sl_business_organize o ON u.organizeId = o.id " +
                "LEFT JOIN sl_business_agency a ON o.agencyId = a.id " +
                "LEFT JOIN sl_product p ON l.productId = p.id " +
                "LEFT JOIN (select name,loanId from sl_loan_borrower where master=1)lb ON lb.loanId=l.id "+
                "WHERE " +
                "l. STATUS = 'ABLE' " +
                "AND l.loanStatus = 'CLEARED'";

        String countSqlStr = "SELECT "+
                " COUNT(l.id) AS 'number' "+
                "FROM " +
                "   sl_loan l " +
                "LEFT JOIN sl_loan_repay lr ON lr.loanId = l.id AND lr.period = (SELECT MAX(period) FROM sl_loan_repay lr2 where lr2.loanId = l.id) " +
                "LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id " +
                "LEFT JOIN sl_business_user u ON u.id = l.saleId " +
                "LEFT JOIN sl_business_organize o ON u.organizeId = o.id " +
                "LEFT JOIN sl_business_agency a ON o.agencyId = a.id " +
                "LEFT JOIN sl_product p ON l.productId = p.id " +
                "WHERE " +
                "l. STATUS = 'ABLE' " +
                "AND l.loanStatus = 'CLEARED'";

        if (StringUtils.isNotEmpty(agencyId)){
            sqlStr+=" AND a.id=@agencyId ";
            countSqlStr+=" AND a.id=@agencyId ";
        }
        if (StringUtils.isNotEmpty(orgId)){
            sqlStr+=" AND o.id=@orgId ";
            countSqlStr+=" AND o.id=@orgId ";
        }

        if (StringUtils.isNotEmpty(businessLine)){
            sqlStr+=" AND  o.businessLine=@businessLine ";
            countSqlStr+=" AND  o.businessLine=@businessLine ";
        }

        if (StringUtils.isNotEmpty(productType)){
            sqlStr+=" AND  l.productTypeId=@productTypeId ";
            countSqlStr+=" AND  l.productTypeId=@productTypeId ";
        }

        if (StringUtils.isNotEmpty(borrower)){
            sqlStr+=" AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
            countSqlStr+=" AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
        }

        if (StringUtils.isNotEmpty(product)) {
            sqlStr += " AND  l.productId=@productId ";
            countSqlStr += " AND  l.productId=@productId ";
        }

        String beginDate = null;
        String endDate = null;
        if (StringUtils.isNotEmpty(repayDate)){
            String date[] = repayDate.split("to");

            if(null != date && date.length > 1){
                beginDate = date[0];
                endDate = date[1];
                sqlStr+=" AND lr.repayDate>=@beginDate  AND lr.repayDate<=@endDate ";
                countSqlStr+=" AND lr.repayDate>=@beginDate  AND lr.repayDate<=@endDate ";
            }else {
                sqlStr += " AND lr.repayDate>='" + localBefore7Days + "'  AND lr.repayDate<='" + localTodayDate + "' ";
                countSqlStr += " AND lr.repayDate>='" + localBefore7Days + "'  AND lr.repayDate<='" + localTodayDate + "' ";
            }


        }else {
            sqlStr += " AND lr.repayDate>='" + localBefore7Days + "'  AND lr.repayDate<='" + localTodayDate + "' ";
            countSqlStr += " AND lr.repayDate>='" + localBefore7Days + "'  AND lr.repayDate<='" + localTodayDate + "' ";
        }

        if (StringUtils.isNotEmpty(channelId)){
            sqlStr+=" AND  l.channelId = @channelId ";
            countSqlStr+=" AND  l.channelId = @channelId ";
        }

        sqlStr+=" GROUP BY l.code " +
                "ORDER BY repayDate DESC "+
                " LIMIT @start,@length ";
        countSqlStr+=" ORDER BY repayDate DESC";

        Sql sql = Sqls.create(sqlStr);
        Sql countSql = Sqls.create(countSqlStr);
        sql.setParam("agencyId",agencyId);
        sql.setParam("orgId",orgId);
        sql.setParam("businessLine",businessLine);
        sql.setParam("productTypeId",productType);
        sql.setParam("borrower",'%'+borrower+'%');
        sql.setParam("beginDate",beginDate+" 00:00:00");
        sql.setParam("endDate",endDate+" 23:59:59");
        sql.setParam("start",param.getStart());
        sql.setParam("length",param.getLength());
        sql.setParam("channelId",channelId);
        sql.setParam("productId", product);

        sql.setCallback(Sqls.callback.maps());
        dao().execute(sql);
        List<Map> list = sql.getList(Map.class);

        if (CollectionUtils.isNotEmpty(list)) {
            for (Map<String,String> map:list) {
                map.put("saleName",loanService.getBusinessSource( map.get("channelId"), map.get("businessLine"), map.get("orgCode"), map.get("saleName")));
            }
        }

        countSql.setParam("agencyId",agencyId);
        countSql.setParam("orgId",orgId);
        countSql.setParam("businessLine",businessLine);
        countSql.setParam("productTypeId",productType);
        countSql.setParam("borrower",'%'+borrower+'%');
        countSql.setParam("beginDate",beginDate+" 00:00:00");
        countSql.setParam("endDate",endDate+" 23:59:59");
        countSql.setParam("channelId",channelId);
        countSql.setParam("productId", product);

        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();

        return new DataTables(param.getDraw(),count,count,list);
    }

    //逾期表格处理数据
    public DataTables queryOverdueListForTable(DataTableParam param){


        DataTables dataTable = queryOverdueList(param);
        if(dataTable.getRecordsTotal()<1){
            return dataTable;
        }
        List<Map> list = dataTable.getData();
        list.stream().forEach(m->{
            String loanId = (String)m.get("loanId");
            List<LoanRepay> loanRepayList = this.dao().query(LoanRepay.class,Cnd.where("loanId","=",loanId).and("status","=",LoanRepayStatus.OVERDUE).orderBy("period","asc"));

            BigDecimal amount = BigDecimal.ZERO;
            BigDecimal interest = BigDecimal.ZERO;
            BigDecimal otherFee = BigDecimal.ZERO;
            for(LoanRepay loanRepay:loanRepayList){
                if(LoanRepayStatus.OVERDUE.equals(loanRepay.getStatus())){
                    amount= amount.add(null == loanRepay.getAmount() ? BigDecimal.ZERO :loanRepay.getAmount()).subtract(null == loanRepay.getRepayAmount() ? BigDecimal.ZERO :loanRepay.getRepayAmount());
                    interest = interest.add(null == loanRepay.getAmount() ? BigDecimal.ZERO :loanRepay.getInterest()).subtract(null == loanRepay.getRepayInterest() ? BigDecimal.ZERO :loanRepay.getRepayInterest());
                    List<LoanFee> loanFeeList = this.dao().query(LoanFee.class,Cnd.where("repayId","=",loanRepay.getId()).and("feeType","=",FeeType.OVERDUE_FEE));
                    for(LoanFee loanFee : loanFeeList){
                        otherFee = otherFee.add(null == loanFee.getFeeAmount() ? BigDecimal.ZERO : loanFee.getFeeAmount()).subtract(null == loanFee.getRepayFeeAmount() ? BigDecimal.ZERO :loanFee.getRepayFeeAmount());
                    }
                }
            }

            m.put("amount",amount);
            m.put("interest",interest);
            m.put("otherFee",otherFee);
//            m.put("dueDate",CollectionUtils.isNotEmpty(loanRepayList) ? loanRepayList.get(0).getDueDate():"--");
//            m.put("dueDays",CollectionUtils.isNotEmpty(loanRepayList) ? loanRepayList.get(0).getOverdueDays():"--");
        });
        return dataTable;
    }

    //已还清表格处理数据
    public Object queryClearedListForTable(DataTableParam param){

        DataTables dataTable = queryClearedList(param);
        if(dataTable.getRecordsTotal()<1){
            return dataTable;
        }
        return dataTable;
    }

    /**
     * 根据Id和单状态查询
     * @param id
     * @param loanRepayStatus
     * @return
     */
    public List<LoanRepay> fetchById(String id ,LoanRepayStatus loanRepayStatus) {

        return dao().query(LoanRepay.class,Cnd.where("status","=",loanRepayStatus).and("id","=",id));
    }

    /**
     * 根据loanId查询最近3天快逾期数据
     * @param loanId
     * @return
     */
    public List<LoanRepay> fetchByLoanId(String loanId) {
        return dao().query(LoanRepay.class,Cnd.where("loanId","=",loanId).and("status","=",LoanRepayStatus.LOANED).and("dueDate",">=",localStartDate).and("dueDate","<=",localEndDate));
    }

    /**
     * 根据repayId查询还款数据
     * @param repayId
     * @return
     */
    public List<LoanRepay> fetchByrepayId(String repayId) {
        return dao().query(LoanRepay.class,Cnd.where("id","=",repayId));
    }

    /**
     * 根据loanId查询还款数据
     * @param loanId
     * @return
     */
    public List<LoanRepay> queryAllByloanId(String loanId) {
        return dao().query(LoanRepay.class,Cnd.where("loanId","=",loanId).asc("period"));
    }

    /**
     * 根据loanId和单状态查询逾期数据
     * @param loanId
     * @param loanRepayStatus
     * @return
     */
    public List<LoanRepay> fetchOverdueByLoanId(String loanId,LoanRepayStatus loanRepayStatus) {
        return dao().query(LoanRepay.class,Cnd.where("loanId","=",loanId).and("status","=",loanRepayStatus).asc("dueDate"));
    }

    /**
     * 根据repayId和收取节点查询费用
     * @param repayId
     * @param chargeNode
     * @return
     */
    public List<LoanFee> fetchLoanFeeByRepayId(String repayId, FeeChargeNode chargeNode) {
        return this.dao().query(LoanFee.class,Cnd.where("repayId","=",repayId).and("chargeNode","=",chargeNode));
    }

    /**
     * 根据repayId查询票据放款记录
     * @param repayId
     * @return
     */
    public BillLoanRepay getBillRepayById (String repayId){
        return this.dao().fetch(BillLoanRepay.class, Cnd.where("repayId","=",repayId));
    }

    public void clearedProofExport(String loanId ,HttpServletResponse response)throws Exception{

        List<LoanBorrower> list = dao().query(LoanBorrower.class,Cnd.where("loanId","=",loanId));
        Loan loan = loanService.fetchById(loanId);
        List<LoanRepay> loanRepays = dao().query(LoanRepay.class,Cnd.where("loanId","=",loanId).desc("period"));
        LoanSubject loanSubject = dao().fetch(LoanSubject.class,Cnd.where("id","=",loan.getLoanSubjectId()));
        ClearedProof clearedProof = new ClearedProof();
        clearedProof.setCode(loan.getCode());
        clearedProof.setProofDate(new Date());
        clearedProof.setRepayDate( loanRepays.get(0).getRepayDate());
        clearedProof.setLoanCompany(loanSubject.getName());
        clearedProof.setLoanMoney(loan.getActualAmount());
        clearedProof.setLoanDate(loan.getLoanTime());
        List blist = new ArrayList();
        for(LoanBorrower loanBorrower : list){
            if(loanBorrower.isMaster()){
                clearedProof.setBorrowerID(loanBorrower.getCertifNumber());
                clearedProof.setBorrowerName(loanBorrower.getName());
                clearedProof.setCertiType(loanBorrower.getCertifType().getDescription()+"号码");
            }else{
                Map<String,String> map  =new HashMap<String,String>();
                map.put("borrowerID",loanBorrower.getCertifNumber());
                map.put("borrowerName",loanBorrower.getName());
                blist.add(map);
            }
        }
        if(CollectionUtils.isNotEmpty(blist)){
            clearedProof.setCommon(blist);
        }


        String contextTemp = "\t\t\n"+
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t业务单号：${code}U\n" +
                "\t\t借款人${borrowerName}U（${certiType}N：${borrowerID}U）\n" +
                "\t\t$[共同借款人${common.borrowerName}U（身份证号码：${common.borrowerID}U）]\n" +
                "\t\t在我司进行的贷款￥${loanMoney()}U元（大写${loanMoneyChinese()}U），贷款发放日期${getY(loanDate)}U年${getM(loanDate)}U月${getD(loanDate)}U日，借款人已于${getY(repayDate)}U年${getM(repayDate)}U月${getD(repayDate)}U日结清贷款本息。\n" +
                "\t\t特此证明。\n" +
                "\t\t\n" +
                "\t\t\n" +
                "\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t${loanCompany}N\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t${getY(proofDate)}N年${getM(proofDate)}N月${getD(proofDate)}N日\t";


        PdfUtil.generalPdf(response,clearedProof,"结清证明",contextTemp,true);
    }


    //快到期短信提醒
    public void sendMessageForExpiring(){
        //短信模板id
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String  localStartDate = sdf.format(new Date())+" 00:00:00";
        String localTodayDate = sdf.format(new Date())+" 23:59:59";
        DataTableParam param = new DataTableParam();
        param.setDraw(1);
        param.setLength(999999);
        param.setStart(0);
        Map map  = new HashMap();
        param.setSearchKeys(map);
        param.getSearchKeys().put("repayDate", localStartDate+"~"+localTodayDate);
        DataTables dataTable = queryPostLoanList(param);
        if(dataTable.getRecordsTotal()<1){
            return ;
        }
        List<Map> list = dataTable.getData();
        list.stream().forEach(m->{
            String buName = (String) m.get("buName");
            String businessUserId = (String) m.get("businessUserId");
            String borrserName = (String) m.get("borrserName");
            String businessLine = (String) m.get("businessLine");
            String repayId = (String)m.get("id");
            String orgCode = (String) m.get("orgCode");
            String channelId = (String)m.get("channelId");
            String loanId = (String)m.get("loanId");
            String applyer = (String)m.get("applyer");
            String billRepayId = (String)m.get("billRepayId");
            String productTempType =(String)m.get("productTempType");
            BigDecimal repayAmount = BigDecimal.ZERO;
            BigDecimal repayInterest = BigDecimal.ZERO;
            BigDecimal repayTotalAmount = BigDecimal.ZERO;

            List<LoanRepay> loanRepayList = this.dao().query(LoanRepay.class,Cnd.where("id","=",repayId).orderBy("period","asc"));
            for(LoanRepay loanRepay:loanRepayList){
                repayAmount = repayAmount.add(loanRepay.getAmount()==null?BigDecimal.ZERO:loanRepay.getAmount());
                repayInterest = repayInterest.add(loanRepay.getInterest()==null?BigDecimal.ZERO:loanRepay.getInterest());
                repayTotalAmount = repayTotalAmount.add(loanRepay.getTotalAmount()==null?BigDecimal.ZERO:loanRepay.getTotalAmount());
            }
            if(StringUtils.isNotEmpty(productTempType) && (("CHEDAI").equals(productTempType) || ("GERENDAI").equals(productTempType) || ("HONGBEN").equals(productTempType)) || ("RRC").equals(productTempType)){
                Map msg = new HashMap<String,String>();
                msg.put("borrserName",borrserName);
                msg.put("repayTotalAmount",TextFormatUtils.formatBigDecimal(repayTotalAmount));

                if(StringUtils.isNotEmpty(channelId)){
                    Channel channel=channelService.fetch(channelId);
                    if(channel!=null){
                        if(channel.getChannelType()!=null){
                            if (!channel.getChannelType().equals("0")){
                                BusinessUser manager = businessUserService.fetchById(channel.getManagerId());
                                if(null != manager && StringUtils.isNotEmpty(manager.getMobile())){
                                    msg.put("saleName",manager.getName());
                                    String resultStr = ApiRequestUtil.sendSmsByAPI(manager.getMobile().trim(),msg, ApiPropsUtils.getValueByKey("chedai_expiring_tempId"));
                                    log.info("sendMessageForExpiring job result"+resultStr);
                                }
                            }
                        }
                    }
                }else {
                    BusinessUser businessUser = null;
                    if(StringUtils.isNotEmpty(businessUserId)){
                        businessUser = businessUserService.fetchById(businessUserId);
                        msg.put("saleName",businessUser.getName());
                        String resultStr = ApiRequestUtil.sendSmsByAPI(businessUser.getMobile().trim(),msg, ApiPropsUtils.getValueByKey("chedai_expiring_tempId"));
                        log.info("sendMessageForExpiring job result"+resultStr);
                    }
                    if(StringUtils.isNotEmpty(orgCode) && StringUtils.isNotEmpty(businessLine)){
                        BusinessOrganize businessOrganize = businessOrganizeService.fetchByCodeAndLine(orgCode,businessLine);
                        if(null != businessOrganize && StringUtils.isNotEmpty(businessOrganize.getManagerId())){
                            BusinessUser businessUserManager = businessUserService.fetchById(businessOrganize.getManagerId());
                            if(!businessUser.equals(businessUserManager)) {
                                msg.put("saleName", businessUserManager.getName());
                                String resultStr = ApiRequestUtil.sendSmsByAPI(businessUserManager.getMobile().trim(), msg, ApiPropsUtils.getValueByKey("chedai_expiring_tempId"));
                                log.info("sendMessageForExpiring job result" + resultStr);
                            }
                        }
                    }
                }

            }else if(("SHULOU").equals(productTempType)){
                Map msg = new HashMap<String,String>();
                msg.put("borrserName",borrserName);
                msg.put("repayTotalAmount",TextFormatUtils.formatBigDecimal(repayTotalAmount));

                if(StringUtils.isNotEmpty(channelId)){
                    Channel channel=channelService.fetch(channelId);
                    if(channel!=null){
                        if(channel.getChannelType()!=null){
                            if (!channel.getChannelType().equals("0")){
                                BusinessUser manager = businessUserService.fetchById(channel.getManagerId());
                                if(null != manager && StringUtils.isNotEmpty(manager.getMobile())){
                                    msg.put("saleName",manager.getName());
                                    String resultStr = ApiRequestUtil.sendSmsByAPI(manager.getMobile().trim(),msg, ApiPropsUtils.getValueByKey("shulou_expiring_tempId"));
                                    log.info("sendMessageForExpiring job result"+resultStr);
                                }
                            }
                        }
                    }
                }else {
                    BusinessUser businessUser = null;
                    if(StringUtils.isNotEmpty(businessUserId)){
                        businessUser = businessUserService.fetchById(businessUserId);
                        msg.put("saleName",businessUser.getName());
                        String resultStr = ApiRequestUtil.sendSmsByAPI(businessUser.getMobile().trim(),msg, ApiPropsUtils.getValueByKey("shulou_expiring_tempId"));
                        log.info("sendMessageForExpiring job result"+resultStr);
                    }
                    if(StringUtils.isNotEmpty(orgCode) && StringUtils.isNotEmpty(businessLine)){
                        BusinessOrganize businessOrganize = businessOrganizeService.fetchByCodeAndLine(orgCode,businessLine);
                        if(null != businessOrganize && StringUtils.isNotEmpty(businessOrganize.getManagerId())){
                            BusinessUser businessUserManager = businessUserService.fetchById(businessOrganize.getManagerId());
                            if(!businessUser.equals(businessUserManager)){
                                msg.put("saleName",businessUserManager.getName());
                                String resultStr = ApiRequestUtil.sendSmsByAPI(businessUserManager.getMobile().trim(),msg, ApiPropsUtils.getValueByKey("shulou_expiring_tempId"));
                                log.info("sendMessageForExpiring job result"+resultStr);
                            }

                        }
                    }
                }


            }else if(("YINPIAO").equals(productTempType) || ("PIAOJU").equals(productTempType)){
                BillLoanRepay billLoanRepay = this.dao().fetch(BillLoanRepay.class, Cnd.where("loanId", "=", loanId));
                LoanRepay loanRepay = this.dao().fetch(LoanRepay.class,Cnd.where("id","=",billRepayId));
                Map msg = new HashMap<String,String>();
                msg.put("billNo",billLoanRepay.getBillNo());
                msg.put("Amount",TextFormatUtils.formatBigDecimal(loanRepay.getAmount() == null ? new BigDecimal(0) : loanRepay.getAmount()));
                msg.put("payer",billLoanRepay.getPayer());
                msg.put("payee",billLoanRepay.getPayee());
                User user1 = userService.fetchByName("钟思雨");
                User user2 = userService.fetchByName("王少玲");
                if(null != user1) {
                    String resultStr1 = ApiRequestUtil.sendSmsByAPI(user1.getMobile().trim(), msg, ApiPropsUtils.getValueByKey("bill_expiring_tempId"));
                    log.info("sendMessageForExpiring job result"+resultStr1);
                }
                if(null != user2){
                    String resultStr2 = ApiRequestUtil.sendSmsByAPI(user2.getMobile().trim(),msg, ApiPropsUtils.getValueByKey("bill_expiring_tempId"));
                    log.info("sendMessageForExpiring job result"+resultStr2);
                }
            }else if(("BAOLI").equals(productTempType)){
                Map msg = new HashMap<String,String>();
                msg.put("borrserName",borrserName);
                msg.put("Amount",TextFormatUtils.formatBigDecimal(repayTotalAmount));
                if(StringUtils.isNotEmpty(applyer)){
                    User user = userService.fetchById(applyer);
                    String resultStr = ApiRequestUtil.sendSmsByAPI(user.getMobile().trim(),msg, ApiPropsUtils.getValueByKey("baoli_expiring_tempId"));
                    log.info("sendMessageForExpiring job result"+resultStr);
                }
            }


        });
    }

    //--逾期记录
    /**
     * 新增逾期记录
     * @param overdueRecord
     */
    public OverdueRecord add(OverdueRecord overdueRecord) {
        if(null==overdueRecord){
            return null;
        }
        return dao().insert(overdueRecord);
    }


    /**
     * 根据repayId查询
     * @param repayId
     * @return
     */
    public List<OverdueRecord> queryByRepayId(String repayId) {
        return dao().query(OverdueRecord.class, Cnd.where("repayId","=", repayId).asc("position"));
    }

    public int getMaxCount(){
        return dao().func(OverdueRecord.class, "count", "id", Cnd.where("1", "=", "1"));
    }

}

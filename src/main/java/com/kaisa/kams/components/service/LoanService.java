package com.kaisa.kams.components.service;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.TextFormatUtils;
import com.kaisa.kams.components.view.loan.MobileApplyView;
import com.kaisa.kams.components.view.report.ServiceQueryView;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.business.BusinessOrganize;
import com.kaisa.kams.models.business.BusinessUser;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.sql.SqlCallback;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by weid on 2016/12/9.
 */
@IocBean(fields="dao")
public class LoanService  extends IdNameEntityService<Loan> {

    public static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final Log log = Logs.get();
    @Inject
    private BusinessUserService businessUserService;

    @Inject
    private BusinessOrganizeService businessOrganizeService;

    @Inject
    private ChannelService channelService;

    @Inject
    private ProductInfoTmplService productInfoTmplService;

    @Inject
    private ProductService productService;

    @Inject
    private BillLoanService billLoanService;

    /**
     * 查询录单
     * @param param
     * @return
     */
    public DataTables query(DataTableParam param) {
        User user = ShiroSession.getLoginUser();
        String userId = user.getId();
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String borrower = "";
        String apply = "";
        String submitTime = "";
        String status = "";
        String channelId = "";
        String productType = "";
        String product = "";
        String source = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            apply = keys.get("apply");
            borrower = keys.get("borrower");
            submitTime = keys.get("submitTime");
            status = keys.get("status");
            channelId = keys.get("channelId");
            productType =  keys.get("productType");
            product = keys.get("product");
            source = keys.get("source");
        }
        Date beginDate = setQueryStartDateTime(submitTime);
        Date endDate = setQueryEndDateTime(submitTime);
        String sqlStr = "SELECT" +
                " IFNULL(l.id,'--') AS 'id'," +
                " IFNULL(l.`code`,'--') AS 'code'," +
                " IFNULL(pt.name,'--') AS 'productTypeName'," +
                " IFNULL(sp.name,'--') AS 'productName'," +
                " IFNULL(u.name,'--') AS 'saleName'," +
                " IFNULL(lb.name,'--') AS 'borrserName'," +
                " IFNULL(l.submitTime,'--') AS 'submitTime'," +
                " IFNULL(l.amount,'--') AS 'amount'," +
                " IFNULL(l.term,'--') AS 'term'," +
                " IFNULL(l.approveStatusDesc,'--') AS 'approveStatus'," +
                " IFNULL(wt.display_Name,'--') AS 'nextStatus'," +
                " IFNULL(o.name,'--') AS 'organizeName'," +
                " IFNULL(l.loanStatus,'--') AS 'loanStatus'," +
                " IFNULL(l.termType,'--') AS 'termType'," +
                " IFNULL(l.channelId,'--') AS 'channelId',"+
                " IFNULL(o.businessLine,'--') AS 'businessLine',"+
                " IFNULL(o.code,'--') AS 'orgCode',"+
                " IFNULL(a.name,'--') AS 'agencyName'," +
                " IFNULL(bl.totalAmount,'--') AS 'billTotalAmount'" +
                " FROM sl_loan l" +
                " LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id" +
                " LEFT JOIN sl_product sp ON  sp.id = l.productId" +
                " LEFT JOIN sl_business_user u ON u.id = l.saleId" +
                " LEFT JOIN sl_business_organize o ON  u.organizeId = o.id" +
                " LEFT JOIN sl_business_agency a ON o.agencyId = a.id" +
                " LEFT JOIN (select name,loanId from sl_loan_borrower where master=1) lb ON lb.loanId=l.id" +
                " LEFT JOIN (select  substring(variable, POSITION(\"\\\"loanId\\\":\" IN variable)+10, 36) loanId,display_Name  from wf_task) wt on l.id=wt.loanId" +
                " LEFT JOIN  (SELECT DISTINCT(loanId) loanId FROM sl_approval_result where userId=@userId) sar" +
                " ON l.id = sar.loanId"+
                " LEFT JOIN sl_bill_loan bl ON bl.loanId=l.id"+
                " WHERE l.status = 'ABLE' ";

        String countSqlStr = "SELECT count(l.id) AS number" +
                " FROM sl_loan l" +
                " LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id" +
                " LEFT JOIN sl_product sp ON  sp.id = l.productId" +
                " LEFT JOIN sl_business_user u ON u.id = l.saleId" +
                " LEFT JOIN  (SELECT DISTINCT(loanId) loanId FROM sl_approval_result where userId=@userId) sar" +
                " ON l.id = sar.loanId"+
                " where l.STATUS = 'ABLE' ";

        if (StringUtils.isNotEmpty(apply)){
            sqlStr+=" AND u.name=@apply ";
            countSqlStr+=" AND u.name=@apply ";
        }

        if (StringUtils.isNotEmpty(borrower)){
            sqlStr+=" AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
            countSqlStr+=" AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
        }

        if (null!=beginDate&&null!=endDate){
            sqlStr+=" AND l.submitTime>=@beginDate  AND l.submitTime<=@endDate ";
            countSqlStr+=" AND l.submitTime>=@beginDate  AND l.submitTime<=@endDate ";
        }
        if (StringUtils.isNotEmpty(status)){
            sqlStr+=" AND  l.loanStatus=@status ";
            countSqlStr+=" AND  l.loanStatus=@status ";
        }

        if (StringUtils.isNotEmpty(channelId)) {
            sqlStr+=" AND  l.channelId=@channelId ";
            countSqlStr+=" AND  l.channelId=@channelId ";
        }

        if (StringUtils.isNotEmpty(productType)) {
            sqlStr += " AND  l.productTypeId=@productType ";
            countSqlStr += " AND  l.productTypeId=@productType ";
        }


        if (StringUtils.isNotEmpty(product)) {
            sqlStr += " AND  l.productId=@productId ";
            countSqlStr += " AND  l.productId=@productId ";
        }

        if (StringUtils.isNotEmpty(source) && ("CHANNEL").equals(source)) {
            sqlStr += " AND  l.loanStatus !='CHANNELSAVE' ";
            countSqlStr += " AND  l.loanStatus !='CHANNELSAVE' ";
            sqlStr += " AND  l.source='CHANNEL' ";
            countSqlStr += " AND  l.source='CHANNEL' ";
            sqlStr += " AND (l.channelApplyId = @userId OR sar.loanId is not null ) ";
            countSqlStr += " AND (l.channelApplyId = @userId OR sar.loanId is not null ) ";
        }else {
            sqlStr += " AND (l.applyId = @userId OR sar.loanId is not null) ";
            countSqlStr += " AND (l.applyId = @userId OR sar.loanId is not null) ";
        }

        sqlStr+=" GROUP BY l.code order by l.submitTime desc,l.updateTime desc ";

        Sql sql = Sqls.create(sqlStr);
        Sql countSql = Sqls.create(countSqlStr);
        sql.setParam("updateBy",user.getName());
        sql.setParam("apply",apply);
        sql.setParam("borrower",'%'+borrower+'%');
        sql.setParam("beginDate",beginDate);
        sql.setParam("endDate",endDate);
        sql.setParam("userId",userId);
        sql.setParam("status",status);
        sql.setParam("channelId",channelId);
        sql.setParam("productType", productType);
        sql.setParam("productId", product);
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.maps());
        dao().execute(sql);
        List<Map> list = sql.getList(Map.class);


        countSql.setParam("updateBy",user.getName());
        countSql.setParam("apply",apply);
        countSql.setParam("borrower",'%'+borrower+'%');
        countSql.setParam("beginDate",beginDate);
        countSql.setParam("endDate",endDate);
        countSql.setParam("userId",userId);
        countSql.setParam("status",status);
        countSql.setParam("channelId",channelId);
        countSql.setParam("productType", productType);
        countSql.setParam("productId", product);
        getCount(countSql);
        dao().execute(countSql);
        int count = countSql.getInt();
        if(CollectionUtils.isEmpty(list)){
            list = new ArrayList<>();
        }
        getSaleName(list);
        return new DataTables(param.getDraw(),count,count,list);
    }


    public DataTables queryAllBusinessUser(DataTableParam param){
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String borrower = "";
        String apply = "";
        String submitTime = "";
        String status = "";
        String productType = "";
        String product = "";
        String channelId = "";
        String orgId = "";
        String repayDate = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            apply = keys.get("apply");
            borrower = keys.get("borrower");
            submitTime = keys.get("submitTime");
            status = keys.get("status");
            productType =  keys.get("productType");
            product = keys.get("product");
            channelId = keys.get("channelId");
            orgId = keys.get("orgId");
            repayDate = keys.get("repayDate");
        }
        Date beginDate = setQueryStartDateTime(submitTime);
        Date endDate = setQueryEndDateTime(submitTime);
        Date beginRepayDate = setQueryStartDateTime(repayDate);
        Date endRepayDate = setQueryEndDateTime(repayDate);
        String sqlStr = "SELECT"+
                " IFNULL(l.id,'--') AS 'id',"+
                " IFNULL(l.`code`,'--') AS 'code',"+
                " IFNULL(pt.name,'--') AS 'productTypeName',"+
                " IFNULL(sp.name,'--') AS 'productName',"+
                " IFNULL(u.name,'--') AS 'saleName',"+
                " IFNULL(lb.name,'--') AS 'borrserName',"+
                " IFNULL(l.submitTime,'--') AS 'submitTime',"+
                " IFNULL(l.amount,'--') AS 'amount',"+
                " IFNULL(l.term,'--') AS 'term',"+
                " IFNULL(l.approveStatusDesc,'--') AS 'approveStatus',"+
                " wt.display_Name AS 'nextStatus', "+
                " IFNULL(o.name,'--') AS 'organizeName',"+
                " IFNULL(l.loanStatus,'--') AS 'loanStatus',"+
                " IFNULL(l.termType,'--') AS 'termType',"+
                " IFNULL(l.channelId,'--') AS 'channelId',"+
                " IFNULL(o.businessLine,'--') AS 'businessLine',"+
                " IFNULL(o.code,'--') AS 'orgCode',"+
                " IFNULL(a.name,'--') AS 'agencyName' ,"+
                " IF(blr.actualDueDate IS NULL , lr.dueDate,blr.actualDueDate) AS 'dueDate', " +
                " t.productTempType AS 'productTempType' "+
                " FROM sl_loan l" +
                " LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id" +
                " LEFT JOIN sl_loan_repay lr ON l.id = lr.loanId" +
                " LEFT JOIN sl_bill_loan_repay blr ON lr.id = blr.repayId " +
                " LEFT JOIN sl_business_user u ON  u.id = l.saleId" +
                " LEFT JOIN sl_business_organize o ON u.organizeId = o.id " +
                " LEFT JOIN sl_business_agency a ON o.agencyId = a.id" +
                " LEFT JOIN sl_product sp ON sp.id = l.productId " +
                " LEFT JOIN sl_product_info_tmpl t ON sp.infoTmpId = t.id "+
                " LEFT JOIN (select name,loanId from sl_loan_borrower where master=1) lb ON lb.loanId=l.id" +
                " LEFT JOIN (select  substring(variable, POSITION(\"\\\"loanId\\\":\" IN variable)+10, 36) loanId,display_Name  from wf_task) wt on l.id=wt.loanId" +
                " WHERE l.status = 'ABLE'";



        String countSqlStr = "SELECT "+
                " COUNT( DISTINCT l.id) AS 'number' "+
                " FROM sl_loan l"+
                " LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id" +
                " LEFT JOIN sl_loan_repay lr ON l.id = lr.loanId" +
                " LEFT JOIN sl_business_user u ON  u.id = l.saleId" +
                " LEFT JOIN sl_business_organize o ON u.organizeId = o.id " +
                " LEFT JOIN sl_business_agency a ON o.agencyId = a.id" +
                " LEFT JOIN sl_product sp ON sp.id = l.productId " +
                " LEFT JOIN sl_product_info_tmpl t ON sp.infoTmpId = t.id "+
                " WHERE l.status = 'ABLE'";


        if (StringUtils.isNotEmpty(apply)){
            sqlStr+=" AND u.name=@apply ";
            countSqlStr+=" AND u.name=@apply ";
        }

        if (StringUtils.isNotEmpty(borrower)){
            sqlStr+=" AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
            countSqlStr+=" AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
        }
        if (StringUtils.isNotEmpty(channelId)) {
            sqlStr+=" AND  l.channelId=@channelId ";
            countSqlStr+=" AND  l.channelId=@channelId ";
        }
        if (StringUtils.isNotEmpty(orgId)) {
            sqlStr += " AND  o.id=@orgId ";
            countSqlStr += " AND  o.id=@orgId ";
        }

        if (null!=beginDate&&null!=endDate){
            sqlStr+=" AND l.submitTime>=@beginDate  AND l.submitTime<=@endDate ";
            countSqlStr+=" AND l.submitTime>=@beginDate  AND l.submitTime<=@endDate ";
        }
        if (null!=beginRepayDate&&null!=endRepayDate){
            sqlStr+=" AND (IF(t.productTempType='PIAOJU' || t.productTempType='YINPIAO' , (SELECT CASE l.loanStatus WHEN 'LOANED' THEN (SELECT min(slrb.actualDueDate) from sl_loan_repay slr LEFT JOIN sl_bill_loan_repay slrb ON slrb.repayId = slr.id  WHERE  slr.loanId = l.id ORDER BY slr.period DESC) WHEN 'OVERDUE'  THEN  (IFNULL((SELECT MAX(slrb.actualDueDate) from sl_loan_repay slr LEFT JOIN sl_bill_loan_repay slrb ON slrb.repayId = slr.id  WHERE  slr.loanId = l.id AND slr.status='OVERDUE' ORDER BY slr.period DESC),(SELECT MAX(slrb.actualDueDate) from sl_loan_repay slr LEFT JOIN sl_bill_loan_repay slrb ON slrb.repayId = slr.id  WHERE  slr.loanId = l.id AND slr.status='LOANED' ORDER BY slr.period DESC))) ELSE (SELECT MAX(slrb.actualDueDate) from sl_loan_repay slr LEFT JOIN sl_bill_loan_repay slrb ON slrb.repayId = slr.id WHERE  slr.loanId = l.id AND slr.status in('CLEARED','OVERDUE_CLEARED','AHEAD_CLEARED') ORDER BY slr.period DESC) END ) ,(SELECT CASE l.loanStatus WHEN 'LOANED' THEN (SELECT min(slr.dueDate) from sl_loan_repay slr WHERE  slr.loanId = l.id ORDER BY slr.period DESC) WHEN 'OVERDUE'  THEN  (IFNULL((SELECT MAX(slr.dueDate) from sl_loan_repay slr WHERE  slr.loanId = l.id AND slr.status='OVERDUE' ORDER BY slr.period DESC),(SELECT MAX(slr.dueDate) from sl_loan_repay slr WHERE  slr.loanId = l.id AND slr.status='LOANED' ORDER BY slr.period DESC))) ELSE (SELECT MAX(slr.dueDate) from sl_loan_repay slr WHERE  slr.loanId = l.id AND slr.status in('CLEARED','OVERDUE_CLEARED','AHEAD_CLEARED') ORDER BY slr.period DESC) END )) ) >=@beginRepayDate  " +
                    "AND (IF(t.productTempType='PIAOJU' || t.productTempType='YINPIAO' , (SELECT CASE l.loanStatus WHEN 'LOANED' THEN (SELECT min(slrb.actualDueDate) from sl_loan_repay slr LEFT JOIN sl_bill_loan_repay slrb ON slrb.repayId = slr.id  WHERE  slr.loanId = l.id ORDER BY slr.period DESC) WHEN 'OVERDUE'  THEN  (IFNULL((SELECT MAX(slrb.actualDueDate) from sl_loan_repay slr LEFT JOIN sl_bill_loan_repay slrb ON slrb.repayId = slr.id  WHERE  slr.loanId = l.id AND slr.status='OVERDUE' ORDER BY slr.period DESC),(SELECT MAX(slrb.actualDueDate) from sl_loan_repay slr LEFT JOIN sl_bill_loan_repay slrb ON slrb.repayId = slr.id  WHERE  slr.loanId = l.id AND slr.status='LOANED' ORDER BY slr.period DESC))) ELSE (SELECT MAX(slrb.actualDueDate) from sl_loan_repay slr LEFT JOIN sl_bill_loan_repay slrb ON slrb.repayId = slr.id WHERE  slr.loanId = l.id AND slr.status in('CLEARED','OVERDUE_CLEARED','AHEAD_CLEARED') ORDER BY slr.period DESC) END ) ,(SELECT CASE l.loanStatus WHEN 'LOANED' THEN (SELECT min(slr.dueDate) from sl_loan_repay slr WHERE  slr.loanId = l.id ORDER BY slr.period DESC) WHEN 'OVERDUE'  THEN  (IFNULL((SELECT MAX(slr.dueDate) from sl_loan_repay slr WHERE  slr.loanId = l.id AND slr.status='OVERDUE' ORDER BY slr.period DESC),(SELECT MAX(slr.dueDate) from sl_loan_repay slr WHERE  slr.loanId = l.id AND slr.status='LOANED' ORDER BY slr.period DESC))) ELSE (SELECT MAX(slr.dueDate) from sl_loan_repay slr WHERE  slr.loanId = l.id AND slr.status in('CLEARED','OVERDUE_CLEARED','AHEAD_CLEARED') ORDER BY slr.period DESC) END ))) <=@endRepayDate ";
            countSqlStr+=" AND (IF(t.productTempType='PIAOJU' || t.productTempType='YINPIAO' , (SELECT CASE l.loanStatus WHEN 'LOANED' THEN (SELECT min(slrb.actualDueDate) from sl_loan_repay slr LEFT JOIN sl_bill_loan_repay slrb ON slrb.repayId = slr.id  WHERE  slr.loanId = l.id ORDER BY slr.period DESC) WHEN 'OVERDUE'  THEN  (IFNULL((SELECT MAX(slrb.actualDueDate) from sl_loan_repay slr LEFT JOIN sl_bill_loan_repay slrb ON slrb.repayId = slr.id  WHERE  slr.loanId = l.id AND slr.status='OVERDUE' ORDER BY slr.period DESC),(SELECT MAX(slrb.actualDueDate) from sl_loan_repay slr LEFT JOIN sl_bill_loan_repay slrb ON slrb.repayId = slr.id  WHERE  slr.loanId = l.id AND slr.status='LOANED' ORDER BY slr.period DESC))) ELSE (SELECT MAX(slrb.actualDueDate) from sl_loan_repay slr LEFT JOIN sl_bill_loan_repay slrb ON slrb.repayId = slr.id WHERE  slr.loanId = l.id AND slr.status in('CLEARED','OVERDUE_CLEARED','AHEAD_CLEARED') ORDER BY slr.period DESC) END ) ,(SELECT CASE l.loanStatus WHEN 'LOANED' THEN (SELECT min(slr.dueDate) from sl_loan_repay slr WHERE  slr.loanId = l.id ORDER BY slr.period DESC) WHEN 'OVERDUE'  THEN  (IFNULL((SELECT MAX(slr.dueDate) from sl_loan_repay slr WHERE  slr.loanId = l.id AND slr.status='OVERDUE' ORDER BY slr.period DESC),(SELECT MAX(slr.dueDate) from sl_loan_repay slr WHERE  slr.loanId = l.id AND slr.status='LOANED' ORDER BY slr.period DESC))) ELSE (SELECT MAX(slr.dueDate) from sl_loan_repay slr WHERE  slr.loanId = l.id AND slr.status in('CLEARED','OVERDUE_CLEARED','AHEAD_CLEARED') ORDER BY slr.period DESC) END ))) >=@beginRepayDate  " +
                    "AND (IF(t.productTempType='PIAOJU' || t.productTempType='YINPIAO' , (SELECT CASE l.loanStatus WHEN 'LOANED' THEN (SELECT min(slrb.actualDueDate) from sl_loan_repay slr LEFT JOIN sl_bill_loan_repay slrb ON slrb.repayId = slr.id  WHERE  slr.loanId = l.id ORDER BY slr.period DESC) WHEN 'OVERDUE'  THEN  (IFNULL((SELECT MAX(slrb.actualDueDate) from sl_loan_repay slr LEFT JOIN sl_bill_loan_repay slrb ON slrb.repayId = slr.id  WHERE  slr.loanId = l.id AND slr.status='OVERDUE' ORDER BY slr.period DESC),(SELECT MAX(slrb.actualDueDate) from sl_loan_repay slr LEFT JOIN sl_bill_loan_repay slrb ON slrb.repayId = slr.id  WHERE  slr.loanId = l.id AND slr.status='LOANED' ORDER BY slr.period DESC))) ELSE (SELECT MAX(slrb.actualDueDate) from sl_loan_repay slr LEFT JOIN sl_bill_loan_repay slrb ON slrb.repayId = slr.id WHERE  slr.loanId = l.id AND slr.status in('CLEARED','OVERDUE_CLEARED','AHEAD_CLEARED') ORDER BY slr.period DESC) END ) ,(SELECT CASE l.loanStatus WHEN 'LOANED' THEN (SELECT min(slr.dueDate) from sl_loan_repay slr WHERE  slr.loanId = l.id ORDER BY slr.period DESC) WHEN 'OVERDUE'  THEN  (IFNULL((SELECT MAX(slr.dueDate) from sl_loan_repay slr WHERE  slr.loanId = l.id AND slr.status='OVERDUE' ORDER BY slr.period DESC),(SELECT MAX(slr.dueDate) from sl_loan_repay slr WHERE  slr.loanId = l.id AND slr.status='LOANED' ORDER BY slr.period DESC))) ELSE (SELECT MAX(slr.dueDate) from sl_loan_repay slr WHERE  slr.loanId = l.id AND slr.status in('CLEARED','OVERDUE_CLEARED','AHEAD_CLEARED') ORDER BY slr.period DESC) END )) ) <=@endRepayDate ";
        }

        if (StringUtils.isNotEmpty(productType)) {
            sqlStr += " AND  l.productTypeId=@productType ";
            countSqlStr += " AND  l.productTypeId=@productType ";
        }

        if (StringUtils.isNotEmpty(product)) {
            sqlStr += " AND  l.productId=@productId ";
            countSqlStr += " AND  l.productId=@productId ";
        }

        String inSql = " AND ( l.saleId in ";
        inSql += getSqlByCurrentOrganization();
        String channelIds = getSqlByChannel();
        if(channelIds.length()>2){
            inSql += "OR l.channelId in  ";
            inSql += channelIds;
        }
        inSql += " ) ";
        sqlStr += inSql;
        countSqlStr += inSql;

        if (StringUtils.isNotEmpty(status)){
            sqlStr+=" AND  l.loanStatus=@status ";
            countSqlStr+=" AND  l.loanStatus=@status ";
        }
        sqlStr+=" GROUP BY l.code order by l.submitTime desc,l.updateTime desc ";
        Sql sql = Sqls.create(sqlStr);
        Sql countSql = Sqls.create(countSqlStr);
        sql.setParam("apply",apply);
        sql.setParam("borrower",'%'+borrower+'%');
        sql.setParam("beginDate",beginDate);
        sql.setParam("endDate",endDate);
        sql.setParam("status",status);
        sql.setParam("productType", productType);
        sql.setParam("productId", product);
        sql.setParam("channelId",channelId);
        sql.setParam("orgId", orgId);
        sql.setParam("beginRepayDate", beginRepayDate);
        sql.setParam("endRepayDate", endRepayDate);
        sql.setPager(pager);
//        sql.setCallback(Sqls.callback.maps());
//        dao().execute(sql);
//        List<Map> list = sql.getList(Map.class);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(ServiceQueryView.class));
        dao().execute(sql);
        List<ServiceQueryView> list = sql.getList(ServiceQueryView.class);


        countSql.setParam("apply",apply);
        countSql.setParam("borrower",'%'+borrower+'%');
        countSql.setParam("beginDate",beginDate);
        countSql.setParam("endDate",endDate);
        countSql.setParam("status",status);
        countSql.setParam("productType", productType);
        countSql.setParam("productId", product);
        countSql.setParam("channelId",channelId);
        countSql.setParam("orgId", orgId);
        countSql.setParam("beginRepayDate", beginRepayDate);
        countSql.setParam("endRepayDate", endRepayDate);
        getCount(countSql);
        dao().execute(countSql);
        int count = countSql.getInt();
        if(CollectionUtils.isEmpty(list)){
            list = new ArrayList<>();
        }
        //getSaleName(list);
        for (ServiceQueryView serviceQueryView:list) {
            serviceQueryView.setSaleName(getBusinessSource(serviceQueryView.getChannelId(), serviceQueryView.getBusinessLine(), serviceQueryView.getOrgCode(), serviceQueryView.getSaleName()));
            serviceQueryView.setDueDate(null);
            if(productInfoTmplService.isBill(serviceQueryView.getId())){
                List<LoanRepay> lrList = this.dao().query(LoanRepay.class,Cnd.where("loanId","=",serviceQueryView.getId()).and("status","=",LoanRepayStatus.LOANED).orderBy("period","desc"));
                if(("LOANED").equals(serviceQueryView.getLoanStatus())){
                    if(CollectionUtils.isNotEmpty(lrList)){
                        BillLoanRepay billLoanRepay = this.dao().fetch(BillLoanRepay.class, Cnd.where("repayId","=",lrList.get(lrList.size()-1).getId()));
                        if(null != billLoanRepay && null != billLoanRepay.getActualDueDate()){
                            serviceQueryView.setDueDate(billLoanRepay.getActualDueDate());
                        }
                    }
                }else if(("OVERDUE").equals(serviceQueryView.getLoanStatus())){
                    List<LoanRepay> dList = this.dao().query(LoanRepay.class,Cnd.where("loanId","=",serviceQueryView.getId()).and("status","=",LoanRepayStatus.OVERDUE).orderBy("period","desc"));
                    if(CollectionUtils.isNotEmpty(dList)){
                        BillLoanRepay billLoanRepay = this.dao().fetch(BillLoanRepay.class,Cnd.where("repayId","=",dList.get(dList.size()-1).getId()) );
                        if(null != billLoanRepay && null != billLoanRepay.getActualDueDate()) {
                            serviceQueryView.setDueDate(billLoanRepay.getActualDueDate());
                        }
                    }else {
                        if(CollectionUtils.isNotEmpty(lrList)){
                            BillLoanRepay billLoanRepay = this.dao().fetch(BillLoanRepay.class, Cnd.where("repayId","=",lrList.get(0).getId()));
                            if(null != billLoanRepay && null != billLoanRepay.getActualDueDate()){
                                serviceQueryView.setDueDate(billLoanRepay.getActualDueDate());
                            }
                        }

                    }
                }else if(("CLEARED").equals(serviceQueryView.getLoanStatus())){
                    List<LoanRepay> acrlrList = this.dao().query(LoanRepay.class,Cnd.where("loanId","=",serviceQueryView.getId()).and("status","in",new LoanRepayStatus[]{LoanRepayStatus.CLEARED,LoanRepayStatus.OVERDUE_CLEARED,LoanRepayStatus.AHEAD_CLEARED}).orderBy("period","desc"));
                    if(CollectionUtils.isNotEmpty(acrlrList) ){
                        BillLoanRepay billLoanRepay = this.dao().fetch(BillLoanRepay.class,Cnd.where("repayId","=", acrlrList.get(0).getId()));
                        if(null != billLoanRepay && null != billLoanRepay.getActualDueDate()) {
                            serviceQueryView.setDueDate(billLoanRepay.getActualDueDate());
                        }
                    }
                }
            }else {
                List<LoanRepay> lrList = this.dao().query(LoanRepay.class,Cnd.where("loanId","=",serviceQueryView.getId()).and("status","=",LoanRepayStatus.LOANED).orderBy("period","desc"));
                if(("LOANED").equals(serviceQueryView.getLoanStatus())){
                    if(CollectionUtils.isNotEmpty(lrList)){
                        serviceQueryView.setDueDate(lrList.get(lrList.size()-1).getDueDate());
                    }
                }else if(("OVERDUE").equals(serviceQueryView.getLoanStatus())){
                    List<LoanRepay> dList = this.dao().query(LoanRepay.class,Cnd.where("loanId","=",serviceQueryView.getId()).and("status","=",LoanRepayStatus.OVERDUE).orderBy("period","desc"));
                    if(CollectionUtils.isNotEmpty(dList)){
                        serviceQueryView.setDueDate(dList.get(dList.size()-1).getDueDate());
                    }else {
                        if(CollectionUtils.isNotEmpty(lrList)) {
                            serviceQueryView.setDueDate(lrList.get(0).getDueDate());
                        }
                    }
                }else if(("CLEARED").equals(serviceQueryView.getLoanStatus())){
                    List<LoanRepay> acrlrList = this.dao().query(LoanRepay.class,Cnd.where("loanId","=",serviceQueryView.getId()).and("status","in",new LoanRepayStatus[]{LoanRepayStatus.CLEARED,LoanRepayStatus.OVERDUE_CLEARED,LoanRepayStatus.AHEAD_CLEARED}).orderBy("period","desc"));
                    if(CollectionUtils.isNotEmpty(acrlrList) ){
                        serviceQueryView.setDueDate(acrlrList.get(0).getDueDate());
                    }
                }
            }

        }
        return new DataTables(param.getDraw(),count,count,list);
    }


    private String getSqlByCurrentOrganization(){
        //这里还需要过滤一遍 这里通过身份证号码关联查询
        String insql = "  (";
        String   idNumber =  ShiroSession.getLoginUser().getCertificateNumber();
        BusinessUser businessUser =  businessUserService.fetchByIdNumber(idNumber);
        if(businessUser!=null){
            String mamanageId = businessUser.getId();
            List<BusinessOrganize> businessOrganizeList = businessOrganizeService.listByManageId(mamanageId);
            Set<String> saleIdSet = new HashSet();
            for(BusinessOrganize businessOrganize : businessOrganizeList){
                List<String> list = null;
                list = businessUserService.list(businessOrganize.getCode());
                if(list!=null){
                    list.stream().forEach(id -> {
                        saleIdSet.add("'"+id+"'");
                    });
                }
            }
            saleIdSet.add("'"+mamanageId+"'");
            insql += String.join(",",saleIdSet);
        }else {
            insql += "''";
        }
        insql += ")";
        return insql;
    }


    private String getSqlByChannel(){
        String insql = "(";
        String   idNumber =  ShiroSession.getLoginUser().getCertificateNumber();
        BusinessUser businessUser =  businessUserService.fetchByIdNumber(idNumber);
        if(businessUser!=null){
            String mamanageId = businessUser.getId();
            List<Channel> channelList = channelService.dao().query(Channel.class,Cnd.where("managerId","=",mamanageId).and("status", "=", PublicStatus.ABLE));
            Set<String> channelIds = new HashSet();
            for(Channel channel : channelList){
                channelIds.add("'"+channel.getId()+"'");
            }
            insql += String.join(",",channelIds);
        }
        insql += ")";
        return insql;
    }

    public DataTables queryAllByChannel(DataTableParam param) {
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String apply = "";
        String borrower = "";
        String channel = "";
        String status = "";
        String loanTime = "";
        String orgId = "";
        String productType = "";
        String borrowerId = "";
        Date startTime = null;
        Date endTime  = null;
        String product = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            apply = keys.get("apply");
            borrower = keys.get("borrower");
            channel = keys.get("channel");
            status = keys.get("status");
            loanTime = keys.get("loanTime");
            orgId  = keys.get("orgId");
            productType =  keys.get("productType");
            borrowerId = keys.get("borrowerId");
            product = keys.get("product");
        }
        String sqlStr = "SELECT" +
                " IFNULL(l.id,'--') AS 'id'," +
                " IFNULL(l.`code`,'--') AS 'code'," +
                " IFNULL(pt.name,'--') AS 'productTypeName'," +
                " IFNULL(sp.name,'--') AS 'productName'," +
                " IFNULL(u.name,'--') AS 'saleName'," +
                " IFNULL(lb.name,'--') AS 'borrserName'," +
                " IFNULL(l.submitTime,'--') AS 'submitTime'," +
                " IFNULL(l.amount,'--') AS 'amount'," +
                " IFNULL(l.term,'--') AS 'term'," +
                " IFNULL(l.approveStatusDesc,'--') AS 'approveStatus'," +
                " IFNULL(wt.display_Name,'--') AS 'nextStatus'," +
                " IFNULL(o.name,'--') AS 'organizeName'," +
                " IFNULL(l.loanStatus,'--') AS 'loanStatus'," +
                " IFNULL(l.termType,'--') AS 'termType'," +
                " IFNULL(l.channelId,'--') AS 'channelId',"+
                " IFNULL(o.businessLine,'--') AS 'businessLine',"+
                " IFNULL(o.code,'--') AS 'orgCode',"+
                " IFNULL(a.name,'--') AS 'agencyName'" +
                " FROM sl_loan l" +
                " LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id" +
                " LEFT JOIN sl_business_user u ON u.id = l.saleId" +
                " LEFT JOIN sl_business_organize o ON u.organizeId = o.id" +
                " LEFT JOIN sl_business_agency a ON o.agencyId = a.id" +
                " LEFT JOIN sl_product sp ON  sp.id = l.productId" +
                " LEFT JOIN sl_product_info_tmpl sit ON sp.infoTmpId = sit.id" +
                " LEFT JOIN sl_channel sc ON l.channelId  =  sc.id" +
                " LEFT JOIN (select name,loanId from sl_loan_borrower where master=1) lb ON lb.loanId=l.id" +
                " LEFT JOIN (select  substring(variable, POSITION(\"\\\"loanId\\\":\" IN variable)+10, 36) loanId,display_Name  from wf_task) wt on l.id=wt.loanId" +
                " WHERE l.status = 'ABLE'" +
                " AND sit.productTempType in('SHULOU','HONGBEN','CHEDAI','RRC','GERENDAI','BAOLI')";

        String countSqlStr = "SELECT " +
                " COUNT(DISTINCT l.id) AS 'number' " +
                " FROM sl_loan l" +
                " LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id" +
                " LEFT JOIN sl_business_user u ON u.id = l.saleId" +
                " LEFT JOIN sl_business_organize o ON u.organizeId = o.id" +
                " LEFT JOIN sl_business_agency a ON o.agencyId = a.id" +
                " LEFT JOIN sl_product sp ON  sp.id = l.productId" +
                " LEFT JOIN sl_product_info_tmpl sit ON sp.infoTmpId = sit.id" +
                " LEFT JOIN sl_channel sc ON l.channelId  =  sc.id" +
                " WHERE l.status = 'ABLE'" +
                " AND sit.productTempType in('SHULOU','HONGBEN','CHEDAI','RRC','GERENDAI','BAOLI')";

        if (StringUtils.isNotEmpty(apply)) {
            sqlStr += " AND u.name=@apply ";
            countSqlStr += " AND u.name=@apply ";
        }

        if (StringUtils.isNotEmpty(borrower)) {
            sqlStr += " AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
            countSqlStr += " AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
        }

        if (StringUtils.isNotEmpty(channel)) {
            if ("0".equals(channel)) {
                sqlStr += " AND  (sc.channelType=@channel  OR l.channelId is null OR l.channelId='') ";
                countSqlStr += " AND  (sc.channelType=@channel OR l.channelId is null OR l.channelId='') ";
            }
            if ("1".equals(channel)) {
                sqlStr += " AND  sc.channelType=@channel ";
                countSqlStr += " AND  sc.channelType=@channel ";
            }

        }
        if (StringUtils.isNotEmpty(status)) {
            sqlStr += " AND  l.loanStatus=@status ";
            countSqlStr += " AND  l.loanStatus=@status ";
        }

        if (StringUtils.isNotEmpty(loanTime)) {
            startTime = setQueryStartDateTime(loanTime);
            endTime = setQueryEndDateTime(loanTime);
            sqlStr += " AND  l.loanTime BETWEEN @startTime AND @endTime ";
            countSqlStr += " AND  l.loanTime BETWEEN @startTime AND @endTime ";
        }

        if (StringUtils.isNotEmpty(orgId)) {
            sqlStr += " AND  o.id=@orgId ";
            countSqlStr += " AND  o.id=@orgId ";
        }

        if (StringUtils.isNotEmpty(productType)) {
            sqlStr += " AND  l.productTypeId=@productType ";
            countSqlStr += " AND  l.productTypeId=@productType ";
        }

        if (StringUtils.isNotEmpty(borrowerId)) {
            sqlStr += " AND  l.id in (select loanId from sl_loan_borrower b where b.borrowerId = @borrowerId) ";
            countSqlStr += " AND  l.id in (select loanId from sl_loan_borrower b where b.borrowerId = @borrowerId) ";
        }

        if (StringUtils.isNotEmpty(product)) {
            sqlStr += " AND  l.productId=@productId ";
            countSqlStr += " AND  l.productId=@productId ";
        }

        sqlStr += " order by l.submitTime desc,l.updateTime desc ";

        Sql sql = Sqls.create(sqlStr);
        Sql countSql = Sqls.create(countSqlStr);
        sql.setParam("apply", apply);
        sql.setParam("borrower", '%' + borrower + '%');
        sql.setParam("channel", channel);
        sql.setParam("status", status);
        sql.setParam("productType", productType);
        sql.setParam("orgId", orgId);
        sql.setParam("borrowerId", borrowerId);
        sql.setParam("startTime", startTime);
        sql.setParam("endTime", endTime);
        sql.setParam("productId", product);
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.maps());
        dao().execute(sql);
        List<Map> list = sql.getList(Map.class);
        countSql.setParam("apply", apply);
        countSql.setParam("borrower", '%' + borrower + '%');
        countSql.setParam("channel", channel);
        countSql.setParam("status", status);
        countSql.setParam("productType", productType);
        countSql.setParam("orgId", orgId);
        countSql.setParam("borrowerId", borrowerId);
        countSql.setParam("startTime", startTime);
        countSql.setParam("endTime", endTime);
        countSql.setParam("productId", product);

        getCount(countSql);
        dao().execute(countSql);
        int count = countSql.getInt();
        if (CollectionUtils.isEmpty(list)) {
            list = new ArrayList<>();
        }
        getSaleName(list);
        return new DataTables(param.getDraw(), count, count, list);
    }

    private void getSaleName(List<Map> list) {
        for (Map<String,String> map:list) {
            map.put("saleName",getBusinessSource(map.get("channelId"), map.get("businessLine"), map.get("orgCode"), map.get("saleName")));
            map.remove("channelId");
            map.remove("businessLine");
            map.remove("orgCode");
        }
    }


    public DataTables queryBillBusinessuser(int start, int length, int draw, String billCode, String payer,String bankName, Date beginDate, Date endDate,BigDecimal beginAmount,BigDecimal endAmount,String billType) {
        Pager pager= DataTablesUtil.getDataTableToPager(start,length);
        String sqlStr = "SELECT  " +
                "IFNULL(l.id, '--')AS 'loanId', " +
                "IFNULL(blr.billNo, '--') AS 'billNo',  " +
                "IFNULL(blr.payer, '--') AS 'payer', " +
                "IFNULL(blr.bankName, '--') AS 'bankName', " +
                "IFNULL(bl.discountTime, '--') AS 'discountTime', " +
                "IFNULL(lr.dueDate, '--') AS 'dueDate', " +
                "IFNULL(lr.amount, '--') AS 'totalAmount', " +
                "IFNULL(se.price, '--') AS 'price', "+
                "IFNULL(blr.overdueDays, '--') AS 'overdueDays' " +
                "from  " +
                "sl_bill_loan bl " +
                "LEFT JOIN sl_loan l ON bl.loanId = l.id " +
                "LEFT JOIN sl_product sp ON sp.id = l.productId " +
                "LEFT JOIN sl_product_info_tmpl sit ON sp.infoTmpId = sit.id " +
                "LEFT JOIN sl_loan_repay lr ON lr.loanId = bl.loanId  AND lr.dueDate = (SELECT MAX(lr2.dueDate) FROM sl_loan_repay lr2 WHERE lr2.loanId = bl.loanId ) " +
                "LEFT JOIN sl_bill_loan_repay blr ON blr.repayId = lr.id "+
                "LEFT JOIN sl_enterprise se ON blr.payer = se.name "+
                "WHERE  " +
                "l.status = 'ABLE' ";

        String countSqlStr = "SELECT "+
                " COUNT(DISTINCT l.id) AS 'number' "+
                "from  " +
                "sl_bill_loan bl " +
                "LEFT JOIN sl_loan l ON bl.loanId = l.id " +
                "LEFT JOIN sl_product sp ON sp.id = l.productId " +
                "LEFT JOIN sl_product_info_tmpl sit ON sp.infoTmpId = sit.id " +
                "LEFT JOIN sl_loan_repay lr ON lr.loanId = bl.loanId  AND lr.dueDate = (SELECT MAX(lr2.dueDate) FROM sl_loan_repay lr2 WHERE lr2.loanId = bl.loanId ) " +
                "LEFT JOIN sl_bill_loan_repay blr ON blr.repayId = lr.id  "+
                "LEFT JOIN sl_enterprise se ON blr.payer = se.name "+
                "WHERE  " +
                "l.status = 'ABLE' " ;

        if (StringUtils.isNotEmpty(billCode)){
            sqlStr+=" AND blr.billNo=@billCode ";
            countSqlStr+=" AND blr.billNo=@billCode ";
        }

        if (StringUtils.isNotEmpty(payer)){
            sqlStr+=" AND  blr.payer like @payer ";
            countSqlStr+=" AND  blr.payer like @payer ";
        }
        if (StringUtils.isNotEmpty(bankName)){
            sqlStr+=" AND  blr.bankName like @bankName ";
            countSqlStr+=" AND  blr.bankName like @bankName ";
        }

        if (null!=beginDate&&null!=endDate){
            sqlStr+=" AND bl.discountTime>=@beginDate  AND bl.discountTime<=@endDate ";
            countSqlStr+=" AND bl.discountTime>=@beginDate  AND bl.discountTime<=@endDate ";
        }
        if (null != beginAmount){
            sqlStr+=" AND  lr.amount>=@beginAmount ";
            countSqlStr+=" AND  lr.amount>=@beginAmount ";
        }
        if (null != endAmount){
            sqlStr+=" AND  lr.amount<=@endAmount ";
            countSqlStr+="  AND  lr.amount<=@endAmount ";
        }
        if (StringUtils.isNotEmpty(billType)){
            sqlStr+=" AND  sit.productTempType = @billType ";
            countSqlStr+=" AND  sit.productTempType = @billType ";
        }

        //================================start=======================
        String insql = "AND l.saleId in ";
        insql += getSqlByCurrentOrganization();
        sqlStr += insql;
        countSqlStr += insql;
        //=================================end======================================

        sqlStr+=" GROUP BY bl.id order by bl.discountTime DESC ";

        Sql sql = Sqls.create(sqlStr);
        Sql countSql = Sqls.create(countSqlStr);
        sql.setParam("billCode",billCode);
        sql.setParam("payer",'%'+payer+'%');
        sql.setParam("bankName",'%'+bankName+'%');
        sql.setParam("beginDate",beginDate);
        sql.setParam("endDate",endDate);
        sql.setParam("beginAmount",beginAmount);
        sql.setParam("endAmount",endAmount);
        sql.setParam("billType",billType);
        sql.setPager(pager);
        sql.setCallback(new SqlCallback() {
            public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
                List<Map> list = new LinkedList<Map>();
                while (rs.next()) {
                    Map tmp=new HashMap();
                    tmp.put("loanId",rs.getString("loanId"));
                    tmp.put("billNo",rs.getString("billNo"));
                    tmp.put("payer",rs.getString("payer"));
                    tmp.put("bankName",rs.getString("bankName"));
                    tmp.put("discountTime",rs.getString("discountTime"));
                    tmp.put("dueDate",rs.getString("dueDate"));
                    tmp.put("totalAmount",rs.getString("totalAmount"));
                    tmp.put("overdueDays",rs.getString("overdueDays"));
                    tmp.put("price",rs.getString("price"));
                    list.add(tmp);
                }
                return list;
            }
        });
        dao().execute(sql);
        List<Map> list = sql.getList(Map.class);



        countSql.setParam("billCode",billCode);
        countSql.setParam("payer",'%'+payer+'%');
        countSql.setParam("bankName",'%'+bankName+'%');
        countSql.setParam("beginDate",beginDate);
        countSql.setParam("endDate",endDate);
        countSql.setParam("beginAmount",beginAmount);
        countSql.setParam("endAmount",endAmount);
        countSql.setParam("billType",billType);
        getCount(countSql);
        dao().execute(countSql);
        int count = countSql.getInt();
        if(null==list){
            list = new ArrayList<>();
        }
        return new DataTables(draw,count,count,list);
    }

    public DataTables queryBill(DataTableParam param) {
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String billCode = "";
        String payer = "";
        String bankName = "";
        String discountTime = "";
        String billType = "";
        BigDecimal beginAmount = null;
        BigDecimal endAmount = null;
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            billCode = keys.get("billCode");
            payer = keys.get("payer");
            bankName = keys.get("bankName");
            discountTime = keys.get("discountTime");
            billType = keys.get("billType");
            if(StringUtils.isNotEmpty(keys.get("beginAmount"))){
                beginAmount = new BigDecimal(keys.get("beginAmount"));
            }
            if(StringUtils.isNotEmpty(keys.get("endAmount"))){
                endAmount = new BigDecimal(keys.get("endAmount"));
            }
        }
        Date beginDate = setQueryStartDateTime(discountTime);
        Date endDate = setQueryEndDateTime(discountTime);
        String sqlStr ="SELECT IFNULL(blr.loanId, '--')AS 'loanId'," +
                "IFNULL(l.loanStatus, '--')AS 'loanStatus', " +
                "IFNULL(l.code, '--')AS 'loanCode', " +
                "IFNULL(blr.billNo, '--') AS 'billNo',  " +
                "IFNULL(blr.payer, '--') AS 'payer', " +
                "IFNULL(blr.bankName, '--') AS 'bankName', " +
                "IFNULL(blr.disDate, '--') AS 'discountTime', " +
                "IFNULL(lr.dueDate, '--') AS 'dueDate', " +
                "IFNULL(lr.amount, '--') AS 'totalAmount', " +
                "IFNULL(blr.costRate, '--') AS 'costRate', "+
                "IFNULL(blr.overdueDays, '--') AS 'overdueDays' " +
                "FROM sl_bill_loan_repay blr " +
                "LEFT JOIN sl_loan l ON blr.loanId = l.id " +
                "LEFT JOIN sl_product sp ON sp.id = l.productId " +
                "LEFT JOIN sl_product_info_tmpl sit ON sp.infoTmpId = sit.id " +
                "LEFT JOIN sl_loan_repay lr ON blr.repayId=lr.id " +
                "LEFT JOIN sl_enterprise se ON blr.payer = se.name "+
                "WHERE 1=1 ";

        String countSqlStr = "SELECT COUNT(blr.id) AS 'number' "+
                "FROM sl_bill_loan_repay blr " +
                "LEFT JOIN sl_loan l ON blr.loanId = l.id " +
                "LEFT JOIN sl_product sp ON sp.id = l.productId " +
                "LEFT JOIN sl_product_info_tmpl sit ON sp.infoTmpId = sit.id " +
                "LEFT JOIN sl_loan_repay lr ON blr.repayId=lr.id " +
                "LEFT JOIN sl_enterprise se ON blr.payer = se.name "+
                "WHERE 1=1 ";

        if (StringUtils.isNotEmpty(billCode)){
            sqlStr+=" AND blr.billNo=@billCode ";
            countSqlStr+=" AND blr.billNo=@billCode ";
        }

        if (StringUtils.isNotEmpty(payer)){
            sqlStr+=" AND  blr.payer like @payer ";
            countSqlStr+=" AND  blr.payer like @payer ";
        }
        if (StringUtils.isNotEmpty(bankName)){
            sqlStr+=" AND  blr.bankName like @bankName ";
            countSqlStr+=" AND  blr.bankName like @bankName ";
        }

        if (null!=beginDate&&null!=endDate){
            sqlStr+=" AND blr.disDate>=@beginDate  AND blr.disDate<=@endDate ";
            countSqlStr+=" AND blr.disDate>=@beginDate  AND blr.disDate<=@endDate ";
        }
        if (null != beginAmount){
            sqlStr+=" AND  lr.amount>=@beginAmount ";
            countSqlStr+=" AND  lr.amount>=@beginAmount ";
        }
        if (null != endAmount){
            sqlStr+=" AND  lr.amount<=@endAmount ";
            countSqlStr+="  AND  lr.amount<=@endAmount ";
        }
        if (StringUtils.isNotEmpty(billType)){
            sqlStr+=" AND  sit.productTempType = @billType ";
            countSqlStr+=" AND  sit.productTempType = @billType ";
        }

        sqlStr+="ORDER BY blr.disDate DESC ";

        Sql sql = Sqls.create(sqlStr);
        Sql countSql = Sqls.create(countSqlStr);
        sql.setParam("billCode",billCode);
        sql.setParam("payer",'%'+payer+'%');
        sql.setParam("bankName",'%'+bankName+'%');
        sql.setParam("beginDate",beginDate);
        sql.setParam("endDate",endDate);
        sql.setParam("beginAmount",beginAmount);
        sql.setParam("endAmount",endAmount);
        sql.setParam("billType",billType);
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.maps());
        dao().execute(sql);
        List<Map> list = sql.getList(Map.class);
        countSql.setParam("billCode",billCode);
        countSql.setParam("payer",'%'+payer+'%');
        countSql.setParam("bankName",'%'+bankName+'%');
        countSql.setParam("beginDate",beginDate);
        countSql.setParam("endDate",endDate);
        countSql.setParam("beginAmount",beginAmount);
        countSql.setParam("endAmount",endAmount);
        countSql.setParam("billType",billType);
        getCount(countSql);
        dao().execute(countSql);
        int count = countSql.getInt();
        if(CollectionUtils.isEmpty(list)){
            list = new ArrayList<>();
        }
        return new DataTables(param.getDraw(),count,count,list);
    }

    private void getCount(Sql countSql) {
        countSql.setCallback(Sqls.callback.integer());
    }

    /**
     * 获取产品后8位编号
     * @param productId
     * @return
     */
    public String fetchMaxCode(String productId) {
         String maxCode = (String)dao().func2(Loan.class,"max","code",Cnd.where("productId","=",productId));
         if (null == maxCode) {
             return String.format("%08d",1);
         }
         int maxCodeValue = Integer.valueOf(maxCode.substring(maxCode.length()-8))+1;
         return String.format("%08d",maxCodeValue);
    }

    /**
     * 新增
     * @param loan
     * @return
     */
    public Loan add(Loan loan) {
        if(null==loan) {
            return null;
        }
        return dao().insert(loan);
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Map fetchMapById(String id) {
        String sqlStr = "SELECT "+
                " l.id AS 'id',"+
                " IFNULL(l.`code`,'--') AS 'code',"+
                " IFNULL(pt.name,'--') AS 'productTypeName',"+
                " IFNULL(u.name,'--') AS 'saleName',"+
                " IFNULL(o.code,'--') AS 'saleCode',"+
                " IFNULL(o.businessLine,'--') AS 'businessLine',"+
                " IFNULL(USER.name,'--') AS 'applyName',"+
                " IFNULL(b.name,'--') AS 'borrserName',"+
                " IFNULL(l.submitTime,'--') AS 'submitTime',"+
                " IFNULL(l.actualAmount,'--') AS 'actualAmount',"+
                " IFNULL(l.amount,'--') AS 'amount',"+
                " IFNULL(l.termType,'--') AS 'termType',"+
                " IFNULL(l.repayMethod,'--') AS 'repayMethod',"+
                " IFNULL(l.term,'--') AS 'term',"+
                " IFNULL(l.loanStatus,'--') AS 'loanStatus',"+
                " IFNULL(l.approveStatus,'--') AS 'approveStatus',"+
                " IFNULL(l.interestRate,'--') AS 'interestRate',"+
                " IFNULL(l.interestAmount,'--') AS 'interestAmount',"+
                " IFNULL(l.loanLimitType,'--') AS 'loanLimitType',"+
                " IFNULL(o.name,'--') AS 'organizeName',"+
                " IFNULL(l.termType,'--') AS 'termType',"+
                " IFNULL(a.name,'--') AS 'agencyName',"+
                " IFNULL(l.repayDateType,'--') AS 'repayDateType',"+
                " IFNULL(l.grace,'--') AS 'grace',"+
                " IFNULL(l.minInterestAmount,'--') AS 'minInterestAmount',"+
                " l.calculateMethodAboutDay AS 'calculateMethodAboutDay',"+
                " l.channelId AS 'channelId',"+
                " IFNULL(ls.name,'--') AS 'loanSubjectName'"+
                "FROM sl_loan l" +
                "  left join sl_product_type pt on l.productTypeId=pt.id" +
                "  left join sl_loan_borrower b on b.id = l.masterBorrowerId" +
                "  left join sl_user USER on USER.id = l.applyId" +
                "  left join sl_loan_subject ls on ls.id = l.loanSubjectId" +
                "  left join sl_business_user u on u.id = l.saleId" +
                "  left join sl_business_organize o on o.id  = u.organizeId" +
                "  left join sl_business_agency a on o.agencyId = a.id"+
                "  WHERE l.id = @loanId";
        sqlStr+=" order by l.updateTime desc ";
        Sql sql = Sqls.create(sqlStr);
        sql.setParam("loanId",id);

        sql.setCallback(new SqlCallback() {
            public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
                Map map = new HashMap();
                while (rs.next()) {
                    map.put("id",rs.getString("id"));
                    map.put("code",rs.getString("code"));
                    map.put("productTypeName",rs.getString("productTypeName"));
                    map.put("saleName",rs.getString("saleName"));
                    map.put("saleCode",rs.getString("saleCode"));
                    map.put("applyName",rs.getString("applyName"));
                    map.put("borrserName",rs.getString("borrserName"));
                    map.put("submitTime",rs.getString("submitTime"));
                    map.put("amount",rs.getString("amount"));
                    map.put("businessLine",rs.getString("businessLine"));
                    map.put("termType",rs.getString("termType"));
                    map.put("repayMethod",rs.getString("repayMethod"));
                    map.put("term",rs.getString("term"));
                    map.put("loanStatus",rs.getString("loanStatus"));
                    map.put("termType",rs.getString("termType"));
                    map.put("approveStatus",rs.getString("approveStatus"));
                    map.put("interestRate",rs.getString("interestRate"));
                    map.put("organizeName",rs.getString("organizeName"));
                    map.put("agencyName",rs.getString("agencyName"));
                    map.put("interestAmount",rs.getString("interestAmount"));
                    map.put("loanLimitType",rs.getString("loanLimitType"));
                    map.put("repayDateType",rs.getString("repayDateType"));
                    map.put("minInterestAmount",rs.getString("minInterestAmount"));
                    map.put("calculateMethodAboutDay",rs.getString("calculateMethodAboutDay"));
                    map.put("channelId",rs.getString("channelId"));
                    map.put("loanSubjectName",rs.getString("loanSubjectName"));
                    map.put("actualAmount",rs.getString("actualAmount"));
                    map.put("grace",rs.getString("grace"));
                }
                return map;
            }
        });

        dao().execute(sql);
        Object obj = sql.getResult();
        Map result = null==obj ? null :(Map)obj;
        return  result;
    }

    /**
     * 通过Id查找
     * @param id
     * @return
     */
    public Loan fetchById(String id) {
      return dao().fetch(Loan.class,Cnd.where("status","=",PublicStatus.ABLE).and("id","=",id));
    }

    /**
     * 通过code查找
     * @param code
     * @return
     */
    public Loan fetchByCode(String code) {
        return dao().fetch(Loan.class,Cnd.where("status","=",PublicStatus.ABLE).and("code","=",code));
    }

    /**
     * 修改订单
     * @param loan
     * @return
     */
    public boolean update(Loan loan) {
        if(null==loan){
            return false;
        }
        List param = new ArrayList<>();

        if(StringUtils.isNotEmpty(loan.getProductTypeId())){
            param.add("productTypeId");
        }

        if(StringUtils.isNotEmpty(loan.getProductId())){
            param.add("productId");
        }

        if(StringUtils.isNotEmpty(loan.getLoanSubjectId())){
            param.add("loanSubjectId");
        }

        if(StringUtils.isNotEmpty(loan.getMasterBorrowerId())){
            param.add("masterBorrowerId");
        }

        if(null!=loan.getRepayMethod()){
            param.add("repayMethod");
        }

        if(null!=loan.getLoanLimitType()){
            param.add("loanLimitType");
        }

        if(null!=loan.getInterestAmount()){
            param.add("interestAmount");
        }

        if(null!=loan.getInterestRate()){
            param.add("interestRate");
        }


        if (StringUtils.isNotEmpty(loan.getApproveStatus())){
            param.add("approveStatus");
        }

        if(StringUtils.isNotEmpty(loan.getCode())){
            param.add("code");
        }

        if(null!=loan.getLoanLimitType()&&StringUtils.isNotEmpty(loan.getLoanLimitType().name())){
            param.add("loanLimitType");
        }

        if(StringUtils.isNotEmpty(loan.getSaleCode())){
            param.add("saleCode");
        }

        if(StringUtils.isNotEmpty(loan.getSaleName())){
            param.add("saleName");
        }

        if(StringUtils.isNotEmpty(loan.getTerm())){
            param.add("term");
        }

        if(null!=loan.getAmount()){
            param.add("amount");
        }

        if(null!=loan.getLoanStatus()){
            param.add("loanStatus");
        }

        if(null!=loan.getSubmitTime()){
            param.add("submitTime");
        }

        if(null!=loan.getTermType()){
            param.add("termType");
        }

        if(StringUtils.isNotEmpty(loan.getApplyId())){
            param.add("applyId");
        }

        if(null!=loan.getRepayDateType()){
            param.add("repayDateType");
        }

        if(!StringUtils.isEmpty(loan.getApproveStatusDesc())){
            param.add("approveStatusDesc");
        }

        if(!StringUtils.isEmpty(loan.getStep())){
            param.add("step");
        }

        if(null!=loan.getMinInterestAmount()) {
            param.add("minInterestAmount");
        }

        if(StringUtils.isNotEmpty(loan.getSaleId())){
            param.add("saleId");
        }
        if(StringUtils.isNotEmpty(loan.getEngagedSaleId())){
            param.add("engagedSaleId");
        }
        if(StringUtils.isNotEmpty(loan.getEngagedSaleName())){
            param.add("engagedSaleName");
        }

        if (null != loan.getChannelId()) {
            param.add("channelId");
        }

        if ("".equals(loan.getChannelId())) {
            loan.setChannelId(null);
        }
        if (null != loan.getCalculateMethodAboutDay()) {
            param.add("calculateMethodAboutDay");
        }
        if (null != loan.getActualAmount()) {
            param.add("actualAmount");
        }

        if (LoanTermType.MOTHS==loan.getTermType()) {
            loan.setCalculateMethodAboutDay(null);
        }
        if(loan.getGrace()!=0){
            param.add("grace");
        }
        if(null!=loan.getUpdateBy()){
            param.add("updateBy");
        }
        loan.setUpdateTime(new Date());
        param.add("updateTime");

        String paramStr = StringUtils.join(param,"|");
        int flag = dao().update(loan,"^("+paramStr+")$");
        return flag>0;
    }
    private List<Map> queryProcess(String sqlStr,String applyId,int start,int length){

        if(ChannelUserType.CHANNEL_USER.equals(ShiroSession.getLoginUser().getType())){
            if (!StringUtils.isEmpty(applyId)){
                sqlStr+=" AND l.channelApplyId=@applyId ";
            }
        }else {
            if (!StringUtils.isEmpty(applyId)){
                sqlStr+=" AND l.applyId=@applyId ";
            }
        }
        sqlStr+=" order by l.updateTime desc limit @start,@length";
        Sql sql = Sqls.create(sqlStr);
        sql.setParam("applyId",applyId);
        sql.setParam("start",start);
        sql.setParam("length",length);
        sql.setCallback(new SqlCallback() {
            public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
                List<Map> list = new LinkedList<Map>();
                while (rs.next()) {
                    Map tmp=new HashMap();
                    tmp.put("id",rs.getString("id"));
                    tmp.put("code",rs.getString("code"));
                    tmp.put("productTypeName",rs.getString("productTypeName"));
                    tmp.put("saleName",getBusinessSource( rs.getString("channelId"), rs.getString("businessLine"), rs.getString("orgCode"), rs.getString("saleName")));
                    tmp.put("borrserName",rs.getString("borrserName"));
                    tmp.put("submitTime",rs.getString("submitTime"));
                    tmp.put("amount",rs.getString("amount"));
                    tmp.put("term",rs.getString("term"));
                    tmp.put("loanStatus",rs.getString("loanStatus"));
                    tmp.put("termType",rs.getString("termType"));
                    tmp.put("approveStatus",rs.getString("approveStatus"));
                    tmp.put("organizeName",rs.getString("organizeName"));
                    tmp.put("agencyName",rs.getString("agencyName"));
                    list.add(tmp);
                }
                return list;
            }
        });
        dao().execute(sql);
        List<Map> list = sql.getList(Map.class);
        return list;
    }
    private int queryProcessCount(String countSqlStr,String applyId){
        if(ChannelUserType.CHANNEL_USER.equals(ShiroSession.getLoginUser().getType())){
            if (!StringUtils.isEmpty(applyId)){
                countSqlStr+=" AND l.channelApplyId=@applyId ";
            }
        }else {
            if (!StringUtils.isEmpty(applyId)){
                countSqlStr+=" AND l.applyId=@applyId ";
            }
        }

        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("applyId",applyId);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        return countSql.getInt();
    }

    /**
     * 查询
     * @param start
     * @param length
     * @param draw
     * @param applyId
     * @return
     */
    public Object queryProcess(int start, int length, int draw, String applyId) {
        String sqlStr = "SELECT"+
                " IFNULL(l.id,'--') AS 'id',"+
                " IFNULL(l.`code`,'--') AS 'code',"+
                " IFNULL(pt.name,'--') AS 'productTypeName',"+
                " IFNULL(u.name,'--') AS 'saleName',"+
                " IFNULL(b.name,'--') AS 'borrserName',"+
                " IFNULL(l.submitTime,'--') AS 'submitTime',"+
                " IFNULL(l.amount,'--') AS 'amount',"+
                " IFNULL(l.term,'--') AS 'term',"+
                " IFNULL(l.approveStatusDesc,'--') AS 'approveStatus',"+
                " IFNULL(l.loanStatus,'--') AS 'loanStatus',"+
                " IFNULL(o.name,'--') AS 'organizeName',"+
                " IFNULL(l.termType,'--') AS 'termType',"+
                " IFNULL(l.channelId,'--') AS 'channelId',"+
                " IFNULL(o.businessLine,'--') AS 'businessLine',"+
                " IFNULL(o.code,'--') AS 'orgCode',"+
                " IFNULL(a.name,'--') AS 'agencyName'"+
                " FROM sl_loan l,sl_product_type pt,sl_business_user u,sl_loan_borrower b,sl_business_organize o,sl_business_agency a"+
                " WHERE u.id = l.saleId"+
                " AND l.`status` = 'ABLE'"+
                " AND l.productTypeId = pt.id"+
                " AND l.masterBorrowerId = b.id"+
                " AND u.organizeId = o.id"+
                " AND o.agencyId = a.id"+
                " AND l.loanStatus in ('SAVE','LOANCANCEL') " +
                " AND  l.source !='CHANNEL' ";
        String countSqlStr = "SELECT "+
                " COUNT(DISTINCT l.id) AS 'number' "+
                " FROM sl_loan l,sl_product_type pt,sl_business_user u,sl_loan_borrower b,sl_business_organize o,sl_business_agency a"+
                " WHERE u.id = l.saleId"+
                " AND l.`status` = 'ABLE'"+
                " AND l.productTypeId = pt.id"+
                " AND l.masterBorrowerId = b.id"+
                " AND u.organizeId = o.id"+
                " AND o.agencyId = a.id"+
                " AND l.loanStatus in ('SAVE','LOANCANCEL') " +
                " AND  l.source !='CHANNEL' ";

        String connRejectStr = " AND (l.approveStatusDesc like '%退回%' or l.approveStatusDesc like '%拒绝%' or l.approveStatusDesc like '%同意%') ";
        List<Map> list = this.queryProcess(sqlStr+connRejectStr,applyId,start,length);
        int count;
        if(list!=null&&list.size()<length){
            count = this.queryProcessCount(countSqlStr+connRejectStr,applyId);
            sqlStr = sqlStr+
                    " AND (l.approveStatusDesc is null or (l.approveStatusDesc not like '%退回%' and l.approveStatusDesc not like '%拒绝%' and l.approveStatusDesc not like '%同意%' )) ";
            List<Map> notRejectList = this.queryProcess(sqlStr,applyId,start-count<0?0:start-count,length-list.size());
            list.addAll(notRejectList);
        }
        count = this.queryProcessCount(countSqlStr,applyId);
        if(null==list){
            list = new ArrayList<>();
        }
        return new DataTables(draw,count,count,list);

    }

    /**
     * 查询渠道待提单和已提单
     * @param param
     * @return
     */
    public DataTables queryChannelProcess(DataTableParam param) {
        String applyId = ShiroSession.getLoginUser().getId();
        User user = ShiroSession.getLoginUser();
        if(ChannelUserType.COMPANY_USER.equals(user.getType())){
            applyId = null;
        }
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String status = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            status = keys.get("status");
        }
        String sqlStr = "SELECT"+
                " IFNULL(l.id,'--') AS 'id',"+
                " IFNULL(l.`code`,'--') AS 'code',"+
                " IFNULL(pt.name,'--') AS 'productTypeName',"+
                " IFNULL(u.name,'--') AS 'saleName',"+
                " IFNULL(b.name,'--') AS 'borrserName',"+
                " IFNULL(l.submitTime,'--') AS 'submitTime',"+
                " IFNULL(l.amount,'--') AS 'amount',"+
                " IFNULL(l.term,'--') AS 'term',"+
                " IFNULL(l.approveStatusDesc,'--') AS 'approveStatus',"+
                " IFNULL(l.loanStatus,'--') AS 'loanStatus',"+
                " IFNULL(o.name,'--') AS 'organizeName',"+
                " IFNULL(l.termType,'--') AS 'termType',"+
                " IFNULL(l.channelId,'--') AS 'channelId',"+
                " IFNULL(o.businessLine,'--') AS 'businessLine',"+
                " IFNULL(o.code,'--') AS 'orgCode',"+
                " IFNULL(a.name,'--') AS 'agencyName'"+
                " FROM sl_loan l,sl_product_type pt,sl_business_user u,sl_loan_borrower b,sl_business_organize o,sl_business_agency a"+
                " WHERE u.id = l.saleId"+
                " AND l.`status` = 'ABLE'"+
                " AND l.productTypeId = pt.id"+
                " AND l.masterBorrowerId = b.id"+
                " AND u.organizeId = o.id"+
                " AND o.agencyId = a.id " +
                " AND l.source = 'CHANNEL' ";
        String countSqlStr = "SELECT "+
                " COUNT(DISTINCT l.id) AS 'number' "+
                " FROM sl_loan l,sl_product_type pt,sl_business_user u,sl_loan_borrower b,sl_business_organize o,sl_business_agency a"+
                " WHERE u.id = l.saleId"+
                " AND l.`status` = 'ABLE'"+
                " AND l.productTypeId = pt.id"+
                " AND l.masterBorrowerId = b.id"+
                " AND u.organizeId = o.id"+
                " AND o.agencyId = a.id " +
                " AND l.source = 'CHANNEL' ";
        if (StringUtils.isNotEmpty(status)) {
            if(("SUBMIT").equals(status)){
                sqlStr += " AND  l.loanStatus = 'SAVE' ";
                countSqlStr += " AND  l.loanStatus = 'SAVE' ";
            }else {
                sqlStr += " AND  l.loanStatus='CHANNELSAVE' ";
                countSqlStr += " AND  l.loanStatus='CHANNELSAVE' ";
            }
        }
        String connRejectStr = " AND (l.approveStatusDesc like '%退回%' or l.approveStatusDesc like '%拒绝%' or l.approveStatusDesc like '%同意%') ";
        List<Map> list = this.queryProcess(sqlStr+connRejectStr,applyId,param.getStart(),param.getLength());
        int count;
        if(list!=null&&list.size()<param.getLength()){
            count = this.queryProcessCount(countSqlStr+connRejectStr,applyId);
            sqlStr = sqlStr+
                    " AND (l.approveStatusDesc is null or (l.approveStatusDesc not like '%退回%' and l.approveStatusDesc not like '%拒绝%' and l.approveStatusDesc not like '%同意%' )) ";
            List<Map> notRejectList = this.queryProcess(sqlStr,applyId,param.getStart()-count<0?0:param.getStart()-count,param.getLength()-list.size());
            list.addAll(notRejectList);
        }
        count = this.queryProcessCount(countSqlStr,applyId);
        if(null==list){
            list = new ArrayList<>();
        }
        return new DataTables(param.getDraw(),count,count,list);

    }

    /**
     * 查询待审批列表
     * @param start
     * @param length
     * @param loanIds
     * @return
     */
    public List<Map> queryApprovalList(int start, int length, List<String> loanIds) {
        if(CollectionUtils.isEmpty(loanIds)){
            return new ArrayList<>();
        }
        Pager pager= DataTablesUtil.getDataTableToPager(start,length);
        String sqlStr = "SELECT DISTINCT "+
                " IFNULL(l.id,'--') AS 'id',"+
                " IFNULL(l.`code`,'--') AS 'code',"+
                " IFNULL(pt.name,'--') AS 'productTypeName',"+
                " IFNULL(p.name,'--') AS 'productName',"+
                " IFNULL(u.name,'--') AS 'saleName',"+
                " IFNULL(b.name,'--') AS 'borrserName',"+
                " IFNULL(l.submitTime,'--') AS 'submitTime',"+
                " IFNULL(l.amount,'--') AS 'amount',"+
                " IFNULL(l.term,'--') AS 'term',"+
                " IFNULL(l.approveStatusDesc,'--') AS 'approveStatus',"+
                " (SELECT display_Name FROM wf_task wt WHERE wt.variable like CONCAT('%',l.id,'%') limit 1 OFFSET 0 ) AS 'nextStatus', "+
                " IFNULL(o.name,'--') AS 'organizeName',"+
                " IFNULL(l.termType,'--') AS 'termType',"+
                " IFNULL(l.loanStatus,'--') AS 'loanStatus',"+
                " IFNULL(l.channelId,'--') AS 'channelId',"+
                " IFNULL(o.businessLine,'--') AS 'businessLine',"+
                " IFNULL(o.code,'--') AS 'orgCode',"+
                " IFNULL(a.name,'--') AS 'agencyName'"+
                " FROM sl_loan l" +
                " LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id" +
                " LEFT JOIN sl_product p ON l.productId = p.id" +
                " LEFT JOIN sl_business_user u ON u.id = l.saleId" +
                " LEFT JOIN sl_loan_borrower b ON l.masterBorrowerId = b.id" +
                " LEFT JOIN sl_business_organize o ON u.organizeId = o.id" +
                " LEFT JOIN sl_business_agency a ON o.agencyId = a.id"+
                " WHERE l.status = 'ABLE'"+
                " AND l.loanStatus = 'SUBMIT' "+
                " AND l.id IN (@ids)"+
                " ORDER BY l.submitTime asc,l.updateTime asc ";
        Sql sql = Sqls.create(sqlStr);
        sql.setParam("ids",loanIds.toArray());
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.maps());
//        sql.setCallback(new SqlCallback() {
//            public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
//                List<Map> list = new LinkedList<Map>();
//                while (rs.next()) {
//                    Map tmp=new HashMap();
//                    tmp.put("id",rs.getString("id"));
//                    tmp.put("code",rs.getString("code"));
//                    tmp.put("productTypeName",rs.getString("productTypeName"));
//                    tmp.put("productName",rs.getString("productName"));
//                    tmp.put("saleName",getBusinessSource( rs.getString("channelId"), rs.getString("businessLine"), rs.getString("orgCode"), rs.getString("saleName")));
//                    tmp.put("borrserName",rs.getString("borrserName"));
//                    tmp.put("submitTime",rs.getString("submitTime"));
//                    tmp.put("amount",rs.getString("amount"));
//                    tmp.put("term",rs.getString("term"));
//                    tmp.put("loanStatus",rs.getString("loanStatus"));
//                    tmp.put("approveStatus",rs.getString("approveStatus"));
//                    tmp.put("termType",rs.getString("termType"));
//                    tmp.put("organizeName",rs.getString("organizeName"));
//                    tmp.put("agencyName",rs.getString("agencyName"));
//                    tmp.put("nextStatus",rs.getString("nextStatus"));
//                    list.add(tmp);
//                }
//                return list;
//            }
//        });
        dao().execute(sql);
        List<Map> list = sql.getList(Map.class);
        if(null == list){
            list = new ArrayList<>();
        }
        for (Map rs:list) {
            rs.put("saleName",getBusinessSource((String)rs.get("channelId"), (String)rs.get("businessLine"), (String)rs.get("orgCode"), (String)rs.get("saleName")));
        }
        return list;
    }


    public int queryApprovalCount(List<String> loanIds) {
        if (CollectionUtils.isNotEmpty(loanIds)) {
            String countSqlStr = "SELECT "+
                    " COUNT(id) AS 'number' "+
                    " FROM sl_loan" +
                    " WHERE status = 'ABLE'"+
                    " AND loanStatus = 'SUBMIT' "+
                    " AND id IN (@ids)";
            Sql countSql = Sqls.create(countSqlStr);
            countSql.setParam("ids",loanIds.toArray());
            countSql.setCallback(Sqls.callback.integer());
            dao().execute(countSql);
            int count = countSql.getInt();
            return count;
        }
        return 0;
    }

    /**
     * 查询待放款列表
     * @return
     */
    public Object queryLoanList(DataTableParam param) {

        String agencyId = "";
        String businessLine = "";
        String orgId = "";
        String borrower = "";
        String productTypeId = "";
        String loanTime = "";
        String loanStatus = "";
        String channelId = "";
        String product = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            agencyId = keys.get("agencyId");
            businessLine = keys.get("businessLine");
            orgId = keys.get("orgId");
            borrower = keys.get("borrower");
            productTypeId = keys.get("productType");
            loanTime = keys.get("loanTime");
            loanStatus = keys.get("loanStatus");
            channelId = keys.get("channelId");
            product = keys.get("product");
        }
        LoanStatus[] statusArr = {LoanStatus.LOANED, LoanStatus.CLEARED, LoanStatus.OVERDUE};
        statusArr = getLoanStatuses(loanStatus, statusArr);
        String sqlStr = " SELECT DISTINCT "+
        " IFNULL(l.id,'--') AS 'id',"+
        " IFNULL(l.`code`,'--') AS 'code',"+
        " IFNULL(pt.name,'--') AS 'productTypeName',"+
        " IFNULL(sp.name,'--') AS 'productName'," +
        " IFNULL(u.name,'--') AS 'saleName',"+
        " lb.name AS 'borrserName',"+
        " IFNULL(l.submitTime,'--') AS 'submitTime',"+
        " IFNULL(l.amount,'--') AS 'amount',"+
        " IFNULL(l.actualAmount,'--') AS 'actualAmount',"+
        " IFNULL(l.loanTime,'--') AS 'loanTime',"+
        " IFNULL(o.businessLine,'--') AS 'businessLine',"+
        " IFNULL(l.term,'--') AS 'term',"+
        " IFNULL(l.approveStatusDesc,'--') AS 'approveStatus',"+
        " IFNULL(o.name,'--') AS 'organizeName',"+
        " IFNULL(l.termType,'--') AS 'termType',"+
        " IFNULL(l.loanStatus,'--') AS 'loanStatus',"+
        " IFNULL(l.elementStatus,'--') AS 'elementStatus',"+
        " IFNULL(a.name,'--') AS 'agencyName',"+
        " IFNULL(l.elementConfirmName,'--') AS 'elementConfirmName',"+
        " IFNULL(l.channelId,'--') AS 'channelId',"+
        " IFNULL(o.businessLine,'--') AS 'businessLine',"+
        " IFNULL(o.code,'--') AS 'orgCode',"+
        " IFNULL(l.elementConfirmTime,'--') AS 'elementConfirmTime'"+
        " FROM sl_loan l " +
               "  LEFT JOIN (select name,loanId from sl_loan_borrower where master=1) lb ON lb.loanId=l.id" +
               "  LEFT JOIN sl_product_type pt on l.productTypeId = pt.id" +
                " LEFT JOIN sl_product sp ON  sp.id = l.productId" +
                " LEFT JOIN sl_business_user u on u.id=  l.saleId" +
               "  LEFT JOIN sl_business_organize o on u.organizeId = o.id" +
               "  LEFT JOIN sl_business_agency a on o.agencyId = a.id" +
               "  WHERE   l.status = 'ABLE'"+
        " AND l.loanStatus in (@loanStatus) ";


        String countSqlStr =" SELECT  COUNT(DISTINCT l.id) AS 'number'  "+
                " FROM sl_loan l " +
                "  LEFT JOIN sl_product_type pt on l.productTypeId = pt.id" +
                "  LEFT JOIN sl_product sp ON  sp.id = l.productId" +
                "  LEFT JOIN sl_business_user u on u.id=  l.saleId" +
                "  LEFT JOIN sl_business_organize o on u.organizeId = o.id" +
                "  LEFT JOIN sl_business_agency a on o.agencyId = a.id" +
                "  WHERE   l.status = 'ABLE'"+
                " AND l.loanStatus in (@loanStatus) ";

        String[] loanStatusArr = new String[statusArr.length];
        int i=0;
        for(LoanStatus status:statusArr){
            loanStatusArr[i++]=status.name();
        }
        if(StringUtils.isNotEmpty(agencyId)){
            sqlStr += " AND o.agencyId = @agencyId " ;
            countSqlStr += " AND o.agencyId= @agencyId ";
        }
        if(StringUtils.isNotEmpty(businessLine)){
            sqlStr+=" AND  o.businessLine=@businessLine ";
            countSqlStr+=" AND  o.businessLine=@businessLine ";
        }
        if(StringUtils.isNotEmpty(orgId)){
            sqlStr+=" AND o.id=@orgId ";
            countSqlStr+=" AND o.id=@orgId ";
        }
        if(StringUtils.isNotEmpty(borrower)){
            sqlStr+=" AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
            countSqlStr+=" AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
        }
        if(StringUtils.isNotEmpty(channelId)){
            sqlStr+=" AND  l.channelId = @channelId ";
            countSqlStr+=" AND  l.channelId = @channelId ";
        }
        if (StringUtils.isNotEmpty(product)) {
            sqlStr += " AND  l.productId=@productId ";
            countSqlStr += " AND  l.productId=@productId ";
        }
        String beginDate = null;
        String endDate = null;
        if (StringUtils.isNotEmpty(loanTime)){
            String date[] = loanTime.split("to");
            if(null != date && date.length > 1){
                beginDate = date[0];
                endDate = date[1];
                sqlStr+=" AND l.loanTime>=@beginDate  AND l.loanTime<=@endDate ";
                countSqlStr+=" AND l.loanTime>=@beginDate  AND l.loanTime<=@endDate ";
            }

        }
        if (StringUtils.isNotEmpty(productTypeId)){
            sqlStr+=" AND  l.productTypeId=@productTypeId ";
            countSqlStr+=" AND  l.productTypeId=@productTypeId ";
        }
        if(loanStatusArr!=null&&loanStatusArr.length==1&&loanStatusArr[0].equals(LoanStatus.APPROVEEND.name())){
            sqlStr+=" order by l.submitTime asc,l.updateTime asc ";
        }else{
            sqlStr+=" order by l.submitTime desc,l.updateTime desc ";
        }
        sqlStr += "limit @start,@length ";
        Sql sql = Sqls.create(sqlStr);
        sql.setParam("loanStatus",loanStatusArr);
        sql.setParam("agencyId",agencyId);
        sql.setParam("orgId",orgId);
        sql.setParam("businessLine",businessLine);
        sql.setParam("productTypeId",productTypeId);
        sql.setParam("borrower",'%'+borrower+'%');
        sql.setParam("beginDate",beginDate+" 00:00:00");
        sql.setParam("endDate",endDate+" 23:59:59");
        sql.setParam("start",param.getStart());
        sql.setParam("length",param.getLength());
        sql.setParam("channelId",channelId);
        sql.setParam("productId", product);


        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("loanStatus",loanStatusArr);
        countSql.setParam("agencyId",agencyId);
        countSql.setParam("orgId",orgId);
        countSql.setParam("businessLine",businessLine);
        countSql.setParam("productTypeId",productTypeId);
        countSql.setParam("borrower",'%'+borrower+'%');
        countSql.setParam("beginDate",beginDate+" 00:00:00");
        countSql.setParam("endDate",endDate+" 23:59:59");
        countSql.setParam("channelId",channelId);
        countSql.setParam("productId", product);

        sql.setCallback(Sqls.callback.maps());
        dao().execute(sql);
        List<Map> list = sql.getList(Map.class);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        if(CollectionUtils.isEmpty(list)){
            list = new ArrayList<>();
        }
        for (Map<String,String> map:list) {
            map.put("saleName",getBusinessSource( map.get("channelId"),  map.get("businessLine"), map.get("orgCode"), map.get("saleName")));
        }
        return new DataTables(param.getDraw(),count,count,list);
    }

    private LoanStatus[] getLoanStatuses(String loanStatus, LoanStatus[] statusArr) {
        if (StringUtils.isNotEmpty(loanStatus)) {
            String[] loanStatusStr = loanStatus.split(",");
            statusArr = new LoanStatus[loanStatusStr.length];
            int index = 0;
            for (String status : loanStatusStr) {
                statusArr[index++] = LoanStatus.valueOf(status);
            }
        }
        return statusArr;
    }


    public Object queryLoanElementList(DataTableParam param){

        String borrower = "";
        String saleName = "";
        String loanStatus = "";
        String channelId = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            saleName = keys.get("saleName");
            borrower = keys.get("borrower");
            loanStatus = keys.get("loanStatus");
            channelId = keys.get("channelId");
        }
        LoanStatus[] statusArr = {LoanStatus.LOANED, LoanStatus.CLEARED, LoanStatus.OVERDUE};
        statusArr = getLoanStatuses(loanStatus, statusArr);
        String sqlStr = " SELECT DISTINCT "+
                " IFNULL(l.id,'--') AS 'id',"+
                " IFNULL(l.`code`,'--') AS 'code',"+
                " IFNULL(pt.name,'--') AS 'productTypeName',"+
                " IFNULL(u.name,'--') AS 'saleName',"+
                " (select lb.name from sl_loan_borrower lb where lb.loanId=l.id and lb.master=1) AS 'borrserName',"+
                " IFNULL(l.submitTime,'--') AS 'submitTime',"+
                " IFNULL(l.amount,'--') AS 'amount',"+
                " IFNULL(l.actualAmount,'--') AS 'actualAmount',"+
                " IFNULL(l.loanTime,'--') AS 'loanTime',"+
                " IFNULL(o.businessLine,'--') AS 'businessLine',"+
                " IFNULL(l.term,'--') AS 'term',"+
                " IFNULL(l.approveStatusDesc,'--') AS 'approveStatus',"+
                " IFNULL(o.name,'--') AS 'organizeName',"+
                " IFNULL(l.termType,'--') AS 'termType',"+
                " IFNULL(l.loanStatus,'--') AS 'loanStatus',"+
                " IFNULL(l.elementStatus,'--') AS 'elementStatus',"+
                " IFNULL(a.name,'--') AS 'agencyName',"+
                " IFNULL(l.elementStatus,'--') AS 'elementStatus',"+
                " IFNULL(l.elementConfirmName,'--') AS 'elementConfirmName',"+
                " IFNULL(l.channelId,'--') AS 'channelId',"+
                " IFNULL(o.businessLine,'--') AS 'businessLine',"+
                " IFNULL(o.code,'--') AS 'orgCode',"+
                " IFNULL(l.elementConfirmTime,'--') AS 'elementConfirmTime'"+
                " FROM sl_loan l,sl_product_type pt,sl_business_user u,sl_business_organize o,sl_business_agency a,sl_product p,sl_product_info_tmpl tm"+
                " WHERE u.id = l.saleId "+
                " AND l.`status` = 'ABLE'"+
                " AND l.productTypeId = pt.id"+
                " AND u.organizeId = o.id"+
                " AND o.agencyId = a.id"+
                " AND l.productId = p.id"+
                " AND p.infoTmpId = tm.id"+
                " AND tm.`productTempType` = 'SHULOU'"+
                " AND l.loanStatus in (@loanStatus) ";


        String countSqlStr =" SELECT  COUNT(DISTINCT l.id) AS 'number'  "+
                " FROM sl_loan l,sl_product_type pt,sl_business_user u,sl_business_organize o,sl_business_agency a,sl_product p,sl_product_info_tmpl tm "+
                " WHERE u.id = l.saleId "+
                " AND l.`status` = 'ABLE'"+
                " AND l.productTypeId = pt.id"+
                " AND u.organizeId = o.id"+
                " AND o.agencyId = a.id"+
                " AND l.productId = p.id"+
                " AND p.infoTmpId = tm.id"+
                " AND tm.`productTempType` = 'SHULOU'"+
                " AND l.loanStatus in (@loanStatus) ";

        String[] loanStatusArr = new String[statusArr.length];
        int i=0;
        for(LoanStatus status:statusArr){
            loanStatusArr[i++]=status.name();
        }

        if(StringUtils.isNotEmpty(borrower)){
            sqlStr+=" AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
            countSqlStr+=" AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
        }
        if (StringUtils.isNotEmpty(saleName)){
            sqlStr+=" AND  u.name like @saleName ";
            countSqlStr+=" AND  u.name like @saleName ";
        }
        if (StringUtils.isNotEmpty(channelId)){
            sqlStr+=" AND  l.channelId = @channelId ";
            countSqlStr+=" AND  l.channelId = @channelId ";
        }

        sqlStr+=" order by l.updateTime desc limit @start,@length ";


        Sql sql = Sqls.create(sqlStr);
        sql.setParam("loanStatus",loanStatusArr);

        sql.setParam("start",param.getStart());
        sql.setParam("length",param.getLength());
        sql.setParam("borrower",'%'+borrower+'%');
        sql.setParam("saleName",'%'+saleName+'%');
        sql.setParam("channelId",channelId);

        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("loanStatus",loanStatusArr);
        countSql.setParam("borrower",'%'+borrower+'%');
        countSql.setParam("saleName",'%'+saleName+'%');
        countSql.setParam("channelId",channelId);

        sql.setCallback(Sqls.callback.maps());
        dao().execute(sql);
        List<Map> list = sql.getList(Map.class);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        if(CollectionUtils.isEmpty(list)){
            list = new ArrayList<>();
        }
        for(Map<String,String> map:list) {
            map.put("saleName",getBusinessSource( map.get("channelId"), map.get("businessLine"), map.get("orgCode"), map.get("saleName")));
        }

        return new DataTables(param.getDraw(),count,count,list);
    }

    public  int  updateLoanElementStatus(Loan loan){
        User user= ShiroSession.getLoginUser();
        if(user!=null){
            loan.setElementConfirmName(String.valueOf(user.getName()));
            loan.setElementConfirmTime(new Date());
        }
           return  dao().update(loan);
    }

    /**
     * 查询已完成审批列表
     * @param param
     * @return
     */
    public Object queryApprovalCompleteList(DataTableParam param) {
        String userId = ShiroSession.getLoginUser().getId();
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String productType = "";
        String borrower = "";
        String approvalTime = "";
        String status = "";
        BigDecimal minAmount = null;
        BigDecimal maxAmount = null;
        String channelId = "";
        String product = "";
        String apply = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            productType = keys.get("productType");
            borrower = keys.get("borrower");
            approvalTime = keys.get("approvalTime");
            status = keys.get("status");
            minAmount = StringUtils.isEmpty(keys.get("minAmount"))==true ? null : new BigDecimal(keys.get("minAmount"));
            maxAmount = StringUtils.isEmpty(keys.get("maxAmount"))==true ? null : new BigDecimal(keys.get("maxAmount"));
            channelId = keys.get("channelId");
            product = keys.get("product");
            apply = keys.get("apply");
        }
        Date beginDate = setQueryStartDateTime(approvalTime);
        Date endDate = setQueryEndDateTime(approvalTime);
        String sqlStr = "SELECT"+
                " IFNULL(l.id,'--') AS 'id',"+
                " IFNULL(l.`code`,'--') AS 'code',"+
                " IFNULL(pt.name,'--') AS 'productTypeName',"+
                " IFNULL(sp.name,'--') AS 'productName'," +
                " IFNULL(u.name,'--') AS 'saleName',"+
                " IFNULL(lb.name,'--') AS 'borrserName'," +
                " IFNULL(l.submitTime,'--') AS 'submitTime',"+
                " IFNULL(l.amount,'--') AS 'amount',"+
                " IFNULL(l.term,'--') AS 'term',"+
                " IFNULL(l.approveStatusDesc,'--') AS 'approveStatus',"+
                " IFNULL(wt.display_Name,'--') AS 'nextStatus'," +
                " IFNULL(o.name,'--') AS 'organizeName',"+
                " IFNULL(l.termType,'--') AS 'termType',"+
                " IFNULL(l.loanStatus,'--') AS 'loanStatus',"+
                " IFNULL(l.channelId,'--') AS 'channelId',"+
                " IFNULL(o.businessLine,'--') AS 'businessLine',"+
                " IFNULL(o.code,'--') AS 'orgCode',"+
                " IFNULL(a.name,'--') AS 'agencyName'"+
                " FROM sl_loan l"+
                " LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id " +
                " LEFT JOIN sl_product sp ON l.productId = sp.id " +
                " LEFT JOIN sl_business_user u ON u.id = l.saleId " +
                " LEFT JOIN sl_business_organize o ON u.organizeId = o.id " +
                " LEFT JOIN sl_business_agency a  ON o.agencyId = a.id"+
                " LEFT JOIN (select name,loanId from sl_loan_borrower where master=1) lb ON lb.loanId=l.id" +
                " LEFT JOIN (select  substring(variable, POSITION(\"\\\"loanId\\\":\" IN variable)+10, 36) loanId,display_Name  from wf_task) wt on l.id=wt.loanId" +
                " WHERE l.status = 'ABLE'"+
                " AND  @userId IN (SELECT r.userId FROM sl_approval_result r WHERE r.loanId = l.id  and r.flowConfigureType ='BORROW_APPLY' )";

        String countSqlStr = "SELECT "+
                " count(l.id) AS 'number' "+
                " FROM sl_loan l" +
                " LEFT JOIN sl_product_type pt ON l.productTypeId = pt.id " +
                " LEFT JOIN sl_product sp ON l.productId = sp.id " +
                " LEFT JOIN sl_business_user u ON u.id = l.saleId " +
                " LEFT JOIN sl_business_organize o ON u.organizeId = o.id " +
                " LEFT JOIN sl_business_agency a  ON o.agencyId = a.id"+
                " WHERE l.status = 'ABLE'"+
                " AND  @userId IN (SELECT r.userId FROM sl_approval_result r WHERE r.loanId = l.id and r.flowConfigureType ='BORROW_APPLY')";

        if (!StringUtils.isEmpty(productType)){
            sqlStr+=" AND l.productTypeId=@productType ";
            countSqlStr+=" AND l.productTypeId=@productType ";
        }

        if (StringUtils.isNotEmpty(borrower)){
            sqlStr+=" AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
            countSqlStr+=" AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
        }

        if (null!=beginDate&&null!=endDate){
            sqlStr+=" AND l.submitTime>=@beginDate  AND l.submitTime<=@endDate ";
            countSqlStr+=" AND l.submitTime>=@beginDate  AND l.submitTime<=@endDate ";
        }
        if (StringUtils.isNotEmpty(status)){
            sqlStr+=" AND  l.loanStatus=@status ";
            countSqlStr+=" AND  l.loanStatus=@status ";
        }
        if (StringUtils.isNotEmpty(channelId)){
            sqlStr+=" AND  l.channelId=@channelId ";
            countSqlStr+=" AND  l.channelId=@channelId ";
        }

        if (StringUtils.isNotEmpty(product)) {
            sqlStr += " AND  l.productId=@productId ";
            countSqlStr += " AND  l.productId=@productId ";
        }

        if(StringUtils.isNotEmpty(apply)){
            sqlStr += " AND u.name like @apply ";
            countSqlStr += " AND  u.name like @apply ";
        }

        if(minAmount==null){
            minAmount = BigDecimal.ZERO;
        }
        if(maxAmount==null){
            maxAmount = BigDecimal.valueOf(Double.MAX_VALUE);
        }
        sqlStr += " AND (l.amount>=@minAmount and l.amount<=@maxAmount) ";
        countSqlStr += " AND (l.amount>=@minAmount and l.amount<=@maxAmount) ";

        sqlStr+=" order by l.submitTime desc,l.updateTime desc ";

        Sql sql = Sqls.create(sqlStr);
        Sql countSql = Sqls.create(countSqlStr);
        sql.setParam("productType",productType);
        sql.setParam("borrower",'%'+borrower+'%');
        sql.setParam("beginDate",beginDate);
        sql.setParam("endDate",endDate);
        sql.setParam("userId",userId);
        sql.setParam("status",status);
        sql.setParam("minAmount",minAmount);
        sql.setParam("maxAmount",maxAmount);
        sql.setParam("channelId",channelId);
        sql.setParam("productId", product);
        sql.setParam("apply",'%'+apply+'%');
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.maps());
        dao().execute(sql);
        List<Map> list = sql.getList(Map.class);

        countSql.setParam("productType",productType);
        countSql.setParam("borrower",'%'+borrower+'%');
        countSql.setParam("beginDate",beginDate);
        countSql.setParam("endDate",endDate);
        countSql.setParam("userId",userId);
        countSql.setParam("status",status);
        countSql.setParam("minAmount",minAmount);
        countSql.setParam("maxAmount",maxAmount);
        countSql.setParam("channelId",channelId);
        countSql.setParam("productId", product);
        countSql.setParam("apply",'%'+apply+'%');
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        if(CollectionUtils.isEmpty(list)){
            list = new ArrayList<>();
        }
        for(Map<String,String> map:list){
            map.put("saleName",getBusinessSource( map.get("channelId"), map.get("businessLine"), map.get("orgCode"), map.get("saleName")));
        }
        return new DataTables(param.getDraw(),count,count,list);
    }

    public List<MobileApplyView> queryApplyList(int pageNumber, int length, String businessName, String borrowerName) {
        String querySql = "select l.id id,l.code code,u.name businessName, " +
                "(select lb.name from sl_loan_borrower lb where lb.loanId=l.id and lb.master=1 limit 1) borrowerName,l.amount amount,l.termType termType, " +
                "l.term term, l.submitTime submitTime, " +
                " lp.name productTypeName, " +
                " p.name productName " +
                "from sl_loan l " +
                "left join sl_business_user u " +
                "on l.saleId=u.id " +
                "LEFT JOIN sl_product_type lp " +
                "ON lp.id=l.productTypeId " +
                "LEFT JOIN sl_product p ON l.productId = p.id " +
                "where l.status='ABLE' ";

        if (StringUtils.isNotEmpty(businessName)) {
            querySql += "and u.name like @businessName ";
        }
        if (StringUtils.isNotEmpty(borrowerName)) {
            querySql += " AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
        }
        querySql+=" order by l.submitTime desc,l.updateTime desc";
        Sql sql = Sqls.queryEntity(querySql);
        sql.setParam("borrower",'%'+borrowerName+'%');
        sql.setParam("businessName",'%'+businessName+'%');
        Pager pager = new Pager();
        pager.setPageNumber(pageNumber);
        pager.setPageSize(length);
        sql.setPager(pager);
        sql.setEntity(dao().getEntity(MobileApplyView.class));
        dao().execute(sql);
        return sql.getList(MobileApplyView.class);

    }

    /**
     * 根据id更新状态
     * @param id
     * @param loanStatus
     * @return
     */
    public  boolean  updateLoanStatus(String id ,LoanStatus loanStatus){
        User user= ShiroSession.getLoginUser();
        return dao().update(Loan.class, Chain.make("LoanStatus",loanStatus).add("updateBy",user.getName()).add("updateTime",new Date()),Cnd.where("id","=",id))>0;
    }

    public NutMap cancel(String loanId){
        Loan loan = this.fetchById(loanId);
        loan.setLoanStatus(LoanStatus.LOANCANCEL);
        boolean flag = this.update(loan);
        Product product = productService.fetchEnableProductById(loan.getProductId());
        if(null != product && ("商业承兑汇票").equals(product.getName())) {
            billLoanService.changeAmount(loanId);
        }
        return new NutMap().setv("ok",flag);
    }
    public String getBusinessSource(String channelId,String line,String orgCode,String saleName){
        String source="";
        String lineName="";
        String lineCode="";
        saleName = TextFormatUtils.replaceNull(saleName,"");
        //老数据自营渠道
        if("--".equals(channelId)|| Strings.isBlank(channelId)){
            lineName=TextFormatUtils.replaceNull(BusinessLine.getDescription(line),"");
            lineCode=TextFormatUtils.replaceNull(BusinessLine.getCode(line),"");
            if(Strings.isBlank(line)){
                source="自营|--";
            }else {
                source="自营|"+lineName+"-"+orgCode+"-"+saleName;
            }

        }else {
            Channel channel=channelService.fetch(channelId);
            if(channel!=null){
                if(channel.getChannelType()!=null){
                    if (channel.getChannelType().equals("0")){
                        lineName=TextFormatUtils.replaceNull(BusinessLine.getDescription(line),"");
                        lineCode=TextFormatUtils.replaceNull(BusinessLine.getCode(line),"");
                        if(Strings.isBlank(line)){
                            source="自营|--";
                        }else {
                            source="自营|"+lineName+"-"+orgCode+"-"+saleName;
                        }
                    }else {
                        source="渠道|"+channel.getName();
                    }
                }
            }
        }
        return  source;
    }

    private Date setQueryStartDateTime(String submitTime) {
        try {
            if (StringUtils.isNotEmpty(submitTime)) {
                String[] arr = submitTime.split("~");
                if (null != arr && arr.length == 2) {
                    return df.parse(arr[0] + " 00:00:00");
                }
            }
        } catch (ParseException e) {
            log.debug("传入时间错误");
        }
        return null;
    }

    private Date setQueryEndDateTime(String submitTime) {
        try {
            if (StringUtils.isNotEmpty(submitTime)) {
                String[] arr = submitTime.split("~");
                if (null != arr && arr.length == 2) {
                    return df.parse(arr[1] + " 23:59:59");
                }
            }
        } catch (ParseException e) {
            log.debug("传入时间错误");
        }
        return null;
    }

}

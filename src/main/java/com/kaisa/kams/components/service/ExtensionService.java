package com.kaisa.kams.components.service;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.base.BaseService;
import com.kaisa.kams.components.service.flow.FlowConfigureService;
import com.kaisa.kams.components.service.flow.FlowService;
import com.kaisa.kams.components.utils.*;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.business.BusinessUser;
import org.apache.commons.lang.StringUtils;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.WorkItem;
import org.snaker.engine.model.TaskModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouchuang on 2017/10/31.
 */
@IocBean(fields = "dao")
public class ExtensionService extends BaseService<Extension> {
    @Inject
    private FlowConfigureService flowConfigureService;
    @Inject
    private FlowService flowService;
    @Inject
    private UserService userService;
    @Inject
    private LoanService loanService;
    @Inject
    private ChannelService channelService;
    @Inject
    private BusinessUserService businessUserService;
    @Inject
    private BusinessExtensionService businessExtensionService;

    public NutMap getExtensionById(String id){
        Extension extension  = this.fetch(id);
        LoanExtension loanExtension = businessExtensionService.getLoanExtensionByExtension(extension);
        if (loanExtension.getInterestRate() != null) {
            loanExtension.setInterestRate(loanExtension.getInterestRate());
        }
        extension.setLoanExtension(loanExtension);
        NutMap result = businessExtensionService.queryRepayByExtension(extension);
        return result;
    }


    @Aop(TransAop.READ_COMMITTED)
    public void startApprovalProcess(String loanId,
                                      LoanTermType termType,
                                      String term,
                                      LoanLimitType loanLimitType,
                                      BigDecimal interest,
                                      String repayMethod, String enterpriseExplain
            , String enterpriseAgreement, String calculationMethod, String repayDateType)throws Exception{
        LoanExtension loanExtension = businessExtensionService.getLoanExtension(loanId, termType, term, loanLimitType, interest, enterpriseExplain, enterpriseAgreement, repayMethod, calculationMethod, repayDateType);
        Extension extension = null;
        try {
            extension = getExtensionByLoanExtension(loanExtension);
        }catch (Exception e){
            e.printStackTrace();
            throw new KamsException("获取不到借款申请单！");
        }
        Extension currentExtension = getCurrentExtensionByLoanId(loanId);
        //如果该单已经展期申请过，并且是编辑状态，则更新该单，不是重新申请
        if(currentExtension!=null){
            if(ApprovalStatusType.IN_EDIT.equals(currentExtension.getApprovalStatusType())){
                extension.setId(currentExtension.getId());
                extension.setApproveStatus(currentExtension.getApproveStatus());
                extension.setApproveStatusDesc(currentExtension.getApproveStatusDesc());
            }else if(ApprovalStatusType.IN_APPROVAL.equals(currentExtension.getApprovalStatusType())){
                throw new KamsException("当前展期申请单已经在审批中！");
            }
        }
        try {
            updateExtension(extension, null);
        }catch (Exception e){
            e.printStackTrace();
            throw new KamsException("更新审批订单失败！");
        }
        FlowConfigureType flowType = FlowConfigureType.EXTENSION;
        ProductProcess productProcess = flowConfigureService.fetchEnableProductProductById(extension.getProductId(), flowType);
        if (productProcess == null) {
            throw new KamsException("提交订单失败:产品尚未配置流程！");
        }
        //查看当前产品的流程文件是否存在
        boolean snakerExist = flowService.existSnakerFile(extension.getProductId(), flowType);
        if (!snakerExist) {
            throw new KamsException("提交订单失败:流程引擎未部署或产品未启动！");
        }

        Order order = flowService.startInstanceWithExtension(extension, flowType);
        if (null == order) {
            throw new KamsException("提交订单失败:初始化流程引擎失败！");
        } else {
            extension.setApprovalStatusType(ApprovalStatusType.IN_APPROVAL);
        }
        try{
            updateExtension(extension,null);
        }catch (Exception e){
            throw  new KamsException("保存展期审批失败！");
        }
    }

    private Extension getExtensionByLoanExtension(LoanExtension loanExtension)throws Exception{
        Loan loan  = dao().fetch(Loan.class,loanExtension.getLoanId());
        LoanRepay loanRepay = dao().fetch(LoanRepay.class,Cnd.where("loanId","=",loanExtension.getLoanId()).desc("period"));
        Extension extension = new Extension();
        extension.setLoanId(loanExtension.getLoanId());
        extension.setTerm(loanExtension.getTerm());
        extension.setTermType(loanExtension.getTermType());
        extension.setLoanLimitType(loanExtension.getLoanLimitType());
        extension.setInterestAmount(loanExtension.getInterestAmount());
        extension.setInterestRate(loanExtension.getInterestRate());
        extension.setEnterpriseExplain(loanExtension.getEnterpriseExplain());
        extension.setEnterpriseAgreement(loanExtension.getEnterpriseAgreement());
        extension.setPosition(loanExtension.getPosition());
        extension.setRepayMethod(loanExtension.getRepayMethod());
        extension.setCalculationMethod(loanExtension.getCalculationMethod());
        extension.setRepayDateType(loanExtension.getRepayDateType());
        extension.setProductId(loan.getProductId());
        extension.setApprovalStatusType(ApprovalStatusType.IN_EDIT);
        extension.setAmount(loanRepay.getAmount().subtract(DecimalFormatUtils.getNotNull(loanRepay.getRepayAmount())));
        extension.updateOperator();
        return extension;
    }
    public Extension updateExtension(Extension extension,String filter){
        if(StringUtils.isEmpty(filter)) {
            filter = "^(amount|termType|term|loanLimitType|interestAmount|interestRate|enterpriseExplain|enterpriseAgreement|position|repayMethod|calculationMethod|repayDateType|approvalStatusType|approveStatus|approveStatusDesc|productId|updateBy|updateTime)$";
        }
        if(StringUtils.isEmpty(extension.getId())){
            extension.setExtensionCode(this.getNextFlowCode());
        }
        return persistence(extension,filter);
    }

    public LoanExtension updateLoanExtension(LoanExtension loanExtension,String filter){
        if(StringUtils.isEmpty(filter)){
            filter = "^(extensionId|status|updateBy|updateTime)$";
        }
        return persistence(loanExtension,filter);
    }
    private String getNextFlowCode(){
        Cnd cnd = Cnd.where("1","=","1");
        cnd.desc("extensionCode");
        Extension extension = dao().fetch(Extension.class,cnd);
        if(extension!=null){
            String flowCode = extension.getExtensionCode();
            String   maxCode =  flowCode.replaceAll("[A-Z]+[0]+","");
            maxCode =""+ (Integer.parseInt(maxCode)+1);
            return "YWZQ000000".substring(0,10-maxCode.length())+maxCode;
        }else{
            return "YWZQ000001";
        }
    }

    public boolean deleteLoanExtensionById(String loanExtensionId){
        int flag = dao().clear(LoanExtension.class,Cnd.where("id","=",loanExtensionId));
        return flag>0;
    }

    public void disabledLoanExtensionById(String loanExtensionId){
        LoanExtension loanExtension = dao().fetch(LoanExtension.class,loanExtensionId);
        updateLoanExtension(loanExtension,null);
    }
    /**
     * 查询待审批列表
     * @param param
     * @return
     */
    public DataTables queryApprovalList(DataTableParam param) {
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String type;
        String submitDate ;
        String extensionCode;
        String code;
        Date beginDate = null;
        Date endDate = null;
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            type =  keys.get("type");
            code = keys.get("code");
            extensionCode = keys.get("extensionCode");
            submitDate = keys.get("submitDate");
            if (StringUtils.isNotEmpty(submitDate)) {
                String date[] = submitDate.split("to");
                if (null != date && date.length > 1) {
                    beginDate = TimeUtils.formatDate("yyyy-MM-dd", date[0]);
                    endDate = TimeUtils.formatDate("yyyy-MM-dd", date[1]);
                }
            }
        } else {
            return null;
        }
        User user = userService.fetchLinksById(ShiroSession.getLoginUser().getId());
        List<Role> roles = user.getRoles();

        if(null==roles||roles.isEmpty()){
            return new DataTables(param.getDraw(),0,0,null);
        }
        List<WorkItem> majorWorks = flowService.getWorkItemList(roles);
        //获取到所有的loanId
        List<String> extensionIds = new ArrayList<>();
        List<String> orderIds = new ArrayList<>();
        for (WorkItem workItem:majorWorks){
            if(org.apache.commons.lang3.StringUtils.isEmpty(workItem.getTaskName())||workItem.getTaskKey().indexOf(type)<0){
                continue;
            }
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(workItem.getOrderId())) {
                orderIds.add(workItem.getOrderId());
            }
        }
        if (orderIds.size()>0) {
            extensionIds = getExtensionIds(orderIds);
        }
        //获取到需要当前用户处理的列表
        if(null==extensionIds||extensionIds.size()==0){
            return new  DataTables(param.getDraw(),0,0,new ArrayList<>());
        }

        String ids  = "('"+StringUtils.join(extensionIds.toArray(),"','")+"')";
        String sqlStr = "SELECT se.* ,spt.name as productTypeName,slb.name as borrowerName,sl.loanStatus,sl.code "+
                " FROM sl_extension se "+
                " LEFT JOIN sl_loan sl on sl.id = se.loanId"+
                " LEFT JOIN sl_product_type spt on spt.id = sl.productTypeId"+
                " LEFT JOIN sl_loan_borrower slb on slb.loanId = sl.id  and slb.master = 1 "+
                " LEFT JOIN (select  substring(variable, POSITION(\"\\\"extensionId\\\":\" IN variable)+15, 36) extensionId,display_Name  from wf_task) wt on se.id=wt.extensionId" +
                " WHERE 1=1"+
                " AND se.approvalStatusType = 'IN_APPROVAL' "+
                " AND se.id IN "+ids.toString();
        String countSqlStr = "SELECT "+
                " COUNT(DISTINCT se.id) AS 'number' "+
                " FROM sl_extension se "+
                " LEFT JOIN sl_loan sl on sl.id = se.loanId"+
                " WHERE 1=1"+
                " AND se.approvalStatusType = 'IN_APPROVAL' "+
                " AND se.id IN "+ids.toString();
        if(StringUtils.isNotEmpty(extensionCode)){
            sqlStr += " AND se.extensionCode like @extensionCode";
            countSqlStr += " AND se.extensionCode like @extensionCode";
        }
        if(StringUtils.isNotEmpty(code)){
            sqlStr += " AND sl.code like @code";
            countSqlStr += " AND sl.code like @code";
        }
        if (beginDate != null && endDate != null) {
            sqlStr += "and se.createTime between @beginDate and @endDate ";
            countSqlStr += " and se.createTime between @beginDate and @endDate ";
        }
        sqlStr += " order by se.updateTime desc";
        Sql sql = Sqls.create(sqlStr);
        sql.setParam("extensionCode", '%' + extensionCode + '%');
        sql.setParam("beginDate", beginDate);
        sql.setParam("endDate", endDate);
        sql.setParam("code",'%'+code+ '%');
        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("extensionCode", '%' + extensionCode + '%');
        countSql.setParam("beginDate", beginDate);
        countSql.setParam("endDate", endDate);
        countSql.setParam("code",'%'+code+ '%');
        sql.setPager(pager);
        sql.setCallback(SqlUtil.getSqlCallback(Extension.class));
        dao().execute(sql);
        List<Extension> list = sql.getList(Extension.class);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        if(null==list){
            list = new ArrayList<>();
        }
        for(Extension extension : list){
            setChannelInfo(extension);
        }
        return new DataTables(param.getDraw(),count,count,list);
    }
    public LoanExtension getLoanExtensionByLoanExtensionId(String loanExtensionId){
        LoanExtension loanExtension = dao().fetch(LoanExtension.class,loanExtensionId);
        return loanExtension;
    }
    private void setChannelInfo(Extension extension) {
        Loan loan  = dao().fetch(Loan.class,extension.getLoanId());
        Channel channel = null;
        if(StringUtils.isNotEmpty(loan.getChannelId())&&(channel=channelService.fetch(loan.getChannelId()))!=null&&ChannelType.QD.getCode().equals(channel.getChannelType())){
            extension.setBusinessSource("渠道|"+channel.getName());
            return;
        }else{
            BusinessUser businessUser = businessUserService.fetchById(loan.getSaleId());
            extension.setBusinessSource("自营|房贷-"+businessUser.getOrganize().getCode()+"-"+businessUser.getName());
            return;
        }
    }

    /**
     * 获取extensionId列表
     * @param orderIds
     * @return
     */
    public List<String> getExtensionIds(List<String> orderIds) {
        Sql sql = Sqls.create("SELECT extensionId FROM sl_extension_order WHERE orderId in(@orderIds)");
        sql.setParam("orderIds", orderIds.toArray());
        sql.setCallback(Sqls.callback.strs());
        dao().execute(sql);
        return sql.getList(String.class);
    }

    /**
     * 查询已完成审批列表
     * @param param
     * @return
     */
    public DataTables queryApprovalCompleteList(DataTableParam param) {
        String userId = ShiroSession.getLoginUser().getId();
        String approvalType ;
        String submitDate ;
        String extensionCode;
        String code;
        Date beginDate = null;
        Date endDate = null;
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            approvalType =  keys.get("type");
            extensionCode = keys.get("extensionCode");
            submitDate = keys.get("submitDate");
            code = keys.get("code");
            if (StringUtils.isNotEmpty(submitDate)) {
                String date[] = submitDate.split("to");
                if (null != date && date.length > 1) {
                    beginDate = TimeUtils.formatDate("yyyy-MM-dd", date[0]);
                    endDate = TimeUtils.formatDate("yyyy-MM-dd", date[1]);
                }
            }
        } else {
            return new DataTables(param.getDraw(),0,0,new ArrayList());
        }

        String sqlStr = "SELECT  se.* ,spt.name as productTypeName,slb.name as borrowerName,sl.loanStatus,sl.code    FROM  sl_extension se  " +
                " LEFT JOIN sl_loan sl on sl.id = se.loanId"+
                " LEFT JOIN sl_product_type spt on spt.id = sl.productTypeId"+
                " LEFT JOIN sl_loan_borrower slb on slb.loanId = sl.id and slb.master = 1 "+
                " LEFT JOIN ( " +
                " SELECT " +
                "  substring( " +
                "   variable, " +
                "   POSITION( " +
                "    \"\\\"extensionId\\\":\" IN variable " +
                "   ) + 15, " +
                "   36 " +
                "  ) extensionId, " +
                "  display_Name " +
                " FROM " +
                "  wf_task " +
                ") wt ON se.id = wt.extensionId " +
                "WHERE " +
                " 1 = 1 " +
                "AND @userId IN ( " +
                " SELECT " +
                "  r.userId " +
                " FROM " +
                "  sl_approval_result r " +
                " WHERE " +
                "  r.loanId = se.id " +
                " AND flowConfigureType = 'EXTENSION' " +
                " AND r.approvalType = @approvalType " +
                ") " ;
        String countSqlStr = "SELECT "+
                " count(se.id) AS 'number' "+
                " FROM sl_extension se " +
                " LEFT JOIN sl_loan sl on sl.id = se.loanId"+
                " WHERE 1=1 "+
                " AND  @userId IN (SELECT r.userId FROM sl_approval_result r WHERE r.loanId = se.id AND r.flowConfigureType ='EXTENSION' AND r.approvalType =@approvalType)";
        if(StringUtils.isNotEmpty(extensionCode)){
            sqlStr += " AND se.extensionCode like @extensionCode";
            countSqlStr += " AND se.extensionCode like @extensionCode";
        }
        if(StringUtils.isNotEmpty(code)){
            sqlStr += " AND sl.code like @code";
            countSqlStr += " AND sl.code like @code";
        }
        if (beginDate != null && endDate != null) {
            sqlStr += "and se.createTime between @beginDate and @endDate ";
            countSqlStr += " and se.createTime between @beginDate and @endDate ";
        }
        sqlStr += " order by se.updateTime desc";
        Sql sql = Sqls.create(sqlStr);
        sql.setParam("approvalType",approvalType);
        sql.setParam("userId",userId);
        sql.setParam("extensionCode", '%' + extensionCode + '%');
        sql.setParam("beginDate", beginDate);
        sql.setParam("endDate", endDate);
        sql.setParam("code", '%'+code+ '%');
        sql.setPager(pager);
        sql.setCallback(SqlUtil.getSqlCallback(Extension.class));
        dao().execute(sql);
        List<Extension> list = sql.getList(Extension.class);
        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("approvalType",approvalType);
        countSql.setParam("userId",userId);
        countSql.setParam("extensionCode", '%' + extensionCode + '%');
        countSql.setParam("beginDate", beginDate);
        countSql.setParam("endDate", endDate);
        countSql.setParam("code", '%'+code+ '%');
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        for(Extension extension : list){
            setChannelInfo(extension);
        }
        return new DataTables(param.getDraw(),count,count,list);
    }

    public  Extension getCurrentExtensionByLoanId(String loanId){
        return dao().fetch(Extension.class,Cnd.where("loanId","=",loanId).desc("extensionCode"));
    }
}

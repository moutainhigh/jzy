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
import org.apache.commons.collections.CollectionUtils;
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
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.WorkItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @description：费用免除数据访问层
 * @author：zhouchuang
 * @date：2017-11-14:48
 */
@IocBean(fields="dao")
public class CostExemptionService extends BaseService<CostExemption> {

    @Inject
    private UserService userService;
    @Inject
    private FlowService flowService;
    @Inject
    private FlowConfigureService flowConfigureService;
    @Inject
    private ChannelService channelService;
    @Inject
    private BusinessUserService businessUserService;
    @Inject
    private LoanRepayService loanRepayService;
    @Inject
    private ProductInfoTmplService productInfoTmplService;

    @Aop(TransAop.READ_COMMITTED)
    public void update(CostExemption costExemption )throws Exception{
        costExemption.setApprovalStatusType(ApprovalStatusType.IN_EDIT);
        Loan loan = dao().fetch(Loan.class,costExemption.getLoanId());
        costExemption.setProductId(loan.getProductId());
        costExemption.updateOperator();
        try {
            updateCostExemption(costExemption, "^(businessId|businessName|approvalStatusType|updateBy|updateTime)$");
        }catch (Exception e){
            throw new KamsException("费用免除申请保存失败");
        }
        if(CollectionUtils.isNotEmpty(costExemption.getCostExemptionItemList())){
            for(CostExemptionItem costExemptionItem : costExemption.getCostExemptionItemList()){
                costExemptionItem.setCostExemptionId(costExemption.getId());
                try{
                    updateCostExemptionItem(costExemptionItem,null);
                }catch (Exception e){
                    throw new KamsException("费用免除详情保存失败");
                }

            }
            removeDeletedBaseModel(this.queryCostExemptionItemByCostExemptionId(costExemption.getId()),costExemption.getCostExemptionItemList());
        }
    }

    public void cancelByCostExemptionId(String id)throws Exception{
        CostExemption costExemption  = fetch(id);
        costExemption.setApprovalStatusType(ApprovalStatusType.CANCEL);
        updateCostExemption(costExemption,null);
    }

    /**
     *审批完成后修改费用方法
     */
    @Aop(TransAop.READ_COMMITTED)
    public void updateFee(CostExemption costExemption)throws Exception{
        List<CostExemptionItem> costExemptionItems = queryCostExemptionItemByCostExemptionId(costExemption.getId());
        for(CostExemptionItem costExemptionItem : costExemptionItems){
            BigDecimal reductionFeeAmount ;
            LoanRepay loanRepay = loanRepayService.getLoanRepayByRepayId(costExemptionItem.getRepayId());
            LoanRepayRecord saveLoanRepayRecode=null;
            if(DecimalFormatUtils.isNotEmpty(costExemptionItem.getInterest())){
                loanRepay.setRepayInterest(DecimalFormatUtils.getNotNull(loanRepay.getRepayInterest()).add(costExemptionItem.getInterest()));
            }
            saveLoanRepayRecode = generalLoanRepayRecode(loanRepay,costExemptionItem.getInterest());
            if(DecimalFormatUtils.isNotEmpty(costExemptionItem.getGuaranteeFee())){
                generalLoanFeeRecode(FeeType.GUARANTEE_FEE,costExemptionItem.getGuaranteeFee(),saveLoanRepayRecode);
            }
            if(DecimalFormatUtils.isNotEmpty(costExemptionItem.getManageFee())){
                generalLoanFeeRecode(FeeType.MANAGE_FEE,costExemptionItem.getManageFee(),saveLoanRepayRecode);
            }
            if(DecimalFormatUtils.isNotEmpty(costExemptionItem.getOverdueFee())){
                generalLoanFeeRecode(FeeType.OVERDUE_FEE,costExemptionItem.getOverdueFee(),saveLoanRepayRecode);
            }
            if(DecimalFormatUtils.isNotEmpty(costExemptionItem.getPrepaymentFee())){
                generalLoanFeeRecode(FeeType.PREPAYMENT_FEE,costExemptionItem.getPrepaymentFee(),saveLoanRepayRecode);
            }
            if(DecimalFormatUtils.isNotEmpty(costExemptionItem.getPrepaymentFeeRate())){
                generalLoanFeeRecode(FeeType.PREPAYMENT_FEE_RATE,costExemptionItem.getPrepaymentFeeRate(),saveLoanRepayRecode);
            }
            if(DecimalFormatUtils.isNotEmpty(costExemptionItem.getServiceFee())){
                generalLoanFeeRecode(FeeType.SERVICE_FEE,costExemptionItem.getServiceFee(),saveLoanRepayRecode);
            }
            reductionFeeAmount = DecimalFormatUtils.getNotNull(costExemptionItem.getGuaranteeFee()).add( DecimalFormatUtils.getNotNull(costExemptionItem.getManageFee())).add(DecimalFormatUtils.getNotNull(costExemptionItem.getOverdueFee())).
                    add( DecimalFormatUtils.getNotNull(costExemptionItem.getPrepaymentFee())).add( DecimalFormatUtils.getNotNull(costExemptionItem.getPrepaymentFeeRate())).add( DecimalFormatUtils.getNotNull(costExemptionItem.getServiceFee()));
            loanRepay.setRepayFeeAmount(DecimalFormatUtils.getNotNull(loanRepay.getRepayFeeAmount()).add(reductionFeeAmount));
            loanRepay.setRepayTotalAmount(DecimalFormatUtils.getNotNull(loanRepay.getRepayTotalAmount()).add(reductionFeeAmount).add(costExemptionItem.getInterest()));
            loanRepayService.updateLoanRepay(loanRepay);
        }

    }
    public String validate(CostExemption costExemption){
        String msg = "";
        List<CostExemptionItem> costExemptionItems = queryCostExemptionItemByCostExemptionId(costExemption.getId());
        for(CostExemptionItem costExemptionItem : costExemptionItems){
            BigDecimal reductionFeeAmount ;
            LoanRepay loanRepay = loanRepayService.getLoanRepayByRepayId(costExemptionItem.getRepayId());
            if(DecimalFormatUtils.isNotEmpty(costExemptionItem.getInterest())){
                loanRepay.setRepayInterest(DecimalFormatUtils.getNotNull(loanRepay.getRepayInterest()).add(costExemptionItem.getInterest()));
            }
            if(DecimalFormatUtils.isNotEmpty(costExemptionItem.getGuaranteeFee())){
                msg += validateFee(FeeType.GUARANTEE_FEE,costExemptionItem.getGuaranteeFee(),costExemptionItem.getRepayId());
            }
            if(DecimalFormatUtils.isNotEmpty(costExemptionItem.getManageFee())){
                msg += validateFee(FeeType.MANAGE_FEE,costExemptionItem.getManageFee(),costExemptionItem.getRepayId());
            }
            if(DecimalFormatUtils.isNotEmpty(costExemptionItem.getOverdueFee())){
                msg += validateFee(FeeType.OVERDUE_FEE,costExemptionItem.getOverdueFee(),costExemptionItem.getRepayId());
            }
            if(DecimalFormatUtils.isNotEmpty(costExemptionItem.getPrepaymentFee())){
                msg += validateFee(FeeType.PREPAYMENT_FEE,costExemptionItem.getPrepaymentFee(),costExemptionItem.getRepayId());
            }
            if(DecimalFormatUtils.isNotEmpty(costExemptionItem.getPrepaymentFeeRate())){
                msg += validateFee(FeeType.PREPAYMENT_FEE_RATE,costExemptionItem.getPrepaymentFeeRate(),costExemptionItem.getRepayId());
            }
            if(DecimalFormatUtils.isNotEmpty(costExemptionItem.getServiceFee())){
                msg += validateFee(FeeType.SERVICE_FEE,costExemptionItem.getServiceFee(),costExemptionItem.getRepayId());
            }
            reductionFeeAmount = DecimalFormatUtils.getNotNull(costExemptionItem.getGuaranteeFee()).add( DecimalFormatUtils.getNotNull(costExemptionItem.getManageFee())).add(DecimalFormatUtils.getNotNull(costExemptionItem.getOverdueFee())).
                    add( DecimalFormatUtils.getNotNull(costExemptionItem.getPrepaymentFee())).add( DecimalFormatUtils.getNotNull(costExemptionItem.getPrepaymentFeeRate())).add( DecimalFormatUtils.getNotNull(costExemptionItem.getServiceFee()));
            loanRepay.setRepayFeeAmount(DecimalFormatUtils.getNotNull(loanRepay.getRepayFeeAmount()).add(reductionFeeAmount));
            loanRepay.setRepayTotalAmount(DecimalFormatUtils.getNotNull(loanRepay.getRepayTotalAmount()).add(reductionFeeAmount).add(costExemptionItem.getInterest()));
            msg += loanRepayService.validateLoanRepay(loanRepay);
        }
        return msg;
    }

    private LoanRepayRecord generalLoanRepayRecode(LoanRepay loanRepay,BigDecimal interest){
        LoanRepayRecord loanRepayRecord = new LoanRepayRecord();
        loanRepayRecord.setRepayDate(new Date());
        loanRepayRecord.setRepayTotalAmount(interest);
        loanRepayRecord.setRepayId(loanRepay.getId());
        loanRepayRecord.setRepayInterest(interest);
        loanRepayRecord.setRepayAmount(BigDecimal.ZERO);
        loanRepayRecord.updateOperator();
        loanRepayRecord.setCreateBy("系统");
        loanRepayRecord.setUpdateBy("系统");
        return dao().insert(loanRepayRecord);
    }
    private String  validateFee(FeeType feeType,BigDecimal amount,String  repayId){
        LoanFee loanFee = loanRepayService.getLoanFeeByRepayIdAndFeeType(repayId,feeType);
        loanFee.setRepayFeeAmount(DecimalFormatUtils.getNotNull(loanFee.getRepayFeeAmount()).add(amount));
        if(DecimalFormatUtils.compare(loanFee.getFeeAmount(),loanFee.getRepayFeeAmount())==-1)
            return feeType.getDescription()+"、";
        return "";
    }
    private void generalLoanFeeRecode(FeeType feeType,BigDecimal amount,LoanRepayRecord loanRepayRecord)throws KamsException{
        LoanFee loanFee = loanRepayService.getLoanFeeByRepayIdAndFeeType(loanRepayRecord.getRepayId(),feeType);
        loanFee.setDueDate(new Date());
        loanFee.setRepayFeeAmount(DecimalFormatUtils.getNotNull(loanFee.getRepayFeeAmount()).add(amount));
        LoanFeeRecord feeRecord = new LoanFeeRecord();
        feeRecord.setRepayFeeAmount(amount);
        feeRecord.setFeeAmount(loanFee.getFeeAmount());
        feeRecord.setLoanFeeId(loanFee.getId());
        feeRecord.setRepayRecordId(loanRepayRecord.getId());
        feeRecord.setRepayId(loanRepayRecord.getRepayId());
        feeRecord.updateOperator();
        feeRecord.setCreateBy("系统");
        feeRecord.setUpdateBy("系统");
        if(DecimalFormatUtils.compare(loanFee.getFeeAmount(),loanFee.getRepayFeeAmount())==-1)throw new KamsException("“"+feeType.getDescription()+"”费用减免金额应该小于应还金额");
        dao().insert(feeRecord);
        dao().update(loanFee,"^(dueDate|repayFeeAmount|updateTime)$");
    }
    private void reductionLoanFeeFeeAmount(String repayId,FeeType feeType,BigDecimal amount){
        LoanFee loanFee = loanRepayService.getLoanFeeByRepayIdAndFeeType(repayId,feeType);
        loanFee.setFeeAmount(loanFee.getFeeAmount().subtract(amount));
        loanRepayService.updateLoanFeeAmount(loanFee);
    }

    private List<CostExemptionItem> queryCostExemptionItemByCostExemptionId(String costExemptionId){
        return dao().query(CostExemptionItem.class,Cnd.where("costExemptionId","=",costExemptionId).asc("period"));
    }
    public CostExemption updateCostExemption(CostExemption costExemption, String filter){
        if(StringUtils.isEmpty(filter)) {
            filter = "^(businessId|businessName|approvalStatusType|approveStatus|approveStatusDesc|productId|updateBy|updateTime)$";
        }
        if(StringUtils.isEmpty(costExemption.getId())){
            costExemption.setCostExemptionCode(this.getNextFlowCode());
        }
        return persistence(costExemption,filter);
    }
    public CostExemptionItem updateCostExemptionItem(CostExemptionItem costExemptionItem, String filter){
        if(StringUtils.isEmpty(filter)) {
            filter = "^(updateBy|updateTime|costExemptionId|serviceFee|guaranteeFee|manageFee|overdueFee|prepaymentFee|prepaymentFeeRate|interest|serviceFeePre|guaranteeFeePre|manageFeePre|overdueFeePre|prepaymentFeePre|prepaymentFeeRatePre|interestPre|period|repayId|exemptionReason)$";
        }
        return persistence(costExemptionItem,filter);
    }
    private String getNextFlowCode(){
        Cnd cnd = Cnd.where("1","=","1");
        cnd.desc("costExemptionCode");
        CostExemption costExemption = dao().fetch(CostExemption.class,cnd);
        if(costExemption!=null){
            String flowCode = costExemption.getCostExemptionCode();
            String   maxCode =  flowCode.replaceAll("[A-Z]+[0]+","");
            maxCode =""+ (Integer.parseInt(maxCode)+1);
            return "FYMC000000".substring(0,10-maxCode.length())+maxCode;
        }else{
            return "FYMC000001";
        }
    }

    private boolean checkLoanInApproval(String loanId){
        CostExemption costExemption  =  dao().fetch(CostExemption.class,Cnd.where("loanId","=",loanId).and("approvalStatusType","in","IN_EDIT,IN_APPROVAL".split(",")));
        return costExemption!=null;
    }
    public NutMap getLoanByCode(String code)throws Exception{
        NutMap result = new NutMap();
        Loan loan  =  dao().fetch(Loan.class,Cnd.where("code","=",code).and("status","=",PublicStatus.ABLE).and("loanStatus","in","LOANED,OVERDUE".split(",")));
        if(loan==null){
            throw new KamsException("该订单不存在，或者不在还款中或者逾期状态");
        }
        if(productInfoTmplService.isBill(loan.getId())){
            throw new KamsException("该订单为票据产品，暂时不支持该产品做费用减免");
        }
        if(checkLoanInApproval(loan.getId())){
            throw new KamsException("该单正在审批或编辑中");
        }
        result.setv("id",loan.getId());
        result.setv("code",loan.getCode());
        result.setv("amount",loan.getActualAmount());
        result.setv("loanDate",loan.getLoanTime());
        result.setv("term",loan.termType2());
        LoanBorrower borrower = dao().fetch(LoanBorrower.class,loan.getMasterBorrowerId());
        result.setv("borrower",borrower.getName());
        ProductType productType = dao().fetch(ProductType.class,loan.getProductTypeId());
        result.setv("productType",productType.getName());
        result.setv("businessSource",getBusinessSource(loan));
        result.setv("dueDate",null);
        return result;
    }

    public String  getLoanByCostExemptionId(String id){
        CostExemption costExemption = fetch(id);
        return costExemption.getLoanId();
    }

    private String getBusinessSource(Loan loan) {
        Channel channel = null;
        if(StringUtils.isNotEmpty(loan.getChannelId())&&(channel=channelService.fetch(loan.getChannelId()))!=null&&ChannelType.QD.getCode().equals(channel.getChannelType())){
            return "渠道|"+channel.getName();
        }else{
            if(StringUtils.isNotEmpty(loan.getSaleId())){
                BusinessUser businessUser = businessUserService.fetchById(loan.getSaleId());
                if(businessUser!=null){
                    return "自营|"+TextFormatUtils.replaceNull(BusinessLine.getDescription(businessUser.getOrganize().getBusinessLine().name()),"")+"-"+businessUser.getOrganize().getCode()+"-"+businessUser.getName();
                }else{
                    return "自营|----";
                }
            }else{
                return "自营|----";
            }


        }
    }
    public DataTables costExemptionList(DataTableParam param){
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(),param.getLength());
        String businessName ;
        String code;
        String approvalStatusType = null;
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            businessName = keys.get("businessName");
            code = keys.get("code");
        } else {
            return null;
        }
        String sqlStr = "select sce.*,sl.code,sl.saleName from sl_cost_exemption sce " +
                " left join sl_loan sl on sl.id = sce.loanId" +
                " where 1=1 and sce.createrId = @createrId " ;
        String countSqlStr = "select count(DISTINCT sce.id) from sl_cost_exemption sce left join sl_loan sl on sl.id = sce.loanId where 1=1 and sce.createrId = @createrId ";
        if (StringUtils.isNotEmpty(businessName)) {
            sqlStr += " and sce.businessName like @businessName ";
            countSqlStr += " and sce.businessName like @businessName ";
        }
        if (StringUtils.isNotEmpty(code)) {
            sqlStr += " and sl.code like @code ";
            countSqlStr += " and sl.code like @code  ";
        }
        sqlStr += " order by sce.costExemptionCode desc ";
        Sql sql = Sqls.create(sqlStr);
        sql.setParam("businessName","%"+businessName+"%");
        sql.setParam("code","%"+code+"%");
        sql.setParam("createrId",ShiroSession.getLoginUser().getId());
        sql.setPager(pager);
        sql.setCallback(SqlUtil.getSqlCallback(CostExemption.class));
        sql.setEntity(dao().getEntity(CostExemption.class));
        dao().execute(sql);
        List<CostExemption> list = sql.getList(CostExemption.class);
        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("businessName","%"+businessName+"%");
        countSql.setParam("code","%"+code+"%");
        countSql.setParam("createrId",ShiroSession.getLoginUser().getId());
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        return new DataTables(param.getDraw(),count,count,list);
    }

    public List<CostExemptionItem> getCostExemptionItemList(String costExemptionId){
        return dao().query(CostExemptionItem.class,Cnd.where("costExemptionId","=",costExemptionId).asc("period"));
    }

    public boolean deleteCostExemptionListByCostExemptionId(String costExemptionId){
        int flag = dao().clear(CostExemptionItem.class,Cnd.where("costExemptionId","=",costExemptionId));
        return flag>0;
    }
    public CostExemption getCostExemptionById(String id){
        CostExemption costExemption = fetch(id);
        List<CostExemptionItem> costExemptionItems = getCostExemptionItemList(id);
        if(ApprovalStatusType.IN_EDIT.equals(costExemption.getApprovalStatusType())){
            for(CostExemptionItem costExemptionItem : costExemptionItems){
                Map  newLoanRepayFee = loanRepayService.fetchLoanRepayFeeByRepayId(costExemptionItem.getRepayId());
                costExemptionItem.setInterestPre(new BigDecimal(newLoanRepayFee.get("interest").toString()));
                costExemptionItem.setPrepaymentFeePre(new BigDecimal(newLoanRepayFee.get("prepaymentFee").toString()));
                costExemptionItem.setPrepaymentFeeRatePre(new BigDecimal(newLoanRepayFee.get("prepaymentFeeRate").toString()));
                costExemptionItem.setOverdueFeePre(new BigDecimal(newLoanRepayFee.get("overdueFee").toString()));
                costExemptionItem.setManageFeePre(new BigDecimal(newLoanRepayFee.get("manageFee").toString()));
                costExemptionItem.setGuaranteeFeePre(new BigDecimal(newLoanRepayFee.get("guaranteeFee").toString()));
                costExemptionItem.setServiceFeePre(new BigDecimal(newLoanRepayFee.get("serviceFee").toString()));
            }
        }
        costExemption.setCostExemptionItemList(costExemptionItems);
        return costExemption;
    }

    /**
     * 查询已完成审批列表
     * @param param
     * @return
     */
    public DataTables queryApprovalCompleteList(DataTableParam param) {
        String userId = ShiroSession.getLoginUser().getId();
        String approvalType ;
        String businessName;
        String code;
        String costExemptionCode;
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            approvalType =  keys.get("type");
            businessName  =  keys.get("businessName");
            code = keys.get("code");
            costExemptionCode = keys.get("costExemptionCode");
        } else {
            return new DataTables(param.getDraw(),0,0,new ArrayList());
        }

        String sqlStr = "SELECT  se.*,sl.code,sl.saleName   " +
                " FROM  sl_cost_exemption se  " +
                " left join sl_loan sl on sl.id = se.loanId "+
                " LEFT JOIN ( " +
                " SELECT " +
                "  substring( " +
                "   variable, " +
                "   POSITION( " +
                "    \"\\\"costExemptionId\\\":\" IN variable " +
                "   ) + 19, " +
                "   36 " +
                "  ) costExemptionId, " +
                "  display_Name " +
                " FROM " +
                "  wf_task " +
                ") wt ON se.id = wt.costExemptionId " +
                "WHERE " +
                " 1 = 1 " +
                "AND @userId IN ( " +
                " SELECT " +
                "  r.userId " +
                " FROM " +
                "  sl_approval_result r " +
                " WHERE " +
                "  r.loanId = se.id " +
                " AND flowConfigureType = 'COST_WAIVER' " +
                " AND r.approvalType = @approvalType " +
                ") " ;
        String countSqlStr = "SELECT "+
                " count(se.id) AS 'number' "+
                " FROM sl_cost_exemption se " +
                " left join sl_loan sl on sl.id = se.loanId "+
                " WHERE 1=1 "+
                " AND  @userId IN (SELECT r.userId FROM sl_approval_result r WHERE r.loanId = se.id AND r.flowConfigureType ='COST_WAIVER' AND r.approvalType =@approvalType)";

        if(StringUtils.isNotEmpty(businessName)){
            sqlStr += " AND se.businessName like @businessName";
            countSqlStr += " AND se.businessName like @businessName";
        }
        if(StringUtils.isNotEmpty(code)){
            sqlStr += " AND sl.code like @code";
            countSqlStr += " AND sl.code like @code";
        }
        if(StringUtils.isNotEmpty(costExemptionCode)){
            sqlStr += " AND se.costExemptionCode like @costExemptionCode";
            countSqlStr += " AND se.costExemptionCode like @costExemptionCode";
        }
        sqlStr += " order by se.updateTime desc";
        Sql sql = Sqls.create(sqlStr);
        sql.setParam("approvalType",approvalType);
        sql.setParam("userId",userId);
        sql.setParam("code",'%'+code+'%');
        sql.setParam("businessName",'%'+businessName+'%');
        sql.setParam("costExemptionCode",'%'+costExemptionCode+'%');
        sql.setPager(pager);
        sql.setCallback(SqlUtil.getSqlCallback(CostExemption.class));
        dao().execute(sql);
        List<CostExemption> list = sql.getList(CostExemption.class);
        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("approvalType",approvalType);
        countSql.setParam("userId",userId);
        countSql.setParam("code",'%'+code+'%');
        countSql.setParam("businessName",'%'+businessName+'%');
        countSql.setParam("costExemptionCode",'%'+costExemptionCode+'%');
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        for(CostExemption costExemption : list){
            //setChannelInfo(extension);
        }
        return new DataTables(param.getDraw(),count,count,list);
    }
    /**
     * 查询待审批列表
     * @param param
     * @return
     */
    public DataTables queryApprovalList(DataTableParam param) {
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String type;
        String businessName;
        String code;
        String costExemptionCode;
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            type =  keys.get("type");
            businessName = keys.get("businessName");
            code = keys.get("code");
            costExemptionCode = keys.get("costExemptionCode");
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
        List<String> costExemptionIds = new ArrayList<>();
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
            costExemptionIds = getCostExemptionIds(orderIds);
        }
        //获取到需要当前用户处理的列表
        if(null==costExemptionIds||costExemptionIds.size()==0){
            return new  DataTables(param.getDraw(),0,0,new ArrayList<>());
        }

        String ids  = "('"+StringUtils.join(costExemptionIds.toArray(),"','")+"')";
        String sqlStr = "SELECT se.* ,sl.code,sl.saleName   "+
                " FROM sl_cost_exemption se "+
                " left join sl_loan sl on sl.id = se.loanId "+
                " LEFT JOIN (select  substring(variable, POSITION(\"\\\"costExemptionId\\\":\" IN variable)+19, 36) costExemptionId,display_Name  from wf_task) wt on se.id=wt.costExemptionId" +
                " WHERE 1=1"+
                " AND se.approvalStatusType = 'IN_APPROVAL' "+
                " AND se.id IN "+ids.toString();
        String countSqlStr = "SELECT "+
                " COUNT(DISTINCT se.id) AS 'number' "+
                " FROM sl_cost_exemption se "+
                " left join sl_loan sl on sl.id = se.loanId "+
                " WHERE 1=1"+
                " AND se.approvalStatusType = 'IN_APPROVAL' "+
                " AND se.id IN "+ids.toString();

        if(StringUtils.isNotEmpty(businessName)){
            sqlStr += " AND se.businessName like @businessName";
            countSqlStr += " AND se.businessName like @businessName";
        }
        if(StringUtils.isNotEmpty(code)){
            sqlStr += " AND sl.code like @code";
            countSqlStr += " AND sl.code like @code";
        }
        if(StringUtils.isNotEmpty(costExemptionCode)){
            sqlStr += " AND se.costExemptionCode like @costExemptionCode";
            countSqlStr += " AND se.costExemptionCode like @costExemptionCode";
        }

        sqlStr += " order by se.updateTime desc";
        Sql sql = Sqls.create(sqlStr);
        sql.setParam("code",'%'+code+'%');
        sql.setParam("businessName",'%'+businessName+'%');
        sql.setParam("costExemptionCode",'%'+costExemptionCode+'%');
        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("code",'%'+code+'%');
        countSql.setParam("businessName",'%'+businessName+'%');
        countSql.setParam("costExemptionCode",'%'+costExemptionCode+'%');
        sql.setPager(pager);
        sql.setCallback(SqlUtil.getSqlCallback(CostExemption.class));
        dao().execute(sql);
        List<CostExemption> list = sql.getList(CostExemption.class);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        if(null==list){
            list = new ArrayList<>();
        }
        for(CostExemption costExemption : list){
            //setChannelInfo(extension);
        }
        return new DataTables(param.getDraw(),count,count,list);
    }
    /**
     * 获取costExemptionId列表
     * @param orderIds
     * @return
     */
    public List<String> getCostExemptionIds(List<String> orderIds) {
        Sql sql = Sqls.create("SELECT costExemptionId FROM sl_cost_exemption_order WHERE orderId in(@orderIds)");
        sql.setParam("orderIds", orderIds.toArray());
        sql.setCallback(Sqls.callback.strs());
        dao().execute(sql);
        return sql.getList(String.class);
    }

    @Aop(TransAop.READ_COMMITTED)
    public void startApprovalProcess(String id)throws Exception{
        CostExemption costExemption  = fetch(id);
        if(costExemption==null){
            throw new KamsException("提交订单失败:查询不到数据！");
        }
        FlowConfigureType flowType = FlowConfigureType.COST_WAIVER;
        ProductProcess productProcess = flowConfigureService.fetchEnableProductProductById(costExemption.getProductId(), flowType);
        if (productProcess == null) {
            throw new KamsException("提交订单失败:产品尚未配置流程！");
        }
        //查看当前产品的流程文件是否存在
        boolean snakerExist = flowService.existSnakerFile(costExemption.getProductId(), flowType);
        if (!snakerExist) {
            throw new KamsException("提交订单失败:流程引擎未部署或产品未启动！");
        }

        Order order = flowService.startInstanceWithCostExemption(costExemption, flowType);
        if (null == order) {
            costExemption.setApprovalStatusType(ApprovalStatusType.IN_EDIT);
            throw new KamsException("提交订单失败:初始化流程引擎失败！");
        } else {
            costExemption.setApprovalStatusType(ApprovalStatusType.IN_APPROVAL);
        }
        try{
            costExemption.updateOperator();
            updateCostExemption(costExemption,"^(approvalStatusType|productId|updateBy|updateTime)$");
        }catch (Exception e){
            throw  new KamsException("保存展期审批失败！");
        }
    }

    public boolean deleteCostExemptionItemByCostExemptionId(String costExemptionId){
        int flag = dao().clear(CostExemptionItem.class,Cnd.where("costExemptionId","=",costExemptionId));
        return flag>0;
    }
}

package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.*;
import com.kaisa.kams.components.utils.LoanCalculator;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by sunwanchao on 2016/12/12.
 */
@IocBean
@At("/loan")
public class LoanController {
    @Inject
    private LoanRepayService loanRepayService;

    @Inject
    private LoanService loanService;

    @Inject
    private LoanSubjectService loanSubjectService;

    @Inject
    private LoanRepayRecordService loanRepayRecordService;

    @Inject
    private ProductTypeService productTypeService;

    @Inject
    private ProductInfoTmplService productInfoTmplService;

    @Inject
    private BillLoanService billLoanService;

    @Inject
    private ProductService productService;


    @Inject
    protected BorrowerAccountService borrowerAccountService;

    @Inject
    protected LoanBorrowerService loanBorrowerService;

    @Inject
    protected BorrowerService borrowerService;




    @At
    @Ok("beetl:/loan/pending.html")
    @RequiresPermissions("pending:view")
    public Context pending() {
        Context ctx = Lang.context();
        return ctx;
    }

    @At
    @Ok("beetl:/loan/list.html")
    @RequiresPermissions("loan:view")
    public Context list() {
        Context ctx = Lang.context();
        ctx.set("businessLineList", BusinessLine.values());
        return ctx;
    }

    @At
    @Ok("beetl:/loan/confirm.html")
    public Context confirm(@Param("loanId") String loanId) {
        Context ctx = Lang.context();
        ctx.set("menus", ShiroSession.getMenu());
        ctx.set("loanId", loanId);
        return ctx;
    }
    @At
    @Ok("beetl:/loan/element.html")
    public Context element() {
        Context ctx = Lang.context();
        return ctx;
    }

    @At("/fetch_by_id")
    @Ok("json")
    public Object fetchById(@Param("loanId") String loanId) {
        return loanRepayService.fetchLoanById(loanId);
    }

    /**
     * 生成还款计划
     *
     * @return
     * @Param loanId
     */
    @At("/generate_loan_repay")
    @Ok("json")
    public Object generateLoanRepay(@Param("loanId") String loanId, @Param("loanTime") Date loanTime) {
        if (productInfoTmplService.isBill(loanId)) {
            return billLoanService.queryBillLoanRepayMap(loanId);
        }
        return loanRepayService.generateLoanRepay(loanService.fetchById(loanId), loanTime);
    }

    /**
     * 放款确认
     *
     * @param loanId
     * @param loanTime
     * @return
     */
    @POST
    @At
    @Ok("json")
    @Aop("auditInterceptor")
    public Object confirm(@Param("loanId") String loanId,
                          @Param("loanTime") Date loanTime,
                          @Param("subjectId") String subjectId,
                          @Param("subjectAccountId") String loanSubjectAccountId,
                          @Param("loanFeeInfo") String loanFeeInfo) {
        if (productInfoTmplService.isBill(loanId)) {
            return billLoanService.loan(loanId, subjectId, loanSubjectAccountId, loanTime);
        }
        return loanRepayService.loan(loanId, subjectId, loanSubjectAccountId, loanFeeInfo, loanTime);
    }

    /**
     * 查询费用
     *
     * @param loanId
     * @return
     */
    @At
    @Ok("json")
    public Object queryFeeByLoanId(@Param("loanId") String loanId) {
        return loanRepayService.queryLoanNodeFeeByLoanId(loanId, ShiroSession.getLoginUser());
    }

    /**
     * 查询放款列表
     *
     * @return
     */
    @At("/query_loan_list")
    @POST
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("loan:view")
    public Object queryLoanList(@Param("..")DataTableParam param) {
        return loanService.queryLoanList(param);
    }

    /**
     * 赎楼要件控制
     * @param param
     * @return
     */
    @At("/query_loan_element_list")
    @POST
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    public Object queryLoanElementList(@Param("..")DataTableParam param){
        return loanService.queryLoanElementList(param);
    }
    /**
     * 修改赎楼要件状态
     * @param
     * @return
     */
    @At("/update_loan_element_status")
    @Ok("json")
    @POST
    public Object updateLoanElementStatus(@Param("id") String id,@Param("elementStatus") String elementStatus){
        NutMap nutMap=new NutMap();
        Loan loan=loanService.fetchById(id);
        if(loan!=null){
            if("CONTROL".equals(elementStatus)){
                loan.setElementStatus(elementStatus);
            }else {
                loan.setElementStatus(elementStatus);
            }
            int num=loanService.updateLoanElementStatus(loan);
            if(num==1){
                return nutMap.setv("ok",true).setv("msg","修改成功.");
            }else {
                return nutMap.setv("ok",false).setv("msg","修改失败.");
            }
        }
        return nutMap.setv("ok",false).setv("msg","修改失败.");
    }

    @At("/query_gather_loan_list")
    @Ok("json")
    @POST
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("gather:view")
    public DataTables queryGatherLoanList(@Param("..")DataTableParam param) {
        return loanRepayService.queryLoanRepayList(param);
    }

    /**
     * 根据标的id查询放款主体及应收费用
     *
     * @param loanId
     * @return
     */
    @At("/fetch_loan_info")
    @GET
    @Ok("json")
    //@RequiresPermissions("loan:view")
    public Object fetchLoanInfoById(@Param("loanId") String loanId) {
        Loan loan = loanService.fetchById(loanId);
        NutMap nutMap = new NutMap();
        nutMap.setv("loan", loan);
        nutMap.setv("loanSubject", loanSubjectService.fetch(loan.getLoanSubjectId()));
        nutMap.setv("loanFeeList", loanRepayService.queryLoanNodeFeeByLoanId(loanId, ShiroSession.getLoginUser()));
        nutMap.setv("loanRepayList", this.queryLoanRepayList(loanId));
        nutMap.setv("loanRecordList", this.queryLoanRecordList(loanId));
//        nutMap.setv("canPreRepay",setPreconditionRepaymentAble(nutMap.get("loanRepayList"),loanId));
        return nutMap;
    }

    private boolean setPreconditionRepaymentAble(Object object,String loanId){
        if(object!=null) {
            if (productInfoTmplService.isBill(loanId)) {
              /*  billLoanService.queryBillLoanRepay(loanId);
                List<BillLoanRepay> list = (List<BillLoanRepay>) object;
                return checkBillPreconditionRepaymentAble(list);*/
                //票据不能提前还款
                return false;
            } else {
                List<LoanRepay> list = (List<LoanRepay>) object;
                return checkPreconditionRepaymentAble(list);
            }
        }else{
            return false;
        }

    }
    //非票据的提前还款能力
    private boolean checkPreconditionRepaymentAble(List<LoanRepay> list){
        boolean isOk = false;
        if(CollectionUtils.isNotEmpty(list)){
            isOk = LoanRepayStatus.LOANED.equals( ((LoanRepay)list.get(list.size()-1)).getStatus() )?true:false;
            if(isOk){
                for(LoanRepay loanRepay : list){
                    if(LoanRepayStatus.OVERDUE.equals(loanRepay.getStatus())){
                        isOk = false;
                        break ;
                    }
                };
            }
        }
        return isOk;
    }
    //票据的提前还款能力
    private boolean checkBillPreconditionRepaymentAble(List<BillLoanRepay> list){
        boolean isOk = false;
        if(CollectionUtils.isNotEmpty(list)){
            isOk = LoanRepayStatus.LOANED.equals( ((BillLoanRepay)list.get(list.size()-1)).getLoanRepay().getStatus() )?true:false;
            if(isOk){
                for(BillLoanRepay billLoanRepay : list){
                    if(LoanRepayStatus.OVERDUE.equals(billLoanRepay.getLoanRepay().getStatus())){
                        isOk = false;
                        break ;
                    }
                };
            }
        }
        return isOk;
    }

    @At("/can_pre_repayment_list")
    @GET
    public Object getPreRepaymentList(@Param("loanId") String loanId){
        if (productInfoTmplService.isBill(loanId)) {
            return billLoanService.queryCanPreRepayList(loanId);
        } else {
             return loanRepayService.queryCanPreRepayList(loanId);
        }
    }

    @At("/pre_repayment")
    @POST
    public NutMap preRepayment(@Param("loanId") String loanId){
        NutMap nutMap = new NutMap();
        Object object = this.queryLoanRepayList(loanId);
        if (productInfoTmplService.isBill(loanId)) {
            nutMap.setv("ok",false);
            nutMap.setv("msg", "票据业务单不能提前还款");
        } else {
            //先校验是否可调用接口
            if(object!=null&&checkPreconditionRepaymentAble((List<LoanRepay>)object)){
                try {
                    nutMap.setv("ok", loanRepayService.preRepayment(loanId));
                    nutMap.setv("msg","提前还款成功");
                } catch (Exception e) {
                    nutMap.setv("ok",false);
                    nutMap.setv("msg","提前还款失败");
                }
            }else{
                nutMap.setv("ok",false);
                nutMap.setv("msg", "该业务单不能提前还款");
            }
        }
        return nutMap;
    }

    private Object queryLoanRepayList(String loanId) {
        Object loanRepayList;
        if (productInfoTmplService.isBill(loanId)) {
            loanRepayList = billLoanService.queryBillLoanRepay(loanId);
        } else {
            loanRepayList = loanRepayService.queryLoanRepayByLoanId(loanId);
        }
        return loanRepayList;
    }

    private Object queryLoanRecordList(String loanId) {
        Object loanRecordList;
        loanRecordList = loanRepayService.queryLoanRecordByLoanId(loanId);
        return loanRecordList;
    }

    @At("/query_loan_repay")
    @Ok("json")
    @RequiresPermissions("loan:view")
    public Object queryLoanRepay(@Param("loanId") String loanId) {
        Loan loan = loanService.fetchById(loanId);
        NutMap nutMap = new NutMap();
        nutMap.setv("loan", loan);
        nutMap.setv("loanSubject", loanSubjectService.fetch(loan.getLoanSubjectId()));
        nutMap.setv("loanFeeList", loanRepayService.queryLoanNodeFeeByLoanId(loanId, ShiroSession.getLoginUser()));
        List<LoanRepay> loanRepayList = loanRepayService.queryLoanRepayByLoanId(loanId);
        int period = 1;
        for (LoanRepay lr : loanRepayList) {
            LoanRepayStatus status = lr.getStatus();
            if (LoanRepayStatus.CLEARED.equals(status) || LoanRepayStatus.OVERDUE_CLEARED.equals(status) || LoanRepayStatus.AHEAD_CLEARED.equals(status)) {
                period++;
            }
        }
        Object repayList = loanRepayList;
        if (productInfoTmplService.isBill(loanId)) {
            repayList = billLoanService.queryBillLoanRepay(loanId);
        }
        nutMap.setv("period", period);
        nutMap.setv("loanRepayList", repayList);
        nutMap.setv("canPreRepay",setPreconditionRepaymentAble(nutMap.get("loanRepayList"),loanId));
        return nutMap;
    }

    @At
    @Ok("beetl:/loan/gather.html")
    @RequiresPermissions("gather:view")
    public void gather() {}

    @At("/fetch_repay_record_by_id")
    @Ok("json")
    @RequiresPermissions("loan:view")
    public Object fetchRepayRecordById(@Param("id") String id) {
        return loanRepayRecordService.fetchById(id);
    }

    @At("/query_repay_record_by_id")
    @Ok("json")
    @RequiresPermissions("loan:view")
    public Object queryRepayRecordById(@Param("repayId") String repayId) {
        return loanRepayRecordService.queryByRepayId(repayId);
    }

    /**
     * 新增还款记录
     *
     * @param repayId
     * @param repayDate
     * @param repayAmount
     * @param repayInterest
     * @param repayFeeInfo
     * @return
     */
    @At("/insert_repay_record")
    @Ok("json")
    @Aop("auditInterceptor")
    @RequiresPermissions("loan:create")
    public Object insertRepayRecord(@Param("repayId") String repayId,
                                    @Param("repayDate") Date repayDate,
                                    @Param("repayAmount") BigDecimal repayAmount,
                                    @Param("repayInterest") BigDecimal repayInterest,
                                    @Param("repayFeeInfo") String repayFeeInfo,
                                    @Param("remark") String remark) {
        return loanRepayService.insertRepayRecord(repayId, repayDate, repayAmount, repayInterest, repayFeeInfo,remark, ShiroSession.getLoginUser());
    }

    /**
     * 删除还款记录
     *
     * @param id
     * @return
     */
    @At("/delete_repay_record_by_id")
    @Ok("json")
    @Aop("auditInterceptor")
    @RequiresPermissions("loan:delete")
    public Object deleteRepayRecordById(@Param("id") String id) {
        return loanRepayService.deleteByRecordId(id);
    }

    /**
     * 足额还款
     */
    @At("/full_repay")
    @Ok("json")
    public Object fullRepay(@Param("repayId") String repayId) {
        return loanRepayService.fullRepay(repayId);
    }

    /**
     * 本期还款确认
     */
    @POST
    @At("/clear_loan_repay")
    @Ok("json")
    @Aop("auditInterceptor")
    public NutMap clearLoanRepay(@Param("loanId") String loanId,
                                 @Param("repayId") String repayId,
                                 @Param("remark") String remark,
                                 @Param("clear") Boolean clear) {
        NutMap result = new NutMap();
        if (productInfoTmplService.isBill(loanId)) {
            boolean flag = false;
            flag = billLoanService.clearLoanRepay(repayId);
            if(flag){
                Loan loan = loanService.fetchById(loanId);
                Product product = productService.fetchEnableProductById(loan.getProductId());
                if(null != product && product.getName().equals("商业承兑汇票")) {
                    billLoanService.changeAmountForClear(repayId,loan);
                }
            }
            result.setv("ok",flag);
        }
        String msg = loanRepayService.checkClearLoanRepay(repayId);
        if(StringUtils.isEmpty(msg)||clear==true){
            result.setv("ok",loanRepayService.clearLoanRepay(repayId,remark));
        }else {
            result.setv("status",msg.contains("不允许结清")?"ALERT":"PROMPT");
            result.setv("ok",false);
            result.setv("msg",msg);
        }
        return result;
    }

    @At("/query_repay_record")
    @Ok("json")
    public Object queryRepayRecord(@Param("loanId") String loanId,
                                   @Param("repayId") String repayId) {
        if(productInfoTmplService.isBill(loanId)){
            return billLoanService.queryBillLoanRepayById(repayId);
        }
        return loanRepayService.queryRepayRecord(repayId);
    }

    @At("/calc_fee")
    @Ok("json")
    public Object calcFee(@Param("repayId") String repayId, @Param("repayDate") Date repayDate) {
        return loanRepayService.calcFee(repayId, repayDate);
    }

    /**
     * @param amount
     * @param loanTermType
     * @param term
     * @param repayMethod
     * @param loanLimitType
     * @param interestRate
     * @param interestAmount
     * @param repayDateType
     * @return
     */
    @POST
    @At("/calculate")
    @Ok("json")
    public Object calculate(@Param("amount") BigDecimal amount,
                             @Param("loanTermType")LoanTermType loanTermType,
                             @Param("term")String term,
                             @Param("repayMethod")LoanRepayMethod repayMethod,
                             @Param("loanLimitType")LoanLimitType loanLimitType,
                             @Param("interestRate")BigDecimal interestRate,
                             @Param("interestAmount")BigDecimal interestAmount,
                             @Param("repayDateType")LoanRepayDateType repayDateType,
                             @Param("minInterestAmount")BigDecimal minInterestAmount,
                             @Param("calculateMethodAboutDay")CalculateMethodAboutDay calculateMethodAboutDay){
        return LoanCalculator.calcuate(amount,loanTermType,term,repayMethod,loanLimitType,LoanLimitType.FIX_AMOUNT.equals(loanLimitType)?interestAmount:interestRate,repayDateType,new Date(),minInterestAmount,calculateMethodAboutDay);
    }


    @At("/calculate")
    @Ok("json")
    public Object calculate(@Param("loanId") String loanId) {
        Loan loan = loanRepayService.fetchLoanById(loanId);
        if (loan == null) {
            return null;
        }
        return LoanCalculator.calcuate(loan.getAmount(),loan.getTermType(),loan.getTerm(),loan.getRepayMethod(),loan.getLoanLimitType(),LoanLimitType.FIX_AMOUNT.equals(loan.getLoanLimitType())?loan.getInterestAmount():loan.getInterestRate(),loan.getRepayDateType(),loan.getLoanTime()==null?new Date():loan.getLoanTime(),loan.getMinInterestAmount(),loan.getCalculateMethodAboutDay());
    }

    @POST
    @At("/cancel")
    @Ok("json")
    public Object cancel(@Param("loanId") String loanId){
        return loanService.cancel(loanId);
    }

    /**
     * 放款信息弹窗
     * @param loanId
     * @return
     */
    @POST
    @At("/init_loan_record")
    @Ok("json")
    public Object initLoanRecord(@Param("loanId") String loanId){
        NutMap result = new NutMap();
        Loan loan = loanService.fetchById(loanId);
        List<BorrowerAccount> borrowerAccountList = borrowerAccountService.queryFormatAccountsByLoanId(loanId);
        if (productInfoTmplService.isBill(loanId)) {
            LoanBorrower loanBorrower = loanBorrowerService.fetchById(loan.getMasterBorrowerId());
            Borrower borrower = borrowerService.fetchById1(loanBorrower.getBorrowerId());
            result.put("borrower",borrower);
        }
        ProductInfoTmpl tmpl=productInfoTmplService.fetchByProductId(loan.getProductId());
        for(BorrowerAccount borrowerAccount:borrowerAccountList){
            borrowerAccount.setAccount(loanRepayService.getAccount(borrowerAccount,tmpl.getProductTempType()));
        }
        result.put("borrowerAccountList",borrowerAccountList);
        return result;
    }

}

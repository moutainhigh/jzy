package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.service.flow.FlowService;
import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.*;
import com.kaisa.kams.enums.FlowConfigureType;
import com.kaisa.kams.models.*;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by lw on 2017/8/30.
 */
@IocBean
@At("/bill_intermediary_apply")
public class IntermediaryApplyController {

    @Inject
    private BillLoanService billLoanService;

    @Inject
    private IntermediaryApplyService intermediaryApplyService;

    @Inject
    private UserService userService;

    @Inject
    private FlowService flowService;

    @Inject
    private LoanOrderService loanOrderService;

    @Inject
    private BorrowerService borrowerService;

    @Inject
    private LoanRepayService loanRepayService;

    @Inject
    private LoanService loanService;

    @Inject
    private LoanSubjectService loanSubjectService;


    /**
     * 跳转待申请列表
     */
    @At("/pending_application_list")
    @GET
    @Ok("beetl:/specialApply/intermediaryFee/list_ apply.html")
    @RequiresUser
    @RequiresPermissions("intermediary_apply:view")
    public Context toPendingList() {
        Context ctx = Lang.context();
        return ctx;
    }


    /**
     * 查询待申请列表
     */
    @At("/query_pending_list")
    @POST
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    public Object queryPendingList(@Param("..")DataTableParam param) {
        return intermediaryApplyService.intermediaryApplyList(param);
    }

    /**
     * 业务跳转待审批、审批列表
     */
    @At("/process_business_list")
    @GET
    @Ok("beetl:/specialApply/intermediaryFee/list_approval_business.html")
    @RequiresUser
    @RequiresPermissions("intermediary_approval_business:view")
    public Context toProcessBusinessList() {
        Context ctx = Lang.context();
        return ctx;
    }
    /**
     * 风控跳转待审批、审批列表
     */
    @At("/process_risk_list")
    @GET
    @Ok("beetl:/specialApply/intermediaryFee/list_approval_risk.html")
    @RequiresUser
    @RequiresPermissions("intermediary_approval_risk:view")
    public Context toProcessRiskList() {
        Context ctx = Lang.context();
        return ctx;
    }
    /**
     * 财务跳转待审批、审批列表
     */
    @At("/process_finance_list")
    @GET
    @Ok("beetl:/specialApply/intermediaryFee/list_approval_finance.html")
    @RequiresUser
    @RequiresPermissions("intermediary_approval_finance:view")
    public Context toProcessFinanceList() {
        Context ctx = Lang.context();
        return ctx;
    }


    /**
     * 查询待审批列表
     */
    @At("/query_process_list")
    @POST
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    public Object queryProcessList(@Param("..")DataTableParam param) {
        return intermediaryApplyService.queryApprovalList(param);
    }

    /**
     * 查询已审批列表
     */
    @At("/query_process_approved_list")
    @POST
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    public Object queryApprovedList(@Param("..")DataTableParam param) {
        return intermediaryApplyService.queryApprovalCompleteList(param);
    }

    /**
     * 跳转待放款、放款列表
     */
    @At("/loan_list")
    @GET
    @Ok("beetl:/specialApply/intermediaryFee/list_payment.html")
    @RequiresPermissions("intermediary_loan:view")
    public Context toLoanList() {
        Context ctx = Lang.context();
        return ctx;
    }


    /**
     * 查询待放款、放款列表
     */
    @At("/query_loan_list")
    @POST
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    public Object queryLoanList(@Param("..")DataTableParam param) {
        return intermediaryApplyService.queryLoanList(param);
    }

    /**
     * 补录
     */
    @At("/makeup_intermediary")
    @POST
    @Ok("json")
    @RequiresPermissions("intermediary_apply:update")
    public NutMap makeup(@Param("intermediaryApplyId") String intermediaryApplyId,@Param("..")Intermediary intermediary) {

        NutMap result = new NutMap();
        if (null == intermediary) {
            result.put("ok", false);
            result.put("msg", "补录信息错误");
            return result;
        }
        Boolean flag = updateApply(intermediaryApplyId,intermediary);
        if (flag) {
            result.put("ok", true);
            result.put("msg", "补录成功");
        } else {
            result.put("ok", false);
            result.put("msg", "补录失败");
        }
        return result;
    }

    public boolean updateApply(String intermediaryApplyId,Intermediary intermediary) {
        boolean flag = false;
        if (null == intermediary) {
            return flag;
        }
        IntermediaryApply intermediaryApply = intermediaryApplyService.fetchById(intermediaryApplyId);
        intermediaryApply.setName(intermediary.getName());
        intermediaryApply.setIdNumber(intermediary.getIdNumber());
        intermediaryApply.setAccount(intermediary.getAccount());
        intermediaryApply.setPhone(intermediary.getPhone());
        intermediaryApply.setAddress(intermediary.getAddress());
        intermediaryApply.setBank(intermediary.getBank());
        intermediaryApply.setServiceContractFileUrls(intermediary.getServiceContractFileUrls());
        flag = intermediaryApplyService.update(intermediaryApply);
        return flag;
    }

    /**
     * 补录弹窗
     */
    @At("/makeup_intermediary_init")
    @POST
    @Ok("json")
    @RequiresPermissions("intermediary_apply:update")
    public NutMap makeupInit(@Param("loanId") String loanId) {

        NutMap result = new NutMap();
        if (StringUtils.isEmpty(loanId)) {
            result.put("ok", false);
            result.put("msg", "补录信息错误");
            return result;
        }
        BillLoan billLoan = billLoanService.fetchBillLoanByLoanId(loanId);
        Intermediary resultIntermediary = billLoanService.getIntermediary(billLoan);
        IntermediaryApply intermediaryApply = intermediaryApplyService.fetchByLoanId(loanId);
        intermediaryApply.setIntermediaryFee(intermediaryApply.getIntermediaryFee().setScale(2, BigDecimal.ROUND_HALF_UP));
        result.put("intermediaryApply",intermediaryApply);
        result.put("intermediary",resultIntermediary);
        result.put("billLoan",billLoan);
        return result;
    }

    /**
     * 提交
     */
    @At("/submit_intermediary")
    @POST
    @Ok("json")
    @RequiresPermissions("intermediary_apply:update")
    public NutMap submitIntermediary(@Param("loanId") String loanId,@Param("intermediaryApplyId") String intermediaryApplyId,@Param("..")Intermediary intermediary) {

        NutMap result = new NutMap();
        if (StringUtils.isEmpty(loanId) || StringUtils.isEmpty(intermediaryApplyId) || null == intermediary) {
            result.put("ok", false);
            result.put("msg", "提交信息错误");
            return result;
        }
        //保存
        Boolean flag = updateApply(intermediaryApplyId,intermediary);
        if (flag) {
            result.put("ok", true);
            result.put("msg", "补录成功");
        } else {
            result.put("ok", false);
            result.put("msg", "补录失败");
        }
        //提交
        result = loanRepayService.submitApply(loanId,intermediaryApplyId, FlowConfigureType.BROKERAGE_FEE);
        if (null != result && ("false").equals(result.get("ok").toString())) {
            return result;
        }
        return result;
    }


    /**
     * 查看
     */
    @At("/detail")
    @GET
    @Ok("beetl:/specialApply/detail.html")
    public Context view() {
        Context ctx = Lang.context();
        return ctx;
    }


    /**
     * 审批
     */
    @At("/approval")
    @GET
    @Ok("beetl:/specialApply/approvalForm.html")
    public Context approval() {
        Context ctx = Lang.context();
        return ctx;
    }

    /**
     * 查看
     */
    @At("/query_bill")
    @Ok("json")
    public NutMap queryBillLoan(@Param("loanId") String loanId,@Param("intermediaryApplyId") String intermediaryApplyId) {
        return billLoanService.queryBillLoanInfoForIntermediaryApply(loanId,intermediaryApplyId);
    }

    /**
     * 放款信息弹窗
     * @param loanId
     * @return
     */
    @POST
    @At("/init_loan")
    @Ok("json")
    public NutMap initLoan(@Param("loanId") String loanId){
        NutMap result = new NutMap();
        Loan loan = loanService.fetchById(loanId);
        BillLoan billLoan = billLoanService.fetchBillLoanByLoanId(loanId);
        LoanSubject loanSubject = loanSubjectService.fetch(loan.getLoanSubjectId());
        Intermediary intermediary = billLoanService.getIntermediary(billLoan);
        IntermediaryApply intermediaryApply = intermediaryApplyService.fetchByLoanId(loanId);
        result.put("intermediaryFee",billLoan.getAfterTaxIntermediaryFee());
        result.put("intermediary",intermediary);
        result.put("intermediaryApply",intermediaryApply);
        result.put("loanSubject",loanSubject.getName());
        return result;
    }

    /**
     * 放款
     * @param loanId
     * @return
     */
    @POST
    @At("/confirm_loan")
    @Ok("json")
    public NutMap confirmLoan(@Param("loanId") String loanId){
        NutMap result = new NutMap();
        result = intermediaryApplyService.loan(loanId,ShiroSession.getLoginUser());
        return result;
    }

    @At("/document_download")
    @Ok("void")
    public void documentDownload(@Param("loanId")String loanId , HttpServletResponse response  ) {
        try{
            intermediaryApplyService.documentDownload(loanId,response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

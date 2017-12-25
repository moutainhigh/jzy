package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.service.ExtensionService;
import com.kaisa.kams.components.service.LoanSubjectService;
import com.kaisa.kams.components.utils.KamsException;
import com.kaisa.kams.enums.LoanLimitType;
import com.kaisa.kams.enums.LoanTermType;
import com.kaisa.kams.models.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by zhouchuang on 2017/10/31.
 */
@IocBean
@At("/extension")
public class ExtensionController {

    @Inject
    private ExtensionService extensionService;
    @Inject
    private LoanSubjectService loanSubjectService;


    @POST
    @At("/submit_approval")
    @Ok("json")
    @RequiresPermissions("business_extension:view")
    public Object startApprovalProcess(@Param("loanId") String loanId,
                         @Param("termType") LoanTermType termType,
                         @Param("term") String term,
                         @Param("loanLimitType") LoanLimitType loanLimitType,
                         @Param("interest") BigDecimal interest,
                         @Param("repayMethod") String repayMethod,@Param("enterpriseExplain") String enterpriseExplain,
                         @Param("enterpriseAgreement") String enterpriseAgreement,@Param("calculationMethod") String calculationMethod
            ,@Param("repayDateType") String repayDateType){
        NutMap result = new NutMap();
        try {
            extensionService.startApprovalProcess(loanId, termType, term, loanLimitType, interest, repayMethod, enterpriseExplain, enterpriseAgreement, calculationMethod, repayDateType);
        }catch (KamsException e){
            result.setv("ok",false);
            result.setv("msg",e.getMessage());
            return  result;
        }catch (Exception e){
            result.setv("ok",false);
            result.setv("msg","展期审批提交异常");
            return  result;
        }
        result.setv("ok",true);
        result.setv("msg","展期审批提交成功！");
        return result;
    }

    @POST
    @At("/get")
    @Ok("json")
    public NutMap get( @Param("id")String id){
        NutMap result  = extensionService.getExtensionById(id);
        return result;
    }


    /**
     * 跳转业务展期审批页面列表
     * @return
     */
    @At("/list_approval_business")
    @RequiresPermissions("extensionBusinessApproval:view")
    @Ok("beetl:/specialApply/extension/list_approval_business.html")
    public Context listApprovalBusiness(){
        Context ctx = Lang.context();  //返回的Map
        List<LoanSubject> list =  loanSubjectService.queryAble();
        ctx.set("loanSubjectList",list);
        return ctx;
    }
    /**
     * 跳转风控展期审批页面列表
     * @return
     */
    @At("/list_approval_risk")
    @RequiresPermissions("extensionRiskApproval:view")
    @Ok("beetl:/specialApply/extension/list_approval_risk.html")
    public Context listApprovalRisk(){
        Context ctx = Lang.context();  //返回的Map
        List<LoanSubject> list =  loanSubjectService.queryAble();
        ctx.set("loanSubjectList",list);
        return ctx;
    }

    /**
     * 跳转高管展期审批页面列表
     * @return
     */
    @At("/list_approval_senior")
    @RequiresPermissions("extensionSeniorApproval:view")
    @Ok("beetl:/specialApply/extension/list_approval_senior.html")
    public Context listApprovalenior(){
        Context ctx = Lang.context();  //返回的Map
        List<LoanSubject> list =  loanSubjectService.queryAble();
        ctx.set("loanSubjectList",list);
        return ctx;
    }

    /**
     * 公共的审批页面
     * @return
     */
    @At("/approval_page")
    @Ok("beetl:/specialApply/approvalForm.html")
    public Context approvalPage(){
        Context ctx = Lang.context();  //返回的Map
        List<LoanSubject> list =  loanSubjectService.queryAble();
        ctx.set("loanSubjectList",list);
        return ctx;
    }
    /**
     * 展期查看
     * @return
     */
    @At("/view")
    @GET
    @Ok("beetl:/specialApply/detail.html")
    public Context view(@Param("id")String id){
        Context ctx = Lang.context();
        List<LoanSubject> list =  loanSubjectService.queryAble();
        ctx.set("loanSubjectList",list);
        return ctx;
    }


    /**
     * 跳转财务展期审批页面列表
     * @return
     */
    @At("/list_approval_finance")
    @RequiresPermissions("extensionFinanceApproval:view")
    @Ok("beetl:/specialApply/extension/list_approval_finance.html")
    public Context listApprovalFinance(){
        Context ctx = Lang.context();  //返回的Map
        List<LoanSubject> list =  loanSubjectService.queryAble();
        ctx.set("loanSubjectList",list);
        return ctx;
    }


    /**
     * 查询展期列表
     * @param param
     * @return
     */
    @At("/approval_list")
    @Ok("json")
    @POST
    @AdaptBy(type=JsonAdaptor.class)
    public DataTables approvalList(@Param("..")DataTableParam param){
        return extensionService.queryApprovalList(param);
    }

    /**
     * 查询展期列表已完成列表
     * @param param
     * @return
     */
    @At("/approval_complete_list")
    @Ok("json")
    @POST
    @AdaptBy(type=JsonAdaptor.class)
    public DataTables queryApprovalCompleteList(@Param("..")DataTableParam param){
        return extensionService.queryApprovalCompleteList(param);
    }
}

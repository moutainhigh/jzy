package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.service.LoanSubjectService;
import com.kaisa.kams.components.service.MortgageService;
import com.kaisa.kams.enums.HouseMortgageType;
import com.kaisa.kams.enums.MortgageDocumentType;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.LoanSubject;
import com.kaisa.kams.models.Mortgage;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by zhouchuang on 2017/9/15.
 */
@IocBean
@At("/mortgage")
public class MortgageController {

    @Inject
    private MortgageService mortgageService;
    @Inject
    private LoanSubjectService loanSubjectService;

    /**
     * 跳转抵押页面
     */
    @At
    @RequiresPermissions("mortgage:view")
    @Ok("beetl:/specialApply/mortgage/list_apply.html")
    public Context index() {
        Context ctx = Lang.context();
        List<LoanSubject> list = loanSubjectService.queryAble();
        ctx.set("loanSubjectList", list);
        return ctx;
    }

    /**
     * 跳转抵押业务审批页面列表
     */
    @At("/list_approval_business")
    @RequiresPermissions("mortgageBusinessInfoApproval:view")
    @Ok("beetl:/specialApply/mortgage/list_approval_business.html")
    public Context listApprovalBusiness() {
        Context ctx = Lang.context();
        List<LoanSubject> list = loanSubjectService.queryAble();
        ctx.set("loanSubjectList", list);
        return ctx;
    }

    /**
     * 跳转抵押风控审批页面列表
     */
    @At("/list_approval_risk")
    @RequiresPermissions("mortgageBusinessRiskApproval:view")
    @Ok("beetl:/specialApply/mortgage/list_approval_risk.html")
    public Context listApprovalRisk() {
        Context ctx = Lang.context();
        List<LoanSubject> list = loanSubjectService.queryAble();
        ctx.set("loanSubjectList", list);
        return ctx;
    }

    /**
     * 跳转抵押业务审批页面列表
     */
    @At("/list_approval_finance")
    @RequiresPermissions("mortgageBusinessFinanceApproval:view")
    @Ok("beetl:/specialApply/mortgage/list_approval_finance.html")
    public Context listApprovalFinance() {
        Context ctx = Lang.context();
        List<LoanSubject> list = loanSubjectService.queryAble();
        ctx.set("loanSubjectList", list);
        return ctx;
    }

    /**
     * 公共的审批页面
     */
    @At("/approval_page")
    @Ok("beetl:/specialApply/approvalForm.html")
    public Context approvalPage() {
        Context ctx = Lang.context();
        List<LoanSubject> list = loanSubjectService.queryAble();
        ctx.set("loanSubjectList", list);
        return ctx;
    }


    /**
     * 查询抵押列表
     */
    @At("/approval_list")
    @Ok("json")
    @POST
    @AdaptBy(type = JsonAdaptor.class)
    public DataTables approvalList(@Param("..") DataTableParam param) {
        return mortgageService.queryApprovalList(param);
    }

    /**
     * 查询抵押列表已完成列表
     */
    @At("/approval_complete_list")
    @Ok("json")
    @POST
    @AdaptBy(type = JsonAdaptor.class)
    public DataTables queryApprovalCompleteList(@Param("..") DataTableParam param) {
        return mortgageService.queryApprovalCompleteList(param);
    }


    /**
     * 查询抵押列表
     */
    @At("/list")
    @Ok("json")
    @POST
    @AdaptBy(type = JsonAdaptor.class)
    @RequiresPermissions("mortgage:view")
    public DataTables list(@Param("..") DataTableParam param) {
        return mortgageService.mortgageList(param);
    }

    @At("/update")
    @POST
    @Ok("json")
    @AdaptBy(type = JsonAdaptor.class)
    @RequiresPermissions("mortgage:view")
    public NutMap update(@Param("..") Mortgage mortgage) {
        NutMap result = new NutMap();
        Mortgage saveMortgage = mortgageService.update(mortgage, result);
        return result;
    }

    //提交抵押审批
    @At("/submit_approval")
    @POST
    @Ok("json")
    @RequiresPermissions("mortgage:view")
    public NutMap submitApproval(@Param("id") String id) {
        NutMap result = new NutMap();
        Mortgage mortgage = mortgageService.getSimpleMortgageById(id);
        if (mortgage == null) {
            result.setv("ok", false);
            result.setv("msg", "找不到对应的订单");
        } else {
            result = mortgageService.startApprovalProcess(mortgage, result);
        }

        return result;
    }

    //取消抵押单，让其不可显示
    @At("/cancel")
    @POST
    @Ok("json")
    @RequiresPermissions("mortgage:view")
    public NutMap cancel(@Param("id") String id) {
        NutMap result = new NutMap();
        int i = mortgageService.cancelMortgageById(id);
        if (i > 0) {
            result.setv("ok", true);
            result.setv("msg", "取消成功");
        } else {
            result.setv("ok", false);
            result.setv("msg", "取消失败");
        }
        return result;
    }

    /**
     * 抵押编辑
     */
    @At("/edit")
    @GET
    @Ok("beetl:/specialApply/mortgage/edit.html")
    @RequiresPermissions("mortgage:view")
    public Context edit(@Param("id") String id) {
        Context ctx = Lang.context();
        List<LoanSubject> list = loanSubjectService.queryAble();
        ctx.set("loanSubjectList", list);
        return ctx;
    }


    @At("/get")
    @POST
    @Ok("json")
    public Mortgage get(@Param("id") String id) {
        Mortgage mortgage = mortgageService.getMortgageById(id);
        return mortgage;
    }

    /**
     * 抵押查看
     */
    @At("/view")
    @GET
    @Ok("beetl:/specialApply/detail.html")
    public Context view() {
        Context ctx = Lang.context();
        List<LoanSubject> list = loanSubjectService.queryAble();
        ctx.set("loanSubjectList", list);
        return ctx;
    }

    @At("/mortgage_document_list")
    @Ok("json")
    public List getMortgageDocumnetList(@Param("houseMortgageType") HouseMortgageType houseMortgageType) {
        List list = MortgageDocumentType.mortgageDocumentlist(houseMortgageType);
        return list;
    }


    @At("/document_download")
    @Ok("void")
    public void documentDownload(@Param("mortgageId") String mortgageId, @Param("houseId") String houseId, @Param("mortgageDocumentType") MortgageDocumentType mortgageDocumentType, HttpServletResponse response) {
        try {
            mortgageService.documentDownload(mortgageId, houseId, mortgageDocumentType, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @At("/approval_download")
    @Ok("void")
    public void approvalDownload(@Param("id") String id, HttpServletResponse response) {
        try {
            mortgageService.approvalDownload(id, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

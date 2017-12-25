package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.service.BillLoanService;
import com.kaisa.kams.components.service.CostExemptionService;
import com.kaisa.kams.components.service.LoanRepayService;
import com.kaisa.kams.components.service.ProductInfoTmplService;
import com.kaisa.kams.components.utils.KamsException;
import com.kaisa.kams.models.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @description：费用免除控制层
 * @author：zhouchuang
 * @date：2017-11-14:48
 */
@IocBean
@At("/cost_exemption")
public class CostExemptionController {
    @Inject
    private CostExemptionService costExemptionService;
    @Inject
    private LoanRepayService loanRepayService;
    @Inject
    private ProductInfoTmplService productInfoTmplService;
    @Inject
    private BillLoanService billLoanService;

    /**
     * 跳转费用减免页面
     * @return
     */
    @At
    @RequiresPermissions("costExemption:view")
    @Ok("beetl:/specialApply/costExemption/list_apply.html")
    public void index(){
    }

    /**
     * 查询及费减免列表
     * @param param
     * @return
     */
    @At("/list")
    @Ok("json")
    @POST
    @AdaptBy(type=JsonAdaptor.class)
    public DataTables list(@Param("..")DataTableParam param){
        return costExemptionService.costExemptionList(param);
    }

    @At("/getLoan")
    @Ok("json")
    @POST
    public NutMap getLoan(@Param("code")String code){
        try {
            NutMap result =  costExemptionService.getLoanByCode(code);
            return result;
        }catch(KamsException e){
            NutMap result = new NutMap();
            result.setv("ok",false);
            result.setv("msg",e.getMessage());
            return result;
        }catch (Exception e){
            NutMap result = new NutMap();
            result.setv("ok",false);
            result.setv("msg","关联失败，数据异常");
            return result;
        }
    }

    @POST
    @At("/get")
    @Ok("json")
    public CostExemption get(@Param("id")String id){
        CostExemption costExemption  = costExemptionService.getCostExemptionById(id);
        return costExemption;
    }

    @At("/delete")
    @POST
    @Ok("json")
    public NutMap delete(@Param("id")String id){
        NutMap result = new NutMap();
        try {
            costExemptionService.cancelByCostExemptionId(id);
        }catch (Exception e){
            result.setv("ok",false);
            result.setv("msg","费用免除取消失败");
            return  result;
        }
        result.setv("ok",true);
        result.setv("msg","费用免除取消成功！");
        return result;
    }



    @At("/update")
    @POST
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    public NutMap update(@Param("..")CostExemption costExemption){
        NutMap result = new NutMap();
        try {
            costExemptionService.update(costExemption);
        }catch (KamsException e){
            result.setv("ok",false);
            result.setv("msg",e.getMessage());
            return  result;
        }catch (Exception e){
            result.setv("ok",false);
            result.setv("msg","保存失败");
            return  result;
        }
        result.setv("ok",true);
        result.setv("msg","费用免除提交成功！");
        result.setv("id",costExemption.getId());
        return result;
    }


    @At("/loan_repay_list")
    @POST
    @Ok("json")
    public Object queryLoanRepayByLoanId(@Param("loanId")String loanId){
        if (productInfoTmplService.isBill(loanId)) {
            return  billLoanService.queryBillLoanRepay(loanId);
        }
        return loanRepayService.queryLoanRepayByLoanId(loanId);
    }

    @At("/loan_repay_fee")
    @POST
    @Ok("json")
    public Map fetchLoanRepayFeeByRepayId(@Param("repayId")String repayId){
        return loanRepayService.fetchLoanRepayFeeByRepayId(repayId);
    }


    /**
     * 费用免除编辑
     * @return
     */
    @At("/edit")
    @GET
    @Ok("beetl:/specialApply/costExemption/edit.html")
    public void edit(@Param("id")String id,@Param("loanId")String loanId){
    }

    /**
     * 跳转业务展期审批页面列表
     * @return
     */
    @At("/list_approval_business")
    @RequiresPermissions("costExemptionBusinessApproval:view")
    @Ok("beetl:/specialApply/costExemption/list_approval_business.html")
    public void listApprovalBusiness(){
    }
    /**
     * 跳转风控展期审批页面列表
     * @return
     */
    @At("/list_approval_risk")
    @RequiresPermissions("costExemptionRiskApproval:view")
    @Ok("beetl:/specialApply/costExemption/list_approval_risk.html")
    public void listApprovalRisk(){
    }
    /**
     * 跳转风控财务审批页面列表
     * @return
     */
    @At("/list_approval_finance")
    @RequiresPermissions("costExemptionFinanceApproval:view")
    @Ok("beetl:/specialApply/costExemption/list_approval_finance.html")
    public void listApprovalFinance(){
    }

    /**
     * 跳转风控高管审批页面列表
     * @return
     */
    @At("/list_approval_senior")
    @RequiresPermissions("costExemptionSeniorApproval:view")
    @Ok("beetl:/specialApply/costExemption/list_approval_senior.html")
    public void listApprovalSenior(){
    }

    /**
     * 公共的审批页面
     * @return
     */
    @At("/approval_page")
    @Ok("beetl:/specialApply/approvalForm.html")
    public void approvalPage(){
    }
    /**
     * 费用免除查看
     * @return
     */
    @At("/view")
    @GET
    @Ok("beetl:/specialApply/detail.html")
    public void  view(@Param("id")String id){
    }

    /**
     * 费用免除列表
     * @param param
     * @return
     */
    @At("/approval_list")
    @Ok("json")
    @POST
    @AdaptBy(type=JsonAdaptor.class)
    public DataTables approvalList(@Param("..")DataTableParam param){
        return costExemptionService.queryApprovalList(param);
    }

    /**
     * 查询费用免除列表已完成列表
     * @param param
     * @return
     */
    @At("/approval_complete_list")
    @Ok("json")
    @POST
    @AdaptBy(type=JsonAdaptor.class)
    public DataTables queryApprovalCompleteList(@Param("..")DataTableParam param){
        return costExemptionService.queryApprovalCompleteList(param);
    }


    //提交费用减免审批
    @At("/submit_approval")
    @POST
    @Ok("json")
    public NutMap submitApproval( @Param("id")String id ){
        NutMap result = new NutMap();
        try {
            costExemptionService.startApprovalProcess(id);
        }catch (KamsException e){
            result.setv("ok",false);
            result.setv("msg",e.getMessage());
            return  result;
        }catch (Exception e ){
            result.setv("ok",false);
            result.setv("msg","处理异常");
            return  result;
        }
        result.setv("ok",true);
        result.setv("msg","展期审批提交成功！");
        return result;
    }
}

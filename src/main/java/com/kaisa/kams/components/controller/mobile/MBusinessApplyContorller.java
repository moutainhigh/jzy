package com.kaisa.kams.components.controller.mobile;

import com.kaisa.kams.components.controller.base.BusinessApplyBaseController;
import com.kaisa.kams.components.view.loan.MobileApplyView;
import com.kaisa.kams.models.DataTables;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by pengyueyang on 2017/1/6.
 * H5接口
 */
@IocBean
@At("/m/business_apply")
public class MBusinessApplyContorller extends BusinessApplyBaseController{


    @At("/query_risk_media_manifest")
    @GET
    @Ok("json")
    public Object queryRiskediaManifest(@Param("loanId")String loanId){
        return super.queryRiskediaManifest(loanId);
    }

    @At("/query_approval_list")
    @GET
    @Ok("json")
    public DataTables queryApprovalList(@Param("start")int start,
                                        @Param("length")int length,
                                        @Param("draw")int draw,
                                        @Param("type")String type){
        return super.queryApprovalList(start,Integer.MAX_VALUE,draw,type);
    }

    @At("/fetch_base")
    @GET
    @Ok("json")
    public NutMap fetchBaseByLoanId(@Param("id")String id){
        return super.fetchBaseByLoanId(id);
    }

    @At("/fetch_loan")
    @GET
    @Ok("json")
    public NutMap fetchLoan(@Param("id")String id){
        return super.fetchLoan(id);
    }

    @At("/query_business_info")
    @GET
    @Ok("json")
    public Object queryBusinessInfo(@Param("id")String id){
        return super.queryBusinessInfo(id);
    }

    @At("/get_apply_list")
    @GET
    @Ok("json")
    public NutMap queryApplyList(@Param("pageNumber")int pageNumber,
                                 @Param("length")int length,
                                 @Param("businessName")String businessName,
                                 @Param("borrowerName")String borrowerName){
        NutMap result = new NutMap();
        List<MobileApplyView> applyList =  loanService.queryApplyList(pageNumber,length,businessName,borrowerName);
        Map data = new HashMap<>();
        data.put("applyList",applyList);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",data);
        return result;
    }

    @At("/query_bill")
    @Ok("json")
    public NutMap queryBillLoan(@Param("loanId") String loanId) {
        return billLoanService.queryBillLoanInfo(loanId);
    }

    @At("/save_loan_attach")
    @Ok("json")
    public Object saveLoanAttach(@Param("loanId") String loanId,
                                 @Param("attachId") String attachId,
                                 @Param("attachDetails") String attachDetails) {
        NutMap result = new NutMap();
        try {
            productMediaAttachService.updateMediaAttachByRiskForH5(attachId, attachDetails, loanId);
            result.put("ok", true);
            result.put("msg", "保存成功");
        }catch (Exception e) {
            result.put("ok", false);
            result.put("msg", "保存失败");
        }
        return result;
    }


    @At("/risk_approve_info_complete")
    @GET
    @Ok("json")
    public Object riskApproveInfoComplete(@Param("loanId")String loanId){
        return super.riskApproveInfoComplete(loanId);
    }
}

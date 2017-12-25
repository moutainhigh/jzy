package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.service.flow.FlowService;
import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.*;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.TimeUtils;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.business.BusinessOrganize;
import com.kaisa.kams.models.business.BusinessUser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by lw on 2017/9/15.
 */
@IocBean
@At("/house_noMortgage_apply")
public class HouseNoMortgageApplyController {

    @Inject
    private HouseNoMortgageApplyService houseNoMortgageApplyService;

    @Inject
    private ProductInfoTmplService productInfoTmplService;

    @Inject
    private LoanRepayService loanRepayService;

    @Inject
    private LoanService loanService;

    @Inject
    private HouseInfoService houseInfoService;

    @Inject
    private BusinessUserService businessUserService;

    @Inject
    private BusinessOrganizeService businessOrganizeService;




    /**
     * 房产解押申请列表
     */
    @At("/pending_application_list")
    @Ok("beetl:/specialApply/noMortgage/list_apply.html")
    @RequiresPermissions("house_noMortgage_apply:view")
    public Context toPendingList() {
        Context ctx = Lang.context();
        ctx.set("addressTypeList",AddressType.values());
        ctx.set("approvalStatusTypeList", ApprovalStatusType.values());
        return ctx;
    }

    /**
     * 查询申请列表
     */
    @At("/query_pending_list")
    @POST
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    public Object queryPendingList(@Param("..")DataTableParam param) {
        return houseNoMortgageApplyService.houseNoMortgageApplyList(param);
    }


    /**
     * 房产业务审批列表
     */
    @At("/process_list")
    @Ok("beetl:/specialApply/noMortgage/list_approval_business.html")
    @RequiresPermissions("house_noMortgage_business:view")
    public Context toProcessBusinessList() {
        Context ctx = Lang.context();
        ctx.set("addressTypeList",AddressType.values());
        ctx.set("approvalStatusTypeList", ApprovalStatusType.values());
        return ctx;
    }

    /**
     * 风控跳转待审批、审批列表
     */
    @At("/process_risk_list")
    @GET
    @Ok("beetl:/specialApply/noMortgage/list_approval_risk.html")
    @RequiresUser
    @RequiresPermissions("house_noMortgage_risk:view")
    public Context toProcessRiskList() {
        Context ctx = Lang.context();
        ctx.set("addressTypeList",AddressType.values());
        ctx.set("approvalStatusTypeList", ApprovalStatusType.values());
        return ctx;
    }
    /**
     * 财务跳转待审批、审批列表
     */
    @At("/process_finance_list")
    @GET
    @Ok("beetl:/specialApply/noMortgage/list_approval_finance.html")
    @RequiresUser
    @RequiresPermissions("house_noMortgage_finance:view")
    public Context toProcessFinanceList() {
        Context ctx = Lang.context();
        ctx.set("addressTypeList",AddressType.values());
        ctx.set("approvalStatusTypeList", ApprovalStatusType.values());
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
        return houseNoMortgageApplyService.queryApprovalList(param);
    }

    /**
     * 查询已审批列表
     */
    @At("/query_process_approved_list")
    @POST
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    public Object queryApprovedList(@Param("..")DataTableParam param) {
        return houseNoMortgageApplyService.queryApprovalCompleteList(param);
    }

    /**
     * 新增申请单跳转
     */
    @At("/to_add")
    @Ok("beetl:/specialApply/intermediaryFee/list_approval_business.html")
    @RequiresPermissions("house_noMortgage_apply:view")
    public Context toAdd() {
        Context ctx = Lang.context();
        ctx.set("addressTypeList", AddressType.values());
        return ctx;
    }

    /**
     * 申请
     */
    @At("/apply_init")
    @POST
    @Ok("json")
    public NutMap applyInit(@Param("businessCode") String businessCode,@Param("applyId") String applyId) {

        NutMap result = new NutMap();
        if (StringUtils.isEmpty(businessCode)) {
            result.put("ok", false);
            result.put("msg", "业务单不能为空");
            return result;
        }
        //是否已申请
        HouseNoMortgageApply editHouseNoMortgageApply = null;
        HouseNoMortgageApply houseNoMortgageApply = houseNoMortgageApplyService.fetchByCode(businessCode);
        if(StringUtils.isNotEmpty(applyId)){
            editHouseNoMortgageApply = houseNoMortgageApplyService.queryHouseNoMortgageApplyById(applyId);
        }
        if(null == houseNoMortgageApply){
            Loan loan = loanService.fetchByCode(businessCode);
            if(null != loan){
                ProductInfoTmpl tmpl=productInfoTmplService.fetchByProductId(loan.getProductId());
                if(ProductTempType.HONGBEN.equals(tmpl.getProductTempType())){
                    //是否已结清
                    if(LoanStatus.CLEARED.equals(loan.getLoanStatus())){
                        List<HouseInfo> houseInfoList = houseNoMortgageApplyService.setHouseInfoStr(loan.getId());
                        List<LoanRepay> loanRepayList = loanRepayService.queryLoanRepayByLoanId(loan.getId());
                        result.put("houseInfoList",houseInfoList);
                        result.put("loanAmount",loan.getActualAmount());
                        result.put("clearDate",loanRepayList.get(loanRepayList.size()-1).getRepayDate());
                        result.put("loanId",loan.getId());
                        result.put("houseNoMortgageApply",editHouseNoMortgageApply);
                        return result;
                    }else {
                        result.put("ok", false);
                        result.put("msg", "业务单未结清");
                        return result;
                    }
                }else {
                    result.put("ok", false);
                    result.put("msg", "业务单不是红本类型");
                    return result;
                }

            }else {
                result.put("ok", false);
                result.put("msg", "业务单不存在");
                return result;
            }
        }else {
            result.put("ok", false);
            result.put("msg", "业务单已申请解押");
            return result;
        }

    }

    private NutMap addNoMortgage(String loanId, String houseListStr,Date mortgageDate, AddressType addressType,String applyId) {
        NutMap result = new NutMap();
        if (StringUtils.isEmpty(loanId) || StringUtils.isEmpty(houseListStr) || null == addressType) {
            result.put("ok", false);
            result.put("msg", "提交信息错误");
            return result;
        }
        HouseNoMortgageApply exsitHouseNoMortgageApply = houseNoMortgageApplyService.queryHouseNoMortgageApplyById(applyId);
        if(StringUtils.isNotEmpty(houseListStr)){
            List<HouseInfo> houseInfoList = Json.fromJsonAsList(HouseInfo.class,houseListStr);
            for(HouseInfo houseInfo : houseInfoList){
                boolean houseFlag = houseInfoService.updateWarrantNumber(houseInfo.getId(),houseInfo.getWarrantNumber());
                if(houseFlag){
                    result.put("ok", true);
                    result.put("msg", "房产信息保存成功");
                } else {
                    result.put("ok", false);
                    result.put("msg", "房产信息保存失败");
                }
            }

        }
        HouseNoMortgageApply houseNoMortgageApply = new HouseNoMortgageApply();
        if(null != exsitHouseNoMortgageApply){
            houseNoMortgageApply = exsitHouseNoMortgageApply;
        }
        Date now = new Date();
        Loan loan = loanService.fetchById(loanId);
        BusinessUser businessUser = businessUserService.fetchById(loan.getSaleId());
        if(null != businessUser){
            BusinessOrganize businessOrganize = businessOrganizeService.fetchById(businessUser.getOrganizeId());
            if(null != businessOrganize){
                String businessSource = loanService.getBusinessSource(loan.getChannelId(),businessOrganize.getBusinessLine().toString(),businessOrganize.getCode(),loan.getSaleName());
                houseNoMortgageApply.setBusinessSource(businessSource);
            }
        }
        List<HouseInfo> houseInfoList = houseInfoService.queryByLoanId(loanId);
        if(CollectionUtils.isNotEmpty(houseInfoList)){
            houseNoMortgageApply.setBorrower(houseInfoList.get(0).getOwer());
        }
        houseNoMortgageApply.setProductId(loan.getProductId());
        houseNoMortgageApply.setBusinessCode(loan.getCode());
        houseNoMortgageApply.setLoanId(loanId);
        houseNoMortgageApply.setAddressType(addressType);
        houseNoMortgageApply.setMortgageDate(mortgageDate);
        houseNoMortgageApply.setApplyCode(houseNoMortgageApplyService.fetchMaxCode());
        houseNoMortgageApply.setSubmitDate(now);
        houseNoMortgageApply.setLoanStatus(ApprovalStatusType.IN_EDIT);
        houseNoMortgageApply.setUpdateBy(ShiroSession.getLoginUser().getName());
        houseNoMortgageApply.setUpdateTime(now);
        houseNoMortgageApply.setCreateBy(ShiroSession.getLoginUser().getName());
        houseNoMortgageApply.setCreateTime(now);
        if(null == exsitHouseNoMortgageApply) {
            houseNoMortgageApply = houseNoMortgageApplyService.addHouseNoMortgageApply(houseNoMortgageApply);
            if (null != houseNoMortgageApply) {
                result.put("ok", true);
                result.put("houseNoMortgageApply", houseNoMortgageApply);
                result.put("msg", "保存成功");
            } else {
                result.put("ok", false);
                result.put("msg", "保存失败");
            }
        }else {
            boolean updateFlag = houseNoMortgageApplyService.updateHouseNoMortgageApply(houseNoMortgageApply);
            if (updateFlag) {
                result.put("ok", true);
                result.put("houseNoMortgageApply", houseNoMortgageApply);
                result.put("msg", "保存成功");
            } else {
                result.put("ok", false);
                result.put("msg", "保存失败");
            }
        }
        return result;
    }


    /**
     * 保存或者提交
     */
    @At("/add_houseNoMortgageApply")
    @POST
    @Ok("json")
    public NutMap submitHouseNoMortgageApply(@Param("loanId") String loanId,
                                             @Param("houseListStr") String houseListStr,
                                             @Param("addressType") AddressType addressType,
                                             @Param("mortgageDate") Date mortgageDate,
                                             @Param("type") String type,
                                             @Param("applyId") String applyId) {
        NutMap result = new NutMap();
        HouseNoMortgageApply houseNoMortgageApply = null;
        if(StringUtils.isNotEmpty(type) && ("add").equals(type)){
            result = addNoMortgage(loanId, houseListStr,mortgageDate, addressType,applyId);
            return result;
        }else if(StringUtils.isNotEmpty(type) && ("submit").equals(type)){
            result = addNoMortgage(loanId, houseListStr,mortgageDate, addressType,applyId);
            if(null != result && ("true").equals(result.get("ok").toString()) && null != result.get("houseNoMortgageApply")){
                houseNoMortgageApply = (HouseNoMortgageApply) result.get("houseNoMortgageApply");
            }
            //提交
            result = houseNoMortgageApplyService.startApprovalProcess(houseNoMortgageApply,result);
            if (null != result && ("false").equals(result.get("ok").toString())) {
                return result;
            }
            return result;
        }else {
            result.put("ok", false);
            result.put("msg", "提交信息错误");
            return result;
        }

    }

    /**
     * 编辑
     * @return
     */
    @At("/edit")
    @Ok("beetl:/mortgage/edit.html")
    @RequiresUser
    public Context edit(@Param("id")String id){
        Context ctx = Lang.context();
        return ctx;
    }


    /**
     * 查看、编辑
     * @param applyId
     * @param loanId
     * @return
     */
    @At("/query_detail")
    @POST
    @Ok("json")
    public NutMap get( @Param("applyId")String applyId,@Param("loanId")String loanId ){
        NutMap result = new NutMap();
        Loan loan = loanService.fetchById(loanId);
        List<LoanRepay> loanRepayList = loanRepayService.queryLoanRepayByLoanId(loan.getId());
        HouseNoMortgageApply houseNoMortgageApply  = houseNoMortgageApplyService.queryHouseNoMortgageApplyById(applyId);
        List<HouseInfo> houseInfoList = houseNoMortgageApplyService.setHouseInfoStr(loan.getId());
        result.put("houseInfoList",houseInfoList);
        result.put("loanAmount",loan.getActualAmount());
        result.put("clearDate",loanRepayList.get(loanRepayList.size()-1).getRepayDate());
        result.put("houseNoMortgageApply",houseNoMortgageApply);
        result.put("loanId",loan.getId());
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
     * 取消申请单
     * @param id
     * @return
     */
    @At("/update_status")
    @POST
    @Ok("json")
    public Object updateStatus(@Param("id") String id) {
        NutMap result = new NutMap();
        HouseNoMortgageApply houseNoMortgageApply = houseNoMortgageApplyService.queryHouseNoMortgageApplyById(id);
        if(null != houseNoMortgageApply && ApprovalStatusType.CANCEL.equals(houseNoMortgageApply.getLoanStatus())){
            result.put("ok", false);
            result.put("msg", "业务单不能重复取消");
            return result;
        }
        boolean flag = houseNoMortgageApplyService.updateLoanStatus(id,ApprovalStatusType.CANCEL);
        if (flag) {
            result.put("ok", true);
            result.put("msg", "取消成功");
        } else {
            result.put("ok", false);
            result.put("msg", "取消失败");
        }
        return result;
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

    @At("/document_list")
    @Ok("json")
    public List getMortgageDocumnetList(@Param("addressType")AddressType addressType){
        List list=   MortgageDocumentType.noMortgageDocumentlist(addressType);
        return list;
    }

    @At("/document_download")
    @Ok("void")
    public void documentDownload(@Param("houseId")String houseId ,@Param("loanId")String loanId ,@Param("mortgageDocumentType")MortgageDocumentType mortgageDocumentType ,@Param("applyId")String applyId,HttpServletResponse response ) {

        try{
            houseNoMortgageApplyService.documentDownload(houseId,loanId,mortgageDocumentType,applyId,response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @At("/approval_download")
    @Ok("void")
    public void approvalDownload(@Param("id")String id , HttpServletResponse response  ) {
        try{
            houseNoMortgageApplyService.approvalDownload(id,response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

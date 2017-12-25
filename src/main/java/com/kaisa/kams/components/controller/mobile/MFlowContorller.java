package com.kaisa.kams.components.controller.mobile;

import com.kaisa.kams.components.controller.base.FlowBaseController;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.enums.ApprovalType;
import com.kaisa.kams.enums.FlowConfigureType;
import com.kaisa.kams.models.LoanRiskInfo;


import org.apache.shiro.authz.annotation.RequiresUser;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.ViewModel;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

import java.util.Date;


/**
 * Created by pengyueyang on 2017/1/6.
 */
@IocBean
@At("/m/flow")
public class MFlowContorller extends FlowBaseController {

    /**
     * 获取当前用户对loanId对应的单的待处理节点
     * @param loanId
     * @return
     */
    @At("/query_user_approval")
    @GET
    @Ok("json")
    @RequiresUser
    public Object queryUserApproval(@Param("loanId") String loanId, @Param("flowConfigureType")FlowConfigureType flowConfigureType){
        return super.queryApproval(loanId,flowConfigureType);
    }


    /**
     * 流程节点审批
     * @param orderId
     * @param taskId
     * @param approvalCodeStr
     * @return
     */
    @At("/node_approval")
    @POST
    @Ok("json")
    public Object nodeApproval(@Param("loanId") String loanId,
                               @Param("orderId")String orderId,
                               @Param("taskId")String taskId,
                               @Param("approvalCode")String approvalCodeStr,
                               @Param("approvalType") ApprovalType approvalType,
                               @Param("content") String content,
                               @Param("needRepeatFlow") boolean needRepeatFlow,
                               @Param("enterprise") boolean enterprise,
                               @Param("flowConfigureType")FlowConfigureType flowConfigureType,
                               @Param("intermediary") boolean intermediary){
        return super.nodeApproval(loanId,orderId,taskId,approvalCodeStr,approvalType,content,needRepeatFlow,enterprise,flowConfigureType,intermediary);
    }


    @At("/approve_result_list")
    @Ok("beetl:/h5/approveResultList.html")
    @GET
    public void approveResultList(@Param("loanId")String loanId,
                                 @Param("approvalType")String approvalType, ViewModel model){
        model.setv("loanId",loanId);
        model.setv("approvalType",approvalType);
    }


    //后去借款流程审批列表，注意，是借款流程，不是别的，别的你需要重新写一个
    @At("/query_approval_list")
    @GET
    @Ok("json")
    public Object queryApproval(@Param("loanId")String loanId,
                               @Param("approvalType")ApprovalType approvalType){
        return super.queryApprovalList(loanId,approvalType,FlowConfigureType.BORROW_APPLY);
    }

    @At("/save_loan_risk_info_content")
    @POST
    @Ok("json")
    public Object saveLoanRiskInfoContent(@Param("loanId")String loanId,
                                          @Param("riskInfoContent")String riskInfoContent){
        NutMap resultMap = new NutMap();
        boolean result;
        LoanRiskInfo loanRiskInfo = loanRiskInfoService.fetchByLoanId(loanId);
        if (null != loanRiskInfo) {
            loanRiskInfo.setContent(riskInfoContent);
            loanRiskInfo.setUpdateTime(new Date());
            loanRiskInfo.setUpdateBy(ShiroSession.getLoginUser().getId());
            result = loanRiskInfoService.update(loanRiskInfo);
            resultMap.put("ok",result);
            return resultMap;
        }
        loanRiskInfo = new LoanRiskInfo();
        loanRiskInfo.setLoanId(loanId);
        loanRiskInfo.setContent(riskInfoContent);
        loanRiskInfo.setCreateBy(ShiroSession.getLoginUser().getName());
        loanRiskInfo.setCreateTime(new Date());
        loanRiskInfo.setUpdateBy(ShiroSession.getLoginUser().getName());
        loanRiskInfo.setUpdateTime(new Date());
        result = loanRiskInfoService.add(loanRiskInfo)!=null?true:false;
        resultMap.put("ok",result);
        return resultMap;

    }
}

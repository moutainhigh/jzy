package com.kaisa.kams.components.controller.flow;

import com.kaisa.kams.components.controller.base.FlowBaseController;
import com.kaisa.kams.enums.ApprovalType;
import com.kaisa.kams.enums.FlowConfigureType;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

/**
 * 流程相关处理
 * Created by weid on 2016/12/6.
 */
@IocBean
@At("/flow")
public class FlowControler extends FlowBaseController {

    @At("/to_approval_list")
    @GET
    @Ok("beetl:/flow/approval_list.html")
    public void toApprovalList(){
    }

    /**
     * 获取当前单所有的审批信息
     * @return
     */
    @At("/query_approval")
    @GET
    public Object queryApproval(@Param("loanId")String loanId,
                                 @Param("approvalType")ApprovalType approvalType, @Param("flowConfigureType")FlowConfigureType flowConfigureType){
        if(flowConfigureType.equals(FlowConfigureType.MORTGAGE)){
            return super.queryApprovalListMortgage(loanId,approvalType,flowConfigureType);
        }else if(FlowConfigureType.EXTENSION.equals(flowConfigureType)){
            return super.queryApprovalListExtension(loanId,approvalType,flowConfigureType);
        }else if(FlowConfigureType.COST_WAIVER.equals(flowConfigureType)){
            return super.queryApprovalListCostExemption(loanId,approvalType,flowConfigureType);
        }else{
            return super.queryApprovalList(loanId,approvalType,flowConfigureType);
        }

    }

    /**
     * 获取当前单所有的审批信息（抵押）
     * @return
     */
    @At("/query_approval_mortgage")
    @GET
    public Object queryApprovalMortgage(@Param("mortgageId")String mortgageId,
                                @Param("approvalType")ApprovalType approvalType, @Param("flowConfigureType")FlowConfigureType flowConfigureType){
        return super.queryApprovalListMortgage(mortgageId,approvalType,flowConfigureType);
    }



    /**
     * 获取当前用户对loanId对应的单的待处理节点
     * @param loanId
     * @return
     */
    @At("/query_user_approval")
    @GET
    public Object queryUserApproval(@Param("loanId") String loanId, @Param("flowConfigureType")FlowConfigureType flowConfigureType){
        if(flowConfigureType.equals(FlowConfigureType.MORTGAGE)){
            return super.queryApprovalMortgage(loanId,flowConfigureType);
        }else if(FlowConfigureType.EXTENSION.equals(flowConfigureType)){
            return super.queryApprovalExtension(loanId,flowConfigureType);
        }else if(FlowConfigureType.COST_WAIVER.equals(flowConfigureType)){
            return super.queryApprovalCostExemption(loanId,flowConfigureType);
        }else{
            return super.queryApproval(loanId,flowConfigureType);
        }

    }
    /**
     * 获取当前用户对loanId对应的单的待处理节点（抵押）
     * @param mortgageId
     * @return
     */
    @At("/query_user_approval_mortgage")
    @GET
    public Object queryUserApprovalMortgage(@Param("mortgageId") String mortgageId, @Param("flowConfigureType")FlowConfigureType flowConfigureType){
        return super.queryApprovalMortgage(mortgageId,flowConfigureType);
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
        if(flowConfigureType.equals(FlowConfigureType.MORTGAGE)){
            return super.nodeApprovalMortgage(loanId,orderId,taskId,approvalCodeStr,approvalType,content,needRepeatFlow,enterprise,flowConfigureType);
        }else if(FlowConfigureType.EXTENSION.equals(flowConfigureType)){
            return super.nodeApprovalExtension(loanId,orderId,taskId,approvalCodeStr,approvalType,content,needRepeatFlow,enterprise,flowConfigureType);
        }else if(FlowConfigureType.COST_WAIVER.equals(flowConfigureType)){
            return super.nodeApprovalCostExemption(loanId,orderId,taskId,approvalCodeStr,approvalType,content,needRepeatFlow,enterprise,flowConfigureType);
        }else{
            return super.nodeApproval(loanId,orderId,taskId,approvalCodeStr,approvalType,content,needRepeatFlow,enterprise,flowConfigureType,intermediary);
        }


    }
    /**
     * 流程节点审批（抵押）
     * @param orderId
     * @param taskId
     * @param approvalCodeStr
     * @return
     */
    @At("/node_approval_mortgage")
    @POST
    public NutMap nodeApprovalMortgage(@Param("mortgageId") String mortgageId,
                                       @Param("orderId")String orderId,
                                       @Param("taskId")String taskId,
                                       @Param("approvalCode")String approvalCodeStr,
                                       @Param("approvalType") ApprovalType approvalType,
                                       @Param("content") String content,
                                       @Param("needRepeatFlow") boolean needRepeatFlow, @Param("enterprise") boolean enterprise, @Param("flowConfigureType")FlowConfigureType flowConfigureType){
        return super.nodeApprovalMortgage(mortgageId,orderId,taskId,approvalCodeStr,approvalType,content,needRepeatFlow,enterprise,flowConfigureType);

    }

}

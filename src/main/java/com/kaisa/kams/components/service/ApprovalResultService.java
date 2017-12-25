package com.kaisa.kams.components.service;

import com.kaisa.kams.enums.ApprovalType;
import com.kaisa.kams.enums.FlowConfigureType;
import com.kaisa.kams.models.flow.ApprovalResult;

import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.IdNameEntityService;

import java.util.List;

/**
 * 审批流程结果表
 * Created by weid on 2016/12/15.
 */
@IocBean(fields="dao")
public class ApprovalResultService extends IdNameEntityService<ApprovalResult> {

    /**
     * 查询
     * @param loanId
     * @param approvalType
     * @return
     */
    public List<ApprovalResult> query(String loanId, ApprovalType approvalType, FlowConfigureType flowConfigureType) {
        Cnd cnd = Cnd.where("loanId","=",loanId);
        if(null!=approvalType){
            cnd.and("approvalType","=",approvalType);
        }
        if(null!=flowConfigureType){
            cnd.and("flowConfigureType","=",flowConfigureType);
        }
        cnd.asc("approvalTime");
        return dao().query(ApprovalResult.class,cnd);
    }

    /**
     * 新增
     * @param approvalResult
     * @return
     */
    public ApprovalResult add(ApprovalResult approvalResult) {
        return dao().insert(approvalResult);
    }

    /**
     * 获取第一个节点
     * @param loanId
     * @return
     */
    public ApprovalResult fetchFirst(String loanId,FlowConfigureType flowConfigureType) {
        return dao().fetch(ApprovalResult.class,Cnd.where("loanId","=",loanId).and("flowConfigureType","=",flowConfigureType).asc("approvalTime"));
    }

    /**
     * 获取当前流程进行到的最后一个节点
     * @param loanId
     * @return
     */
    public ApprovalResult fetchLast(String loanId,FlowConfigureType flowConfigureType){
        return dao().fetch(ApprovalResult.class,Cnd.where("loanId","=",loanId).and("flowConfigureType","=",flowConfigureType).desc("approvalTime"));
    }

    /**
     * 查看当前用户对单的审批节点
     * @param loanId
     * @param approvalType
     * @param userId
     * @return
     */
    public List<ApprovalResult> queryByTypeAndUserId(String loanId, ApprovalType approvalType, String userId,FlowConfigureType flowConfigureType) {
        return dao().query(ApprovalResult.class,Cnd.where("loanId","=",loanId).and("approvalType","=",approvalType).and("userId","=",userId).and("flowConfigureType","=",flowConfigureType).desc("approvalTime"));
    }
}

package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.HistoryLoanRelationService;
import com.kaisa.kams.components.service.LoanService;
import com.kaisa.kams.components.view.loan.HistoryLoanRelationView;
import com.kaisa.kams.enums.LoanStatus;
import com.kaisa.kams.models.HistoryLoanRelation;
import com.kaisa.kams.models.Loan;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.*;


import java.util.Date;
import java.util.List;

/**
 * 历史关联订单处理
 * Created by luoyj on 2017/09/16.
 */
@IocBean
@At("/history_loan_relation")
public class HistoryLoanRelationControler{

    @Inject
    private HistoryLoanRelationService historyLoanRelationService;

    @Inject
    private LoanService loanService;


    /**
     * 查询历史订单和历史业务关联接口
     * @param loanId
     * @return
     */
    @At("/query_history_loan_relation")
    @POST
    @Ok("json")
    public NutMap queryHistoryLoanRelation(@Param("loanId") String loanId){
        NutMap nutMap = new NutMap();
        List<HistoryLoanRelationView> historyLoanRelationList= historyLoanRelationService.queryHistoryLoanRelationList(loanId);
        List<HistoryLoanRelationView> loanList=historyLoanRelationService.queryHistoryLoanById(loanId);
        nutMap.setv("historyLoanRelationList",historyLoanRelationList);
        nutMap.setv("loanList",loanList);
      return nutMap;
    }

    /**
     * 通过code关联历史订单接口
     * @param loanId
     * @return
     */
    @At("/relation_loan_by_code")
    @POST
    @Ok("json")
    public NutMap relationLoanByCode(@Param("code") String code,@Param("loanId") String loanId,@Param("approveStatus") String approveStatus){
        NutMap map=new NutMap();
        //通过code 查询出LoanId
       Loan loan=loanService.fetch(Cnd.where("code","=",code));
        if(loan==null){
            map.put("ok",false);
            map.put("message","该历史借款单不存在.");
            return map;
        }
        if(loan.getId().equals(loanId)){
            map.put("ok",false);
            map.put("message","不能和本业务单关联.");
            return map;
        }
        //判断此订单是否为取消订单
        if(loan.getLoanStatus().equals(LoanStatus.CANCEL)
                ||loan.getLoanStatus().equals(LoanStatus.LOANCANCEL)
                ||loan.getLoanStatus().equals(LoanStatus.APPROVEREJECT)
                ||loan.getLoanStatus().equals(LoanStatus.SAVE)
                ||loan.getLoanStatus().equals(LoanStatus.CHANNELSAVE)){
            map.put("ok",false);
            map.put("message","该历史借款单为编辑/已取消/已拒绝/取消放款状态订单,不能关联.");
            return map;
        }
        //判断此单能否关联
        HistoryLoanRelation h=historyLoanRelationService.fetchByRelationLoanIdAndLoanId(loan.getId(),loanId);
        if(h!=null){
            map.put("ok",false);
            map.put("message","该历史借款单已经关联.");
            return  map;
        }
        //通过code关联订单
        HistoryLoanRelation history=new HistoryLoanRelation();
        history.setLoanId(loanId);
        history.setRelationLoanId(loan.getId());
        history.setCreateBy(ShiroSession.getLoginUser().getName());
        history.setCreateTime(new Date());
        // 查询本单状态
        if(StringUtils.isEmpty(approveStatus)){
            history.setApproveStatus(LoanStatus.SAVE.toString());
        }else {
            history.setApproveStatus(approveStatus);
        }
        HistoryLoanRelation historyNew=historyLoanRelationService.insertHistoryLoanRelation(history);
        if(historyNew==null){
            map.put("ok",false);
            map.put("data","该历史借款单关联失败.");
            return map;
        }
        map.put("ok",true);
        map.put("data","该历史借款单关联成功.");
        return map;
    }


    /**
     * 根据id删除历史关联信息记录
     * @param loanId
     * @param relationLoanId
     * @return
     */
    @At("/del_history_loan_relation")
    @POST
    @Ok("json")
    public NutMap delHistoryLoanRelation(@Param("loanId") String loanId,@Param("relationLoanId") String relationLoanId,@Param("approveStatus") String approveStatus){
        NutMap map=new NutMap();
        HistoryLoanRelation h=historyLoanRelationService.fetchByRelationLoanIdAndLoanId(relationLoanId,loanId);
        //判断该操作员是否可以删除
        String userName=ShiroSession.getLoginUser().getName();
        if(h==null){
            map.put("ok",false);
            map.put("message","删除的订单不存在.");
            return map;
        }
        if(StringUtils.isEmpty(approveStatus)){
            if(!LoanStatus.SAVE.toString().equals(h.getApproveStatus())){
                map.put("ok",false);
                map.put("message","没有删除权限.");
                return map;
            }
        }else {
            if(!userName.equals(h.getCreateBy())||!approveStatus.equals(h.getApproveStatus())){
                map.put("ok",false);
                map.put("message","没有删除权限.");
                return map;
            }
        }

        //删除关联记录
        int i=historyLoanRelationService.delete(h.getId());
        if(i==0){
            map.put("ok",false);
            map.put("message","删除异常.");
            return map;
        }
        map.put("ok",true);
        map.put("message","删除成功.");
        return map;
    }

}

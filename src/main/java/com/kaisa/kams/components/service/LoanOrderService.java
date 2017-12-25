package com.kaisa.kams.components.service;

import com.kaisa.kams.enums.FlowConfigureType;
import com.kaisa.kams.models.LoanFeeTemp;
import com.kaisa.kams.models.LoanOrder;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.sql.SqlCallback;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * 流程实例订单
 * Created by weid on 2016/12/19.
 */
@IocBean(fields="dao")
public class LoanOrderService extends IdNameEntityService<LoanOrder> {

    private static final Log log = Logs.get();

    /**
     * 新增
     * @param loanOrder
     * @return
     */
    public LoanOrder add(LoanOrder loanOrder){
       if(null==loanOrder){
           return null;
       }
       return dao().insert(loanOrder);
    }

    /**
     * 通过orderId查询
     * @param orderId
     * @return
     */
    public LoanOrder fetchByOrderId(String orderId){
      if(StringUtils.isEmpty(orderId)){
          return null;
      }
      return dao().fetch(LoanOrder.class, Cnd.where("orderId","=",orderId));
    }

    /**
     * 通过loanId查询
     * @param loanId
     * @return
     */
    public LoanOrder fetchByLoanId(String loanId, FlowConfigureType flowConfigureType){
        return dao().fetch(LoanOrder.class, Cnd.where("loanId","=",loanId).and("flowConfigureType","=",flowConfigureType));
    }


    /**
     * 通过loanId删除
     * @param loanId
     * @return
     */
    public boolean deleteByLoanId(String loanId,FlowConfigureType flowConfigureType) {
        int flag = dao().clear(LoanOrder.class,Cnd.where("loanId","=",loanId).and("flowConfigureType","=",flowConfigureType));
        return flag>0;
    }

    /**
     * 获取loanId列表
     * @param orderIds
     * @return
     */
    public List<String> getLoanIds(List<String> orderIds) {
        Sql sql = Sqls.create("SELECT loanId FROM sl_loan_order WHERE orderId in(@orderIds)");
        sql.setParam("orderIds", orderIds.toArray());
        sql.setCallback(Sqls.callback.strs());
        dao().execute(sql);
        return sql.getList(String.class);
    }
}

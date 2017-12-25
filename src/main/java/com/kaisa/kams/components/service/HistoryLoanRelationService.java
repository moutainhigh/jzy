package com.kaisa.kams.components.service;


import com.kaisa.kams.components.view.loan.HistoryLoanRelationView;
import com.kaisa.kams.enums.LoanStatus;
import com.kaisa.kams.models.HistoryLoanRelation;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.IdNameEntityService;
import java.util.ArrayList;;
import java.util.List;

/**
 * 历史借款关联服务层
 * Created by luoyj on 2017/9/15.
 */
@IocBean(fields="dao")
public class HistoryLoanRelationService extends IdNameEntityService<HistoryLoanRelation> {


    /**
     * 通过relationLoanId 和 loanId 查询HistoryLoanRelation
     * @param relationLoanId
     * @param loanId
     * @return
     */
    public   HistoryLoanRelation  fetchByRelationLoanIdAndLoanId(String relationLoanId,String loanId){
        return dao().fetch(HistoryLoanRelation.class,Cnd.where("relationLoanId","=",relationLoanId).and("loanId","=",loanId));
    }

    /**
     * 新增历史单关联记录
     * @param historyLoanRelation
     * @return
     */
    public  HistoryLoanRelation insertHistoryLoanRelation(HistoryLoanRelation historyLoanRelation){
        return  dao().insert(historyLoanRelation);
    }


    public  List<HistoryLoanRelationView> queryHistoryLoanById(String loanId){
        List<HistoryLoanRelationView> historyLoanRelationViewList = new ArrayList<>();
        //查询出本单的相关借款人id
        List<String> borrowerIdList = getBorrowerIdListByLoanId(loanId);
        if(CollectionUtils.isEmpty(borrowerIdList)){
            return historyLoanRelationViewList;
        }
        List<String> loanIdIdList = getLoanIdListByBorrowerIdList(borrowerIdList,loanId);
        if(CollectionUtils.isEmpty(loanIdIdList)){
            return historyLoanRelationViewList;
        }
        historyLoanRelationViewList = getUnRelationViewList(loanIdIdList,loanId);
        return convertListRepayStatus(historyLoanRelationViewList);
    }

    private List<HistoryLoanRelationView> convertListRepayStatus(List<HistoryLoanRelationView> historyLoanRelationViewList) {
        if (CollectionUtils.isEmpty(historyLoanRelationViewList)) {
            return historyLoanRelationViewList;
        }
        for (HistoryLoanRelationView view : historyLoanRelationViewList) {
            view.setRepayStatus(getRepayStatusDescription(view.getRepayStatus()));
        }
        return historyLoanRelationViewList;
    }

    private String getRepayStatusDescription(String repayStatus) {
        if (StringUtils.isEmpty(repayStatus)) {
            return repayStatus;
        }
        return LoanStatus.valueOf(repayStatus).getDescription();
    }

    private List<String> getBorrowerIdListByLoanId(String loanId) {
        String querySql = "select distinct(borrowerId) from sl_loan_borrower where loanId=@loanId";
        Sql sql = Sqls.create(querySql);
        sql.setParam("loanId", loanId);
        sql.setCallback(Sqls.callback.strs());
        dao().execute(sql);
        return sql.getList(String.class);
    }

    private List<String> getLoanIdListByBorrowerIdList(List<String> borrowerIdList,String loanId) {
        if(CollectionUtils.isEmpty(borrowerIdList)){
            return new ArrayList<>();
        }
        String querySql = "select distinct(loanId) from sl_loan_borrower where loanId<>@loanId and borrowerId in(@borrowerIds)";
        Sql sql = Sqls.create(querySql);
        sql.setParam("loanId", loanId);
        sql.setParam("borrowerIds", borrowerIdList.toArray());
        sql.setCallback(Sqls.callback.strs());
        dao().execute(sql);
        return sql.getList(String.class);
    }

    private List<HistoryLoanRelationView> getUnRelationViewList(List<String> loanIdList, String loanId) {
        if (CollectionUtils.isEmpty(loanIdList)) {
            return new ArrayList<>();
        }
        String querySql = "select l.id id,l.code code,l.loanStatus repayStatus,b.name name from " +
                "(select id,code,masterBorrowerId,loanStatus from sl_loan where id \n" +
                "in (@loanIds) and loanStatus not in (@loanStatus)) l\n" +
                "left join (select relationLoanId from sl_history_loan_relation where loanId=@loanId)h on l.id=h.relationLoanId\n" +
                "left join sl_loan_borrower b on b.id=l.masterBorrowerId\n" +
                "where h.relationLoanId is null";
        Sql sql = Sqls.create(querySql);
        sql.setParam("loanStatus",getQueryLoanStatusList());
        sql.setParam("loanIds", loanIdList.toArray());
        sql.setParam("loanId",loanId);
        sql.setCallback(Sqls.callback.entities());
        Entity<HistoryLoanRelationView> entity = dao().getEntity(HistoryLoanRelationView.class);
        sql.setEntity(entity);
        dao().execute(sql);
        return sql.getList(HistoryLoanRelationView.class);

    }

    private String[] getQueryLoanStatusList() {
        return new String[]{LoanStatus.SAVE.name(),LoanStatus.CHANNELSAVE.name(),LoanStatus.CANCEL.name(),
                LoanStatus.LOANCANCEL.name(),LoanStatus.APPROVEREJECT.name()};
    }


    public List<HistoryLoanRelationView> queryHistoryLoanRelationList(String loanId) {
        List<HistoryLoanRelationView> historyLoanRelationViewList=new ArrayList<>();
        List<String> relationLoanIds = getLoanIdListByRelationLoanId(loanId);
        if(CollectionUtils.isNotEmpty(relationLoanIds)) {
            historyLoanRelationViewList = getRelationViewList(relationLoanIds);
            return convertListRepayStatus(historyLoanRelationViewList);
        }
        return historyLoanRelationViewList;
    }

    private List<HistoryLoanRelationView> getRelationViewList(List<String> relationLoanIds) {
        String querySql = "select l.id id,l.code code,l.loanStatus repayStatus,b.name name from sl_loan l\n" +
                "left join sl_loan_borrower b on b.id=l.masterBorrowerId\n" +
                "where l.id in(@loanIds)";
        Sql sql = Sqls.create(querySql);
        sql.setParam("loanIds", relationLoanIds.toArray());
        sql.setCallback(Sqls.callback.entities());
        Entity<HistoryLoanRelationView> entity = dao().getEntity(HistoryLoanRelationView.class);
        sql.setEntity(entity);
        dao().execute(sql);
        return sql.getList(HistoryLoanRelationView.class);
    }


    private List<String> getLoanIdListByRelationLoanId(String loanId) {
        String querySql = "select distinct(relationLoanId) from sl_history_loan_relation where loanId=@loanId";
        Sql sql = Sqls.create(querySql);
        sql.setParam("loanId", loanId);
        sql.setCallback(Sqls.callback.strs());
        dao().execute(sql);
        return sql.getList(String.class);
    }


}

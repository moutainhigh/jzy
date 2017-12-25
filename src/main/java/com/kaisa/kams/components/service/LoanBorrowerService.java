package com.kaisa.kams.components.service;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.Borrower;
import com.kaisa.kams.models.LoanBorrower;
import com.kaisa.kams.models.User;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.util.Date;
import java.util.List;

/**
 * Created by weid on 2016/12/13.
 */
@IocBean(fields="dao")
public class LoanBorrowerService extends IdNameEntityService<LoanBorrower> {
    /**
     * 新增
     * @param loanBorrower
     */
    public LoanBorrower add(LoanBorrower loanBorrower) {
      if(null==loanBorrower){
            return null;
       }
      return dao().insert(loanBorrower);
    }

    /**
     * 新增
     * @param loanBorrowerList
     */
    public List add(List<LoanBorrower> loanBorrowerList) {
        if(null==loanBorrowerList){
            return null;
        }
        return dao().insert(loanBorrowerList);
    }

    /**
     * 根据loanId查询
     * @param loanId
     * @return
     */
    public List<LoanBorrower> queryByLoanId(String loanId) {
        return dao().query(LoanBorrower.class, Cnd.where("status","=", PublicStatus.ABLE).and("loanId","=",loanId).desc("master"));
    }

    /**
     * 查找共同借款人
     * @param loanId
     * @return
     */
    public List<LoanBorrower> queryCooperateByLoanId(String loanId) {
        return dao().query(LoanBorrower.class, Cnd.where("status","=", PublicStatus.ABLE).and("loanId","=",loanId).and("master","=",false));
    }



    /**
     * 通过loanId删除
     * @param loanId
     * @return
     */
    public boolean deleteByLoanId(String loanId) {
        return  dao().clear(LoanBorrower.class,Cnd.where("loanId","=",loanId))>0;
    }

    /**
     * 查找主借款人信息
     * @param id
     * @return
     */
    public LoanBorrower fetchById(String id) {
         return dao().fetch(LoanBorrower.class,Cnd.where("id","=",id));
    }

    /**
     * 修改
     * @param borrower
     */
    public boolean update(Borrower borrower) {
        if(null==borrower ||  StringUtils.isEmpty(borrower.getId())){
            return false;
        }
        User user= ShiroSession.getLoginUser();
        int flag = dao().update(LoanBorrower.class, Chain.make("name",borrower.getName()).add("certifNumber",borrower.getCertifNumber()).add("updateBy",user.getName()).add("updateTime",new Date())
                .add("legalRepresentative",borrower.getLegalRepresentative()) .add("legalRepresentativePhone",borrower.getLegalRepresentativePhone())
                .add("linkman",borrower.getLinkman()) .add("linkmanPhone",borrower.getLinkmanPhone())
                .add("residence",borrower.getResidence()),Cnd.where("borrowerId","=",borrower.getId()));
        return flag>0;
    }

    /**
     * 查询当前单的所有借款人与共同借款人的姓名（权属人）
     */
    public List<LoanBorrower> queryPropertyOwnersById(String loanId){
        return dao().query(LoanBorrower.class, Cnd.where("status","=", PublicStatus.ABLE).and("loanId","=",loanId));

    }

    /**
     * 查询（权属人）
     */
    public List<Borrower> queryPropertyOwnersByIds(String ids){
        return dao().query(Borrower.class, Cnd.where("status","=", PublicStatus.ABLE).and("id","in",ids));
    }

    public List<Borrower> queryPropertyOwnersByIdList(List<String> idList){
        if (CollectionUtils.isNotEmpty(idList)) {
            String[] array = idList.toArray(new String[idList.size()]);
            Criteria cri = Cnd.cri();
            cri.where().andEquals("status", PublicStatus.ABLE);
            cri.where().andIn("id", array);
            return dao().query(Borrower.class, cri, null);
        }
        return null;
    }
}

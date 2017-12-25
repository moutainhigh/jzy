package com.kaisa.kams.components.service;


import com.kaisa.kams.models.LoanMortgage;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.util.List;

/**
 * Created with IntelliJ IDEA
 * Created By dengly
 * Date: 2017/9/18
 */
@IocBean(fields="dao")
public class LoanMortgageService extends IdNameEntityService<LoanMortgage> {
    private static final Log log = Logs.get();

    /**
     * 根据业务单id查询已关联的房产抵押编号
     */
    public List<LoanMortgage> queryLoanMortgage(String loanId){
        return dao().query(LoanMortgage.class, Cnd.where("loanId","=",loanId));
    }


    /**
     * 插入操作，关联当前业务单与房产抵押
     */
    public LoanMortgage addLoanMortgage(LoanMortgage loanMortgage){
        if (null == loanMortgage){
            return null;
        }
        return dao().insert(loanMortgage);
    }

    /**
     * 删除操作，取消当前单与房产抵押的关联
     */
    public int deleteLoanMortgage(String loanId,String mortgageCode){
        return dao().clear(LoanMortgage.class,Cnd.where("loanId","=",loanId).and("mortgageCode","=",mortgageCode));
    }

    /**
     * 已经被关联的抵押房产不能被再次关联
     */
    public List<LoanMortgage> queryMortgageCode(String mortgageCode,String loanId){
        return dao().query(LoanMortgage.class, Cnd.where("mortgageCode","=",mortgageCode).and("loanId","=",loanId));
    }


}

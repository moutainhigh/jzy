package com.kaisa.kams.components.service;

import com.kaisa.kams.components.utils.TextFormatUtils;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.Borrower;
import com.kaisa.kams.models.BorrowerAccount;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.util.Collections;
import java.util.List;

/**
 * 借款人收款账号
 * Created by weid on 2016/12/12.
 */
@IocBean(fields="dao")
public class BorrowerAccountService  extends IdNameEntityService<BorrowerAccount> {
    private static final Log log = Logs.get();

    /**
     * 新增收款账号
     * @param borrowerAccount
     */
    public BorrowerAccount add(BorrowerAccount borrowerAccount) {
        if (null==borrowerAccount){
            return null;
        }
        return dao().insert(borrowerAccount);
    }

    /**
     * 新增收款账号
     * @param borrowerAccountList
     */
    public List add(List<BorrowerAccount> borrowerAccountList) {
        if (null==borrowerAccountList){
            return null;
        }
        return dao().insert(borrowerAccountList);
    }

    /**
     * 通过LoanId查询账号
     * @param loanId
     * @return
     */
    public List<BorrowerAccount> queryByLoanId(String loanId) {
        return dao().query(BorrowerAccount.class, Cnd.where("status","=",PublicStatus.ABLE).and("loanId","=",loanId).asc("position"));
    }

    /**
     * 通过loanId删除
     * @param loanId
     */
    public boolean deleteByLoanId(String loanId) {
        return dao().clear(BorrowerAccount.class,Cnd.where("loanId","=",loanId))>0;
    }

    /**
     * 通过LoanId查询账号 并格式化银行账号
     * @param loanId
     * @return
     */
    public List<BorrowerAccount> queryFormatAccountsByLoanId(String loanId) {
        List<BorrowerAccount>  accounts = queryByLoanId(loanId);
        if (CollectionUtils.isNotEmpty(accounts)) {
            accounts.forEach(account -> account.setAccount(TextFormatUtils.formatAccount(account.getAccount())));
        }
        return accounts;
    }



}

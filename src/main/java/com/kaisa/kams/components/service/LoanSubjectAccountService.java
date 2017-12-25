package com.kaisa.kams.components.service;

import com.kaisa.kams.components.utils.TextFormatUtils;
import com.kaisa.kams.models.LoanSubjectAccount;

import org.apache.commons.collections.CollectionUtils;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.IdNameEntityService;

import java.util.List;

/**
 * Created by sunwanchao on 2016/12/6.
 */
@IocBean(fields = "dao")
public class LoanSubjectAccountService extends IdNameEntityService<LoanSubjectAccount> {
    public List<LoanSubjectAccount> query(String subjectId){
        return this.dao().query(LoanSubjectAccount.class, Cnd.where("subjectId","=",subjectId).orderBy("position","asc"));
    }

    /**
     * 通过subjectId查询账号 并格式化银行账号
     * @param subjectId
     * @return
     */
    public List<LoanSubjectAccount> queryFormatAccountsBySubjectId(String subjectId) {
        List<LoanSubjectAccount>  accounts = query(subjectId);
        if (CollectionUtils.isNotEmpty(accounts)) {
            accounts.forEach(account -> account.setAccountNo(TextFormatUtils.formatAccount(account.getAccountNo())));
        }
        return accounts;
    }

    /**
     * 通过Id查询账号 并格式化银行账号
     * @param id
     * @return
     */
    public LoanSubjectAccount queryFormatAccountsById(String id) {
        LoanSubjectAccount account = this.dao().fetch(LoanSubjectAccount.class, Cnd.where("id","=",id));
        if (null != account) {
            account.setAccountNo(TextFormatUtils.formatAccount(account.getAccountNo()));
        }
        return account;
    }
}

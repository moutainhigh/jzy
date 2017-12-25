package com.kaisa.kams.components.service;

import com.kaisa.kams.components.utils.TextFormatUtils;
import com.kaisa.kams.models.BankInfo;
import org.apache.commons.collections.CollectionUtils;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.IdNameEntityService;

import java.util.List;

/**
 * Created by sunwanchao on 2017/3/20.
 */
@IocBean(fields = "dao")
public class BankInfoService extends IdNameEntityService<BankInfo> {
    public List<BankInfo> queryByLoanId(String loanId){
        return dao().query(BankInfo.class, Cnd.where("loanId","=",loanId).orderBy("position","asc"));
    }

    public boolean deleteByLoanId(String loanId){
        return dao().clear(BankInfo.class, Cnd.where("loanId","=",loanId))>0;
    }

    public void save(List<BankInfo> bankInfoList){
        this.dao().insert(bankInfoList);
    }

    /**
     * 通过LoanId查询账号 并格式化银行账号
     * @param loanId
     * @return
     */
    public List<BankInfo> queryFormatBankInfoByLoanId(String loanId) {
        List<BankInfo>  bankInfoList = queryByLoanId(loanId);
        if (CollectionUtils.isNotEmpty(bankInfoList)) {
            bankInfoList.forEach(bankInfo -> bankInfo.setAccount(TextFormatUtils.formatAccount(bankInfo.getAccount())));
        }
        return bankInfoList;
    }

}

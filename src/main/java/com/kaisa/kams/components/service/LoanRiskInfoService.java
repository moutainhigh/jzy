package com.kaisa.kams.components.service;

import com.kaisa.kams.models.LoanRiskInfo;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.IdNameEntityService;

import java.util.List;

/**
 * Created by weid on 2016/12/20.
 */
@IocBean(fields = "dao")
public class LoanRiskInfoService extends IdNameEntityService<LoanRiskInfo> {

    /**
     * 新增
     * @param loanRiskInfo
     * @return
     */
    public LoanRiskInfo add(LoanRiskInfo loanRiskInfo){
        if(null==loanRiskInfo){
             return null;
        }
       return dao().insert(loanRiskInfo);
    }

    /**
     * 修改
     * @param loanRiskInfo
     * @return
     */
    public boolean update(LoanRiskInfo loanRiskInfo){
         if(null==loanRiskInfo){
             return false;
         }
        int flag=dao().update(loanRiskInfo,"^(content|updateBy|updateTime)$");
        return flag>0;
    }

    /**
     * 根据LoanId查找
     * @param loanId
     * @return
     */
    public LoanRiskInfo fetchByLoanId(String loanId){
         return dao().fetch(LoanRiskInfo.class, Cnd.where("loanId","=",loanId).and("extensionId","is",null));
    }
    public LoanRiskInfo fetchByExtensionId(String extensionId){
        return dao().fetch(LoanRiskInfo.class, Cnd.where("extensionId","=",extensionId));
    }
    public List<LoanRiskInfo> listByLoanId(String loanId){
        return this.query(Cnd.where("loanId","=",loanId).asc("createTime"));
    }
}

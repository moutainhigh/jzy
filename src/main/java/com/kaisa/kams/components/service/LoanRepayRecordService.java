package com.kaisa.kams.components.service;

import com.kaisa.kams.models.LoanRepayRecord;

import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.IdNameEntityService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunwanchao on 2016/12/26.
 */
@IocBean(fields = "dao")
public class LoanRepayRecordService extends IdNameEntityService<LoanRepayRecord> {
    public List<LoanRepayRecord> queryByRepayId(String repayId){
        if (StringUtils.isNotEmpty(repayId)) {
            return this.dao().query(LoanRepayRecord.class, Cnd.where("repayId", "=", repayId));
        }
        return new ArrayList<>();
    }

    public LoanRepayRecord fetchById(String id){
        return this.dao().fetch(LoanRepayRecord.class,id);
    }

    public int deleteById(String id){
        return this.dao().delete(LoanRepayRecord.class,id);
    }
}

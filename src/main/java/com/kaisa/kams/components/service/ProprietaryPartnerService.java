package com.kaisa.kams.components.service;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.ProprietaryPartner;
import com.kaisa.kams.models.User;

import org.nutz.dao.Cnd;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.util.Daos;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.service.IdNameEntityService;

import java.util.Date;
import java.util.List;

/**
 * Created by luoyj on 2017/4/17.
 */
@IocBean(fields = "dao")
public class ProprietaryPartnerService extends IdNameEntityService<ProprietaryPartner> {


    public Object listByName(String name, Pager pager, int draw) {
        return query(name, pager, draw);
    }

    /**
     * 获取自营合作方list
     */
    public DataTables query(String name, Pager pager, int draw) {
        Cnd cnd = null;
        List<ProprietaryPartner> proprietaryPartners;
        if (!Strings.isBlank(name)) {
            SqlExpressionGroup e = Cnd.exps("name", "like", "%" + name + "%").or("fullName", "like", "%" + name + "%");
            cnd = Cnd.where(e);
        }
        proprietaryPartners = this.query(cnd, pager);
        return new DataTables(draw, this.dao().count(ProprietaryPartner.class), this.dao().count(ProprietaryPartner.class, cnd), proprietaryPartners);
    }


    /**
     * 新增自营合作方
     */
    public boolean insert(ProprietaryPartner proprietaryPartner) {
        User user = ShiroSession.getLoginUser();
        // 获取新增的记录的人员
        if (proprietaryPartner != null) {
            proprietaryPartner.setCreateTime(new Date());
            proprietaryPartner.setCreateBy(String.valueOf(user.getName()));
        }
        ProprietaryPartner proprietaryPartner1 = dao().insert(proprietaryPartner);
        if (proprietaryPartner1 != null) {
            return true;
        }
        return false;
    }

    /**
     * 修改自营合作方
     */
    public boolean update(ProprietaryPartner proprietaryPartner) {
        User user = ShiroSession.getLoginUser();
        // 获取修改的记录的人员
        if (user != null) {
            proprietaryPartner.setUpdateBy(String.valueOf(user.getName()));
        }
        proprietaryPartner.setUpdateTime(new Date());
        return Daos.ext(dao(), FieldFilter.locked(ProprietaryPartner.class, "^id|createBy|createTime")).update(proprietaryPartner) > 0;
    }


}

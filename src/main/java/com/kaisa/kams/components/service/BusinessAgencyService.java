package com.kaisa.kams.components.service;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.utils.JsonTreeData;
import com.kaisa.kams.components.utils.TreeNodeUtil;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.User;
import com.kaisa.kams.models.business.BusinessAgency;
import org.nutz.dao.Cnd;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 机构服务层
 * Created by luoyj on 2016/12/07.
 */
@IocBean(fields = "dao")
public class BusinessAgencyService extends IdNameEntityService<BusinessAgency> {
    private static final Log log = Logs.get();

    /**
     * 获取有效的机构树结构数据
     *
     * @return
     */
    public List<JsonTreeData> queryAgencyTree() {
        List<JsonTreeData> jsonTreeDatasList = new ArrayList<JsonTreeData>();
        List<BusinessAgency> listAgency = dao().query(BusinessAgency.class, null);
        if (listAgency.size() > 0) {
            for (BusinessAgency agency : listAgency) {
                JsonTreeData treeData = new JsonTreeData();
                treeData.setId(agency.getId());
                treeData.setPid(agency.getParentId());
                treeData.setPath(agency.getPath());
                treeData.setText(agency.getName());
                jsonTreeDatasList.add(treeData);
            }
        }
        return jsonTreeDatasList;
    }

    /**
     * 模糊查询机构数据
     *
     * @param search
     * @return
     */
    public List<BusinessAgency> queryByParam(String search) {
        List<BusinessAgency> agencyList = null;
        if (!Strings.isBlank(search)) {
            Cnd cnd = Cnd.where(Cnd.exp("status", "=", "ABLE")).and(Cnd.exps("name", "like", "%" + search + "%").or("code", "like", "%" + search + "%"));
            agencyList = dao().query(BusinessAgency.class, cnd);
        }
        return agencyList;
    }

    /**
     * 通过id查询机构的信息以及上一级机构信息
     *
     * @param id
     * @return
     */
    public BusinessAgency fetchById(String id) {
        BusinessAgency businessAgency = dao().fetchLinks(dao().fetch(BusinessAgency.class, id), "parent");
        return businessAgency;
    }

    /**
     * 判断机构名是否可用
     * @param name
     * @param type
     * @param id
     * @return
     */
    public boolean eableName(String name, String type, String id) {
        BusinessAgency businessAgency = dao().fetch(BusinessAgency.class, Cnd.where("name","=",name));
        if (businessAgency != null) {
            if (type.equals("add")) {
                return false;
            } else {
                if (!businessAgency.getId().equals(id)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 判断code是否可用
     * @param code
     * @param type
     * @param id
     * @return
     */
    public boolean eableCode(String code, String type, String id) {
        BusinessAgency businessAgency = dao().fetch(BusinessAgency.class, Cnd.where("code","=",code));
        if (businessAgency != null) {
            if (type.equals("add")) {
                return false;
            } else {
                if (!businessAgency.getId().equals(id)) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * 新增机构
     *
     * @param agency
     * @return
     */
    public boolean add(BusinessAgency agency) {
        log.info("agency:" + agency.toString());
        // 判断机构名是否重复
        User user = ShiroSession.getLoginUser();
        // 获取新增的记录的人员
        if (user != null) {
            agency.setCreateBy(String.valueOf(user.getName()));
        }
        agency.setCreateTime(new Date());
        //执行添加机构操作
        BusinessAgency b = dao().insert(agency);
        if (b != null) {
            return true;
        }
        return false;
    }

    /**
     * 修改机构
     *
     * @param agency
     * @return
     */
    public boolean update(BusinessAgency agency) {
        log.info("agency:" + agency.toString());
        User user = ShiroSession.getLoginUser();
        // 获取修改的记录的人员
        if (user != null) {
            agency.setUpdateBy(user.getName());
        }
        agency.setUpdateTime(new Date());
        boolean b = Daos.ext(dao(), FieldFilter.locked(BusinessAgency.class, "^id|createBy|createTime|path|level|parentId")).update(agency) > 0;
        if (b == true) {
            return true;
        }
        return false;
    }
}

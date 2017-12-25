package com.kaisa.kams.components.service;

import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.Product;
import com.kaisa.kams.models.Seal;
import com.kaisa.kams.models.User;
import com.kaisa.kams.models.WfTemplet;
import org.apache.commons.lang3.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.util.cri.SqlExpression;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.IdNameEntityService;

/**
 * Created by weid on 2017/3/24.
 */
@IocBean(fields = "dao")
public class WfTempletSevice extends IdNameEntityService<WfTemplet> {


    /**
     * 新增
     * @param wfTemplet
     * @return
     */
    public WfTemplet add(WfTemplet wfTemplet) {
        if(null==wfTemplet) {
            return null;
        }
        return dao().insert(wfTemplet);
    }


    /**
     * 通过产品Id查询
     * @param productId
     * @return
     */
    public WfTemplet getByProductId(String productId){
        WfTemplet wfTemplet = dao().fetch(WfTemplet.class, Cnd.where("productId","=",productId));
        return wfTemplet;
    }

    /**
     * 通过产品Id查询
     * @param productId
     * @return
     */
    public WfTemplet getByProductIdAndType(String productId,String flowType){
        Cnd cnd =  Cnd.where("productId","=",productId);
        if(StringUtils.isNotEmpty(flowType)){
            cnd.and("flowType","=",flowType);
        }else{
            cnd.and("flowType","is",null);
        }
        WfTemplet wfTemplet = dao().fetch(WfTemplet.class,cnd);
        return wfTemplet;
    }

    /**
     * 修改产品Id
     * @param wff
     * @return
     */
    public WfTemplet update(WfTemplet wff) {
        if(null==wff){
            return null;
        }
        return  dao().update(wff,"^(content|updateBy|updateTime)$")>0 ? wff : null;
    }
}

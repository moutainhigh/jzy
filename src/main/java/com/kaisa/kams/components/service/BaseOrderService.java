package com.kaisa.kams.components.service;

import com.kaisa.kams.components.service.base.BaseService;
import com.kaisa.kams.enums.FlowConfigureType;
import com.kaisa.kams.models.BaseOrder;
import com.kaisa.kams.models.CostExemptionOrder;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;

/**
 * @description：订单的基础service，抽象大部分方法
 * @author：zhouchuang
 * @date：2017-11-16:55
 */
@IocBean(fields = "dao")
public class BaseOrderService extends BaseService<BaseOrder>{

    public  <T extends BaseOrder> T  addBaseOrder(T t){
        if(null==t){
            return null;
        }
        return dao().insert(t);
    }
    public <T extends BaseOrder> T getBaseOrderById(Class<T> clazz,String key ,String id, FlowConfigureType flowConfigureType){
        return  dao().fetch(clazz,Cnd.where(key,"=",id).and("flowConfigureType","=",flowConfigureType).desc("createTime"));
    }
    public <T extends BaseOrder> boolean deleteBaseOrderById(Class<T> clazz,String key ,String id,FlowConfigureType flowConfigureType) {
        int flag = dao().clear(clazz,Cnd.where(key,"=",id).and("flowConfigureType","=",flowConfigureType));
        return flag>0;
    }
}

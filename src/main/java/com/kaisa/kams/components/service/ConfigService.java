package com.kaisa.kams.components.service;


import com.kaisa.kams.models.Config;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.IdNameEntityService;


/**
 * Created by weid on 2016/12/13.
 */
@IocBean(fields="dao")
public class ConfigService extends IdNameEntityService<Config> {

    /**
     * 查找短信开关配置
     * @param name
     * @return
     */
    public Config fetchByName(String name) {
         return dao().fetch(Config.class,Cnd.where("name","=",name));
    }

}

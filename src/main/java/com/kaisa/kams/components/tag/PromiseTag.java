package com.kaisa.kams.components.tag;

import com.kaisa.kams.components.job.OverdueLoanSchedule;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.MenuService;
import org.beetl.core.Context;
import org.beetl.core.GeneralVarTagBinding;
import org.beetl.core.Tag;
import org.beetl.core.statement.Statement;
import org.nutz.dao.entity.annotation.Comment;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snaker.engine.access.Page;

/**
 * Created by zhouchuang on 2017/4/18.
 */
public class PromiseTag extends GeneralVarTagBinding {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromiseTag.class);

    private MenuService menuService;
    @Override
    public void render() {
        Ioc ioc = Mvcs.getIoc();
        menuService =  ioc.get(MenuService.class,"menuService");
        String hasPromise  = (String)getAttributeValue("hasPromise");
        String loginId   = ShiroSession.getLoginUser().getId();
        int count =    menuService.findMenuByPromise(hasPromise,loginId);
        if(count>0)
            doBodyRender();
        else{
            LOGGER.info("用户："+ShiroSession.getLoginUser().getName()+"没有"+hasPromise+"权限");
        }
    }
}

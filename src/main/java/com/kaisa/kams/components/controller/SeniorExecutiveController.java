package com.kaisa.kams.components.controller;

import com.kaisa.kams.enums.LoanStatus;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.mvc.annotation.*;

/**
 * Created by zhouchuang on 2017/10/18.
 */
@IocBean
@At("/senior_executive")
public class SeniorExecutiveController {
    /**
     * 跳转到高管管理页面
     *
     * @return
     */
    @At
    @Ok("beetl:/seniorExecutive/list.html")
    @RequiresPermissions("seniorExecutive:view")
    public Context approvalList() {
        Context ctx = Lang.context();
        ctx.set("statusList", LoanStatus.values());
        return ctx;
    }
}

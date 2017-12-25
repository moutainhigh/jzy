package com.kaisa.kams.components.controller;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;

/**
 * 权限控制层
 * Created by weid on 2016/11/24.
 */
@IocBean
@At("/authority")
public class AuthorityController {

    private static final Log log = Logs.get();

    /**
     * 跳转 用户没有相关操作权限
     * @return
     */
    @At
    @GET
    @Ok("beetl:/users/login.html")
    public Context unauthorized(){
        Context ctx = Lang.context();
        return ctx;
    }


}

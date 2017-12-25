package com.kaisa.kams.components.controller;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;

/**
 * Created by weid on 2016/11/23.
 */
@IocBean
@At("/")
public class HomeController {


    /**
     * 跳转到登录页面
     * @return
     */
    @At("/")
    @Ok("redirect:/user/toLogin")
    public Context toLogin(){
        Context ctx = Lang.context();
        return ctx;
    }
}

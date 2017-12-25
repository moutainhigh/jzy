package com.kaisa;


import com.kaisa.kams.components.aop.CheckUserSession;

import org.beetl.ext.nutz.BeetlViewMaker;
import org.nutz.integration.shiro.ShiroSessionProvider;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.ChainBy;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.IocBy;
import org.nutz.mvc.annotation.Modules;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.SessionBy;
import org.nutz.mvc.annotation.SetupBy;
import org.nutz.mvc.annotation.Views;

import org.nutz.mvc.ioc.provider.ComboIocProvider;

/**
 * Created by dominic on 2016/11/9.
 */

@SetupBy(value=MainSetup.class)
@IocBy(type=ComboIocProvider.class, args={"*js", "ioc/",
        "*anno", "com.kaisa.kams",
        "*tx",
        "*async",
        "*quartz",
        "*jedis",
        "*com.kaisa.kams.components.flow.SnakerIocLoader"
})
@Views({BeetlViewMaker.class})
@ChainBy(args="mvc/mvc-chain.js")
@SessionBy(ShiroSessionProvider.class)
@Modules
@Filters(@By(type = CheckUserSession.class, args = {"user", "/user/toLogin","/m/to_login","/m/index"}))
@Fail("beetl:/errors/500.html")
@Ok("json")
public class MainModule {

}

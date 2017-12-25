package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.security.ShiroSession;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

/**
 * @description：权限测试控制层
 * @author：zhouchuang
 * @date：2017-12-15:33
 */
@IocBean
@At("/permission")
public class PermissionController {

    /**
     * 跳转到角色管理页面
     * @return
     */
    @At("/permissionIndex")
    @RequiresPermissions("PermissionTest:view")
    @Ok("beetl:/permission/permission_index.html")
    public void index(){ }


    @At("/approval")
    @POST
    @RequiresPermissions("PermissionTest:hasApproval")
    public NutMap  approval(){
        NutMap result =  new NutMap();
        result.setv("ok",true);
        result.setv("msg","审批成功");
        return result;
    }

    @At("/test")
    @POST
    @RequiresPermissions("PermissionTest:hasTest")
    public NutMap  test(){
        NutMap result =  new NutMap();
        result.setv("ok",true);
        result.setv("msg","测试成功");
        return result;
    }
}

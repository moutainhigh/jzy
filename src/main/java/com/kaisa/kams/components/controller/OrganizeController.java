package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.OrganizeService;
import com.kaisa.kams.components.utils.JsonTreeData;
import com.kaisa.kams.models.Organize;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

import java.util.*;


/**
 * Created by luoyj on 2016/11/23
 */
@IocBean
@At("/organize")
public class OrganizeController {

    @Inject
    private OrganizeService organizeService;

    /**
     * 跳转到组织岗位页面
     * @return
     */
    @At
    @Ok("beetl:/organize/organize_index.html")
    public Context index(){
        Context ctx = Lang.context();
        //获取菜单
        ctx.set("menus",ShiroSession.getMenu());
        return ctx;
    }

    /**
     * 获取组织岗位listTree结果
     * @return
     */
    @At("/list_organize")
    @Ok("json")
    @POST
    public Object  listOrganize(){
        NutMap nutMap=new NutMap();
        List<JsonTreeData> listData=organizeService.getOrganizeTree();
        nutMap.setv("data",listData);
        return nutMap;
    }

    /**
     * 新增组织岗位信息
     * @param org
     * @return
     */
    @At("/save_organize")
    @Ok("json")
    @POST
    public Object saveOrganize(@Param("::org.")Organize org){
        return  organizeService.add(org);
    }

    /**
     * 修改组织岗位信息
     * @param org
     * @return
     */
    @At("/update_organize")
    @Ok("json")
    @POST
    public Object updateOrganize(@Param("::org.")Organize org){
        return organizeService.update(org);
    }

    /**
     * 删除组织岗位信息
     * @param id
     * @return
     */
    @At("/delete_organize")
    @POST
    @Ok("json")
    public Object deleteOrganize(@Param("id")String id){
          return organizeService.delete(id);
    }

    @At("/fetch_organize")
    @POST
    @Ok("json")
    public Object fetchOrganize(@Param("id")String id){
        return  organizeService.fetch(id);
    }
}

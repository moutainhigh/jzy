package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.BusinessOrganizeService;
import com.kaisa.kams.components.service.OrganizeService;
import com.kaisa.kams.components.utils.JsonTreeData;
import com.kaisa.kams.models.Organize;
import com.kaisa.kams.models.business.BusinessOrganize;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;

import java.util.List;


/**
 * Created by luoyj on 2016/12/06
 */
@IocBean
@At("/business_organize")
public class BusinessOrganizeController {
    private static final Log log = Logs.get();
    @Inject
    private BusinessOrganizeService businessOrganizeService;

    /**
     * 跳转到组织页面
     * @return
     */
    @At
    @Ok("beetl:/business/organize/organize_index.html")
    @RequiresPermissions("businessOrganize:view")
    public Context index(){
        Context ctx = Lang.context();
        //获取菜单
        ctx.set("menus",ShiroSession.getMenu());
        return ctx;
    }

    /**
     * 查询组织列表（包含机构信息）
     * @param param
     * @return
     */
    @At("/list_organize")
    @Ok("json")
    @POST
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("businessOrganize:view")
    public Object listOrganize(@Param("..")DataTableParam param){
        return businessOrganizeService.queryByParam(param);
    }

    /**
     * 模糊查询组织信息（不包含机构数据）
     * @param search
     * @return
     */
    @At("/list_organize_search")
    @Ok("json")
    @POST
    public Object listOrganizeBySearch(@Param("search")String search,@Param("organizeType")String organizeType){
        NutMap nutMap=new NutMap();
        try{
            log.info("search:"+search+"organizeType:"+organizeType);
            nutMap.setv("data",businessOrganizeService.queryBySearch(search,organizeType));
            nutMap.setv("ok",true).setv("msg","成功");
        }catch (Exception e){
            log.error(e.getMessage());
            nutMap.setv("ok",false).setv("msg","获取数据异常");
        }
        return nutMap;
    }

    /**
     * 通过id查询某条组织信息（包括机构信息）
     * @param id
     * @return
     */
    @At("/fetch_organize")
    @Ok("json")
    @POST
    public Object fetchOrganize(@Param("id")String id){
        NutMap nutMap=new NutMap();
        try{
            log.info("id:"+id);
            nutMap.setv("data",businessOrganizeService.fetchById(id));
            nutMap.setv("ok",true).setv("msg","成功");
        }catch (Exception e){
            log.error(e.getMessage());
            nutMap.setv("ok",false).setv("msg","获取数据异常");
        }
       return nutMap;
    }

    /**
     * 新增组织
     * @param businessOrganize
     * @return
     */
    @At("/save_organize")
    @Ok("json")
    @POST
    @Aop("auditInterceptor")
    @RequiresPermissions("businessOrganize:create")
    public Object saveOrganize(@Param("::org.")BusinessOrganize businessOrganize){
        NutMap nutMap=new NutMap();
        //判断name
        boolean b_name=businessOrganizeService.eableName(businessOrganize.getId(),businessOrganize.getName(),"add");
        if(!b_name){
            return nutMap.setv("ok",false).setv("msg","组织" + "名称重复");
        }
        boolean b_saveOrganize=businessOrganizeService.add(businessOrganize);
        if(!b_saveOrganize){

            return nutMap.setv("ok",false).setv("msg","新增组织失败.");
        }
        return  nutMap.setv("ok",true).setv("msg","新增组织成功.");
    }

    /**
     * 修改组织
     * @param businessOrganize
     * @return
     */
    @At("/update_organize")
    @Ok("json")
    @POST
    @Aop("auditInterceptor")
    @RequiresPermissions("businessOrganize:update")
    public Object updateOrganize(@Param("::org.")BusinessOrganize businessOrganize){
        NutMap nutMap=new NutMap();
        //判断name
        boolean b_name=businessOrganizeService.eableName(businessOrganize.getId(),businessOrganize.getName(),"update");
        if(!b_name){
            return nutMap.setv("ok",false).setv("msg","组织" +
                    "名称重复");
        }
        boolean b_saveOrganize=businessOrganizeService.update(businessOrganize);
        if(!b_saveOrganize){

            return nutMap.setv("ok",false).setv("msg","修改组织失败.");
        }
        return  nutMap.setv("ok",true).setv("msg","修改组织成功.");
    }

    /**
     * 获取业务条线
     * @return
     */
    @At("/get_business_line")
    @Ok("json")
    @POST
    public Object getBusinessLine(){
        List<NutMap> list=businessOrganizeService.getBusinessLine();
        if(list.size()>0){
            return new NutMap().setv("data",list).setv("ok",true);
        }
        return  new NutMap().setv("ok",false).setv("msg","获取数据异常.");
    }

    /**
     * 获取业务组织code
     * @return
     */
    @At("/get_org_code")
    @Ok("json")
    @POST
    public Object getOrgCode(@Param("id")String orgId,@Param("organizeType")String organizeType,@Param("lines")String lines){
        String code=businessOrganizeService.getOrgCode(organizeType,orgId,lines);
        if(!Strings.isBlank(code)){
            return new NutMap().setv("data",code).setv("ok",true);
        }
        return  new NutMap().setv("ok",false).setv("msg","获取数据异常.");
    }
}

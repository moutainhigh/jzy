package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.BusinessAgencyService;
import com.kaisa.kams.components.utils.JsonTreeData;
import com.kaisa.kams.components.utils.TreeNodeUtil;
import com.kaisa.kams.models.business.BusinessAgency;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

import java.util.List;


/**
 * 业务机构controller
 * Created by luoyj on 2016/12/06
 */
@IocBean
@At("/business_agency")
public class BusinessAgencyController {
    private static final Log log = Logs.get();
    @Inject
    private BusinessAgencyService businessAgencyService;
    /**
     * 跳转到机构页面
     * @return
     */
    @At
    @Ok("beetl:/business/agency/agency_index.html")
    @RequiresPermissions("businessAgency:view")
    public Context index(){
        Context ctx = Lang.context();
        //获取菜单
        ctx.set("menus",ShiroSession.getMenu());
        return ctx;
    }

    /**
     * 查询机构Tree数据
     * @return
     */
    @At("/list_agency_tree")
    @Ok("json")
    @POST
    @RequiresPermissions("businessAgency:view")
    public Object listAgencyTree(){
        NutMap nutMap=new NutMap();
        try {
            nutMap.setv("data", TreeNodeUtil.getfatherNode(businessAgencyService.queryAgencyTree()));
            nutMap.setv("ok",true).setv("msg","成功");
        }catch (Exception e){
            log.error(e.getMessage());
            nutMap.setv("ok",false).setv("msg","获取数据异常");
        }
        return nutMap;
    }


    /**
     * 通过模糊查询（name，code）机构数据
     * @param search
     * @return
     */
    @At("/list_agency")
    @Ok("json")
    @POST
    public Object listAgency(@Param("search")String search){
        NutMap nutMap=new NutMap();
        try {
            log.info("search:"+search);
            nutMap.setv("data", businessAgencyService.queryByParam(search));
            nutMap.setv("ok",true).setv("msg","成功");

        }catch (Exception e){
            log.error(e.getMessage());
            nutMap.setv("ok",false).setv("msg","获取数据异常");
        }
        return nutMap;
    }

    /**
     * 通过id查询机构信息
     * @param id
     * @return
     */
    @At("/fetch_agency")
    @Ok("json")
    @POST
    public  Object fetchAgency(@Param("id")String id){
        NutMap nutMap=new NutMap();
        try
        {
            log.info("id:"+id);
            nutMap.setv("data", businessAgencyService.fetchById(id));
            nutMap.setv("ok",true).setv("msg","成功");
        }catch (Exception e){
            log.error(e.getMessage());
            nutMap.setv("ok",false).setv("msg","获取数据异常");
        }
        return nutMap;
    }

    /**
     * 保存机构信息
     * @param agency
     * @return
     */
    @At("/save_agency")
    @Ok("json")
    @POST
    @Aop("auditInterceptor")
    @RequiresPermissions("businessAgency:create")
    public Object saveAgency(@Param("::agency.")BusinessAgency agency){
        NutMap nutMap=new NutMap();
        // 判断code
        boolean b_code=businessAgencyService.eableCode(agency.getCode(),"add",agency.getId());
        if(!b_code){
            return nutMap.setv("ok",false).setv("msg","机构代码重复");
        }
        //判断name
        boolean b_name=businessAgencyService.eableName(agency.getName(),"add",agency.getId());
        if(!b_name){
            return nutMap.setv("ok",false).setv("msg","机构名称重复");
        }
        //保存
        boolean b_saveAgency=businessAgencyService.add(agency);
        if(!b_saveAgency){
            return nutMap.setv("ok",false).setv("msg","新增机构失败.");
        }
        return  nutMap.setv("ok",true).setv("msg","新增机构成功.");
    }

    /**
     * 修改机构信息
     * @param agency
     * @return
     */
    @At("/update_agency")
    @Ok("json")
    @POST
    @Aop("auditInterceptor")
    @RequiresPermissions("businessAgency:update")
    public Object updateAgency(@Param("::agency.")BusinessAgency agency){

        NutMap nutMap=new NutMap();
        // 判断code
        boolean b_code=businessAgencyService.eableCode(agency.getCode(),"update",agency.getId());
        if(!b_code){
            return nutMap.setv("ok",false).setv("msg","机构代码重复");
        }
        //判断name
        boolean b_name=businessAgencyService.eableName(agency.getName(),"update",agency.getId());
        if(!b_name){
            return nutMap.setv("ok",false).setv("msg","机构名称重复");
        }
        //保存
        boolean b_saveAgency=businessAgencyService.update(agency);
        if(!b_saveAgency){
            return nutMap.setv("ok",false).setv("msg","修改机构失败.");
        }
        return  nutMap.setv("ok",true).setv("msg","修改机构成功.");
    }
}

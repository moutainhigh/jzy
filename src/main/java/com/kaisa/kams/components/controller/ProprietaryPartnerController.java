package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.service.ProprietaryPartnerService;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.models.ProprietaryPartner;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.pager.Pager;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;


/**
 * Created by luoyj on 2017/4/17.
 */
@IocBean
@At("/proprietary_partner")
public class ProprietaryPartnerController {
    @Inject
    private ProprietaryPartnerService proprietaryPartnerService;

    @At
    @Ok("beetl:/proprietaryPartner/list.html")
    @RequiresPermissions("partnerManager:view")
    public void list(){

    }

    @At("/list_by_partner_name")
    @Ok("json")
    @POST
    @RequiresPermissions("partnerManager:view")
    public Object listBypartnerName(@Param("draw")int draw,@Param("start") int start,@Param("length")int length,@Param("name") String name){
        Pager pager= DataTablesUtil.getDataTableToPager(start,length);
        return proprietaryPartnerService.listByName(name,pager,draw);
    }

    @At("/fetch_by_id")
    @POST
    @Ok("json")
    public Object fetchById(@Param("id") String id){
        NutMap nutMap=new NutMap();
        try{
            nutMap.setv("data",proprietaryPartnerService.fetch(id));
            nutMap.setv("ok",true).setv("msg","成功");
        }catch (Exception e){
            nutMap.setv("ok",false).setv("msg","获取数据异常");
        }
        return nutMap;
    }

    @At("/save_proprietary_partner")
    @Ok("json")
    @POST
    @RequiresPermissions("partnerManager:create")
    public Object save(@Param("::proPartner.")ProprietaryPartner proprietaryPartner){
        NutMap nutMap=new NutMap();
        boolean b_saveChannel=proprietaryPartnerService.insert(proprietaryPartner);
        if(!b_saveChannel){
            return nutMap.setv("ok",false).setv("msg","新增自营合作方失败.");
        }
        return  nutMap.setv("ok",true).setv("msg","新增自营合作方成功.");
    }

    @At("/update_proprietary_partner")
    @Ok("json")
    @POST
    @RequiresPermissions("partnerManager:update")
    public Object update(@Param("::proPartner.")ProprietaryPartner proprietaryPartner) {

        NutMap nutMap=new NutMap();
        boolean b_updateChannel=proprietaryPartnerService.update(proprietaryPartner);
        if(!b_updateChannel){
            return nutMap.setv("ok",false).setv("msg","修改自营合作方失败.");
        }
        return  nutMap.setv("ok",true).setv("msg","修改自营合作方成功.");
    }
}

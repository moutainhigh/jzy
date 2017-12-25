package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.service.MediaTemplateService;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.models.ProductMediaTmpl;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;


/**
 * Created by luoyj on 2016/12/02.
 */
@IocBean
@At("/media_temp")
public class MediaTemplateController {

    @Inject
    private MediaTemplateService mediaTemplateService;

    /**
     * 跳转到影像资料页面
     * @return
     */
    @At
    @Ok("beetl:/mediaTemp/list.html")
    @RequiresPermissions("mediaTemp:view")
    public Context index(){
        Context ctx = Lang.context();
        return ctx;
    }

    /**
     * 跳转到添加影像资料页面
     * @return
     */
    @At
    @Ok("beetl:/mediaTemp/add.html")
    @RequiresPermissions("mediaTemp:create")
    public Context add(){
        Context ctx = Lang.context();
        // 获取最新模板code
        String code=mediaTemplateService.getCode();
        ctx.set("code",code);
        return ctx;
    }

    /**
     * 跳转到编辑影像资料页面
     * @return
     */
    @At
    @Ok("beetl:/mediaTemp/update.html")
    @RequiresPermissions("mediaTemp:update")
    public Context edit(@Param("id")String id){
        Context ctx = Lang.context();
        ctx.set("productMediaTmpl", mediaTemplateService.fetchById(id));
        ctx.set("id",id);
        return ctx;
    }

    /**
     * 跳转到查看影像资料页面
     * @return
     */
    @At
    @Ok("beetl:/mediaTemp/detail.html")
    @RequiresPermissions("mediaTemp:view")
    public Context view(@Param("id")String id){
        Context ctx = Lang.context();
        ProductMediaTmpl p= mediaTemplateService.fetchById(id);
        ctx.set("productMediaTmpl", p);
        ctx.set("creatTime", DateUtil.formatDateTimeToString(p.getCreateTime()));
        if(p.getUpdateTime()!=null){
            ctx.set("updateTime", DateUtil.formatDateTimeToString(p.getUpdateTime()));
        }else {
            ctx.set("updateTime","");
        }
        ctx.set("id",id);
        return ctx;
    }

    @At("/list_media_temp")
    @POST
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("mediaTemp:view")
    public Object listTempByParam(@Param("..")DataTableParam param){
        return mediaTemplateService.queryByParam(param);
    }
    @At("/save_media_temp")
    @POST
    @Ok("json")
    @Aop("auditInterceptor")
    @RequiresPermissions("mediaTemp:create")
    public Object saveMediaTemp(@Param("::mediaTmpl.")ProductMediaTmpl mediaTmpl,@Param("jsonObject")String jsonObject){
        NutMap nutMap =new NutMap();
        //先验证名称是否重复
        if(Strings.isBlank(mediaTmpl.getName())){
            nutMap.setv("ok",false).setv("msg","影像名称不能为空.");
            return nutMap;
        }
        boolean b_name=mediaTemplateService.enableName(mediaTmpl.getName(),"add",mediaTmpl.getId());
        if (b_name==true){
           boolean b_p= mediaTemplateService.add(mediaTmpl,jsonObject);
            if(b_p==true){
                return nutMap.setv("ok",true).setv("msg","新增成功.");
            }
            return nutMap.setv("ok",false).setv("msg","新增失败.");
        }
        return nutMap.setv("ok",false).setv("msg","影像配置名称重复.");
    }
    @At("/update_media_temp")
    @POST
    @Ok("json")
    @Aop("auditInterceptor")
    @RequiresPermissions("mediaTemp:update")
    public Object updateMediaTemp(@Param("::mediaTmpl.")ProductMediaTmpl mediaTmpl,@Param("jsonObject")String jsonObject){
        NutMap nutMap =new NutMap();
        //先验证名称是否重复
        if(Strings.isBlank(mediaTmpl.getName())){
            nutMap.setv("ok",false).setv("msg","影像名称不能为空.");
            return nutMap;
        }
        boolean b_name=mediaTemplateService.enableName(mediaTmpl.getName(),"update",mediaTmpl.getId());
        if (b_name==true){
            boolean b_p= mediaTemplateService.update(mediaTmpl,jsonObject);
            if(b_p==true){
                return nutMap.setv("ok",true).setv("msg","修改成功.");
            }
            return nutMap.setv("ok",false).setv("msg","修改失败.");
        }
        return nutMap.setv("ok",false).setv("msg","影像配置名称重复.");
    }

    @At("/fetch_media_temp")
    @POST
    @Ok("json")
    public Object fetchMediaTemp(@Param("id")String id){
       return  mediaTemplateService.fetchById(id);
    }



    @At("/list_mediaItem")
    @POST
    @Ok("json")
    public Object listMediaItem(@Param("start")int start,@Param("length")int length,@Param("draw")int draw,@Param("id")String id,@Param("type")String type){

        return mediaTemplateService.queryMediaItemBytmId(start,length,draw,id,type);
    }

}

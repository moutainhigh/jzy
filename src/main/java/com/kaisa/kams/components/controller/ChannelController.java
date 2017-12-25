package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.ChannelService;
import com.kaisa.kams.components.service.UserService;
import com.kaisa.kams.enums.ChannelUserType;
import com.kaisa.kams.models.Channel;

import com.kaisa.kams.models.User;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.upload.TempFile;
import org.nutz.mvc.upload.UploadAdaptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Created by pengyueyang on 2017/3/1.
 */
@IocBean
@At("/channel")
public class ChannelController {
    @Inject
    private ChannelService channelService;

    @Inject
    protected UserService userService;

    @At("/get_channels_by_name")
    @Ok("json")
    @POST
    public Object getChannelsByName(@Param("name")String name){
        NutMap nutMap=new NutMap();
        nutMap.setv("data",channelService.findChannelsByName(name));
        nutMap.setv("ok",true).setv("msg","成功");
        return nutMap;
    }

    @At
    @Ok("beetl:/channel/list.html")
    @RequiresPermissions("partnerManager:view")
    public Context list(){
        Context ctx = Lang.context();
        return ctx;
    }

    @At
    @Ok("beetl:/proprietaryPartner/list.html")
    @RequiresPermissions("partnerManager:view")
    public Context partnerList(){
        Context ctx = Lang.context();
        return ctx;
    }
    @At
    @Ok("beetl:/proprietaryPartner/add.html")
    @RequiresPermissions("partnerManager:view")
    public Context partnerAdd(){
        Context ctx = Lang.context();
        return ctx;
    }
    @At
    @Ok("beetl:/proprietaryPartner/edit.html")
    @RequiresPermissions("partnerManager:view")
    public Context partnerEdit(){
        Context ctx = Lang.context();
        return ctx;
    }
    @At
    @Ok("beetl:/proprietaryPartner/view.html")
    @RequiresPermissions("partnerManager:view")
    public Context partnerView(){
        Context ctx = Lang.context();
        return ctx;
    }

    @At
    @Ok("beetl:/channel/add.html")
    public Context add(){
        Context ctx = Lang.context();
        return ctx;
    }
    @At
    @Ok("beetl:/channel/edit.html")
    public Context edit(@Param("id") String id){
        Context ctx = Lang.context();
        ctx.set("id", id);
        return ctx;
    }
    @At
    @Ok("beetl:/channel/view.html")
    public Context view(@Param("id") String id){
        Context ctx = Lang.context();
        ctx.set("id", id);
        return ctx;
    }
    @At
    @Ok("beetl:/channel/upload.html")
    public Context upload(){
        Context ctx = Lang.context();
        return ctx;
    }

    @At("/list_by_channel_name")
    @Ok("json")
    @POST
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("channelManager:view")
    public Object listByChannelName(@Param("..")DataTableParam param){
        //Pager pager= DataTablesUtil.getDataTableToPager(start,length);
        return channelService.listByChannelName(param);
    }

    @At("/fetch_by_id")
    @POST
    @Ok("json")
    public Object fetchById(@Param("id") String id){
        NutMap nutMap=new NutMap();
        try{
            nutMap.setv("data",channelService.fetch(id));
            nutMap.setv("ok",true).setv("msg","成功");
        }catch (Exception e){
            nutMap.setv("ok",false).setv("msg","获取数据异常");
        }
        return nutMap;
    }


    /**
     * 获渠道code
     * @return
     */
    @At("/get_channel_code")
    @Ok("json")
    @POST
    public Object getChannelCode(){
        String code=channelService.getCode();
        if(!Strings.isBlank(code)){
            return new NutMap().setv("data",code).setv("ok",true);
        }
        return  new NutMap().setv("ok",false).setv("msg","获取数据异常.");
    }

    @At("/save_channel")
    @Ok("json")
    @POST
    @RequiresPermissions("channelManager:create")
    public Object save(@Param("::channel.")Channel channel){
        NutMap nutMap=new NutMap();
        boolean b_saveChannel=channelService.insert(channel);
        if(!b_saveChannel){
            if(channel.getChannelType().equals("1")){
                return nutMap.setv("ok",false).setv("msg","新增渠道失败.");
            }else {
                return nutMap.setv("ok",false).setv("msg","新增自营合作方失败.");
            }
        }
        if(channel.getChannelType().equals("1")){
            return  nutMap.setv("ok",true).setv("msg","新增渠道成功.");
        }else {
            return  nutMap.setv("ok",true).setv("msg","新增自营合作方成功.");
        }
    }

    @At("/update_channel")
    @Ok("json")
    @POST
    @RequiresPermissions("channelManager:update")
    public Object update(@Param("::channel.")Channel channel) {

        NutMap nutMap=new NutMap();
        boolean b_updateChannel=channelService.update(channel);
        if(!b_updateChannel){
            if(channel.getChannelType().equals("1")){
                return nutMap.setv("ok",false).setv("msg","修改渠道失败.");
            }else {
                return nutMap.setv("ok",false).setv("msg","修改自营合作方失败.");
            }
        }
        if(channel.getChannelType().equals("1")){
            return  nutMap.setv("ok",true).setv("msg","修改渠道成功.");
        }else {
            return  nutMap.setv("ok",true).setv("msg","修改自营合作方成功.");
        }
    }
    @At("/list_channel_name")
    @Ok("json")
    @POST
    public Object listChannelName(@Param("channelName") String channelName,@Param("channelType") String channelType){
        NutMap nutMap=new NutMap();
        return  nutMap.setv("data", channelService.listChannelName(channelName,channelType));
    }
    @At("/upload")
    @Ok("json")
    @POST
    @AdaptBy(type = UploadAdaptor.class, args = { "/data0/java/uploadTemp", "8192", "UTF-8", "10" })
    public Object upload(@Param("Filedata")TempFile file, HttpServletRequest request, HttpServletResponse response){
        NutMap nutMap=new NutMap();
        return  nutMap.setv("result",channelService.readExcelFile(file));
    }

    /**
     * 根据输入的渠道名查询业务来源
     */
    @At("/list_channel_names")
    @Ok("json")
    public NutMap listChannelName(@Param("channelName") String channelName,
                                  @Param("channelType") String channelType,
                                  @Param("cooperationProductType") String cooperationProductType){
        NutMap nutMap = new NutMap();
        String userId = ShiroSession.getLoginUser().getId();
        User u = userService.fetchById(userId);
        if(ChannelUserType.CHANNEL_USER.equals(u.getType())){
           return  nutMap.setv("data",channelService.listChannelNameForChannel(channelName,channelType,cooperationProductType,u));
        }
        return nutMap.setv("data",channelService.listChannelName(channelName,channelType,cooperationProductType));
    }
}

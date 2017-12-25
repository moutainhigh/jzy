package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.BusinessUserService;
import com.kaisa.kams.components.service.UserService;
import com.kaisa.kams.enums.CertificateType;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.enums.UserType;
import com.kaisa.kams.models.User;
import com.kaisa.kams.models.business.BusinessUser;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;


/**
 * Created by luoyj on 2016/12/06
 */
@IocBean
@At("/business_user")
public class BusinessUserController {
    private static final Log log = Logs.get();

    @Inject
    private BusinessUserService businessUserService;


    @Inject
    private UserService userService;
    /**
     * 跳转到人员页面
     * @return
     */
    @At
    @Ok("beetl:/business/user/user_index.html")
    @RequiresPermissions("businessPersonnel:view")
    public Context index(){
        Context ctx = Lang.context();
        //获取菜单
        ctx.set("menus",ShiroSession.getMenu());
        return ctx;
    }

    /**
     * 查询业务人员列表，分页（包含组织和机构信息）
     * @param param
     * @return
     */
    @At("/list_user")
    @Ok("json")
    @POST
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("businessPersonnel:view")
    public Object listUser(@Param("..")DataTableParam param){
        return businessUserService.queryParam(param);
    }

    /**
     * 通过code/name 模糊查询业务人员信息
     * @param search
     * @return
     */
    @At("/list_user_search")
    @Ok("json")
    @POST
    public  Object listUserBySearch(@Param("search")String search){
        NutMap nutMap=new NutMap();
        try{
            log.info("search:"+search);
            nutMap.setv("data",businessUserService.queryBySearch(search));
            nutMap.setv("ok",true).setv("msg","成功");
        }catch (Exception e){
            log.error(e.getMessage());
            nutMap.setv("ok",false).setv("msg","获取数据异常");
        }
        return nutMap;
    }

    /**
     * 通过id查询业务人员信息
     * @param id
     * @return
     */
    @At("/fetch_user")
    @Ok("json")
    @POST
    @RequiresPermissions("businessPersonnel:view")
    public Object fetchUser(@Param("id")String id){
        NutMap nutMap=new NutMap();
        try{
            log.info("id:"+id);
            nutMap.setv("data",businessUserService.fetchById(id));
            nutMap.setv("ok",true).setv("msg","成功");
        }catch (Exception e){
            log.error(e.getMessage());
            nutMap.setv("ok",false).setv("msg","获取数据异常");
        }
        return nutMap;
    }


    @At("/create_user_account")
    @Ok("json")
    @POST
    @RequiresPermissions("businessPersonnel:view")
    public Object createUserAccount(@Param("id")String id){
        NutMap nutMap=new NutMap();
        BusinessUser businessUser  = businessUserService.fetchById(id);
        if (StringUtils.isNotEmpty(businessUser.getUserId())){
            return nutMap.setv("ok", false).setv("msg", "已创建账号，不要重复创建");
        }
        User user  = new User();
        user.setName(businessUser.getName());
        user.setAddress(businessUser.getAddress());
        if(businessUser.getBirthday()==null){
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            try {
                user.setBirthDate(formatter.parse(businessUser.getIdNumber().substring(6,14)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else{
            user.setBirthDate(businessUser.getBirthday());
        }
        user.setCertificateNumber(businessUser.getIdNumber());
        user.setCertificateType(CertificateType.ID);
        user.setCompanyStatus(true);
        user.setEmail(businessUser.getEmail());
        user.setInCompanyDate(businessUser.getEntryDate());
        //这里没有账号，暂时采用手机号码作为用户登陆的账号
        user.setLogin("zc_"+businessUser.getMobile());
        user.setMobile(businessUser.getMobile());
        user.setOrganizeId(businessUser.getOrganizeId());
        user.setOrgName(businessUser.getOrganize().getName());
        user.setSex(businessUser.getSex()==1?"男":"女");
        user.setStatus(PublicStatus.ABLE);
        user.setUserType(UserType.BUSINESS_USER);
        String msg =  userService.add(user);
        if (StringUtils.isNotEmpty(msg) && !msg.startsWith("SUCCESS")) {
            return nutMap.setv("ok", false).setv("msg", msg);
        }
        String msgArray[] = msg.split(":");
        if (!ArrayUtils.isEmpty(msgArray) && msgArray.length==2) {
            businessUser.setUserId(msgArray[1]);
            boolean updateUserIdResult = businessUserService.updateUserId(businessUser);
            if (!updateUserIdResult) {
                return nutMap.setv("ok", false).setv("msg", "新增业务人员登陆账号失败-更新userId错误");
            }
        }

        return nutMap.setv("ok", true).setv("msg", "新增业务人员登陆账号成功");
    }

    /**
     * 新增业务员人员
     * @param businessUser
     * @return
     */
    @At("/save_user")
    @Ok("json")
    @POST
    @Aop("auditInterceptor")
    @RequiresPermissions("businessPersonnel:create")
    public Object saveUser(@Param("::user.")BusinessUser businessUser){
        NutMap nutMap=new NutMap();
        boolean b_saveUser=businessUserService.add(businessUser);
        if(!b_saveUser){
            return nutMap.setv("ok",false).setv("msg","新增业务人员失败.");
        }
        return  nutMap.setv("ok",true).setv("msg","新增业务人员成功.");
    }

    /**
     * 修改业务员人员信息
     * @param businessUser
     * @return
     */
    @At("/update_user")
    @Ok("json")
    @POST
    @Aop("auditInterceptor")
    @RequiresPermissions("businessPersonnel:update")
    public  Object updateUser(@Param("::user.")BusinessUser businessUser){
        NutMap nutMap=new NutMap();
        BusinessUser oldBusinessUser = businessUserService.fetchById(businessUser.getId());
        if (null == oldBusinessUser) {
            return nutMap.setv("ok",false).setv("msg","修改业务人员失败-不存在此业务人员.");
        }
        if (StringUtils.isNotEmpty(oldBusinessUser.getUserId()) && isChangeMobileOrIdNumber(oldBusinessUser,businessUser)) {
            User user = new User();
            user.setId(oldBusinessUser.getUserId());
            user.setMobile(businessUser.getMobile());
            if (!oldBusinessUser.getMobile().equals(businessUser.getMobile())) {
                user.setLogin("zc_"+businessUser.getMobile());
            }
            user.setCertificateNumber(businessUser.getIdNumber());
            String msg = userService.updateUserMobileAndIdNumber(user);
            if (StringUtils.isNotEmpty(msg)) {
                return nutMap.setv("ok",false).setv("msg","同步更新用户信息失败："+msg);
            }
        }


        boolean result=businessUserService.update(businessUser);
        if(!result){
            return nutMap.setv("ok",false).setv("msg","修改业务人员失败.");
        }
        return  nutMap.setv("ok",true).setv("msg","修改业务人员成功.");
    }

    private boolean isChangeMobileOrIdNumber(BusinessUser oldBusinessUser, BusinessUser businessUser) {
        if (!oldBusinessUser.getMobile().equals(businessUser.getMobile())) {
            return true;
        }
        if (!oldBusinessUser.getIdNumber().equals(businessUser.getIdNumber())) {
            return true;
        }
        return false;
    }

    /**
     * 获取业务员职位list
     * @return
     */
    @At("/get_user_position")
    @Ok("json")
    @POST
    public Object getUserPosition()
    {
        List<NutMap> list=businessUserService.getPosition();
        if(list.size()>0){
            return new NutMap().setv("data",list).setv("ok",true);
        }
        return  new NutMap().setv("ok",false).setv("msg","获取数据异常.");
    }

    /**
     * 获取业务员code
     * @return
     */
    @At("/get_user_code")
    @Ok("json")
    @POST
    public Object getUserCode(){

        String code=businessUserService.getUserCode();
        if(!Strings.isBlank(code)){
            return new NutMap().setv("data",code).setv("ok",true);
        }
        return  new NutMap().setv("ok",false).setv("msg","获取数据异常.");
    }
    /**
     * 获取证件类型
     * @return
     */
    @At("/get_user_credentials")
    @Ok("json")
    @POST
    public Object getUserCredentialsType(){
        List<NutMap> list=businessUserService.getUserCredentialsType();
        if(list.size()>0){
            return new NutMap().setv("data",list).setv("ok",true);
        }
        return  new NutMap().setv("ok",false).setv("msg","获取数据异常.");
    }

}

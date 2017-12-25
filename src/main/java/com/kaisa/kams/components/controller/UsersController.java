package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.ChannelService;
import com.kaisa.kams.components.service.MenuService;
import com.kaisa.kams.components.service.ProductService;
import com.kaisa.kams.components.service.UserService;
import com.kaisa.kams.components.utils.EndecryptUtils;
import com.kaisa.kams.models.Channel;
import com.kaisa.kams.models.Product;
import com.kaisa.kams.models.User;

import nl.captcha.Captcha;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户控制层
 * Created by weid on 2016/11/17.
 */
@IocBean
@At("/user")
public class UsersController {

    @Inject
    private UserService userService;

    @Inject
    private MenuService menuService;

    @Inject
    private ChannelService channelService;

    @Inject
    private ProductService productService;

    /**
     * 跳转到登录页面
     * @return
     */
    @At
    @Ok("beetl:${obj}")
    @Filters
    public String toLogin(HttpServletRequest request,HttpServletResponse response){
        response.setHeader("sessionStatus","false");
        if (request.getHeader("User-Agent").toLowerCase().contains("mobile")){
            return "/h5/login.html";
        }
        if (!request.getHeader("User-Agent").toLowerCase().contains("chrome")) {
            return "/users/chrome.html";
        }
        return "/users/login.html";
    }

    /**
     * 登录
     * @return
     */
    @At
    @POST
    @Ok("json")
    @Filters
    public NutMap checkLogin(@Param("login")String login,
                             @Param("password")String password){
        NutMap nutMap=new NutMap();
        nutMap.put("ok", false);
        if (StringUtils.isEmpty(login) || StringUtils.isEmpty(password)){
            nutMap.put("message", "账号或密码不能为空");
            return nutMap;
        }
        User user = userService.fetch(login,password);
        if (null == user){
            nutMap.put("message", "账号或密码错误");
            return nutMap;
        }
        nutMap.put("ok", true);
        return nutMap;
    }

    /**
     * 检测用户是否登录
     * @return
     */
    @At
    @Ok("redirect:${obj==true?'/user/home':'/user/toLogin'}")
    @Filters
    public boolean login(@Param("::user.")User user){

        if(checkUser(user)){
            return false;
        }

        User currentUser = userService.fetch(user.getLogin(),user.getPassword());
        if (null==currentUser){
            return false;
        }

        if (!userService.loginByShiro(currentUser)) {
            return false;
        }

        List<Map> menus = menuService.drawMenu(currentUser);
        ShiroSession.setMenu(menus);
        return true;
    }

    private boolean checkUser(User user) {
        if (null == user) {
            return true;
        }
        if (StringUtils.isEmpty(user.getLogin())) {
            return true;
        }
        if (StringUtils.isEmpty(user.getPassword())) {
            return true;
        }
        return false;
    }

    /**
     * 跳转到登陆后主页
     * @return
     */
    @At
    @Ok("beetl:/index.html")
    public void home(){}


    /**
     * 用户登录
     * @return
     */
    @At
    @Ok("redirect:/user/toLogin")
    public void logout(){
        Subject subject = ShiroSession.getSubject();
        if (subject.isAuthenticated()) {
            subject.logout();
        }
    }

    /**
     * 跳转找回密码页面
     * @return
     */
    @At
    @Ok("beetl:/users/findPassword.html")
    @Filters
    public void toFindPassword(){}

    /**
     * 找回密码-发送新密码到注册手机号
     * @return
     */
    @At
    @POST
    @Ok("json")
    @Aop("auditInterceptor")
    @Filters
    public Object findPassword(@Param("login")String login,
                               @Param("imageCaptcha")String imageCaptcha){
        NutMap nutMap=new NutMap();
        nutMap.put("ok",false);
        if(StringUtils.isEmpty(login)||StringUtils.isEmpty(imageCaptcha)){
            nutMap.put("message","用户名、手机号或者验证码不能为空");
            return nutMap;
        }
        //验证码校验
        Captcha captcha=(Captcha)ShiroSession.getAttribute(Captcha.NAME);
        if(!imageCaptcha.equals(captcha.getAnswer())) {
            nutMap.put("message","验证码输入有误");
            return nutMap;
        }

        User currentUser=userService.fetchByNameOrMobile(login);
        if(currentUser==null) {
            nutMap.put("message","账号不存在");
            return nutMap;
        }
        boolean result = userService.updateUserPassword(currentUser);
        if (result){
            nutMap.put("ok",true);
            nutMap.put("message","找回密码成功，新密码已经发送到尾号为"+currentUser.getMobile().substring(currentUser.getMobile().length()-4)+"的注册手机上");
        }else{
            nutMap.put("message","修改密码失败");
        }
        return nutMap;
    }

    /**
     * 跳转重置密码页面
     * @return
     */
    @At
    @Ok("beetl:/users/resetPassword.html")
    public void toResetPassword(){}

    /**
     * 重置密码
     * @return
     */
    @At
    @POST
    @Ok("json")
    @Aop("auditInterceptor")
    public NutMap resetPassword(@Param("oldPassword")String oldPassword,
                        @Param("newPassword")String newPassword){
        NutMap nutMap=new NutMap();
        nutMap.put("ok",false);
        if(StringUtils.isEmpty(oldPassword)||StringUtils.isEmpty(newPassword)){
           nutMap.put("message","数据验证失败");
           return nutMap;
        }

        if (!EndecryptUtils.md5Encrypt(oldPassword).equals(ShiroSession.getLoginUser().getPassword())){
            nutMap.put("message","原密码输入错误");
            return nutMap;
        }

       boolean flag = userService.resetPassword(ShiroSession.getLoginUser().getId(),newPassword);
        if (flag){
            nutMap.put("ok",true);
            nutMap.put("message","重置密码成功");
        }else{
            nutMap.put("message","重置密码失败");
        }
        return nutMap;
    }

    /**
     * 跳转到用户维护页面
     * @return
     */
    @At
    @RequiresPermissions("user:view")
    @Ok("beetl:/users/index.html")
    public void index(){

    }

    /**
     * 新增用户
     * @param user
     * @return
     */
    @At("/save_user")
    @POST
    @Ok("json")
    @Aop("auditInterceptor")
    @RequiresPermissions("user:create")
    public NutMap saveUser(@Param("::user.")User user){
        NutMap nutMap=new NutMap();
        String result = userService.add(user);
        if (StringUtils.isNotEmpty(result) && !result.startsWith("SUCCESS")) {
            return nutMap.setv("ok", false).setv("msg", result);
        }
        return nutMap.setv("ok", true).setv("msg", "添加成功");
    }

    /**
     * 修改用户信息
     * @param user
     * @return
     */
    @At("/update_user")
    @POST
    @Ok("json")
    @Aop("auditInterceptor")
    @RequiresPermissions("user:update")
    public NutMap updateUser(@Param("::user.")User user){
        NutMap nutMap=new NutMap();
        String result = userService.update(user);
        if (StringUtils.isEmpty(result)) {
            return nutMap.setv("ok", true).setv("msg", "修改成功");
        }
        return nutMap.setv("ok", false).setv("msg", result);
    }

    /**
     * 通过id查询某用户详情
     * @param id
     * @return
     */
    @At("/fetch_user")
    @Ok("json")
    @POST
    public NutMap fetchUserById(@Param("id")String id){
        NutMap nutMap=new NutMap();
        User user=userService.fetchLinksById(id);
        return  nutMap.setv("data",user);
    }


    @At("/list_user")
    @POST
    @RequiresPermissions("user:view")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    public Object listUserByParam(@Param("..")DataTableParam param){
        return  userService.queryByParam(param);
    }

    /**
     * 查询所有有效人员信息
     * @return
     */
    @At("/list_all")
    @Ok("json")
    @POST
    public Object listAll(){
        Map<String,Object> map=new HashMap<>();
        List<User> listData=userService.queryAllValid();
        map.put("data",listData);
        return map;
    }

    /**
     * 获取渠道
     * @param
     * @return
     */
    @At("/get_channel")
    @Ok("json")
    public NutMap getChannel(){
        NutMap result=new NutMap();
        List<Channel> channelList = channelService.listAbleByType();
        result.put("channelList",channelList);
        return result;
    }

    /**
     * 获取产品
     * @param
     * @return
     */
    @At("/get_product")
    @Ok("json")
    public NutMap getProduct(){
        NutMap result=new NutMap();
        List<Product> productList = productService.queryListAll();
        result.put("productList",productList);
        return result;
    }
}

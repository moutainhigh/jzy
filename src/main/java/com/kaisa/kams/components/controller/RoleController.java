package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.MenuService;
import com.kaisa.kams.components.service.RoleService;
import com.kaisa.kams.components.service.UserService;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.Borrower;
import com.kaisa.kams.models.Menu;
import com.kaisa.kams.models.Role;
import com.kaisa.kams.models.User;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by wangj on 2016/11/25
 */
@IocBean
@At("/role")
public class RoleController {

    @Inject
    private RoleService roleService;
    @Inject
    private MenuService menuService;
    @Inject
    private UserService userService;

    /**
     * 跳转到角色管理页面
     * @return
     */
    @At
    @RequiresPermissions("role:view")
    @Ok("beetl:/role/index.html")
    public Context index(){
        Context ctx = Lang.context();  //返回的Map
        ctx.set("menus",ShiroSession.getMenu());  //加载菜单
        return ctx;
    }

    /**
     * 查询所有效有角色信息
     * @return
     */
    @At
    @Ok("json")
    @POST
    public Object list(){
        Map<String,Object> map=new HashMap<String,Object>();
        List<Role> listData=roleService.queryAllValid();
        map.put("data",listData);
        return map;
    }

    /**
     * 查询角色信息，分页（包含菜单权限）
     * @param param
     * @return
     */
    @At("/get_table")
    @Ok("json")
    @POST
    @AdaptBy(type=JsonAdaptor.class)
    public Object getTableByParam(@Param("..")DataTableParam param){
        return roleService.getTableByParam(param);
    }

    /**
     * 查询角色信息，分页（包含菜单权限）
     * @param name
     * @return
     */
    @At("/list_by_name")
    @Ok("json")
    @POST
    public Object listByName(@Param("name")String name){
        NutMap result = new NutMap();
        if(StringUtils.isEmpty(name)){
            result.put("ok",false);
            result.put("msg","角色名称不能为空");
            return result;
        }

        List<Role> borrowerList =roleService.listByName(name);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",borrowerList);
        return result;
    }

    /**
     * 查询全部菜单信息(新增使用)
     * @return
     */
    @At("/menu_list_all")
    @Ok("json")
    @POST
    public Object menuList(){
        Map<String,Object> map=new HashMap<String,Object>();
        List<Menu> listData=menuService.queryMenuList();
        map.put("data",listData);
        return map;
    }

    /**
     * 按角色Id查询菜单信息
     * @return
     */
    @At("/menu_list_by_role_id")
    @Ok("json")
    @POST
    public Object roleMenuList(@Param("id") String id){
        Map<String,Object> map=new HashMap<String,Object>();
        List<Map> listData=menuService.queryMenuListByRoleId(id);
        map.put("data",listData);
        return map;
    }

    /**
     * 按角色Id查询人员信息
     * @return
     */
    @At("/user_list_by_role_id")
    @Ok("json")
    @POST
    public Object userList(@Param("id") String id){
        Map<String,Object> map=new HashMap<String,Object>();
        List<Map> listData=userService.queryUserListByRoleId(id);
        map.put("data",listData);
        return map;
    }


    /**
     * 新增角色
     * @param role
     * @param menuIds
     * @param permissions
     * @return
     */
    @At
    @Ok("json")
    @Aop("auditInterceptor")
    @POST
    @RequiresPermissions("role:create")
    public Object add(@Param("::role.")Role role,@Param("menuIds") String menuIds,@Param("permissions") String permissions){
        NutMap nutMap=new NutMap();
        String[] id=menuIds.split(",");
        String[] permission=permissions.split(",");
        String userId=ShiroSession.getLoginUser().getId()+"";
        role.setCreateTime(new Date());
        role.setCreateBy(userId);
        role.setStatus(PublicStatus.ABLE);
        Map result=roleService.add(role,userId,id,permission);
        if((boolean)result.get("ok"))
            return nutMap.setv("ok", result.get("ok")).setv("msg", result.get("msg")).setv("roleName",result.get("roleName"));
        else
            return nutMap.setv("ok", result.get("ok")).setv("msg", result.get("msg"));
    }

    /**
     * 更新角色信息
     * @param role
     * @param menuIds
     * @param permissions
     * @return
     */
    @At
    @Ok("json")
    @Aop("auditInterceptor")
    @POST
    @RequiresPermissions("role:update")
    public Object update(@Param("::role.")Role role,@Param("menuIds") String menuIds,@Param("permissions") String permissions){
        NutMap nutMap=new NutMap();
        String userId=ShiroSession.getLoginUser().getId()+"";
        String[] id=menuIds.split(",");
        String[] permission=permissions.split(",");
        role.setUpdateTime(new Date());
        role.setUpdateBy(userId);
        Map result=roleService.update(role,userId,id,permission);
        return nutMap.setv("ok", result.get("ok")).setv("msg", result.get("msg"));
    }

    /**
     * 更新角色下的用户信息
     * @param role
     * @param userIds
     * @return
     */
    @At("/update_user")
    @Ok("json")
    @Aop("auditInterceptor")
    @POST
    @RequiresPermissions("role:update")
    public Object updateUser(@Param("::role.")Role role,@Param("userIds") String userIds){
        NutMap nutMap=new NutMap();
        String userId=ShiroSession.getLoginUser().getId()+"";
        role.setUpdateTime(new Date());
        role.setUpdateBy(userId);
        Map result=roleService.updateUser(role,userId,userIds);
        return nutMap.setv("ok", result.get("ok")).setv("msg", result.get("msg"));
    }

    /**
     * 删除角色的菜单权限
     * @param id
     * @param menuId
     * @return
     */
    @At("/delete_role_menu")
    @Ok("json")
    @Aop("auditInterceptor")
    @POST
    @RequiresPermissions("role:delete")
    public Object deleteRoleMenu(@Param("id")String id,@Param("menuId")String menuId) {
        NutMap nutMap=new NutMap();
        int num=roleService.deleteRoleMenu(id,menuId);
        if(num>0){
            return nutMap.setv("ok",true).setv("msg","删除成功.");
        }
        return nutMap.setv("ok",false).setv("msg","删除失败.");
    }

    /**
     * 删除角色信息
     * @param id
     * @return
     */
    @At
    @Ok("json")
    @Aop("auditInterceptor")
    @POST
    @RequiresPermissions("role:delete")
    public Object delete(@Param("id")String id) {
        NutMap nutMap=new NutMap();
        Role role=roleService.fetchLinksById(id);
        boolean bz=false;
        if(role.getUsers().size()>0){
            for(User u:role.getUsers()){
                if(PublicStatus.ABLE.equals(u.getStatus())){
                    bz=true;
                    break;
                }
            }
        }
        if(bz) {
            return nutMap.setv("ok", false).setv("msg", "删除失败，该角色下存在有效用户信息.");
        }
        int num=roleService.delete(id);
        if(num>0){
            return nutMap.setv("ok",true).setv("msg","删除成功.");
        }
        return nutMap.setv("ok",false).setv("msg","删除失败.");
    }

//    @At
//    public Object query(@Param("name")String name, @Param("..")Pager pager) {
//        Cnd cnd = Strings.isBlank(name)? null : Cnd.where("name", "like", "%"+name+"%");
//        QueryResult qr = new QueryResult();
//        qr.setList(dao.query(User.class, cnd, pager));
//        pager.setRecordCount(dao.count(User.class, cnd));
//        qr.setPager(pager);
//        return qr; //默认分页是第1页,每页20条
//    }
    @At
    @GET
    public boolean hasPromise(@Param("promise")String promise){
        return menuService.findMenuByPromise(promise,ShiroSession.getLoginUser().getId())>0;
    }
}

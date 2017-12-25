package com.kaisa.kams.components.service;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Cnd;
import org.nutz.dao.pager.Pager;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.util.*;

/**
 * 角色管理服务层
 * Created by wangj on 2016/11/25.
 */
@IocBean(fields = "dao")
public class RoleService extends IdNameEntityService<Role> {
    private static final Log log = Logs.get();
    @Inject
    private MenuService menuService;

    /**
     * 新增角色信息
     *
     * @param role       角色id
     * @param userId     用户id
     * @param menuId     菜单权限
     * @param permission 菜单明细权限
     * @return
     */
    @Aop(TransAop.READ_COMMITTED)
    public Map add(Role role, String userId, String[] menuId, String[] permission) {
        Map result = new HashMap();
        //判断角色名是否重复
        Role rs = dao().fetch(Role.class, Cnd.where("name", "=", role.getName()).and("status", "=", PublicStatus.ABLE));
        if (rs != null) {
            result.put("ok", false);
            result.put("msg", "角色名重复,新增失败.");
            return result;
        }
        //add
        try {
            Role Or = dao().insert(role);
            if (Or != null) {
                //角色菜单批量插入
                //List<RoleMenu> roleMenuList = constructRoleMenu(role.getId(), userId, menuId, permission,);
                //改为最新的封roleMenu方法，里面包含了权限按钮的封装
                List<RoleMenu> roleMenuList = getRoleMenus(role, userId, menuId, permission);
                dao().fastInsert(roleMenuList);
                result.put("ok", true);
                result.put("msg", "新增成功.");
                result.put("roleName", Or.getName());
                return result;
            } else {
                result.put("ok", false);
                result.put("msg", "新增失败.");
                return result;
            }
        } catch (Exception e) {
            log.error("failed to add in RoleService,exception={}", e);
            result.put("ok", false);
            result.put("msg", "新增失败,系统异常.");
            return result;
        }
    }

    /**
     * 查询所有有效角色信息
     *
     * @return
     */
    public List<Role> queryAllValid() {
        List<Role> list = dao().query(Role.class, Cnd.where("status", "=", PublicStatus.ABLE));
        return list;
    }

    /**
     * 通过roleId查询角色的用户信息
     *
     * @param id
     * @return
     */
    public Role fetchLinksById(String id) {
        return dao().fetchLinks(dao().fetch(Role.class, id), "users");
    }

    /**
     * 根据查询输入名称 查询角色信息
     *
     * @param param
     * @return
     */
    public DataTables getTableByParam(DataTableParam param) {
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());

        String name = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            name = keys.get("name");
        }
        List<Role> uList = new ArrayList<>();
        Cnd cnd;
        DataTables dataTables;
        try {
            log.info("roleservice queryByName param: start: name:" + name);
            if ("".equals(name)) {
                cnd = Cnd.where("status", "=", PublicStatus.ABLE);
            } else {
                cnd = Cnd.where("status", "=", PublicStatus.ABLE).and("name", "like", "%" + name + "%");
            }

            //Pager pager = DataTablesUtil.getDataTableToPager(start, length);
            List<Role> userList = dao().query(Role.class, cnd, pager);
            for (Role r : userList) {
                r.setMenus(menuService.queryMenuListByRoleId(r.getId()));
                uList.add(r);
            }
            dataTables = new DataTables(param.getDraw(), dao().count(Role.class), dao().count(Role.class, cnd), uList);
            dataTables.setOk(true);
            dataTables.setMsg("成功");
        } catch (Exception e) {
            log.error("fail to queryByName in RoleService,exception={}" + e);
            dataTables = new DataTables(0, 0, 0, null);
            dataTables.setOk(false);
            dataTables.setMsg("获取数据失败.");
        }
        return dataTables;
    }

    /**
     * 修改角色信息
     *
     * @param role       角色id
     * @param userId     用户id
     * @param menuId     菜单id
     * @param permission 菜单明细权限
     * @return
     */
    @Aop(TransAop.READ_COMMITTED)
    public Map update(Role role, String userId, String[] menuId, String[] permission) {
        Map result = new HashMap();
        //判断角色名是否重复
        Role rs = dao().fetch(Role.class, Cnd.where("name", "=", role.getName()).and("status", "=", PublicStatus.ABLE));
        if (rs != null) {
            if (!rs.getId().equals(role.getId())) {
                result.put("ok", false);
                result.put("msg", "角色名重复,修改失败.");
                return result;
            }
        }

        try {
            int i = dao().update(role, "^(name|updateTime|updateBy)$");
            if (i > 0) {
                //改为最新的封roleMenu方法，里面包含了权限按钮的封装
                List<RoleMenu> roleMenuList = getRoleMenus(role, userId, menuId, permission);
                //角色菜单批量更新
                dao().clear(RoleMenu.class, Cnd.where("roleId", "=", role.getId()));
                dao().fastInsert(roleMenuList);
                result.put("ok", true);
                result.put("msg", "修改成功.");
                return result;
            } else {
                result.put("ok", false);
                result.put("msg", "修改失败.");
                return result;
            }
        } catch (Exception e) {
            log.error("fail to update in RoleService,exception={}" + e);
            result.put("ok", false);
            result.put("msg", "修改失败,系统异常.");
            return result;
        }
    }

    private List<RoleMenu> getRoleMenus(Role role, String userId, String[] menuId, String[] permission) {
        List<Menu> buttonList = new ArrayList<>();
        List<Menu> allButtonList  = menuService.queryAllButton();
        List<String> menuList = new ArrayList<String>();
        //分离开menu和button
        for(String menuid : menuId){
            Menu button = getButtonFromMenu(menuid,allButtonList);
            if(button!=null){
                buttonList.add(button);
            }else{
                menuList.add(menuid);
            }
        }
        //把button合并到父类menu里面去然后返回所有父类
        Map<String,Menu> menuIdMap = mergeButtonInMenu(buttonList);
        return constructRoleMenu(role.getId(), userId, menuList, permission,menuIdMap);
    }

    /**
     * 更新角色下的用户信息
     *
     * @param role    角色id
     * @param userId  当前用户id
     * @param userIds 用户id
     * @return
     */
    @Aop(TransAop.READ_COMMITTED)
    public Map updateUser(Role role, String userId, String userIds) {
        Map result = new HashMap();
        // 清除相对应的角色
        if ("".equals(userIds)) {
            try {
                dao().clearLinks(role, "users");
                result.put("ok", true);
                result.put("msg", "保存成功.");
                return result;
            } catch (Exception e) {
                log.error("fail to updateUser in RoleService,exception={}" + e);
                result.put("ok", false);
                result.put("msg", "保存失败,系统异常.");
                return result;
            }
        }
        String[] id = userIds.split(",");
        List<User> userList = new ArrayList<User>(id.length);
        try {
            //构造users
            for (String uid : id) {
                User u = new User();
                u.setId(uid);
                userList.add(u);
            }
            role.setUsers(userList);
            // 清除相对应的角色，新增修改角色
            dao().clearLinks(role, "users");
            // 关联新的用户角色
            Role r = dao().insertRelation(role, "users");
            if (r != null) {
                result.put("ok", true);
                result.put("msg", "保存成功.");
                return result;
            } else {
                result.put("ok", false);
                result.put("msg", "保存失败.");
                return result;
            }
        } catch (Exception e) {
            log.error("fail to updateUser in RoleService,exception={}" + e);
            result.put("ok", false);
            result.put("msg", "保存失败,系统异常.");
            return result;
        }
    }

    /**
     * 删除角色
     *
     * @param id
     * @return
     */
    @Aop(TransAop.READ_COMMITTED)
    public int delete(String id) {
        int i = dao().delete(Role.class, id);
        if (i > 0)
            //角色菜单批量删除
            dao().clear(RoleMenu.class, Cnd.where("roleId", "=", id));
        return i;
    }

    /**
     * 删除角色的菜单权限
     *
     * @param id     角色id
     * @param menuId 菜单id
     * @return
     */
    public int deleteRoleMenu(String id, String menuId) {
        int i = dao().clear(RoleMenu.class, Cnd.where("roleId", "=", id).and("menuId", "=", menuId).and("status", "=", PublicStatus.ABLE));
        return i;
    }

    /**
     * 构造List<RoleMenu> permission和menuId数组长度必须一致
     *
     * @param roleId     角色id
     * @param userId     用户id
     * @param menuList     菜单id
     * @param permission 菜单明细权限
     */
    protected List constructRoleMenu(String roleId, String userId, List<String>  menuList, String[] permission,Map<String,Menu> menuIdMap) {
        List<RoleMenu> roleMenuList = new ArrayList<>();
        for (int i = 0; i < menuList.size(); i++) {
            String menuId = menuList.get(i);
            RoleMenu tmp = new RoleMenu();
            tmp.setId(tmp.uuid());
            tmp.setRoleId(roleId);
            tmp.setMenuId(menuId);
            tmp.setPermission(permission[i]);
            tmp.setStatus(PublicStatus.ABLE);
            tmp.setCreateBy(userId);
            if(menuIdMap.get(menuId)!=null){
                tmp.setButtonPermission(getButtonPermission(menuIdMap.get(menuId)));
            }
            tmp.setCreateTime(new Date());
            tmp.setUpdateBy(userId);
            tmp.setUpdateTime(new Date());
            roleMenuList.add(tmp);
        }
        return roleMenuList;
    }

    private String getButtonPermission(Menu menu){
        String permission="";
        if(CollectionUtils.isNotEmpty(menu.getButtonList())){
            for(Menu button : menu.getButtonList()){
                permission += ","+button.getAlias();
            }
        }
        if(permission.length()>0){
            permission = permission.substring(1);
        }
        return permission;
    }
    private Map<String,Menu> mergeButtonInMenu(List<Menu> buttonList){
        Map<String,Menu> map = new HashMap<String,Menu>();
        List<Menu> list = new ArrayList<Menu>();
        for(Menu button : buttonList){
            Menu parent  = map.get(button.getParentId());
            if(parent==null){
                parent = button.getParent();
                //把父类置为空，减少关联依赖，改为父类对多个子类
                button.setParent(null);
                map.put(button.getParentId(),parent);
            }
            parent.addButton(button);
        }
        return map;
    }

    private Menu getButtonFromMenu(String menuId,List<Menu> buttonList){
        Menu menu = null;
        for(Menu m : buttonList){
            if(menuId.equalsIgnoreCase(m.getId())){//如果是一个按钮
                menu = m;
                Menu parent = dao().fetch(Menu.class,Cnd.where("parentId","=",m.getParentId()));
                menu.setParent(parent);
                break;
            }
        };
        return menu;
    }

    public List<Role> listByName(String name) {
        Cnd cnd = Cnd.where("status", "=", PublicStatus.ABLE).and("name", "like", "%" + name + "%");
        return dao().query(Role.class, cnd);
    }

    //删除无效的关联，这些按钮可能在菜单修改的时候已经删除了
    public void deleteDisabledButtonPermission(Menu menu){
        List<RoleMenu> list = dao().query(RoleMenu.class,Cnd.where("menuId","=",menu.getId()));
        for(RoleMenu roleMenu : list){
            if(StringUtils.isNotEmpty(roleMenu.getButtonPermission())){
                String str  = roleMenu.getButtonPermission()+",";
                for(String permission : str.split(",")){
                    if(!menu.getButtonPermissions().contains(","+permission+",")){
                        str = str.replace(permission+",","");
                    }
                }
                if(str.length()>0){
                    str = str.substring(0,str.length()-1);
                }
                roleMenu.setButtonPermission(str);
                dao().update(roleMenu,"^(buttonPermission|updateTime|updateBy)$");
            }
        }
    }
}

package com.kaisa.kams.components.service;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.base.BaseService;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.StringFormatUtils;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.Menu;
import com.kaisa.kams.models.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.OrderBy;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.sql.SqlCallback;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.service.IdNameEntityService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 菜单相关接口
 * Created by weid on 2016/11/25.
 */
@IocBean(fields = "dao")
public class MenuService extends BaseService<Menu> {

    private static final Log log = Logs.get();

    @Inject
    private RoleService roleService;

    /**
     * 查询所有PC的菜单
     *
     * @return
     */
    @At("/query")
    public List<Map> drawMenu(User u) {
        if (null == u)
            return null;

        String sqlstr = "SELECT" +
                " distinct " +
                " m.id," +
                " m.name," +
                " m.depth," +
                " m.alias," +
                " m.url," +
                " m.draworder," +
                " m.parentId," +
                " m.platform" +
                " FROM sl_menu m,sl_rolemenu rm, sl_role r, sl_roleuser ru, sl_user u" +
                " WHERE m.id = rm.menuId " +
                "  AND r.id = rm.roleId" +
                "  AND r.id = ru.role" +
                "  AND u.id = ru.user" +
                "  AND m.type = '01' " +
                "  AND m.status = '" + PublicStatus.ABLE + "'" +
                "  AND rm.status = '" + PublicStatus.ABLE + "'" +
                "  AND r.status = '" + PublicStatus.ABLE + "'" +
                "  AND u.status = '" + PublicStatus.ABLE + "'" +
                "  AND u.id = @userid" +
                " ORDER BY m.draworder ASC ";

        Sql sql = Sqls.queryEntity(sqlstr);
        sql.setEntity(dao().getEntity(Menu.class));
        sql.params().set("userid", u.getId());
        dao().execute(sql);
        List<Menu> menuList = sql.getList(Menu.class);
        if (null == menuList && menuList.size() == 0) {
            return null;
        }

        List<Map> result = new ArrayList<>();
        List sons = null;
        for (Menu menu : menuList) {
            if ("1".equals(menu.getDepth())) {
                Map tmp = new HashMap<>();
                tmp.put("self", menu);
                sons = new ArrayList();
                tmp.put("sons", sons);
                result.add(tmp);
            } else if ("2".equals(menu.getDepth())) {
                if (null != sons)
                    sons.add(menu);
            }
        }
        return result;
    }

    /**
     * 查询所有有效菜单信息
     * Created by wangj on 2016/11/28.
     */
    public List<Menu> queryMenuList() {
        List<Menu> list = dao().query(Menu.class, Cnd.where("status", "=", PublicStatus.ABLE).orderBy("draworder", "ASC"));
        if (list.size() > 0) {
            for (Menu m : list) {
                if (m.getParentId() != null && !"".equals(m.getParentId())) {
                    Menu mtmp = fetchById(m.getParentId());
                    if (mtmp != null) {
                        m.setParent(mtmp);
                        m.setName(m.getParent().getName() + "--" + ("02".equalsIgnoreCase(m.getType())?"["+m.getName()+"]":m.getName()));
                    }
                }
            }
        }
        return list;
    }

    /**
     * 查询所有1级菜单
     * Created by wangj on 2016/12/29.
     */
    public List<Menu> queryParentMenuList() {
        List<Menu> list = dao().query(Menu.class, Cnd.where("status", "=", PublicStatus.ABLE).and("depth", "=", "1").orderBy("draworder", "ASC"));
        return list;
    }

    /**
     * 查询子级菜单按照倒排序DESC
     * Created by wangj on 2016/12/29.
     */
    public List<Menu> querySonMenuList(String parentId, String orderBy) {
        OrderBy or = Cnd.where("status", "=", PublicStatus.ABLE).and("parentId", "=", parentId).orderBy("draworder", orderBy);
        List<Menu> list = dao().query(Menu.class, or);
        return list;
    }

    /**
     * 按角色Id查询菜单信息(批量)
     *
     * @param roleId
     * @return
     */
    public List<Map> queryMenuListByRoleId(String roleId) {
        StringBuffer sqlBuffer = new StringBuffer("SELECT a.id,a.name, " +
                "b.menuId, " +
                "b.buttonPermission,c.name menuName,(SELECT t. NAME FROM sl_menu t WHERE t.id = c.parentId) parentMenuName, " +
                "b.permission FROM sl_role a,sl_rolemenu b,sl_menu c where a.id=b.roleId and b.menuId=c.id and a.status='ABLE' and b.status='ABLE' and c.status='ABLE'");
        if (StringUtils.isNotEmpty(roleId)) {
            sqlBuffer.append(" and a.id='").append(roleId).append("'");
        }
        sqlBuffer.append(" ORDER BY c.draworder ASC");
        Sql sql = Sqls.create(sqlBuffer.toString());
        sql.setCallback(new SqlCallback() {
            public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
                List<Map> list = new LinkedList<>();
                while (rs.next()) {
                    Map tmp = new HashMap();
                    tmp.put("id", rs.getString("id"));
                    tmp.put("name", rs.getString("name"));
                    tmp.put("menuId", rs.getString("menuId"));
                    tmp.put("parentMenuName", rs.getString("parentMenuName"));
                    if (!"".equals(rs.getString("parentMenuName")) && null != rs.getString("parentMenuName"))
                        tmp.put("menuName", rs.getString("parentMenuName") + "--" + rs.getString("menuName"));
                    else
                        tmp.put("menuName", rs.getString("menuName"));
                    tmp.put("permission", rs.getString("permission"));
                    tmp.put("buttonPermission",rs.getString("buttonPermission"));
                    list.add(tmp);
                }
                return list;
            }
        });
        dao().execute(sql);
        List<Map> list  =  sql.getList(Map.class);
        List<Map> buttonList= new ArrayList<Map>();
        for(Map map : list){
            if(StringFormatUtils.isNotEmpty(map.get("buttonPermission"))){
                buttonList.addAll(getMapsByPermissionAndParentId(map));
            }
        }
        list.addAll(buttonList);
        return list;
    }

    private List<Map> getMapsByPermissionAndParentId(Map map ){
        List<Menu> list  = getMenusByPermissionAndParentId(map.get("buttonPermission").toString().split(","),map.get("menuId").toString());
        List<Map> maplist  = new ArrayList<Map>();
        for(Menu menu : list){
            Map newMap  = new HashMap();
            newMap.put("id",map.get("id"));
            newMap.put("name",map.get("name"));
            newMap.put("menuId",menu.getId());
            newMap.put("parentMenuName",map.get("menuName"));
            newMap.put("menuName",map.get("menuName")+"--["+menu.getName()+"]");
            newMap.put("permission", map.get("permission"));
            maplist.add(newMap);
        }
        return maplist;
    }

    private List<Menu> getMenusByPermissionAndParentId(String[] permission,String parentId){
        return dao().query(Menu.class,Cnd.where("parentId","=",parentId).and("alias","in",permission).and("status","=",PublicStatus.ABLE));
    }

    /**
     * 按角色Id查询菜单信息(合并)
     *
     * @param roleId
     * @return
     */
    public String fetchMenuInfoByRoleId(String roleId) {
        StringBuffer sqlBuffer = new StringBuffer("SELECT P.id,group_concat(CONCAT('<div class=\"ui label\">',P.menuName,'</div>') ORDER BY p.menuId SEPARATOR '<br>') menuName " +
                "FROM (SELECT a.id,b.menuId,(SELECT t.name FROM sl_menu t " +
                "WHERE t.id = b.menuId) menuName " +
                " FROM sl_role a,sl_rolemenu b WHERE a.id = b.roleId AND a.id ='").append(roleId).append("') P");
        Sql sql = Sqls.create(sqlBuffer.toString());
        sql.setCallback(new SqlCallback() {
            public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
                String menuName = "";
                while (rs.next()) {
                    menuName = rs.getString("menuName");
                }
                return menuName;
            }
        });
        dao().execute(sql);
        return sql.getObject(String.class);
    }

    /**
     * 根据查询输入名称 查询菜单信息
     *
     * @param param
     * @return
     */
    public DataTables queryByName(DataTableParam param) {
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());

        String name = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            name = keys.get("name");

        }
        List<Menu> mList = new ArrayList<Menu>();
        Cnd cnd ;
        OrderBy or ;
        DataTables dataTables = null;
        try {
            log.info("menuservice queryByName param: name:" + name);
            or = Cnd.where("status", "=", PublicStatus.ABLE).and("name", "like", "%" + name + "%").and("type", "in", "01,03".split(",")).orderBy("draworder", "ASC");
            //Pager pager = DataTablesUtil.getDataTableToPager(start, length);
            List<Menu> menuList = dao().query(Menu.class, or, pager);
            for (Menu m : menuList) {
                if (m.getParentId() != null && !"".equals(m.getParentId())) {
                    Menu mtmp = fetchById(m.getParentId());
                    if (mtmp != null) {
                        m.setParent(mtmp);
                    }
                }
                mList.add(m);
            }

            dataTables = new DataTables(param.getDraw(), dao().count(Menu.class), dao().count(Menu.class, or), mList);
            dataTables.setOk(true);
            dataTables.setMsg("成功");
        } catch (Exception e) {
            log.error("fail to queryByName in MenuService,exception={}" + e);
            dataTables = new DataTables(0, 0, 0, null);
            dataTables.setOk(false);
            dataTables.setMsg("获取数据失败.");
        }
        return dataTables;
    }


    public List<Menu> buttonListByMenu(String menuId){
        return dao().query(Menu.class,Cnd.where("parentId","=",menuId).and("type","=","02"));
    }

    public List<Menu> queryAllButton(){
        return dao().query(Menu.class,Cnd.where("type","=","02"));
    }
    /**
     * 删除菜单
     *
     * @param id
     * @return
     */
    @Aop(TransAop.READ_COMMITTED)
    public int delete(String id) {
        try {
            Menu menu = fetchById(id);
            menu.setStatus(PublicStatus.DISABLED);
            // 执行修改方法
            if (ShiroSession.getLoginUser() != null) {
                menu.setUpdateBy(String.valueOf(ShiroSession.getLoginUser().getId()));
            }
            menu.setUpdateTime(new Date());
            dao().update(menu, "^(updateTime|updateBy|status)$");
            //失效子菜单
            List<Menu> list = dao().query(Menu.class, Cnd.where("status", "=", PublicStatus.ABLE).and("parentId", "=", id));
            if (list.size() > 0) {
                for (Menu m : list) {
                    m.setStatus(PublicStatus.DISABLED);
                    if (ShiroSession.getLoginUser() != null) {
                        m.setUpdateBy(String.valueOf(ShiroSession.getLoginUser().getId()));
                    }
                    m.setUpdateTime(new Date());
                    dao().update(m, "^(updateTime|updateBy|status)$");
                }
            }
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 新增菜单信息
     *
     * @param menu 菜单信息
     * @return
     */
    @Aop(TransAop.READ_COMMITTED)
    public Map add(Menu menu) {
        Map result = new HashMap();

        //如果存在别名，不给保存
        if(isExistAlias(menu)){
            result.put("ok", false);
            result.put("msg", "新增菜单失败：已存在同样的菜单别名");
            return result;
        }
        // 获取父级菜单信息
        if (menu.getParentId() != null && !"999999".equals(menu.getParentId())) {
            Menu parentMenu = fetchById(menu.getParentId());
            if (parentMenu != null) {
                if (!menu.getPlatform().equals(parentMenu.getPlatform())) {
                    result.put("ok", false);
                    result.put("msg", "新增菜单失败：所属平台与父级所属平台不一致");
                    return result;
                }
                //设置菜单深度
                menu.setDepth((Integer.parseInt(parentMenu.getDepth()) + 1) + "");
                //设置菜单级别编码
                String draworder = "";
                List<Menu> menuList = querySonMenuList(menu.getParentId(), "DESC");
                if (menuList.size() > 0) {
                    draworder = (Integer.parseInt(menuList.get(0).getDraworder()) + 1) + "";
                    if (draworder.length() < 4)
                        draworder = "0" + draworder;
                    menu.setDraworder(draworder);
                } else
                    menu.setDraworder(parentMenu.getDraworder() + "01");
            } else {
                result.put("ok", false);
                result.put("msg", "新增菜单失败：父级不存在");
                return result;
            }
        } else {
            menu.setParentId(null);
            menu.setAlias("");
            menu.setUrl("");
            menu.setDepth("1");
            List<Menu> list = queryParentMenuList();
            if (list.size() > 0) {
                Menu tmp = list.get(list.size() - 1);
                String drawTmp = (Integer.parseInt(tmp.getDraworder()) + 1) + "";
                if (drawTmp.length() < 2)
                    drawTmp = "0" + drawTmp;
                menu.setDraworder(drawTmp);
            }
        }
        menu.setStatus(PublicStatus.ABLE);
        menu.setCreateTime(new Date());
        menu.setCreateBy(String.valueOf(ShiroSession.getLoginUser().getId()));
        Menu u = dao().insert(menu);
        //同步更新菜单下面的按钮
        updateButton(u,"^(status|name|url|alias|updateBy|updateTime)$");
        if (u != null) {
            result.put("ok", true);
            result.put("msg", "新增菜单成功.");
            result.put("userId", u.getId());
            return result;
        }
        result.put("ok", false);
        result.put("msg", "新增菜单失败.");
        return result;
    }

    public void updateButton(Menu menu,String filter){
        if(CollectionUtils.isNotEmpty(menu.getButtonList())){
            for(Menu button : menu.getButtonList()){
                button.setParentId(menu.getId());
                button.setType("02");
                button.setStatus(PublicStatus.ABLE);
                persistence(button,filter);
            }
        }
        removeDeletedBaseModel(buttonListByMenu(menu.getId()),menu.getButtonList());
    }

    private boolean isExistAlias(Menu menu){
        Cnd cnd = Cnd.where("alias","=",menu.getAlias()).and("type","in","01,03".split(","));
        if(StringUtils.isNotEmpty(menu.getId())){
            cnd.and("id","<>",menu.getId());
        }
        return dao().fetch(Menu.class,cnd)!=null;
    }
    /**
     * 更新菜单信息
     *
     * @param menu 菜单信息
     * @return
     */
    @Aop(TransAop.READ_COMMITTED)
    public Map update(Menu menu) {
        Map result = new HashMap();

        //如果存在别名，不给保存
        if(isExistAlias(menu)){
            result.put("ok", false);
            result.put("msg", "新增菜单失败：已存在同样的菜单别名");
            return result;
        }

        if ("999999".equals(menu.getParentId()) || null == menu.getParentId()) {
            menu.setParentId(null);  //清空父Id
            menu.setAlias("");
            menu.setUrl("");
        }

        int parentReorderIdx = -1;   //重排后的位置
        Menu originalMenu = fetchById(menu.getId());  //更新前的menu
        if (originalMenu == null) {
            result.put("ok", false);
            result.put("msg", "菜单修改失败：无法获取菜单原始信息");
            return result;
        }
        if (menu.getParentId() != null) {
            if (!menu.getPlatform().equals(fetchById(menu.getParentId()).getPlatform())) {
                result.put("ok", false);
                result.put("msg", "菜单修改失败：所属平台与父级所属平台不一致");
                return result;
            }
        }
        //菜单信息初始化
        menu.setDepth(originalMenu.getDepth());
        menu.setDraworder(originalMenu.getDraworder());
        menu.setCreateBy(originalMenu.getCreateBy());
        menu.setCreateTime(originalMenu.getCreateTime());

        List<Menu> listReorder;
        List<Menu> listReorderFinal = new ArrayList<>();
        List<List> listReorderSon = new ArrayList<>();
        //原来是1级
        if (originalMenu.getParentId() == null) {
            //1级变2级，层级和位置根据新的父亲节点生成，并且重排原来的1级顺序
            if (menu.getParentId() != null) {
                if (menu.getParentId().equals(menu.getId())) {
                    result.put("ok", false);
                    result.put("msg", "菜单修改失败：菜单不能修改成自身的子菜单");
                    return result;
                }
                if (querySonMenuList(menu.getId(), "DESC").size() > 0) {
                    result.put("ok", false);
                    result.put("msg", "菜单修改失败：请先将其子菜单全部移除再做层级调整");
                    return result;
                }
                //a 先将原菜单之后的1级菜单，重排顺序
                listReorder = queryParentMenuList();  //得到目前所有的1级菜单
                for (int i = Integer.parseInt(originalMenu.getDraworder()); i < listReorder.size(); i++) {
                    Menu m = listReorder.get(i);
                    String draworder = (Integer.parseInt(m.getDraworder()) - 1) + "";
                    if (draworder.length() < 2)
                        draworder = "0" + draworder;
                    m.setDraworder(draworder);
                    listReorderFinal.add(m);
                    //存在子菜单，子序号也要处理
                    List<Menu> reorderSonList = querySonMenuList(m.getId(), "ASC");
                    if (reorderSonList.size() > 0) {
                        for (Menu reorderSonMenu : reorderSonList) {
                            reorderSonMenu.setDraworder(draworder + reorderSonMenu.getDraworder().substring(2));//设置子级新编号
                        }
                        listReorderSon.add(reorderSonList);
                    }
                    //获取父级重排后的位置
                    if (m.getId().equals(menu.getParentId())) {
                        parentReorderIdx = listReorderFinal.size() - 1;
                    }
                }
                //b 设置新的深度和编码
                Menu parentMenu = null;
                if (parentReorderIdx > -1)   //父级被重排，则重排里面取
                    parentMenu = listReorderFinal.get(parentReorderIdx);
                else
                    parentMenu = fetchById(menu.getParentId());
                if (parentMenu != null) {
                    //设置菜单深度
                    menu.setDepth((Integer.parseInt(parentMenu.getDepth()) + 1) + "");
                    //设置菜单级别编码
                    String draworder = "";
                    List<Menu> menuList = querySonMenuList(menu.getParentId(), "DESC");
                    if (menuList.size() > 0) {
                        draworder = (menuList.size() + 1) + "";
                        if (draworder.length() < 2)
                            draworder = "0" + draworder;
                        menu.setDraworder(parentMenu.getDraworder() + draworder);
                    } else
                        menu.setDraworder(parentMenu.getDraworder() + "01");
                } else {
                    result.put("ok", false);
                    result.put("msg", "菜单修改失败：父级不存在");
                    return result;
                }
            }
        }
        //原来是2级
        else {
            //2级变1级
            if (menu.getParentId() == null) {
                //设置1级层级和顺序编码，重新排列原来2级顺序
                //a 设置1级编码
                List<Menu> menuList = queryParentMenuList();  //得到目前所有的1级菜单
                String draworder = "";
                if (menuList.size() > 0) {
                    draworder = (Integer.parseInt(menuList.get(menuList.size() - 1).getDraworder()) + 1) + "";
                    if (draworder.length() < 2)
                        draworder = "0" + draworder;
                } else
                    draworder = "01";
                menu.setDraworder(draworder);
                menu.setDepth("1");
                //b 重新排列原来2级顺序编码
                listReorder = querySonMenuList(originalMenu.getParentId(), "ASC");
                for (int i = Integer.parseInt(originalMenu.getDraworder().substring(2)); i < listReorder.size(); i++) {
                    Menu m = listReorder.get(i);
                    String draworderSon = (Integer.parseInt(m.getDraworder()) - 1) + "";
                    if (draworderSon.length() < 4)
                        draworderSon = "0" + draworderSon;
                    m.setDraworder(draworderSon);
                    listReorderFinal.add(m);
                }
            }
            //2级变不同父2级
            else if (menu.getParentId() != null && !originalMenu.getParentId().equals(menu.getParentId())) {
                //设置2级层级和顺序编码，重新排列原来2级顺序
                //a 设置2级新的深度和编码
                Menu parentMenu = fetchById(menu.getParentId());
                if (parentMenu != null) {
                    //设置菜单深度
                    menu.setDepth((Integer.parseInt(parentMenu.getDepth()) + 1) + "");
                    //设置菜单级别编码
                    String draworder = "";
                    List<Menu> menuList = querySonMenuList(menu.getParentId(), "DESC");
                    if (menuList.size() > 0) {
                        draworder = (Integer.parseInt(menuList.get(0).getDraworder()) + 1) + "";
                        if (draworder.length() < 4)
                            draworder = "0" + draworder;
                        menu.setDraworder(draworder);
                    } else
                        menu.setDraworder(parentMenu.getDraworder() + "01");
                } else {
                    result.put("ok", false);
                    result.put("msg", "菜单修改失败：父级不存在");
                    return result;
                }
                //b 重新排列原来2级顺序编码
                listReorder = querySonMenuList(originalMenu.getParentId(), "ASC");
                for (int i = Integer.parseInt(originalMenu.getDraworder().substring(2)); i < listReorder.size(); i++) {
                    Menu m = listReorder.get(i);
                    String draworderSon = (Integer.parseInt(m.getDraworder()) - 1) + "";
                    if (draworderSon.length() < 4)
                        draworderSon = "0" + draworderSon;
                    m.setDraworder(draworderSon);
                    listReorderFinal.add(m);
                }
            }
        }
        //设置公共信息
        menu.setStatus(PublicStatus.ABLE);
        menu.setUpdateTime(new Date());
        menu.setUpdateBy(String.valueOf(ShiroSession.getLoginUser().getId()));
        //更新自身
        int updateNum = dao().update(menu);
        //同步更新菜单下面的按钮
        updateButton(menu,"^(status|name|url|alias|updateBy|updateTime)$");
        //修改权限菜单表里面已经关联的按钮权限，如果有删除的，则一并删除
        roleService.deleteDisabledButtonPermission(menu);
        //批量重排
        if (listReorderFinal.size() > 0) {
            dao().update(listReorderFinal);
        }
        //批量重排需要处理的子级
        if (listReorderSon.size() > 0) {
            for (List l : listReorderSon) {
                dao().update(l);
            }
        }
        if (updateNum > 0) {
            result.put("ok", true);
            result.put("msg", "菜单修改成功.");
            return result;
        }
        result.put("ok", false);
        result.put("msg", "菜单修改失败.");

        return result;
    }

    /**
     * 通过id查询菜单信息
     *
     * @param id
     * @return
     */
    public Menu fetchById(String id) {
        List<Menu> buttonList = buttonListByMenu(id);
        Menu menu = dao().fetch(Menu.class, Cnd.where("id", "=", id).and("status", "=", PublicStatus.ABLE));
        menu.setButtonList(buttonList);
        return menu;
    }


    public int findMenuByPromise(String promise,String loginId ){
        String sqlstr = "SELECT " +
                " count(*) as number " +
                " FROM " +
                " sl_rolemenu sr " +
                " LEFT JOIN sl_menu sm ON sr.menuId = sm.id " +
                (promise.contains(":")?" and sr.buttonPermission like @promise":"")+
                " WHERE " +
                " sm.alias = @alias and sm.`status` = 'ABLE' " +
                " AND sr.roleId IN ( " +
                " SELECT " +
                "  sru.role " +
                " FROM " +
                "  sl_roleuser sru " +
                " WHERE " +
                "  sru. USER = @loginId " +
                ")" ;

        Sql countSql = Sqls.create(sqlstr);
        countSql.setParam("loginId",loginId);
        countSql.setParam("alias",promise.split(":")[0]);
        countSql.setParam("promise","%"+(promise.contains(":")?promise.split(":")[1]:"")+"%");
        countSql.setCallback(new SqlCallback() {
            public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
                int result = 0;
                while (rs.next()) {
                    result = rs.getInt("number");
                }
                return result;
            }
        });
        dao().execute(countSql);
        int count = countSql.getInt();
        return count;
    }
}

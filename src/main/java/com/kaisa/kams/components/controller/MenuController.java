package com.kaisa.kams.components.controller;

import com.alibaba.druid.util.HttpClientUtils;
import com.google.gson.Gson;
import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.MenuService;
import com.kaisa.kams.models.Menu;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.Cnd;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by wangj on 2016/11/25
 */
@IocBean
@At("/menu")
public class MenuController {

    @Inject
    private MenuService menuService;

    /**
     * 跳转到角色管理页面
     * @return
     */
    @At
    @RequiresPermissions("menu:view")
    @Ok("beetl:/menu/index.html")
    public Context index(){
        Context ctx = Lang.context();  //返回的Map
        ctx.set("menus",ShiroSession.getMenu());  //加载菜单
        return ctx;
    }

    /**
     * 查询菜单信息，分页（包含菜单权限）
     * @param param
     * @return
     */
    @At("/list_by_name")
    @Ok("json")
    @POST
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("menu:view")
    public Object listByName(@Param("..")DataTableParam param){
        return menuService.queryByName(param);
    }

    /**
     * 查询所有1级菜单信息(新增使用)
     * @return
     */
    @At("/list_parent")
    @Ok("json")
    @POST
    public Object queryParentMenuList(){
        Map<String,Object> map=new HashMap<String,Object>();
        List<Menu> listData=menuService.queryParentMenuList();
        map.put("data",listData);
        return map;
    }

    /**
     * 删除菜单(包含子菜单)信息
     * @param id
     * @return
     */
    @At
    @Ok("json")
    @Aop("auditInterceptor")
    @POST
    @RequiresPermissions("menu:delete")
    public Object delete(@Param("id")String id) {
        NutMap nutMap=new NutMap();
        int num=menuService.delete(id);
        if(num>0){
            return nutMap.setv("ok",true).setv("msg","失效成功.");
        }
        return nutMap.setv("ok",false).setv("msg","失效失败.");
    }
    /**
     * 新增菜单
     * @param menu
     * @return
     */
    @At("/save_menu")
    @POST
    @AdaptBy(type = JsonAdaptor.class)
    @RequiresPermissions("menu:create")
    public Object saveMenu(@Param("..")Menu menu){
        NutMap nutMap=new NutMap();
        Map result =menuService.add(menu);
        return nutMap.setv("ok", result.get("ok")).setv("msg", result.get("msg"));
    }


    @At("/updateAllMenu")
    public void updateAllMenu(){

        String text =

                "展期报表\t\t查询\t\thasQuery\n" +
                        "导出\t\thasExport";


        Menu returnMenu = null;
        for(String str :text.split("\n")){
            List<String> values = new ArrayList<>();
            for(String value : str.split("\t")){
                if(StringUtils.isNotEmpty(value)){
                    value = value.trim();
                    values.add(value);
                }
            }
            if(values.size()==3){
                if(returnMenu!=null){
                    menuService.update(returnMenu);
                }
                String menuName = values.remove(0);
                returnMenu = menuService.dao().fetch(Menu.class, Cnd.where("name","=",menuName).and("type","=","01"));
            }
            Menu button = new Menu();
            button.setName(values.remove(0));
            button.setAlias(values.remove(0));
            returnMenu.addButton(button);
//            System.out.println(returnMenu);
//            menuService.update(returnMenu);
        }
        menuService.update(returnMenu);
    }
    @At("/addAllMenu")
    public void addAllMenu(){
        String json  = "{\n" +
                "    \"id\":\"\",\n" +
                "    \"name\":\"${menuName}_${tabName}\",\n" +
                "    \"alias\":\"${alias}\",\n" +
                "    \"url\":\"/\",\n" +
                "    \"platform\":\"PC\",\n" +
                "    \"parentId\":\"cff1e57c-c8d4-11e6-a7f3-005056902907\",\n" +
                "    \"type\":\"03\",\n" +
                "    \"buttonList\":[\n" +
                "    ${buttonList} "+
                "    ]\n" +
                "}";
        String button = "{\n" +
                "            \"name\":\"${buttonName}\",\n" +
                "            \"alias\":\"${buttonAlias}\",\n" +
                "            \"url\":\"\",\n" +
                "            \"id\":\"\"\n" +
                "        }";
        String text = "待推单\t待推单\t查询\tLoanToBePush\thasQuery\n" +
                "业务详情\t\thasView\n" +
                "推单\t\thasPush\n" +
                "部分推单\t查询\tPartLoanToBePush\thasQuery\n" +
                "业务详情\t\thasView\n" +
                "推单\t\thasPush\n" +
                "推单完成\t查询\tCompleteLoanToBePush\thasQuery\n" +
                "业务详情\t\thasView\n" +
                "\n" +
                "推单审核\t待审批\t查询\tLoanPushToBeApproved\thasQuery\n" +
                "审批\t\thasApproval\n" +
                "已审批\t查询\tLoanPushHaveBeenApproved\thasQuery";


        for(String str :text.split("\n\n")){
            String menustr = json;
            String menuName = "";
            String buttonstr = "";
            for(String line : str.split("\n")){
                List<String> values = new ArrayList<>();
                for(String value : line.split("\t")){
                    if(StringUtils.isNotEmpty(value)){
                        value = value.trim();
                        values.add(value);
                    }
                }
                if(values.size()==5){
                    menuName = values.remove(0);
                }
                if(values.size()==4){
                    if(menustr!=json){
                        buttonstr = buttonstr.substring(0,buttonstr.length()-1);
                        menustr =  menustr.replace("${buttonList}",buttonstr);
                        System.out.println(menustr);
                        Gson gson  = new Gson();
                        Menu menu =  gson.fromJson(menustr,Menu.class);
                        menuService.add(menu);
                    }
                    menustr = json;
                    buttonstr="";
                    menustr = menustr.replace("${menuName}",menuName).
                            replace("${tabName}",values.remove(0)).
                            replace("${alias}",values.remove(1));
                }
                buttonstr += button.replace("${buttonName}",values.remove(0))
                        .replace("${buttonAlias}",values.remove(0))+",";

            }
            buttonstr = buttonstr.substring(0,buttonstr.length()-1);
            menustr =  menustr.replace("${buttonList}",buttonstr);
            System.out.println(menustr);
            Gson gson  = new Gson();
            Menu menu =  gson.fromJson(menustr,Menu.class);
            menuService.add(menu);
        }
    }
    /**
     * 更新菜单
     * @param menu
     * @return
     */
    @At("/update_menu")
    @POST
    @AdaptBy(type = JsonAdaptor.class)
    @RequiresPermissions("menu:update")
    public Object updateMenu(@Param("..")Menu menu){
        NutMap nutMap=new NutMap();
        Map result =menuService.update(menu);
        return nutMap.setv("ok", result.get("ok")).setv("msg", result.get("msg"));
    }

    /**
     * 通过id查询菜单详情
     * @param id
     * @return
     */
    @At("/fetch_menu")
    @Ok("json")
    @POST
    public Object fetchMenuById(@Param("id")String id){
        NutMap nutMap=new NutMap();
        Menu menu=menuService.fetchById(id);
        if(menu==null){
            nutMap.setv("data","");
        }
        return  nutMap.setv("data",menu);
    }
}

package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.ChannelService;
import com.kaisa.kams.components.service.ProductTypeService;
import com.kaisa.kams.components.view.product.ProductTypeNode;
import com.kaisa.kams.enums.GuarantyType;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.Channel;
import com.kaisa.kams.models.ProductType;
import com.kaisa.kams.models.User;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by liuwen01 on 2016/11/22.
 */
@IocBean
@At("/product_type")
public class ProductTypeController {

    private static final Log log = Logs.get();

    @Inject
    private ProductTypeService productTypeService;
    @Inject
    private ChannelService channelService;

    /**
     * 跳转到产品类型管理页面
     *
     * @return
     */
    @At
    @Ok("beetl:/productType/list.html")
    @RequiresPermissions("productType:view")
    public Context list() {
        Context ctx = Lang.context();
        List<ProductType> typeList = productTypeService.queryAll();
        ctx.set("typeList", typeList);
        ctx.set("guarantyType", GuarantyType.values());
        return ctx;
    }

    @At
    @Ok("json")
    @RequiresPermissions("productType:view")
    public Object listAll(){
        NutMap result=new NutMap();
        List<ProductType> typeList=productTypeService.queryAll();
        result.setv("data",typeList);
        return result;
    }

    /**
     * 保存
     * @param productType
     * @return
     */
    @At("/add")
    @Ok("json")
    @POST
    @RequiresPermissions("productType:create")
    public Object add(@Param("..") ProductType productType) {
        User u = ShiroSession.getLoginUser();
        if(u!=null){
            productType.setUpdateBy(u.getLogin());
            productType.setCreateBy(u.getLogin());
        }
        Date now = new Date();
        productType.setUpdateTime(now);
        productType.setCreateTime(now);
        return addOrUpdate(productType);
    }

    /**
     * 修改
     * @param productType
     * @return
     */
    @At("/update")
    @Ok("json")
    @POST
    @RequiresPermissions("productType:update")
    public Object update(@Param("..") ProductType productType) {
        User u = ShiroSession.getLoginUser();
        if(u!=null){
            productType.setUpdateBy(u.getLogin());
            productType.setUpdateTime(new Date());
        }
        return addOrUpdate(productType);
    }

    /**
     * 编辑跳转方法
     * @param typeId
     * @return
     */
    @At("/edit_init")
    @Ok("json")
    @POST
    @RequiresPermissions("productType:update")
    public Object editInit(@Param("typeId") String typeId) {
        NutMap result = new NutMap();
        try {
            ProductType productType = productTypeService.fetchById(typeId);
            result.put("productType", productType);
            result.put("ok", true);
        }catch (Exception e) {
            result.put("ok", false);
            result.put("msg", "获取code异常！");
        }
        return result;
    }

    /**
     * 新增获取code
     * @return
     */
    @At("/get_code")
    @Ok("json")
    @POST
    public Object getCode() {
        NutMap result = new NutMap();
        //生成产品编号code
        try {
            result.put("code", productTypeService.getMaxFormatCode());
            result.put("ok", true);
        }catch (Exception e) {
            result.put("ok", false);
            result.put("msg", "获取code异常！");
        }
        return result;
    }

    /**
     * 添加或者修改
     * @param productType
     * @return
     */
    private NutMap addOrUpdate(ProductType productType) {
        NutMap resultMap = new NutMap();
        if (productType != null) {
            boolean validateResult = productTypeService.validateName(productType.getName(),productType.getId());
            if (!validateResult) {
                return resultMap.setv("ok", false).setv("msg", "产品类型名称重复！");
            }
            if (StringUtils.isNotEmpty(productType.getId())) {
                ProductType productTypeExist = productTypeService.fetch(productType.getId());
                if (null == productTypeExist) {
                    return resultMap.setv("ok", false).setv("msg", "无法找到改产品类型！");
                }
                if (productTypeExist.getStatus()== PublicStatus.ABLE &&
                        productType.getStatus() == PublicStatus.DISABLED &&
                        productTypeService.exitProduct(productType.getId())) {
                    return resultMap.setv("ok", false).setv("msg", "存在有效的子产品,不能将此产品类型改为失效！");
                }
                boolean updateResult = productTypeService.update(productType);
                if (updateResult) {
                    return resultMap.setv("ok", true).setv("msg", "修改成功");
                }
                return resultMap.setv("ok", false).setv("msg", "修改失败");
            }
            boolean addResult = productTypeService.add(productType);
            if (addResult) {
                return resultMap.setv("ok", true).setv("msg", "添加成功");
            }
            return resultMap.setv("ok", false).setv("msg", "添加失败");
        }
        return resultMap.setv("ok", false).setv("msg", "参数异常！");
    }

    @At("/list_productType_tree")
    @Ok("json")
    @POST
    public Object getProductTree(@Param("channelId")String channelId){

        NutMap nutMap=new NutMap();
        try {
            if(!Strings.isBlank(channelId)){
                Channel  channel=channelService.fetch(channelId);
                if(channel!=null){
                    String cooperationProductType=channel.getCooperationProductType();
                    if(!Strings.isBlank(cooperationProductType)){
                        //解析json字符串
                        List<ProductTypeNode> productTypeNodeList=Json.fromJsonAsList(ProductTypeNode.class,cooperationProductType);
                        nutMap.setv("data",productTypeService.getChannelProductType(productTypeNodeList));
                    }else {
                        nutMap.setv("data",productTypeService.getProductType());
                    }
                }
            }else {
                nutMap.setv("data",productTypeService.getProductType());
            }
            nutMap.setv("ok",true).setv("msg","成功");
        }catch (Exception e){
            log.error(e.getMessage());
            nutMap.setv("ok",false).setv("msg","获取数据异常");
        }
        return nutMap;
    }
}

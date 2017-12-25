package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.service.flow.FlowConfigureService;
import com.kaisa.kams.components.service.flow.FlowControlTmplService;
import com.kaisa.kams.components.service.flow.FlowService;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.*;
import com.kaisa.kams.components.view.product.OrganizeNode;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.business.ProductsToOrganize;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 产品信息维护
 * Created by weid on 2016/11/28.
 */
@IocBean
@At("/product")
public class ProductController {

    @Inject
    private ProductTypeService productTypeService;

    @Inject
    private ProductService productService;

    @Inject
    private ProductInfoTmplService productInfoTmplService;

    @Inject
    private MediaTemplateService mediaTemplateService;

    @Inject
    private FlowControlTmplService flowControlTmplService;

    @Inject
    private FlowService flowService;

    @Inject
    private FlowConfigureService flowConfigureService;


    /**
     * 产品列表页面
     */
    @At()
    @GET
    @Ok("beetl:/product/list.html")
    @RequiresPermissions("product:view")
    public Context list() {
        Context ctx = Lang.context();

        //获取符合条件的产品
        List<ProductType> productTypeList = productTypeService.queryAll(PublicStatus.ABLE);

        ctx.set("productTypeList", productTypeList);
        return ctx;
    }

    /**
     * 产品列表
     * @return
     */
    @At
    @Ok("json")
    @RequiresPermissions("product:view")
    public Object listAll(){
        NutMap result=new NutMap();
        List<Product> productList=productService.queryListAll();
        result.setv("data",productList);
        return result;
    }

    /**
     * 跳转 添加产品列表页面
     */
    @GET
    @At("/add_init")
    @Ok("beetl:/product/add.html")
    @RequiresPermissions("product:create")
    public Context addInit() {
        Context ctx = Lang.context();

        //获取符合条件的产品类型
        List<ProductType> productTypeList = productTypeService.queryAll(PublicStatus.ABLE);

        //查询产品信息模板
        List<ProductInfoTmpl> productInfoTmplList = productInfoTmplService.queryAll(PublicStatus.ABLE);

        //查询产品影像资料模板
        List<ProductMediaTmpl> productMediaTmplList = mediaTemplateService.queryAll(PublicStatus.ABLE);


        ctx.set("feeCycleType", FeeCycleType.values());
        ctx.set("feeChargeType", FeeChargeType.values());
        ctx.set("publicStatus", PublicStatus.values());
        ctx.set("feeType", FeeType.values());
        ctx.set("businessType", BusinessType.values());
        ctx.set("productTypeList", productTypeList);
        ctx.set("productInfoTmplList", productInfoTmplList);
        ctx.set("productMediaTmplList", productMediaTmplList);
        ctx.set("feeChargeNode", FeeChargeNode.values());
        ctx.set("channelType", ChannelType.values());
        return ctx;
    }

    /**
     * 跳转 修改产品列表页面
     */
    @GET
    @At("/edit_init")
    @Ok("beetl:/product/update.html")
    @RequiresPermissions("product:update")
    public Context editInit(@Param("id") String id) {
        Context ctx = Lang.context();
        //获取符合条件的产品
        List<ProductType> productTypeList = productTypeService.queryAll(PublicStatus.ABLE);

        //查询产品模板
        List<ProductInfoTmpl> productInfoTmplList = productInfoTmplService.queryAll(PublicStatus.ABLE);

        //查询产品影像资料模板
        List<ProductMediaTmpl> productMediaTmplList = mediaTemplateService.queryAll(PublicStatus.ABLE);

        ctx.set("feeCycleType", FeeCycleType.values());
        ctx.set("feeChargeType", FeeChargeType.values());
        ctx.set("publicStatus", PublicStatus.values());
        ctx.set("feeType", FeeType.values());
        ctx.set("businessType", BusinessType.values());
        ctx.set("productTypeList", productTypeList);
        ctx.set("productInfoTmplList", productInfoTmplList);
        ctx.set("feeChargeNode", FeeChargeNode.values());
        ctx.set("productMediaTmplList", productMediaTmplList);
        ctx.set("channelType", ChannelType.values());
        ctx.set("id", id == null || "".equals(id.trim()) ? "" : id);
        return ctx;
    }

    /**
     * 校验code和name
     * @param code
     * @param name
     * @return
     */
    private NutMap validateCodeAndName(String code,String name) {
        NutMap result = new NutMap();

        //验证code
        if (StringUtils.isEmpty(code)) {
            result.put("ok", false);
            result.put("msg", "产品编码不符合要求");
            return result;
        }
        //验证名称
        if (StringUtils.isEmpty(name)) {
            result.put("ok", false);
            result.put("msg", "产品名称不符合要求");
            return result;
        }

        //验证编码是否重复
        boolean enable = productService.enableCode(code);
        result.put("ok",enable);
        if(!enable) {
            result.put("msg", "产品编码重复");
            return result;
        }

        //验证名称是否重复
        enable = productService.enableName(null,name);
        result.put("ok",enable);
        if(!enable) {
            result.put("msg", "产品名称重复");
            return result;
        }
        return result;
    }

    /**
     * 校验name
     * @param id
     * @param name
     * @return
     */
    public NutMap validateName(String id,String name) {
        NutMap result = new NutMap();

        //验证名称
        if (StringUtils.isEmpty(name)) {
            result.put("ok", false);
            result.put("msg", "非法名称");
            return result;
        }
        //验证名称是否重复
        boolean enable = productService.enableName(id,name);
        result.put("ok",enable);
        if(!enable) {
            result.put("msg", "产品名称重复");
            return result;
        }
        return result;
    }


    /**
     * 新增产品
     */
    @At
    @POST
    @Ok("json")
    @RequiresPermissions("product:create")
    public Object add(@Param("::product.") Product product) {

        NutMap result = new NutMap();

        if (null == product) {
            result.put("ok", false);
            result.put("msg", "产品信息错误");
            return result;
        }
        //校验
        NutMap validateResult = validateCodeAndName(product.getCode(),product.getName());
        if (!(boolean)validateResult.get("ok")) {
            return validateResult;
        }


        ProductType productType = productTypeService.fetch(product.getTypeId());
        if (null == productType) {
            result.put("ok", false);
            result.put("msg", "找不到对应的产品大类");
            return result;
        }
        product.setCreateBy(ShiroSession.getLoginUser().getName());
        product.setCreateTime(new Date());
        product.setUpdateBy(ShiroSession.getLoginUser().getName());
        product.setUpdateTime(new Date());

        //新增数据
        Product data = productService.add(product);


        if (data == null) {
            result.put("ok", false);
            result.put("msg", "新增产品失败");
        } else {
            result.put("ok", true);
            result.put("msg", "新增产品成功");
            result.put("id", data.getId());
        }
        return result;
    }


    /**
     * 根据Id获取产品信息
     */
    @At
    @POST
    @Ok("json")
    @RequiresPermissions("product:update")
    public Object fetchById(@Param("id") String id) {
        NutMap result = new NutMap();
        Product product = productService.fetch(id);
        if (null == product) {
            result.put("ok", false);
            result.put("msg", "查询产品失败");
            return result;
        }
        product = productService.formatProductInterest(product);
        result.put("ok", true);
        result.put("msg", "查询产品成功");
        result.put("data", product);
        return result;
    }


    /**
     * 修改产品
     */
    @At
    @POST
    @Ok("json")
    @RequiresPermissions("product:update")
    public Object update(@Param("::product.") Product product) {

        NutMap result = new NutMap();
        if (null == product || null == product.getId()) {
            result.put("ok", false);
            result.put("msg", "产品信息错误");
            return result;
        }

        //校验
        NutMap validateResult = validateName(product.getId(),product.getName());
        if (!(boolean)validateResult.get("ok")) {
            return validateResult;
        }

        product.setUpdateBy(ShiroSession.getLoginUser().getName());
        product.setUpdateTime(new Date());


        //新增数据
        boolean flag = productService.update(product);


        if (flag) {
            result.put("ok", true);
            result.put("msg", "修改产品成功");
        } else {
            result.put("ok", false);
            result.put("msg", "修改产品失败");
        }
        return result;
    }


    @At
    @POST
    @Ok("json")
    public Object queryByType(@Param("typeId") String typeId){
        NutMap result = new NutMap();
        List<Product> products = productService.queryByType(typeId);
        if (null==products){
            result.put("ok",false);
            result.put("msg","查询产品失败");
            return result;
        }
        result.put("ok",true);
        result.put("msg","查询产品成功");
        result.put("data",products);
        return result;
    }

    @POST
    @Ok("json")
    @At("/get_organize_product")
    public Object getOrganizeProduct(@Param("productId") String productId){
        NutMap result = new NutMap();
        try {
            List<OrganizeNode> nodes = productService.getOrganizeProduct(productId);
            result.put("ok", true);
            result.put("data", nodes);
        }catch (Exception e) {
            result.put("ok", false);
            result.put("msg", "获取组织机构信息错误");
        }
        return result;
    }


    /**
     * 产品组织关联列表
     * @return
     */
    @At("/list_product_organize")
    @Ok("json")
    public Object listProductOrganize(@Param("productId") String productId){
        NutMap result=new NutMap();
        List<ProductsToOrganize> organizeList = productService.listProductOrganize(productId);
        result.setv("data",organizeList);
        return result;
    }

    /**
     * 产品列表
     * @return
     */
    @At("/add_organize_product")
    @Ok("json")
    public Object addOrganizeProduct(@Param("productId") String productId,@Param("nodes")String nodes){

        NutMap result = new NutMap();
        if (null == nodes || StringUtils.isEmpty(productId)) {
            result.put("ok", false);
            result.put("msg", "参数异常");
            return result;
        }
        try {
            String msg = "关联成功";
            boolean addResult = productService.addProductToOrganize(productId, Json.fromJsonAsList(OrganizeNode.class,nodes));
            result.put("ok", addResult);
            if (!addResult) {
                msg = "关联失败";
            }
            result.put("msg", msg);
        } catch (Exception e) {
            result.put("ok", false);
            result.put("msg", "后台异常");
        }
        return result;
    }


    /**
     * 启用产品
     * @return
     */
    @At("/start")
    @Ok("json")
    public Object start(@Param("productId") String productId){

        NutMap result = new NutMap();
        if (StringUtils.isEmpty(productId)) {
            result.put("ok", false);
            result.put("msg", "参数异常");
            return result;
        }
        Boolean updateResult = productService.updateProcessAndStatus(productId);
        if (updateResult) {
            result.put("ok", true);
            result.put("msg", "启用成功可以开始业务申请了！");
            return result;
        }
        result.put("ok", false);
        result.put("msg", "启用失败！");
        return result;
    }

    /**
     * 启用产品
     * @return
     */
    @At("/stop")
    @Ok("json")
    public Object stop(@Param("productId") String productId){

        NutMap result = new NutMap();
        if (StringUtils.isEmpty(productId)) {
            result.put("ok", false);
            result.put("msg", "参数异常");
            return result;
        }
        Boolean updateResult = productService.updateFlowConfigStatus(productId);
        if (updateResult) {
            result.put("ok", true);
            result.put("msg", "禁用成功！");
            return result;
        }
        result.put("ok", false);
        result.put("msg", "禁用失败！");
        return result;
    }

    @At("/get_user")
    @Ok("json")
    public Object getBusinessUsersByKeyWord(@Param("keyWord") String keyWord){
        NutMap nutMap=new NutMap();
        try {
            nutMap.setv("data",productService.getProductUsers(keyWord));
            nutMap.setv("ok",true).setv("msg","成功");
        } catch (Exception e) {
            nutMap.setv("ok",false).setv("msg","获取数据异常");
            return nutMap;
        }

        return nutMap;
    }
    /**
     * 获取产品业务员
     * @param productId
     * @param keyWord
     * @return
     */
    @At("/get_prd_user")
    @Ok("json")
    public Object getProductUsers(@Param("productId") String productId,@Param("keyWord") String keyWord){

        NutMap nutMap=new NutMap();

        if (StringUtils.isEmpty(productId)) {
            nutMap.setv("ok",false).setv("msg","获取数据异常");
            return nutMap;
        }
        try {
            nutMap.setv("data",productService.getProductUsers(productId,keyWord));
            nutMap.setv("ok",true).setv("msg","成功");
        } catch (Exception e) {
            nutMap.setv("ok",false).setv("msg","获取数据异常");
            return nutMap;
        }

        return nutMap;
    }

    /**
     * 查看
     */
    @GET
    @At("/view")
    @Ok("beetl:/product/view.html")
    @RequiresPermissions("product:view")
    public Context view(@Param("id") String id) {
        Context ctx = Lang.context();
        //获取符合条件的产品
        List<ProductType> productTypeList = productTypeService.queryAll(PublicStatus.ABLE);

        //查询产品模板
        List<ProductInfoTmpl> productInfoTmplList = productInfoTmplService.queryAll(PublicStatus.ABLE);

        //查询产品影像资料模板
        List<ProductMediaTmpl> productMediaTmplList = mediaTemplateService.queryAll(PublicStatus.ABLE);

        ctx.set("productTypeList", productTypeList);
        ctx.set("productInfoTmplList", productInfoTmplList);
        ctx.set("productMediaTmplList", productMediaTmplList);

        ctx.set("id", id == null || "".equals(id.trim()) ? "" : id);
        return ctx;
    }

    /**
     * 搜索已经配置流程的产品
     * @param keyWord
     * @return
     */
    @At("/search_product_flow")
    @Ok("json")
    public Object getProductFlow(@Param("keyWord") String keyWord){

        NutMap nutMap=new NutMap();
        try {
            nutMap.setv("data",productService.getProductFlow(keyWord));
            nutMap.setv("ok",true).setv("msg","成功");
        } catch (Exception e) {
            nutMap.setv("ok",false).setv("msg","获取数据异常");
            return nutMap;
        }

        return nutMap;
    }

    /**
     * 流程复制
     * @param productId
     * @param flowProductId
     * @return
     */
    @At("/add_flow")
    @Ok("json")
    public Object addFlow(@Param("productId") String productId,@Param("flowProductId") String flowProductId){

        NutMap nutMap=new NutMap();
        try {
            if (StringUtils.isEmpty(productId)||StringUtils.isEmpty(flowProductId)) {
                nutMap.setv("ok", false).setv("msg", "产品信息错误");
                return nutMap;
            }
            Product currentProduct = productService.fetchEnableProductById(productId);
            Product flowProduct = productService.fetchEnableProductById(flowProductId);
            if (null == currentProduct || null == flowProduct) {
                nutMap.setv("ok", false).setv("msg", "复制产品信息错误");
                return nutMap;
            }
            boolean result = flowControlTmplService.copyFlowControl(flowProductId,productId,currentProduct.getTypeId());
            nutMap.setv("ok", false).setv("msg", "复制流程失败！");
            if (result) {
                nutMap.setv("ok", true).setv("msg", "复制流程成功");
            }
        } catch (Exception e) {
            nutMap.setv("ok",false).setv("msg","后台处理异常");
            return nutMap;
        }

        return nutMap;
    }

    /**
     * 获取可用的产品列表
     * @return
     */
    @At
    @Ok("json")
    @RequiresPermissions("product:view")
    public NutMap queryAvailableProductList(@Param("flowConfigureType") FlowConfigureType flowConfigureType,@Param("id") String id){
        NutMap result=new NutMap();
        List<Product> productList=productService.queryDisplayedProductList(flowConfigureType,id);
        result.setv("data",productList);
        return result;
    }
}

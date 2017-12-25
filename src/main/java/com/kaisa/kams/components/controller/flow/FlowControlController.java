package com.kaisa.kams.components.controller.flow;

import com.kaisa.kams.components.service.flow.FlowControlItemService;
import com.kaisa.kams.components.service.flow.FlowControlTmplService;
import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.*;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.view.product.ProductView;
import com.kaisa.kams.enums.FlowControlType;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.flow.FlowControlItem;
import com.kaisa.kams.models.flow.FlowControlTmpl;
import com.kaisa.kams.models.Product;
import com.kaisa.kams.models.Role;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.pager.Pager;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;

import java.util.*;

/**
 * 流程管理设置
 * Created by weid on 2016/12/6.
 */
@IocBean
@At("/flow_control")
public class FlowControlController {

    @Inject
    private ProductTypeService productTypeService;

    @Inject
    private FlowControlTmplService flowControlTmplService;

    @Inject
    private FlowControlItemService flowControlItemService;

    @Inject
    private ProductService productService;

    @Inject
    private RoleService roleService;




    /**
     * 跳转到业务流程页面
     * @return
     */
    @At("/bussiness_index")
    @GET
    @Ok("beetl:/flowControl/bussiness/index.html")
    @RequiresPermissions("flowControl_business:view")
    public Context bussinessIndex(){
        Context ctx = Lang.context();
        //查询所有的产品大类
        ctx.set("productTypes",productTypeService.queryAbleAll());
        ctx.set("status", PublicStatus.values());
        ctx.set("type", FlowControlType.BUSINESS_CONTROL);
        return ctx;
    }

    /**
     * 跳转到风控流程页面
     * @return
     */
    @At("/risk_index")
    @GET
    @Ok("beetl:/flowControl/risk/index.html")
    @RequiresPermissions("flowControl_risk:view")
    public Context riskIndex(){
        Context ctx = Lang.context();
        //查询所有的产品大类
        ctx.set("productTypes",productTypeService.queryAbleAll());
        ctx.set("status", PublicStatus.values());
        ctx.set("type", FlowControlType.RISK_CONTROL);
        return ctx;
    }

    /**
     * 跳转到财务流程页面
     * @return
     */
    @At("/finance_index")
    @GET
    @Ok("beetl:/flowControl/financial/index.html")
    @RequiresPermissions("flowControl_finance:view")
    public Context financeIndex(){
        Context ctx = Lang.context();
        //查询所有的产品大类
        ctx.set("productTypes",productTypeService.queryAbleAll());
        ctx.set("status", PublicStatus.values());
        ctx.set("type", FlowControlType.FINANCE_CONTROL);
        return ctx;
    }

    @At
    @POST
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("flowControl_business:view")
    public Object list(@Param("..")DataTableParam param){
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());

        String code = "";
        String name = "";
        String productType = "";
        String product = "";
        FlowControlType type = null;
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            code = keys.get("code");
            name = keys.get("name");
            productType = keys.get("productType");
            product = keys.get("product");
            if(StringUtils.isNotEmpty(keys.get("type"))){
                type = FlowControlType.valueOf(keys.get("type"));
            }
        }
       return flowControlTmplService.query(param.getStart(),param.getLength(),param.getDraw(),code,name,productType,product,type);
    }


    @At
    @POST
    public Object add(@Param("::flowControlTmpl.")FlowControlTmpl flowControlTmpl,
                      @Param("flowControlItems")String flowControlItems){
        NutMap result = new NutMap();
        List<FlowControlItem> flowControlItemList = Json.fromJsonAsList(FlowControlItem.class, flowControlItems);

        if(null==flowControlTmpl||null==flowControlTmpl.getType()){
                result.put("ok",false);
                result.put("msg","流程模板信息错误");
                return result;
        }

        if (StringUtils.isEmpty(flowControlItems)||CollectionUtils.isEmpty(flowControlItemList)) {
            result.put("ok",false);
            result.put("msg","流程项信息为空！");
            return result;
        }



        //设置新建人
        flowControlTmpl.setCreateBy(ShiroSession.getLoginUser().getName());
        flowControlTmpl.setCreateTime(new Date());
        flowControlTmpl.setUpdateBy(ShiroSession.getLoginUser().getName());
        flowControlTmpl.setUpdateTime(new Date());

        //获取到产品
        Product product = productService.fetchEnableProductById(flowControlTmpl.getProductId());
        if(null==product){
            result.put("ok",false);
            result.put("msg","产品不存在");
            return result;
        }

        //查询当前产品是否已经添加改类型的流程
        List<FlowControlTmpl> flowControlTmpls = flowControlTmplService.query(product.getId(),flowControlTmpl.getType());
        if(null!=flowControlTmpls&&flowControlTmpls.size()>0){
            result.put("ok",false);
            result.put("msg","该产品已有"+flowControlTmpl.getType().getDescription()+"流程");
            return result;
        }


        //查询当前类型的编码
        flowControlTmpl.setCode(flowControlTmpl.getType().getCode()+(product==null?"":product.getCode()));
        FlowControlTmpl fct = flowControlTmplService.add(flowControlTmpl);
        if(null==fct){
            result.put("ok",false);
            result.put("msg","保存失败");
            return result;
        }



        if(null!=flowControlItemList&&flowControlItemList.size()>0){
            for (int i=0; i<flowControlItemList.size();i++){
                FlowControlItem fci = flowControlItemList.get(i);
                fci.setCode(fct.getType().getCode()+ (i+1));
                fci.setTmplId(fct.getId());
                fci.setCreateBy(ShiroSession.getLoginUser().getName());
                fci.setCreateTime(new Date());
                fci.setUpdateBy(ShiroSession.getLoginUser().getName());
                fci.setUpdateTime(new Date());
            }
        }

        flowControlItemService.batchAdd(flowControlItemList);
        result.put("ok",true);
        result.put("msg","保存成功");
        result.put("data",fct);
        return result;
    }


    @At
    @POST
    public Object update(@Param("::flowControlTmpl.")FlowControlTmpl flowControlTmpl,
                      @Param("flowControlItems")String flowControlItems){
        NutMap result = new NutMap();
        List<FlowControlItem> flowControlItemList = Json.fromJsonAsList(FlowControlItem.class, flowControlItems);

        if(null==flowControlTmpl){
            result.put("ok",false);
            result.put("msg","流程模板信息错误");
            return result;
        }

        if (StringUtils.isEmpty(flowControlItems)||CollectionUtils.isEmpty(flowControlItemList)) {
            result.put("ok",false);
            result.put("msg","流程项信息为空！");
            return result;
        }

        //设置新建人
        flowControlTmpl.setUpdateBy(ShiroSession.getLoginUser().getName());
        flowControlTmpl.setUpdateTime(new Date());

        //修改
        boolean flag = flowControlTmplService.update(flowControlTmpl);
        if(!flag){
            result.put("ok",false);
            result.put("msg","修改失败");
            return result;
        }

        FlowControlTmpl fct = flowControlTmplService.fetchById(flowControlTmpl.getId());

        //根据tmpId删除所有的item
        flowControlItemService.deleteByItemId(flowControlTmpl.getId());

        if(null!=flowControlItemList&&flowControlItemList.size()>0){
            for (int i=0; i<flowControlItemList.size();i++){

                FlowControlItem fci = flowControlItemList.get(i);
                fci.setCode(fct.getType().getCode()+ (i+1));
                fci.setTmplId(fct.getId());
                fci.setCreateBy(ShiroSession.getLoginUser().getName());
                fci.setCreateTime(new Date());
                fci.setUpdateBy(ShiroSession.getLoginUser().getName());
                fci.setUpdateTime(new Date());
            }
        }
        flowControlItemService.batchAdd(flowControlItemList);
        result.put("ok",true);
        result.put("msg","保存成功");
        result.put("data",flag);
        return result;
    }


    /**
     * 修改财务信息
     * @param id
     * @param flowControlItems
     * @return
     */
    @At("/update/financal")
    @POST
    @RequiresPermissions("flowControl_finance:update")
    public Object updateFinancal(@Param("id")String id,
                                 @Param("flowControlItems")String flowControlItems){
        NutMap result = new NutMap();
        List<FlowControlItem> flowControlItemList = Json.fromJsonAsList(FlowControlItem.class, flowControlItems);
        if(CollectionUtils.isEmpty(flowControlItemList)) {
            result.put("ok",false);
            result.put("msg","修改失败,流程项为空");
            return result;
        }

        FlowControlTmpl flowControlTmpl = flowControlTmplService.fetchById(id);

        if(null==flowControlTmpl){
            result.put("ok",false);
            result.put("msg","修改失败");
            return result;
        }

        //根据tmpId删除所有的item
        flowControlItemService.deleteByItemId(id);

        if(null!=flowControlItemList&&flowControlItemList.size()>0){
            for (int i=0; i<flowControlItemList.size();i++){

                FlowControlItem fci = flowControlItemList.get(i);
                fci.setCode(flowControlTmpl.getType().getCode()+ (i+1));
                fci.setTmplId(flowControlTmpl.getId());
                fci.setCreateBy(ShiroSession.getLoginUser().getName());
                fci.setCreateTime(new Date());
                fci.setUpdateBy(ShiroSession.getLoginUser().getName());
                fci.setUpdateTime(new Date());
            }
        }
        flowControlItemService.batchAdd(flowControlItemList);
        result.put("ok",true);
        result.put("msg","保存成功");
        return result;
    }

    /**
     * 通过Id获取
     * @param id
     * @return
     */
    @At
    @POST
    public Object fetchById(@Param("id")String id){
        NutMap result = new NutMap();
        FlowControlTmpl flowControlTmpl = flowControlTmplService.fetchById(id);
        ProductView product = productService.fetchProductViewById(flowControlTmpl.getProductId());
        List<FlowControlItem> flowControlItems =  flowControlItemService.queryByTmplId(id);
        for(FlowControlItem flowControlItem :flowControlItems){
            Role role = roleService.fetch(flowControlItem.getOrganizeId());
            flowControlItem.setOrganizeName(role.getName());
        }
        result.put("ok",true);
        result.put("msg","保存成功");
        result.put("tmpl",flowControlTmpl);
        result.put("items",flowControlItems);
        result.put("product",product);
        return  result;
    }


    /**
     * 通过产品Id获取流程图
     * @param productId 产品Id
     * @return
     */
    @At("/fetch_by_productId")
    public Object fetchFlowByProductId(@Param("productId") String productId){
        NutMap result = new NutMap();
         if(StringUtils.isEmpty(productId)){
             result.put("ok",false);
             result.put("msg","产品Id不能为空");
             return result;
         }

        //查看产品是否存在
        Product product = productService.fetchEnableProductById(productId);
        if(null==product){
            result.put("ok",false);
            result.put("msg","产品不存在");
            return result;
        }

        List<FlowControlItem> data = new ArrayList<>();
        FlowControlTmpl business = flowControlTmplService.fetchByProductIdAndType(productId,FlowControlType.BUSINESS_CONTROL);
        if(null!=business){
             //获取到所有的子节点
            List<FlowControlItem> flowControlItems = flowControlItemService.queryByTmplId(business.getId());
            if(null!=flowControlItems&&flowControlItems.size()>0){
                for (FlowControlItem flowControlItem:flowControlItems){
                    data.add(flowControlItem);
                }
            }
        }

        FlowControlTmpl risk = flowControlTmplService.fetchByProductIdAndType(productId,FlowControlType.RISK_CONTROL);
        if(null!=risk){
            //获取到所有的子节点
            List<FlowControlItem> flowControlItems = flowControlItemService.queryByTmplId(risk.getId());
            if(null!=flowControlItems&&flowControlItems.size()>0){
                for (FlowControlItem flowControlItem:flowControlItems){
                    data.add(flowControlItem);
                }
            }
        }

        FlowControlTmpl finance = flowControlTmplService.fetchByProductIdAndType(productId,FlowControlType.FINANCE_CONTROL);
        if(null!=finance){
            //获取到所有的子节点
            List<FlowControlItem> flowControlItems = flowControlItemService.queryByTmplId(finance.getId());
            if(null!=flowControlItems&&flowControlItems.size()>0){
                for (FlowControlItem flowControlItem:flowControlItems){
                    data.add(flowControlItem);
                }
            }
        }
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",data);
        return result;
    }
}

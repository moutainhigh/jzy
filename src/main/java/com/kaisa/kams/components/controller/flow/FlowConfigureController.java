package com.kaisa.kams.components.controller.flow;

import com.kaisa.kams.models.flow.FlowConfigure;
import com.kaisa.kams.components.view.flow.FlowConfigureModuleVO;
import com.kaisa.kams.components.view.flow.FlowConfigureTypeVO;
import com.kaisa.kams.components.service.flow.FlowConfigureService;
import com.kaisa.kams.components.service.flow.FlowControlTmplService;
import com.kaisa.kams.components.service.flow.FlowService;
import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.service.*;
import com.kaisa.kams.components.utils.ParamData;
import com.kaisa.kams.models.*;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;

import java.util.List;

/**
 * Created by zhouchuang on 2017/8/29.
 */

@IocBean
@At("/flow_configure_control")
public class FlowConfigureController {
    @Inject
    FlowConfigureService flowConfigureService;
    @Inject
    private FlowService flowService;
    @Inject
    private ProductService productService;
    @Inject
    private FlowControlTmplService flowControlTmplService;
    @Inject
    private ProductInfoTmplService productInfoTmplService;
    @Inject
    private WfTempletSevice wfTempletSevice;
    /**
     * 流程配置list
     * @return
     */
    @At("/index")
    @GET
    @Ok("beetl:/flowSettings/list.html")
    @RequiresPermissions("flowConfigure:view")
    public void index(){}


    /**
     * 流程配置add
     * @return
     */
    @At("/add")
    @GET
    @Ok("beetl:/flowSettings/add.html")
    @RequiresPermissions("flowConfigure:view")
    public void add(){}


    @At("/update")
    @POST
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("flowConfigure:view")
    public Object update(@Param("..")FlowConfigure flowConfigure){
        NutMap result = new NutMap();
        if(StringUtils.isEmpty(flowConfigure.getId())&&flowConfigureService.checkIsExist(flowConfigure)){
            result.put("ok",false);
            result.put("msg","该流程类型对应的产品已经存在");
            return result;
        }
        flowConfigureService.update(flowConfigure,result);
        return result;
    }
    /**
     * 流程配置edit
     * @return
     */
    @At("/edit")
    @GET
    @Ok("beetl:/flowSettings/edit.html")
    @RequiresPermissions("flowConfigure:view")
    public void edit(){
    }


    @At("/get")
    @POST
    @RequiresPermissions("flowConfigure:view")
    public Object get( @Param("id")String id ){
        FlowConfigure flowConfigure  = flowConfigureService.getFlowConfigureById(id);
        return flowConfigure;
    }

    /**
     * 流程配置view
     * @return
     */
    @At("/view")
    @GET
    @Ok("beetl:/flowSettings/view.html")
    @RequiresPermissions("flowConfigure:view")
    public void view(){
    }


    @At("/list")
    @POST
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("flowConfigure:view")
    public Object list(@Param("..")DataTableParam param){
        ParamData paramData  = new ParamData();
        paramData.setStart(param.getStart());
        paramData.setLength(param.getLength());
        paramData.setDraw(param.getDraw());
        Cnd cnd = Cnd.where("1","=","1");
        if(param.getSearchKeys()!=null&&StringUtils.isNotEmpty(param.getSearchKeys().get("flowType"))){
            cnd = cnd.and("flowType","=",param.getSearchKeys().get("flowType"));
        }
        paramData.setCnd(cnd);
        DataTables dataTables  = flowConfigureService.queryFlowConfigurePage(paramData);
        return dataTables;
    }

    @At("/flowTypeList")
    @POST
    @RequiresPermissions("flowConfigure:view")
    public Object flowTypeList(){
        List<FlowConfigureTypeVO> flowConfigureTypeList = flowConfigureService.queryFlowConfigureTypeList();
        return flowConfigureTypeList;
    }

    @At("/flowModuleList")
    @POST
    @RequiresPermissions("flowConfigure:view")
    public Object flowModuleList(){
        List<FlowConfigureModuleVO> flowConfigureModuleVOList = flowConfigureService.queryFlowConfigureModuleList();
        return flowConfigureModuleVOList;
    }

    /**
     * 启用产品
     * @return
     */
    @At("/start")
    @POST
    @RequiresPermissions("flowConfigure:view")
    public Object start(@Param("id")String id){
        NutMap result = new NutMap();
        FlowConfigure flowConfigure =  flowConfigureService.getFlowConfigureById(id);
        List<String> products =  productService.getProducts(flowConfigure.getProductId());
        try {
            for(String productId : products){
                WfTemplet wfTemplet = flowControlTmplService.newConverSnakerXml(productId, flowConfigure.getFlowType().name());
                if(null==wfTemplet){
                    result.put("ok", false);
                    result.put("msg", "启用失败！");
                    return result;
                }
                String processId = flowService.deployNewFlow(productId,flowConfigure.getFlowType().name());
                if(StringUtils.isEmpty(processId)){
                    result.put("ok", false);
                    result.put("msg", "启用失败！");
                    return result;
                }
                productService.bindProcessIdForProduct(productId,processId,flowConfigure.getFlowType());
            }
            Boolean updateResult =flowConfigureService.able(flowConfigure);
            if (!updateResult) {
                result.put("ok", false);
                result.put("msg", "启用失败！");
                return result;
            }
        }catch (Exception e) {
            e.printStackTrace();
            result.put("ok", false);
            result.put("msg", "后台异常");
        }
        result.put("ok", true);
        result.put("msg", "启用成功可以开始业务申请了！");
        return result;
    }
    /**
     * 停止产品
     * @return
     */
    @At("/stop")
    @POST
    @RequiresPermissions("flowConfigure:view")
    public Object stop(@Param("id")String id){

        NutMap result = new NutMap();
        FlowConfigure flowConfigure =  flowConfigureService.getFlowConfigureById(id);
        try {
            List<String> products =  productService.getProducts(flowConfigure.getProductId());
            for(String productId : products){
                productService.unbindProcessIdForProduct(productId,flowConfigure.getFlowType());
            }
            flowConfigureService.disable(flowConfigure);
            result.put("ok", true);
            result.put("msg", "禁用成功！");
        } catch (Exception e) {
            result.put("ok", false);
            result.put("msg", "后台异常");
        }
        return result;
    }
}

package com.kaisa.kams.components.controller.hisotry;

import com.kaisa.kams.components.service.*;
import com.kaisa.kams.components.service.history.ProductImportGerendaiService;
import com.kaisa.kams.components.service.history.ProductImportShulouService;
import com.kaisa.kams.components.utils.ParamData;
import com.kaisa.kams.components.utils.ParamUtil;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.history.ProductImportGerendai;
import com.kaisa.kams.models.history.ProductImportXinYongDai;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.upload.TempFile;
import org.nutz.mvc.upload.UploadAdaptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by lw on 2017/5/8.
 */
@IocBean
@At("/gerendaiImport")
public class GerendaiHistoryRecordImportController {


    @Inject
    private ProductImportShulouService productImportShulouService;

    @Inject
    private ProductImportGerendaiService productImportGerendaiService;
    @Inject
    private ProductTypeService productTypeService;
    @Inject
    private LoanSubjectService loanSubjectService;
    @Inject
    private ChannelService channelService;
    /**
     * 产品历史数据导入页面-个人贷
     */
    @At("/gerendaiIndex")
    @Ok("beetl:/productImport/gerendaiIndex.html")
    @RequiresPermissions("productImport_gerendai:view")
    public Context gerendaiIndex() {
        Context ctx = Lang.context();
        //List<ProductType> typeList=productTypeService.queryAll();
        //查询放款主体
        List<LoanSubject> loanSubjects = loanSubjectService.queryAble();
        List<Channel> channels =  channelService.listAble();
        ctx.set("loanSubjects", loanSubjects);
        //ctx.set("productTypeList", typeList);
        ctx.set("channels",channels);
        return ctx;
    }


    @At("/uploadGerendai")
    @Ok("json")
    @POST
    @AdaptBy(type = UploadAdaptor.class, args = { "/data0/java/uploadTemp" })
    public Object uploadShulou(@Param("Filedata")TempFile file, HttpServletRequest request, HttpServletResponse response){
        NutMap nutMap=new NutMap();
        String msg =  productImportGerendaiService.readExcelFile(file);
        return  nutMap.setv("ok","导入数据成功".equals(msg)?true:false).setv("msg",msg);
    }


    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_gerendai:view")
    public Object listGerendai( HttpServletRequest request){
        NutMap result=new NutMap();
        ParamData  paramData = ParamUtil.getParamFromRequest(request,ProductImportGerendai.class);
        if(StringUtils.isNotEmpty(request.getParameter("excludeStatus"))){
            paramData.getCnd().desc("updateTime");
        }else {
            paramData.getCnd().desc("createTime");
        }
        return productImportGerendaiService.queryPage(paramData);
    }



    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_gerendai:view")
    public Object deleteGerendaiById(@Param("id")String id) {
        NutMap nutMap=new NutMap();
        int num=productImportGerendaiService.delete(id);
        if(num>0){
            return nutMap.setv("ok",true).setv("msg","删除成功.");
        }
        return nutMap.setv("ok",false).setv("msg","删除失败.");
    }


    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_gerendai:view")
    public Object excludeGerendai(@Param("id")String id) {
        NutMap nutMap=new NutMap();
        Map<String,Integer> num=productImportGerendaiService.excludeGerendaiById(id);
        if(num.get("success")>0){
            return nutMap.setv("ok",true).setv("msg","处理成功"+num.get("success")+"条，处理失败"+num.get("failure")+"条");
        }
        return nutMap.setv("ok",false).setv("msg","处理失败"+num.get("failure")+"条");
    }

    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_gerendai:view")
    public Object batchExcludeGerendai(HttpServletRequest request){
        NutMap nutMap=new NutMap();
        ParamData  paramData = ParamUtil.getParamFromRequest(request,ProductImportGerendai.class);
        Map<String,Integer> num  =  productImportGerendaiService.excludeGerendaiByEntity(paramData);
        if(num.get("success")>0){
            return nutMap.setv("ok",true).setv("msg","处理成功"+num.get("success")+"条，处理失败"+num.get("failure")+"条");
        }
        return nutMap.setv("ok",false).setv("msg","处理失败"+num.get("failure")+"条");
    }


    /**
     * 产品历史数据导入页面-信用贷
     */
    @At("/xinyongdaiIndex")
    @Ok("beetl:/productImport/xinyongdaiIndex.html")
    @RequiresPermissions("productImport_xinyongdai:view")
    public Context xinyongdaiIndex() {
        Context ctx = Lang.context();
        //List<ProductType> typeList=productTypeService.queryAll();
        //查询放款主体
        List<LoanSubject> loanSubjects = loanSubjectService.queryAble();
        List<Channel> channels =  channelService.listAble();
        ctx.set("loanSubjects", loanSubjects);
        //ctx.set("productTypeList", typeList);
        ctx.set("channels",channels);
        return ctx;
    }


    @At("/uploadXinyongdai")
    @Ok("json")
    @POST
    @AdaptBy(type = UploadAdaptor.class, args = { "/data0/java/uploadTemp" })
    public Object uploadXinyongdai(@Param("Filedata")TempFile file, HttpServletRequest request, HttpServletResponse response){
        NutMap nutMap=new NutMap();
        String msg =  productImportGerendaiService.readXinyongExcelFile(file);
        return  nutMap.setv("ok","导入数据成功".equals(msg)?true:false).setv("msg",msg);
    }


    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_xinyongdai:view")
    public Object listXinyongdai( HttpServletRequest request){
        NutMap result=new NutMap();
        ParamData  paramData = ParamUtil.getParamFromRequest(request,ProductImportXinYongDai.class);
        if(StringUtils.isNotEmpty(request.getParameter("excludeStatus"))){
            paramData.getCnd().desc("updateTime");
        }else {
            paramData.getCnd().desc("createTime");
        }
        return productImportGerendaiService.queryXinyongdaiPage(paramData);
    }



    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_xinyongdai:view")
    public Object deleteXinyongdaiById(@Param("id")String id) {

        return productImportGerendaiService.deleteXYDById(id);

    }


    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_xinyongdai:view")
    public Object excludeXinyongdai(@Param("id")String id) {
        NutMap nutMap=new NutMap();
        Map<String,Integer> num=productImportGerendaiService.excludeXinyongdaiById(id);
        if(num.get("success")>0){
            return nutMap.setv("ok",true).setv("msg","处理成功"+num.get("success")+"条，处理失败"+num.get("failure")+"条");
        }
        return nutMap.setv("ok",false).setv("msg","处理失败"+num.get("failure")+"条");
    }

    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_xinyongdai:view")
    public Object batchExcludeXinyongdai(HttpServletRequest request){
        NutMap nutMap=new NutMap();
        ParamData  paramData = ParamUtil.getParamFromRequest(request,ProductImportXinYongDai.class);
        Map<String,Integer> num  =  productImportGerendaiService.excludeXinyongdaiByEntity(paramData);
        if(num.get("success")>0){
            return nutMap.setv("ok",true).setv("msg","处理成功"+num.get("success")+"条，处理失败"+num.get("failure")+"条");
        }
        return nutMap.setv("ok",false).setv("msg","处理失败"+num.get("failure")+"条");
    }

}

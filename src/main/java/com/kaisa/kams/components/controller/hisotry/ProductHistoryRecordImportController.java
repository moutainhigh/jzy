package com.kaisa.kams.components.controller.hisotry;

import com.kaisa.kams.components.service.*;
import com.kaisa.kams.components.service.history.ProductImportBaoliService;
import com.kaisa.kams.components.service.history.ProductImportHongbenService;
import com.kaisa.kams.components.service.history.ProductImportPiaojuService;
import com.kaisa.kams.components.service.history.ProductImportShulouService;
import com.kaisa.kams.components.utils.ParamData;
import com.kaisa.kams.components.utils.ParamUtil;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.history.ProductImportBaoli;
import com.kaisa.kams.models.history.ProductImportHongben;
import com.kaisa.kams.models.history.ProductImportPiaoju;
import com.kaisa.kams.models.history.ProductImportShulou;

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
 * Created by zhouchuang on 2017/4/26.
 */
@IocBean
@At("/productImport")
public class ProductHistoryRecordImportController {


    @Inject
    private ProductImportShulouService productImportShulouService;
    @Inject
    private ProductImportHongbenService productImportHongbenService;
    @Inject
    private ProductImportBaoliService productImportBaoliService;
    @Inject
    private ProductTypeService productTypeService;
    @Inject
    private LoanSubjectService loanSubjectService;
    @Inject
    private ChannelService channelService;
    @Inject
    private ProductImportPiaojuService productImportPiaojuService;
    /**
     * 产品历史数据导入页面-赎楼贷
     */
    @At("/shulouIndex")
    @Ok("beetl:/productImport/shulouIndex.html")
    @RequiresPermissions("productImport_shulou:view")
    public Context shulouIndex() {
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

    /**
     * 产品历史数据导入页面-赎楼贷
     */
    @At("/baoliIndex")
    @Ok("beetl:/productImport/baoliIndex.html")
    @RequiresPermissions("productImport_baoli:view")
    public Context baoliIndex() {
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

    /**
     * 产品历史数据导入页面-红本
     */
    @At("/hongbenIndex")
    @Ok("beetl:/productImport/hongbenIndex.html")
    @RequiresPermissions("productImport_hongben:view")
    public Context hongbenIndex() {
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

    /**
     * 产品历史数据导入页面-票据
     */
    @At("/piaojuIndex")
    @Ok("beetl:/productImport/piaojuIndex.html")
    @RequiresPermissions("productImport_piaoju:view")
    public Context piaojuIndex() {
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



    @At("/uploadShulou")
    @Ok("json")
    @POST
    @AdaptBy(type = UploadAdaptor.class, args = { "/data0/java/uploadTemp" })
    public Object uploadShulou(@Param("Filedata")TempFile file, HttpServletRequest request, HttpServletResponse response){
        NutMap nutMap=new NutMap();
        String msg =  productImportShulouService.readExcelFile(file);
        return  nutMap.setv("ok","导入数据成功".equals(msg)?true:false).setv("msg",msg);
    }

    @At("/uploadBaoli")
    @Ok("json")
    @POST
    @AdaptBy(type = UploadAdaptor.class, args = { "/data0/java/uploadTemp" })
    public Object uploadBaoli(@Param("Filedata")TempFile file, HttpServletRequest request, HttpServletResponse response){
        NutMap nutMap=new NutMap();
        String msg =  productImportBaoliService.readExcelFile(file);
        return  nutMap.setv("ok","导入数据成功".equals(msg)?true:false).setv("msg",msg);
    }


    @At("/uploadHongben")
    @Ok("json")
    @POST
    @AdaptBy(type = UploadAdaptor.class, args = { "/data0/java/uploadTemp" })
    public Object uploadHongben(@Param("Filedata")TempFile file, HttpServletRequest request, HttpServletResponse response){
        NutMap nutMap=new NutMap();
        String msg =  productImportHongbenService.readExcelFile(file);
        return  nutMap.setv("ok","导入数据成功".equals(msg)?true:false).setv("msg",msg);
    }

    @At("/uploadPiaoju")
    @Ok("json")
    @POST
    @AdaptBy(type = UploadAdaptor.class, args = { "/data0/java/uploadTemp" })
    public Object uploadPiaoju(@Param("Filedata")TempFile file, HttpServletRequest request, HttpServletResponse response){
        NutMap nutMap=new NutMap();
        String msg =  productImportPiaojuService.readExcelFile(file);
        return  nutMap.setv("ok","导入数据成功".equals(msg)?true:false).setv("msg",msg);
    }



    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_shulou:view")
    public Object listShulou( HttpServletRequest request){
        NutMap result=new NutMap();
        ParamData  paramData = ParamUtil.getParamFromRequest(request,ProductImportShulou.class);
        if(StringUtils.isNotEmpty(request.getParameter("excludeStatus"))){
            paramData.getCnd().desc("updateTime");
        }else {
            paramData.getCnd().desc("createTime");
        }
        return productImportShulouService.queryPage(paramData);
    }

    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_hongben:view")
    public Object listHongben( HttpServletRequest request){
        NutMap result=new NutMap();
        ParamData  paramData = ParamUtil.getParamFromRequest(request,ProductImportHongben.class);
        if(StringUtils.isNotEmpty(request.getParameter("excludeStatus"))){
            paramData.getCnd().desc("updateTime");
        }else {
            paramData.getCnd().desc("createTime");
        }
        return productImportHongbenService.queryPage(paramData);
    }

    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_baoli:view")
    public Object listBaoli( HttpServletRequest request){
        NutMap result=new NutMap();
        ParamData  paramData = ParamUtil.getParamFromRequest(request,ProductImportHongben.class);
        if(StringUtils.isNotEmpty(request.getParameter("excludeStatus"))){
            paramData.getCnd().desc("updateTime");
        }else {
            paramData.getCnd().desc("createTime");
        }
        return productImportBaoliService.queryPage(paramData);
    }

    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_piaoju:view")
    public Object listPiaoju( HttpServletRequest request){
        NutMap result=new NutMap();
        ParamData  paramData = ParamUtil.getParamFromRequest(request,ProductImportPiaoju.class);
        if(StringUtils.isNotEmpty(request.getParameter("excludeStatus"))){
            paramData.getCnd().desc("updateTime");
        }else {
            paramData.getCnd().desc("createTime");
        }
        return productImportPiaojuService.queryPage(paramData);
    }



    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_shulou:view")
    public Object deleteShulouById(@Param("id")String id) {
        NutMap nutMap=new NutMap();
        int num=productImportShulouService.delete(id);
        if(num>0){
            return nutMap.setv("ok",true).setv("msg","删除成功.");
        }
        return nutMap.setv("ok",false).setv("msg","删除失败.");
    }

    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_hongben:view")
    public Object deleteHongbenById(@Param("id")String id) {
        NutMap nutMap=new NutMap();
        int num=productImportHongbenService.delete(id);
        if(num>0){
            return nutMap.setv("ok",true).setv("msg","删除成功.");
        }
        return nutMap.setv("ok",false).setv("msg","删除失败.");
    }

    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_baoli:view")
    public Object deleteBaoliById(@Param("id")String id) {
        NutMap nutMap=new NutMap();
        int num=productImportBaoliService.delete(id);
        if(num>0){
            return nutMap.setv("ok",true).setv("msg","删除成功.");
        }
        return nutMap.setv("ok",false).setv("msg","删除失败.");
    }


    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_piaoju:view")
    public Object deletePiaojuById(@Param("id")String id) {
        NutMap nutMap=new NutMap();
        int num=productImportPiaojuService.delete(id);
        if(num>0){
            return nutMap.setv("ok",true).setv("msg","删除成功.");
        }
        return nutMap.setv("ok",false).setv("msg","删除失败.");
    }


    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_shulou:view")
    public Object excludeShulou(@Param("id")String id) {
        NutMap nutMap=new NutMap();
        Map<String,Integer> num=productImportShulouService.excludeShulouById(id);
        if(num.get("success")>0){
            return nutMap.setv("ok",true).setv("msg","处理成功"+num.get("success")+"条，处理失败"+num.get("failure")+"条");
        }
        return nutMap.setv("ok",false).setv("msg","处理失败"+num.get("failure")+"条");
    }

    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_hongben:view")
    public Object excludeHongben(@Param("id")String id) {
        NutMap nutMap=new NutMap();
        Map<String,Integer> num=productImportHongbenService.excludeById(id);
        if(num.get("success")>0){
            return nutMap.setv("ok",true).setv("msg","处理成功"+num.get("success")+"条，处理失败"+num.get("failure")+"条");
        }
        return nutMap.setv("ok",false).setv("msg","处理失败"+num.get("failure")+"条");
    }

    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_baoli:view")
    public Object excludeBaoli(@Param("id")String id) {
        NutMap nutMap=new NutMap();
        Map<String,Integer> num=productImportBaoliService.excludeById(id);
        if(num.get("success")>0){
            return nutMap.setv("ok",true).setv("msg","处理成功"+num.get("success")+"条，处理失败"+num.get("failure")+"条");
        }
        return nutMap.setv("ok",false).setv("msg","处理失败"+num.get("failure")+"条");
    }



    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_piaoju:view")
    public Object excludePiaoju(@Param("id")String id) {
        NutMap nutMap=new NutMap();
        Map<String,Integer> num=productImportPiaojuService.excludeById(id);
        if(num.get("success")>0){
            return nutMap.setv("ok",true).setv("msg","处理成功"+num.get("success")+"条，处理失败"+num.get("failure")+"条");
        }
        return nutMap.setv("ok",false).setv("msg","处理失败"+num.get("failure")+"条");
    }




    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_shulou:view")
    public Object batchExcludeShulou(HttpServletRequest request){
        NutMap nutMap=new NutMap();
        ParamData  paramData = ParamUtil.getParamFromRequest(request,ProductImportShulou.class);
        Map<String,Integer> num  =  productImportShulouService.excludeShulouByEntity(paramData);
        if(num.get("success")>0){
            return nutMap.setv("ok",true).setv("msg","处理成功"+num.get("success")+"条，处理失败"+num.get("failure")+"条");
        }
        return nutMap.setv("ok",false).setv("msg","处理失败"+num.get("failure")+"条");
    }

    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_hongben:view")
    public Object batchExcludeHongben(HttpServletRequest request){
        NutMap nutMap=new NutMap();
        ParamData  paramData = ParamUtil.getParamFromRequest(request,ProductImportShulou.class);
        Map<String,Integer> num  =  productImportHongbenService.excludeByEntity(paramData);
        if(num.get("success")>0){
            return nutMap.setv("ok",true).setv("msg","处理成功"+num.get("success")+"条，处理失败"+num.get("failure")+"条");
        }
        return nutMap.setv("ok",false).setv("msg","处理失败"+num.get("failure")+"条");
    }


    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_piaoju:view")
    public Object batchExcludePiaoju(HttpServletRequest request){
        NutMap nutMap=new NutMap();
        ParamData  paramData = ParamUtil.getParamFromRequest(request,ProductImportPiaoju.class);
        Map<String,Integer> num  =  productImportPiaojuService.excludeByEntity(paramData);
        if(num.get("success")>0){
            return nutMap.setv("ok",true).setv("msg","处理成功"+num.get("success")+"条，处理失败"+num.get("failure")+"条");
        }
        return nutMap.setv("ok",false).setv("msg","处理失败"+num.get("failure")+"条");
    }
    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_baoli:view")
    public Object batchExcludeBaoli(HttpServletRequest request){
        NutMap nutMap=new NutMap();
        ParamData  paramData = ParamUtil.getParamFromRequest(request,ProductImportBaoli.class);
        Map<String,Integer> num  =  productImportBaoliService.excludeByEntity(paramData);
        if(num.get("success")>0){
            return nutMap.setv("ok",true).setv("msg","处理成功"+num.get("success")+"条，处理失败"+num.get("failure")+"条");
        }
        return nutMap.setv("ok",false).setv("msg","处理失败"+num.get("failure")+"条");
    }
}

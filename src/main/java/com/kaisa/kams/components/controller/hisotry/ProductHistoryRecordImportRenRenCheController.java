package com.kaisa.kams.components.controller.hisotry;

import com.kaisa.kams.components.service.*;
import com.kaisa.kams.components.service.history.ProductImportRenRenCheService;
import com.kaisa.kams.components.utils.ParamData;
import com.kaisa.kams.components.utils.ParamUtil;
import com.kaisa.kams.models.Channel;
import com.kaisa.kams.models.LoanSubject;
import com.kaisa.kams.models.history.ProductImportRenRenChe;
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
 * Created by luoyj on 2017/5/08.
 */
@IocBean
@At("/productImportRenRenChe")
public class ProductHistoryRecordImportRenRenCheController {


    @Inject
    private ProductImportRenRenCheService productImportRenRenCheService;
    @Inject
    private ProductTypeService productTypeService;
    @Inject
    private LoanSubjectService loanSubjectService;
    @Inject
    private ChannelService channelService;
    /**
     * 产品历史数据导入页面-车贷
     */
    @At("/renrencheIndex")
    @Ok("beetl:/productImport/renrencheIndex.html")
    @RequiresPermissions("productImport_renrenche:view")
    public Context chedaiIndex() {
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


    @At("/uploadrenrenche")
    @Ok("json")
    @POST
    @AdaptBy(type = UploadAdaptor.class, args = { "/data0/java/uploadTemp" })
    public Object uploadChedai(@Param("Filedata")TempFile file, HttpServletRequest request, HttpServletResponse response){
        NutMap nutMap=new NutMap();
        String msg =  productImportRenRenCheService.readExcelFile(file);
        return  nutMap.setv("ok","导入数据成功".equals(msg)?true:false).setv("msg",msg);
    }


    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_renrenche:view")
    public Object listRenRenChe( HttpServletRequest request){
        NutMap result=new NutMap();
        ParamData  paramData = ParamUtil.getParamFromRequest(request,ProductImportRenRenChe.class);
        if(StringUtils.isNotEmpty(request.getParameter("excludeStatus"))){
            paramData.getCnd().desc("updateTime");
        }else {
            paramData.getCnd().desc("createTime");
        }
        return productImportRenRenCheService.queryPage(paramData);
    }

//    @At
//    @Ok("json")
//    @POST
//    @RequiresPermissions("productImport_chedai:view")
//    public Object deleteChedaiById(@Param("id")String id) {
//        NutMap nutMap=new NutMap();
//        int num=productImportCheDaiService.delete(id);
//        if(num>0){
//            return nutMap.setv("ok",true).setv("msg","删除成功.");
//        }
//        return nutMap.setv("ok",false).setv("msg","删除失败.");
//    }
    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_renrenche:view")
    public Object excludeRenRenChe(@Param("id")String id) {
        NutMap nutMap=new NutMap();
       Map<String,Integer> num=productImportRenRenCheService.excludeRenRenCheById(id);
        if(num.get("success")>0){
            return nutMap.setv("ok",true).setv("msg","处理成功"+num.get("success")+"条，处理失败"+num.get("failure")+"条");
        }
        return nutMap.setv("ok",false).setv("msg","处理失败"+num.get("failure")+"条");
    }

    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_renrenche:view")
    public Object batchExcludeRenRenChe(HttpServletRequest request){
        NutMap nutMap=new NutMap();
        ParamData  paramData = ParamUtil.getParamFromRequest(request,ProductImportRenRenChe.class);
        Map<String,Integer> num  =  productImportRenRenCheService.excludeRenRenCheByEntity(paramData);
        if(num.get("success")>0){
            return nutMap.setv("ok",true).setv("msg","处理成功"+num.get("success")+"条，处理失败"+num.get("failure")+"条");
        }
        return nutMap.setv("ok",false).setv("msg","处理失败"+num.get("failure")+"条");
    }
    @At
    @Ok("json")
    @POST
    @RequiresPermissions("productImport_renrenche:view")
    public Object deleteRenRenCheById(@Param("id")String id) {
        NutMap nutMap=new NutMap();
        int num=productImportRenRenCheService.delete(id);
        if(num>0){
            return nutMap.setv("ok",true).setv("msg","删除成功.");
        }
        return nutMap.setv("ok",false).setv("msg","删除失败.");
    }
}

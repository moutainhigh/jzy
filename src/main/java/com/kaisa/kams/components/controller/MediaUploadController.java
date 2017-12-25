package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.controller.base.BaseUploadController;
import com.kaisa.kams.components.service.MediaUploadService;

import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.impl.AdaptorErrorContext;
import org.nutz.mvc.upload.TempFile;
import org.nutz.mvc.upload.UploadAdaptor;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by sunwanchao on 2016/11/29.
 */
@IocBean
@At("/media_upload")
public class MediaUploadController extends BaseUploadController{

    @Inject
    private MediaUploadService mediaUploadService;
    /*
   * 附件上传例子
   */
    @AdaptBy(type = UploadAdaptor.class, args = {"${app.root}/WEB-INF/tmp",
            "8192", "utf-8", "20000", "104857600"})
    // 缓存在WEB-INF/tmp里面，文件大小上限100M(1024*1024B*100)单位为(B字节)
    @POST
    @Ok("redirect:/media_upload/list")
    @At("/upload")
    public void upload(@Param("loanId") String loanId,@Param("additional") String additional,@Param("file") TempFile[] tfs,
                       HttpServletRequest hsr,
                       AdaptorErrorContext aec) {
        mediaUploadService.upload(loanId,additional,tfs,hsr,aec);
    }

    @At
    @GET
    @Ok("beetl:/media/list.html")
    public void list(){
    }

    @At
    @GET
    @Ok("json")
    public Object listAll(){
        return mediaUploadService.query();
    }

    @At
    @GET
    @Ok("json")
    public Object listMediaTemplate(@Param("loanId") String loanId){
        return mediaUploadService.queryByLoanId(loanId);
    }

    @At
    @GET
    @Ok("json")
    public boolean updateByLoanIdAndItemName(@Param("loanId") String loanId,@Param("itemName") String itemName){
        mediaUploadService.updateByLoanIdAndItemName(loanId,itemName);
        return true;
    }

    @At
    @GET
    @Ok("json")
    public Object fetchAliOssToken(@Param("dir") String dir) {
       return super.fetchAliOssToken(dir);
    }
}

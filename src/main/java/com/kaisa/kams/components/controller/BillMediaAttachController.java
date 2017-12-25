package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.BillMediaAttachService;
import com.kaisa.kams.components.service.LoanService;
import com.kaisa.kams.components.service.MediaTemplateService;
import com.kaisa.kams.components.service.ProductMediaAttachService;
import com.kaisa.kams.components.service.ProductService;
import com.kaisa.kams.enums.ProductMediaItemType;
import com.kaisa.kams.models.Product;
import com.kaisa.kams.models.ProductMediaAttach;
import com.kaisa.kams.models.ProductMediaAttachDetail;
import com.kaisa.kams.models.ProductMediaItem;

import net.sf.json.JSONArray;

import org.apache.commons.lang.StringUtils;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

import java.util.Date;
import java.util.List;

/**
 * 票据影像Controller
 * Created by lw on 2017/03/01.
 */
@IocBean
@At("/bill_media_attach")
public class BillMediaAttachController {

    @Inject
    private ProductService productService;

    @Inject
    private LoanService loanService;

    @Inject
    private ProductMediaAttachService productMediaAttachService;

    @Inject
    private BillMediaAttachService billMediaAttachService;

    @Inject
    private MediaTemplateService mediaTemplateService;

    @At("/query_bill_media")
    @POST
    @Ok("json")
    public Object queryBillMediaAttach (@Param("productId") String productId){
        NutMap result = new NutMap();

        //查找到产品
        Product prd = productService.fetchEnableProductById(productId);
        if (null == prd) {
            result.put("ok", false);
            result.put("msg", "查找不到产品");
            return result;
        }
        //新增所有的影像资料
        List<ProductMediaItem> productMediaItems = mediaTemplateService.queryByTmp(prd.getMediaTmpId());
        for (ProductMediaItem productMediaItem : productMediaItems) {
            ProductMediaAttach productMediaAttach = new ProductMediaAttach(productMediaItem);
            productMediaAttach.setCreateBy(ShiroSession.getLoginUser().getName());
            productMediaAttach.setCreateTime(new Date());
            productMediaAttach.setUpdateTime(new Date());
            //productMediaAttach.setLoanId(resultLoan.getId());
            productMediaAttachService.add(productMediaAttach);
        }

        //查询影像资料信息
        List<ProductMediaAttach> productMediaAttaches = billMediaAttachService.queryDetailByType(ProductMediaItemType.BILL);
        result.put("data", productMediaAttaches);
        return result;
    }


    @At("/query_bill_media_by_borrwer")
    @POST
    @Ok("json")
    public Object queryBillMediaAttachByborrwer (@Param("masterBorrowerId") String masterBorrowerId,
                                                 @Param("productId") String productId,
                                                 @Param("loanId") String loanId){
        NutMap result = new NutMap();

        //查询贴现人影像模板资料信息
        result = billMediaAttachService.queryBillByBorrowerIdAndType(masterBorrowerId, productId,loanId);
        return result;
    }

    /**
     * 修改票据资料信息
     */
    @At("/update_bill_media")
    @POST
    @Ok("json")
    public Object updateBillMedia(@Param("attachItemStr") String attachItemStr,
                                          @Param("tmplId") String tmplId,
                                          @Param("masterBorrowerId") String masterBorrowerId) {
        NutMap result = new NutMap();

        //验证参数是否合法
        if (StringUtils.isEmpty(attachItemStr)) {
            result.put("ok", false);
            result.put("msg", "无影像资料信息");
            return result;
        }

        //修改影像
        List<ProductMediaAttach> productMediaAttaches = Json.fromJsonAsList(ProductMediaAttach.class, attachItemStr);
        for (ProductMediaAttach productMediaAttach : productMediaAttaches) {
            String mediaDetail = JSONArray.fromObject(productMediaAttach.getProductMediaAttachDetails()).toString();
            List<ProductMediaAttachDetail> mediaDetailList = productMediaAttach.getProductMediaAttachDetails();
            //billMediaAttachService.addProductAndBillMediaAttach(productMediaAttach,mediaDetail,masterBorrowerId,tmplId);
        }
        List<ProductMediaAttach> productMediaAttachesReturn = billMediaAttachService.queryDetailByType(ProductMediaItemType.BILL);

        result.put("ok", true);
        result.put("msg", "保存成功");
        result.put("data", productMediaAttachesReturn);
        return result;
    }

}

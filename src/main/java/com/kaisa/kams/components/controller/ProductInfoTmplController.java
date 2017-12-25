package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.service.*;
import com.kaisa.kams.models.Loan;
import com.kaisa.kams.models.Product;
import com.kaisa.kams.models.ProductInfoTmpl;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

/**
 * 产品信息Controller
 * Created by weid on 2016/11/29.
 */
@IocBean
@At("/product_info_tmpl")
public class ProductInfoTmplController {

    @Inject
    private ProductService productService;

    @Inject
    private LoanService loanService;

    /**
     * 产品模板信息服务层
     */
    @Inject
    private ProductInfoTmplService productInfoTmplService;


    /**
     * 查找模板信息
     * @param loanId
     * @return
     */
    @At("fetch_by_product")
    @GET
    @Ok("json")
    public Object fetchByProduct(@Param("loanId")String loanId){
        NutMap result = new NutMap();
        //获取Loan
        Loan loan =  loanService.fetchById(loanId);

        if(null==loan){
            result.put("ok",false);
            result.put("msg","查找不到录单信息");
            return result;
        }

        //获取到当前的产品
        Product product = productService.fetchEnableProductById(loan.getProductId());

        if (null==product){
            result.put("ok",false);
            result.put("msg","查找不到产品信息");
            return result;
        }

        //查询要加载的模板
        ProductInfoTmpl productInfoTmpl =  productInfoTmplService.fetchById(product.getInfoTmpId());
        result.put("ok",true);
        result.put("msg","查找成功");
        result.put("data",productInfoTmpl);
        return result;
    }






}

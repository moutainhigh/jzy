package com.kaisa.kams.components.controller.product;

import com.kaisa.kams.components.service.product.ProductTypeCascadeService;
import com.kaisa.kams.components.view.product.ProductTypeCascadeView;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;

import java.util.List;


/**
 * Created by pengyueyang on 2017/6/7.
 */
@IocBean
@At("/product_type_cascade")
public class ProductTypeCascadeController {

    @Inject
    private ProductTypeCascadeService productTypeCascadeService;

    @At("/get_list")
    public List<ProductTypeCascadeView> getProductTypeCascadeList() {
        return productTypeCascadeService.getProductTypeCascadeList();
    }

}

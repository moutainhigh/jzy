package com.kaisa.kams.test.components.service.product;

import com.kaisa.kams.components.service.product.ProductTypeCascadeService;
import com.kaisa.kams.components.view.product.ProductTypeCascadeView;
import com.kaisa.kams.test.BaseTest;

import org.junit.Test;
import org.nutz.ioc.loader.annotation.Inject;

import java.util.List;

/**
 * Created by wangqx on 2017/6/12.
 */
public class ProductTypeCascadeServiceTest extends BaseTest {
    @Inject
    ProductTypeCascadeService productTypeCascadeService;

    @Test
    public void testGetProductTypeCascadeList(){
        List<ProductTypeCascadeView> list = productTypeCascadeService.getProductTypeCascadeList();
        assertEquals(5,list.size());
    }

    @Test
    public void testGetProductTypeCascadeService(){
        assertNotNull(productTypeCascadeService);
    }


}

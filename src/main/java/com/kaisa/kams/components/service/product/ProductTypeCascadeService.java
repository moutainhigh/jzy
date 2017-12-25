package com.kaisa.kams.components.service.product;

import com.kaisa.kams.components.view.product.ProductCascadeView;
import com.kaisa.kams.components.view.product.ProductTypeCascadeView;

import org.apache.commons.collections.CollectionUtils;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by pengyueyang on 2017/6/7.
 */
@IocBean
public class ProductTypeCascadeService {

    @Inject
    private Dao dao;

    public List<ProductTypeCascadeView> getProductTypeCascadeList() {
        List<ProductTypeCascadeView> productTypeCascadeViewList = new ArrayList<>();
        String sqlStr = "SELECT" +
                " p.id productId," +
                " p.name productName," +
                " pt.id productTypeId," +
                " pt.name productTypeName" +
                " FROM" +
                " sl_product p" +
                " LEFT JOIN sl_product_type pt ON p.typeId = pt.id" +
                " where p.status='ABLE' order by pt.name";
        Sql sql = Sqls.create(sqlStr);
        sql.setCallback(Sqls.callback.maps());
        dao.execute(sql);
        List<Map> result = sql.getList(Map.class);
        if (CollectionUtils.isNotEmpty(result)) {
            String tmpProductTypeName = (String) result.get(0).get("productTypeName");
            String productTypeId = (String) result.get(0).get("productTypeId");
            String productTypeName = null;
            List<ProductCascadeView> productCascadeViews = new ArrayList<>();
            ProductTypeCascadeView productTypeCascadeView = new ProductTypeCascadeView();
            productTypeCascadeView.setProductTypeName(tmpProductTypeName);
            productTypeCascadeView.setProductTypeId(productTypeId);
            String productName = (String) result.get(0).get("productName");
            String productId = (String) result.get(0).get("productId");
            productCascadeViews.add(new ProductCascadeView(productId, productName));
            for (int i = 1; i < result.size(); i++) {
                productTypeName = (String) result.get(i).get("productTypeName");
                productName = (String) result.get(i).get("productName");
                productId = (String) result.get(i).get("productId");
                if (!tmpProductTypeName.equals(productTypeName)) {
                    productTypeCascadeView.setProducts(productCascadeViews);
                    productTypeCascadeViewList.add(productTypeCascadeView);
                    tmpProductTypeName = productTypeName;
                    productCascadeViews = new ArrayList<>();
                    productTypeCascadeView = new ProductTypeCascadeView();
                    productTypeId = (String) result.get(i).get("productTypeId");
                    productTypeCascadeView.setProductTypeName(productTypeName);
                    productTypeCascadeView.setProductTypeId(productTypeId);
                }
                productCascadeViews.add(new ProductCascadeView(productId, productName));
            }
            productTypeCascadeView.setProducts(productCascadeViews);
            productTypeCascadeViewList.add(productTypeCascadeView);
        }
        return productTypeCascadeViewList;
    }
}

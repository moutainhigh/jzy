package com.kaisa.kams.components.service;

import com.kaisa.kams.components.view.product.ProductTypeNode;
import com.kaisa.kams.enums.GuarantyType;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.Product;
import com.kaisa.kams.models.ProductType;
import org.nutz.dao.Cnd;
import org.nutz.dao.FieldMatcher;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.IdNameEntityService;

import java.util.ArrayList;
import java.util.List;


/**
 * 产品类型服务层
 * Created by lw on 2016/11/23.
 */
@IocBean(fields = "dao")
public class ProductTypeService extends IdNameEntityService<ProductType> {

    @Inject
    private ProductService productService;
    /**
     * 添加
     * @param productType
     * @return
     */
    public boolean add(ProductType productType) {
        if (productType != null) {
            int code = this.getMaxCode();
            if (code>Integer.valueOf(productType.getCode())) {
                productType.setCode(formatCode(code));
            }
            return dao().insert(productType) == null ? false : true;
        }
        return false;
    }

    /**
     * 更新数据
     * @param productType
     * @return
     */
    public boolean update(ProductType productType) {
        if (productType != null) {
            return dao().update(productType, "^(name|description|businessType|status|guarantyType|updateBy|updateTime)$") > 0;
        }
        return false;
    }

    /**
     * 验证名称
     * @param name
     * @param id
     * @return
     */
    public boolean validateName(String name,String id) {
        ProductType productType = dao().fetch(ProductType.class, Cnd.where("name", "=", name));
        if (null == productType) {
            return true;
        }
        if (null != productType && productType.getId().equals(id) ) {
            return true;
        }
        return false;
    }

    /**
     * 是否存在产品
     * @param id
     * @return
     */
    public boolean exitProduct(String id) {
        return dao().count(Product.class,Cnd.where("typeId","=",id))>0;
    }

    /**
     * 产品类型list(根据有无抵押)
     *
     * @return
     */
    public List<ProductType> queryListByType(GuarantyType guarantyType) {
        return dao().query(ProductType.class, Cnd.where("guarantyType", "=", guarantyType));
    }

    /**
     * 产品类型list
     *
     * @return
     */
    public List<ProductType> queryAll() {
        return dao().query(ProductType.class, Cnd.orderBy().asc("code"));
    }

    /**
     * 根据类型id查询产品类型
     *
     * @param id
     * @return
     */
    public ProductType fetchById(String id) {
        if (null != id) {
            return dao().fetch(ProductType.class, id);
        }
        return null;
    }

    /**
     * 根据code查询产品类型
     *
     * @param code
     * @return
     */
    public ProductType fetchByCode(String code) {
        if (null != code) {
            return dao().fetch(ProductType.class, Cnd.where("code", "=", code));
        }
        return null;
    }

    /**
     * 根据name查询产品类型
     *
     * @param name
     * @return
     */
    public ProductType fetchByName(String name) {
        if (null != name) {
            return dao().fetch(ProductType.class, Cnd.where("name", "=", name));
        }
        return null;
    }

    /**
     * 获取当前的最大code
     *
     * @param
     * @return
     */
    private int getMaxCode() {
        int initCode = 1;
        try {
            int count = dao().func(ProductType.class,"count","id");
            if (0 == count) {
                return initCode;
            } else {
                return count + 1;
            }
        }catch (Exception e){
            return initCode;
        }
    }

    private String formatCode(int code){
        return String.format("%02d",code);
    }

    public String getMaxFormatCode(){
        return formatCode(getMaxCode());
    }

    /**
     * 查询所有有效的产品大类
     */
    public List<ProductType> queryAbleAll() {
        return queryAll(PublicStatus.ABLE);
    }

    /**
     * 根据状态查询
     * 产品类型list
     * @return
     */
    public List<ProductType> queryAll(PublicStatus status) {
        FieldMatcher fieldMatcher = new FieldMatcher();
        fieldMatcher.setActived("^id|name|code$");
        return dao().query(ProductType.class, Cnd.where("status","=",status).asc("code"),null,fieldMatcher);
    }

    /**
     * 封装产品一级和二级tree数据
     * @return
     */
    public List<ProductTypeNode> getProductType(){
        List<ProductTypeNode> productTypeNodeList=new ArrayList<ProductTypeNode>();
        List<ProductType> productTypes=queryAbleAll();
        if(productTypes.size()>0){
            for (ProductType productType:productTypes){
                String id=productType.getId();
                ProductTypeNode productTypeNode_f=new ProductTypeNode();
                productTypeNode_f.setId(id);
                productTypeNode_f.setName(productType.getName());
                productTypeNode_f.setOpen(false);
                productTypeNode_f.setPId("0");
                productTypeNodeList.add(productTypeNode_f);
                List<Product> products=productService.queryAbleByType(id);
                if(products.size()>0){
                    for(Product product:products){
                        ProductTypeNode productTypeNode_z=new ProductTypeNode();
                        productTypeNode_z.setId(product.getId());
                        productTypeNode_z.setName(product.getName());
                        productTypeNode_z.setOpen(false);
                        productTypeNode_z.setPId(id);
                        productTypeNodeList.add(productTypeNode_z);
                    }
                }
            }
        }
        return productTypeNodeList;
    }

    public List<ProductTypeNode> getChannelProductType(List<ProductTypeNode> productTypeNodes){
        List <ProductTypeNode> productTypeList=new ArrayList<ProductTypeNode>();
        List<ProductTypeNode> productTypeNodeList=getProductType();
        if(productTypeNodeList.size()>0){
              for(ProductTypeNode productTypeNode_f:productTypeNodeList){
                  for(ProductTypeNode productTypeNode_z:productTypeNodes){
                      if(productTypeNode_f.getId().equals(productTypeNode_z.getId())){
                          productTypeNode_f.setChecked(true);
                      }
                  }
                  productTypeList.add(productTypeNode_f);
              }
        }
        return productTypeList;
    }
}

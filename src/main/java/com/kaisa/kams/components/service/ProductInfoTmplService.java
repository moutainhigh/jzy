package com.kaisa.kams.components.service;

import com.alibaba.druid.util.StringUtils;
import com.kaisa.kams.enums.ProductTempType;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.Loan;
import com.kaisa.kams.models.Product;
import com.kaisa.kams.models.ProductInfoTmpl;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.util.List;

/**
 * 产品信息模板服务层
 * Created by weid on 2016/11/29.
 */
@IocBean(fields = "dao")
public class ProductInfoTmplService extends IdNameEntityService<ProductInfoTmpl> {

    private static final Log log = Logs.get();


    /**
     * 更具状态查找所有产品模板
     *
     * @return
     */
    public List<ProductInfoTmpl> queryAll(PublicStatus status) {
        return dao().query(ProductInfoTmpl.class, Cnd.where("status", "=", status));
    }


    /**
     * 通过LoanId查找
     *
     * @param id
     */
    public ProductInfoTmpl fetchById(String id) {
        return dao().fetch(ProductInfoTmpl.class, Cnd.where("status", "=", PublicStatus.ABLE).and("id", "=", id));
    }


    public ProductInfoTmpl fetchByProductId(String productId) {
        if(StringUtils.isEmpty(productId)){
            return null;
        }
        Product product = this.dao().fetch(Product.class, productId);
        if (product != null) {
            return this.dao().fetch(ProductInfoTmpl.class, product.getInfoTmpId());
        }
        return null;
    }

    public ProductTempType fetchByLoanId(String loanId){
        if(StringUtils.isEmpty(loanId)){
            return null;
        }
        Loan loan = this.dao().fetch(Loan.class,loanId);
        if(loan == null){
            return null;
        }
        ProductInfoTmpl productInfoTmpl = this.fetchByProductId(loan.getProductId());
        if(productInfoTmpl == null){
            return null;
        }
        return productInfoTmpl.getProductTempType();
    }

    public boolean isBill(String loanId){
        ProductTempType productTempType = this.fetchByLoanId(loanId);
        return ProductTempType.isBill(productTempType);
    }
}

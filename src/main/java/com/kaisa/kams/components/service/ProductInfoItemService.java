package com.kaisa.kams.components.service;

import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.Organize;
import com.kaisa.kams.models.ProductInfoItem;
import jdk.nashorn.internal.objects.annotations.Where;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.util.List;

/**
 * Created by weid  on 2016/11/29.
 */
@IocBean(fields="dao")
public class ProductInfoItemService extends IdNameEntityService<ProductInfoItem> {
    private static final Log log = Logs.get();

    /**
     * 通过录单信息查找
     * @param loanId
     * @return
     */
    public List<ProductInfoItem> queryByLoanId(String loanId) {
        return dao().query(ProductInfoItem.class, Cnd.where("loanId","=", loanId));
    }

    /**
     * 新增
     * @param productInfoItem
     * @return
     */
    public  ProductInfoItem add(ProductInfoItem productInfoItem) {
        if(null==productInfoItem){
            return null;
        }
        return dao().insert(productInfoItem);
    }

    /**
     * 新增
     * @param productInfoItemList
     * @return
     */
    public  List add(List<ProductInfoItem> productInfoItemList) {
        if(null==productInfoItemList){
            return null;
        }
        return dao().insert(productInfoItemList);
    }

    /**
     * 删除
     * @param loanId
     * @return
     */
    public boolean deleteByLoanId(String loanId) {
       return dao().clear(ProductInfoItem.class, Cnd.where("loanId","=",loanId))>0;
    }
}

package com.kaisa.kams.components.service;

import com.kaisa.kams.enums.FeeType;
import com.kaisa.kams.models.ProductFee;

import org.nutz.dao.Cnd;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.util.List;

/**
 * Created by weid on 2016/11/30.
 */
@IocBean(fields="dao")
public class ProductFeeService extends IdNameEntityService<ProductFee> {

    private static final Log log = Logs.get();

    private static final String FEE_CODE_PREFIX = "FY";

    private static final int START_CODE = 1;

    /**
     * 新增产品费用
     * @param fee
     * @return
     */
    public ProductFee add(ProductFee fee) {
        fee.setCode(getCode(fee.getCode(),fee.getProductId()));
        return null==fee? null:dao().insert(fee);
    }

    /**
     * 根据产品id查找费用
     * @param productId
     * @return
     */
    public List<ProductFee> queryFeeByProductId(String productId) {
        return dao().query(ProductFee.class, Cnd.where("productId","=",productId).asc("createTime"));
    }

    /**
     * 根据产品id和渠道id查找费用
     * @param productId
     * @return
     */
    public List<ProductFee> queryFeeByProductIdAndChannlType(String productId, List<String> channelType) {
        return dao().query(ProductFee.class, Cnd.where("productId","=",productId).and("channelType","in",channelType).asc("createTime"));
    }

    /**
     * 根据id获取
     * @param id
     * @return
     */
    public ProductFee fetchById(String id) {
        return dao().fetch(ProductFee.class,Cnd.where("id","=",id));
    }

    /**
     * 修改费用
     * @param fee
     * @return
     */
    public boolean update(ProductFee fee) {
        return Daos.ext(dao(), FieldFilter.locked(ProductFee.class, "^id|productId|code|createBy|createTime$")).update(fee)>0;
    }

    /**
     * 获取编码
     * @param productCode
     * @param productId
     * @return
     */
    public String getCode(String productCode,String productId) {

        StringBuffer code = new StringBuffer(FEE_CODE_PREFIX);
        code.append(productCode);
        Object maxCode =  dao().func2(ProductFee.class,"max","code",Cnd.where("productId","=",productId));
        if (null != maxCode) {
            String dataCode = (String)maxCode;
            int originCode = Integer.valueOf(dataCode.substring(dataCode.length()-3));
            code.append(String.format("%03d",originCode+START_CODE));
        } else {
            code.append(String.format("%03d",START_CODE));
        }
        return code.toString();

    }

    /**
     * 删除产品费用
     * @param feeId
     * @return
     */
    public boolean deleteById(String feeId) {
        return dao().delete(ProductFee.class,feeId)>0;
    }


    /**
     * 重复费用
     * @param fee
     * @return
     */
    public boolean isRepeat(ProductFee fee) {
        ProductFee exitFee=null;
        if (null == fee) {
            return false;
        }
        if (null != fee.getId()) {
            if(fee.getFeeType().equals(FeeType.OVERDUE_FEE)||fee.getFeeType().equals(FeeType.PREPAYMENT_FEE)){
                exitFee = dao().fetch(ProductFee.class,Cnd.where("channelType","=",fee.getChannelType()).and("repayMethod", "=", fee.getRepayMethod()).
                        and("feeType", "=", fee.getFeeType()).and("productId","=",fee.getProductId()).and("id","<>",fee.getId()));
            }else {
                exitFee = dao().fetch(ProductFee.class, Cnd.where("feeType", "=", fee.getFeeType()).
                        and("repayMethod", "=", fee.getRepayMethod()).and("productId", "=", fee.getProductId()).and("id", "<>", fee.getId()));
            }
            if (null != exitFee) {
                return true;
            }
        }
        if (null == fee.getId()) {
            if(fee.getFeeType().equals(FeeType.OVERDUE_FEE)||fee.getFeeType().equals(FeeType.PREPAYMENT_FEE)){
                exitFee = dao().fetch(ProductFee.class,Cnd.where("feeType","=",fee.getFeeType()).and("channelType","=",fee.getChannelType()).and("repayMethod", "=", fee.getRepayMethod()).and("productId","=",fee.getProductId()));
            }else {
                exitFee = dao().fetch(ProductFee.class, Cnd.where("feeType", "=", fee.getFeeType()).and("repayMethod", "=", fee.getRepayMethod()).and("productId", "=", fee.getProductId()));
            }
                if (null != exitFee) {
                return true;
            }
        }
        return false;
    }
}

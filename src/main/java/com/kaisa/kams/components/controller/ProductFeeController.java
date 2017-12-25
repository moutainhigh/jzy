package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.ProductFeeService;
import com.kaisa.kams.models.ProductFee;

import org.apache.commons.lang.StringUtils;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by weid  on 2016/11/30.
 */
@IocBean
@At("/product_fee")
public class ProductFeeController {

    @Inject
    private ProductFeeService productFeeService;

    /**
     * 添加费用
     * @param fee
     * @return
     */
    @POST
    @At
    @Ok("json")

    public Object add(@Param("::fee.")ProductFee fee){
        NutMap result = new  NutMap();
        if (null==fee){
            result.put("ok",false);
            result.put("msg","参数验证失败！");
            return result;
        }
        if (productFeeService.isRepeat(fee)) {
            result.put("ok",false);
            result.put("msg","不能添加重复费用！");
            return result;
        }
        fee.setCreateBy(ShiroSession.getLoginUser().getName());
        fee.setUpdateBy(ShiroSession.getLoginUser().getName());
        fee.setCreateTime(new Date());
        fee.setUpdateTime(new Date());

        ProductFee data = productFeeService.add(fee);
        if (null!=data){
            result.put("ok",true);
            result.put("msg","新增成功");
            result.put("data",data);
        }else{
            result.put("ok",false);
            result.put("msg","参数验证失败");
        }
        return result;
    }

    /**
     * 根据产品Id查询所有的产品费用
     * @param productId
     * @return
     */
    @POST
    @Ok("json")

    @At("/list")
    public Object queryByProductId(@Param("productId")String productId){
        NutMap result = new  NutMap();
        try {
            List<ProductFee> feeList = productFeeService.queryFeeByProductId(productId);
            result.put("data", feeList);
        }catch (Exception e) {
            result.put("error","后端处理错误！");
        }
        return result;
    }

    /**
     * 根据Id查询产品费用
     * @param id
     * @return
     */
    @POST
    @At("/get_by_id")
    @Ok("json")
    public Object fetchById(@Param("id")String id){
        NutMap result = new  NutMap();
        ProductFee fee = productFeeService.fetchById(id);
        result.put("ok",true);
        result.put("msg","查找成功");
        result.put("fee",fee);
        return result;
    }

    /**
     * 修改费用
     * @param fee
     * @return
     */
    @POST
    @At
    @Ok("json")
    public Object update(@Param("::fee.")ProductFee fee){
        NutMap result = new  NutMap();
        if (null==fee){
            result.put("ok",false);
            result.put("msg","参数验证失败");
            return result;
        }
        if (productFeeService.isRepeat(fee)) {
            result.put("ok",false);
            result.put("msg","产品费用重复！");
            return result;
        }
        fee.setUpdateBy(ShiroSession.getLoginUser().getName());
        fee.setUpdateTime(new Date());
        boolean flag = productFeeService.update(fee);
        if (flag){
            result.put("ok",true);
            result.put("msg","修改成功");
        }else{
            result.put("ok",false);
            result.put("msg","修改失败");
        }
        return result;
    }


    /**
     * 删除
     * @param feeId
     * @return
     */
    @POST
    @At("/delete")
    @Ok("json")
    public Object delete(@Param("feeId")String feeId){
        NutMap result = new  NutMap();
        if (StringUtils.isEmpty(feeId)){
            result.put("ok",false);
            result.put("msg","参数验证失败");
            return result;
        }
        boolean flag = productFeeService.deleteById(feeId);
        if (flag){
            result.put("ok",true);
            result.put("msg","删除成功");
        }else{
            result.put("ok",false);
            result.put("msg","删除失败");
        }
        return result;
    }


}

package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.*;
import com.kaisa.kams.components.utils.ProductTypesUtils;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouchuang on 2017/7/31.
 */
@IocBean
@At("/product")
public class ProfitController {
    @Inject
    ProductProfitService productProfitService;
    @Inject
    LoanService loanService;
    @Inject
    ProductTypeService productTypeService;
    /**
     * 利润计算数据
     */
    @At("/profit_calculation_data")
    @Ok("beetl:/profit/profit_calculation_data.html")
    @RequiresPermissions("profit_calculation_data:view")
    public Context shulouIndex() {
        Context ctx = Lang.context();
        return ctx;
    }

    /**
     * 产品列表
     * @return
     */
    @At("/profit_calculation_data_page")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("profit_calculation_data:view")
    public Object list(@Param("..")DataTableParam param){
        return productProfitService.queryListAll(param);
    }


    /**
     * 获取产品利润配置
     */
    @At("/get_product_profit_config")
    @Ok("json")
    @RequiresPermissions("profit_calculation_data:view")
    public Object getProductProfit(@Param("id") String id) {
        ProductProfit productProfit =productProfitService.fetchEnableProductProfitById(id);
        return productProfit;
    }


    /**
     * 新增产品利润配置
     */
    @At("/add_product_profit_config")
    @POST
    @Ok("json")
    @RequiresPermissions("profit_calculation_data:view")
    public Object addProductProfit(@Param("::productProfit.") ProductProfit productProfit) {

        NutMap result = new NutMap();
        
        if (null == productProfit ) {
            result.put("ok", false);
            result.put("msg", "产品利润配置信息错误");
            return result;
        }

        ProductProfit existProductProfit = productProfitService.fetchEnableProductProfitByProductTypeId(productProfit.getProductTypeId());
        if(existProductProfit!=null){
            result.put("ok", false);
            result.put("msg", "已经存在对应的产品利润信息");
            return result;
        }
        ProductProfit saveProductProfit = productProfitService.add(productProfit);


        if (saveProductProfit == null) {
            result.put("ok", false);
            result.put("msg", "新增产品利润配置失败");
        } else {
            result.put("ok", true);
            result.put("msg", "新增产品利润配置成功");
            result.put("id", saveProductProfit.getId());
        }
        return result;
    }

    /**
     * 修改产品利润配置
     */
    @At("/edit_product_profit_config")
    @POST
    @Ok("json")
    @RequiresPermissions("profit_calculation_data:view")
    public Object editProductProfit(@Param("::productProfit.") ProductProfit productProfit) {

        NutMap result = new NutMap();
        if (null == productProfit ) {
            result.put("ok", false);
            result.put("msg", "产品利润配置信息错误");
            return result;
        }

        if (StringUtils.isEmpty(productProfit.getId() )) {
            result.put("ok", false);
            result.put("msg", "缺少产品利润主键");
            return result;
        }



        ProductProfit existProductProfit = productProfitService.fetchEnableProductProfitById(productProfit.getId());


        existProductProfit.setInterestRevenue(productProfit.getInterestRevenue());
        existProductProfit.setSurtax(productProfit.getSurtax());
        existProductProfit.setLaborCostSelf(productProfit.getLaborCostSelf());
        existProductProfit.setLaborCostChannel(productProfit.getLaborCostChannel());
        existProductProfit.setAdministrativeExpenses(productProfit.getAdministrativeExpenses());
        existProductProfit.setBrokerageFee(productProfit.getBrokerageFee());
        existProductProfit.setBadAssetsReserve(productProfit.getBadAssetsReserve());
        existProductProfit.setOperatingCostMonth(productProfit.getOperatingCostMonth());
        existProductProfit.setOperatingCostDay(productProfit.getOperatingCostDay());
        existProductProfit.setCapitalCostDay(productProfit.getCapitalCostDay());
        existProductProfit.setCapitalCostMonth(productProfit.getCapitalCostMonth());
        existProductProfit.setTotalTax(productProfit.getTotalTax());
        existProductProfit.setValueAddedTax(productProfit.getValueAddedTax());
        existProductProfit.setUpdateBy(ShiroSession.getLoginUser().getName());
        existProductProfit.setUpdateTime(new Date());




        //修改数据
        boolean flag = productProfitService.update(existProductProfit);


        if (flag) {
            result.put("ok", true);
            result.put("msg", "修改产品利润配置成功");
        } else {
            result.put("ok", false);
            result.put("msg", "修改产品利润配置失败");
        }
        return result;
    }

    /**
     * 查看利润，如果没有实时生成并数据库
     */
    @At("/view_loan_profit")
    @Ok("json")
    public NutMap getLoanProfit(@Param("loanId") String loanId){
        NutMap nutMap=new NutMap();
        nutMap.setv("ok",true).setv("msg","查询成功.");
        LoanProfit loanProfitEntity = productProfitService.fetchEnableLoanProfitByLoanId(loanId);
        if(loanProfitEntity!=null) {
            nutMap.setv("loanProfit",loanProfitEntity);
            return nutMap;
        }

        Loan loan = loanService.fetchById(loanId);

        ProductType productType = productTypeService.fetchById(loan.getProductTypeId());
        if("BAOLIPINGTAI".contains(productType.getProductType())){
            nutMap.setv("ok",true).setv("msg","该产品不做利润计算处理").setv("code","002");
            return nutMap;
        }

        ProductProfit productProfit = productProfitService.fetchEnableProductProfitByProductTypeId(loan.getProductTypeId());
        if(productProfit==null){
            nutMap.setv("ok",true).setv("msg","没有对应产品利润参数配置").setv("code","001");
            return nutMap;
        }

        String type =  productType.getProductType();
        if(StringUtils.isNotEmpty(type)){
            if("PIAOJU".equals(type)){
                LoanProfit returnLoanProfit =  productProfitService.getBillProfit(loan,productProfit);
                nutMap.setv("loanProfit",returnLoanProfit);
                return nutMap;
            }else{
                LoanProfit returnLoanProfit =  productProfitService.getCommonProfit(loan,productProfit);
                nutMap.setv("loanProfit",returnLoanProfit);
                return nutMap;
            }
        }else{
            nutMap.setv("ok",false).setv("msg","产品没有对应类型");
            return nutMap;
        }
    }
}

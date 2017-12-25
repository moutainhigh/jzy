package com.kaisa.kams.components.controller.mobile;

import com.kaisa.kams.components.service.LoanService;
import com.kaisa.kams.components.service.ProductProfitService;
import com.kaisa.kams.components.service.ProductTypeService;
import com.kaisa.kams.models.Loan;
import com.kaisa.kams.models.LoanProfit;
import com.kaisa.kams.models.ProductProfit;
import com.kaisa.kams.models.ProductType;

import org.apache.commons.lang.StringUtils;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

/**
 * Created by zhouchuang on 2017/8/2.
 */
@IocBean
@At("/m/product")
public class MProfitController {

    @Inject
    ProductProfitService productProfitService;
    @Inject
    LoanService loanService;
    @Inject
    ProductTypeService productTypeService;
    /**
     * 查看利润，如果没有实时生成并数据库
     */
    @At("/view_loan_profit")
    @Ok("json")
    public Object getLoanProfit(@Param("loanId") String loanId){
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

/*

    private Object getCommonProfit(NutMap nutMap,Loan loan ,ProductProfit productProfit){

        LoanProfit loanProfit  =  new LoanProfit();
        BigDecimal interest = productProfitService.getTnterest(loan);
        BigDecimal capitalCost = loan.getTermType().equals(LoanTermType.DAYS)?(loan.getAmount().multiply(new BigDecimal(loan.getTerm())).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP).multiply(productProfit.getCapitalCostDay())):
                (loan.getAmount().multiply(new BigDecimal(loan.getTerm())).divide(new BigDecimal(1200),10,BigDecimal.ROUND_HALF_UP).multiply(productProfit.getCapitalCostMonth()));
        BigDecimal valueAddedTax = interest.divide(productProfit.getTotalTax(),10,BigDecimal.ROUND_HALF_UP).multiply(productProfit.getValueAddedTax()).divide(new BigDecimal(100),10,BigDecimal.ROUND_HALF_UP);
        BigDecimal surtax = valueAddedTax.multiply(productProfit.getSurtax()).divide(new BigDecimal(100),10,BigDecimal.ROUND_HALF_UP);
        Channel channel = null;
        if(StringUtils.isNotEmpty(loan.getChannelId())){
            channel = channelService.fetch(loan.getChannelId());
        }
        BigDecimal laborCost  =  channel!=null?(   "0".equals(channel.getChannelType())?productProfit.getLaborCostSelf():productProfit.getLaborCostChannel()   ):productProfit.getLaborCostSelf();
        BigDecimal administrativeExpenses = productProfit.getAdministrativeExpenses();
        BigDecimal badAssetsReserve = loan.getAmount().multiply(productProfit.getBadAssetsReserve()).divide(new BigDecimal(100),10,BigDecimal.ROUND_HALF_UP);
        BigDecimal operatingCost  = loan.getAmount().multiply(new BigDecimal(loan.getTerm())).multiply(loan.getTermType().equals(LoanTermType.DAYS)?(productProfit.getOperatingCostDay().divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP)):
                (productProfit.getOperatingCostMonth().divide(new BigDecimal(1200),10,BigDecimal.ROUND_HALF_UP)));

        // 净利润=利息收入（含税）-资金成本（含税）-增值税-营业税金及附加-人工费用-行政费用-资产坏账拨备-金服运营成本（赎楼，房贷，车押贷）。
        BigDecimal profit = interest.subtract(capitalCost).subtract(valueAddedTax).subtract(surtax).subtract(laborCost).subtract(administrativeExpenses).subtract(badAssetsReserve).subtract(operatingCost);
        loanProfit.setAdministrativeExpenses(administrativeExpenses);
        loanProfit.setBadAssetsReserve(badAssetsReserve);
        loanProfit.setCapitalCost(capitalCost);
        loanProfit.setInterestRevenue(interest);
        loanProfit.setLaborCost(laborCost);
        loanProfit.setOperatingCost(operatingCost);
        loanProfit.setSurtax(surtax);
        loanProfit.setProfit(profit);
        loanProfit.setValueAddedTax(valueAddedTax);
        loanProfit.setLoanId(loan.getId());

        LoanProfit saveLoanProfit =  productProfitService.addLoanProfit(loanProfit);
        nutMap.setv("loanProfit",saveLoanProfit);
        return nutMap;
    }


    private Object getBillProfit(NutMap nutMap,Loan loan ,ProductProfit productProfit){

        BillLoan billLoan = productProfitService.fetchEnableBillLoanByLoanId(loan.getId());
        List<BillLoanRepay>  billLoanRepays =  billLoanService.queryBillLoanRepay(loan.getId());
        LoanProfit loanProfit  =  new LoanProfit();
        BigDecimal interest = billLoan.getInterest();
        BigDecimal valueAddedTax = interest.divide(productProfit.getTotalTax(),10,BigDecimal.ROUND_HALF_UP).multiply(productProfit.getValueAddedTax()).divide(new BigDecimal(100),10,BigDecimal.ROUND_HALF_UP);
        BigDecimal surtax = valueAddedTax.multiply(productProfit.getSurtax()).divide(new BigDecimal(100),10,BigDecimal.ROUND_HALF_UP);
        Channel channel = null;
        if(StringUtils.isNotEmpty(loan.getChannelId())){
            channel = channelService.fetch(loan.getChannelId());
        }
        BigDecimal laborCost  =  channel!=null?(   "0".equals(channel.getChannelType())?productProfit.getLaborCostSelf():productProfit.getLaborCostChannel()   ):productProfit.getLaborCostSelf();
        BigDecimal administrativeExpenses = productProfit.getAdministrativeExpenses();
        BigDecimal badAssetsReserve = billLoan.getTotalAmount().multiply(productProfit.getBadAssetsReserve()).divide(new BigDecimal(100),10,BigDecimal.ROUND_HALF_UP);
        BigDecimal operatingCost  = new BigDecimal(0);
        BigDecimal capitalCost = new BigDecimal(0);
        for(BillLoanRepay billLoanRepay : billLoanRepays){
            capitalCost  = capitalCost.add(billLoan.getTotalAmount().divide(  new BigDecimal(1).add( ( new BigDecimal(billLoanRepay.getDisDays()).multiply(productProfit.getCapitalCostDay()).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP))),10,BigDecimal.ROUND_HALF_UP).multiply( new BigDecimal(billLoanRepay.getDisDays() )).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP).multiply(productProfit.getCapitalCostDay()));
            operatingCost = operatingCost.add(billLoan.getTotalAmount().divide(  new BigDecimal(1).add( ( new BigDecimal(billLoanRepay.getDisDays()).multiply(productProfit.getCapitalCostDay()).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP))),10,BigDecimal.ROUND_HALF_UP).multiply( new BigDecimal(billLoanRepay.getDisDays() )).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP).multiply(productProfit.getOperatingCostDay()));
        };
        BigDecimal brokerageFee  =  productProfit.getBrokerageFee().multiply(billLoan.getIntermediaryTotalFee());

        //净利润=利息收入（含税）-资金成本（含税）-增值税-营业税金及附加-人工费用-行政费用-资产坏账拨备-金服运营成本-居间费及税金（票据融资）
        BigDecimal profit = interest.subtract(capitalCost).subtract(valueAddedTax).subtract(surtax).subtract(laborCost).subtract(administrativeExpenses).subtract(badAssetsReserve).subtract(operatingCost).subtract(brokerageFee);

        loanProfit.setAdministrativeExpenses(administrativeExpenses);
        loanProfit.setBadAssetsReserve(badAssetsReserve);
        loanProfit.setCapitalCost(capitalCost);
        loanProfit.setInterestRevenue(interest);
        loanProfit.setLaborCost(laborCost);
        loanProfit.setOperatingCost(operatingCost);
        loanProfit.setSurtax(surtax);
        loanProfit.setProfit(profit);
        loanProfit.setValueAddedTax(valueAddedTax);
        loanProfit.setBrokerageFee(brokerageFee);
        loanProfit.setLoanId(loan.getId());

        LoanProfit saveLoanProfit =  productProfitService.addLoanProfit(loanProfit);
        nutMap.setv("loanProfit",saveLoanProfit);
        return nutMap;
    }
*/

}

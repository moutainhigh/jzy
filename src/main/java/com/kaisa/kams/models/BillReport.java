package com.kaisa.kams.models;

import com.kaisa.kams.components.utils.excelUtil.ExcelAssistant;
import com.kaisa.kams.components.utils.report.DataConvertUtils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by zhouchuang on 2017/5/17.
 */
@Data
@NoArgsConstructor
public class BillReport {

    //业务单号
    @ExcelAssistant(titleName = "业务单号",width = 5000)
    private String businessOrderNo="";
    //票据号码
    @ExcelAssistant(titleName = "票据号码",width = 8000)
    private String billCode="";
    //出票人
    @ExcelAssistant(titleName = "出票人",width = 8000)
    private String drawer="";
    //贴现人
    @ExcelAssistant(titleName = "贴现人",width = 8000)
    private String discountProposer="";
    //票据类型
    @ExcelAssistant(titleName = "票据类型")
    private String billType="";
    //经办人员
    @ExcelAssistant(titleName = "业务员")
    private String operator="";
    //业务日期
    @ExcelAssistant(titleName = "业务日期")
    private Date businessDate;
    //票据开票日期
    @ExcelAssistant(titleName = "票据开票日期")
    private Date billingDate;
    //票据到期日期
    @ExcelAssistant(titleName = "票据到期日期")
    private Date expireDate;
    //实际到期日
    @ExcelAssistant(titleName = "实际到期日")
    private Date actualExpireDate;
    //票据收款日期
    @ExcelAssistant(titleName = "票据收款日期")
    private Date billReceivablesDate;
    //距离到期天数
    @ExcelAssistant(titleName = "距离到期天数")
    private Integer distanceExpireDay=0;
    //调整天数
    @ExcelAssistant(titleName = "调整天数")
    private Integer adjustedDay=0;
    //总天数
    @ExcelAssistant(titleName = "总天数")
    private Integer totalDay=0;
    //居间费
    @ExcelAssistant(titleName = "居间费（元）",precision=6)
    private BigDecimal intermediaryTotalFee=new BigDecimal(0);
    //居间人
    @ExcelAssistant(titleName = "居间人")
    private String intermediaryName="";
    //票据面值
    @ExcelAssistant(titleName = "票据面值（元）")
    private BigDecimal parValue=new BigDecimal(0);
    //票据利息
    @ExcelAssistant(titleName = "票据利息（元）")
    private BigDecimal billInterest=new BigDecimal(0);
    //其它费用
    @ExcelAssistant(titleName = "其它费用（元）")
    private BigDecimal otherFee=new BigDecimal(0);
    //资金成本
    @ExcelAssistant(titleName = "资金成本（元）")
    private BigDecimal capitalCost=new BigDecimal(0);
    //资金利率（可配置）
    @ExcelAssistant(titleName = "资金利率",precision=6)
    private BigDecimal capitalInterestRate=new BigDecimal(0);
    //日均收入
    @ExcelAssistant(titleName = "日均收入（元）")
    private BigDecimal dailyAverageIncome=new BigDecimal(0);
    //日均成本
    @ExcelAssistant(titleName = "日均成本（元）")
    private BigDecimal dailyAverageCost=new BigDecimal(0);

    private BigDecimal costRate;

    //放款主体
    @ExcelAssistant(titleName = "放款主体")
    private String loanSubject;
    //已兑付金额（元）
    @ExcelAssistant(titleName = "已兑付金额（元）")
    private BigDecimal repayAmount;
    //应还本金
    private BigDecimal receivablePrincipal;
    //实还利息
    private BigDecimal repayInterest;
    //实还费用
    @ExcelAssistant(titleName = "实还费用（元）")
    private BigDecimal repayFee;
    //应还费用
    private BigDecimal receivableFee;
    //实还总计
    @ExcelAssistant(titleName = "实还总计（元）")
    private BigDecimal repayTotal;
    //未兑付金额
    @ExcelAssistant(titleName = "未兑付金额（元）")
    private BigDecimal outstandingAmount;
    private BigDecimal totalAmount;
    //未还利息
    private BigDecimal outstandingInterest;
    //未还费用
    @ExcelAssistant(titleName = "未还费用（元）")
    private BigDecimal outstandingFee;
    //未还合计
    @ExcelAssistant(titleName = "未还合计（元）")
    private BigDecimal outstandingTotal;
    //贷后状态
    @ExcelAssistant(titleName = "贷后状态")
    private String status;

    public void dataConversion(List<ProductRate> list){
        totalDay =(int)((actualExpireDate.getTime()-businessDate.getTime())/(1000*24*3600));
        if(totalDay>0){
            if(billType.equals("银行承兑汇票")){
                capitalInterestRate = new BigDecimal(0.05);
            }else{
                String term = DataConvertUtils.formatTerm(totalDay);
                if(costRate.doubleValue()>0.12){
                    for(ProductRate productRate : list){
                        if(term.equals(productRate.getTerm())){
                            capitalInterestRate = productRate.getCapitalRate();
                            break;
                        }
                    }
                }else{
                    BigDecimal rate = costRate.multiply(new BigDecimal(totalDay)).divide(new BigDecimal(360),10,BigDecimal.ROUND_HALF_EVEN);
                    BigDecimal rzrate = (rate.divide(new BigDecimal(1).subtract(rate),10,BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal(360).divide(new BigDecimal(totalDay),10,BigDecimal.ROUND_HALF_EVEN));
                    BigDecimal capitalCostTemp1  = rzrate.subtract(new BigDecimal(0.03));

                    BigDecimal capitalCostTemp2 = new BigDecimal(0) ;
                    for(ProductRate productRate : list){
                        if(term.equals(productRate.getTerm())){
                            capitalCostTemp2 = productRate.getCapitalRate();
                            break;
                        }
                    }
                    capitalInterestRate  = new BigDecimal(Math.min(capitalCostTemp1.doubleValue(),capitalCostTemp2.doubleValue()));
                }
            }
            if(parValue!=null&&billInterest!=null&&capitalInterestRate.doubleValue()!=0.0){
                capitalCost = (parValue.subtract(billInterest)).multiply(new BigDecimal(totalDay)).multiply(capitalInterestRate).divide(new BigDecimal(360),10,BigDecimal.ROUND_HALF_EVEN);
                dailyAverageIncome =  billInterest.divide(new BigDecimal(totalDay),10,BigDecimal.ROUND_HALF_EVEN);
                dailyAverageCost = capitalCost.divide(new BigDecimal(totalDay),10,BigDecimal.ROUND_HALF_EVEN);
            }
        }
    }

    public void setOtherVal(){
        this.setOutstandingAmount(bigDecimalCompare(parValue.subtract(repayAmount)));
        this.setOutstandingFee(bigDecimalCompare(receivableFee.subtract(repayFee)));
        this.setRepayTotal(repayAmount.add(repayFee));
        this.setOutstandingTotal(bigDecimalCompare(outstandingAmount.add(outstandingFee)));
    }

    private BigDecimal bigDecimalCompare(BigDecimal val){
        BigDecimal result = null;
        if(null != val){
            result = (val).compareTo(BigDecimal.ZERO) >=0 ? val : new BigDecimal(0.00);
        }
        return result;
    }



}

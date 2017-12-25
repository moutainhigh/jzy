package com.kaisa.kams.components.view.report;

import com.kaisa.kams.components.utils.CoverUtil;
import com.kaisa.kams.components.utils.excelUtil.ExcelAssistant;
import com.kaisa.kams.models.BillLoanRepay;
import com.kaisa.kams.models.ProductRate;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zhouchuang on 2017/5/17.
 */
@Data
@NoArgsConstructor
public class GerendaiReportView {

    //业务单号
    @ExcelAssistant(titleName = "业务单号",width = 5000)
    private String businessNumber;
    private String loanId;
    //产品类型
    @ExcelAssistant(titleName = "产品类型",width = 8000)
    private String productType;
    //来源
    @ExcelAssistant(titleName = "来源")
    private String businessSource;
    //业务员
    @ExcelAssistant(titleName = "业务员",width = 8000)
    private String businesser;
    //借款人
    @ExcelAssistant(titleName = "借款人")
    private String borrower;
    //身份证号
    @ExcelAssistant(titleName = "身份证号")
    private String idNumber;
    //放款金额
    @ExcelAssistant(titleName = "放款金额（元）")
    private BigDecimal loanAmount;
    //放款期限
    @ExcelAssistant(titleName = "放款期限")
    private String loanTerm;

    private String termType;
    //年利率
    @ExcelAssistant(titleName = "年利率(%)")
    private BigDecimal yearRate;
    //业务日期
    @ExcelAssistant(titleName = "业务日期")
    private Date businessDate;

    @ExcelAssistant(titleName = "合同到期日")
    private Date loanDueDate;

    @ExcelAssistant(titleName = "实际结清日")
    private Date clearedDate;
    //总天数
    @ExcelAssistant(titleName = "总天数")
    private Integer totalDays;
    //还款方式
    @ExcelAssistant(titleName = "还款方式")
    private String repaymentMethods;

    //资金成本
    @ExcelAssistant(titleName = "资金成本（元）")
    private BigDecimal financeCost;
    //资金利率
    @ExcelAssistant(titleName = "资金利率",precision=6)
    private BigDecimal financeRate;
    //其它费用
    @ExcelAssistant(titleName = "应还费用（元）")
    private BigDecimal otherCharges;
    //合计
    @ExcelAssistant(titleName = "合计（元）")
    private BigDecimal total;
    //日均收入
    @ExcelAssistant(titleName = "日均收入（元）")
    private BigDecimal averageDailyIncome;
    //日均成本
    @ExcelAssistant(titleName = "日均成本（元）")
    private BigDecimal averageDailyCost;
    //月收入
    @ExcelAssistant(titleName = "月收入（元）")
    private BigDecimal monthlyIncome;
    //月成本
    @ExcelAssistant(titleName = "月成本（元）")
    private BigDecimal monthlyCost;
    //放款主体
    @ExcelAssistant(titleName = "放款主体")
    private String loanSubject;
    //应收本金
    @ExcelAssistant(titleName = "应还本金（元）")
    private BigDecimal receivablePrincipal;
    //实还本金
    @ExcelAssistant(titleName = "实还本金（元）")
    private BigDecimal repayAmount;
    //未还本金
    @ExcelAssistant(titleName = "未还本金（元）")
    private BigDecimal outstandingAmount;

    //应收利息
    @ExcelAssistant(titleName = "应还利息（元）")
    private BigDecimal receivableInterest;
    //实还利息
    @ExcelAssistant(titleName = "实还利息（元）")
    private BigDecimal repayInterest;
    //未还利息
    @ExcelAssistant(titleName = "未还利息（元）")
    private BigDecimal outstandingInterest;
    //应收费用
    private BigDecimal receivableFee;
    //实还费用
    @ExcelAssistant(titleName = "实还费用（元）")
    private BigDecimal repayFee;
    //未还费用
    @ExcelAssistant(titleName = "未还费用（元）")
    private BigDecimal outstandingFee;
    //应收合计
    private BigDecimal receivableTotal;
    //实还合计
    @ExcelAssistant(titleName = "实还合计（元）")
    private BigDecimal repayTotal;
    //未还合计
    @ExcelAssistant(titleName = "未还合计（元）")
    private BigDecimal outstandingTotal;

    //贷后状态
    @ExcelAssistant(titleName = "贷后状态")
    private String status;


    public void dataConversion(List<ProductRate> list,int totalDays){
        //total_days =(int)((actual_repayment_date.getTime()-business_date.getTime())/(1000*24*3600));
        if(totalDays>0) {

            //capitalInterestRate = costRate;
            String term = "";
            if (totalDays <= 7) {
                term = "1D-7D";
            } else if (totalDays <= 15) {
                term = "8D-15D";
            } else if (totalDays <= 20) {
                term = "16D-20D";
            } else if (totalDays <= 44) {
                term = "21D-1M";
            } else {
                int month = Math.round((totalDays+15) / 30);
                month=Math.min(12,month);
                term = month + "M";
            }

            for (ProductRate productRate : list) {
                if (term.equals(productRate.getTerm())) {
                    financeRate = productRate.getCapitalRate();
                    break;
                }
            }


        }
        //borrower = CoverUtil.coverName(borrower);
        idNumber = CoverUtil.coverIdNumber(idNumber);
    }

    public void setCommonInfo(FinanceReportRepayData feeData) {
        this.receivablePrincipal = feeData.getReceivablePrincipal();
        this.repayAmount = feeData.getRepayAmount();
        this.outstandingAmount = feeData.getOutstandingAmount();
        this.receivableInterest = feeData.getReceivableInterest();
        this.repayInterest = feeData.getRepayInterest();
        this.outstandingInterest = feeData.getOutstandingInterest();
        this.receivableFee = feeData.getReceivableFee();
        this.repayFee = feeData.getRepayFee();
        this.outstandingFee = otherCharges.subtract(feeData.getRepayFee());
        this.receivableTotal = feeData.getReceivableTotal();
        this.repayTotal = feeData.getRepayTotal();
        this.outstandingTotal = feeData.getOutstandingTotal();
        this.total = feeData.getReceivablePrincipal().add(feeData.getReceivableInterest().add(otherCharges));
    }

    public static void main(String[] args){
        for(Field field : BillLoanRepay.class.getDeclaredFields()){
            System.out.print("sblr."+field.getName()+",");
        }
    }

}

package com.kaisa.kams.models.report;

import com.kaisa.kams.components.utils.excelUtil.ExcelAssistant;
import com.kaisa.kams.components.utils.report.DataConvertUtils;
import com.kaisa.kams.models.ProductRate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Created by luoyj on 2017/5/22.
 * 房产信息表
 */
@Data
@NoArgsConstructor
public class HouseReport{

    //业务单号
    @ExcelAssistant(titleName = "业务单号",width = 5000)
    private String business_number;
    private String loanId;

    //产品类型
    @ExcelAssistant(titleName = "产品类型",width = 8000)
    private String productType;
    //来源
    @ExcelAssistant(titleName = "来源",width = 1000)
    private String source;
    //业务员
    @ExcelAssistant(titleName = "业务员")
    private  String businesser;
    //借款人
    @ExcelAssistant(titleName = "借款人")
    private String borrower;
    //身份证号
    @ExcelAssistant(titleName = "身份证号")
    private String ID_number;
    //放款金额
    @ExcelAssistant(titleName = "放款金额（元）")
    private BigDecimal loan_amount;
    //放款期限（天）
    @ExcelAssistant(titleName = "放款期限（天）")
    private String loan_term;
    private String termType;
    //年利率(%)
    @ExcelAssistant(titleName = "年利率（%）")
    private BigDecimal  year_rate;
    //业务日期
    @ExcelAssistant(titleName = "业务日期")
    private Date business_date;

    @ExcelAssistant(titleName = "合同到期日")
    private  Date  loanDueDate;

    @ExcelAssistant(titleName = "实际结清日")
    private  Date clearedDate;
    //总天数
    @ExcelAssistant(titleName = "总天数")
    private Integer total_days;

    //还款方式
    @ExcelAssistant(titleName = "还款方式")
    private String repaymentMethods;
    //应收本金
    private  BigDecimal receivable_principal;
    //应收利息
    private BigDecimal receivable_interest;
    //其它费用
    @ExcelAssistant(titleName = "应还费用（元）")
    private BigDecimal other_charges;
    //合计
    @ExcelAssistant(titleName = "合计（元）")
    private BigDecimal total;
    //资金成本
    @ExcelAssistant(titleName = "资金成本（元）")
    private BigDecimal finance_cost;
    //资金利率（可配置）
    @ExcelAssistant(titleName = "资金利率",precision=6)
    private BigDecimal finance_rate;

    //日均收入
    @ExcelAssistant(titleName = "日均收入（元）")
    private BigDecimal average_daily_income;
    //日均成本
    @ExcelAssistant(titleName = "日均成本（元）")
    private BigDecimal average_daily_cost;
    //月收入
    @ExcelAssistant(titleName = "月收入（元）")
    private BigDecimal monthly_income;
    //月成本
    @ExcelAssistant(titleName = "月成本（元）")
    private BigDecimal monthly_cost;

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


    public BigDecimal dataConversion(List<ProductRate> list, int totalDays){
        if(totalDays>0) {
            String term = DataConvertUtils.formatTerm(totalDays);
            for (ProductRate productRate : list) {
                if (term.equals(productRate.getTerm())) {
                    finance_rate = productRate.getCapitalRate();
                    break;
                }
            }
        }
        return finance_rate;
    }




}

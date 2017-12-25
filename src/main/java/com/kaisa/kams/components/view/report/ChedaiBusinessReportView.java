package com.kaisa.kams.components.view.report;

import com.kaisa.kams.components.utils.excelUtil.ExcelAssistant;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zhouchuang on 2017/5/17.
 */
@Data
@NoArgsConstructor
public class ChedaiBusinessReportView {

    //业务单号
    @ExcelAssistant(titleName = "业务单号",width = 5000)
    private String businessOrderNo;
    private String loanId;
    //产品名
    @ExcelAssistant(titleName = "产品名",width = 8000)
    private String productName;
    //放款主体
    @ExcelAssistant(titleName = "放款主体",width = 8000)
    private String loanSubject;
    //提单人
    @ExcelAssistant(titleName = "提单人")
    private String submitter;
    //借款人
    @ExcelAssistant(titleName = "借款人")
    private String borrower;
    //证件号码
    @ExcelAssistant(titleName = "证件号码")
    private String idNumber;
    //状态
    @ExcelAssistant(titleName = "状态")
    private String status;
    //渠道
    @ExcelAssistant(titleName = "渠道")
    private String channel;
    private String channelId;

    //机构
    @ExcelAssistant(titleName = "机构")
    private String agencyName;
    //组织
    @ExcelAssistant(titleName = "组织")
    private String organizeName;
    //业务员
    //@ExcelAssistant(titleName = "业务员")
    private String businessName;
    //业务来源
    @ExcelAssistant(titleName = "业务来源")
    private String businessSource;
    private String businessLine;
    private String orgCode;
    //业务提供方
    @ExcelAssistant(titleName = "业务提供方")
    private String businessProvider;
    //借款金额
    @ExcelAssistant(titleName = "借款金额（元）")
    private BigDecimal actualAmount;
    //实还本金
    @ExcelAssistant(titleName = "实还本金（元）")
    private BigDecimal loanPrincipal;
    //借款期限
    @ExcelAssistant(titleName = "借款期限")
    private String loanTerm;
    private String termType;
    //借款利息
    @ExcelAssistant(titleName = "借款利息(%)",precision=6)
    private BigDecimal borrowRate;
    //计息方式
    @ExcelAssistant(titleName = "计息方式")
    private String interestMode;
    private String loanLimitType;

    /* 一次性服务费 */
    @ExcelAssistant(titleName = "应收一次性服务费（元）")
    private BigDecimal prePaymentFeeAmount;
    /* 实收一次性服务费 */
    @ExcelAssistant(titleName = "实收一次性服务费（元）")
    private BigDecimal actualPrePaymentFeeAmount;
    /* 实收罚息 */
    @ExcelAssistant(titleName = "实收罚息（元）")
    private BigDecimal actualOverdueFeeAmount;
    /* 应收总费服务费（包含罚息） */
    @ExcelAssistant(titleName = "应收总费服务费（元）")
    private BigDecimal totalServiceFeeAmount;
    /* 实收总费服务费（包含罚息） */
    @ExcelAssistant(titleName = "实收总费服务费（元）")
    private BigDecimal actualServiceTotalFeeAmount;
    /* 实收总费用 = 实收总服务费 + 实收利息 */
    @ExcelAssistant(titleName = "实收总费用（元）")
    private BigDecimal actualTotalFeeAmount;
    //应收利息
    @ExcelAssistant(titleName = "应收利息（元）")
    private BigDecimal receivableInterest ;
    //实还利息
    @ExcelAssistant(titleName = "实还利息（元）")
    private BigDecimal actualInterest;
    //应收总金额
    @ExcelAssistant(titleName = "应收本息（元）")
    private BigDecimal receivableTotal ;
    // 实还总额 = 实还本金 + 实收总费用
    @ExcelAssistant(titleName = "实收本息（元）")
    private BigDecimal actualTotalAmount;
    //实际放款日期
    @ExcelAssistant(titleName = "实际放款日期")
    private Date actualLoanDate;
    //应还日期
    @ExcelAssistant(titleName = "应还日期")
    private Date dueDate;
    //实际结清日期
    @ExcelAssistant(titleName = "实际结清日期")
    private Date actualClearedDate;
    //最近还款日
    @ExcelAssistant(titleName = "最近还款日")
    private Date interestBearingDate;
    //已还期数
    @ExcelAssistant(titleName = "已还期数")
    private String repaymentNumber;
    //车牌号码
    @ExcelAssistant(titleName = "车牌号码")
    private String carNumber;
    //车辆品牌
    @ExcelAssistant(titleName = "车辆品牌")
    private String carBrand;
    //车辆估值（元）
    @ExcelAssistant(titleName = "车辆估值（元）")
    private String carValuation;
    //还款方式
    @ExcelAssistant(titleName = "还款方式")
    private String repayMethod;
    //提单日期
    @ExcelAssistant(titleName = "提单日期")
    private Date submitTime;
    /**
     * 利润
     */
    @ExcelAssistant(titleName = "净利润（元）")
    private BigDecimal profit;

    public void setCommonInfo(BusinessReportFeeData feeData) {
        this.prePaymentFeeAmount = feeData.getPrePaymentFeeAmount();
        this.actualPrePaymentFeeAmount = feeData.getActualPrePaymentFeeAmount();
        this.actualOverdueFeeAmount = feeData.getActualOverdueFeeAmount();
        this.totalServiceFeeAmount = feeData.getTotalServiceFeeAmount();
        this.actualServiceTotalFeeAmount = feeData.getActualServiceTotalFeeAmount();
    }


}

package com.kaisa.kams.components.view.report;

import com.kaisa.kams.components.utils.excelUtil.ExcelAssistant;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Created by luoyj on 2017/5/26.
 * 赎楼报表信息表
 */
@Data
@NoArgsConstructor
public class HouseBusinessShuLouReport {

    //业务单号
    @ExcelAssistant(titleName = "业务单号",width = 5000)
    private String businessOrderNo;

    private String loanId;

    //产品名
    @ExcelAssistant(titleName = "产品名")
    private String productName;

    //放款主体
    @ExcelAssistant(titleName = "放款主体")
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

    //业务提供方
    @ExcelAssistant(titleName = "业务提供方")
    private String businessProvide;
    private String businessName;

    //机构
    @ExcelAssistant(titleName = "机构")
    private String agencyName;
    //组织
    @ExcelAssistant(titleName = "组织")
    private String organizeName;

    //业务来源
    @ExcelAssistant(titleName = "业务来源")
    private String businessSource;
    private String businessLine;
    private String orgCode;

    //借款金额
    @ExcelAssistant(titleName = "借款金额（元）")
    private BigDecimal loanPrincipal;

    /* 实还本金 */
    @ExcelAssistant(titleName = "实收本金（元）")
    private BigDecimal actualAmount;

    //借款期限
    @ExcelAssistant(titleName = "借款期限")
    private String loanTerm;
    private String termType;

    //费率
    @ExcelAssistant(titleName = "借款利息（%）")
    private String borrowRate;

    //应还利息
    @ExcelAssistant(titleName = "应还利息（元）")
    private BigDecimal receivableInterest;

    //实收利息
    @ExcelAssistant(titleName = "实收利息（元）")
    private BigDecimal actualInterest;

    //应收本息
    @ExcelAssistant(titleName = "应收本息（元）")
    private BigDecimal receivableTotal;

    /* 实收本息 */
    @ExcelAssistant(titleName = "实收本息（元）")
    private BigDecimal actualTotalAmount;

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

    //实际放款日期
    @ExcelAssistant(titleName = "实际放款日期")
    private String actualLoanDate;
    private Date loanTime;

    //应结清日
    @ExcelAssistant(titleName = "应结清日")
    private String dueDate;

    //实际结清日期
    @ExcelAssistant(titleName = "实际结清日期")
    private String actualClearedDate;


    //是否展期
    @ExcelAssistant(titleName = "是否展期")
    private String isExtension;

    //展期期限
    @ExcelAssistant(titleName = "展期期限(天)")
    private String extensionNumber;

    //实际展期期限
    @ExcelAssistant(titleName = "实际展期期限(天)")
    private String actualExtensionNumber;

    //展期到期日
    @ExcelAssistant(titleName = "展期到期日")
    private String extensionDueDate;

    //赎楼银行
    @ExcelAssistant(titleName = "赎楼银行")
    private String redeemFloorBank;

    //审批银行
    @ExcelAssistant(titleName = "审批银行")
    private String approvalBank;

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

    public void setFeeInfo(BusinessReportFeeData feeData) {
        prePaymentFeeAmount = feeData.getPrePaymentFeeAmount();
        actualPrePaymentFeeAmount = feeData.getActualPrePaymentFeeAmount();
        actualOverdueFeeAmount = feeData.getActualOverdueFeeAmount();
        totalServiceFeeAmount = feeData.getTotalServiceFeeAmount();
        actualServiceTotalFeeAmount = feeData.getActualServiceTotalFeeAmount();
    }

}

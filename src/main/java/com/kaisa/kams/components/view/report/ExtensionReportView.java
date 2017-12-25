package com.kaisa.kams.components.view.report;

import com.kaisa.kams.components.utils.excelUtil.ExcelAssistant;
import com.kaisa.kams.enums.LoanRepayMethod;
import com.kaisa.kams.enums.LoanStatus;
import com.kaisa.kams.enums.LoanTermType;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author pengyueyang created on 2017/12/11.
 */
@Data
@NoArgsConstructor
public class ExtensionReportView {

    @ExcelAssistant(titleName = "业务单号",width = 5000)
    private String code;
    private String loanId;

    @ExcelAssistant(titleName = "产品类型")
    private String productName;

    @ExcelAssistant(titleName = "来源")
    private String source;

    @ExcelAssistant(titleName = "业务员")
    private String salesman;

    @ExcelAssistant(titleName = "借款人")
    private String borrower;

    @ExcelAssistant(titleName = "身份证号")
    private String idNumber;

    @ExcelAssistant(titleName = "原始放款金额（元）")
    private String loanAmount;

    @ExcelAssistant(titleName = "放款日期")
    private Date loanTime;

    @ExcelAssistant(titleName = "展期次数")
    private int extensionCount;
    private int maxPosition;

    @ExcelAssistant(titleName = "展期期限")
    private String extensionTermStr;
    private String extensionTerm;
    private LoanTermType extensionTermType;

    @ExcelAssistant(titleName = "展期到期日")
    private Date extensionDueDate;

    @ExcelAssistant(titleName = "实际结清日")
    private Date actualClearDate;

    @ExcelAssistant(titleName = "总天数")
    private String totalDays;

    @ExcelAssistant(titleName = "还款方式")
    private String repayMethod;
    private LoanRepayMethod loanRepayMethod;

    @ExcelAssistant(titleName = "放款主体")
    private String loanSubject;

    @ExcelAssistant(titleName = "展期应还本金（元）")
    private BigDecimal extensionAmount;

    @ExcelAssistant(titleName = "展期实还本金（元）")
    private BigDecimal extensionRepayAmount;

    @ExcelAssistant(titleName = "展期未还本金（元）")
    private BigDecimal extensionRemainAmount;

    @ExcelAssistant(titleName = "展期应还利息（元）")
    private BigDecimal extensionInterest;

    @ExcelAssistant(titleName = "展期实还利息（元）")
    private BigDecimal extensionRepayInterest;

    @ExcelAssistant(titleName = "展期未还利息（元）")
    private BigDecimal extensionRemainInterest;

    @ExcelAssistant(titleName = "贷后状态")
    private String loanStatusStr;
    private LoanStatus loanStatus;

}

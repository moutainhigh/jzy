package com.kaisa.kams.models.report;

import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.excelUtil.ExcelAssistant;
import com.kaisa.kams.components.utils.report.ReportUtils;
import com.kaisa.kams.enums.CalculateMethodAboutDay;
import com.kaisa.kams.enums.LoanLimitType;
import com.kaisa.kams.enums.LoanRepayMethod;
import com.kaisa.kams.enums.LoanTermType;
import com.kaisa.kams.models.BaseModel;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by wangqx on 2017/6/22.
 */
@Table("sl_gerendai_finance_daily_report")
@Data
@NoArgsConstructor
public class GeRenDaiFinanceDailyReport extends BaseModel {

    @ExcelAssistant(titleName = "业务单号", width = 5000)
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 30)
    private String businessOrderNo;

    @ExcelAssistant(titleName = "产品类型", width = 8000)
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 50)
    private String productType;

    @ExcelAssistant(titleName = "业务人员", width = 8000)
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 50)
    private String businessUser;

    @ExcelAssistant(titleName = "借款人")
    @ColDefine(type = ColType.VARCHAR, width = 50)
    @Column
    private String borrower;

    @ExcelAssistant(titleName = "身份证号")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    @Column
    private String idNumber;

    @ExcelAssistant(titleName = "放款金额")
    @ColDefine(type = ColType.FLOAT, width = 14)
    @Column
    private BigDecimal loanAmount;

    @ExcelAssistant(titleName = "放款期限")
    @ColDefine(type = ColType.VARCHAR, width = 20)
    @Column
    private String loanTerm;
    private String term;
    private LoanTermType termType;

    @ExcelAssistant(titleName = "年利率(%)")
    @ColDefine(type = ColType.FLOAT, width = 14)
    @Column
    private BigDecimal yearRate;
    private BigDecimal interestAmount;
    private BigDecimal interestRate;
    private LoanLimitType loanLimitType;

    @ExcelAssistant(titleName = "业务时间")
    @Column
    @ColDefine(type = ColType.DATETIME)
    private Date businessDate;

    @ExcelAssistant(titleName = "合同到期日")
    @Column
    @ColDefine(type = ColType.DATE)
    private Date dueDate;

    @ExcelAssistant(titleName = "实际结清日")
    @Column
    @ColDefine(type = ColType.DATETIME)
    private Date actualRepayDate;

    @ExcelAssistant(titleName = "总天数")
    @Column
    @ColDefine(type = ColType.INT)
    private Integer totalDays;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 60)
    private LoanRepayMethod repayMethod;

    @ExcelAssistant(titleName = "还款方式")
    private String repayMethodStr;

    @ExcelAssistant(titleName = "应收本金（元）")
    @Column
    @ColDefine(type = ColType.FLOAT, width = 14)
    private BigDecimal principal;

    @ExcelAssistant(titleName = "应收利息（元）")
    @Column
    @ColDefine(type = ColType.FLOAT, width = 14)
    private BigDecimal interest;

    @ExcelAssistant(titleName = "其它费用（元）")
    @Column
    @ColDefine(type = ColType.FLOAT, width = 14)
    private BigDecimal otherFee;

    @ExcelAssistant(titleName = "合计（元）")
    @Column
    @ColDefine(type = ColType.FLOAT, width = 14)
    private BigDecimal totalAmount;

    private CalculateMethodAboutDay calculateMethodAboutDay;

    @Column
    @ColDefine(type = ColType.DATE)
    private Date recordDate;

    public void setValues() {
        sumTotalAmount();
        BigDecimal interestRate = convertToInterestRate();
        switch (termType.name()) {
            case "DAYS":
                this.yearRate = ReportUtils.getYearRateByDayInterestRate(interestRate);
                this.loanTerm = ReportUtils.getDaysLoanTerm(term);
                this.totalDays = Integer.valueOf(term);
                break;
            case "MOTHS":
                this.yearRate = ReportUtils.getYearRateByMonthInterestRate(interestRate);
                this.loanTerm = ReportUtils.getMonthsLoanTerm(term);
                this.totalDays = DateUtil.diffTwoDate(dueDate,businessDate);
                break;
            case "YEAS":
                this.yearRate = interestRate;
                this.loanTerm = ReportUtils.getYearsLoanTerm(term);
                this.totalDays = DateUtil.diffTwoDate(dueDate,businessDate);
                break;
            case "FIXED_DATE":
                this.yearRate = ReportUtils.getYearRateByDayInterestRate(interestRate);
                this.loanTerm = ReportUtils.getFixedDateLoanTerm(term);
                this.totalDays = DateUtil.diffTwoDate(dueDate,businessDate);
                if (CalculateMethodAboutDay.CALCULATE_HEAD_AND_TAIL.equals(calculateMethodAboutDay)) {
                    totalDays += 1;
                }
                break;
            default:
                this.yearRate = BigDecimal.ZERO;
                break;
        }
    }

    private void sumTotalAmount() {
        totalAmount = BigDecimal.ZERO;
        if (null != principal) {
            totalAmount = totalAmount.add(principal);
        }
        if (null != interest) {
            totalAmount = totalAmount.add(interest);
        }
        if (null != otherFee) {
            totalAmount = totalAmount.add(otherFee);
        }
    }

    private BigDecimal convertToInterestRate() {
        if (LoanLimitType.FIX_AMOUNT.equals(loanLimitType)) {
            //TODO 换算方法需产品以后确认
            return BigDecimal.ZERO;
        }
        return interestRate;
    }


}

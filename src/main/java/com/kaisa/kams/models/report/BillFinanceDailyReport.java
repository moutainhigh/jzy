package com.kaisa.kams.models.report;

import com.kaisa.kams.components.utils.excelUtil.ExcelAssistant;
import com.kaisa.kams.enums.LoanRepayStatus;
import com.kaisa.kams.models.BaseModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by wangqx on 2017/6/22.
 */
@Table("sl_bill_finance_daily_report")
@Data
@NoArgsConstructor
public class BillFinanceDailyReport extends BaseModel {

    @ExcelAssistant(titleName = "业务单号", width = 5000)
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 30)
    private String businessOrderNo;

    @ExcelAssistant(titleName = "票据号码", width = 8000)
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 120)
    private String billCode;

    @ExcelAssistant(titleName = "出票人", width = 8000)
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private String drawer;

    @ExcelAssistant(titleName = "贴现人", width = 8000)
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 50)
    private String discounter;

    @ExcelAssistant(titleName = "票据类型")
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private String billType;

    @ExcelAssistant(titleName = "业务员")
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private String businessUser = "";

    @ExcelAssistant(titleName = "业务日期")
    @Column
    @ColDefine(type = ColType.DATETIME)
    private Date businessDate;

    @ExcelAssistant(titleName = "票据开票日期")
    @Column
    @ColDefine(type = ColType.DATE)
    private Date billingDate;

    @ExcelAssistant(titleName = "票据到期日期")
    @Column
    @ColDefine(type = ColType.DATE)
    private Date dueDate;

    @Column
    @ColDefine(type = ColType.DATE)
    private Date actualDueDate;

    @ExcelAssistant(titleName = "票据收款日期")
    @Column
    @ColDefine(type = ColType.DATETIME)
    private Date billReceiveDate;

    @ExcelAssistant(titleName = "距离到期天数")
    @Column
    @ColDefine(type = ColType.INT)
    private Integer expireDays;

    private LoanRepayStatus status;


    @ExcelAssistant(titleName = "调整天数")
    @Column
    @ColDefine(type = ColType.INT)
    private Integer adjustedDays;

    @ExcelAssistant(titleName = "总天数")
    @Column
    @ColDefine(type = ColType.INT)
    private Integer totalDays;

    @ExcelAssistant(titleName = "居间费（元）", precision = 2)
    @Column
    @ColDefine(type = ColType.FLOAT, precision = 10, width = 24)
    private BigDecimal intermediaryFee;

    @ExcelAssistant(titleName = "居间人")
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private String intermediaryName;

    @ExcelAssistant(titleName = "票据面值（元）")
    @Column
    @ColDefine(type = ColType.FLOAT, width = 14)
    private BigDecimal billAmount;

    @ExcelAssistant(titleName = "票据利息（元）")
    @Column
    @ColDefine(type = ColType.FLOAT, width = 14)
    private BigDecimal billInterest;

    @ExcelAssistant(titleName = "其它费用（元）")
    @Column
    @ColDefine(type = ColType.FLOAT, width = 14)
    private BigDecimal otherFee;

    @Column
    @ColDefine(type = ColType.DATE)
    private Date recordDate;
}

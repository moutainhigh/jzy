package com.kaisa.kams.models.report;

import com.kaisa.kams.components.utils.excelUtil.ExcelAssistant;
import com.kaisa.kams.models.BaseModel;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import java.math.BigDecimal;

/**
 * Created by zhouchuang on 2017/6/9.
 */
@Table("sl_comprehensive_report")
@Data
@NoArgsConstructor
public class ComprehensiveDayReport extends BaseModel {


    @Column("productType")
    @ColDefine(type = ColType.VARCHAR,width = 60)
    @ExcelAssistant(titleName = "产品类型")
    private String productType="";

    @Column("businessSubmissionNumber")
    @ColDefine(type = ColType.INT,width = 10)
    @ExcelAssistant(titleName = "业务提交笔数")
    private Integer businessSubmissionNumber=0;

    @Column("approvalPassNumber")
    @ColDefine(type = ColType.INT,width = 10)
    @ExcelAssistant(titleName = "审批通过笔数")
    private Integer approvalPassNumber=0;

    @Column("approvalRejectNumber")
    @ColDefine(type = ColType.INT,width = 10)
    @ExcelAssistant(titleName = "审批拒绝笔数")
    private Integer approvalRejectNumber=0;

    @Column("loanNumber")
    @ColDefine(type = ColType.INT,width = 10)
    @ExcelAssistant(titleName = "放款笔数")
    private Integer loanNumber=0;

    @Column("loanMoney")
    @ColDefine(type = ColType.FLOAT, width = 16)
    @ExcelAssistant(titleName = "放款金额（元）")
    private BigDecimal loanMoney=new BigDecimal(0);

    @Column("paymentRecordNum")
    @ColDefine(type = ColType.INT,width = 10)
    @ExcelAssistant(titleName = "计划中还款笔数")
    private Integer paymentRecordNum=0;

    @Column("paymentRecordMoney")
    @ColDefine(type = ColType.FLOAT, width = 16)
    @ExcelAssistant(titleName = "计划中还款金额（元）")
    private BigDecimal paymentRecordMoney=new BigDecimal(0);



    @Column("repaymentNumber")
    @ColDefine(type = ColType.INT,width = 10)
    @ExcelAssistant(titleName = "应还款笔数")
    private Integer repaymentNumber = 0;

    @Column("repaymentMoney")
    @ColDefine(type = ColType.FLOAT, width = 16)
    @ExcelAssistant(titleName = "应还款金额（元）")
    private BigDecimal repaymentMoney=new BigDecimal(0);

    @Column("actualRepaymentNumber")
    @ColDefine(type = ColType.INT,width = 10)
    @ExcelAssistant(titleName = "总回款笔数")
    private Integer actualRepaymentNumber = 0;

    @Column("actualRepaymentMoney")
    @ColDefine(type = ColType.FLOAT, width = 16)
    @ExcelAssistant(titleName = "总回款金额（元）")
    private BigDecimal actualRepaymentMoney=new BigDecimal(0);

    @Column("overdueNumber")
    @ColDefine(type = ColType.INT,width = 10)
     @ExcelAssistant(titleName = "逾期笔数")
     private Integer overdueNumber = 0;

    @Column("overdueMoney")
    @ColDefine(type = ColType.FLOAT, width = 16)
     @ExcelAssistant(titleName = "逾期金额（元）")
     private BigDecimal overdueMoney = new BigDecimal(0);

    @Column("overdueRate")
    @ColDefine(type = ColType.FLOAT, width = 16)
    @ExcelAssistant(titleName = "逾期率",showPercentile = true)
    private BigDecimal overdueRate = new BigDecimal(0);

    @Column("averageLoan")
    @ColDefine(type = ColType.INT,width = 10)
    @ExcelAssistant(titleName = "平均借款期限（天）")
    private Integer averageLoan =0;


    @Column("totalDays")
    @ColDefine(type = ColType.INT,width = 10)
    private Integer totalDays;



}

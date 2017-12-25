package com.kaisa.kams.models.report;

import com.kaisa.kams.components.utils.excelUtil.ExcelAssistant;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by lw on 2017/6/6.
 */
@Data
@NoArgsConstructor
public class BillBusinessReport {

    //业务单号
    @ExcelAssistant(titleName = "业务单号",width = 5000,mergeColumn=true)
    private String businessOrderNo="";
    //票据类型
    @ExcelAssistant(titleName = "票据类型")
    private String billType="";
    //票据号码
    @ExcelAssistant(titleName = "票号",width = 8000)
    private String billCode="";
    //提单人
    @ExcelAssistant(titleName = "提单人",width = 8000)
    private String submitter="";
    //提单日期
    @ExcelAssistant(titleName = "提单日期")
    private Date submitDate;
    //票据金额
    @ExcelAssistant(titleName = "票据金额")
    private BigDecimal billAmount=new BigDecimal(0);
    //票据开票日期
    @ExcelAssistant(titleName = "票据开票日期")
    private Date billingDate;
    //票据到期日期
    @ExcelAssistant(titleName = "实际到期日")
    private Date expireDate;
    //放款主体
    @ExcelAssistant(titleName = "放款主体",width = 8000)
    private String loanSubject;
    //实际放款日期
    @ExcelAssistant(titleName = "实际放款日期")
    private Date actualLoanDate;
    //业务来源
    @ExcelAssistant(titleName = "业务来源")
    private String businessSource;

    private String channelId;
    private String businessLine;
    private String orgCode;
    //付款金额
    @ExcelAssistant(titleName = "付款金额（元）")
    private BigDecimal payAmount;

    //贴现人
    @ExcelAssistant(titleName = "贴现人",width = 8000)
    private String discountPerson="";
    //成本报价
    @ExcelAssistant(titleName = "成本报价(%)")
    private BigDecimal cost;
    //实际贴现天数
    @ExcelAssistant(titleName = "实际贴现天数")
    private Integer discountDays=0;
    //贴现利息
    @ExcelAssistant(titleName = "贴现利息")
    private BigDecimal billInterest=new BigDecimal(0);

    //实付居间费
    @ExcelAssistant(titleName = "实付居间费",precision=2)
    private BigDecimal intermediaryTotalFee=new BigDecimal(0);
    //居间人
    @ExcelAssistant(titleName = "居间人")
    private String intermediaryName="";
    //业务员
    @ExcelAssistant(titleName = "业务员")
    private String saleName="";
    //状态
    @ExcelAssistant(titleName = "票据状态")
    private String status;
    //结清日期
    @ExcelAssistant(titleName = "结清日期")
    private Date clearedDate;
    //业务单状态
    @ExcelAssistant(titleName = "业务单状态")
    private String loanStatus;
    /**
     * 利润
     */
    @ExcelAssistant(titleName = "净利润（元）",mergeColumn=true,mergeRelationColumn="businessOrderNo")
    private BigDecimal profit;


}

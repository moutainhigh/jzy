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
public class ServiceQueryView {

    //业务单号
    @ExcelAssistant(titleName = "业务单号",width = 5000)
    private String code;
    private String id;
    //产品类型
    @ExcelAssistant(titleName = "产品类型",width = 8000)
    private String productTypeName;
    private String productTempType;
    //产品子类
    @ExcelAssistant(titleName = "产品子类",width = 8000)
    private String productName;
    //组织
    @ExcelAssistant(titleName = "组织",width = 8000)
    private String organizeName;
    //业务来源
    @ExcelAssistant(titleName = "业务来源")
    private String saleName;
    //借款人
    @ExcelAssistant(titleName = "借款人")
    private String borrserName;
    //申请金额（元）
    @ExcelAssistant(titleName = "申请金额（元）")
    private BigDecimal amount;
    //期限
    @ExcelAssistant(titleName = "期限")
    private String term;

    private String termType;

    //状态
    @ExcelAssistant(titleName = "状态")
    private String loanStatus;

    //审批状态
    @ExcelAssistant(titleName = "审批状态")
    private String approveStatus;

    //应还日期
    @ExcelAssistant(titleName = "应还日期")
    private Date dueDate;

    private Date submitTime;

    private String nextStatus;
    private String channelId;
    private String businessLine;
    private String orgCode;
    private String agencyName;

}

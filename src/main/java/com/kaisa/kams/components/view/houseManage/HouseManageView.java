package com.kaisa.kams.components.view.houseManage;

import com.kaisa.kams.components.utils.excelUtil.Condition;
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
public class HouseManageView {

    //业务单号
    @ExcelAssistant(titleName = "业务单号",width = 5000)
    private String businessCode;
    private String loanId;
    private String houseId;
    private String id;

    /**
     * 产品名称
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="产品名",NonNull=true)
    private String productName;

    /**
     * 放款主体
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="放款主体",NonNull=true)
    private String loanSubject;

    /**
     * 借款人
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="借款人",NonNull=true)
    private String borrower;
    /**
     * 业务来源
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="业务来源",NonNull=true)
    private String saleName;
    private String businessLine;
    private String orgCode;
    /**
     * 渠道名称
     */
    private String channelId;

    /**
     * 借款金额
     */
    @ExcelAssistant(titleName="借款金额（元）",NonNull=true)
    private BigDecimal loanPrincipal;

    //借款期限
    @ExcelAssistant(titleName = "借款期限")
    private String loanTerm;
    private String termType;

    /**
     * 房产证号
     */
    @ExcelAssistant(titleName = "房产证号")
    private String code;

    /**
     * 权属人
     */
    @ExcelAssistant(titleName = "权属人")
    private String ower;

    /**
     * 房产面积
     */
    @ExcelAssistant(titleName = "房产面积（㎡）")
    private String area;

    /**
     * 房产地址
     */
    @ExcelAssistant(titleName = "房产地址")
    private String address;

    /**
     * 房产估值（万元）
     */
    @ExcelAssistant(titleName = "房产估值（万元）")
    private String price;

    /**
     * 抵押类型
     */
    @ExcelAssistant(titleName = "抵押类型")
    private String mortgageType;


    /**
     * 担保责权
     */
    @ExcelAssistant(titleName = "担保责权(元)")
    private String guaranteeResponsibility;

    //抵押日期
    @ExcelAssistant(titleName = "抵押日期")
    private Date mortgageDate;

    //解押日期
    @ExcelAssistant(titleName = "解押日期")
    private Date noMortgageDate;

    /**
     * 产权状态
     */
    @ExcelAssistant(titleName = "产权状态")
    private String propertyRightStatus;

    /**
     * 存放状态
     */
    @ExcelAssistant(titleName = "存放状态")
    private String storageStatus;

    //入库时间
    @ExcelAssistant(titleName = "入库时间")
    private Date inDate;

    //出库时间
    @ExcelAssistant(titleName = "出库时间")
    private Date outDate;

}

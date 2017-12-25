package com.kaisa.kams.models;

import com.kaisa.kams.components.utils.excelUtil.Condition;
import com.kaisa.kams.components.utils.excelUtil.ExcelAssistant;
import com.kaisa.kams.components.utils.excelUtil.ExcelBaseModel;
import com.kaisa.kams.enums.MortgageType;
import com.kaisa.kams.enums.PropertyRightStatus;
import com.kaisa.kams.enums.StorageStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by liuwen01 on 2017/8/14.
 */
@Table("sl_house_manage")
@Data
@NoArgsConstructor
public class HouseManage extends BaseModel {

    /**
     * loanId
     */
    @Column("loanId")
    private String loanId;



    //业务单号
    @ExcelAssistant(titleName = "业务单号",width = 5000)
    @Column("businessCode")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String businessCode;

    /**
     * 产品名称
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="产品名",NonNull=true)
    @Column("productName")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String productName;

    /**
     * 放款主体
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="放款主体",NonNull=true)
    @Column("loanSubject")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String loanSubject;

    /**
     * 借款人
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="借款人",NonNull=true)
    @Column("borrower")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String borrower;
    /**
     * 业务来源
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="业务来源",NonNull=true)
    @Column("saleName")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String saleName;
    private String businessLine;
    private String orgCode;

    /**
     * 业务员
     */
    @Column("saleMan")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String saleMan;

    /**
     * 渠道名称
     */
    @Column("channelId")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String channelId;

    /**
     * 渠道类型
     */
    @Column("channelType")
    private String channelType;

    /**
     * 借款金额
     */
    @ExcelAssistant(titleName="借款金额（元）",NonNull=true)
    @Column("loanPrincipal")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal loanPrincipal;

    //放款日期
    @ExcelAssistant(titleName = "放款时间")
    @Column("loanTime")
    @ColDefine(type = ColType.DATETIME)
    private Date loanTime;

    //借款期限
    @ExcelAssistant(titleName = "借款期限")
    @Column("loanTerm")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String loanTerm;
    private String termType;

    /**
     * 房产证号
     */
    @ExcelAssistant(titleName = "房产证号")
    @Column("code")
    @ColDefine(type = ColType.VARCHAR, width=250)
    private String code;

    /**
     * 权属人
     */
    @ExcelAssistant(titleName = "权属人")
    @Column("ower")
    @ColDefine(type = ColType.VARCHAR, width=250)
    private String ower;

    /**
     * 房产面积
     */
    @ExcelAssistant(titleName = "房产面积（㎡）")
    @Column("area")
    @ColDefine(type = ColType.FLOAT, width=16)
    private String area;

    /**
     * 房产地址
     */
    @Column("address")
    @ColDefine(type = ColType.VARCHAR, width=150)
    @ExcelAssistant(titleName = "房产地址")
    private String address;

    /**
     * 房产估值（万元）
     */
    @ExcelAssistant(titleName = "房产估值（万元）")
    @Column("price")
    @ColDefine(type = ColType.FLOAT, width=16)
    private String price;


    /**
     * 抵押类型
     */
    @Column("mortgageType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private MortgageType mortgageType;

    @ExcelAssistant(titleName = "抵押类型")
    private String mortgageTypeStr;

    /**
     * 担保责权
     */
    @ExcelAssistant(titleName = "担保债权(元)",precision=2)
    @Column("guaranteeResponsibility")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private BigDecimal guaranteeResponsibility;

    //抵押日期
    @ExcelAssistant(titleName = "抵押日期")
    @Column("mortgageDate")
    @ColDefine(type = ColType.DATETIME)
    private Date mortgageDate;

    //解押日期
    @ExcelAssistant(titleName = "解押日期")
    @Column("noMortgageDate")
    @ColDefine(type = ColType.DATETIME)
    private Date noMortgageDate;

    /**
     * 产权状态
     */
    @Column("propertyRightStatus")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private PropertyRightStatus propertyRightStatus;

    @ExcelAssistant(titleName = "产权状态")
    private String propertyRightStatusStr;

    /**
     * 存放状态
     */

    @Column("storageStatus")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private StorageStatus storageStatus;

    @ExcelAssistant(titleName = "存放状态")
    private String storageStatusStr;

    //入库时间
    @ExcelAssistant(titleName = "入库时间")
    @Column("inDate")
    @ColDefine(type = ColType.DATETIME)
    private Date inDate;

    //出库时间
    @ExcelAssistant(titleName = "出库时间")
    @Column("outDate")
    @ColDefine(type = ColType.DATETIME)
    private Date outDate;

    /**
     * 支持多附件保存urls
     */
    @Column("fileUrls")
    @ColDefine(type = ColType.TEXT)
    private String fileUrls;

    //到期日
    @ExcelAssistant(titleName = "到期日")
    @Column("dueDate")
    @ColDefine(type = ColType.DATETIME)
    private Date dueDate;

    //展期到期日
    @ExcelAssistant(titleName = "展期到期日")
    @Column("extensionDueDate")
    @ColDefine(type = ColType.DATETIME)
    private Date extensionDueDate;

}

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
 * Created by zhouchuang on 2017/5/24.
 */
@Table("sl_risk_control_report")
@Data
@NoArgsConstructor
public class RiskControlReport extends BaseModel {

    @Column("productType")
    @ColDefine(type = ColType.VARCHAR,width = 60)
    @ExcelAssistant(mergeTitleName  = "产品大类",titleName = "产品大类",mergeColumn=true)
    private String productType;


    @Column("productName")
    @ColDefine(type = ColType.VARCHAR,width = 60)
    @ExcelAssistant(mergeTitleName  = "产品子类",titleName = "产品子类",width = 5000)
    private String productName;


    //要求用产品编码排序，用于排序，并没有什么卵用
    @Column("code")
    @ColDefine(type = ColType.VARCHAR,width = 60)
    private String code;


    private String lastljfkCountLoanId  = "";
    @Column("ljfkCount")
    @ColDefine(type = ColType.INT,width = 10)
    @ExcelAssistant(mergeTitleName = "累计放款",titleName="笔数")
    private Integer ljfkCount=0;

    @Column("ljfkMoney")
    @ColDefine(type = ColType.FLOAT, width = 16)
    @ExcelAssistant(mergeTitleName = "累计放款",titleName="金额（元）")
    private BigDecimal ljfkMoney=new BigDecimal(0);


    private String lastDsdkCountLoanId  = "";
    @Column("dsdkCount")
    @ColDefine(type = ColType.INT,width = 10)
    @ExcelAssistant(mergeTitleName = "待收贷款",titleName="笔数")
    private Integer dsdkCount=0;

    @Column("dsdkPrincipal")
    @ColDefine(type = ColType.FLOAT, width = 16)
    @ExcelAssistant(mergeTitleName = "待收贷款",titleName="待收本金（元）")
    private BigDecimal dsdkPrincipal=new BigDecimal(0);

    @Column("dsdkInterest")
    @ColDefine(type = ColType.FLOAT, width = 16)
    @ExcelAssistant(mergeTitleName = "待收贷款",titleName="待收利息（元）")
    private BigDecimal dsdkInterest=new BigDecimal(0);


    private String lastyq030CountLoanId  = "";
    @Column("yq030Count")
    @ColDefine(type = ColType.INT,width = 10)
    @ExcelAssistant(mergeTitleName = "逾期0-30天",titleName="笔数")
    private Integer yq030Count=0;

    @Column("yq030Principal")
    @ColDefine(type = ColType.FLOAT, width = 16)
    @ExcelAssistant(mergeTitleName = "逾期0-30天",titleName="逾期本金（元）")
    private BigDecimal yq030Principal=new BigDecimal(0);

    @Column("yq030Interest")
    @ColDefine(type = ColType.FLOAT, width = 16)
    @ExcelAssistant(mergeTitleName = "逾期0-30天",titleName="逾期利息（元）")
    private BigDecimal yq030Interest=new BigDecimal(0);


    private String lastyq3160CountLoanId  = "";
    @Column("yq3160Count")
    @ColDefine(type = ColType.INT,width = 10)
    @ExcelAssistant(mergeTitleName = "逾期31-60天",titleName="笔数")
    private Integer yq3160Count=0;

    @Column("yq3160Principal")
    @ColDefine(type = ColType.FLOAT, width = 16)
    @ExcelAssistant(mergeTitleName = "逾期31-60天",titleName="逾期本金（元）")
    private BigDecimal yq3160Principal=new BigDecimal(0);

    @Column("yq3160Interest")
    @ColDefine(type = ColType.FLOAT, width = 16)
    @ExcelAssistant(mergeTitleName = "逾期31-60天",titleName="逾期利息（元）")
    private BigDecimal yq3160Interest=new BigDecimal(0);


    private String lastyq6190CountLoanId  = "";
    @Column("yq6190Count")
    @ColDefine(type = ColType.INT,width = 10)
    @ExcelAssistant(mergeTitleName = "逾期61-90天",titleName="笔数")
    private Integer yq6190Count=0;

    @Column("yq6190Principal")
    @ColDefine(type = ColType.FLOAT, width = 16)
    @ExcelAssistant(mergeTitleName = "逾期61-90天",titleName="逾期本金（元）")
    private BigDecimal yq6190Principal=new BigDecimal(0);

    @Column("yq6190Interest")
    @ColDefine(type = ColType.FLOAT, width = 16)
    @ExcelAssistant(mergeTitleName = "逾期61-90天",titleName="逾期利息（元）")
    private BigDecimal yq6190Interest=new BigDecimal(0);


    private String lastyq91180CountLoanId  = "";
    @Column("yq91180Count")
    @ColDefine(type = ColType.INT,width = 10)
    @ExcelAssistant(mergeTitleName = "逾期91-180天",titleName="笔数")
    private Integer yq91180Count=0;

    @Column("yq91180Principal")
    @ColDefine(type = ColType.FLOAT, width = 16)
    @ExcelAssistant(mergeTitleName = "逾期91-180天",titleName="逾期本金（元）")
    private BigDecimal yq91180Principal=new BigDecimal(0);

    @Column("yq91180Interest")
    @ColDefine(type = ColType.FLOAT, width = 16)
    @ExcelAssistant(mergeTitleName = "逾期91-180天",titleName="逾期利息（元）")
    private BigDecimal yq91180Interest=new BigDecimal(0);


    private String lastyq180CountLoanId  = "";
    @Column("yq180Count")
    @ColDefine(type = ColType.INT,width = 10)
    @ExcelAssistant(mergeTitleName = "逾期180天以上",titleName="笔数")
    private Integer yq180Count=0;

    @Column("yq180Principal")
    @ColDefine(type = ColType.FLOAT, width = 16)
    @ExcelAssistant(mergeTitleName = "逾期180天以上",titleName="逾期本金（元）")
    private BigDecimal yq180Principal=new BigDecimal(0);

    @Column("yq180Interest")
    @ColDefine(type = ColType.FLOAT, width = 16)
    @ExcelAssistant(mergeTitleName = "逾期180天以上",titleName="逾期利息（元）")
    private BigDecimal yq180Interest=new BigDecimal(0);


    private String lastyqTotalCountLoanId  = "";
    @Column("yqTotalCount")
    @ColDefine(type = ColType.INT,width = 10)
    @ExcelAssistant(mergeTitleName = "总逾期笔数",titleName="总逾期笔数")
    private Integer yqTotalCount=0;





}

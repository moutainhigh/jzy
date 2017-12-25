package com.kaisa.kams.models.history;

import com.kaisa.kams.components.utils.excelUtil.Condition;
import com.kaisa.kams.components.utils.excelUtil.ExcelAssistant;
import com.kaisa.kams.components.utils.excelUtil.ExcelBaseModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by zhouchuang on 2017/5/8.
 */
@Table("sl_product_import_piaoju")
@Data
@NoArgsConstructor
public class ProductImportPiaoju extends ExcelBaseModel {

//    /**
//     * 编号
//     */
//    @ExcelAssistant(titleName="编号",NonNull=true)
//    @Column("number")
//    @ColDefine(type = ColType.INT, width=10)
//    private int number;

    /**
     * 调整天数
     */
    @ExcelAssistant(titleName="调整天数",NonNull=true)
    @Column("adjDays")
    @ColDefine(type = ColType.FLOAT,precision = 0)
    private BigDecimal adjDays;

    /**
     * 票据号码
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="票据号码",NonNull=true)
    @Column("billNo")
    @ColDefine(type = ColType.VARCHAR, width=128)
    private String billNo;
    /**
     * 开票人 付款人/出票人
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="付款人/出票人",NonNull=true)
    @Column("drawer")
    @ColDefine(type = ColType.VARCHAR, width=128)
    private String drawer;

    /**
     * 付款人账号
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="付款人账号",NonNull=true)
    @Column("payAcount")
    @ColDefine(type = ColType.VARCHAR, width=128)
    private String payAcount;


    /**
     * 收款人
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="收款人",NonNull=true)
    @Column("payee")
    @ColDefine(type = ColType.VARCHAR, width=128)
    private String payee;


    /**
     * 贴现申请人
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="贴现申请人",NonNull=true)
    @Column("discountProposer")
    @ColDefine(type = ColType.VARCHAR, width=128)
    private String discountProposer;


    /**
     * 付款银行
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="付款银行")
    @Column("payingBank")
    @ColDefine(type = ColType.VARCHAR, width=128)
    private String payingBank;


    /**
     * 类型  商票/银票
     */
    @ExcelAssistant(titleName="商票/银票",NonNull=true)
    @Column("type")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String type;


    /**
     * 贴现日期
     */
    @ExcelAssistant(titleName="贴现日期",NonNull=true)
    @Column("discountDate")
    private Date discountDate;

    /**
     * 出票日
     */
    @ExcelAssistant(titleName="出票日",NonNull=true)
    @Column("billingDate")
    private Date billingDate;

    /**
     * 到期日
     */
    @ExcelAssistant(titleName="到期日",NonNull=true)
    @Column("expireDate")
    private Date expireDate;


    /**
     * 票面金额
     */
    @ExcelAssistant(titleName="出票金额",NonNull=true)
    @Column("parValue")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal parValue;
    /**
     * 贴现利息
     */
    @ExcelAssistant(titleName="贴现利息",NonNull=true)
    @Column("discountInterest")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal discountInterest;

    /**
     * 付款金额
     */
    @ExcelAssistant(titleName="付款金额",NonNull=true)
    @Column("payAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal payAmount;

    /**
     * 底价
     */
    @ExcelAssistant(titleName="底价",NonNull=true)
    @Column("price")
    @ColDefine(type = ColType.FLOAT, width=16,precision = 6)
    private BigDecimal price;

    /**
     * 贴现天数
     * */
    @ExcelAssistant(titleName="贴现天数",NonNull=true)
    @Column("discountDays")
    @ColDefine(type = ColType.FLOAT, precision = 0)
    private BigDecimal discountDays;

    @Override
    public void dataConversion(){

    }

    public String toString(){
        return this.billNo+this.drawer+this.payee+this.discountProposer+this.payingBank+this.type+this.discountDate+this.billingDate+this.expireDate+this.parValue+this.discountInterest;
    }


    public static void main(String[] args){
       for(Field field : ProductImportPiaoju.class.getDeclaredFields()){
           System.out.print("this."+field.getName()+"+");
       }
    }

    @Override
    public boolean validata(){
        boolean baseValidata =  super.validata();
        return !"01".equals(super.importMsg);
    }
}

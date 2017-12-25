package com.kaisa.kams.models;

import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.DecimalFormatUtils;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Comment;
import org.nutz.dao.entity.annotation.One;
import org.nutz.dao.entity.annotation.Table;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zhouchuang on 2017/9/15.
 */
@Table("sl_mortgage_house")
@Data
@NoArgsConstructor
public class MortgageHouse extends BaseModel {



    @Column("sortNo")
    @Comment("排序")
    private int sortNo;

    @Column("mortgageId")
    @Comment("抵押ID")
    @ColDefine(type = ColType.VARCHAR,width = 64)
    private String mortgageId;

    @Column("houseId")
    @Comment("房子ID")
    @ColDefine(type = ColType.VARCHAR,width = 64)
    private String houseId;

    @One(target =House.class,field ="houseId" )
    private House house;

    @Column("loanInterestRate")
    @Comment("借款利率")
    @ColDefine(type = ColType.FLOAT, width=16,precision = 10)
    private BigDecimal loanInterestRate;
    public String loanInterestRate(){
        return DecimalFormatUtils.removeZeroFormat(this.loanInterestRate)+"";
    }

    @Column("maximumLoanAmount")
    @Comment("最高借款金额")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal maximumLoanAmount;

    @Column("internalEvaluationValue")
    @Comment("内部评估价值")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal internalEvaluationValue;


    private String chineseNumer="零,壹,贰,叁,肆,伍,陆,柒,捌,玖,拾";
    private String chineseUnit=",拾,佰,仟,万,拾,佰,仟,亿,拾,佰,仟,兆,拾,佰,仟,京,拾,佰,仟,垓";
    public String loanAmountUnit(){
        if(DecimalFormatUtils.isNotEmpty(internalEvaluationValue)){
            return internalEvaluationValue+"（元）";
        }else{
            return maximumLoanAmount+"（元）";
        }
    }
    public String chineseLoanAmount(){
        String[] cnnum = chineseNumer.split(",");
        String[] cnunit = chineseUnit.split(",");
        String loanmoneycn  = "";
        if(DecimalFormatUtils.isNotEmpty(internalEvaluationValue)){
            loanmoneycn = internalEvaluationValue.toString().split("\\.")[0];
        }else{
            loanmoneycn = maximumLoanAmount.toString().split("\\.")[0];
        }
        String chiesemoney = "";
        for(int i=0;i<loanmoneycn.length();i++){
            chiesemoney += cnnum[Integer.parseInt(loanmoneycn.substring(i,i+1))]+(Integer.parseInt(loanmoneycn.substring(i,i+1))==0&&(loanmoneycn.length()-i-1)%4!=0?"":cnunit[loanmoneycn.length()-i-1]);
            chiesemoney  =  chiesemoney.replace("零京","京零");
            chiesemoney  =  chiesemoney.replace("京兆","京");
            chiesemoney  =  chiesemoney.replace("京亿","京");
            chiesemoney  =  chiesemoney.replace("京万","京");
            chiesemoney  =  chiesemoney.replace("零兆","兆零");
            chiesemoney  =  chiesemoney.replace("兆亿","兆");
            chiesemoney  =  chiesemoney.replace("兆万","兆");
            chiesemoney  =  chiesemoney.replace("零亿","亿零");
            chiesemoney  =  chiesemoney.replace("亿万","亿");
            chiesemoney  =  chiesemoney.replace("零万","万零");
            chiesemoney = chiesemoney.replaceAll("零+","零");

        }
        if(chiesemoney.endsWith("零")){
            chiesemoney = chiesemoney.substring(0,chiesemoney.length()-1);
        }
       return chiesemoney;
    }

    @Column("startBorrowingTime")
    @Comment("开始借款时间")
    @ColDefine(type = ColType.DATETIME)
    private Date startBorrowingTime;
    public String startBorrowingTime(){
        return DateUtil.formatDateToString(this.startBorrowingTime);
    }

    private int startY;
    private int startM;
    private int startD;

    private int endY;
    private int endM;
    private int endD;

    public void splitTime(){
        if(startBorrowingTime!=null){
            startY =  startBorrowingTime.getYear()+1900;
            startM =  startBorrowingTime.getMonth()+1;
            startD =  startBorrowingTime.getDate();
        }
        if(endBorrowingTime!=null){
            endY = endBorrowingTime.getYear()+1900;
            endM = endBorrowingTime.getMonth()+1;
            endD = endBorrowingTime.getDate();
        }
    }

    @Column("endBorrowingTime")
    @Comment("最后借款时间")
    @ColDefine(type = ColType.DATETIME)
    private Date endBorrowingTime;
    public String endBorrowingTime(){
        return DateUtil.formatDateToString(this.endBorrowingTime);
    }


    private String borrowingTime;

}
package com.kaisa.kams.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhouchuang on 2017/6/15.
 */
@Data
@NoArgsConstructor
public class ClearedProof {
    private String borrowerID;
    private String borrowerName;
    private String code;
    private BigDecimal loanMoney=new BigDecimal(0);
    private List<HashMap<String,String>> common = new ArrayList<HashMap<String,String>>();
    private Date loanDate;
    private Date repayDate;
    private Date proofDate;
    private String loanCompany;
    private String certiType;
    private String chineseNumer="零,壹,贰,叁,肆,伍,陆,柒,捌,玖,拾";
    private String chineseUnit=",拾,佰,仟,万,拾,佰,仟,亿,拾,佰,仟,兆,拾,佰,仟,京,拾,佰,仟,垓";
    public String  loanMoneyChinese(){
        String[] cnnum = chineseNumer.split(",");
        String[] cnunit = chineseUnit.split(",");
        String loanmoneycn = loanMoney.toString().split("\\.")[0];
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
        chiesemoney +="元";
        if(loanMoney.toString().contains(".")&&!loanMoney.toString().split("\\.")[1].startsWith("00")){
            String ps  = loanMoney.toString().split("\\.")[1];
            if(Integer.parseInt(ps.substring(0,1))>0)
                chiesemoney += cnnum[Integer.parseInt(ps.substring(0,1))]+"角";
            if(ps.length()>1)
                if(Integer.parseInt(ps.substring(1,2))>0)
                    chiesemoney += cnnum[Integer.parseInt(ps.substring(1,2))]+"分";
        }
        return chiesemoney+(loanMoney.toString().contains(".")&&!loanMoney.toString().contains(".00")?"":"整");
    }

    public String loanMoney(){
        if(loanMoney!=null){
            String loanMoneys = loanMoney.toString();
            String ints  = loanMoneys.split("\\.")[0];
            int n = (ints.length()-1)/3;
            String newLoanMoney = "";
            newLoanMoney  =ints.substring(0,ints.length()-n*3)+",";
            while(n>0){
                newLoanMoney += ints.substring(ints.length()-n*3,ints.length()+(1-n)*3)+",";
                n--;
            }
            //newLoanMoney = newLoanMoney.substring(0,newLoanMoney.length()-(ints.length()>3?1:0));
            newLoanMoney = newLoanMoney.substring(0,newLoanMoney.length()-1);
            return newLoanMoney+(loanMoneys.split("\\.").length>1?("."+loanMoneys.split("\\.")[1]):"");
        }else{
            return"";
        }
    }
    public String getY(Date date){
        return date==null?"????":((date.getYear()+1900)+"");
    }
    public String getM(Date date){
        return date==null?"??":getFull(date.getMonth()+1);
    }
    public String getD(Date date){
        return date==null?"??":getFull(date.getDate());
    }

    public String getFull(int n){
        return (n>9?"":"0")+n;
    }

    public static void main(String[] args){
        ClearedProof clearedProof = new ClearedProof();
        clearedProof.setLoanMoney(new BigDecimal(1000000011001.1));
        System.out.println(clearedProof.loanMoney());
        System.out.println(clearedProof.loanMoneyChinese());

    }
}

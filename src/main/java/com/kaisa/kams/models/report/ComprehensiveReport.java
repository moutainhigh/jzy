package com.kaisa.kams.models.report;

import com.kaisa.kams.components.utils.excelUtil.ExcelAssistant;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by zhouchuang on 2017/6/7.
 */

@Data
@NoArgsConstructor
public class ComprehensiveReport  {


    @ExcelAssistant(titleName = "产品类型")
    private String productType="";

    @ExcelAssistant(titleName = "业务提交笔数")
    private Integer businessSubmissionNumber=0;

    @ExcelAssistant(titleName = "审批通过笔数")
    private Integer approvalPassNumber=0;

    @ExcelAssistant(titleName = "审批拒绝笔数")
    private Integer approvalRejectNumber=0;

    @ExcelAssistant(titleName = "放款笔数")
    private Integer loanNumber=0;

    @ExcelAssistant(titleName = "放款金额（元）")
    private BigDecimal loanMoney=new BigDecimal(0);

    @ExcelAssistant(titleName = "应还款笔数")
    private Integer repaymentNumber = 0;

    @ExcelAssistant(titleName = "应还款金额（元）")
    private BigDecimal repaymentMoney=new BigDecimal(0);

    @ExcelAssistant(titleName = "实际结清笔数")
    private Integer actualRepaymentNumber = 0;

    @ExcelAssistant(titleName = "实际结清金额（元）")
    private BigDecimal actualRepaymentMoney=new BigDecimal(0);

   /* @ExcelAssistant(titleName = "逾期笔数")
    private Integer overdueNumber = 0;

    @ExcelAssistant(titleName = "逾期金额（元）")
    private BigDecimal overdueMoney = new BigDecimal(0);

    @ExcelAssistant(titleName = "逾期率",showPercentile = true)
    private BigDecimal overdueRate = new BigDecimal(0);
*/
    @ExcelAssistant(titleName = "平均借款期限（天）")
    private Integer averageLoan =0;

    private String  productTypeId;

    private Integer totalDays;



    public static void main(String[] args){
         /*String json  = "{ " +
                " \"ok\":true ," +
                " \"message\":\"\"," +
                " \"draw\":12," +
                " \"recordsTotal\":27," +
                " \"recordsFiltered\":23," +
                " \"data\":[{";

        for(Field field : ComprehensiveDayReport.class.getDeclaredFields()){
            json += "\""+field.getName()+"\":"+(field.getGenericType()==String.class||field.getGenericType()==Date.class?"\"\"":1)+",";
        }
        json  = json.substring(0,json.length()-1);
        json += "}]}";
        System.out.println(json);*/

       /* String json="{\"start\":1,\"length\":1,\"draw\":1,\"searchKeys\":{$temp}}";
        String temp = "";
        HashMap<String,Object> map  =new HashMap<String,Object>();
        map.put("report_time","asdf");
        Iterator it = map.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,Object> entry = (Map.Entry<String,Object>)it.next();
            temp += "\""+entry.getKey()+"\":"+(entry.getValue() instanceof Integer?1:"\"\"")+",";
        }
        temp = temp.substring(0,temp.length()-1);
        json =  json.replace("$temp" ,temp);
        System.out.println(json);*/

        System.out.println(Math.ceil(125/3.0));
    }
}

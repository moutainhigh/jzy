package com.kaisa.kams.components.utils.excelUtil;

import org.nutz.lang.Files;


import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by wangqx on 2017/6/7.
 */
public class CreateSqlUtils {

    private static final String SPLIT_STR = "/";

    private static final String LOAN_SUBJECT_ID = "876bdbfe-9582-4097-b74b-c2da410afda8";

    private static final String LOAN_SUBJECT_ACCOUNT_ID = "75536b2eabe04e32abbe82e4608287bd";

    private static final String UPDATE_SQL_STR = "update sl_loan set loanSubjectId='" + LOAN_SUBJECT_ID + "',loanSubjectAccountId='" + LOAN_SUBJECT_ACCOUNT_ID + "' where id =(" +
            "select id from (select l.id from sl_loan l " +
            "left join sl_loan_borrower lb on l.id = lb.loanId " +
            "where l.loanTime='%s' and lb.name='%s' and amount='%s')ld);\n";

    public static void main(String args[]) {
        List<String> borrowers = Files.readLines(new File("F:\\资产管理系统日常版本\\20170608\\borrower.txt"));
        List<String> loanTimes = Files.readLines(new File("F:\\资产管理系统日常版本\\20170608\\loanTime.txt"));
        List<String> amounts = Files.readLines(new File("F:\\资产管理系统日常版本\\20170608\\amount.txt"));
        File outFile = new File("F:\\资产管理系统日常版本\\20170608\\更新放款主体.sql");
        int index = 0;
        for (String borrower : borrowers) {
            Files.appendWrite(outFile, String.format(UPDATE_SQL_STR,loanTimes.get(index),borrower,getAmount(amounts.get(index))));
            index++;
        }

        borrowers = Files.readLines(new File("F:\\资产管理系统日常版本\\20170608\\borrower-1.txt"));
        loanTimes = Files.readLines(new File("F:\\资产管理系统日常版本\\20170608\\loanTime-1.txt"));
        amounts = Files.readLines(new File("F:\\资产管理系统日常版本\\20170608\\amount-1.txt"));
        outFile = new File("F:\\资产管理系统日常版本\\20170608\\更新放款主体-1.sql");
        index = 0;
        Set<String> set = new HashSet<>();
        for (String borrower : borrowers) {
            Files.appendWrite(outFile, String.format(UPDATE_SQL_STR,loanTimes.get(index),borrower,getAmountFix(amounts.get(index))));
            index++;
//               set.add(loanTimes.get(index)+borrower+getAmountFix(amounts.get(index)));
        }

//        System.out.print(set.size());
    }

//    private static String formatDate(String date) {
//        String array[] = date.split(SPLIT_STR);
//        StringBuffer formatStr = new StringBuffer(array[0]);
//        int month = Integer.valueOf(array[1]);
//        if (month<10) {
//            array[1] = "0"+array[1];
//        }
//        formatStr.append(array[1]);
//        int day = Integer.valueOf(array[2]);
//        if (month<10) {
//            array[2] = "0"+array[2];
//        }
//        formatStr.append(array[2]);
//        return formatStr.toString();
//    }

    private static String getAmount(String amount) {
        amount = amount.replace(",","");
        amount = amount.replace(" ","");
        return amount+".00";
    }

    private static String getAmountFix(String amount) {
        amount = amount.replace(",","");
        amount = amount.replace(" ","");
        return amount;
    }
}

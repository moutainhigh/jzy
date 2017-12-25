package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 单状态
 * Created by weid on 2016/12/12.
 */
@Getter
@AllArgsConstructor
public enum LoanStatus {

    CHANNELSAVE("4611","渠道编辑中"),
    SAVE("4600","编辑中"),
    CANCEL("4605","已取消"),
    SUBMIT("4601","审批中"),
    APPROVEREJECT("4602","已拒绝"),
    APPROVEEND("4603","等待放款"),
    LOANED("4604","还款中"),
    CLEARED("4608","已还清"),
    OVERDUE("4609","已逾期"),
    LOANCANCEL("4610","取消放款");
    private String code;
    private String description;


    public static boolean isLoaned(String status) {
        if (LoanStatus.LOANED.name().equals(status)) {
            return true;
        }
        if (LoanStatus.OVERDUE.name().equals(status)) {
            return true;
        }
        if (LoanStatus.CLEARED.name().equals(status)) {
            return true;
        }
        return false;
    }

    public static LoanStatus getLoanStatusByName(String name){
        for(LoanStatus loanStatus :LoanStatus.values()){
            if(loanStatus.name().equals(name)){
                return loanStatus;
            }
        }
        return null;
    }
}

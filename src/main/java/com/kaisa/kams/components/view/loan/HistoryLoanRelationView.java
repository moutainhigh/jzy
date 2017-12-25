package com.kaisa.kams.components.view.loan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by luoyj on 2017/09/16.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryLoanRelationView {
    private String id;
    private String code;
    private String name;
    private String repayStatus;
    private String operator;
}

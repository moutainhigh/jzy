package com.kaisa.kams.components.view.loan;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by sunwanchao on 2017/2/27.
 */
@Data
public class AbsRepay {
    private BigDecimal amount;
    private BigDecimal interest;
    private BigDecimal depositRate;//打款利率
    private Date dueDate;
}

package com.kaisa.kams.components.view.loan;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pengyueyang on 2017/3/2.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobileApplyView {
    private String id;

    private String code;

    private String businessName;

    private String borrowerName;

    private BigDecimal amount;

    private String termType;

    private String term;

    private Date submitTime;

    private String productTypeName;

    private String productName;


}

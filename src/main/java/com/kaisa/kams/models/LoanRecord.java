package com.kaisa.kams.models;

import com.kaisa.kams.enums.AmountType;
import com.kaisa.kams.enums.LoanStatus;
import com.kaisa.kams.enums.PublicStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import java.math.BigDecimal;
import java.util.Date;

/**
 *放款记录表
 * Created by lw on 2017/8/29.
 */
@Table("sl_loan_record")
@Data
@NoArgsConstructor
public class LoanRecord extends BaseModel{


    /**
     * loanId
     */
    @Column("loanId")
    private String loanId;

    /**
     * 放款编号
     */
    @Column("loanCode")
    @ColDefine(type=ColType.VARCHAR,width=40)
    private String loanCode;



    /**
     * 金额类型
     */
    @Column("amountType")
    @ColDefine(type=ColType.VARCHAR,width=40)
    private AmountType amountType;

    /**
     * 放款主体
     */
    @Column("loanSubject")
    @ColDefine(type=ColType.VARCHAR,width=50)
    private String loanSubject;

    /**
     * 放款账户
     */
    @Column("payAcount")
    @ColDefine(type=ColType.VARCHAR,width=50)
    private String payAcount;


    /**
     * 放款金额
     */
    @Column("loanAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal loanAmount;

    /**
     * 放款日期
     */
    @Column("loanDate")
    @ColDefine(type = ColType.DATETIME)
    private Date loanDate;

    /**
     * 收款人
     */
    @Column("payee")
    @ColDefine(type=ColType.VARCHAR,width=50)
    private String payee ;

    /**
     * 收款账户
     */
    @Column("payeeAcount")
    @ColDefine(type=ColType.VARCHAR,width=50)
    private String payeeAcount;

    /**
     * 放款状态
     */
    @Column("loanStatus")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private LoanStatus loanStatus;

    /**
     * 顺序位置
     */
    @Column("position")
    @ColDefine(type = ColType.INT,width = 10)
    private int position;

}

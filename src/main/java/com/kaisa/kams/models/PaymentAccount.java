package com.kaisa.kams.models;

import com.kaisa.kams.enums.PublicStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

/**
 * Created by lw on 2017/4/24.
 */
@Table("sl_payment_account")
@Data
@NoArgsConstructor
public class PaymentAccount extends BaseModel{

    @Column("bankName")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String bankName;

    @Column("name")
    @ColDefine(type = ColType.VARCHAR,width = 30)
    private String name;

    @Column("account")
    @ColDefine(type = ColType.VARCHAR,width = 30)
    private String account;

    /**
     * 详见PublicStatus
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private PublicStatus status;

}

package com.kaisa.kams.models.push;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Comment;
import org.nutz.dao.entity.annotation.Default;
import org.nutz.dao.entity.annotation.EL;
import org.nutz.dao.entity.annotation.PK;
import org.nutz.dao.entity.annotation.Prev;
import org.nutz.dao.entity.annotation.Table;
import org.nutz.ioc.loader.annotation.Inject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 票据推单票号关联表
 * Created by pengyueyang on 2017/11/2.
 */
@Table("sl_bill_loan_push")
@Data
@NoArgsConstructor
@PK("id")
public class BillLoanPush implements Serializable {

    public String uuid(){
        return UUID.randomUUID().toString();
    }

    @Comment("主键id")
    @Prev(els=@EL("$me.uuid()"))
    private String id;

    @Comment("业务推送单id")
    @Column("pushId")
    private String pushId;

    @Comment("业务推送单订单id")
    @Column("pushOrderId")
    private String pushOrderId;

    @Comment("票号")
    @Column("billNo")
    @ColDefine(type = ColType.VARCHAR,width = 120)
    private String billNo;

    @Comment("票面金额")
    @Column("billAmount")
    @ColDefine(type = ColType.FLOAT, width = 16)
    private BigDecimal billAmount;

    @Comment("版本")
    @Column("version")
    @ColDefine(type = ColType.INT)
    @Default("0")
    private Integer version;

}

package com.kaisa.kams.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import java.math.BigDecimal;

/**
 * Created by zhouchuang on 2017/5/23.
 */
@Table("sl_product_rate")
@Data
@NoArgsConstructor
public class ProductRate extends BaseModel{
    /**
     *期限
     */
    @Column("term")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String term;

    /**
     *标准利率
     */
    @Column("standardRate")
    @ColDefine(type = ColType.FLOAT, width=16,precision=4)
    private BigDecimal standardRate;

    /**
     *补贴利息
     */
    @Column("subsidyRate")
    @ColDefine(type = ColType.FLOAT, width=16,precision=4)
    private BigDecimal subsidyRate;


    /**
     *资金利率
     */
    @Column("capitalRate")
    @ColDefine(type = ColType.FLOAT, width=16,precision=4)
    private BigDecimal capitalRate;
}

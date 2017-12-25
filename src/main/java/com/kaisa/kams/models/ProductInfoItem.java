package com.kaisa.kams.models;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pengyueyang on 2016/11/24.
 * 产品信息条目值
 */
@Table("sl_product_info_item")
@Data
@NoArgsConstructor
public class ProductInfoItem extends BaseModel{
    /**
     * 模板Id
     */
    @Column("tmplId")
    private String tmplId;

    /**
     * 实际值
     */
    @Column("dataValue")
    @ColDefine(type = ColType.TEXT)
    private String dataValue;

    /**
     * loanId 贷款id
     */
    @Column("loanId")
    private String loanId;

    /**
     * key 属性名
     */
    @Column("keyName")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String keyName;

    /**
     * type 数据类型
     */
    @Column("type")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String type;


}

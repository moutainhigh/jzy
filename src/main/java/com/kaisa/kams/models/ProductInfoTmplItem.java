package com.kaisa.kams.models;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.One;
import org.nutz.dao.entity.annotation.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pengyueyang on 2016/11/24.
 * 产品信息配置条目表
 */
@Table("sl_product_info_tmpl_item")
@Data
@NoArgsConstructor
public class ProductInfoTmplItem extends BaseModel{
    /**
     * 模板id
     */
    @Column("tmplId")
    private String tmplId;

    /**
     * 产品模板
     */
    @One(target = ProductInfoTmpl.class, field ="tmplId" )
    private ProductInfoTmpl productInfoTmpl;

    /**
     * label
     */
    @Column("label")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String label;

    /**
     * 类型[枚举需要定义]
     */
    @Column("type")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String type;

    /**
     * 数据值
     */
    @Column("dataValue")
    @ColDefine(type = ColType.TEXT)
    private String dataValue;

    /**
     * 排序
     */
    @Column("rank")
    private int rank;

    /**
     * 属性名称 在同一个模板中唯一校验
     * @return
     */
    @Column("keyName")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String keyName;


    /**
     * 表单验证正则表达式
     */
    @Column("regex")
    @ColDefine(type = ColType.VARCHAR, width=254)
    private String regex;

}

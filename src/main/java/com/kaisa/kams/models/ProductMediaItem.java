package com.kaisa.kams.models;

import com.kaisa.kams.enums.ProductMediaItemType;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pengyueyang on 2016/11/24.
 * 产品影像配置表
 */
@Table("sl_product_media_item")
@Data
@NoArgsConstructor
public class ProductMediaItem extends BaseModel{
    /**
     * 产品影像配置模板Id
     */
    @Column("tmplId")
    private String tmplId;

    /**
     * 影像名称
     */
    @Column("name")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String name;

    /**
     * 是否必填 false非必填 true必填
     */
    @Column("required")
    private boolean required;


    /**
     * 产品影像资料类型
     */
    @Column("mediaItemType")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private ProductMediaItemType mediaItemType;

    /**
     * 产品影像资料类型Code
     */
    @Column("mediaItemTypeCode")
    @ColDefine(type = ColType.VARCHAR, width=10)
    private String code;

}

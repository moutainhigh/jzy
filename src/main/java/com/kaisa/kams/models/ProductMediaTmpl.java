package com.kaisa.kams.models;

import com.kaisa.kams.enums.PublicStatus;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;


import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pengyueyang on 2016/11/24.
 * 产品影像配置模板表
 */
@Table("sl_product_media_tmpl")
@Data
@NoArgsConstructor
public class ProductMediaTmpl extends BaseModel{

    /**
     * 影像资料名称
     */
    @Column("name")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String name;

    /**
     * 影像编码
     */
    @Column("code")
    @ColDefine(type = ColType.CHAR,width = 8)
    private String code;

    /**
     * 影像资料说明
     */
    @Column("description")
    @ColDefine(type = ColType.VARCHAR,width = 300)
    private String description;

    /**
     * 是否生效
     *
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private PublicStatus status;

    /**
     * 状态名称
     */
    private String statusName;

}

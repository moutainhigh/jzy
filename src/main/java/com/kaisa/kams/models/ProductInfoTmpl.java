package com.kaisa.kams.models;

import com.kaisa.kams.enums.ProductTempType;
import com.kaisa.kams.enums.PublicStatus;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * Created by pengyueyang on 2016/11/24.
 * 产品信息模板表
 */
@Table("sl_product_info_tmpl")
@Data
@NoArgsConstructor
public class ProductInfoTmpl extends BaseModel{
    /**
     * 模板名称 唯一
     */
    @Column("name")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private String name;

    /**
     * 是否启用
     * 详见PublicStatus
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private PublicStatus status;

    /**
     * 模板编辑路径
     */
    @Column("addUrl")
    @ColDefine(type = ColType.VARCHAR,width = 256)
    private String addUrl;

    /**
     * 模板查看路径
     */
    @Column("viewUrl")
    @ColDefine(type = ColType.VARCHAR,width = 256)
    private String viewUrl;

    /**
     * 录单add入口地址
     */
    @Column("addEntryUrl")
    @ColDefine(type = ColType.VARCHAR,width = 256)
    private String addEntryUrl;

    /**
     * 录单update入口地址
     */
    @Column("updateEntryUrl")
    @ColDefine(type = ColType.VARCHAR,width = 256)
    private String updateEntryUrl;

    /**
     * excel模板下载地址
     */
    @Column("excelUrl")
    @ColDefine(type = ColType.VARCHAR,width = 256)
    private String excelUrl;


    @Column("productTempType")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private ProductTempType productTempType;
}

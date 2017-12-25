package com.kaisa.kams.models;

import com.kaisa.kams.enums.BusinessType;
import com.kaisa.kams.enums.GuarantyType;
import com.kaisa.kams.enums.PublicStatus;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pengyueyang on 2016/11/24.
 * 产品类型实体
 */
@Table("sl_product_type")
@Data
@NoArgsConstructor
public class ProductType extends BaseModel{
    /**
     * 产品类型名称
     */
    @Column("name")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String name;

    /**
     * 产品担保类型
     */
    @Column("guarantyType")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private GuarantyType guarantyType;

    /**
     * 业务类型
     */
    @Column("businessType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private BusinessType businessType;

    /**
     * 产品类型编码
     */
    @Column("code")
    @ColDefine(type = ColType.VARCHAR,width = 4)
    private String code;

    /**
     * 产品大类说明
     */
    @Column("description")
    @ColDefine(type = ColType.VARCHAR,width = 300)
    private String description;

    /**
     * 详见PublicStatus
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private PublicStatus status;

    /**
     * 产品类型  PIAOJU/HONGBEN/SHULOU/GERENDAI/CHEDAI/BAOLI/PINGTAI
     */
    @Column("productType")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private String productType;


}

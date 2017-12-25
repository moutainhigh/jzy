package com.kaisa.kams.models.business;



import org.nutz.dao.entity.annotation.*;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 组织产品关联表
 * Created by pengyueyang on 2016/12/05.
 */
@Data
@NoArgsConstructor
@Table("sl_business_products_organize")
@PK("id")
public class ProductsToOrganize{

    public String uuid(){
        return UUID.randomUUID().toString();
    }


    @Prev(els=@EL("uuid()"))
    private String id;
    //业务组织id
    @Column("organizeId")
    private String organizeId;
    //产品Id
    @Column("productId")
    private String productId;
    //编码
    @Column("code")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private String code;
    //业务组织名称
    @Column("organizeName")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String organizeName;

}

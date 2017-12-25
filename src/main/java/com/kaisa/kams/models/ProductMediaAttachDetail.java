package com.kaisa.kams.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

/**
 * Created by lw on 2017/02/22.
 * 产品影像附件表详情（上传多个附件）
 */
@Table("sl_product_media_attach_detail")
@Data
@NoArgsConstructor
public class ProductMediaAttachDetail extends BaseModel{
    /**
     * 关联产品影像附件表Id
     */
    @Column("productMediaAttachId")
    private String productMediaAttachId;

    /**
     * 附件名称[生成的名称]
     */
    @Column("attachName")
    @ColDefine(type = ColType.VARCHAR,width = 128)
    private String attachName;

    /**
     * 附件地址
     */
    @Column("url")
    @ColDefine(type = ColType.VARCHAR,width = 312)
    private String url;

}

package com.kaisa.kams.models;

import com.kaisa.kams.enums.ProductMediaItemType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

import java.util.List;

/**
 * Created by pengyueyang on 2016/11/25.
 * 产品影像附件表
 */
@Table("sl_product_media_attach")
@Data
@NoArgsConstructor
public class ProductMediaAttach extends BaseModel{
    /**
     * 贷款申请Id
     */
    @Column("loanId")
    private String loanId;

    /**
     * 模板id
     */
    @Column("tmplId")
    private String tmplId;

    /**
     * 影像名称（对应影像配置表中的影像名称name字段）
     */
    @Column("itemName")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String itemName;

    /**
     * 附件名称[生成的名称]
     */
    @Column("attachName")
    @ColDefine(type = ColType.VARCHAR,width = 64)
    private String attachName;

    /**
     * 附件地址
     */
    @Column("url")
    @ColDefine(type = ColType.VARCHAR,width = 256)
    private String url;

    /**
     * 是否必填 false非必填 true必填
     */
    @Column("required")
    private boolean required;

    /**
     * 借款人
     */
    @Column("masterBorrowerId")
    private String masterBorrowerId;
    /**
     * 产品影像资料类型
     */
    @Column("mediaItemType")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private ProductMediaItemType mediaItemType;

    /**
     * 关联产品影像附件详情表
     */
    @Many(target = ProductMediaAttachDetail.class, field ="productMediaAttachId" )
    private List<ProductMediaAttachDetail> productMediaAttachDetails;


    public ProductMediaAttach(ProductMediaItem productMediaItem) {
        this.tmplId = productMediaItem.getTmplId();
        this.itemName = productMediaItem.getName();
        this.required = productMediaItem.isRequired();
        this.mediaItemType = productMediaItem.getMediaItemType();
    }
}

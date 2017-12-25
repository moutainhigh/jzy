package com.kaisa.kams.models;

import com.kaisa.kams.enums.ProductMediaItemType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

import java.util.List;

/**
 * Created by lw on 2016/2/28.
 * 票据影像附件表
 */
@Table("sl_bill_media_attach")
@Data
@NoArgsConstructor
public class BillMediaAttach extends BaseModel{

    /**
     * 影像名称（对应影像配置表中的影像名称name字段）
     */
    @Column("itemName")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String itemName;

    /**
     * 模板id
     */
    @Column("tmplId")
    private String tmplId;

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
     * 产品影像资料类型Code
     */
    @Column("mediaItemTypeCode")
    @ColDefine(type = ColType.VARCHAR, width=10)
    private String code;

    /**
     * 关联产品影像附件详情表
     */
    @Many(target = ProductMediaAttachDetail.class, field ="productMediaAttachId" )
    private List<ProductMediaAttachDetail> productMediaAttachDetails;

}

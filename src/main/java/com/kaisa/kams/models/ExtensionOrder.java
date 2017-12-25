package com.kaisa.kams.models;

import com.kaisa.kams.enums.FlowConfigureType;
import com.kaisa.kams.enums.FlowControlType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

/**
 * 流程实例订单表
 * Created by weid on 2016/12/19.
 */
@Table("sl_extension_order")
@Data
@NoArgsConstructor
public class ExtensionOrder extends BaseOrder  {
    @Column("extensionId")
    private String extensionId;
}

package com.kaisa.kams.components.view.flow;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.Column;

/**
 * Created by zhouchuang on 2017/8/29.
 */
@Data
@NoArgsConstructor
public class FlowConfigureTypeVO {
    /**
     * 流程描述
     */
    private String desc;
    /**
     * 流程名称
     */
    private String name;

    /**
     * 流程Code
     */
    private String code;

}

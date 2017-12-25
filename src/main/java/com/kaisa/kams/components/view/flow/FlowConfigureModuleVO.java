package com.kaisa.kams.components.view.flow;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.Column;

/**
 * Created by zhouchuang on 2017/8/29.
 */
@Data
@NoArgsConstructor
public class FlowConfigureModuleVO {

    /**
     * 模块描述
     */
    private String desc;
    /**
     * 模块名称
     */
    private String name;

    /**
     * 模块Code
     */
    private String code;
}

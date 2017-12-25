package com.kaisa.kams.components.view.push;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资料文件
 * @author pengyueyang created on 2017/11/13.
 */
@Data
@NoArgsConstructor
public class AttachView {
    /** 资料项名称 */
    private String itemName;

    /** 资料类型 */
    private String mediaItemType;

    /** 资料名称 */
    private String attachNames;

    /** 资料下载url */
    private String urls;

}

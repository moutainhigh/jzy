package com.kaisa.kams.components.view.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pengyueyang on 2016/12/20.
 * 组织机构节点
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizeNode {
    //节点Id
    private String id;
    //父Id
    private String pId;
    //显示名字
    private String name;
    //是否被选中
    private boolean checked;
    //是否展开
    private boolean open;
}

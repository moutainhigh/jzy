package com.kaisa.kams.components.view.bugInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by lw on 2017/1/16.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
/*
附件
 */
public class Attachments {
    //标题
    private String title;
    //描述
    private String description;
    //链接
    private String url;
    //级别（warning|info|primary|error|muted|success）
    private String color;
}

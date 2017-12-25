package com.kaisa.kams.components.params.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by zhouchuang on 2017/8/14.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OaParam extends DataTableParam{
    private String oaUserAccount  = "";
}

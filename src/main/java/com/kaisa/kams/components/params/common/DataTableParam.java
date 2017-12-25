package com.kaisa.kams.components.params.common;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pengyueyang on 2017/3/20.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataTableParam {

    private int start;

    private int length;

    private int draw;

    private Map<String,String> searchKeys;
}

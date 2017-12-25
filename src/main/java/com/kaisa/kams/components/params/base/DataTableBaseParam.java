package com.kaisa.kams.components.params.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by wangqx on 2017/10/18.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataTableBaseParam {

    private int start;

    private int length;

    private int draw;

}

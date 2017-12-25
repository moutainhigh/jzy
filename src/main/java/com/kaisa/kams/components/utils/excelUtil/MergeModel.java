package com.kaisa.kams.components.utils.excelUtil;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;

/**
 * Created by zhouchuang on 2017/8/11.
 */
@Data
@NoArgsConstructor
public class MergeModel {
    private Integer cross=0;
    private String refValue;
    private String value;
    private Field refField;
    public void add(){
        cross  ++;
    }
}

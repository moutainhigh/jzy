package com.kaisa.kams.components.utils;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.Cnd;

/**
 * Created by zhouchuang on 2017/4/28.
 */

@Data
@NoArgsConstructor
public class ParamData {
    private int draw;
    private int start;
    private int length;
    private Cnd cnd;
}

package com.kaisa.kams.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.lang.util.NutMap;

/**
 * Created by pengyueyang on 2016/11/29.
 * datatable数据结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataTables {
    /**
     * 页数
     */
    private int draw;
    /**
     * 总记录数
     */
    private int recordsTotal;

    /**
     * 过滤后的记录数
     */
    private int recordsFiltered;

    /**
     * 数据列表
     */
    private List data;

    /**
     * 异常信息
     */
    private String error;

    /**
     * 成功或者失败
     */
    private boolean ok;

    /**
     * 成功失败msg
     */
    private String msg;

    public DataTables(int draw, int recordsTotal, int recordsFiltered, List data) {
        this(draw,recordsTotal,recordsFiltered);
        this.data = data;
    }


    public DataTables(int draw, int recordsTotal, int recordsFiltered) {
        this.draw = draw;
        this.recordsTotal = recordsTotal;
        this.recordsFiltered = recordsFiltered;
    }

}

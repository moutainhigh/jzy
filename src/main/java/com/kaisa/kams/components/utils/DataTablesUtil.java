package com.kaisa.kams.components.utils;

import org.nutz.dao.pager.Pager;

/**
 * 前端dataTable框架分页转换兼容后端Pager分页
 * Created by luoyj on 2016/11/30
 */
public class DataTablesUtil {

    /**
     * 转换成Pager分页
     *
     * @param start  第几条数据
     * @param length 一页多少条数数据
     */
    public static Pager getDataTableToPager(int start, int length) {
        // 通过起始数据和条数计算是第几页.
        int pageNumber = getPageNo(start, length);
        Pager p = new Pager();
        p.setPageSize(length);
        p.setPageNumber(pageNumber);
        return p;
    }

    private static int getPageNo(int start, int length) {
        // 通过起始数据和条数计算是第几页.
        if (length == 0) {
            return 1;
        }
        return start / length + 1;
    }

}

package com.kaisa.kams.components.utils.excelUtil;

import lombok.Data;

/**
 * Created by luoyj on 2017/02/28.
 */
@Data
/**
 *  excel 常规和非常规表格循环
 */
public class ExcelTableView {

    private String item1;
    private String item2;
    private String item3;
    private String item4;

    public ExcelTableView() {
    }

    public ExcelTableView(String item1, String item2, String item3, String item4) {
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
        this.item4 = item4;
    }

    @ExcelResources(title = "列一", order = 0)
    public String getItem1() {
        return item1;
    }

    public void setItem1(String item1) {
        this.item1 = item1;
    }
    @ExcelResources(title = "列二", order = 1)
    public String getItem2() {
        return item2;
    }

    public void setItem2(String item2) {
        this.item2 = item2;
    }
    @ExcelResources(title = "列三", order = 2)
    public String getItem3() {
        return item3;
    }

    public void setItem3(String item3) {
        this.item3 = item3;
    }
    @ExcelResources(title = "列四", order = 3)
    public String getItem4() {
        return item4;
    }

    public void setItem4(String item4) {
        this.item4 = item4;
    }

}

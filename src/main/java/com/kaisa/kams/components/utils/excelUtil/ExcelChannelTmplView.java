package com.kaisa.kams.components.utils.excelUtil;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luoyj on 2017/02/28.
 */
@Data
public class ExcelChannelTmplView {

    private String Id;

    private String name;

    private  String code;

    private String manager;



    public ExcelChannelTmplView() {
    }

    public ExcelChannelTmplView(String Id, String name, String code, String manager) {
        this.Id=Id;
        this.name=name;
        this.code=code;
        this.manager=manager;
    }
}

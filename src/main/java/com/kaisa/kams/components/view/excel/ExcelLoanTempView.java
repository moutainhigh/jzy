package com.kaisa.kams.components.view.excel;

import lombok.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Created by luoyj on 2017/02/28.
 */
@Data
public class ExcelLoanTempView {
    private Map<String, String> map;
    private Map<String,List<?>> listMap;

    public ExcelLoanTempView(){}

    public Map<String, String> getMap() {
        if(this.map==null){
             this.map= new HashMap<>();
        }
        return map;
    }

    public Map<String,List<?>> getListMap() {
        if (this.listMap==null){
            this.listMap=new HashMap<>();
        }
        return listMap;
    }
}

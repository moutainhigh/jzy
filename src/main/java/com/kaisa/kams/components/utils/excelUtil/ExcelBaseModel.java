package com.kaisa.kams.components.utils.excelUtil;

import com.kaisa.kams.models.BaseModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;

import java.lang.reflect.Field;
import java.math.BigDecimal;

/**
 * Created by zhouchuang on 2017/5/2.
 */
@Data
@NoArgsConstructor
public class ExcelBaseModel extends BaseModel {
    /**
     * 处理状态  00未处理 01处理成功 02 处理失败
     * */
    @ExcelAssistant(titleName="处理状态")
    @Column("excludeStatus")
    @ColDefine(type = ColType.VARCHAR, width=2)
    protected String excludeStatus = "00";

    /**
     * 处理失败原因
     * */
    @ExcelAssistant(titleName="处理失败原因")
    @Column("excludeMsg")
    @ColDefine(type = ColType.VARCHAR, width=1024)
    protected String excludeMsg="";


    /**
     * 导入信息  00成功  01失败
     * */
    @ExcelAssistant(titleName="导入状态")
    @Column("importStatus")
    @ColDefine(type = ColType.VARCHAR, width=2)
    protected String importStatus="00";
    /**
     * 导入信息
     * */
    @ExcelAssistant(titleName="导入信息")
    @Column("importMsg")
    @ColDefine(type = ColType.VARCHAR, width=1024)
    protected String importMsg="";

    /**
     * MD5
     * */
    @ExcelAssistant(titleName="MD5")
    @Column("md5")
    @ColDefine(type = ColType.VARCHAR, width=32)
    protected String md5="";

    /**
     * loanId
     * */
    @ExcelAssistant(titleName="loanId")
    @Column("loanId")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String loanId;

    /**
     * version
     * */
    @ExcelAssistant(titleName="导入版本号")
    @Column("version")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String version;

    /**
     * lineNum
     * */
    @ExcelAssistant(titleName="在excel里面的行号")
    @Column("lineNum")
    @ColDefine(type = ColType.INT)
    private int lineNum;

    public void addExcludeMsg(String msg){
        this.excludeMsg += msg+",";
    }

    public boolean validata(){
        Field[] fields = this.getClass().getDeclaredFields();
        for(Field field : fields){
            ExcelAssistant excelAssistant = field.getAnnotation(ExcelAssistant.class);
            if(excelAssistant==null)continue;
            if(excelAssistant.NonNull()){
                field.setAccessible(true);
                try {
                    Object obj = field.get(this);
                    if(obj==null){
                        this.importMsg += excelAssistant.titleName()+"不能为空，";
                    }else{
                        if(obj instanceof String&&obj.toString().equals("")){
                            this.importMsg += excelAssistant.titleName()+"不能为空，";
                        }else if(obj instanceof Integer&&(Integer)obj==0){
                            this.importMsg += excelAssistant.titleName()+"不能为空，";
                        }else if(obj instanceof BigDecimal && ((BigDecimal) obj) .doubleValue()==0.0){
                            this.importMsg += excelAssistant.titleName()+"不能为空，";
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if(!StringUtils.isEmpty(this.importMsg))this.importStatus="01";
        return StringUtils.isEmpty(this.importMsg);
    }

    public void dataConversion(){
    }
}

package com.kaisa.kams.components.utils.pdfUtil;

import lombok.Data;

/**
 * Created by zhouchuang on 2017/10/12.
 */
@Data
public class PdfTableCell {
    private String text;
    private int mergeCol = 1;
    private FontType fontType = FontType.NORMAL; //normal,title,mark  normal为普通的黑体 title为标题的样式 mark 为重要
    private boolean needBg = false;
    public PdfTableCell(String text,int mergeCol,FontType fontType,boolean needBg){
        this.text=text;
        this.mergeCol=mergeCol;
        this.fontType=fontType;
        this.needBg=needBg;
        setType();
    }

    private void setType(){
        if(this.text.startsWith("<BG>")){
            this.fontType = FontType.NORMAL;
            this.setNeedBg(true);
            this.text = this.text.substring(4);
        }else if(this.text.startsWith("<NORMAL>")){
            this.text = this.text.substring(8);
            this.fontType = FontType.NORMAL;
            this.setNeedBg(false);
        }
    }
}

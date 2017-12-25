package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouchuang on 2017/9/20.
 */
@Getter
@AllArgsConstructor
public enum MortgageDocumentType {
    ZXGZ_TXZM("001","涂销证明（注销广州）"),
    ZXGZ_BDCSQDJB("002","不动产申请登记表（注销广州）"),
    ZXGZ_JQZM("003","结清证明（注销广州）"),
    ZXGZ_BBWTS("004","委托书（注销广州）"),
    ZXGZ_FRSQWTS("005","授权委托证明书（注销广州）"),
    ZXGZ_NORMAL_FRSFZ("006","法人身份证.jpg"),
    ZXGZ_NORMAL_FRZMS("007","法人证明书.docx"),
    ZXGZ_NORMAL_FRYYZZ("008","营业执照.jpg"),

    HBZXSZ_ZXSQB("009","注销申请表（红本注销深圳）"),
    HBZXSZ_SQZXFCDYDJ("010","申请注销房产抵押登记（红本注销深圳）"),
    HBZXSZ_NORMAL_4HW1("011","4号文-1.jpg"),
    HBZXSZ_NORMAL_4HW2("012","4号文-2.jpg"),
    HBZXSZ_NORMAL_4HW3("013","4号文-3.jpg"),
    HBZXSZ_NORMAL_4HWBGTZS("014","4号文-变更通知书.jpg"),
    HBZXSZ_NORMAL_4HWJYGG("015","4号文-经营公告.jpg"),
    HBZXSZ_NORMAL_9HW1("016","9号文-1.jpg"),
    HBZXSZ_NORMAL_9HW2("017","9号文-2.jpg"),
    HBZXSZ_NORMAL_FDDBRZMS("018","法定代表人证明书.doc"),
    HBZXSZ_NORMAL_FRSFZ("019","法人身份证.jpg"),
    HBZXSZ_NORMAL_FRSQWTZMS("020","法人授权委托证明书.doc"),
    HBZXSZ_NORMAL_YYZZ("021","营业执照.jpg"),


    HBDYSZ_ZGEDYJKHT("008","最高额抵押借款合同（红本抵押深圳）"),
    HBDYSZ_WTS("009","委托书（红本抵押深圳）"),
    HBDYSZ_DYQSCDJ("010","抵押权首次登记（红本抵押深圳）"),

    DYGZ_ZGEDYJKHT("011","最高额抵押借款合同（抵押广州）"),
    DYGZ_SQWTZMS("012","授权委托证明书（抵押广州）"),
    DYGZ_BDCSQDJB("013","不动产申请登记表（抵押广州）"),

    NORMAL_DY_YYZZ("022","营业执照.jpg"),
    NORMAL_DY_FRSFZ("023","法人身份证.jpg"),
    NORMAL_DYSZ_9HW2("023","9号文-2.jpg"),
    NORMAL_DYSZ_9HW1("024","9号文-1.jpg"),
    NORMAL_DYSZ_4HWJYGG("025","4号文-经营公告.jpg"),
    NORMAL_DYSZ_4HWBGTZS("026","4号文-变更通知书.jpg"),
    NORMAL_DYSZ_4HW1("028","4号文-1.jpg"),
    NORMAL_DYSZ_4HW2("029","4号文-2.jpg"),
    NORMAL_DYSZ_4HW3("030","4号文-3.jpg"),

    NORMAL_DYGZ_FRZMS("031","法人证明书.docx"),
    NORMAL_DYSZ_FRSQWTZMS("032","法人授权委托证明书.doc"),
    NORMAL_DYSZ_FDDBRZMS("033","法定代表人证明书.doc");


    private String code;
    private String description;

    public static List<Map> mortgageDocumentlist(HouseMortgageType houseMortgageType){
        List list = new ArrayList();
        for (MortgageDocumentType mortgageDocumentType : MortgageDocumentType.values()){
            if(houseMortgageType.equals(HouseMortgageType.GUANGZHOU)&&(mortgageDocumentType.name().contains("DYGZ_")||mortgageDocumentType.name().contains("_DY_"))||houseMortgageType.equals(HouseMortgageType.SHENZHEN)&&(mortgageDocumentType.name().contains("DYSZ")||mortgageDocumentType.name().contains("_DY_"))){
                Map map  = new HashMap();
                map.put("name",mortgageDocumentType.description);
                map.put("type",mortgageDocumentType.name());
                list.add(map);
            }
        }
        return list;
    }

    public static List<Map> noMortgageDocumentlist(AddressType addressType){
        List list = new ArrayList();
        for (MortgageDocumentType mortgageDocumentType : MortgageDocumentType.values()){
            if(addressType.equals(AddressType.GZMORTGAGE)&&mortgageDocumentType.name().startsWith("ZXGZ")||addressType.equals(AddressType.SZMORTGAGE)&&mortgageDocumentType.name().startsWith("HBZXSZ")){
                Map map  = new HashMap();
                map.put("name",mortgageDocumentType.description);
                map.put("type",mortgageDocumentType.name());
                list.add(map);
            }
        }
        return list;
    }

}

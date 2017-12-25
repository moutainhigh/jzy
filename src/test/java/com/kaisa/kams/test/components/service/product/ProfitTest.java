package com.kaisa.kams.test.components.service.product;

import com.alibaba.druid.util.HttpClientUtils;
import com.google.gson.Gson;
import com.kaisa.kams.components.params.common.OaMap;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.DecimalFormatUtils;
import com.kaisa.kams.components.utils.flow.ApproveWarnMessageUtils;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.history.ProductImportBaoli;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by zhouchuang on 2017/8/7.
 */
public class ProfitTest {
    @Test
    public void testProfit() {
        Loan loan  = new Loan();
        loan.setAmount(new BigDecimal(66666.66));
        loan.setCalculateMethodAboutDay(CalculateMethodAboutDay.CALCULATE_HEAD_AND_TAIL);
        loan.setLoanTime(new Date(117,7,3));
        loan.setTerm("2017-8-10");
        BigDecimal one = loan.getCalculateMethodAboutDay().equals(CalculateMethodAboutDay.CALCULATE_HEAD_AND_TAIL)?BigDecimal.ONE:BigDecimal.ZERO;
        BigDecimal term =  new BigDecimal(DateUtil.daysBetweenTowDate(loan.getLoanTime()!=null?loan.getLoanTime():new Date(),DateUtil.getStringToDate(loan.getTerm())));
        BigDecimal common = loan.getAmount().multiply(one.add(term)).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP);
        BigDecimal capitalCost = common.multiply(new BigDecimal(2.61));
        System.out.println(capitalCost);


        BigDecimal capitalCost1 = loan.getAmount().multiply(new BigDecimal(loan.getCalculateMethodAboutDay().equals(CalculateMethodAboutDay.CALCULATE_HEAD_AND_TAIL)?1:0+DateUtil.daysBetweenTowDate( loan.getLoanTime()!=null?loan.getLoanTime():new Date(),DateUtil.getStringToDate(loan.getTerm()))))
                    .divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(2.61));

        BigDecimal operatingCost = common.multiply(new BigDecimal(2.51));

        System.out.println(capitalCost1);
        System.out.println(operatingCost);

       // BigDecimal valueAddedTax = new BigDecimal().divide(productProfit.getTotalTax(),10,BigDecimal.ROUND_HALF_UP).multiply(productProfit.getValueAddedTax()).divide(new BigDecimal(100),10,BigDecimal.ROUND_HALF_UP);



        BigDecimal total1= new BigDecimal(6000.00);
        BigDecimal total2 = new BigDecimal(7000.00);
        BigDecimal cost1 = new BigDecimal(2.4);
        BigDecimal op1 = new BigDecimal(2.0);
        BigDecimal dis1 = new BigDecimal(9);
        BigDecimal dis2 = new BigDecimal(10);

        BigDecimal common1 = total1.divide
                (
                        (cost1.multiply(dis1)).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP).add(new BigDecimal(1)),10,BigDecimal.ROUND_HALF_UP
                ).multiply(dis1).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP);
//        System.out.println(common1.multiply(new BigDecimal(2.63)));
//        System.out.println(common1.multiply(new BigDecimal(2.03)));

        BigDecimal common2 = total1.divide
                (
                        (cost1.multiply(dis2)).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP).add(new BigDecimal(1)),10,BigDecimal.ROUND_HALF_UP
                ).multiply(dis2).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP);
//        System.out.println(common2.multiply(new BigDecimal(2.63)));
//        System.out.println(common2.multiply(new BigDecimal(2.03)));

        System.out.println(common1.multiply(cost1));
        System.out.println(common2.multiply(cost1));
        System.out.println(common1.multiply(op1));
        System.out.println(common2.multiply(op1));
        System.out.println(common1.multiply(cost1).add(common2.multiply(cost1)));
        System.out.println(common1.multiply(op1).add(common2.multiply(op1)));


    }

    @Test
    public void testProfitCostOpr() {
        BigDecimal total1= new BigDecimal(6000.00);
        BigDecimal total2 = new BigDecimal(7000.00);
        int day1 = 9;
        int day2 = 10;
        BigDecimal cost = new BigDecimal(2.4);
        BigDecimal opr = new BigDecimal(2.0);

        BigDecimal common1 = total1.divide
                (
                        (cost.multiply(new BigDecimal(day1))).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP).add(new BigDecimal(1)),10,BigDecimal.ROUND_HALF_UP
                ).multiply(new BigDecimal(day1)).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP);

        BigDecimal common2 = total2.divide
                (
                        (cost.multiply(new BigDecimal(day2))).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP).add(new BigDecimal(1)),10,BigDecimal.ROUND_HALF_UP
                ).multiply(new BigDecimal(day2)).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP);


        System.out.println(common1.multiply(cost));
        System.out.println(common2.multiply(cost));
        System.out.println(common1.multiply(cost).add(common2.multiply(cost)));
        System.out.println(common1.multiply(opr).add(common2.multiply(opr)));

    }

//    @Test
//    public void testImplements() throws Exception{
//        BusinessReportCommonData businessReportCommonData = new BusinessReportCommonData();
//        businessReportCommonData.create();
//        for(Field field : businessReportCommonData.getClass().getDeclaredFields()){
//            field.setAccessible(true);
//            System.out.println(IBaseReport.class.isAssignableFrom(field.getType()));
//        }
//    }


    @Test
    public void testOaMap(){
        OaMap oaMap = new OaMap();
        oaMap.put("ok",false);
        oaMap.put("msg","hello");
        oaMap.setv("data.user.username","张三");
        System.out.println(oaMap);
    }
    @Test
    public void testMenu(){
        ApprovalType.getApprovalTypeByName("C");
    }

    @Test
    public void testStaticClass(){
        ApproveWarnMessageUtils.removeTask("12341241");
    }

    @Test
    public void testGetStringToDate(){
        System.out.println(DateUtil.getStringToTime("2017-05-24 17:04:36"));
        System.out.println(DateUtil.minutesBetweenTowDate(DateUtil.getStringToTime("2017-05-24 17:04:36"),DateUtil.getStringToTime("2017-05-24 17:04:37")));
    }
    @Test
    public void testCode(){
        String n  =(9+1)+"";
        System.out.println("LC0000000".substring(0,10-n.length())+n);
    }

    @Test
    public void testCode1(){
        String code = "LC00000009";
        String   n =  code.replaceAll("LC[0]+","");
        n =""+ (Integer.parseInt(n)+1);
        String newCode = "LC0000000".substring(0,10-n.length())+n;
        System.out.println(newCode);
    }
    @Test
    public void getBaoliMD5() {
        Field[] fields = ProductImportBaoli.class.getDeclaredFields();
        for (Field field : fields) {
            System.out.print("this." + field.getName() + "+");
        }
    }
    @Test
    public void testTime(){
        String dates = "2016/1/27";
        String year = dates.split("/")[0];
        String month = dates.split("/")[1];
        String date = dates.split("/")[2];
        Date loanDate =  new Date(Integer.parseInt(year)-1900,Integer.parseInt(month)-1,Integer.parseInt(date));
        System.out.println(loanDate);
    }

    @Test
    public void getJsonArr(){
        String   str  = "[[{\"keyName\":\"address\",\"dataValue\":\"广州\"},{\"keyName\":\"house_code\",\"dataValue\":\"粤房地权证穗字第0421719号\"}]]";
        str = str.substring(1,str.length()-1);
        JSONArray jsonArray = JSONArray.fromObject(str);//把String转换为json
        List list = JSONArray.toList(jsonArray,HashMap.class);//这里的t是Class<T>
        System.out.println(list);
    }

    @Test
    public void getJsonOfMortgage(){
        Mortgage mortgage = new Mortgage();
        mortgage.setApprovalStatusType(ApprovalStatusType.IN_EDIT);
        mortgage.setMortgageCode("DY00001");
        mortgage.setBusinessId("12312");
        mortgage.setBusinessName("jack");
        mortgage.setId("aaaaa");
        mortgage.setHouseMortgageType(HouseMortgageType.GUANGZHOU);
        mortgage.setLoanSubjectId("123");
        MortgageHouse mortgageHouse  = new MortgageHouse();
        mortgageHouse.setMortgageId("aaaaa");
        mortgageHouse.setHouseId("123");
        mortgageHouse.setId("ddddd");
        mortgageHouse.setEndBorrowingTime(new Date());
        mortgageHouse.setStartBorrowingTime(new Date());
        mortgageHouse.setMaximumLoanAmount(BigDecimal.ZERO);
        mortgageHouse.setLoanInterestRate(BigDecimal.ZERO);
        House house = new House();
        house.setArea("100");
        house.setId("bbbbb");
        house.setAddress("罗湖区");
        house.setHousePropertyNumber("LCasd124-2234123");
        EquityHolder equityHolder = new EquityHolder();
        equityHolder.setHouseId("bbbbb");
        equityHolder.setId("ccccc");
        equityHolder.setCertificateNo("4301230102541");
        //equityHolder.setHolderIdentity(HolderIdentityType.CHILDREN);
        equityHolder.setCertifType(LoanerCertifType.ID);
        equityHolder.setHomeAddress("罗湖区");
        equityHolder.setName("张三");
        house.addEquityHolderList(equityHolder);
        mortgageHouse.setHouse(house);
        mortgage.addMortgageHouseList(mortgageHouse);

        Gson gson = new Gson();
        String jsons = gson.toJson(mortgage);
        //System.out.println(jsons);


        CostExemption costExemption = new CostExemption();
        costExemption.setId("123");
        List<CostExemptionItem> costExemptionItems  = new ArrayList<CostExemptionItem>();


        CostExemptionItem costExemptionItem = new CostExemptionItem();
        costExemptionItem.setId("");
        costExemptionItem.setExemptionReason("abc");
        costExemptionItem.setGuaranteeFee(BigDecimal.TEN);
        costExemptionItem.setInterest(BigDecimal.ZERO);
        costExemptionItem.setManageFee(BigDecimal.ZERO);
        costExemptionItem.setPeriod(1);
        costExemptionItem.setOverdueFee(BigDecimal.TEN);
        costExemptionItem.setPrepaymentFee(BigDecimal.ONE);
        costExemptionItem.setPrepaymentFeeRate(BigDecimal.ONE);
        costExemptionItem.setRepayId("123");
        costExemptionItem.setServiceFee(BigDecimal.TEN);

        CostExemptionItem costExemptionItem1 = new CostExemptionItem();
        costExemptionItem1.setId("");
        costExemptionItem1.setExemptionReason("abc");
        costExemptionItem1.setGuaranteeFee(BigDecimal.TEN);
        costExemptionItem1.setInterest(BigDecimal.ZERO);
        costExemptionItem1.setManageFee(BigDecimal.ZERO);
        costExemptionItem1.setPeriod(2);
        costExemptionItem1.setOverdueFee(BigDecimal.TEN);
        costExemptionItem1.setPrepaymentFee(BigDecimal.ONE);
        costExemptionItem1.setPrepaymentFeeRate(BigDecimal.ONE);
        costExemptionItem1.setRepayId("345");
        costExemptionItem1.setServiceFee(BigDecimal.TEN);

        costExemptionItems.add(costExemptionItem);
        costExemptionItems.add(costExemptionItem1);
        costExemption.setCostExemptionItemList(costExemptionItems);
        Gson gson1 = new Gson();
        String jsons1 = gson.toJson(costExemption);
        System.out.println(jsons1);

    }

    @Test
    public void testMatch(){
        String key = "mortgageHouseList0.house.equityHolderList1.name";
        String str = "mortgageHouseList0.house.equityHolderList1.name：mortgageHouseList0.house.equityHolderList1.certi";
        for(String s :str.split("[^\\w\\.\\-_\\(\\)]")){
            System.out.println(s.equals(key));
        }

    }

    @Test
    public void testToJson(){
        String str = "[[{\"keyName\":\"house_code\",\"dataValue\":\"3617010300000039fczh01\"},{\"keyName\":\"user\",\"dataValue\":\"曹永龙\"},{\"keyName\":\"relation\",\"dataValue\":\"1\"},{\"keyName\":\"relation_else\",\"dataValue\":\"\"},{\"keyName\":\"house_name\",\"dataValue\":\"\"},{\"keyName\":\"address\",\"dataValue\":\"深圳发展中心37楼1\"},{\"keyName\":\"area\",\"dataValue\":\"123456\"},{\"keyName\":\"price\",\"dataValue\":\"96966696\"},{\"keyName\":\"channel\",\"dataValue\":\"\"}],[{\"keyName\":\"house_code\",\"dataValue\":\"3617010300000039fczh02\"},{\"keyName\":\"user\",\"dataValue\":\"曹永龙\"},{\"keyName\":\"relation\",\"dataValue\":\"1\"},{\"keyName\":\"relation_else\",\"dataValue\":\"\"},{\"keyName\":\"house_name\",\"dataValue\":\"\"},{\"keyName\":\"address\",\"dataValue\":\"深圳发展中心37楼2\"},{\"keyName\":\"area\",\"dataValue\":\"12345678.01\"},{\"keyName\":\"price\",\"dataValue\":\"12345678.01\"},{\"keyName\":\"channel\",\"dataValue\":\"\"}],[{\"keyName\":\"house_code\",\"dataValue\":\"3617010300000039fczh03\"},{\"keyName\":\"user\",\"dataValue\":\"曹永龙\"},{\"keyName\":\"relation\",\"dataValue\":\"1\"},{\"keyName\":\"relation_else\",\"dataValue\":\"\"},{\"keyName\":\"house_name\",\"dataValue\":\"\"},{\"keyName\":\"address\",\"dataValue\":\"深圳发展中心37楼3\"},{\"keyName\":\"area\",\"dataValue\":\"51.1\"},{\"keyName\":\"price\",\"dataValue\":\"333\"},{\"keyName\":\"channel\",\"dataValue\":\"\"}],[{\"keyName\":\"house_code\",\"dataValue\":\"3617010300000039fczh04\"},{\"keyName\":\"user\",\"dataValue\":\"曹永龙\"},{\"keyName\":\"relation\",\"dataValue\":\"1\"},{\"keyName\":\"relation_else\",\"dataValue\":\"\"},{\"keyName\":\"house_name\",\"dataValue\":\"\"},{\"keyName\":\"address\",\"dataValue\":\"深圳发展中心37楼4\"},{\"keyName\":\"area\",\"dataValue\":\"12345678.01\"},{\"keyName\":\"price\",\"dataValue\":\"96966696\"},{\"keyName\":\"channel\",\"dataValue\":\"\"}],[{\"keyName\":\"house_code\",\"dataValue\":\"3617010300000039fczh05\"},{\"keyName\":\"user\",\"dataValue\":\"曹永龙\"},{\"keyName\":\"relation\",\"dataValue\":\"1\"},{\"keyName\":\"relation_else\",\"dataValue\":\"\"},{\"keyName\":\"house_name\",\"dataValue\":\"\"},{\"keyName\":\"address\",\"dataValue\":\"深圳发展中心37楼5\"},{\"keyName\":\"area\",\"dataValue\":\"51\"},{\"keyName\":\"price\",\"dataValue\":\"96966696\"},{\"keyName\":\"channel\",\"dataValue\":\"\"}]]";
        if(StringUtils.isNotEmpty(str)) {
            JSONArray jsonArray = JSONArray.fromObject(str);
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONArray jsonArray1 = JSONArray.fromObject(jsonArray.get(i));
                for (int j = 0; j < jsonArray1.size(); j++) {
                    JSONObject jsonObject = jsonArray1.getJSONObject(j);
                    String key = jsonObject.get("keyName").toString();
                    if (StringUtils.isNotEmpty(key)) {
                        if (null != jsonObject.get("dataValue")) {
                            map.put(key, jsonObject.get("dataValue").toString());
                        } else {
                            map.put(key, "");
                        }
                    }
                }
            }
            HouseInfo houseInfo = new HouseInfo();
            for (Field field : HouseInfo.class.getDeclaredFields()) {
                String keyName = field.getName().toUpperCase();
                map.forEach((k, v) -> {
                    String mapKey = k.toUpperCase().replace("_", "");
                    if (mapKey.equals("USER")) mapKey = "OWER";
                    if (keyName.equals(mapKey) || mapKey.contains(keyName)) {
                        try {
                            field.setAccessible(true);
                            field.set(houseInfo, v);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                });
            }
        }
    }
    @Test
    public void testCompareTo(){
        String a = "a";
        String b = "b";
        System.out.println(a.compareTo(b));
        Integer c  = 1;
        Integer d =  1;
        System.out.println(d.compareTo(c));
    }

    @Test
    public void testReflect(){
        for(Field field : Extension.class.getDeclaredFields()){
            System.out.println(String.class==(field.getGenericType()));
        }
    }
    @Test
    public void testBig(){
        BigDecimal b = null;
        System.out.println(DecimalFormatUtils.isNotEmpty(b));
        b =new BigDecimal(0.0);
        System.out.println(DecimalFormatUtils.isNotEmpty(b));
        b = BigDecimal.ONE;
        System.out.println(DecimalFormatUtils.isNotEmpty(b));

        BigDecimal a = new BigDecimal(10);
        BigDecimal c =new BigDecimal(4);
        System.out.println(a.subtract(c));

        System.out.println(BigDecimal.TEN.add(BigDecimal.ONE).add(BigDecimal.ZERO));
    }


    @Test
    public void rar()throws  Exception{
        String str = "data:image/jpg;base64,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";
        System.out.println("原来字符串："+str+"   len:"+str.length());
        String yasuo = yasuo(str);
        System.out.println("压缩字符串："+yasuo+"   len:"+yasuo.length());
        System.out.println("解压字符串："+jieya(yasuo)+"   len:"+jieya(yasuo).length());
    }

    @Test
    public void hfm()throws Exception{
        String base64 = "data:image/jpg;base64,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";
        System.out.println("原来字符串："+base64);
        System.out.println("原来字符串长度："+base64.length());
        Map<Character,Map<String,Object>> charMapCount  = new HashMap<Character,Map<String,Object>>();
        for(char c : base64.toCharArray()){
            Map<String,Object> map = charMapCount.get(c);
            if(map==null){
                map = new HashMap<>();
                map.put("char",c);
                map.put("count",1);
                charMapCount.put(c,map);
            }else{
                map.put("count",(Integer)map.get("count")+1);
                charMapCount.put(c,map);
            }
        }
        List<Map<String,Object>> countlist = new ArrayList<Map<String,Object>>(charMapCount.values());
        countlist.sort((Map h1, Map h2) -> ((Integer)h1.get("count")).compareTo((Integer)h2.get("count")));
        HashMap<Character,String> cmb = new HashMap<Character, String>();
        HashMap<String,Character> bmc = new HashMap<String, Character>();
        for(int i=0;i<countlist.size();i++){
            Map<String,Object> map = countlist.get(i);
            cmb.put((Character) map.get("char"),Integer.toBinaryString(i));
            bmc.put(Integer.toBinaryString(i),(Character)map.get("char"));
        }

        List<String> bsort = new ArrayList<String>();
        for(int i=128;i>=0;i--){
            bsort.add(Integer.toBinaryString(i));
        }
        System.out.println(bsort);



        StringBuffer binarySb = new StringBuffer();
        for(char c : base64.toCharArray()){
            binarySb.append(cmb.get(c));
        }
        String binaryStr = binarySb.toString();
        System.out.println("\n\r"+binaryStr);
        StringBuffer cnSb = new StringBuffer();
        for(int i=0;i<binaryStr.length()/14;i++) {
            cnSb.append(BS2CN(binaryStr.substring(i * 14, (i + 1) * 14)));
        }
        String cnStr = cnSb.toString();
        System.out.println(cnStr);
        StringBuffer newBase64Binray = new StringBuffer();
        for(int  i=0;i<cnStr.length();i++){
            String cn = cnStr.substring(i,i+1);
            newBase64Binray.append(CN2BS(cn));
        }
        String newBase64BinaryString = binarySb.toString();
        System.out.println(newBase64BinaryString);
        while(newBase64BinaryString.length()>0){
           for(String str : bsort){
                if(newBase64BinaryString.startsWith(str)){
                    System.out.print(bmc.get(str));
                    newBase64BinaryString  = newBase64BinaryString.substring(str.length());
                }
            }
        }
    }
    private String BS2CN(String b)throws Exception{
        byte[] bytes = new byte[2];
        bytes[0]=(byte)(BS2Int(b.substring(0,7))+0x80);
        bytes[1]=(byte) (BS2Int(b.substring(7,14))+0x80);
        String cn = new String(bytes,"gbk");
        return cn;
    }
    private String CN2C(String cn,Map<String,Character> bmc)throws Exception{
        String str = "";
        byte[] cnb = cn.getBytes("gbk");
        for(int i=0; i< cnb.length ; i++){
            System.out.println(Integer.toBinaryString(cnb[i]&0xff-0x80));
            str+= bmc.get(Integer.toBinaryString(cnb[i]&0xff-0x80));
        }
        return str;
    }
    private String CN2BS(String cn)throws Exception{
        String fullstr = "0000000";
        String bstr = "";
        byte[] cnb = cn.getBytes("gbk");
        for(int i=0; i< cnb.length ; i++){
            String str = (Integer.toBinaryString(cnb[i]&0xff-0x80));
            bstr += (fullstr.substring(0,7-str.length())+str);
        }
        return bstr;
    }
    private Integer BS2Int(String str){
        int sum = 0;
        for(int i=str.length()-1;i>=0;i--){
            if(str.charAt(i)=='1')sum += Math.pow(2,str.length()-i-1);
        }
        return (Integer)sum;
    }
    private String  yasuo(String text)throws Exception{
        String cnStr = "";
        for(int i=0;i<text.length()/2;i++){
            String a2 = text.substring(i*2,(i+1)*2);
            byte [] b = null;
            b  = a2.getBytes("ascii");
            byte[] fullbyte = new byte[2];
            for(int j=0; j< b.length ; j++){
                fullbyte[j] = (byte)(b[j]+0x80);
            }
            cnStr  += new String(fullbyte,"gbk");
        }
        return cnStr;
    }
    private String jieya(String yasuo)throws Exception{
        String aStr = "";
        for(int i=0;i<yasuo.length();i++){
            String cnStr = yasuo.substring(i,i+1);
            byte[] b = cnStr.getBytes("gbk");
            for(int j=0;j<b.length;j++){
                byte[] abyte = new byte[1];
                abyte[0] = (byte)(b[j]&0xff-0x80);
                aStr+=new String(abyte);
            }
        }
        return aStr;
    }


    private void printCN2B(String cnstr){
        byte [] cnb = null;
        try {
            cnb = cnstr.getBytes("utf-8");
            for(int i=0; i< cnb.length ; i++){
                System.out.print(Integer.toBinaryString(cnb[i]&0xff));
//                System.out.println("\n\r"+Integer.toBinaryString(cnb[i]&0xff));
            }
            System.out.println(new String(cnb,"utf-8"));
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
    }

    @Test
    public void testIsEmpty(){
        BigDecimal a = null;
        System.out.println(DecimalFormatUtils.isEmpty(a));
    }

    @Test
    public void testDelete(){
        String str = ",ABC,EFG,";
        String rolestr = "OPQ,EFG,KLM,XYZ,ABC,";
        for(String role :rolestr.split(",")){
            if(!str.contains(role)){
                rolestr = rolestr.replace(role+",","");
            }
        }
        System.out.println(rolestr);
    }

    @Test
    public void getObjectFeilds(){
        StringBuffer stringBuffer   =new StringBuffer();
        for(Field field  :Enterprise.class.getDeclaredFields()){
            stringBuffer.append(field.getName()+"|");
        }
        for(Field field  :Enterprise.class.getSuperclass().getDeclaredFields()){
            stringBuffer.append(field.getName()+"|");
        }
        System.out.println(stringBuffer.toString());
    }


    @Test
    public void updateMenu(){
        String json  = "{\n" +
                "    \"id\":\"${id}\",\n" +
                "    \"name\":\"${menuName}\",\n" +
                "    \"alias\":\"${alias}\",\n" +
                "    \"url\":\"/\",\n" +
                "    \"platform\":\"PC\",\n" +
                "    \"parentId\":\"${parentId}\",\n" +
                "    \"type\":\"03\",\n" +
                "    \"buttonList\":[\n" +
                "    ${buttonList} "+
                "    ]\n" +
                "}";
        String button = "{\n" +
                "            \"name\":\"${buttonName}\",\n" +
                "            \"alias\":\"${buttonAlias}\",\n" +
                "            \"url\":\"\",\n" +
                "            \"id\":\"\"\n" +
                "        }";


        String text = "业务审批查询\t\t查询\t\thasQuery\n" +
                "查看\t\thasView\n" +
                "\n" +
                "业务审批\t\t查询\t\thasQuery\n" +
                "审批\t\thasApproval\n" +
                "\n" +
                "业务展期\t\t查询\t\thasQuery\n" +
                "展期\t\thasExtension\n" +
                "查看\t\thasView\n" +
                "\n";


        for(String str :text.split("\n\n")){
            String menustr = json;
            String buttonstr = "";
            for(String line : str.split("\n")){
                List<String> values = new ArrayList<>();
                for(String value : line.split("\t")){
                    if(StringUtils.isNotEmpty(value)){
                        value = value.trim();
                        values.add(value);
                    }
                }
                if(values.size()==3){
                    menustr = menustr.replace("${menuName}",values.remove(0));
                }
                buttonstr += button.replace("${buttonName}",values.remove(0))
                        .replace("${buttonAlias}",values.remove(0))+",";

            }
            menustr =  menustr.replace("${buttonList}",buttonstr);
            System.out.println(menustr);
            System.out.println("===================");
        }
    }
    @Test
    public void addMenu(){
        String json  = "{\n" +
                "    \"id\":\"\",\n" +
                "    \"name\":\"${menuName}_${tabName}\",\n" +
                "    \"alias\":\"${alias}\",\n" +
                "    \"url\":\"/\",\n" +
                "    \"platform\":\"PC\",\n" +
                "    \"parentId\":\"cff1e57c-c8d4-11e6-a7f3-005056902907\",\n" +
                "    \"type\":\"03\",\n" +
                "    \"buttonList\":[\n" +
                "    ${buttonList} "+
                "    ]\n" +
                "}";
        String button = "{\n" +
                "            \"name\":\"${buttonName}\",\n" +
                "            \"alias\":\"${buttonAlias}\",\n" +
                "            \"url\":\"\",\n" +
                "            \"id\":\"\"\n" +
                "        }";
        String text = "居间费付费申请\t待申请\t查询\tResidencePendingApplication\thasQuery\n" +
                "\t\t补录\t\thasMakeup\n" +
                "\t\t查看\t\thasView\n" +
                "\t已申请\t查询\tResidenceApplied\thasQuery\n" +
                "\t\t查看\t\thasView\n" +
                "\t\t流程查看\t\thasFlowView\n" +
                "\n" +
                "居间费业务审批\t待审批\t查询\tResidenceToBeApproved\thasQuery\n" +
                "\t\t查看\t\thasView\n" +
                "\t\t审批\t\thasApproval\n" +
                "\t已审批\t查询\tResidenceHaveBeenApproved\thasQuery\n" +
                "\t\t查看\t\thasView\n" +
                "\t\t流程查看\t\thasFlowView\n" +
                "\n" +
                "房产解押信息审批\t待审批\t查询\tDecompressionToBeApproved\thasQuery\n" +
                "\t\t查看\t\thasView\n" +
                "\t\t审批\t\thasApproval\n" +
                "\t已审批\t查询\tDecompressionHaveBeenApproved\thasQuery\n" +
                "\t\t查看\t\thasView\n" +
                "\t\t流程查看\t\thasFlowView\n" +
                "\n" +
                "房产抵押信息审批\t待审批\t查询\tMortgageToBeApproved\thasQuery\n" +
                "\t\t查看\t\thasView\n" +
                "\t\t审批\t\thasApproval\n" +
                "\t已审批\t查询\tMortgageHaveBeenApproved\thasQuery\n" +
                "\t\t查看\t\thasView\n" +
                "\t\t流程查看\t\thasFlowView\n" +
                "\n" +
                "展期业务审批\t待审批\t查询\tExtensionToBeApproved\thasQuery\n" +
                "\t\t查看\t\thasView\n" +
                "\t\t审批\t\thasApproval\n" +
                "\t已审批\t查询\tExtensionHaveBeenApproved\thasQuery\n" +
                "\t\t查看\t\thasView\n" +
                "\t\t流程查看\t\thasFlowView\n" +
                "\n" +
                "费用减免业务审批\t待审批\t查询\tCostReductionToBeApproved\thasQuery\n" +
                "\t\t查看\t\thasView\n" +
                "\t\t审批\t\thasApproval\n" +
                "\t已审批\t查询\tCostReductionHaveBeenApproved\thasQuery\n" +
                "\t\t查看\t\thasView\n" +
                "\t\t流程查看\t\thasFlowView\n" +
                "\n" +
                "渠道提单\t渠道待提单\t查询\tChannelToBeBill\thasQuery\n" +
                "\t\t编辑\t\thasEdit\n" +
                "\t\t取消\t\thasCancel\n" +
                "\t\t新增业务\t\thasAdd\n" +
                "\t渠道已提单\t查询\tChannelHaveBeenBill\thasQuery\n" +
                "\t\t查看\t\thasView";


        for(String str :text.split("\n\n")){
            String menustr = json;
            String menuName = "";
            String buttonstr = "";
            for(String line : str.split("\n")){
                List<String> values = new ArrayList<>();
                for(String value : line.split("\t")){
                    if(StringUtils.isNotEmpty(value)){
                        value = value.trim();
                        values.add(value);
                    }
                }
                //System.out.println(values.toString());
                if(values.size()==5){
                    menuName = values.remove(0);
                }
                if(values.size()==4){
                    if(menustr!=json){
                        menustr =  menustr.replace("${buttonList}",buttonstr);
                        System.out.println(menustr);
                    }

                    menustr = json;
                    buttonstr="";
                    menustr = menustr.replace("${menuName}",menuName).
                            replace("${tabName}",values.remove(0)).
                            replace("${alias}",values.remove(1));
                }
                buttonstr += button.replace("${buttonName}",values.remove(0))
                        .replace("${buttonAlias}",values.remove(0))+",";

            }
            menustr =  menustr.replace("${buttonList}",buttonstr);
            System.out.println(menustr);
            System.out.println("===================");
        }
    }

    @Test
    public void testLuodan(){
        Integer[] ints = new Integer[]{1,2,3,4,4,2,3,1,5,6,5};
        Set<Integer> set  = new HashSet<Integer>();
        int size = set.size();
        for(int i : ints){
            set.add(i);
            if(set.size()==size){
                set.remove(i);
            }
            size  = set.size();
        }
        System.out.println(set.toString());
    }

    @Test
    public void testYouxuLuodan(){
        Integer[] ints = new Integer[]{1,1,2,2,3,4,4,5,5};
        int sum   = 0;
        for(int i=0;i<ints.length;i++){
            sum += ints[i]*(i%2==0?1:-1);
        }
        System.out.println(sum);
    }
}

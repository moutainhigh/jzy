package com.kaisa.kams.components.service.push;

import com.kaisa.kams.components.utils.StringFormatUtils;
import com.kaisa.kams.components.utils.TimeUtils;
import com.kaisa.kams.enums.push.ItemType;
import com.kaisa.kams.models.HouseInfo;
import com.kaisa.kams.models.LoanBorrower;
import com.kaisa.kams.models.ProductInfoItem;
import com.kaisa.kams.models.push.LoanPush;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.mvc.Mvcs;
import org.nutz.service.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取推单初始信息基础类
 *
 * @author pengyueyang created on 2017/11/9.
 */
public abstract class BaseItemTypeStrategyService extends Service {

    private final String DAY_COST = "DAY_COST";
    private final String CASH_COST = "CASH_COST";
    private final String COMMON_BASIC = "{\"basic\" : {\"type\":\"text\",\"title\":\"基本信息\",\"value\":\"${name}， ${age}岁，${sex}，现居住${address}\"},";
    private final String MORTGAGE_HOUSE_INFO = "\"houseinfo\" : {\"type\":\"text\",\"title\":\"房产信息\",\"value\":\"" +
            "房产地址：${houseAddress}\\n房产证号：${houseNumber}\\n房产面积：${houseArea}\\n房产估值：${houseAmount}\"},";
    private final String BANK_HOUSE_INFO = "\"houseinfo\" : {\"type\":\"text\",\"title\":\"房产信息\",\"value\":\"" +
            "房产地址：${houseAddress}；房产证号：${houseNumber}；房产面积：${houseArea}；房产估值：${houseAmount}\"},";
    private final int ID_NUMBER_DIGITS_18 = 18;
    private final int ID_NUMBER_DIGITS_15 = 15;
    private final BigDecimal TEN_THOUSANDS = new BigDecimal(10000);

    public static BaseItemTypeStrategyService newStrategy(ItemType itemType) {
        switch (itemType) {
            case CAR_LOAN:
                return Mvcs.getIoc().get(CarLoanStrategyService.class);
            case HOUSE_MORTGAGE_LOAN:
                return Mvcs.getIoc().get(HouseMortgageLoanStrategyService.class);
            case BANK_HOUSE_LOAN:
                return Mvcs.getIoc().get(BankHouseLoanStrategyService.class);
            case PERSONAL_LOAN:
                return Mvcs.getIoc().get(PersonalLoanStrategyService.class);
            case FACTORING:
                return Mvcs.getIoc().get(FactoringStrategyService.class);
            case BILL:
                return Mvcs.getIoc().get(BillStrategyService.class);
            case BANK_BILL:
                return Mvcs.getIoc().get(BankBillStrategyService.class);
            default:
                throw new IllegalArgumentException("Incorrect ItemType");
        }
    }

    /**
     * 获取推单订单初始内容
     * @param loanPush
     * @return
     */
    public abstract String getInitContent(LoanPush loanPush);

    protected LoanBorrower getMasterLoanBorrowerByLoanId(String loanId) {
        if (StringUtils.isEmpty(loanId)) {
            return null;
        }
        return dao().fetch(LoanBorrower.class, Cnd.where("loanId", "=", loanId).and("master", "=", true));
    }

    protected String getCommonBasic(LoanBorrower loanBorrower) {
        Map<String, String> valuesMap = new HashMap<>(4);
        valuesMap.put("name", loanBorrower.getName());
        valuesMap.put("address", loanBorrower.getAddress() == null ? "【借款人家庭住址】" : loanBorrower.getAddress());
        valuesMap.put("age", getAge(loanBorrower.getCertifNumber()));
        valuesMap.put("sex", getSex(loanBorrower.getCertifNumber()));
        return StringFormatUtils.format(COMMON_BASIC, valuesMap);
    }

    protected Map<String, String> getBusinessInfo(String loanId) {
        if (StringUtils.isEmpty(loanId)) {
            return new HashMap<>(0);
        }
        List<ProductInfoItem> list = dao().query(ProductInfoItem.class, Cnd.where("loanId", "=", loanId));
        Map<String, String> result = new HashMap<>(10);
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(productInfoItem ->
                    result.put(productInfoItem.getKeyName(), productInfoItem.getDataValue())
            );
        }
        return result;
    }

    protected String getMortgageHouseInfo(List<HouseInfo> houseInfoList) {
        return StringFormatUtils.format(MORTGAGE_HOUSE_INFO, getHouseInfo(houseInfoList));

    }

    protected String getBankHouseInfo(List<HouseInfo> houseInfoList) {
        return StringFormatUtils.format(BANK_HOUSE_INFO, getHouseInfo(houseInfoList));
    }


    private HashMap<String, String> getHouseInfo(List<HouseInfo> houseInfoList) {
        StringBuilder houseAddress = new StringBuilder();
        StringBuilder houseNumber = new StringBuilder();
        StringBuilder houseArea = new StringBuilder();
        StringBuilder houseAmount = new StringBuilder();
        HashMap<String, String> valuesMap = new HashMap<>(4);
        if (CollectionUtils.isNotEmpty(houseInfoList)) {
            int size = houseInfoList.size();
            String comma = "，";
            for (int i = 0; i < size; i++) {
                HouseInfo houseInfo = houseInfoList.get(i);
                if (i == size - 1) {
                    comma = "";
                }
                houseAddress.append(houseInfo.getAddress()).append(comma);
                houseNumber.append(houseInfo.getCode()).append(comma);
                houseArea.append(houseInfo.getArea()).append("（㎡）").append(comma);
                houseAmount.append(houseInfo.getPrice()).append("（万元）").append(comma);
            }
        }
        valuesMap.put("houseAddress", houseAddress.toString());
        valuesMap.put("houseNumber", houseNumber.toString());
        valuesMap.put("houseArea", houseArea.toString());
        valuesMap.put("houseAmount", houseAmount.toString());
        return valuesMap;
    }

    protected String getPurposeCH(String purpose) {
        if (StringUtils.isNotEmpty(purpose)) {
            if (DAY_COST.equals(purpose)) {
                return "日常消费";
            } else if (CASH_COST.equals(purpose)) {
                return "资金周转";
            }
        }
        return "【借款用途】";
    }

    protected String getLoanAmountWithTenThousands(String loanId, String defaultValue) {
        if (StringUtils.isEmpty(loanId)) {
            return defaultValue;
        }
        Sql sql = Sqls.fetchString("select actualAmount from sl_loan where id=@loanId");
        sql.setParam("loanId", loanId);
        dao().execute(sql);
        String result = sql.getString();
        if (StringUtils.isNotEmpty(result)) {
            return new BigDecimal(result).divide(TEN_THOUSANDS).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
        }
        return defaultValue;
    }

    protected BigDecimal getLoanAmount(String loanId) {
        if (StringUtils.isEmpty(loanId)) {
            return BigDecimal.ZERO;
        }
        Sql sql = Sqls.fetchString("select actualAmount from sl_loan where id=@loanId");
        sql.setParam("loanId", loanId);
        dao().execute(sql);
        String result = sql.getString();
        if (StringUtils.isNotEmpty(result)) {
            return new BigDecimal(result);
        }
        return BigDecimal.ZERO;
    }

    protected List<HouseInfo> getHouseList(String loanId) {
        return dao().query(HouseInfo.class, Cnd.where("loanId", "=", loanId).asc("position"));
    }

    protected String getAllBorrowers(String loanId) {
        if (StringUtils.isEmpty(loanId)) {
            return "";
        }
        Sql sql = Sqls.fetchString("select GROUP_CONCAT(name) from sl_loan_borrower where loanId=@loanId order by master desc");
        sql.setParam("loanId", loanId);
        dao().execute(sql);
        return sql.getString();
    }

    protected String getProjectDescHouseInfo(List<HouseInfo> houseInfoList) {
        StringBuilder houseInfo = new StringBuilder();
        if (CollectionUtils.isNotEmpty(houseInfoList)) {
            int size = houseInfoList.size();
            String semicolon = ";";
            for (int i = 0; i < size; i++) {
                HouseInfo house = houseInfoList.get(i);
                if (i == size - 1) {
                    semicolon = "";
                }
                houseInfo.append(house.getAddress());
                houseInfo.append("，建筑面积");
                houseInfo.append(house.getArea());
                houseInfo.append("平方米");
                houseInfo.append(semicolon);
            }
        }
        return houseInfo.toString();
    }

    private String getAge(String idNumber) {
        if (!isIdCard(idNumber)) {
            return "";
        }
        String birthdayStr = idNumber.substring(6, 12);
        birthdayStr = "19" + birthdayStr;
        if (idNumber.length() == ID_NUMBER_DIGITS_18) {
            birthdayStr = idNumber.substring(6, 14);
        }
        return Long.toString(TimeUtils.yearsBetween(TimeUtils.formatDateToLocalDate(birthdayStr), LocalDate.now()));
    }

    private String getSex(String idNumber) {
        if (!isIdCard(idNumber)) {
            return "";
        }
        int sex = Character.getNumericValue(idNumber.charAt(ID_NUMBER_DIGITS_15));
        if (idNumber.length() == ID_NUMBER_DIGITS_18) {
            sex = Character.getNumericValue(idNumber.charAt(17));
        }
        return sex % 2 == 0 ? "男" : "女";
    }

    private boolean isIdCard(String idNumber) {
        if (StringUtils.isEmpty(idNumber)) {
            return false;
        }
        if (idNumber.length() != ID_NUMBER_DIGITS_18 && idNumber.length() != ID_NUMBER_DIGITS_15) {
            return false;
        }
        return true;
    }


}

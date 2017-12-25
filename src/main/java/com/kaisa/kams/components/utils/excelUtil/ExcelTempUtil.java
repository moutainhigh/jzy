
package com.kaisa.kams.components.utils.excelUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 该类实现了将一组对象转换为Excel表格
 * 该类利用了BeanUtils框架中的反射完成
 * 使用该类的前提，在相应的实体对象上通过ExcelReources来完成相应的注解
 *
 * @author luoyj
 * @date 2017-02-27
 */
public class ExcelTempUtil {

    private static ExcelTempUtil instance = new ExcelTempUtil();

    private ExcelTempUtil() {
    }

    public static ExcelTempUtil getInstance() {
        return instance;
    }


    /**
     * 将对象转换为Excel并且导出，该方法是基于模板的导出，导出到一个具体的路径中
     *
     * @param data    模板中的替换的常量数据
     * @param template 模板路径
     * @param clz      对象的类型
     */
    public void exportObj2ExcelByTemplate(Map<String, String> data,
                                          String template, String fileName, Map<String, List<?>> map,
                                          Class<?> clz, HttpServletRequest request, HttpServletResponse response) throws SecurityException,
            IllegalArgumentException, NoSuchMethodException,
            ClassNotFoundException, IllegalAccessException,
            InvocationTargetException {
        ExcelTemplate et = ExcelTemplate.getInstance();
        et.readTemplateByPath(template);
        List<ExcelHeader> headers = getHeaderList(clz);
        Collections.sort(headers);
        for (int i = 0; i < map.size(); i++) {
            String key = "datas_" + (i + 1);
            et.hqLocation(key);
            for (Object obj : map.get(key)) {
                et.createNewRow();
                for (ExcelHeader eh : headers) {
                    et.createCell(BeanUtils.getProperty(obj,
                            getMethodName(eh)));
                }
            }
        }
        et.replaceFinalData(data);
        et.setRowHeight();
        et.writeToWeb(fileName, request, response);
    }

    /**
     * 根据标题获取相应的方法名称
     *
     * @param eh
     * @return
     */
    private String getMethodName(ExcelHeader eh) {
        String mn = eh.getMethodName().substring(3);
        mn = mn.substring(0, 1).toLowerCase() + mn.substring(1);
        return mn;
    }

    /**
     * 得到指定类的ExcelHeader集合
     *
     * @param clz 指定的类
     */
    private List<ExcelHeader> getHeaderList(Class<?> clz) {
        List<ExcelHeader> headers = new ArrayList<>();
        for (Method m : clz.getDeclaredMethods()) {
            if (m.getName().startsWith("get")) {
                if (m.isAnnotationPresent(ExcelResources.class)) {
                    ExcelResources er = m.getAnnotation(ExcelResources.class);
                    headers.add(new ExcelHeader(er.title(), er.order(), m.getName()));
                }
            }
        }
        return headers;
    }

    /**
     * 得到标题所在列数和对应方法名的Map对象
     *
     * @param titleRow 标题行
     * @param clz      指定类
     */
    private Map<Integer, String> getHeaderMap(Row titleRow, Class<?> clz) {
        List<ExcelHeader> headers = getHeaderList(clz);
        Map<Integer, String> maps = new HashMap<>();
        for (Cell c : titleRow) {
            String title = c.getStringCellValue();
            for (ExcelHeader eh : headers) {
                if (eh.getTitle().equals(title)) {
                    maps.put(c.getColumnIndex(), eh.getMethodName().replace("get", "set"));
                    break;
                }
            }
        }
        return maps;
    }

    /**
     * 把单元格中的数据转化为String类型
     *
     * @param c 单元格
     */
    private String getCellValue(Cell c) {
        String value;
        switch (c.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                value = "";
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                value = String.valueOf(c.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA:
                value = String.valueOf(c.getCellFormula());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                value = String.valueOf(c.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING:
                value = c.getStringCellValue();
                break;
            default:
                value = null;
                break;
        }
        return value;
    }

}

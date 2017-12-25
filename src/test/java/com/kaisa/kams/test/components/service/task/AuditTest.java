package com.kaisa.kams.test.components.service.task;

import com.kaisa.kams.enums.OperationType;
import com.kaisa.kams.test.BaseTest;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutType;

import java.util.HashMap;

/**
 * @author pengyueyang created on 2017/11/28.
 */
public class AuditTest extends BaseTest {


    @Test
    public void testGetOperationInfo() {
        String OPERATION_INFO_FILE_PATH = "operation-type.json";
        HashMap<String,HashMap<String,OperationType>> OPERATION_INFO = new HashMap<>(10);
        String json = Files.read(OPERATION_INFO_FILE_PATH);
        OPERATION_INFO.putAll((HashMap<String, HashMap<String, OperationType>>) Json.fromJson(NutType.mapStr(NutType.mapStr(OperationType.class)),json));
        int i = 0;

        for (String key : OPERATION_INFO.keySet()) {
            for (String keyTwo : OPERATION_INFO.get(key).keySet()) {
                OPERATION_INFO.get(key).get(keyTwo);
                i++;
            }
        }

        assertEquals(OperationType.values().length,i);
    }

}

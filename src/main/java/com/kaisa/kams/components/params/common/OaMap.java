package com.kaisa.kams.components.params.common;

import org.nutz.lang.util.NutMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhouchuang on 2017/8/16.
 */
public class OaMap extends NutMap {

    public OaMap() {
        super();
        this.put("ok",true);
        this.put("msg","操作成功");
    }

    @Override
    public NutMap setv(String key, Object value) {
        if(key.contains(".")){
            String[] keys = key.split("\\.");
            HashMap<String,Object> currentMap  =  this;
            for(int i=0;i<keys.length-1;i++){
                String k = keys[i];
                if(!currentMap.containsKey(k)){
                    currentMap.put(k,new HashMap<String,Object>());
                }
                currentMap = (HashMap)currentMap.get(k);
            }
            currentMap.put(keys[keys.length-1],value);
        }else{
            this.put(key, value);
        }
        return this;
    }



}

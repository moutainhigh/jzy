package com.kaisa.kams.components.service.base;

import com.kaisa.kams.models.BaseModel;

import org.apache.commons.lang.StringUtils;
import org.nutz.service.IdNameEntityService;

import java.util.Date;
import java.util.List;

/**
 * Created by pengyueyang on 2017/4/18.
 */
public class BaseService<T extends BaseModel> extends IdNameEntityService<T> {

    <T extends BaseModel> T add(T obj){
        obj.setCreateBy("1");
        obj.setCreateTime(new Date());
        obj.setUpdateBy("1");
        obj.setUpdateTime(new Date());
        return dao().insert(obj);
    }

    int update(T obj){
        obj.setUpdateBy("2");
        obj.setUpdateTime(new Date());
        return dao().update(obj);
    }

    protected  <T extends BaseModel> T persistence(T obj ,String filter){
        obj.updateOperator();
        if(StringUtils.isNotEmpty(obj.getId())){
            dao().update(obj,filter);
            return obj;
        }else{
            return  dao().insert(obj);
        }
    }

    /**
     *对比持久状态和提交状态，删除掉游离状态
     */
    public  <T extends BaseModel>  void removeDeletedBaseModel(List<T> storeList, List<T> currentList){
        for(T baseModel : storeList){
            boolean isDeleted = true;
            for(T baseModel1:currentList ){
                if(baseModel.getId().equals(baseModel1.getId())){
                    isDeleted = false;
                    break;
                }
            }
            if (isDeleted){
                dao().delete(baseModel);
            }
        }
    }

}

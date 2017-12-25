package com.kaisa.kams.components.utils;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * TreeNode 获取树节点集合
 * Created by luoyj on 2016/11/24.
 */
public class TreeNodeUtil {
    public final static List<JsonTreeData> getfatherNode(List<JsonTreeData> treeDataList) {
        List<JsonTreeData> newTreeDataList = new ArrayList<JsonTreeData>();
        for (JsonTreeData jsonTreeData : treeDataList) {
            if(StringUtils.isEmpty(jsonTreeData.getPid())) {
                //获取父节点下的子节点
                jsonTreeData.setChildren(getChildrenNode(jsonTreeData.getId(),treeDataList));
                newTreeDataList.add(jsonTreeData);
            }
        }
        return newTreeDataList;
    }

    private final static List<JsonTreeData> getChildrenNode(String pid,List<JsonTreeData> treeDataList) {
        List<JsonTreeData> newTreeDataList=new ArrayList<JsonTreeData>();
        for (JsonTreeData jsonTreeData : treeDataList) {
            if(StringUtils.isEmpty(jsonTreeData.getPid()))  continue;
            //这是一个子节点
            if(jsonTreeData.getPid().equals(pid)){
                //递归获取子节点下的子节点
                jsonTreeData.setChildren(getChildrenNode(jsonTreeData.getId(),treeDataList));
                newTreeDataList.add(jsonTreeData);
            }
        }
        return newTreeDataList;
    }
}

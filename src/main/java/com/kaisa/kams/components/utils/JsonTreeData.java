package com.kaisa.kams.components.utils;

import java.util.List;

/**
 * Created by luoyj on 2016/11/24.
 */
public class JsonTreeData {

    private  String id ;// id

    private String pid;// 父id

    private String text;// 显示文本

    private String  orgType;// 级别位置（机构，组织，或者岗位）

    private String path;// 完整上一级数据

    private List<JsonTreeData> children;//子节点

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setText(String text) {
        this.text = text;
    }


    public void setChildren(List<JsonTreeData> children) {
        this.children = children;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getId() {
        return id;
    }

    public String getPid() {
        return pid;
    }

    public String getText() {
        return text;
    }

    public List<JsonTreeData> getChildren() {
        return children;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public String getOrgType() {
        return orgType;
    }
}

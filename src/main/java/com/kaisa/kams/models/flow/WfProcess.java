package com.kaisa.kams.models.flow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by weid on 2016/12/2.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WfProcess {
    private String id;

    private String name;

    private String displayName;

    private String type;

    private String instanceUrl;

    private int state;

    private byte[] content;

    private int version;

    private String createTime;

    private String creator;

}

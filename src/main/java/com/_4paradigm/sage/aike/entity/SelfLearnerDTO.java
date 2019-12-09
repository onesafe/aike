package com._4paradigm.sage.aike.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by wangyiping on 2019/12/9 4:27 PM.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelfLearnerDTO {
    private String sdkServerUrl;
    private String accessKey;
    private Integer workSpaceID;

    // 模型组所在的空间
    private String nameSpace;
    // 方案名称
    private String solutionName;


}

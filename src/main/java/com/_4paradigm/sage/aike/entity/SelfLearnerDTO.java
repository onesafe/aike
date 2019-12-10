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
    private Long dagID;

    // 方案名称
    private String solutionName;

    private String solutionDescribe;

    private String outputModelGroupName;

    private RunStrategy runStrategy;
}

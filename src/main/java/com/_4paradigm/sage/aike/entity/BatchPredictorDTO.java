package com._4paradigm.sage.aike.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by wangyiping on 2019/12/10 4:11 PM.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchPredictorDTO {
    private Long dagID;

    private String solutionName;

    private String solutionDescribe;

    private String outputTableGroupName;

    private RunStrategy runStrategy;
}

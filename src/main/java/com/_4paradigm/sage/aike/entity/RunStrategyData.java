package com._4paradigm.sage.aike.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by wangyiping on 2019/12/10 11:57 AM.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunStrategyData {
    private String startTime;
    private String endTime;
    private Long interval;
}

package com._4paradigm.sage.aike.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by wangyiping on 2019/12/9 6:22 PM.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DagDMO {
    private long id;
    private String dagName;
    private String dagContent;
}

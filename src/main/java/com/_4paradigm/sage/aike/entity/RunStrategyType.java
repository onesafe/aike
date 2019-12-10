package com._4paradigm.sage.aike.entity;

/**
 * Created by wangyiping on 2019/12/10 11:56 AM.
 */
public enum RunStrategyType {

    scheduleRunByTimePeriod("scheduleRunByTimePeriod");

    private final String value;

    RunStrategyType(String v) {
        value = v;
    }

    public static RunStrategyType fromValue(String v) {
        for (RunStrategyType c : RunStrategyType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}

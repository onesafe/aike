package com._4paradigm.sage.aike.io;

import io.undertow.util.StatusCodes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by wangyiping on 2019/12/9 6:43 PM.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Response<T> {
    private T data;
    private int code;
    private String message;
}

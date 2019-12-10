package com._4paradigm.sage.aike.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Created by wangyiping on 2019/12/10 10:52 AM.
 */
@Configuration
public class Constants {

    public static String sdkServerUrl;

    @Value("${sdkserver.url}")
    public void setSdkServerUrl(String sdkServerUrl) {
        Constants.sdkServerUrl = sdkServerUrl;
    }
}

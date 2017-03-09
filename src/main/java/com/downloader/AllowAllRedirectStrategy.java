package com.downloader;

import org.apache.http.impl.client.DefaultRedirectStrategy;

/**
 * 默认重定向所有302和307
 * Created by Administrator on 2016/11/23.
 */
public class AllowAllRedirectStrategy extends DefaultRedirectStrategy {
    @Override
    protected boolean isRedirectable(String method) {
        return true;
    }
}

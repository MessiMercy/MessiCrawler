package com.proxy.validate;

import com.proxy.IPModel;

/**
 * Created by Administrator on 2016/12/8.
 */
public class MultiValidate implements Runnable {

    private IPModel model;
    private IPValidater validater;

    public MultiValidate(IPModel model, IPValidater validater) {
        this.model = model;
        this.validater = validater;
    }

    @Override
    public void run() {
        if (!validater.validate(model)) {
            model.setCreatedTime(0);
        }
    }
}

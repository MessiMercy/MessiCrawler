package com.model.inter;

import com.model.ShiXinModel;

/**
 * Created by Administrator on 2016/11/10.
 */
public interface IShiXinModelOperation {
    ShiXinModel selectByID(int id);

    void addShiXin(ShiXinModel model);

    void deleteShiXin(int id);

    ShiXinModel selectByName(String name);
}

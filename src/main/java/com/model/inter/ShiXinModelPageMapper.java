package com.model.inter;

import com.model.ShiXinModel;
import com.model.ShiXinModelPage;

import java.util.List;

/**
 * Created by Administrator on 2016/11/15.
 */
public interface ShiXinModelPageMapper {
    List<ShiXinModel> selectShiXinModelPageById(ShiXinModelPage page);

    List<ShiXinModel> selectShiXinModelPageByOffset(ShiXinModelPage page);

    int getCount();
}

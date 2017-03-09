package com.model.inter;

import com.model.Shixincompanydetail;

public interface ShixincompanydetailMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shixincompanydetail record);

    int insertSelective(Shixincompanydetail record);

    Shixincompanydetail selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shixincompanydetail record);

    int updateByPrimaryKey(Shixincompanydetail record);
}
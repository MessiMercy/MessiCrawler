package com.model.inter;

import com.model.Shixinpersondetail;

import java.util.List;

public interface ShixinpersondetailMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shixinpersondetail record);

    int insertSelective(Shixinpersondetail record);

    Shixinpersondetail selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shixinpersondetail record);

    int updateByPrimaryKey(Shixinpersondetail record);

    List<Shixinpersondetail> selectShiXin(String name);
}
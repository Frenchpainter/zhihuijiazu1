package com.zhihui.zhihuijiazu.Dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CurriculumCategoryMapper {

    Map<String,Object> getSpecialTraining(@Param("id") Integer userId);
}

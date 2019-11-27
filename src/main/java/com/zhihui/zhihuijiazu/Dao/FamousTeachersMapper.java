package com.zhihui.zhihuijiazu.Dao;

import com.zhihui.zhihuijiazu.Entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FamousTeachersMapper {

    List<User> findTeacherById(@Param("id") Integer teacherId);
}

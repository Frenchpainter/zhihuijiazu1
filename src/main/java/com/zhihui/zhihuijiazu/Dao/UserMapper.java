package com.zhihui.zhihuijiazu.Dao;

import com.zhihui.zhihuijiazu.Entity.User;
import com.zhihui.zhihuijiazu.Entity.UserLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface UserMapper {

    List<User> userLogin();

    void insert(User user);

    List<User> findUserByName(@Param("username") String username);

    List<User> findUserById(@Param("id") Integer userId);

    void updatePassword(User user);

    void update(User user);

    void insertUserLog(UserLog userLog);

    List<Map<String,Object>> findTest();
}

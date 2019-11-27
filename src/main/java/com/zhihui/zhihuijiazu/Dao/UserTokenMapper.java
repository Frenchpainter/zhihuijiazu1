package com.zhihui.zhihuijiazu.Dao;

import com.zhihui.zhihuijiazu.Entity.User;
import com.zhihui.zhihuijiazu.Entity.UserLog;
import com.zhihui.zhihuijiazu.Entity.UserToken;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserTokenMapper {

    List<UserToken> findByUserId(@Param("userId") Integer userId);

    void insertToken(UserToken userToken);

    void delete(@Param("userId") Integer userId);

}

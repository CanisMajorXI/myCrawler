package com.zqw.mapper;

import com.zqw.pojo.UserInfo;

import java.util.List;

public interface UserInfomationMapper {

    void insertAnUserInfo(UserInfo userInfo);

    List<UserInfo> selectBySex(String sex);

    List<UserInfo> selectAll();
}

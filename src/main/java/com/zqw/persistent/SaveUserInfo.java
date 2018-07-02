package com.zqw.persistent;

import com.zqw.mapper.UserInfomationMapper;
import com.zqw.pojo.UserInfo;
import com.zqw.util.SqlSessionFactoryUtils;
import org.apache.ibatis.session.SqlSession;

public class SaveUserInfo {

    public static void save(UserInfo userInfo) {
        SqlSession sqlSession = SqlSessionFactoryUtils.openSqlSession();
        UserInfomationMapper userInfomationMapper = sqlSession.getMapper(UserInfomationMapper.class);
        try {
            userInfomationMapper.insertAnUserInfo(userInfo);
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSession.rollback();
        }
    }
}

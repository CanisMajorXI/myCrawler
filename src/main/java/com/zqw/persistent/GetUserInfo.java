package com.zqw.persistent;

import com.zqw.mapper.UserInfomationMapper;
import com.zqw.pojo.UserInfo;
import com.zqw.util.SqlSessionFactoryUtils;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.List;

public class GetUserInfo {
    public static final int MALE = 0;
    public static final int FEMALE = 1;
    public static final int SECRET = 2;

    public static List<UserInfo> getUserInfoBySex(int type) {
        String sex = "";
        switch (type) {
            case MALE:
                sex = "男";
                break;
            case FEMALE:
                sex = "女";
                break;
            case SECRET:
                sex = "保密";
                break;
        }
        SqlSession sqlSession = SqlSessionFactoryUtils.openSqlSession();
        UserInfomationMapper userInfomationMapper = sqlSession.getMapper(UserInfomationMapper.class);
        try {
            return userInfomationMapper.selectBySex(sex);
        } catch (Exception e) {
            sqlSession.rollback();
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}

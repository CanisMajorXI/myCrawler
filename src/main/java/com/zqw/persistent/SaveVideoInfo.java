package com.zqw.persistent;

import com.zqw.mapper.VideoMapper;
import com.zqw.pojo.Video;
import com.zqw.util.SqlSessionFactoryUtils;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class SaveVideoInfo {
    public static void save(List<Video>videos) {
        SqlSession sqlSession  = SqlSessionFactoryUtils.openSqlSession();
        VideoMapper videoMapper = sqlSession.getMapper(VideoMapper.class);
        try {
            for(Video video : videos) {
                videoMapper.insert(video);
            }
            sqlSession.commit();
        }catch (Exception e){
            e.printStackTrace();
            sqlSession.rollback();
        }
    }
}

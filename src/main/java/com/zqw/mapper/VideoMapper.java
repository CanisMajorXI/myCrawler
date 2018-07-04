package com.zqw.mapper;

import com.zqw.pojo.Video;
import java.util.List;

public interface VideoMapper {
    int deleteByPrimaryKey(Integer aid);

    int insert(Video record);

    Video selectByPrimaryKey(Integer aid);

    List<Video> selectAll();

    int updateByPrimaryKey(Video record);
}
package com.teriteri.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teriteri.backend.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
//    @Select("SELECT * FROM comment WHERE root_id = #{rootId} AND vid = #{vid}")
//    List<Comment> getChildCommentsByRootId(@Param("rootId") Integer rootId, @Param("vid") Integer vid);

    @Select("SELECT * FROM comment WHERE vid = #{vid} AND root_id = 0")
    List<Comment> getRootCommentsByVid(@Param("vid") Integer vid);

}

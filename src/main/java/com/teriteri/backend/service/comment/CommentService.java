package com.teriteri.backend.service.comment;

import com.teriteri.backend.pojo.Comment;
import com.teriteri.backend.pojo.CommentTree;
import com.teriteri.backend.pojo.CustomResponse;

import java.util.List;

public interface CommentService {
    List<CommentTree> getCommentTreeByVid(Integer vid, Long offset, Integer type);

    boolean sendComment(Integer vid, Integer uid, Integer rootId, Integer parentId, Integer toUserId, String content);

    CustomResponse deleteComment(Integer id, Integer uid);

    CustomResponse updateComment(Integer id, Integer uid, String content);

    List<Comment> getChildCommentsByRootId(Integer rootId, Integer vid, Long start, Long stop);

    List<Comment> getRootCommentsByVid(Integer vid, Long offset, Integer type);

    CommentTree getMoreCommentsById(Integer id);
}

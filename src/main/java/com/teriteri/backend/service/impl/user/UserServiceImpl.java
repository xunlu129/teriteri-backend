package com.teriteri.backend.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.teriteri.backend.mapper.UserMapper;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.User;
import com.teriteri.backend.pojo.VideoStats;
import com.teriteri.backend.pojo.dto.UserDTO;
import com.teriteri.backend.service.user.UserService;
import com.teriteri.backend.service.video.VideoStatsService;
import com.teriteri.backend.utils.ESUtil;
import com.teriteri.backend.utils.OssUtil;
import com.teriteri.backend.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VideoStatsService videoStatsService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ESUtil esUtil;

    @Autowired
    private OssUtil ossUtil;

    @Value("${oss.bucketUrl}")
    private String OSS_BUCKET_URL;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 根据uid查询用户信息
     * @param id 用户ID
     * @return 用户可见信息实体类 UserDTO
     */
    @Override
    public UserDTO getUserById(Integer id) {
        // 从redis中获取最新数据
        User user = redisUtil.getObject("user:" + id, User.class);
        // 如果redis中没有user数据，就从mysql中获取并更新到redis
        if (user == null) {
            user = userMapper.selectById(id);
            if (user == null) {
                return null;    // 如果uid不存在则返回空
            }
            User finalUser = user;
            CompletableFuture.runAsync(() -> {
                redisUtil.setExObjectValue("user:" + finalUser.getUid(), finalUser);  // 默认存活1小时
            }, taskExecutor);
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUid(user.getUid());
        userDTO.setState(user.getState());
        if (user.getState() == 2) {
            userDTO.setNickname("账号已注销");
            userDTO.setAvatar_url("https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png");
            userDTO.setBg_url("https://tinypic.host/images/2023/11/15/69PB2Q5W9D2U7L.png");
            userDTO.setGender(2);
            userDTO.setDescription("-");
            userDTO.setExp(0);
            userDTO.setCoin((double) 0);
            userDTO.setVip(0);
            userDTO.setAuth(0);
            userDTO.setVideoCount(0);
            userDTO.setFollowsCount(0);
            userDTO.setFansCount(0);
            userDTO.setLoveCount(0);
            userDTO.setPlayCount(0);
            return userDTO;
        }
        userDTO.setNickname(user.getNickname());
        userDTO.setAvatar_url(user.getAvatar());
        userDTO.setBg_url(user.getBackground());
        userDTO.setGender(user.getGender());
        userDTO.setDescription(user.getDescription());
        userDTO.setExp(user.getExp());
        userDTO.setCoin(user.getCoin());
        userDTO.setVip(user.getVip());
        userDTO.setAuth(user.getAuth());
        userDTO.setAuthMsg(user.getAuthMsg());
        userDTO.setFollowsCount(0);
        userDTO.setFansCount(0);
        Set<Object> set = redisUtil.zReverange("user_video_upload:" + user.getUid(), 0L, -1L);
        if (set == null || set.size() == 0) {
            userDTO.setVideoCount(0);
            userDTO.setLoveCount(0);
            userDTO.setPlayCount(0);
            return userDTO;
        }

        // 并发执行每个视频数据统计的查询任务
        List<VideoStats> list = set.stream().parallel()
                .map(vid -> videoStatsService.getVideoStatsById((Integer) vid))
                .collect(Collectors.toList());

        int video = list.size(), love = 0, play = 0;
        for (VideoStats videoStats : list) {
            love = love + videoStats.getGood();
            play = play + videoStats.getPlay();
        }
        userDTO.setVideoCount(video);
        userDTO.setLoveCount(love);
        userDTO.setPlayCount(play);
        return userDTO;
    }

    @Override
    public List<UserDTO> getUserByIdList(List<Integer> list) {
        if (list.isEmpty()) return Collections.emptyList();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("uid", list).ne("state", 2);
        List<User> users = userMapper.selectList(queryWrapper);
        if (users.isEmpty()) return Collections.emptyList();
        return list.stream().parallel().flatMap(
                uid -> {
                    User user = users.stream()
                            .filter(u -> Objects.equals(u.getUid(), uid))
                            .findFirst()
                            .orElse(null);
                    if (user == null) return Stream.empty();
                    UserDTO userDTO = new UserDTO(
                            user.getUid(),
                            user.getNickname(),
                            user.getAvatar(),
                            user.getBackground(),
                            user.getGender(),
                            user.getDescription(),
                            user.getExp(),
                            user.getCoin(),
                            user.getVip(),
                            user.getState(),
                            user.getAuth(),
                            user.getAuthMsg(),
                            0,0,0,0,0
                    );
                    Set<Object> set = redisUtil.zReverange("user_video_upload:" + user.getUid(), 0L, -1L);
                    if (set == null || set.size() == 0) {
                        return Stream.of(userDTO);
                    }

                    // 并发执行每个视频数据统计的查询任务
                    List<VideoStats> videoStatsList = set.stream().parallel()
                            .map(vid -> videoStatsService.getVideoStatsById((Integer) vid))
                            .collect(Collectors.toList());

                    int video = videoStatsList.size(), love = 0, play = 0;
                    for (VideoStats videoStats : videoStatsList) {
                        love = love + videoStats.getGood();
                        play = play + videoStats.getPlay();
                    }
                    userDTO.setVideoCount(video);
                    userDTO.setLoveCount(love);
                    userDTO.setPlayCount(play);
                    return Stream.of(userDTO);
                }
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomResponse updateUserInfo(Integer uid, String nickname, String desc, Integer gender) throws IOException {
        CustomResponse customResponse = new CustomResponse();
        if (nickname == null || nickname.trim().length() == 0) {
            customResponse.setCode(500);
            customResponse.setMessage("昵称不能为空");
            return customResponse;
        }
        if (nickname.length() > 24 || desc.length() > 100) {
            customResponse.setCode(500);
            customResponse.setMessage("输入字符过长");
            return customResponse;
        }
        if (Objects.equals(nickname, "账号已注销")) {
            customResponse.setCode(500);
            customResponse.setMessage("昵称非法");
            return customResponse;
        }
        // 查重
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("nickname", nickname).ne("uid", uid);
        User user = userMapper.selectOne(queryWrapper);
        if (user != null) {
            customResponse.setCode(500);
            customResponse.setMessage("该昵称已被其他用户占用");
            return customResponse;
        }
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid)
                .set("nickname", nickname)
                .set("description", desc)
                .set("gender", gender);
        userMapper.update(null, updateWrapper);
        User new_user = new User();
        new_user.setUid(uid);
        new_user.setNickname(nickname);
        esUtil.updateUser(new_user);
        redisUtil.delValue("user:" + uid);
        return customResponse;
    }

    @Override
    public CustomResponse updateUserAvatar(Integer uid, MultipartFile file) throws IOException {
        // 保存封面到OSS，返回URL
        String avatar_url = ossUtil.uploadImage(file, "avatar");
        // 查旧的头像地址
        User user = userMapper.selectById(uid);
        // 先更新数据库
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid).set("avatar", avatar_url);
        userMapper.update(null, updateWrapper);
        CompletableFuture.runAsync(() -> {
            redisUtil.delValue("user:" + uid);  // 删除redis缓存
            // 如果就头像不是初始头像就去删除OSS的源文件
            if (user.getAvatar().startsWith(OSS_BUCKET_URL)) {
                String filename = user.getAvatar().substring(OSS_BUCKET_URL.length());
//                System.out.println("要删除的源文件：" + filename);
                ossUtil.deleteFiles(filename);
            }
        }, taskExecutor);
        return new CustomResponse(200, "OK", avatar_url);
    }
}

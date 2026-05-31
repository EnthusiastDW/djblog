package site.dengwei.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.dengwei.blog.entity.PostPlatformSync;
import site.dengwei.blog.mapper.PostPlatformSyncMapper;
import site.dengwei.blog.service.PostPlatformSyncService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostPlatformSyncServiceImpl implements PostPlatformSyncService {

    private final PostPlatformSyncMapper postPlatformSyncMapper;

    @Override
    public List<PostPlatformSync> getByPostId(Long postId) {
        return postPlatformSyncMapper.selectList(
                new LambdaQueryWrapper<PostPlatformSync>()
                        .eq(PostPlatformSync::getPostId, postId)
                        .orderByAsc(PostPlatformSync::getPlatformCode)
        );
    }

}

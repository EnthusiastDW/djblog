package site.dengwei.blog.service;

import site.dengwei.blog.entity.PostPlatformSync;

import java.util.List;

public interface PostPlatformSyncService {
    List<PostPlatformSync> getByPostId(Long postId);
}

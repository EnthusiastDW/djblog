package site.dengwei.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import site.dengwei.blog.dto.PostDetailDTO;
import site.dengwei.blog.dto.request.*;
import site.dengwei.blog.entity.Post;
import site.dengwei.blog.enums.PostStatus;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.mapper.CategoryMapper;
import site.dengwei.blog.mapper.PostMapper;
import site.dengwei.blog.mapper.TagMapper;
import site.dengwei.blog.mapper.UserMapper;
import site.dengwei.blog.service.PostService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static site.dengwei.utils.SlugUtils.generateSlug;

/**
 * 文章服务实现类
 *
 * @author dengwei
 * @since 2025-09-08 11:56:27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    @Override
    public Post getByIdOrThrow(Long id) {
        Post post = getById(id);
        if (post == null) {
            throw new BusinessException("文章不存在");
        }
        return post;
    }

    @Override
    public PostDetailDTO getPostDetailOrThrow(Long id) {
        PostDetailDTO dto = getPostDetail(id);
        if (dto == null) {
            throw new BusinessException("文章不存在");
        }
        return dto;
    }

    @Override
    public PostDetailDTO getPostDetail(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            return null;
        }

        PostDetailDTO dto = new PostDetailDTO();
        BeanUtils.copyProperties(post, dto);
        dto.setAuthor(userMapper.selectById(post.getAuthorId()));
        dto.setCategory(categoryMapper.selectById(post.getCategoryId()));
        dto.setTags(postMapper.selectTagsByPostId(postId));
        return dto;
    }

    @Override
    public Page<Post> searchPosts(String keyword, Page<Post> page) {
        return postMapper.searchPosts(keyword, page);
    }

    @Override
    public Page<Post> selectDrafts(Page<Post> page) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, PostStatus.DRAFT);
        return page(page, wrapper);
    }

    @Override
    public List<Map<String, Object>> getPostArchives() {
        return postMapper.selectPostArchives();
    }

    @Override
    public Page<Post> getPostsByArchive(String yearMonth, Page<Post> page) {
        return postMapper.selectPostsByArchive(yearMonth, page);
    }

    @Override
    public boolean saveDraft(SaveDraftRequest request) {
        Post post = buildPostFromRequest(request, PostStatus.DRAFT);
        log.info("保存草稿，标题: {}", request.getTitle());
        return saveOrUpdate(post);
    }

    @Override
    public boolean publish(PublishRequest request) {
        Post post = buildPostFromPublishRequest(request, PostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());
        log.info("发布文章，标题: {}", request.getTitle());
        return saveOrUpdate(post);
    }

    @Override
    public boolean createPost(CreatePostRequest request) {
        Post post = buildPostFromCreateRequest(request);
        log.info("创建文章，标题: {}", request.getTitle());
        return save(post);
    }

    @Override
    public boolean updatePost(UpdatePostRequest request) {
        Post post = getByIdOrThrow(request.getId());
        updatePostFromRequest(post, request);
        log.info("更新文章，ID: {}", request.getId());
        return updateById(post);
    }

    @Override
    public boolean deletePosts(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            throw new BusinessException("请选择要删除的文章");
        }
        log.info("删除文章，ID列表: {}", idList);
        return removeByIds(idList);
    }

    private Post buildPostFromRequest(SaveDraftRequest request, PostStatus status) {
        Post post = new Post();
        if (request.getId() != null) {
            post = getById(request.getId());
            if (post == null) {
                throw new BusinessException("文章不存在");
            }
        }
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSlug(processSlug(request.getTitle(), request.getSlug()));
        post.setCoverImage(request.getCoverImage());
        post.setCategoryId(request.getCategoryId());
        post.setStatus(status);
        return post;
    }

    private Post buildPostFromPublishRequest(PublishRequest request, PostStatus status) {
        Post post = new Post();
        if (request.getId() != null) {
            post = getById(request.getId());
            if (post == null) {
                throw new BusinessException("文章不存在");
            }
        }
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSlug(processSlug(request.getTitle(), request.getSlug()));
        post.setCoverImage(request.getCoverImage());
        post.setCategoryId(request.getCategoryId());
        post.setStatus(status);
        return post;
    }

    private Post buildPostFromCreateRequest(CreatePostRequest request) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSlug(processSlug(request.getTitle(), request.getSlug()));
        post.setCoverImage(request.getCoverImage());
        post.setCategoryId(request.getCategoryId());
        return post;
    }

    private void updatePostFromRequest(Post post, UpdatePostRequest request) {
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSlug(processSlug(request.getTitle(), request.getSlug()));
        post.setCoverImage(request.getCoverImage());
        post.setCategoryId(request.getCategoryId());
    }

    private String processSlug(String title, String slug) {
        if (StringUtils.hasText(slug)) {
            return slug;
        }
        return generateSlug(title);
    }
}

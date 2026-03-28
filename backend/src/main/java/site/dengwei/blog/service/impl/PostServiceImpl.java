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
import site.dengwei.blog.dto.PostListDTO;
import site.dengwei.blog.dto.request.*;
import site.dengwei.blog.entity.Category;
import site.dengwei.blog.entity.Post;
import site.dengwei.blog.entity.Tag;
import site.dengwei.blog.enums.PostStatus;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.mapper.CategoryMapper;
import site.dengwei.blog.mapper.PostMapper;
import site.dengwei.blog.mapper.TagMapper;
import site.dengwei.blog.mapper.UserMapper;
import site.dengwei.blog.service.AiSlugService;
import site.dengwei.blog.service.AiSummaryService;
import site.dengwei.blog.service.PostService;
import site.dengwei.blog.service.PostTagService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private final AiSlugService aiSlugService;
    private final PostTagService postTagService;
    private final AiSummaryService aiSummaryService;

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
    public PostDetailDTO getPostBySlugOrThrow(String slug) {
        // 通过 slug 查询文章
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getSlug, slug);
        Post post = getOne(wrapper);
        
        if (post == null) {
            throw new BusinessException("文章不存在");
        }
        
        return getPostDetail(post.getId());
    }

    @Override
    public PostDetailDTO getPostDetail(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            return null;
        }

        PostDetailDTO dto = new PostDetailDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setSlug(post.getSlug());
        dto.setCoverImage(post.getCoverImage());
        dto.setStatus(post.getStatus() != null ? post.getStatus().name() : null);
        dto.setAuthorId(post.getAuthorId());
        dto.setCategoryId(post.getCategoryId());
        dto.setViewCount(post.getViewCount());
        
        if (post.getPublishedAt() != null) {
            dto.setPublishedAt(java.util.Date.from(post.getPublishedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        }
        if (post.getCreatedAt() != null) {
            dto.setCreatedAt(java.util.Date.from(post.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        }
        if (post.getUpdatedAt() != null) {
            dto.setUpdatedAt(java.util.Date.from(post.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        }
        
        dto.setAuthor(userMapper.selectById(post.getAuthorId()));
        Category category = categoryMapper.selectById(post.getCategoryId());
        dto.setCategory(category);
        if (category != null) {
            dto.setCategoryName(category.getName());
        }
        dto.setTags(postMapper.selectTagsByPostId(postId));
        return dto;
    }

    @Override
    public Page<Post> searchPosts(String keyword, Page<Post> page) {
        return postMapper.searchPosts(keyword, page);
    }

    @Override
    public Page<Post> searchPostsAdvanced(String keyword, String title, String summary, Long categoryId, Long tagId, Long authorId, Page<Post> page, PostStatus status) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(Post::getStatus, status);
        }
        
        // 关键词搜索（标题、内容、摘要）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(Post::getTitle, keyword.trim())
                .or()
                .like(Post::getContent, keyword.trim())
                .or()
                .like(Post::getSummary, keyword.trim())
            );
        }
        
        // 标题搜索
        if (StringUtils.hasText(title)) {
            wrapper.like(Post::getTitle, title.trim());
        }
        
        // 摘要搜索
        if (StringUtils.hasText(summary)) {
            wrapper.like(Post::getSummary, summary.trim());
        }
        
        // 分类搜索
        if (categoryId != null) {
            wrapper.eq(Post::getCategoryId, categoryId);
        }
        
        // 标签搜索（需要关联查询）
        if (tagId != null) {
            List<Long> postIds = postMapper.selectPostIdsByTagId(tagId);
            if (postIds.isEmpty()) {
                return new Page<>(page.getCurrent(), page.getSize(), 0);
            }
            wrapper.in(Post::getId, postIds);
        }
        
        // 作者搜索
        if (authorId != null) {
            wrapper.eq(Post::getAuthorId, authorId);
        }
        
        wrapper.orderByDesc(Post::getPublishedAt);
        return page(page, wrapper);
    }

    @Override
    public Page<PostListDTO> getPostListWithRelations(Page<Post> page, Post queryParam) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, PostStatus.PUBLISHED);
        
        if (queryParam != null) {
            if (queryParam.getStatus() != null) {
                wrapper.eq(Post::getStatus, queryParam.getStatus());
            }
            if (queryParam.getCategoryId() != null) {
                wrapper.eq(Post::getCategoryId, queryParam.getCategoryId());
            }
            if (StringUtils.hasText(queryParam.getTitle())) {
                wrapper.like(Post::getTitle, queryParam.getTitle());
            }
        }
        wrapper.orderByDesc(Post::getCreatedAt);
        
        Page<Post> postPage = page(page, wrapper);
        
        // 转换为 DTO 并填充分类和标签信息
        List<PostListDTO> dtoList = postPage.getRecords().stream()
                .map(this::convertToDTO)
                .toList();
        
        Page<PostListDTO> dtoPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    @Override
    public Page<PostListDTO> getAllPostListWithRelations(Page<Post> page, Post queryParam) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        
        if (queryParam != null) {
            if (queryParam.getStatus() != null) {
                wrapper.eq(Post::getStatus, queryParam.getStatus());
            }
            if (queryParam.getCategoryId() != null) {
                wrapper.eq(Post::getCategoryId, queryParam.getCategoryId());
            }
            if (StringUtils.hasText(queryParam.getTitle())) {
                wrapper.like(Post::getTitle, queryParam.getTitle());
            }
        }
        wrapper.orderByDesc(Post::getCreatedAt);
        
        Page<Post> postPage = page(page, wrapper);
        
        List<PostListDTO> dtoList = postPage.getRecords().stream()
                .map(this::convertToDTO)
                .toList();
        
        Page<PostListDTO> dtoPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    /**
     * 转换为 DTO 并填充分类和标签信息
     */
    private PostListDTO convertToDTO(Post post) {
        PostListDTO dto = new PostListDTO();
        BeanUtils.copyProperties(post, dto);
        
        // 填充分类信息
        if (post.getCategoryId() != null) {
            Category category = categoryMapper.selectById(post.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getName());
            }
        }
        
        // 填充分类标签
        List<Tag> tags = postMapper.selectTagsByPostId(post.getId());
        dto.setTags(tags);
        
        // 设置浏览次数
        dto.setViewCount(post.getViewCount());
        
        return dto;
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
    public List<Map<String, Object>> getPostArchivesByYear(Integer size) {
        List<Map<String, Object>> allArchives = postMapper.selectPostArchives();
        
        Map<String, List<Map<String, Object>>> yearMap = new LinkedHashMap<>();
        for (Map<String, Object> archive : allArchives) {
            String yearMonth = (String) archive.get("archiveMonth");
            if (yearMonth != null && yearMonth.length() >= 4) {
                String year = yearMonth.substring(0, 4);
                yearMap.computeIfAbsent(year, k -> new ArrayList<>()).add(archive);
            }
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : yearMap.entrySet()) {
            Map<String, Object> yearGroup = new LinkedHashMap<>();
            yearGroup.put("year", entry.getKey());
            yearGroup.put("months", entry.getValue().subList(0, Math.min(entry.getValue().size(), size)));
            yearGroup.put("totalCount", entry.getValue().size());
            result.add(yearGroup);
        }
        
        return result;
    }

    @Override
    public Page<Post> getPostsByArchive(String yearMonth, Page<Post> page) {
        return postMapper.selectPostsByArchive(yearMonth, page);
    }

    @Override
    public boolean saveDraft(SaveDraftRequest request) {
        Post post = buildPostFromRequest(request, PostStatus.DRAFT);
        log.info("保存草稿，标题: {}", request.getTitle());
        boolean result = saveOrUpdate(post);
        if (result && request.getTagIds() != null && request.getTagIds().length > 0) {
            postTagService.setPostTags(post.getId(), java.util.Arrays.asList(request.getTagIds()));
        }
        return result;
    }

    @Override
    public boolean publish(PublishRequest request) {
        Post post = buildPostFromPublishRequest(request, PostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());
        log.info("发布文章，标题: {}", request.getTitle());
        boolean result = saveOrUpdate(post);
        if (result && request.getTagIds() != null && request.getTagIds().length > 0) {
            postTagService.setPostTags(post.getId(), java.util.Arrays.asList(request.getTagIds()));
        }
        return result;
    }

    @Override
    public boolean createPost(CreatePostRequest request) {
        Post post = buildPostFromCreateRequest(request);
        log.info("创建文章，标题: {}", request.getTitle());
        boolean result = save(post);
        if (result && request.getTagIds() != null && request.getTagIds().length > 0) {
            postTagService.setPostTags(post.getId(), java.util.Arrays.asList(request.getTagIds()));
        }
        return result;
    }

    @Override
    public boolean updatePost(UpdatePostRequest request) {
        Post post = getByIdOrThrow(request.getId());
        updatePostFromRequest(post, request);
        log.info("更新文章，ID: {}", request.getId());
        boolean result = updateById(post);
        if (result && request.getTagIds() != null) {
            postTagService.setPostTags(post.getId(), java.util.Arrays.asList(request.getTagIds()));
        }
        return result;
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
        post.setSlug(processSlug(request.getTitle(), request.getSlug(), request.getId()));
        post.setSummary(request.getSummary());
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
        post.setSlug(processSlug(request.getTitle(), request.getSlug(), request.getId()));
        post.setSummary(request.getSummary());
        post.setCoverImage(request.getCoverImage());
        post.setCategoryId(request.getCategoryId());
        post.setStatus(status);
        return post;
    }

    private Post buildPostFromCreateRequest(CreatePostRequest request) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSlug(processSlug(request.getTitle(), request.getSlug(), null));
        post.setCoverImage(request.getCoverImage());
        post.setCategoryId(request.getCategoryId());
        return post;
    }

    private void updatePostFromRequest(Post post, UpdatePostRequest request) {
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSlug(processSlug(request.getTitle(), request.getSlug(), post.getId()));
        post.setCoverImage(request.getCoverImage());
        post.setCategoryId(request.getCategoryId());
        post.setSummary(request.getSummary());
    }

    /**
     * 使用 AI 自动生成 slug（带重复检测）
     */
    private String processSlug(String title, String slug, Long currentId) {
        // 忽略前端传入的 slug，始终使用 AI 生成
        String baseSlug = aiSlugService.generateSlug(title);
        
        // 检查 slug 是否重复
        return ensureUniqueSlug(baseSlug, currentId);
    }
    
    /**
     * 确保 slug 唯一性，如果重复则添加 ID 后缀
     * @param baseSlug 基础 slug
     * @param currentId 当前文章 ID（更新时使用）
     * @return 唯一的 slug
     */
    private String ensureUniqueSlug(String baseSlug, Long currentId) {
        // 查询是否存在相同的 slug
        Post existingPost = getOne(new LambdaQueryWrapper<Post>()
                .eq(Post::getSlug, baseSlug)
                .ne(currentId != null, Post::getId, currentId));

        if (existingPost == null) {
            // slug 不重复，可以使用
            log.debug("slug 可用：{}", baseSlug);
            return baseSlug;
        }
        // slug 重复，添加后缀
        return baseSlug + "-" + currentId;
    }

    @Override
    public String generateSummary(String title, String content, int maxLength) {
        log.info("AI 生成摘要，标题：{}", title);
        return aiSummaryService.generateSummary(title, content, maxLength);
    }

    @Override
    public Long getTotalViewCount() {
        return postMapper.selectTotalViewCount();
    }

    @Override
    public boolean incrementViewCount(Long id) {
        Post post = getById(id);
        if (post == null) {
            return false;
        }
        Long currentCount = post.getViewCount();
        if (currentCount == null) {
            currentCount = 0L;
        }
        post.setViewCount(currentCount + 1);
        return updateById(post);
    }
}

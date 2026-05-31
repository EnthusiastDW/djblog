package site.dengwei.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import site.dengwei.blog.service.platform.PlatformSyncOrchestrator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 文章服务实现类
 *
 * @author dengwei
 * @since 2025-09-08 11:56:27
 */
@CacheConfig(cacheNames = "post")
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
    private final PlatformSyncOrchestrator platformSyncOrchestrator;

    @Cacheable(key = "#id")
    @Override
    public Post getByIdOrThrow(Long id) {
        Post post = getById(id);
        if (post == null) {
            throw new BusinessException("文章不存在");
        }
        return post;
    }

    @Cacheable(key = "'detail:' + #id")
    @Override
    public PostDetailDTO getPostDetailOrThrow(Long id) {
        PostDetailDTO dto = getPostDetail(id);
        if (dto == null) {
            throw new BusinessException("文章不存在");
        }
        return dto;
    }

    @Cacheable(key = "'slug:' + #slug")
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
        dto.setSummary(post.getSummary());
        dto.setContent(post.getContent());
        dto.setSlug(post.getSlug());
        dto.setCoverImage(post.getCoverImage());
        dto.setStatus(post.getStatus() != null ? post.getStatus().name() : null);
        dto.setAuthorId(post.getAuthorId());
        dto.setCategoryId(post.getCategoryId());
        // 直接从 post 表读取浏览量（已定期同步）
        dto.setViewCount(post.getViewCount() != null ? post.getViewCount() : 0L);
        
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
    public Page<PostListDTO> searchPostsAdvanced(String keyword, String title, String summary, Long categoryId, Long tagId, Long authorId, Page<Post> page, PostStatus status) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        
        // 默认只查询已发布的文章，除非明确指定其他状态
        wrapper.eq(Post::getStatus, Objects.requireNonNullElse(status, PostStatus.PUBLISHED));
        
        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            wrapper.and(w -> w
                .like(Post::getTitle, trimmedKeyword)
                .or()
                .like(Post::getContent, trimmedKeyword)
                .or()
                .like(Post::getSummary, trimmedKeyword)
            );
        }
        
        if (StringUtils.hasText(title)) {
            wrapper.like(Post::getTitle, title.trim());
        }
        
        if (StringUtils.hasText(summary)) {
            wrapper.like(Post::getSummary, summary.trim());
        }
        
        if (categoryId != null) {
            wrapper.eq(Post::getCategoryId, categoryId);
        }
        
        if (tagId != null) {
            List<Long> postIds = postMapper.selectPostIdsByTagId(tagId);
            if (postIds.isEmpty()) {
                return new Page<>(page.getCurrent(), page.getSize(), 0);
            }
            wrapper.in(Post::getId, postIds);
        }
        
        if (authorId != null) {
            wrapper.eq(Post::getAuthorId, authorId);
        }
        
        wrapper.orderByDesc(Post::getPublishedAt);
        Page<Post> postPage = page(page, wrapper);
        
        // 转换为 DTO 并填充分类、标签和匹配内容
        List<PostListDTO> dtoList = postPage.getRecords().stream()
                .map(post -> {
                    PostListDTO dto = convertToDTO(post);
                    // 如果有关键词，提取匹配内容片段
                    if (StringUtils.hasText(keyword)) {
                        dto.setMatchedContent(extractMatchedContent(post, keyword.trim()));
                    }
                    return dto;
                })
                .toList();
        
        Page<PostListDTO> dtoPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public Page<PostListDTO> getPostListWithRelations(Page<Post> page, Post queryParam) {
        return getPostListWithRelationsInternal(page, queryParam, true);
    }

    @Override
    public Page<PostListDTO> getAllPostListWithRelations(Page<Post> page, Post queryParam) {
        return getPostListWithRelationsInternal(page, queryParam, false);
    }

    @Override
    public Page<PostListDTO> getPostListWithRelationsByCategoryIds(Page<Post> page, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new Page<>(page.getCurrent(), page.getSize(), 0);
        }
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, PostStatus.PUBLISHED);
        wrapper.in(Post::getCategoryId, categoryIds);
        wrapper.orderByDesc(Post::getCreatedAt);

        return toPostDtoPage(page(page, wrapper));
    }

    private @NonNull Page<PostListDTO> toPostDtoPage(Page<Post> postPage) {
        List<PostListDTO> dtoList = postPage.getRecords().stream()
                .map(this::convertToDTO)
                .toList();

        Page<PostListDTO> dtoPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    /**
     * 获取文章列表（内部方法）
     * @param page 分页参数
     * @param queryParam 查询条件
     * @param filterPublished 是否只查询已发布的文章
     */
    private Page<PostListDTO> getPostListWithRelationsInternal(Page<Post> page, Post queryParam, boolean filterPublished) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (filterPublished) {
            wrapper.eq(Post::getStatus, PostStatus.PUBLISHED);
        } else {
            // 管理后台不显示已删除的文章
            wrapper.ne(Post::getStatus, PostStatus.DELETED);
        }
        
        if (queryParam != null) {
            wrapper.eq(queryParam.getStatus() != null, Post::getStatus, queryParam.getStatus());
            wrapper.eq(queryParam.getCategoryId() != null, Post::getCategoryId, queryParam.getCategoryId());
            wrapper.like(StringUtils.hasText(queryParam.getTitle()), Post::getTitle, queryParam.getTitle());
        }
        wrapper.orderByDesc(Post::getCreatedAt);

        return toPostDtoPage(page(page, wrapper));
    }
    
    /**
     * 转换为 DTO 并填充分类和标签信息
     */
    private PostListDTO convertToDTO(Post post) {
        PostListDTO dto = new PostListDTO();
        BeanUtils.copyProperties(post, dto);
        
        if (post.getAuthorId() != null) {
            var user = userMapper.selectById(post.getAuthorId());
            if (user != null) {
                dto.setAuthorName(user.getUsername());
            }
        }
        
        if (post.getCategoryId() != null) {
            Category category = categoryMapper.selectById(post.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getName());
            }
        }
        
        List<Tag> tags = postMapper.selectTagsByPostId(post.getId());
        dto.setTags(tags);
        
        // 直接从 post 表读取浏览量（已定期同步）
        dto.setViewCount(post.getViewCount() != null ? post.getViewCount() : 0L);
        
        return dto;
    }

    @Override
    public Page<Post> selectDrafts(Page<Post> page) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, PostStatus.DRAFT);
        return page(page, wrapper);
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
    public Page<PostListDTO> getPostsByArchive(String yearMonth, Page<Post> page) {
        return toPostDtoPage(postMapper.selectPostsByArchive(yearMonth, page));
    }

    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "post", allEntries = true)
    })
    @Override
    public Long saveDraft(SaveDraftRequest request) {
        Post post = buildPostFromDraftOrPublishRequest(request, PostStatus.DRAFT);
        log.info("保存草稿，标题: {}", request.getTitle());
        boolean result = saveOrUpdate(post);
        if (result && request.getTagIds() != null && request.getTagIds().length > 0) {
            postTagService.setPostTags(post.getId(), java.util.Arrays.asList(request.getTagIds()));
        }
        return result ? post.getId() : null;
    }

    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "post", allEntries = true)
    })
    @Override
    public Long publish(PublishRequest request) {
        Post post = buildPostFromDraftOrPublishRequest(request, PostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());
        log.info("发布文章，标题: {}", request.getTitle());
        boolean result = saveOrUpdate(post);
        if (result && request.getTagIds() != null && request.getTagIds().length > 0) {
            postTagService.setPostTags(post.getId(), java.util.Arrays.asList(request.getTagIds()));
        }
        // 触发异步同步到指定平台
        if (result && request.getSyncPlatforms() != null && request.getSyncPlatforms().length > 0) {
            log.info("触发异步同步文章 {} 到平台: {}", post.getId(), Arrays.toString(request.getSyncPlatforms()));
            platformSyncOrchestrator.syncPost(post.getId(), request.getSyncPlatforms());
        }
        return result ? post.getId() : null;
    }

    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "post", allEntries = true)
    })
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

    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "post", allEntries = true)
    })
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

    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "post", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deletePosts(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            throw new BusinessException("请选择要删除的文章");
        }
        log.info("软删除文章，ID列表: {}", idList);
        List<Post> posts = listByIds(idList);
        for (Post post : posts) {
            post.setStatus(PostStatus.DELETED);
        }
        return updateBatchById(posts);
    }

    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "post", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean restorePosts(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            throw new BusinessException("请选择要恢复的文章");
        }
        log.info("恢复文章，ID列表: {}", idList);
        List<Post> posts = listByIds(idList);
        for (Post post : posts) {
            if (post.getStatus() == PostStatus.DELETED) {
                post.setStatus(PostStatus.DRAFT);
            }
        }
        return updateBatchById(posts);
    }

    @Override
    public Page<PostListDTO> getDeletedPosts(Page<Post> page, Post queryParam) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, PostStatus.DELETED);
        
        if (queryParam != null) {
            wrapper.like(StringUtils.hasText(queryParam.getTitle()), Post::getTitle, queryParam.getTitle());
        }
        wrapper.orderByDesc(Post::getUpdatedAt);

        return toPostDtoPage(page(page, wrapper));
    }

    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "post", allEntries = true)
    })
    @Override
    public boolean permanentDelete(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            throw new BusinessException("请选择要彻底删除的文章");
        }
        log.info("彻底删除文章，ID列表: {}", idList);
        return removeByIds(idList);
    }

    private Post buildPostFromDraftOrPublishRequest(BasePostRequest request, PostStatus status) {
        Post post = new Post();
        if (request.getId() != null) {
            post = getById(request.getId());
            if (post == null) {
                throw new BusinessException("文章不存在");
            }
        }
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        // 只有在首次发布时才生成 slug
        post.setSlug(processSlug(post, request.getTitle(), status));
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
        // 创建文章时不生成 slug，等发布时再生成
        post.setSlug(null);
        post.setCoverImage(request.getCoverImage());
        post.setCategoryId(request.getCategoryId());
        return post;
    }

    private void updatePostFromRequest(Post post, UpdatePostRequest request) {
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        // 更新文章时不重新生成 slug，保持原有 slug
        post.setCoverImage(request.getCoverImage());
        post.setCategoryId(request.getCategoryId());
        post.setSummary(request.getSummary());
    }

    /**
     * 处理 slug 生成逻辑
     * - 只有首次发布时才生成 slug
     * - 如果文章已有 slug，则保持不变
     * - 创建和草稿状态都不生成 slug
     */
    private String processSlug(Post existingPost, String title, PostStatus status) {
        // 如果是更新操作且文章已有 slug，直接返回原有 slug
        if (existingPost != null && StringUtils.hasText(existingPost.getSlug())) {
            log.debug("文章已有 slug，保持不变：{}", existingPost.getSlug());
            return existingPost.getSlug();
        }
        
        // 如果是草稿状态或创建新文章，不生成 slug
        if (status == PostStatus.DRAFT || existingPost == null) {
            log.debug("草稿或新建状态，暂不生成 slug");
            return null;
        }
        
        // 首次发布时生成 slug
        String baseSlug = aiSlugService.generateSlug(title);
        return ensureUniqueSlug(baseSlug, existingPost.getId());
    }
    
    /**
     * 确保 slug 唯一性
     * @param baseSlug 基础 slug
     * @param currentId 当前文章 ID（更新时使用，可为 null）
     * @return 唯一的 slug
     */
    private String ensureUniqueSlug(String baseSlug, Long currentId) {
        // 查询是否存在相同的 slug（排除当前文章）
        Post existingPost = getOne(new LambdaQueryWrapper<Post>()
                .eq(Post::getSlug, baseSlug)
                .ne(currentId != null, Post::getId, currentId));

        if (existingPost == null) {
            // slug 不重复，可以使用
            log.debug("slug 可用：{}", baseSlug);
            return baseSlug;
        }
        
        // slug 重复，添加计数器后缀
        int counter = 1;
        while (true) {
            String newSlug = baseSlug + "-" + counter;
            Post checkPost = getOne(new LambdaQueryWrapper<Post>()
                    .eq(Post::getSlug, newSlug)
                    .ne(currentId != null, Post::getId, currentId));
            if (checkPost == null) {
                log.debug("slug 重复，使用：{}", newSlug);
                return newSlug;
            }
            counter++;
        }
    }

    @Override
    public Long getTotalViewCount() {
        // 直接从数据库查询总浏览量（已定期同步到 post 表）
        return postMapper.selectTotalViewCount();
    }

    @Override
    public List<Post> getPopularPosts(int limit) {
        // 直接使用 SQL 查询，按 view_count 降序排序（已定期同步到 post 表）
        return postMapper.selectPopularPosts(limit);
    }

    @CacheEvict(allEntries = true)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importPosts(List<ImportPostRequest> posts, Long userId) {
        Map<String, Object> result = new LinkedHashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (ImportPostRequest postReq : posts) {
            try {
                // 1. 处理分类（自动创建）
                Long categoryId = getOrCreateCategory(postReq.getCategory());

                // 2. 处理标签（自动创建，最多 3 个）
                List<Long> tagIds = getOrCreateTags(postReq.getTags());

                // 3. 生成或处理 slug
                String slug = postReq.getSlug();
                if (slug == null || slug.isEmpty()) {
                    slug = aiSlugService.generateSlug(postReq.getTitle());
                }
                // 确保 slug 唯一
                slug = ensureUniqueSlug(slug, null);

                // 4. 解析日期
                LocalDateTime createdAt = parseDateTime(postReq.getCreatedAt());
                LocalDateTime updatedAt = parseDateTime(postReq.getUpdatedAt());
                LocalDateTime publishedAt = createdAt != null ? createdAt : LocalDateTime.now();

                // 5. 构建文章实体
                Post post = new Post();
                post.setTitle(postReq.getTitle());
                post.setSlug(slug);
                post.setContent(postReq.getContent());
                post.setSummary(postReq.getSummary() != null ? postReq.getSummary() : 
                    aiSummaryService.generateSummary(postReq.getTitle(), postReq.getContent(), 200));
                post.setCoverImage(postReq.getCoverImage());
                post.setStatus(PostStatus.PUBLISHED);
                post.setAuthorId(userId);
                post.setCategoryId(categoryId);
                post.setViewCount(0L);
                post.setPublishedAt(publishedAt);
                post.setCreatedAt(createdAt != null ? createdAt : LocalDateTime.now());
                post.setUpdatedAt(updatedAt != null ? updatedAt : LocalDateTime.now());

                // 6. 保存文章
                save(post);

                // 7. 保存标签关联
                if (!tagIds.isEmpty()) {
                    postTagService.setPostTags(post.getId(), tagIds);
                }

                successCount++;
                log.info("导入文章成功：{}", postReq.getTitle());

            } catch (Exception e) {
                failCount++;
                String errorMsg = String.format("导入失败 [%s]: %s", postReq.getTitle(), e.getMessage());
                errors.add(errorMsg);
                log.error(errorMsg, e);
            }
        }

        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);

        return result;
    }

    /**
     * 获取或创建分类
     */
    private Long getOrCreateCategory(String categoryName) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, categoryName);
        Category category = categoryMapper.selectOne(wrapper);

        if (category == null) {
            category = new Category();
            category.setName(categoryName);
            categoryMapper.insert(category);
            log.info("创建新分类：{}", categoryName);
        }

        return category.getId();
    }

    /**
     * 获取或创建标签列表
     */
    private List<Long> getOrCreateTags(List<String> tagNames) {
        List<Long> tagIds = new ArrayList<>();
        if (tagNames == null || tagNames.isEmpty()) {
            return tagIds;
        }

        for (String tagName : tagNames) {
            LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Tag::getName, tagName);
            Tag tag = tagMapper.selectOne(wrapper);

            if (tag == null) {
                tag = new Tag();
                tag.setName(tagName);
                tagMapper.insert(tag);
                log.info("创建新标签：{}", tagName);
            }

            tagIds.add(tag.getId());
        }

        return tagIds;
    }

    /**
     * 解析日期时间字符串
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            log.warn("日期解析失败：{}, 使用当前时间", dateTimeStr);
            return LocalDateTime.now();
        }
    }
    
    /**
     * 提取匹配内容片段
     * @param post 文章对象
     * @param keyword 搜索关键词
     * @return 匹配的内容片段，前后各保留30个字符（适合前端2行显示）
     */
    private String extractMatchedContent(Post post, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        
        // 将关键词转为小写，用于查找
        String lowerKeyword = keyword.toLowerCase();
        
        // 优先从摘要中查找
        if (StringUtils.hasText(post.getSummary())) {
            int index = post.getSummary().toLowerCase().indexOf(lowerKeyword);
            if (index != -1) {
                return extractSnippet(post.getSummary(), index, keyword, 30);
            }
        }
        
        // 从内容中查找
        if (StringUtils.hasText(post.getContent())) {
            int index = post.getContent().toLowerCase().indexOf(lowerKeyword);
            if (index != -1) {
                return extractSnippet(post.getContent(), index, keyword, 30);
            }
        }
        
        return null;
    }
    
    /**
     * 提取文本片段，以指定位置为中心，前后各保留 contextLength 个字符
     * @param text 原文本
     * @param matchIndex 匹配位置的索引
     * @param keyword 关键词
     * @param contextLength 上下文长度
     * @return 提取的片段
     */
    private String extractSnippet(String text, int matchIndex, String keyword, int contextLength) {
        int start = Math.max(0, matchIndex - contextLength);
        int end = Math.min(text.length(), matchIndex + keyword.length() + contextLength);
        
        String snippet = text.substring(start, end);
        
        // 如果不在开头，添加省略号
        if (start > 0) {
            snippet = "..." + snippet;
        }
        // 如果不在结尾，添加省略号
        if (end < text.length()) {
            snippet = snippet + "...";
        }
        
        return snippet;
    }
}

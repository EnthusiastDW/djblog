package site.dengwei.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.dengwei.blog.dto.PostDetailDTO;
import site.dengwei.blog.dto.request.*;
import site.dengwei.blog.entity.Post;
import site.dengwei.blog.enums.PostStatus;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.service.PostService;
import site.dengwei.common.beans.Response;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * PostController 测试类
 *
 * @author dengwei
 * @since 2025-09-08
 */
@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    private Post testPost;

    @BeforeEach
    void setUp() {
        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("测试文章");
        testPost.setContent("测试内容");
        testPost.setSlug("test-post");
        testPost.setStatus(PostStatus.PUBLISHED);
        testPost.setAuthorId(1L);
        testPost.setPublishedAt(LocalDateTime.now());
    }

    // ==================== 查询测试 ====================

    @Test
    @DisplayName("分页查询所有文章")
    void selectAll_Success() {
        // Given
        Page<Post> page = new Page<>(1, 10);
        Page<Post> resultPage = new Page<>(1, 10);
        resultPage.setRecords(Arrays.asList(testPost));
        when(postService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(resultPage);

        // When
        Response<Page<Post>> response = postController.selectAll(page, new Post());

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(1, response.getData().getRecords().size());
    }

    @Test
    @DisplayName("根据ID查询文章")
    void selectOne_Success() {
        // Given
        when(postService.getByIdOrThrow(1L)).thenReturn(testPost);

        // When
        Response<Post> response = postController.selectOne(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("测试文章", response.getData().getTitle());
    }

    @Test
    @DisplayName("根据ID查询文章不存在")
    void selectOne_NotFound() {
        // Given
        when(postService.getByIdOrThrow(999L)).thenThrow(new BusinessException("文章不存在"));

        // When & Then
        assertThrows(BusinessException.class, () -> postController.selectOne(999L));
    }

    @Test
    @DisplayName("查询文章详情")
    void selectDetail_Success() {
        // Given
        PostDetailDTO dto = new PostDetailDTO();
        dto.setId(1L);
        dto.setTitle("测试文章");
        when(postService.getPostDetailOrThrow(1L)).thenReturn(dto);

        // When
        Response<PostDetailDTO> response = postController.selectDetail(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("测试文章", response.getData().getTitle());
    }

    @Test
    @DisplayName("搜索文章")
    void search_Success() {
        // Given
        Page<Post> page = new Page<>(1, 10);
        when(postService.searchPosts(anyString(), any(Page.class))).thenReturn(page);

        // When
        Response<Page<Post>> response = postController.search(page, "测试");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
    }

    @Test
    @DisplayName("获取文章归档")
    void getArchives_Success() {
        // Given
        when(postService.getPostArchives()).thenReturn(Arrays.asList(Map.of("yearMonth", "2025-01")));

        // When
        Response<List<Map<String, Object>>> response = postController.getArchives();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
    }

    // ==================== 创建测试 ====================

    @Test
    @DisplayName("创建文章成功")
    void insert_Success() {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("新文章");
        request.setContent("新内容");
        when(postService.createPost(any(CreatePostRequest.class))).thenReturn(true);

        Response<Boolean> response = postController.insert(request);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
    }

    @Test
    @DisplayName("发布文章成功")
    void publish_Success() {
        PublishRequest request = new PublishRequest();
        request.setTitle("发布的文章");
        request.setContent("发布的内容");
        when(postService.publish(any(PublishRequest.class))).thenReturn(true);

        Response<Boolean> response = postController.publish(request);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
    }

    @Test
    @DisplayName("保存草稿成功")
    void saveDraft_Success() {
        SaveDraftRequest request = new SaveDraftRequest();
        request.setTitle("草稿");
        request.setContent("草稿内容");
        when(postService.saveDraft(any(SaveDraftRequest.class))).thenReturn(true);

        Response<Boolean> response = postController.saveDraft(request);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
    }

    // ==================== 更新测试 ====================

    @Test
    @DisplayName("更新文章成功")
    void update_Success() {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setId(1L);
        request.setTitle("更新后的文章");
        when(postService.updatePost(any(UpdatePostRequest.class))).thenReturn(true);

        Response<Boolean> response = postController.update(request);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
    }

    // ==================== 删除测试 ====================

    @Test
    @DisplayName("删除文章成功")
    void delete_Success() {
        DeleteRequest request = new DeleteRequest();
        request.setIdList(Arrays.asList(1L));
        when(postService.deletePosts(anyList())).thenReturn(true);

        Response<Boolean> response = postController.delete(request.getIdList());

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
    }
}

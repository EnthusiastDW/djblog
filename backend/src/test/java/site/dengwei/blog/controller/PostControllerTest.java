package site.dengwei.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.dengwei.blog.dto.PostListDTO;
import site.dengwei.blog.entity.Post;
import site.dengwei.blog.service.AiService;
import site.dengwei.blog.service.PostService;
import site.dengwei.blog.dto.Response;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PostController 分类层级相关接口测试
 */
@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private AiService aiService;

    @InjectMocks
    private PostController postController;

    @Test
    @DisplayName("按分类ID列表查询文章 - 包含子分类的文章")
    void selectByCategoryIds_Success() {
        // Given
        Page<Post> page = new Page<>(1, 10);
        Page<PostListDTO> resultPage = new Page<>(1, 10);

        PostListDTO dto = new PostListDTO();
        dto.setId(1L);
        dto.setTitle("Java 入门");
        dto.setCategoryId(3L);
        dto.setCategoryName("Java");

        resultPage.setRecords(Arrays.asList(dto));
        resultPage.setTotal(1);

        when(postService.getPostListWithRelationsByCategoryIds(any(Page.class), anyList()))
                .thenReturn(resultPage);

        // When
        Response<Page<PostListDTO>> response = postController.selectByCategoryIds(
                page, Arrays.asList(2L, 3L, 4L));

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(1, response.getData().getRecords().size());
        assertEquals("Java 入门", response.getData().getRecords().get(0).getTitle());
        verify(postService).getPostListWithRelationsByCategoryIds(eq(page), eq(Arrays.asList(2L, 3L, 4L)));
    }

    @Test
    @DisplayName("按分类ID列表查询 - 空分类列表应返回空结果")
    void selectByCategoryIds_EmptyIds_ShouldReturnEmpty() {
        Page<Post> page = new Page<>(1, 10);
        Page<PostListDTO> emptyPage = new Page<>(1, 10);
        emptyPage.setTotal(0);
        emptyPage.setRecords(Collections.emptyList());

        when(postService.getPostListWithRelationsByCategoryIds(any(Page.class), anyList()))
                .thenReturn(emptyPage);

        Response<Page<PostListDTO>> response = postController.selectByCategoryIds(
                page, Arrays.asList(999L));

        assertNotNull(response);
        assertEquals(0, response.getData().getTotal());
        assertTrue(response.getData().getRecords().isEmpty());
    }

    @Test
    @DisplayName("按分类ID列表查询 - 单个分类ID")
    void selectByCategoryIds_SingleId_ShouldWork() {
        Page<Post> page = new Page<>(1, 10);
        Page<PostListDTO> resultPage = new Page<>(1, 10);
        resultPage.setRecords(Collections.emptyList());
        resultPage.setTotal(0);

        when(postService.getPostListWithRelationsByCategoryIds(any(Page.class), anyList()))
                .thenReturn(resultPage);

        Response<Page<PostListDTO>> response = postController.selectByCategoryIds(
                page, Arrays.asList(1L));

        verify(postService).getPostListWithRelationsByCategoryIds(eq(page), eq(Arrays.asList(1L)));
    }

    @Test
    @DisplayName("按分类ID列表查询 - 多个子分类ID应合并查询")
    void selectByCategoryIds_MultipleChildIds_ShouldQueryAll() {
        Page<Post> page = new Page<>(1, 10);
        Page<PostListDTO> resultPage = new Page<>(1, 10);
        resultPage.setRecords(Collections.emptyList());
        resultPage.setTotal(5);

        List<Long> categoryIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);
        when(postService.getPostListWithRelationsByCategoryIds(any(Page.class), eq(categoryIds)))
                .thenReturn(resultPage);

        Response<Page<PostListDTO>> response = postController.selectByCategoryIds(
                page, categoryIds);

        assertEquals(5, response.getData().getTotal());
    }
}

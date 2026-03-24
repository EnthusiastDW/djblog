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
import site.dengwei.blog.dto.request.CreateTagRequest;
import site.dengwei.blog.dto.request.UpdateTagRequest;
import site.dengwei.blog.entity.Tag;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.service.TagService;
import site.dengwei.blog.dto.Response;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TagController 测试类
 *
 * @author dengwei
 * @since 2025-09-08
 */
@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    @Mock
    private TagService tagService;

    @InjectMocks
    private TagController tagController;

    private Tag testTag;

    @BeforeEach
    void setUp() {
        testTag = new Tag();
        testTag.setId(1L);
        testTag.setName("测试标签");
        testTag.setSlug("test-tag");
    }

    // ==================== 查询测试 ====================

    @Test
    @DisplayName("分页查询所有标签")
    void selectAll_Success() {
        // Given
        Page<Tag> page = new Page<>(1, 10);
        Page<Tag> resultPage = new Page<>(1, 10);
        resultPage.setRecords(Arrays.asList(testTag));
        when(tagService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(resultPage);

        // When
        Response<Page<Tag>> response = tagController.selectAll(page, new Tag());

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(1, response.getData().getRecords().size());
    }

    @Test
    @DisplayName("根据ID查询标签")
    void selectOne_Success() {
        // Given
        when(tagService.getByIdOrThrow(1L)).thenReturn(testTag);

        // When
        Response<Tag> response = tagController.selectOne(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("测试标签", response.getData().getName());
    }

    @Test
    @DisplayName("根据ID查询标签不存在")
    void selectOne_NotFound() {
        // Given
        when(tagService.getByIdOrThrow(999L)).thenThrow(new BusinessException("标签不存在"));

        // When & Then
        assertThrows(BusinessException.class, () -> tagController.selectOne(999L));
    }

    // ==================== 创建测试 ====================

    @Test
    @DisplayName("创建标签成功")
    void insert_Success() {
        // Given
        CreateTagRequest request = new CreateTagRequest();
        request.setName("新标签");
        when(tagService.createTag(any(CreateTagRequest.class))).thenReturn(true);

        // When
        Response<Boolean> response = tagController.insert(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
        verify(tagService).createTag(any(CreateTagRequest.class));
    }

    // ==================== 更新测试 ====================

    @Test
    @DisplayName("更新标签成功")
    void update_Success() {
        // Given
        UpdateTagRequest request = new UpdateTagRequest();
        request.setId(1L);
        request.setName("更新后的标签");
        when(tagService.updateTag(any(UpdateTagRequest.class))).thenReturn(true);

        // When
        Response<Boolean> response = tagController.update(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
        verify(tagService).updateTag(any(UpdateTagRequest.class));
    }

    // ==================== 删除测试 ====================

    @Test
    @DisplayName("删除标签成功")
    void delete_Success() {
        // Given
        when(tagService.deleteTag(1L)).thenReturn(true);

        // When
        Response<Boolean> response = tagController.delete(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
        verify(tagService).deleteTag(1L);
    }
}

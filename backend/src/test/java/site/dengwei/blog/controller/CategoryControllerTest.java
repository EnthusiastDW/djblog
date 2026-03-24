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
import site.dengwei.blog.dto.request.CreateCategoryRequest;
import site.dengwei.blog.dto.request.UpdateCategoryRequest;
import site.dengwei.blog.entity.Category;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.service.CategoryService;
import site.dengwei.blog.dto.Response;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CategoryController 测试类
 *
 * @author dengwei
 * @since 2025-09-08
 */
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("测试分类");
        testCategory.setSlug("test-category");
        testCategory.setDescription("测试分类描述");
    }

    // ==================== 查询测试 ====================

    @Test
    @DisplayName("分页查询所有分类")
    void selectAll_Success() {
        // Given
        Page<Category> page = new Page<>(1, 10);
        Page<Category> resultPage = new Page<>(1, 10);
        resultPage.setRecords(Arrays.asList(testCategory));
        when(categoryService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(resultPage);

        // When
        Response<Page<Category>> response = categoryController.selectAll(page, new Category());

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(1, response.getData().getRecords().size());
    }

    @Test
    @DisplayName("根据ID查询分类")
    void selectOne_Success() {
        // Given
        when(categoryService.getByIdOrThrow(1L)).thenReturn(testCategory);

        // When
        Response<Category> response = categoryController.selectOne(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("测试分类", response.getData().getName());
    }

    @Test
    @DisplayName("根据ID查询分类不存在")
    void selectOne_NotFound() {
        // Given
        when(categoryService.getByIdOrThrow(999L)).thenThrow(new BusinessException("分类不存在"));

        // When & Then
        assertThrows(BusinessException.class, () -> categoryController.selectOne(999L));
    }

    // ==================== 创建测试 ====================

    @Test
    @DisplayName("创建分类成功")
    void insert_Success() {
        // Given
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("新分类");
        request.setDescription("新分类描述");
        when(categoryService.createCategory(any(CreateCategoryRequest.class))).thenReturn(true);

        // When
        Response<Boolean> response = categoryController.insert(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
        verify(categoryService).createCategory(any(CreateCategoryRequest.class));
    }

    // ==================== 更新测试 ====================

    @Test
    @DisplayName("更新分类成功")
    void update_Success() {
        // Given
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setId(1L);
        request.setName("更新后的分类");
        request.setDescription("更新后的描述");
        when(categoryService.updateCategory(any(UpdateCategoryRequest.class))).thenReturn(true);

        // When
        Response<Boolean> response = categoryController.update(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
        verify(categoryService).updateCategory(any(UpdateCategoryRequest.class));
    }

    // ==================== 删除测试 ====================

    @Test
    @DisplayName("删除分类成功")
    void delete_Success() {
        // Given
        when(categoryService.deleteCategory(1L)).thenReturn(true);

        // When
        Response<Boolean> response = categoryController.delete(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
        verify(categoryService).deleteCategory(1L);
    }
}

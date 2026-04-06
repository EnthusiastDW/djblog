package site.dengwei.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.dengwei.blog.dto.CategoryTreeDTO;
import site.dengwei.blog.dto.CategoryWithCountDTO;
import site.dengwei.blog.dto.request.CreateCategoryRequest;
import site.dengwei.blog.dto.request.UpdateCategoryRequest;
import site.dengwei.blog.entity.Category;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.service.CategoryService;
import site.dengwei.blog.dto.Response;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CategoryController 测试类
 * 覆盖分类层级功能的所有 API 接口
 */
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private Category testCategory;
    private CategoryTreeDTO treeDTO;
    private CategoryWithCountDTO withCountDTO;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("测试分类");
        testCategory.setDescription("测试描述");
        testCategory.setParentId(null);

        // 构建树形 DTO
        treeDTO = new CategoryTreeDTO();
        treeDTO.setId(1L);
        treeDTO.setName("技术");
        treeDTO.setPostCount(10L);
        treeDTO.setTotalPostCount(25L);

        CategoryTreeDTO childDTO = new CategoryTreeDTO();
        childDTO.setId(2L);
        childDTO.setName("后端");
        childDTO.setPostCount(8L);
        childDTO.setTotalPostCount(15L);
        treeDTO.setChildren(Arrays.asList(childDTO));

        withCountDTO = new CategoryWithCountDTO();
        withCountDTO.setId(1L);
        withCountDTO.setName("测试分类");
        withCountDTO.setPostCount(5L);
    }

    // ==================== 树形接口测试 ====================

    @Nested
    @DisplayName("树形接口")
    class TreeApiTests {

        @Test
        @DisplayName("获取前台树形分类成功")
        void getTree_Success() {
            when(categoryService.getCategoryTree()).thenReturn(Arrays.asList(treeDTO));

            Response<List<CategoryTreeDTO>> response = categoryController.getTree();

            assertNotNull(response);
            assertEquals(200, response.getCode());
            assertNotNull(response.getData());
            assertEquals(1, response.getData().size());
            assertEquals("技术", response.getData().get(0).getName());
            verify(categoryService).getCategoryTree();
        }

        @Test
        @DisplayName("获取管理后台树形分类成功")
        void getTreeForAdmin_Success() {
            when(categoryService.getCategoryTreeForAdmin()).thenReturn(Arrays.asList(treeDTO));

            Response<List<CategoryTreeDTO>> response = categoryController.getTreeForAdmin();

            assertNotNull(response);
            assertEquals(200, response.getCode());
            assertEquals(1, response.getData().size());
            verify(categoryService).getCategoryTreeForAdmin();
        }

        @Test
        @DisplayName("获取空树形分类成功")
        void getTree_Empty_ShouldReturnEmptyList() {
            when(categoryService.getCategoryTree()).thenReturn(Collections.emptyList());

            Response<List<CategoryTreeDTO>> response = categoryController.getTree();

            assertEquals(200, response.getCode());
            assertNotNull(response.getData());
            assertTrue(response.getData().isEmpty());
        }
    }

    // ==================== 祖先链路接口测试 ====================

    @Nested
    @DisplayName("祖先链路接口")
    class AncestorApiTests {

        @Test
        @DisplayName("获取分类祖先链路成功")
        void getAncestors_Success() {
            Category parent = new Category();
            parent.setId(1L);
            parent.setName("技术");
            parent.setParentId(null);

            Category child = new Category();
            child.setId(2L);
            child.setName("后端");
            child.setParentId(1L);

            when(categoryService.getCategoryAncestors(2L)).thenReturn(Arrays.asList(parent, child));

            Response<List<Category>> response = categoryController.getAncestors(2L);

            assertNotNull(response);
            assertEquals(200, response.getCode());
            assertEquals(2, response.getData().size());
            assertEquals("技术", response.getData().get(0).getName());
            assertEquals("后端", response.getData().get(1).getName());
        }

        @Test
        @DisplayName("获取顶级分类祖先链路只返回自身")
        void getAncestors_Root_ShouldReturnOnlySelf() {
            when(categoryService.getCategoryAncestors(1L)).thenReturn(Arrays.asList(testCategory));

            Response<List<Category>> response = categoryController.getAncestors(1L);

            assertEquals(1, response.getData().size());
            assertEquals("测试分类", response.getData().get(0).getName());
        }
    }

    // ==================== 查询接口测试 ====================

    @Nested
    @DisplayName("查询接口")
    class QueryApiTests {

        @Test
        @DisplayName("分页查询所有分类")
        void selectAll_Success() {
            Page<Category> page = new Page<>(1, 10);
            Page<Category> resultPage = new Page<>(1, 10);
            resultPage.setRecords(Arrays.asList(testCategory));
            when(categoryService.page(any(Page.class), any())).thenReturn(resultPage);

            Response<Page<Category>> response = categoryController.selectAll(page, testCategory);

            assertNotNull(response);
            assertEquals(200, response.getCode());
            assertEquals(1, response.getData().getRecords().size());
        }

        @Test
        @DisplayName("获取所有分类（带文章数量）")
        void getAllWithCount_Success() {
            when(categoryService.getAllCategoriesWithCount()).thenReturn(Arrays.asList(withCountDTO));

            Response<List<CategoryWithCountDTO>> response = categoryController.getAllWithCount();

            assertNotNull(response);
            assertEquals(200, response.getCode());
            assertEquals(1, response.getData().size());
            assertEquals(5L, response.getData().get(0).getPostCount());
        }

        @Test
        @DisplayName("根据ID查询分类")
        void selectOne_Success() {
            when(categoryService.getByIdOrThrow(1L)).thenReturn(testCategory);

            Response<Category> response = categoryController.selectOne(1L);

            assertNotNull(response);
            assertEquals(200, response.getCode());
            assertEquals("测试分类", response.getData().getName());
        }

        @Test
        @DisplayName("根据ID查询分类不存在")
        void selectOne_NotFound() {
            when(categoryService.getByIdOrThrow(999L)).thenThrow(new BusinessException("分类不存在"));

            assertThrows(BusinessException.class, () -> categoryController.selectOne(999L));
        }
    }

    // ==================== 创建测试 ====================

    @Nested
    @DisplayName("创建接口")
    class CreateApiTests {

        @Test
        @DisplayName("创建顶级分类成功")
        void insert_RootCategory_Success() {
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("新分类");
            request.setDescription("新分类描述");
            when(categoryService.createCategory(any(CreateCategoryRequest.class))).thenReturn(1L);

            Response<Long> response = categoryController.insert(request);

            assertNotNull(response);
            assertEquals(200, response.getCode());
            assertEquals(1L, response.getData());
            verify(categoryService).createCategory(any(CreateCategoryRequest.class));
        }

        @Test
        @DisplayName("创建子分类成功")
        void insert_ChildCategory_Success() {
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("子分类");
            request.setParentId(1L);
            when(categoryService.createCategory(any(CreateCategoryRequest.class))).thenReturn(2L);

            Response<Long> response = categoryController.insert(request);

            assertEquals(200, response.getCode());
            assertEquals(2L, response.getData());
        }

        @Test
        @DisplayName("创建分类时名称重复应抛异常")
        void insert_DuplicateName_ShouldThrow() {
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("已存在的分类");
            when(categoryService.createCategory(any(CreateCategoryRequest.class)))
                    .thenThrow(new BusinessException("分类名称已存在"));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> categoryController.insert(request));
            assertEquals("分类名称已存在", ex.getMessage());
        }
    }

    // ==================== 更新测试 ====================

    @Nested
    @DisplayName("更新接口")
    class UpdateApiTests {

        @Test
        @DisplayName("更新分类名称成功")
        void update_Success() {
            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setId(1L);
            request.setName("更新后的分类");
            request.setDescription("更新后的描述");
            when(categoryService.updateCategory(any(UpdateCategoryRequest.class))).thenReturn(true);

            Response<Boolean> response = categoryController.update(request);

            assertNotNull(response);
            assertEquals(200, response.getCode());
            assertTrue(response.getData());
        }

        @Test
        @DisplayName("更新分类的父分类成功")
        void update_ChangeParent_Success() {
            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setId(2L);
            request.setName("后端");
            request.setParentId(1L);
            when(categoryService.updateCategory(any(UpdateCategoryRequest.class))).thenReturn(true);

            Response<Boolean> response = categoryController.update(request);

            assertTrue(response.getData());
        }
    }

    // ==================== 删除测试 ====================

    @Nested
    @DisplayName("删除接口")
    class DeleteApiTests {

        @Test
        @DisplayName("删除分类成功")
        void delete_Success() {
            when(categoryService.deleteCategory(1L)).thenReturn(true);

            Response<Boolean> response = categoryController.delete(1L);

            assertNotNull(response);
            assertEquals(200, response.getCode());
            assertTrue(response.getData());
            verify(categoryService).deleteCategory(1L);
        }

        @Test
        @DisplayName("删除有子分类的分类应抛异常")
        void delete_HasChildren_ShouldThrow() {
            when(categoryService.deleteCategory(1L))
                    .thenThrow(new BusinessException("该分类下有子分类，请先删除或移动子分类"));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> categoryController.delete(1L));
            assertEquals("该分类下有子分类，请先删除或移动子分类", ex.getMessage());
        }

        @Test
        @DisplayName("删除有关联文章的分类应抛异常")
        void delete_HasPosts_ShouldThrow() {
            when(categoryService.deleteCategory(1L))
                    .thenThrow(new BusinessException("该分类下有 5 篇文章，请先移动文章"));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> categoryController.delete(1L));
            assertEquals("该分类下有 5 篇文章，请先移动文章", ex.getMessage());
        }
    }
}

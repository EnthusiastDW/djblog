package site.dengwei.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import site.dengwei.blog.dto.CategoryTreeDTO;
import site.dengwei.blog.dto.request.CreateCategoryRequest;
import site.dengwei.blog.dto.request.UpdateCategoryRequest;
import site.dengwei.blog.entity.Category;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.mapper.CategoryMapper;
import site.dengwei.blog.mapper.PostMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doAnswer;

/**
 * CategoryServiceImpl 单元测试
 * 覆盖分类层级功能的核心业务逻辑
 *
 * 注意：CategoryServiceImpl 继承了 ServiceImpl<CategoryMapper, Category>，
 * 其内部通过 this.baseMapper 调用数据库操作。
 * Mockito 的 @InjectMocks 无法注入父类字段，因此需要通过反射手动设置 baseMapper。
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private PostMapper postMapper;

    private CategoryServiceImpl categoryService;

    // ==================== Mock 数据准备 ====================

    private Category techCategory;      // 技术 (顶级)
    private Category backendCategory;   // 后端 (技术的子分类)
    private Category javaCategory;      // Java (后端的子分类)
    private Category springCategory;    // Spring (Java的子分类)
    private Category frontendCategory;  // 前端 (技术的子分类)

    @BeforeEach
    void setUp() {
        // 手动创建实例并通过反射注入 baseMapper
        categoryService = new CategoryServiceImpl(postMapper);
        ReflectionTestUtils.setField(categoryService, "baseMapper", categoryMapper);

        // 构建层级结构:
        // 技术 (1)
        //   ├── 后端 (2)
        //   │   ├── Java (3)
        //   │   │   └── Spring (4)  ← 第4层
        //   │   └── Go (5)
        //   └── 前端 (6)
        techCategory = createCategory(1L, "技术", null, "技术相关文章");
        backendCategory = createCategory(2L, "后端", 1L, "后端开发");
        javaCategory = createCategory(3L, "Java", 2L, "Java语言");
        springCategory = createCategory(4L, "Spring", 3L, "Spring框架");
        frontendCategory = createCategory(6L, "前端", 1L, "前端开发");
    }

    private Category createCategory(Long id, String name, Long parentId, String description) {
        Category cat = new Category();
        cat.setId(id);
        cat.setName(name);
        cat.setParentId(parentId);
        cat.setDescription(description);
        return cat;
    }

    // ==================== 树形构建测试 ====================

    @Nested
    @DisplayName("树形构建")
    class BuildTreeTests {

        @Test
        @DisplayName("平铺分类列表应正确构建为树形结构")
        void buildTree_FlatList_ShouldBuildCorrectTree() {
            // Given
            List<Category> allCategories = Arrays.asList(
                    techCategory, backendCategory, javaCategory, springCategory, frontendCategory
            );
            when(categoryMapper.selectList(any())).thenReturn(allCategories);
            when(postMapper.countByCategoryId(anyLong())).thenReturn(5L, 3L, 2L, 1L, 4L);

            // When
            List<CategoryTreeDTO> tree = categoryService.getCategoryTree();

            // Then
            // 应该只有1个顶级分类（技术）
            assertEquals(1, tree.size());
            assertEquals("技术", tree.get(0).getName());
            assertEquals(1L, tree.get(0).getId());

            // 技术应该有2个子分类
            List<CategoryTreeDTO> techChildren = tree.get(0).getChildren();
            assertNotNull(techChildren);
            assertEquals(2, techChildren.size());

            // 找到后端子分类
            CategoryTreeDTO backendNode = techChildren.stream()
                    .filter(c -> c.getName().equals("后端"))
                    .findFirst().orElseThrow();
            assertEquals(2L, backendNode.getId());

            // 后端应该有Java子分类
            CategoryTreeDTO javaNode = backendNode.getChildren().stream()
                    .filter(c -> c.getName().equals("Java"))
                    .findFirst().orElseThrow();
            assertEquals(3L, javaNode.getId());

            // Java应该有Spring子分类
            CategoryTreeDTO springNode = javaNode.getChildren().stream()
                    .filter(c -> c.getName().equals("Spring"))
                    .findFirst().orElseThrow();
            assertEquals(4L, springNode.getId());
        }

        @Test
        @DisplayName("空分类列表应返回空树")
        void buildTree_EmptyList_ShouldReturnEmptyTree() {
            when(categoryMapper.selectList(any())).thenReturn(Collections.emptyList());

            List<CategoryTreeDTO> tree = categoryService.getCategoryTree();

            assertNotNull(tree);
            assertTrue(tree.isEmpty());
        }

        @Test
        @DisplayName("多个顶级分类应正确分组")
        void buildTree_MultipleRoots_ShouldGroupCorrectly() {
            Category lifestyle = createCategory(10L, "生活", null, "生活随笔");
            Category travel = createCategory(11L, "旅行", 10L, "旅行日志");

            List<Category> allCategories = Arrays.asList(
                    techCategory, backendCategory, lifestyle, travel
            );
            when(categoryMapper.selectList(any())).thenReturn(allCategories);
            when(postMapper.countByCategoryId(anyLong())).thenReturn(10L, 5L, 3L, 2L);

            List<CategoryTreeDTO> tree = categoryService.getCategoryTree();

            // 应该有2个顶级分类（按文章数降序）
            assertEquals(2, tree.size());
            // 技术文章数 > 生活文章数，所以技术在前
            assertEquals("技术", tree.get(0).getName());
            assertEquals("生活", tree.get(1).getName());
        }

        @Test
        @DisplayName("totalPostCount 应正确汇总所有子分类的文章数")
        void buildTree_ShouldCalculateTotalPostCount() {
            List<Category> allCategories = Arrays.asList(
                    techCategory, backendCategory, javaCategory, frontendCategory
            );
            // 技术:5, 后端:3, Java:2, 前端:4
            when(categoryMapper.selectList(any())).thenReturn(allCategories);
            when(postMapper.countByCategoryId(1L)).thenReturn(5L);
            when(postMapper.countByCategoryId(2L)).thenReturn(3L);
            when(postMapper.countByCategoryId(3L)).thenReturn(2L);
            when(postMapper.countByCategoryId(6L)).thenReturn(4L);

            List<CategoryTreeDTO> tree = categoryService.getCategoryTree();

            CategoryTreeDTO techNode = tree.get(0);
            // 技术本身5篇 + 后端3篇 + Java2篇 + 前端4篇 = 14篇
            assertEquals(14L, techNode.getTotalPostCount());

            // 找到后端
            CategoryTreeDTO backendNode = techNode.getChildren().stream()
                    .filter(c -> c.getName().equals("后端"))
                    .findFirst().orElseThrow();
            // 后端本身3篇 + Java2篇 = 5篇
            assertEquals(5L, backendNode.getTotalPostCount());
        }
    }

    // ==================== 祖先链路测试 ====================

    @Nested
    @DisplayName("祖先链路")
    class AncestorTests {

        @Test
        @DisplayName("第4层分类应返回4级祖先链路")
        void getAncestors_FourLevelCategory_ShouldReturnFullChain() {
            when(categoryMapper.selectById(4L)).thenReturn(springCategory);
            when(categoryMapper.selectById(3L)).thenReturn(javaCategory);
            when(categoryMapper.selectById(2L)).thenReturn(backendCategory);
            when(categoryMapper.selectById(1L)).thenReturn(techCategory);

            List<Category> ancestors = categoryService.getCategoryAncestors(4L);

            assertEquals(4, ancestors.size());
            assertEquals("技术", ancestors.get(0).getName());  // 顶级在前
            assertEquals("后端", ancestors.get(1).getName());
            assertEquals("Java", ancestors.get(2).getName());
            assertEquals("Spring", ancestors.get(3).getName()); // 当前分类在最后
        }

        @Test
        @DisplayName("顶级分类应只返回自身")
        void getAncestors_RootCategory_ShouldReturnOnlySelf() {
            when(categoryMapper.selectById(1L)).thenReturn(techCategory);

            List<Category> ancestors = categoryService.getCategoryAncestors(1L);

            assertEquals(1, ancestors.size());
            assertEquals("技术", ancestors.get(0).getName());
        }

        @Test
        @DisplayName("不存在的分类应返回空列表")
        void getAncestors_NotExist_ShouldReturnEmpty() {
            when(categoryMapper.selectById(999L)).thenReturn(null);

            List<Category> ancestors = categoryService.getCategoryAncestors(999L);

            assertTrue(ancestors.isEmpty());
        }

        @Test
        @DisplayName("父分类不存在时应中断追溯")
        void getAncestors_BrokenChain_ShouldStopAtMissing() {
            Category orphan = createCategory(99L, "孤儿分类", 98L, null);
            when(categoryMapper.selectById(99L)).thenReturn(orphan);
            when(categoryMapper.selectById(98L)).thenReturn(null);

            List<Category> ancestors = categoryService.getCategoryAncestors(99L);

            // 只有自身，因为父分类98不存在
            assertEquals(1, ancestors.size());
            assertEquals("孤儿分类", ancestors.get(0).getName());
        }
    }

    // ==================== 后代ID收集测试 ====================

    @Nested
    @DisplayName("后代ID收集")
    class DescendantTests {

        @Test
        @DisplayName("应正确收集所有层级的后代ID")
        void getDescendantIds_ShouldCollectAllDescendants() {
            when(categoryMapper.selectList(argThat(w ->
                    w instanceof LambdaQueryWrapper
            ))).thenReturn(
                    Arrays.asList(backendCategory, frontendCategory),  // parentId=1 的子分类
                    Collections.singletonList(javaCategory),          // parentId=2 的子分类
                    Collections.singletonList(springCategory),        // parentId=3 的子分类
                    Collections.emptyList()                           // Spring 没有子分类
            );

            List<Long> descendants = categoryService.getDescendantIds(1L);

            // 应包含：自身(1) + 后端(2) + 前端(6) + Java(3) + Spring(4)
            assertEquals(5, descendants.size());
            assertTrue(descendants.contains(1L));
            assertTrue(descendants.contains(2L));
            assertTrue(descendants.contains(3L));
            assertTrue(descendants.contains(4L));
            assertTrue(descendants.contains(6L));
        }

        @Test
        @DisplayName("叶子节点应只返回自身ID")
        void getDescendantIds_LeafCategory_ShouldReturnOnlySelf() {
            when(categoryMapper.selectList(any())).thenReturn(Collections.emptyList());

            List<Long> descendants = categoryService.getDescendantIds(4L);

            assertEquals(1, descendants.size());
            assertEquals(4L, descendants.get(0));
        }
    }

    // ==================== 层级深度测试 ====================

    @Nested
    @DisplayName("层级深度")
    class DepthTests {

        @Test
        @DisplayName("顶级分类深度应为1")
        void getCategoryDepth_Root_ShouldBeOne() {
            when(categoryMapper.selectById(1L)).thenReturn(techCategory);

            assertEquals(1, categoryService.getCategoryDepth(1L));
        }

        @Test
        @DisplayName("第4层分类深度应为4")
        void getCategoryDepth_FourthLevel_ShouldBeFour() {
            when(categoryMapper.selectById(4L)).thenReturn(springCategory);
            when(categoryMapper.selectById(3L)).thenReturn(javaCategory);
            when(categoryMapper.selectById(2L)).thenReturn(backendCategory);
            when(categoryMapper.selectById(1L)).thenReturn(techCategory);

            assertEquals(4, categoryService.getCategoryDepth(4L));
        }

        @Test
        @DisplayName("不存在的分类深度应为0")
        void getCategoryDepth_NotExist_ShouldBeZero() {
            when(categoryMapper.selectById(999L)).thenReturn(null);

            assertEquals(0, categoryService.getCategoryDepth(999L));
        }
    }

    // ==================== 创建分类测试 ====================

    @Nested
    @DisplayName("创建分类")
    class CreateTests {

        @Test
        @DisplayName("创建顶级分类成功")
        void createCategory_RootCategory_ShouldSucceed() {
            when(categoryMapper.selectCount(any())).thenReturn(0L);
            doAnswer(invocation -> {
                Category cat = invocation.getArgument(0, Category.class);
                cat.setId(100L);
                return 1;
            }).when(categoryMapper).insert(any(Category.class));

            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("数据库");
            request.setDescription("数据库相关");

            Long id = categoryService.createCategory(request);

            assertEquals(100L, id);
            verify(categoryMapper).insert(any(Category.class));
        }

        @Test
        @DisplayName("创建子分类成功")
        void createCategory_ChildCategory_ShouldSucceed() {
            when(categoryMapper.selectCount(any())).thenReturn(0L);
            when(categoryMapper.selectById(1L)).thenReturn(techCategory);
            doAnswer(invocation -> {
                Category cat = invocation.getArgument(0, Category.class);
                cat.setId(101L);
                return 1;
            }).when(categoryMapper).insert(any(Category.class));

            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("DevOps");
            request.setParentId(1L);

            Long id = categoryService.createCategory(request);

            assertEquals(101L, id);
        }

        @Test
        @DisplayName("创建分类时名称重复应抛异常")
        void createCategory_DuplicateName_ShouldThrowException() {
            when(categoryMapper.selectCount(any())).thenReturn(1L);

            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("技术");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> categoryService.createCategory(request));
            assertEquals("分类名称已存在", ex.getMessage());
        }

        @Test
        @DisplayName("创建第5层子分类应抛异常（超过MAX_DEPTH=4）")
        void createCategory_ExceedDepth_ShouldThrowException() {
            // Spring在第4层，其深度为4，不能在其下创建子分类
            when(categoryMapper.selectCount(any())).thenReturn(0L);
            when(categoryMapper.selectById(4L)).thenReturn(springCategory);
            when(categoryMapper.selectById(3L)).thenReturn(javaCategory);
            when(categoryMapper.selectById(2L)).thenReturn(backendCategory);
            when(categoryMapper.selectById(1L)).thenReturn(techCategory);

            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("SpringBoot");
            request.setParentId(4L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> categoryService.createCategory(request));
            assertEquals("分类层级深度不能超过 4 级", ex.getMessage());
        }

        @Test
        @DisplayName("父分类不存在应抛异常")
        void createCategory_ParentNotExist_ShouldThrowException() {
            when(categoryMapper.selectCount(any())).thenReturn(0L);
            when(categoryMapper.selectById(999L)).thenReturn(null);

            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("测试");
            request.setParentId(999L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> categoryService.createCategory(request));
            assertEquals("父分类不存在", ex.getMessage());
        }
    }

    // ==================== 更新分类测试 ====================

    @Nested
    @DisplayName("更新分类")
    class UpdateTests {

        @Test
        @DisplayName("更新分类名称成功")
        void updateCategory_ChangeName_ShouldSucceed() {
            when(categoryMapper.selectCount(any())).thenReturn(0L);
            when(categoryMapper.selectById(2L)).thenReturn(backendCategory);
            when(categoryMapper.updateById(any(Category.class))).thenReturn(1);

            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setId(2L);
            request.setName("后端开发");
            request.setDescription("后端开发相关");

            assertTrue(categoryService.updateCategory(request));
        }

        @Test
        @DisplayName("更新分类名称为已存在名称应抛异常")
        void updateCategory_DuplicateName_ShouldThrowException() {
            // 名称 "前端" 已被 frontendCategory(6) 占用
            when(categoryMapper.selectCount(any())).thenReturn(1L);

            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setId(2L);
            request.setName("前端");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> categoryService.updateCategory(request));
            assertEquals("分类名称已存在", ex.getMessage());
        }

        @Test
        @DisplayName("更新分类名称为自身名称应成功（排除自身检查）")
        void updateCategory_SameName_ShouldSucceed() {
            // 同名检查时应排除自身ID
            when(categoryMapper.selectCount(any())).thenReturn(0L);
            when(categoryMapper.selectById(2L)).thenReturn(backendCategory);
            when(categoryMapper.updateById(any(Category.class))).thenReturn(1);

            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setId(2L);
            request.setName("后端");

            assertTrue(categoryService.updateCategory(request));
        }

        @Test
        @DisplayName("不能将分类设为自身的子分类（循环引用）")
        void updateCategory_SetSelfAsChild_ShouldThrowException() {
            when(categoryMapper.selectCount(any())).thenReturn(0L);
            // parentId=2（自身ID）时，validateParentId 应拦截
            when(categoryMapper.selectById(2L)).thenReturn(backendCategory);

            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setId(2L);
            request.setName("后端");
            request.setParentId(2L);  // 设置自身为父分类

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> categoryService.updateCategory(request));
            assertEquals("不能将分类设置为自己的子分类", ex.getMessage());
        }

        @Test
        @DisplayName("不能将分类移动到其子分类下（循环引用）")
        void updateCategory_MoveToDescendant_ShouldThrowException() {
            // 技术想移动到 Java(3) 下面
            // Java 是技术的后代，应被拦截
            when(categoryMapper.selectCount(any())).thenReturn(0L);
            when(categoryMapper.selectById(3L)).thenReturn(javaCategory);
            // getDescendantIds 查询技术的后代
            when(categoryMapper.selectList(argThat(w -> {
                if (!(w instanceof LambdaQueryWrapper)) return false;
                return true;
            }))).thenReturn(
                    Arrays.asList(backendCategory, frontendCategory), // parentId=1 的子分类
                    Collections.singletonList(javaCategory),         // parentId=2 的子分类
                    Collections.singletonList(springCategory),       // parentId=3 的子分类
                    Collections.emptyList()
            );

            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setId(1L);
            request.setName("技术");
            request.setParentId(3L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> categoryService.updateCategory(request));
            assertEquals("不能将分类移动到其子分类下", ex.getMessage());
        }

        @Test
        @DisplayName("移动分类导致层级超过4级应抛异常")
        void updateCategory_MoveExceedDepth_ShouldThrowException() {
            // 将前端(6, 当前层级2)移到Spring(4, 层级4)下 → 层级5，超限
            when(categoryMapper.selectCount(any())).thenReturn(0L);
            when(categoryMapper.selectById(4L)).thenReturn(springCategory);
            when(categoryMapper.selectById(3L)).thenReturn(javaCategory);
            when(categoryMapper.selectById(2L)).thenReturn(backendCategory);
            when(categoryMapper.selectById(1L)).thenReturn(techCategory);
            // getDescendantIds for id=6: 无子分类
            when(categoryMapper.selectList(any())).thenReturn(Collections.emptyList());

            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setId(6L);
            request.setName("前端");
            request.setParentId(4L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> categoryService.updateCategory(request));
            assertEquals("分类层级深度不能超过 4 级", ex.getMessage());
        }

        @Test
        @DisplayName("更新不存在的分类应抛异常")
        void updateCategory_NotExist_ShouldThrowException() {
            when(categoryMapper.selectCount(any())).thenReturn(0L);
            when(categoryMapper.selectById(999L)).thenReturn(null);

            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setId(999L);
            request.setName("不存在");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> categoryService.updateCategory(request));
            assertEquals("分类不存在", ex.getMessage());
        }
    }

    // ==================== 删除分类测试 ====================

    @Nested
    @DisplayName("删除分类")
    class DeleteTests {

        @Test
        @DisplayName("删除叶子分类（无子分类无文章）成功")
        void deleteCategory_LeafNoPosts_ShouldSucceed() {
            when(categoryMapper.selectCount(argThat(w ->
                    w instanceof LambdaQueryWrapper
            ))).thenReturn(0L);  // 无子分类
            when(postMapper.countByCategoryId(4L)).thenReturn(0L);  // 无文章
            when(categoryMapper.deleteById(4L)).thenReturn(1);

            assertTrue(categoryService.deleteCategory(4L));
            verify(categoryMapper).deleteById(4L);
        }

        @Test
        @DisplayName("有子分类时应阻止删除")
        void deleteCategory_HasChildren_ShouldThrowException() {
            when(categoryMapper.selectCount(any())).thenReturn(1L);  // 有子分类

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> categoryService.deleteCategory(1L));
            assertEquals("该分类下有子分类，请先删除或移动子分类", ex.getMessage());
        }

        @Test
        @DisplayName("有关联文章时应阻止删除")
        void deleteCategory_HasPosts_ShouldThrowException() {
            when(categoryMapper.selectCount(any())).thenReturn(0L);  // 无子分类
            when(postMapper.countByCategoryId(1L)).thenReturn(5L);  // 有5篇文章

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> categoryService.deleteCategory(1L));
            assertEquals("该分类下有 5 篇文章，请先移动文章", ex.getMessage());
        }
    }

    // ==================== 辅助方法测试 ====================

    @Nested
    @DisplayName("辅助查询")
    class HelperTests {

        @Test
        @DisplayName("getByIdOrThrow 存在时返回分类")
        void getByIdOrThrow_Exists_ShouldReturn() {
            when(categoryMapper.selectById(1L)).thenReturn(techCategory);

            Category result = categoryService.getByIdOrThrow(1L);

            assertNotNull(result);
            assertEquals("技术", result.getName());
        }

        @Test
        @DisplayName("getByIdOrThrow 不存在时抛异常")
        void getByIdOrThrow_NotExist_ShouldThrow() {
            when(categoryMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class,
                    () -> categoryService.getByIdOrThrow(999L));
        }

        @Test
        @DisplayName("hasChildren 有子分类返回true")
        void hasChildren_True() {
            when(categoryMapper.selectCount(any())).thenReturn(2L);

            assertTrue(categoryService.hasChildren(1L));
        }

        @Test
        @DisplayName("hasChildren 无子分类返回false")
        void hasChildren_False() {
            when(categoryMapper.selectCount(any())).thenReturn(0L);

            assertFalse(categoryService.hasChildren(4L));
        }

        @Test
        @DisplayName("getTotalPostCount 应包含所有子分类的文章")
        void getTotalPostCount_ShouldIncludeAllChildren() {
            // 技术(1) → 后端(2) → Java(3)
            when(categoryMapper.selectList(any())).thenReturn(
                    Arrays.asList(backendCategory, frontendCategory), // parentId=1 的子
                    Collections.singletonList(javaCategory),          // parentId=2 的子
                    Collections.emptyList()                            // parentId=3 的子
            );
            when(postMapper.countByCategoryIds(Arrays.asList(1L, 2L, 3L, 6L))).thenReturn(15L);

            Long total = categoryService.getTotalPostCount(1L);

            assertEquals(15L, total);
        }
    }
}

package site.dengwei.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.dengwei.blog.dto.CategoryTreeDTO;
import site.dengwei.blog.dto.CategoryWithCountDTO;
import site.dengwei.blog.dto.request.CreateCategoryRequest;
import site.dengwei.blog.dto.request.UpdateCategoryRequest;
import site.dengwei.blog.entity.Category;
import site.dengwei.blog.service.CategoryService;
import site.dengwei.blog.util.LambdaQueryUtils;
import site.dengwei.blog.dto.Response;

import java.util.List;

/**
 * 分类控制器
 *
 * @author dengwei
 * @since 2025-09-08 11:56:23
 */
@Tag(name = "Category", description = "文章分类 — 多级树形分类、CRUD、文章查询")
@RestController
@RequestMapping("category")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;

    /**
     * 获取所有分类（带文章数量）- 平铺列表
     */
    @Operation(summary = "所有分类列表", description = "获取所有分类的平铺列表，附带每类文章数量")
    @GetMapping("/all")
    public Response<List<CategoryWithCountDTO>> getAllWithCount() {
        return Response.success(categoryService.getAllCategoriesWithCount());
    }

    /**
     * 获取树形分类（前台用，带文章数量和汇总）
     */
    @Operation(summary = "树形分类（前台）", description = "获取树形结构分类，仅包含有已发布文章的分类")
    @GetMapping("/tree")
    public Response<List<CategoryTreeDTO>> getTree() {
        return Response.success(categoryService.getCategoryTree());
    }

    /**
     * 获取树形分类（管理后台用）
     */
    @Operation(summary = "树形分类（后台）", description = "获取完整的树形结构分类，包含所有状态")
    @GetMapping("/tree/admin")
    public Response<List<CategoryTreeDTO>> getTreeForAdmin() {
        return Response.success(categoryService.getCategoryTreeForAdmin());
    }

    /**
     * 获取分类祖先链路（面包屑用）
     */
    @Operation(summary = "分类祖先链路", description = "获取从根分类到当前分类的路径，用于面包屑导航")
    @GetMapping("/{id}/ancestors")
    public Response<List<Category>> getAncestors(@PathVariable Long id) {
        return Response.success(categoryService.getCategoryAncestors(id));
    }

    /**
     * 分页查询所有分类
     *
     * @param page 分页对象
     * @param category 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分类列表", description = "分页查询分类列表（管理后台）")
    @GetMapping
    public Response<Page<Category>> selectAll(Page<Category> page, Category category) {
        return Response.success(categoryService.page(page, LambdaQueryUtils.buildFromEntity(category)));
    }

    /**
     * 根据ID查询分类
     *
     * @param id 分类ID
     * @return 分类信息
     */
    @Operation(summary = "分类详情", description = "根据分类ID获取分类信息")
    @GetMapping("{id}")
    public Response<Category> selectOne(@PathVariable Long id) {
        return Response.success(categoryService.getByIdOrThrow(id));
    }

    /**
     * 创建分类
     *
     * @param request 创建请求
     * @return 新创建的分类ID
     */
    @Operation(summary = "创建分类", description = "创建新的文章分类（支持多级）")
    @PostMapping
    public Response<Long> insert(@Valid @RequestBody CreateCategoryRequest request) {
        return Response.success(categoryService.createCategory(request));
    }

    /**
     * 更新分类
     *
     * @param request 更新请求
     * @return 操作结果
     */
    @Operation(summary = "更新分类", description = "更新已有分类的名称、排序等信息")
    @PutMapping
    public Response<Boolean> update(@Valid @RequestBody UpdateCategoryRequest request) {
        return Response.success(categoryService.updateCategory(request));
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 操作结果
     */
    @Operation(summary = "删除分类", description = "删除分类（如果分类下有文章则无法删除）")
    @DeleteMapping("{id}")
    public Response<Boolean> delete(@PathVariable Long id) {
        return Response.success(categoryService.deleteCategory(id));
    }
}

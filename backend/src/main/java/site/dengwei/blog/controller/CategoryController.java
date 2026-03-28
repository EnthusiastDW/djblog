package site.dengwei.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
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
@RestController
@RequestMapping("category")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;

    @GetMapping("/all")
    public Response<List<CategoryWithCountDTO>> getAllWithCount() {
        return Response.success(categoryService.getAllCategoriesWithCount());
    }

    /**
     * 分页查询所有分类
     *
     * @param page 分页对象
     * @param category 查询条件
     * @return 分页结果
     */
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
    @DeleteMapping("{id}")
    public Response<Boolean> delete(@PathVariable Long id) {
        return Response.success(categoryService.deleteCategory(id));
    }
}

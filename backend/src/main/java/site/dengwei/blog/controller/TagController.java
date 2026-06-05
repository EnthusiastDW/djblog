package site.dengwei.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.dengwei.blog.dto.TagWithCountDTO;
import site.dengwei.blog.dto.request.CreateTagRequest;
import site.dengwei.blog.dto.request.UpdateTagRequest;
import site.dengwei.blog.entity.Tag;
import site.dengwei.blog.service.TagService;
import site.dengwei.blog.util.LambdaQueryUtils;
import site.dengwei.blog.dto.Response;

import java.util.List;

/**
 * 标签控制器
 *
 * @author dengwei
 * @since 2025-09-08 11:56:28
 */
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tag", description = "文章标签 — 标签 CRUD、标签云、标签下文章查询")
@RestController
@RequestMapping("tag")
@RequiredArgsConstructor
public class TagController {
    
    private final TagService tagService;

    @Operation(summary = "所有标签列表", description = "获取所有标签及其关联的文章数量")
    @GetMapping("/all")
    public Response<List<TagWithCountDTO>> getAllWithCount() {
        return Response.success(tagService.getAllTagsWithCount());
    }

    /**
     * 分页查询所有标签
     *
     * @param page 分页对象
     * @param tag 查询条件
     * @return 分页结果
     */
    @Operation(summary = "标签列表", description = "分页查询标签列表")
    @GetMapping
    public Response<Page<Tag>> selectAll(Page<Tag> page, Tag tag) {
        return Response.success(tagService.page(page, LambdaQueryUtils.buildFromEntity(tag)));
    }

    /**
     * 根据ID查询标签
     *
     * @param id 标签ID
     * @return 标签信息
     */
    @Operation(summary = "标签详情", description = "根据标签ID获取标签信息")
    @GetMapping("{id}")
    public Response<Tag> selectOne(@PathVariable Long id) {
        return Response.success(tagService.getByIdOrThrow(id));
    }

    /**
     * 创建标签
     *
     * @param request 创建请求
     * @return 新创建的标签ID
     */
    @Operation(summary = "创建标签", description = "创建新的文章标签")
    @PostMapping
    public Response<Long> insert(@Valid @RequestBody CreateTagRequest request) {
        return Response.success(tagService.createTag(request));
    }

    /**
     * 更新标签
     *
     * @param request 更新请求
     * @return 操作结果
     */
    @Operation(summary = "更新标签", description = "更新已有标签的名称等信息")
    @PutMapping
    public Response<Boolean> update(@Valid @RequestBody UpdateTagRequest request) {
        return Response.success(tagService.updateTag(request));
    }

    /**
     * 删除标签
     *
     * @param id 标签ID
     * @return 操作结果
     */
    @Operation(summary = "删除标签", description = "删除标签（如果标签下有文章则无法删除）")
    @DeleteMapping("{id}")
    public Response<Boolean> delete(@PathVariable Long id) {
        return Response.success(tagService.deleteTag(id));
    }
}

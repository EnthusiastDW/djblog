package site.dengwei.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
@RestController
@RequestMapping("tag")
@RequiredArgsConstructor
public class TagController {
    
    private final TagService tagService;

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
    @DeleteMapping("{id}")
    public Response<Boolean> delete(@PathVariable Long id) {
        return Response.success(tagService.deleteTag(id));
    }
}

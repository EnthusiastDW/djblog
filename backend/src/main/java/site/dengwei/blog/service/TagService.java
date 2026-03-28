package site.dengwei.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.dengwei.blog.dto.TagWithCountDTO;
import site.dengwei.blog.dto.request.CreateTagRequest;
import site.dengwei.blog.dto.request.UpdateTagRequest;
import site.dengwei.blog.entity.Tag;

import java.util.List;

/**
 * 标签服务接口
 *
 * @author dengwei
 * @since 2025-09-08 11:33:26
 */
public interface TagService extends IService<Tag> {

    /**
     * 获取所有标签
     *
     * @return 标签列表
     */
    List<Tag> getAllTags();

    /**
     * 获取所有标签（带文章数量）
     *
     * @return 标签列表
     */
    List<TagWithCountDTO> getAllTagsWithCount();

    /**
     * 根据ID查询标签，不存在则抛出异常
     *
     * @param id 标签ID
     * @return 标签实体
     */
    Tag getByIdOrThrow(Long id);

    /**
     * 创建标签
     *
     * @param request 创建请求
     * @return 新创建的标签ID
     */
    Long createTag(CreateTagRequest request);

    /**
     * 更新标签
     *
     * @param request 更新请求
     * @return 是否成功
     */
    boolean updateTag(UpdateTagRequest request);

    /**
     * 删除标签
     *
     * @param id 标签ID
     * @return 是否成功
     */
    boolean deleteTag(Long id);
}

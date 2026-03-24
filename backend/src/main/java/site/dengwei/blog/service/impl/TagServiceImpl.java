package site.dengwei.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import site.dengwei.blog.dto.request.CreateTagRequest;
import site.dengwei.blog.dto.request.UpdateTagRequest;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.mapper.TagMapper;
import site.dengwei.blog.entity.Tag;
import site.dengwei.blog.service.TagService;

import java.util.List;

import static site.dengwei.utils.SlugUtils.generateSlug;

/**
 * 标签服务实现类
 *
 * @author dengwei
 * @since 2025-09-08 11:33:27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    @Cacheable(value = "tags", key = "'all'")
    public List<Tag> getAllTags() {
        return baseMapper.selectList(null);
    }

    @Override
    public Tag getByIdOrThrow(Long id) {
        Tag tag = getById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        return tag;
    }

    @Override
    @CacheEvict(value = "tags", key = "'all'")
    public boolean createTag(CreateTagRequest request) {
        String slug = processSlug(request.getName(), request.getSlug());

        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setSlug(slug);

        log.info("创建标签: {}", request.getName());
        return save(tag);
    }

    @Override
    @CacheEvict(value = "tags", key = "'all'")
    public boolean updateTag(UpdateTagRequest request) {
        String slug = processSlug(request.getName(), request.getSlug());

        Tag tag = getByIdOrThrow(request.getId());
        tag.setName(request.getName());
        tag.setSlug(slug);

        log.info("更新标签，ID: {}", request.getId());
        return updateById(tag);
    }

    @Override
    @CacheEvict(value = "tags", key = "'all'")
    public boolean deleteTag(Long id) {
        log.info("删除标签，ID: {}", id);
        return removeById(id);
    }

    /**
     * 处理slug生成和校验
     */
    private String processSlug(String name, String slug) {
        if (StringUtils.hasText(slug)) {
            return slug;
        }
        return generateSlug(name);
    }
}

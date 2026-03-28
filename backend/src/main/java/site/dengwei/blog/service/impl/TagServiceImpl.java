package site.dengwei.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import site.dengwei.blog.dto.TagWithCountDTO;
import site.dengwei.blog.dto.request.CreateTagRequest;
import site.dengwei.blog.dto.request.UpdateTagRequest;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.mapper.TagMapper;
import site.dengwei.blog.mapper.PostMapper;
import site.dengwei.blog.entity.Tag;
import site.dengwei.blog.service.TagService;

import java.util.List;

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

    private final PostMapper postMapper;

    @Override
    @Cacheable(value = "tags", key = "'all'")
    public List<Tag> getAllTags() {
        return baseMapper.selectList(null);
    }

    @Override
    public List<TagWithCountDTO> getAllTagsWithCount() {
        List<Tag> tags = baseMapper.selectList(null);
        return tags.stream().map(tag -> {
            TagWithCountDTO dto = new TagWithCountDTO();
            dto.setId(tag.getId());
            dto.setName(tag.getName());
            dto.setPostCount(postMapper.countByTagId(tag.getId()));
            return dto;
        }).toList();
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
    public Long createTag(CreateTagRequest request) {
        // 检查标签名称是否已存在
        List<Tag> existingTags = baseMapper.selectList(
            new LambdaQueryWrapper<Tag>().eq(Tag::getName, request.getName())
        );
        if (!existingTags.isEmpty()) {
            throw new BusinessException("标签名称已存在");
        }
    
        Tag tag = new Tag();
        tag.setName(request.getName());
    
        save(tag);
        log.info("创建标签：{}, ID: {}", request.getName(), tag.getId());
        return tag.getId();
    }

    @Override
    @CacheEvict(value = "tags", key = "'all'")
    public boolean updateTag(UpdateTagRequest request) {
        Tag tag = getByIdOrThrow(request.getId());
        tag.setName(request.getName());

        log.info("更新标签，ID: {}", request.getId());
        return updateById(tag);
    }

    @Override
    @CacheEvict(value = "tags", key = "'all'")
    public boolean deleteTag(Long id) {
        log.info("删除标签，ID: {}", id);
        return removeById(id);
    }
}

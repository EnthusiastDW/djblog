package site.dengwei.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import site.dengwei.blog.dto.CategoryWithCountDTO;
import site.dengwei.blog.dto.request.CreateCategoryRequest;
import site.dengwei.blog.dto.request.UpdateCategoryRequest;
import site.dengwei.blog.entity.Category;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.mapper.CategoryMapper;
import site.dengwei.blog.mapper.PostMapper;
import site.dengwei.blog.service.CategoryService;

import java.util.List;

/**
 * 分类服务实现类
 *
 * @author dengwei
 * @since 2025-09-08 11:33:27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final PostMapper postMapper;

    @Override
    @Cacheable(value = "categories", key = "'all'")
    public List<Category> getAllCategories() {
        return baseMapper.selectList(null);
    }

    @Override
    public List<CategoryWithCountDTO> getAllCategoriesWithCount() {
        List<Category> categories = baseMapper.selectList(null);
        List<CategoryWithCountDTO> result = categories.stream().map(cat -> {
            CategoryWithCountDTO dto = new CategoryWithCountDTO();
            dto.setId(cat.getId());
            dto.setName(cat.getName());
            dto.setDescription(cat.getDescription());
            dto.setParentId(cat.getParentId());
            dto.setPostCount(postMapper.countByCategoryId(cat.getId()));
            return dto;
        }).sorted((a, b) -> Long.compare(b.getPostCount() != null ? b.getPostCount() : 0L,
                a.getPostCount() != null ? a.getPostCount() : 0L)).toList();
        return result;
    }

    @Override
    public Category getByIdOrThrow(Long id) {
        Category category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        return category;
    }

    @Override
    @CacheEvict(value = "categories", key = "'all'")
    public Long createCategory(CreateCategoryRequest request) {
        // 检查分类名称是否已存在
        List<Category> existingCategories = baseMapper.selectList(
                new LambdaQueryWrapper<Category>().eq(Category::getName, request.getName())
        );
        if (!existingCategories.isEmpty()) {
            throw new BusinessException("分类名称已存在");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setParentId(request.getParentId());

        save(category);
        log.info("创建分类：{}, ID: {}", request.getName(), category.getId());
        return category.getId();
    }

    @Override
    @CacheEvict(value = "categories", key = "'all'")
    public boolean updateCategory(UpdateCategoryRequest request) {
        Category category = getByIdOrThrow(request.getId());
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setParentId(request.getParentId());

        log.info("更新分类，ID: {}", request.getId());
        return updateById(category);
    }

    @Override
    @CacheEvict(value = "categories", key = "'all'")
    public boolean deleteCategory(Long id) {
        log.info("删除分类，ID: {}", id);
        return removeById(id);
    }
}

package site.dengwei.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import site.dengwei.blog.dto.request.CreateCategoryRequest;
import site.dengwei.blog.dto.request.UpdateCategoryRequest;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.mapper.CategoryMapper;
import site.dengwei.blog.entity.Category;
import site.dengwei.blog.service.CategoryService;

import java.util.List;

import static site.dengwei.utils.SlugUtils.generateSlug;

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

    @Override
    @Cacheable(value = "categories", key = "'all'")
    public List<Category> getAllCategories() {
        return baseMapper.selectList(null);
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
    public boolean createCategory(CreateCategoryRequest request) {
        // 生成或校验slug
        String slug = processSlug(request.getName(), request.getSlug());
        
        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(slug);
        category.setDescription(request.getDescription());
        category.setParentId(request.getParentId());
        
        log.info("创建分类: {}", request.getName());
        return save(category);
    }

    @Override
    @CacheEvict(value = "categories", key = "'all'")
    public boolean updateCategory(UpdateCategoryRequest request) {
        // 生成或校验slug
        String slug = processSlug(request.getName(), request.getSlug());
        
        Category category = getByIdOrThrow(request.getId());
        category.setName(request.getName());
        category.setSlug(slug);
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

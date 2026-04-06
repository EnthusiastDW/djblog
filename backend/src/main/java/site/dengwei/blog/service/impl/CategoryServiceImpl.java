package site.dengwei.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import site.dengwei.blog.dto.CategoryTreeDTO;
import site.dengwei.blog.dto.CategoryWithCountDTO;
import site.dengwei.blog.dto.request.CreateCategoryRequest;
import site.dengwei.blog.dto.request.UpdateCategoryRequest;
import site.dengwei.blog.entity.Category;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.mapper.CategoryMapper;
import site.dengwei.blog.mapper.PostMapper;
import site.dengwei.blog.service.CategoryService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * 分类层级深度上限
     */
    private static final int MAX_DEPTH = 4;

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
    @Cacheable(value = "categories", key = "'tree'")
    public List<CategoryTreeDTO> getCategoryTree() {
        List<Category> categories = baseMapper.selectList(null);
        return buildTree(categories, true);
    }

    @Override
    @Cacheable(value = "categories", key = "'treeAdmin'")
    public List<CategoryTreeDTO> getCategoryTreeForAdmin() {
        List<Category> categories = baseMapper.selectList(null);
        return buildTree(categories, true);
    }

    @Override
    public List<Category> getCategoryAncestors(Long categoryId) {
        List<Category> ancestors = new ArrayList<>();
        Category current = getById(categoryId);
        if (current == null) {
            return ancestors;
        }

        // 从当前分类开始，向上追溯到顶级分类
        ancestors.add(current);
        while (current.getParentId() != null) {
            Category parent = getById(current.getParentId());
            if (parent == null) {
                break;
            }
            ancestors.add(parent);
            current = parent;
        }

        // 反转列表，使顶级分类在最前面（面包屑顺序：顶级 > ... > 当前）
        java.util.Collections.reverse(ancestors);
        return ancestors;
    }

    @Override
    public List<Long> getDescendantIds(Long categoryId) {
        List<Long> descendantIds = new ArrayList<>();
        descendantIds.add(categoryId);
        collectDescendantIds(categoryId, descendantIds);
        return descendantIds;
    }

    @Override
    public Long getTotalPostCount(Long categoryId) {
        List<Long> descendantIds = getDescendantIds(categoryId);
        return postMapper.countByCategoryIds(descendantIds);
    }

    @Override
    public boolean hasChildren(Long categoryId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, categoryId);
        return baseMapper.selectCount(wrapper) > 0;
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
    @CacheEvict(value = "categories", allEntries = true)
    public Long createCategory(CreateCategoryRequest request) {
        checkCategoryNameExists(request.getName(), null);

        // 校验父分类
        if (request.getParentId() != null) {
            validateParentId(request.getParentId(), null);
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setParentId(request.getParentId());

        save(category);
        log.info("创建分类：{}, ID: {}, 父级: {}", request.getName(), category.getId(), request.getParentId());
        return category.getId();
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public boolean updateCategory(UpdateCategoryRequest request) {
        checkCategoryNameExists(request.getName(), request.getId());

        // 校验父分类（排除自身及其后代）
        if (request.getParentId() != null) {
            validateParentId(request.getParentId(), request.getId());
        }

        Category category = getByIdOrThrow(request.getId());
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setParentId(request.getParentId());

        log.info("更新分类，ID: {}, 父级: {}", request.getId(), request.getParentId());
        return updateById(category);
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public boolean deleteCategory(Long id) {
        // 安全校验：检查是否有子分类
        if (hasChildren(id)) {
            throw new BusinessException("该分类下有子分类，请先删除或移动子分类");
        }

        // 安全校验：检查是否有文章
        Long postCount = postMapper.countByCategoryId(id);
        if (postCount != null && postCount > 0) {
            throw new BusinessException("该分类下有 " + postCount + " 篇文章，请先移动文章");
        }

        log.info("删除分类，ID: {}", id);
        return removeById(id);
    }

    @Override
    public int getCategoryDepth(Long categoryId) {
        int depth = 1;
        Category current = getById(categoryId);
        if (current == null) {
            return 0;
        }
        while (current.getParentId() != null) {
            depth++;
            Category parent = getById(current.getParentId());
            if (parent == null) {
                break;
            }
            current = parent;
        }
        return depth;
    }

    /**
     * 检查分类名称是否已存在（全局唯一）
     */
    private void checkCategoryNameExists(String name, Long excludeId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, name);
        if (excludeId != null) {
            wrapper.ne(Category::getId, excludeId);
        }
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("分类名称已存在");
        }
    }

    /**
     * 校验父分类ID的有效性
     * 1. 父分类必须存在
     * 2. 父分类不能是自身（编辑时）
     * 3. 父分类不能是自身的后代（防止循环引用）
     * 4. 设置后的层级深度不能超过 MAX_DEPTH
     *
     * @param parentId 父分类ID
     * @param currentId 当前分类ID（编辑时传入，创建时为null）
     */
    private void validateParentId(Long parentId, Long currentId) {
        Category parent = getById(parentId);
        if (parent == null) {
            throw new BusinessException("父分类不存在");
        }

        // 不能设置为自身
        if (currentId != null && parentId.equals(currentId)) {
            throw new BusinessException("不能将分类设置为自己的子分类");
        }

        // 父分类不能是自身的后代（防止循环引用）
        if (currentId != null) {
            List<Long> descendantIds = getDescendantIds(currentId);
            if (descendantIds.contains(parentId)) {
                throw new BusinessException("不能将分类移动到其子分类下");
            }
        }

        // 检查层级深度
        int parentDepth = getCategoryDepth(parentId);
        if (parentDepth >= MAX_DEPTH) {
            throw new BusinessException("分类层级深度不能超过 " + MAX_DEPTH + " 级");
        }
    }

    /**
     * 递归收集后代分类ID
     */
    private void collectDescendantIds(Long parentId, List<Long> result) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, parentId);
        List<Category> children = baseMapper.selectList(wrapper);
        for (Category child : children) {
            result.add(child.getId());
            collectDescendantIds(child.getId(), result);
        }
    }

    /**
     * 构建树形结构
     *
     * @param categories 所有分类列表
     * @param withCount 是否带文章数量
     * @return 树形分类列表
     */
    private List<CategoryTreeDTO> buildTree(List<Category> categories, boolean withCount) {
        // 构建 id -> 文章数量 的映射
        final Map<Long, Long> postCountMap = withCount
                ? categories.stream().collect(Collectors.toMap(Category::getId,
                        cat -> postMapper.countByCategoryId(cat.getId())))
                : Collections.emptyMap();

        // 转换为 DTO
        List<CategoryTreeDTO> allNodes = categories.stream().map(cat -> {
            CategoryTreeDTO dto = new CategoryTreeDTO();
            dto.setId(cat.getId());
            dto.setName(cat.getName());
            dto.setDescription(cat.getDescription());
            dto.setParentId(cat.getParentId());
            if (withCount) {
                dto.setPostCount(postCountMap.getOrDefault(cat.getId(), 0L));
            }
            return dto;
        }).toList();

        // 按 parentId 分组
        Map<Long, List<CategoryTreeDTO>> childrenMap = allNodes.stream()
                .filter(node -> node.getParentId() != null)
                .collect(Collectors.groupingBy(CategoryTreeDTO::getParentId));

        // 找出顶级分类
        List<CategoryTreeDTO> roots = allNodes.stream()
                .filter(node -> node.getParentId() == null)
                .collect(Collectors.toList());

        // 递归构建树并计算 totalPostCount
        for (CategoryTreeDTO root : roots) {
            buildChildren(root, childrenMap);
            if (withCount) {
                calculateTotalPostCount(root);
            }
        }

        // 按文章数量降序排序（顶级和子分类都排序）
        roots.sort(Comparator.comparingLong(
                (CategoryTreeDTO node) -> node.getTotalPostCount() != null ? node.getTotalPostCount() : 0L
        ).reversed());

        return roots;
    }

    /**
     * 递归设置子分类
     */
    private void buildChildren(CategoryTreeDTO parent, Map<Long, List<CategoryTreeDTO>> childrenMap) {
        List<CategoryTreeDTO> children = childrenMap.get(parent.getId());
        if (children != null && !children.isEmpty()) {
            parent.setChildren(children);
            for (CategoryTreeDTO child : children) {
                buildChildren(child, childrenMap);
            }
        }
    }

    /**
     * 递归计算 totalPostCount（含子分类的文章数）
     */
    private void calculateTotalPostCount(CategoryTreeDTO node) {
        long total = node.getPostCount() != null ? node.getPostCount() : 0L;
        if (node.getChildren() != null) {
            for (CategoryTreeDTO child : node.getChildren()) {
                calculateTotalPostCount(child);
                total += child.getTotalPostCount() != null ? child.getTotalPostCount() : 0L;
            }
            // 子分类也按文章数量降序排序
            node.getChildren().sort(Comparator.comparingLong(
                    (CategoryTreeDTO n) -> n.getTotalPostCount() != null ? n.getTotalPostCount() : 0L
            ).reversed());
        }
        node.setTotalPostCount(total);
    }
}

package site.dengwei.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.dengwei.blog.dto.CategoryTreeDTO;
import site.dengwei.blog.dto.CategoryWithCountDTO;
import site.dengwei.blog.dto.request.CreateCategoryRequest;
import site.dengwei.blog.dto.request.UpdateCategoryRequest;
import site.dengwei.blog.entity.Category;

import java.util.List;

/**
 * 分类服务接口
 *
 * @author dengwei
 * @since 2025-09-08 11:33:26
 */
public interface CategoryService extends IService<Category> {

    /**
     * 获取所有分类
     *
     * @return 分类列表
     */
    List<Category> getAllCategories();

    /**
     * 获取所有分类（带文章数量）
     *
     * @return 分类列表
     */
    List<CategoryWithCountDTO> getAllCategoriesWithCount();

    /**
     * 获取树形分类列表（前台用，带文章数量，按文章数量降序）
     *
     * @return 树形分类列表
     */
    List<CategoryTreeDTO> getCategoryTree();

    /**
     * 获取树形分类列表（管理后台用，无文章数量限制，一次全量加载，按文章数量降序）
     *
     * @return 树形分类列表
     */
    List<CategoryTreeDTO> getCategoryTreeForAdmin();

    /**
     * 获取分类的祖先链路（用于面包屑，如：技术 > 后端 > Java）
     * 从当前分类开始，向上追溯到顶级分类
     *
     * @param categoryId 分类ID
     * @return 祖先分类列表（从顶级到当前）
     */
    List<Category> getCategoryAncestors(Long categoryId);

    /**
     * 获取分类的所有后代 ID（用于删除校验和文章查询）
     *
     * @param categoryId 分类ID
     * @return 后代分类ID列表（包含自身）
     */
    List<Long> getDescendantIds(Long categoryId);

    /**
     * 获取分类及其子分类的文章总数
     *
     * @param categoryId 分类ID
     * @return 文章总数
     */
    Long getTotalPostCount(Long categoryId);

    /**
     * 检查分类是否有子分类
     *
     * @param categoryId 分类ID
     * @return 是否有子分类
     */
    boolean hasChildren(Long categoryId);

    /**
     * 根据ID查询分类，不存在则抛出异常
     *
     * @param id 分类ID
     * @return 分类实体
     */
    Category getByIdOrThrow(Long id);

    /**
     * 创建分类
     *
     * @param request 创建请求
     * @return 新创建的分类ID
     */
    Long createCategory(CreateCategoryRequest request);

    /**
     * 更新分类
     *
     * @param request 更新请求
     * @return 是否成功
     */
    boolean updateCategory(UpdateCategoryRequest request);

    /**
     * 删除分类（带安全校验）
     *
     * @param id 分类ID
     * @return 是否成功
     */
    boolean deleteCategory(Long id);

    /**
     * 获取分类的层级深度（顶级为1）
     *
     * @param categoryId 分类ID
     * @return 层级深度
     */
    int getCategoryDepth(Long categoryId);
}

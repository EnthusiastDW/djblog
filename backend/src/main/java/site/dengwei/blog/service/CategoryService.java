package site.dengwei.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
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
     * @return 是否成功
     */
    boolean createCategory(CreateCategoryRequest request);

    /**
     * 更新分类
     *
     * @param request 更新请求
     * @return 是否成功
     */
    boolean updateCategory(UpdateCategoryRequest request);

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 是否成功
     */
    boolean deleteCategory(Long id);
}

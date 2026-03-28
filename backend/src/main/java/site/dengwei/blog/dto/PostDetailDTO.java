package site.dengwei.blog.dto;

import lombok.Data;
import site.dengwei.blog.entity.Category;
import site.dengwei.blog.entity.Tag;
import site.dengwei.blog.entity.User;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 文章详情DTO，包含文章本身及其关联信息
 */
@Data
public class PostDetailDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String title;
    private String content;
    private String slug;
    private String coverImage;
    private String status;
    private Long authorId;
    private Long categoryId;
    private String categoryName;
    private Long viewCount;
    private Date publishedAt;
    private Date createdAt;
    private Date updatedAt;
    
    // 关联信息
    private User author;
    private Category category;
    private List<Tag> tags;
}
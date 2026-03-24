package site.dengwei.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文章-标签关联实体类
 *
 * @author dengwei
 * @since 2025-09-10
 */
@Data
@TableName(value = "post_tag")
public class PostTag implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    private Long postId;

    /**
     * 标签ID
     */
    private Long tagId;
}

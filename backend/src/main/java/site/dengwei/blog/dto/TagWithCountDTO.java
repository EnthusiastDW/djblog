package site.dengwei.blog.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TagWithCountDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    
    @TableField(exist = false)
    private Long postCount;
}
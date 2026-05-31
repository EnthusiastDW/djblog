package site.dengwei.blog.service.platform.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlatformSyncRequest {
    private Long postId;
    private String title;
    private String content;
    private String summary;
    private String slug;
    private List<String> tags;
    private String categoryName;
}

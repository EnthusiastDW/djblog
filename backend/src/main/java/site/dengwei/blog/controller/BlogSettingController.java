package site.dengwei.blog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.dengwei.blog.service.BlogSettingService;
import site.dengwei.blog.dto.Response;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Setting", description = "系统设置 — 博客背景图片、透明度等站点配置")
@RestController
@RequestMapping("setting")
@RequiredArgsConstructor
public class BlogSettingController {
    
    private final BlogSettingService blogSettingService;
    
    @Operation(summary = "获取系统设置", description = "获取博客的系统配置信息，如背景图片(支持多图轮播)、透明度等")
    @GetMapping
    public Response<Map<String, Object>> getSettings() {
        Map<String, Object> settings = new HashMap<>();
        String bgImage = blogSettingService.getValue("bg_image");
        settings.put("bgImage", bgImage != null ? bgImage : "");
        settings.put("bgOpacity", blogSettingService.getValue("bg_opacity") != null ? blogSettingService.getValue("bg_opacity") : "0.3");
        // 多图轮播支持
        String bgImagesJson = blogSettingService.getValue("bg_images");
        settings.put("bgImages", bgImagesJson != null ? bgImagesJson : "[]");
        settings.put("bgCarouselInterval", blogSettingService.getValue("bg_carousel_interval") != null ? blogSettingService.getValue("bg_carousel_interval") : "5");
        return Response.success(settings);
    }
    
    @Operation(summary = "更新系统设置", description = "更新博客的系统配置信息（需要登录）")
    @PutMapping
    public Response<Boolean> updateSetting(@RequestBody Map<String, String> settings) {
        if (settings.containsKey("bgImage")) {
            blogSettingService.setValue("bg_image", settings.get("bgImage"));
        }
        if (settings.containsKey("bgOpacity")) {
            blogSettingService.setValue("bg_opacity", settings.get("bgOpacity"));
        }
        // 多图轮播支持
        if (settings.containsKey("bgImages")) {
            blogSettingService.setValue("bg_images", settings.get("bgImages"));
        }
        if (settings.containsKey("bgCarouselInterval")) {
            blogSettingService.setValue("bg_carousel_interval", settings.get("bgCarouselInterval"));
        }
        return Response.success(true);
    }
}
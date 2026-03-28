package site.dengwei.blog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.dengwei.blog.service.BlogSettingService;
import site.dengwei.blog.dto.Response;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("setting")
@RequiredArgsConstructor
public class BlogSettingController {
    
    private final BlogSettingService blogSettingService;
    
    @GetMapping
    public Response<Map<String, String>> getSettings() {
        Map<String, String> settings = new HashMap<>();
        settings.put("bgImage", blogSettingService.getValue("bg_image") != null ? blogSettingService.getValue("bg_image") : "");
        settings.put("bgOpacity", blogSettingService.getValue("bg_opacity") != null ? blogSettingService.getValue("bg_opacity") : "0.3");
        return Response.success(settings);
    }
    
    @PutMapping
    public Response<Boolean> updateSetting(@RequestBody Map<String, String> settings) {
        if (settings.containsKey("bgImage")) {
            blogSettingService.setValue("bg_image", settings.get("bgImage"));
        }
        if (settings.containsKey("bgOpacity")) {
            blogSettingService.setValue("bg_opacity", settings.get("bgOpacity"));
        }
        return Response.success(true);
    }
}
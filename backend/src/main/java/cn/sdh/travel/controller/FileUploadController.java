package cn.sdh.travel.controller;

import cn.sdh.travel.common.result.Result;
import cn.sdh.travel.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final MinioService minioService;

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }

        try {
            String url = minioService.uploadFile(file, "guide");
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            return Result.success("上传成功", result);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}

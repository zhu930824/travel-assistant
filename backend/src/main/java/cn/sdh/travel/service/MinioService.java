package cn.sdh.travel.service;

import cn.sdh.travel.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    public String uploadFile(MultipartFile file, String folder) {
        try {
            String bucketName = minioConfig.getBucketName();

            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());

            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
                log.info("创建MinIO Bucket: {}", bucketName);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String objectName = folder + "/" + datePath + "/" + UUID.randomUUID() + extension;

            minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());

            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .method(Method.GET)
                .expiry(7, TimeUnit.DAYS)
                .build());

            if (url.contains("?")) {
                url = url.substring(0, url.indexOf("?"));
            }

            log.info("文件上传成功: {}", url);
            return url;

        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            String bucketName = minioConfig.getBucketName();
            String objectName = extractObjectName(fileUrl, bucketName);

            if (objectName != null) {
                minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
                log.info("文件删除成功: {}", objectName);
            }
        } catch (Exception e) {
            log.error("文件删除失败", e);
        }
    }

    private String extractObjectName(String fileUrl, String bucketName) {
        try {
            int bucketIndex = fileUrl.indexOf(bucketName);
            if (bucketIndex == -1) {
                return null;
            }
            return fileUrl.substring(bucketIndex + bucketName.length() + 1);
        } catch (Exception e) {
            return null;
        }
    }
}

package com.healthcare.service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Tencent COS 文件上传封装
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CosService implements DisposableBean {

    @Value("${tencent.cos.secret-id:}")
    private String secretId;

    @Value("${tencent.cos.secret-key:}")
    private String secretKey;

    @Value("${tencent.cos.region:ap-beijing}")
    private String region;

    @Value("${tencent.cos.bucket:}")
    private String bucket;

    @Value("${tencent.cos.public-domain:}")
    private String publicDomain;

    private COSClient client;

    private COSClient client() {
        if (client == null) {
            if (secretId == null || secretId.isBlank() || secretKey == null || secretKey.isBlank()
                    || bucket == null || bucket.isBlank() || publicDomain == null || publicDomain.isBlank()) {
                throw new RuntimeException("对象存储未配置（COS secret/bucket/domain）");
            }
            COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
            ClientConfig clientConfig = new ClientConfig(new Region(region));
            client = new COSClient(cred, clientConfig);
        }
        return client;
    }

    /**
     * 上传头像，返回公网可访问的 URL
     */
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        String original = file.getOriginalFilename();
        String suffix = "";
        if (original != null && original.contains(".")) {
            suffix = original.substring(original.lastIndexOf("."));
        }
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String key = "avatar/" + datePath + "/" + userId + "-" + UUID.randomUUID() + suffix;
        try {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(file.getSize());
            meta.setContentType(file.getContentType());
            PutObjectRequest req = new PutObjectRequest(bucket, key, file.getInputStream(), meta);
            client().putObject(req);
            String domain = publicDomain.endsWith("/") ? publicDomain.substring(0, publicDomain.length() - 1) : publicDomain;
            return domain + "/" + key;
        } catch (IOException | CosClientException e) {
            log.error("Upload avatar to COS failed", e);
            throw new RuntimeException("上传头像失败，请稍后重试");
        }
    }

    @Override
    public void destroy() {
        if (client != null) {
            client.shutdown();
        }
    }
}


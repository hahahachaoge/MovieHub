package com.movie.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.io.InputStream;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    private String appId;

    /**
     * 私钥内容（程序启动时从 privateKeyPath 文件加载）
     */
    private String privateKey;

    /**
     * 支付宝公钥内容（程序启动时从 publicKeyPath 文件加载）
     */
    private String alipayPublicKey;

    /**
     * 私钥文件路径（支持 classpath: 和 file: 前缀）
     */
    @Value("${alipay.private-key-path:file:./certs/alipay-private-key.pem}")
    private Resource privateKeyPath;

    /**
     * 支付宝公钥文件路径
     */
    @Value("${alipay.public-key-path:file:./certs/alipay-public-key.pem}")
    private Resource publicKeyPath;

    private String notifyUrl;
    private String returnUrl;
    private String gatewayUrl;

    @PostConstruct
    public void init() {
        try {
            // 从 PEM 文件加载私钥
            if (privateKeyPath != null && privateKeyPath.exists()) {
                try (InputStream is = privateKeyPath.getInputStream()) {
                    byte[] bytes = is.readAllBytes();
                    this.privateKey = new String(bytes, StandardCharsets.UTF_8).trim();
                    log.info("支付宝私钥已从文件加载: {}", privateKeyPath.getFilename());
                }
            } else {
                log.warn("支付宝私钥文件不存在: {}", privateKeyPath);
            }

            // 从 PEM 文件加载支付宝公钥
            if (publicKeyPath != null && publicKeyPath.exists()) {
                try (InputStream is = publicKeyPath.getInputStream()) {
                    byte[] bytes = is.readAllBytes();
                    this.alipayPublicKey = new String(bytes, StandardCharsets.UTF_8).trim();
                    log.info("支付宝公钥已从文件加载: {}", publicKeyPath.getFilename());
                }
            } else {
                log.warn("支付宝公钥文件不存在: {}", publicKeyPath);
            }
        } catch (Exception e) {
            log.error("加载支付宝密钥文件失败: {}", e.getMessage());
        }
    }
}

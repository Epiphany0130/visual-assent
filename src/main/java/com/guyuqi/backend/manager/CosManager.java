package com.guyuqi.backend.manager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.XML;
import com.guyuqi.backend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.image.ImageLabelRequest;
import com.qcloud.cos.model.ciModel.image.ImageLabelResponse;
import com.qcloud.cos.model.ciModel.image.Label;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CosManager {  
  
    @Resource
    private CosClientConfig cosClientConfig;
  
    @Resource  
    private COSClient cosClient;
  
    // ... 一些操作 COS 的方法
    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传对象（附带图片信息）
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        // 对图片进行处理（获取基本信息也被视作为一种处理）
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图信息
        picOperations.setIsPicInfo(1);
        // 2 图片压缩（转成 WebP）
        // 图片处理规则列表
        ArrayList<PicOperations.Rule> rules = new ArrayList<>();
        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setFileId(webpKey);
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setRule("imageMogr2/format/webp");
        rules.add(compressRule);
        // 缩略图处理，仅对 >20KB 的图片生成缩略图
        if (file.length() > 2 * 1024) {
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            // 缩放规则 /thumbnail/<Width>×<Height>>（如果大于原图宽高，则不处理）
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%dx%d>", 128, 128));
            rules.add(thumbnailRule);
        }
        // 构造处理参数
        // 把列表设置给请求
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 删除对象
     *
     * @param key 文件 key
     */
    public void deleteObject(String key) throws CosClientException {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }

    /**
     * 上传临时搜图文件到 COS（不做图片处理，不存数据库）
     *
     * @param key  唯一键（含目录）
     * @param file 文件
     */
    public String uploadTempSearchImage(String key, File file) {
        putObject(key, file);
        return cosClientConfig.getHost() + "/" + key;
    }

    /**
     * 自动获取图片标签（通过数据万象 detect-label 接口）
     *
     * @param key 图片对象 key
     * @return 标签名称列表，识别失败时返回空列表
     */
    public List<String> detectImageLabel(String key) {
        try {
            ImageLabelRequest request = new ImageLabelRequest();
            request.setBucketName(cosClientConfig.getBucket());
            request.setObjectKey(key);
            request.setScenes("web,camera,album,news");
            ImageLabelResponse response = cosClient.getImageLabel(request);
            List<Label> labels = response.getRecognitionResult();
            if (labels == null || labels.isEmpty()) {
                return new ArrayList<>();
            }
            return labels.stream()
                    .map(Label::getName)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("图片标签识别失败, key={}", key, e);
            return new ArrayList<>();
        }
    }

    /**
     * OCR 文字识别（通过数据万象 OCR 接口）
     *
     * @param key 图片对象 key
     * @return 识别出的文字，识别失败时返回空字符串
     */
    public String recognizeText(String key) {
        try {
            // 生成带 CI 参数的预签名 URL
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                    cosClientConfig.getBucket(), key, HttpMethodName.GET);
            request.addRequestParameter("ci-process", "OCR");
            request.addRequestParameter("type", "general");
            request.addRequestParameter("language-type", "zh");
            // 签名有效期 10 分钟
            request.setExpiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000));
            URL signedUrl = cosClient.generatePresignedUrl(request);
            // 发起 GET 请求
            HttpResponse httpResponse = HttpRequest.get(signedUrl.toString()).execute();
            if (httpResponse.getStatus() != 200) {
                log.error("OCR 请求失败, key={}, status={}", key, httpResponse.getStatus());
                return "";
            }
            // 解析 XML 响应
            JSONObject xmlJson = XML.toJSONObject(httpResponse.body());
            JSONObject response = xmlJson.getJSONObject("Response");
            if (response == null) {
                return "";
            }
            JSONArray textDetections = response.getJSONArray("TextDetections");
            if (textDetections == null || textDetections.isEmpty()) {
                return "";
            }
            // 拼接所有识别出的文字
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < textDetections.size(); i++) {
                JSONObject item = textDetections.getJSONObject(i);
                String detectedText = item.getStr("DetectedText");
                if (detectedText != null && !detectedText.isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(detectedText);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("OCR 文字识别失败, key={}", key, e);
            return "";
        }
    }

    /**
     * 计算 SHA-1 并返回 hex 字符串
     */
    private static String sha1Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return HexFormat.of().formatHex(digest.digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 计算失败", e);
        }
    }

    /**
     * 计算 HMAC-SHA1 并返回 hex 字符串
     */
    private static String hmacSha1Hex(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            return HexFormat.of().formatHex(mac.doFinal(data));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC-SHA1 计算失败", e);
        }
    }

    /**
     * 以图搜图（通过 MetaInsight 多模态混合检索 API）
     *
     * @param imageUrl 图片的完整 URL（COS 地址）
     * @param limit    返回结果数量上限
     * @param threshold 最低相似度分数
     * @return 匹配的图片 COS URI 列表
     */
    public List<String> searchByImage(String imageUrl, int limit, int threshold) {
        try {
            // 从 URL 中提取 bucket 和 path，拼成 cos://bucket/path 格式
            String cosHost = cosClientConfig.getHost();
            String bucket = cosClientConfig.getBucket();
            String path = imageUrl;
            if (imageUrl.startsWith(cosHost)) {
                // URL 是 COS 的地址，直接截取 path 部分
                path = imageUrl.substring(cosHost.length());
            } else if (imageUrl.startsWith("https://")) {
                // URL 是其他网站的图片，取域名后的路径
                int pathStart = imageUrl.indexOf("/", 8);
                if (pathStart > -1) {
                    path = imageUrl.substring(pathStart);
                }
            }
            // 去掉开头的 /
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            // 去掉 COS 图片处理后缀（如 !w640.jpg），这些不是真实文件 key
            int exclIndex = path.indexOf('!');
            if (exclIndex > -1) {
                path = path.substring(0, exclIndex);
            }
            // 拼接 URI
            String cosUri = "cos://" + bucket + "/" + path;

            // 构造请求体
            JSONObject body = new JSONObject();
            body.set("DatasetName", cosClientConfig.getDatasetName()); // 预建的数据集
            body.set("Mode", "pic"); // 图片搜索模式（还有 text 文本模式）
            body.set("Templates", "ImageSearch"); // 使用图片搜索模板
            body.set("SearchURIs", new JSONArray().put(cosUri)); // 要搜索的图片
            body.set("Limit", limit); // 最多返回几条
            body.set("MatchThreshold", threshold); // 最低相似度，低于这个分数的结果不返回
            String payload = body.toString();

            // 构造 API 端点
            String appId = bucket.contains("-") ? bucket.substring(bucket.lastIndexOf("-") + 1) : bucket;
            String region = cosClientConfig.getRegion();
            String ciHost = appId + ".ci." + region + ".myqcloud.com";
            String endpoint = "https://" + ciHost + "/datasetquery/hybridsearch";

            // ====== q-sign 签名算法（HMAC-SHA1） ======、
            // q-sign 就是银行挂号信的完整流程：有效期防旧信重发 + 密钥防伪造 + 内容盖章防篡改。三层保险，缺一不可。
            String secretId = cosClientConfig.getSecretId();
            String secretKey = cosClientConfig.getSecretKey();

            // Step 1: KeyTime = StartTimestamp;EndTimestamp
            // 贴个有效期：这封信从 今天下午2点 到 下午2点10分 有效。过期作废。
            long now = Instant.now().getEpochSecond(); // 获取当前时间戳
            long expires = now + 600; // 600 秒后过期
            String keyTime = now + ";" + expires; // "1718467200;1718467800"

            // Step 2: SignKey = HMAC-SHA1(SecretKey, KeyTime)
            // 用 SecretKey 对 KeyTime 做 HMAC-SHA1
            // 用钥匙算出一个"密码章"：你拿银行给你的钥匙，对着有效期算出一个特殊的章；你的钥匙 + "2点到2点10分" → 密码章 A；这个章只有你能盖，因为钥匙只有你有。
            String signKey = hmacSha1Hex(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    keyTime.getBytes(StandardCharsets.UTF_8));

            // Step 3: HttpString = HttpMethod\nUriPathname\nHttpParameters\nHttpHeaders\n
            // HttpMethod 必须小写，无 query params 和 headers 时保留空行
            // HTTP 请求信息拼接成字符串
            //把信的内容也盖个章：信的内容 → 章 B。这就是 SHA1(HttpString)，证明内容没被动过。
            String httpString = "post\n/datasetquery/hybridsearch\n\n\n";

            // Step 4: StringToSign = sha1\nKeyTime\nSHA1(HttpString)\n
            // 先对 HttpString 做 SHA-1，再和 KeyTime 拼接
            // 把两个章合在一起： 密码章 A + 章 B → 最终签名。这就是 HMAC-SHA1(signKey, stringToSign)
            String httpStringHash = sha1Hex(httpString.getBytes(StandardCharsets.UTF_8));
            String stringToSign = "sha1\n" + keyTime + "\n" + httpStringHash + "\n";

            // Step 5: Signature = HMAC-SHA1(SignKey字符串形式, StringToSign)
            // 重要：SignKey 使用 hex 字符串形式（转为 UTF-8 字节），而非原始二进制
            // 贴到信封上：你把最终签名贴到信封上，发给对方。对方收到信后，拿到你的签名和信的内容，按照同样的算法计算一遍，如果算出的结果和你贴的一样，就证明信是你发的，内容也没被改过。
            String signature = hmacSha1Hex(
                    signKey.getBytes(StandardCharsets.UTF_8),
                    stringToSign.getBytes(StandardCharsets.UTF_8));

            // Step 6: 构造 Authorization header
            String authorization = String.format(
                    "q-sign-algorithm=sha1&q-ak=%s&q-sign-time=%s&q-key-time=%s&q-header-list=&q-url-param-list=&q-signature=%s",
                    secretId, keyTime, keyTime, signature);

            // 发送请求
            log.info("以图搜图请求: endpoint={}, cosUri={}, payload={}", endpoint, cosUri, payload);
            HttpResponse httpResponse = HttpRequest.post(endpoint)
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json")
                    .header("Host", ciHost)
                    .body(payload)
                    .timeout(10000)
                    .execute();

            log.info("以图搜图响应: status={}, body={}", httpResponse.getStatus(), httpResponse.body());
            if (httpResponse.getStatus() != 200) {
                log.error("以图搜图请求失败, status={}, body={}", httpResponse.getStatus(), httpResponse.body());
                return new ArrayList<>();
            }

            // 解析响应（COS 返回的是 XML，不是 JSON）
            JSONObject xmlJson = XML.toJSONObject(httpResponse.body());
            JSONObject response = xmlJson.getJSONObject("Response");
            if (response == null) {
                return new ArrayList<>();
            }
            // 多条结果时 ImageResult 是 JSONArray，单条时是 JSONObject
            Object imageResultObj = response.get("ImageResult");
            JSONArray imageResult;
            if (imageResultObj instanceof JSONArray) {
                imageResult = (JSONArray) imageResultObj;
            } else if (imageResultObj instanceof JSONObject) {
                imageResult = new JSONArray();
                imageResult.add(imageResultObj);
            } else {
                return new ArrayList<>();
            }

            List<String> resultUris = new ArrayList<>();
            for (int i = 0; i < imageResult.size(); i++) {
                JSONObject item = imageResult.getJSONObject(i);
                String uri = item.getStr("URI");
                if (uri != null && !uri.isEmpty()) {
                    resultUris.add(uri);
                }
            }
            return resultUris;
        } catch (Exception e) {
            log.error("以图搜图失败, imageUrl={}", imageUrl, e);
            return new ArrayList<>();
        }
    }
}
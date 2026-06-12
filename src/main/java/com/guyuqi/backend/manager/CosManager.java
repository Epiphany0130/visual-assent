package com.guyuqi.backend.manager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.XML;
import com.guyuqi.backend.config.CosClientConfig;
import com.guyuqi.backend.model.entity.Picture;
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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
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
}
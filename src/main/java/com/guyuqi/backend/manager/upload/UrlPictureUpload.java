package com.guyuqi.backend.manager.upload;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.guyuqi.backend.exception.BusinessException;
import com.guyuqi.backend.exception.ErrorCode;
import com.guyuqi.backend.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * URL 图片上传
 *  @author GuYuqi
 *  @version 1.0
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        String fileUrl = (String) inputSource;
        HttpUtil.downloadFile(fileUrl, file);
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        return FileUtil.mainName(fileUrl);
    }

    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 校验非空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址为空");
        // 校验 URL 格式
        // Java 自带了 URL，如果 URL 格式不正确会抛出 MalformedURLException 异常
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
//            throw new RuntimeException(e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式错误");
        }
        // 校验 URL 协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")
                , ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");
        // 发送 HEAD 请求校验文件是否存在
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            // 未正常返回，无需执行其他操作，直接返回
            // 不能抛异常，因为有些网站是不支持 HEAD 请求的，这样的网站收到 HEAD 请求本身就会返回一个错误的状态码，但不代表文件不存在
            // 抛异常的话就有点过于严格了
            if(httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                return ;
            }
            // 文件存在，文件类型校验
            // 先取字段，响应头中会有 ContentType 字段，表示文件类型
            String contentType = httpResponse.header("Content-Type");
            // 不为空，才校验是否合法，这样校验规则相对宽松
            if(StrUtil.isNotBlank(contentType)) {
                // 允许的图片类型
                // 这里不是单纯的 jpeg、jpg、png、webp，而是 image/jpeg、image/jpg，因为 HTTP 响应头中的 Content-Type 通常带有 image/ 前缀的
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            // 文件存在，文件大小校验
            // 先取字段，响应头中会有 Content-Length 字段，表示文件大小，单位是字节
            String contentLengthStr = httpResponse.header("Content-Length");
            if(StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long TWO_M = 2 * 1024 * 1024L; // 限制文件大小未 2 MB
                    ThrowUtils.throwIf(contentLength > TWO_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        } finally {
            // 释放资源
            if(httpResponse != null) {
                httpResponse.close();
            }
        }
    }
}
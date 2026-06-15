package com.guyuqi.backend.manager.upload;

import cn.hutool.core.io.FileUtil;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * AI 生图上传（复用 PictureUploadTemplate 的完整处理流程）
 */
@Service
public class AiPictureUpload extends PictureUploadTemplate {

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        File sourceFile = (File) inputSource;
        FileUtil.copy(sourceFile, file, true);
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        File sourceFile = (File) inputSource;
        return sourceFile.getName();
    }

    @Override
    protected void validPicture(Object inputSource) {
        // AI 生成的图片跳过校验
    }
}

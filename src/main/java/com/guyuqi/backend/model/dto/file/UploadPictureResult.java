package com.guyuqi.backend.model.dto.file;

import lombok.Data;

@Data
public class UploadPictureResult {  
  
    /**  
     * 图片地址  
     */  
    private String url;  
  
    /**  
     * 图片名称  
     */  
    private String picName;  
  
    /**  
     * 文件体积  
     */  
    private Long picSize;  
  
    /**  
     * 图片宽度  
     */  
    private int picWidth;  
  
    /**  
     * 图片高度  
     */  
    private int picHeight;  
  
    /**  
     * 图片宽高比  
     */  
    private Double picScale;  
  
    /**  
     * 图片格式  
     */  
    private String picFormat;

    /**
     * 缩略图 url
     */
    private String thumbnailUrl;

    /**
     * OCR识别结果（JSON）
     */
    private String ocrResult;

    /**
     * 图片标签（JSON 数组）
     */
    private String tags;

    /**
     * 主色调名称
     */
    private String dominantColor;

    /**
     * 主色调 Hex
     */
    private String dominantColorHex;

    /**
     * 颜色识别结果（JSON）
     */
    private String colorInfo;

    /**
     * AI 分析结果（JSON）
     */
    private String aiAnalysis;

}
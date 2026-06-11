package com.guyuqi.backend.model.dto.picture;

import lombok.Data;

/**
 * 图片批量抓取请求对象
 *
 * @author GuYuqi
 * @version 1.0
 */
@Data
public class PictureUploadByBatchRequest {  
  
    /**  
     * 搜索词  
     */  
    private String searchText;  
  
    /**  
     * 抓取数量  
     */  
    private Integer count = 10;

    /**
     * 名称前缀
     */
    private String namePrefix;
}
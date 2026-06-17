package com.guyuqi.backend.model.dto.space.analyze;

import com.guyuqi.backend.model.vo.space.analyze.SpaceAnalyzeRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户上传行为分析请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 时间维度：day / week / month
     */
    private String timeDimension;
}

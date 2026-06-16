package com.guyuqi.backend.model.vo.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 展示所有空间级别信息
 */
@Data
@AllArgsConstructor
public class SpaceLevel {

    private int value;

    private String text;

    private long maxCount;

    private long maxSize;
}

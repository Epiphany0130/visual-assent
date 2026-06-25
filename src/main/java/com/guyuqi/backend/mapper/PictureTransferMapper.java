package com.guyuqi.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guyuqi.backend.model.entity.PictureTransfer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图片流转记录 Mapper 接口
 */
@Mapper
public interface PictureTransferMapper extends BaseMapper<PictureTransfer> {
}

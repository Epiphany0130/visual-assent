package com.guyuqi.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guyuqi.backend.model.dto.picture.PictureQueryRequest;
import com.guyuqi.backend.model.dto.picture.PictureUploadRequest;
import com.guyuqi.backend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guyuqi.backend.model.entity.User;
import com.guyuqi.backend.model.vo.picture.PictureVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

/**
* @author guyuqi
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2026-06-09 08:44:37
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param multipartFile 图片文件
     * @param pictureUploadRequest 图片上传请求对象
     * @param loginUser 登录用户信息
     * @return 图片视图对象
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /**
     * 构造图片查询条件
     *
     * @param pictureQueryRequest 图片查询请求对象
     * @return 图片查询条件包装器
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片视图对象
     *
     * @param picture 图片实体对象
     * @param request HTTP 请求对象
     * @return 图片视图对象
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 分页获取图片封装
     *
     * @param picturePage 图片分页对象
     * @param request HTTP 请求对象
     * @return 图片视图对象分页
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 图片校验
     *
     * @param picture 图片实体对象
     */
    void validPicture(Picture picture);
}

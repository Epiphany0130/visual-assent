package com.guyuqi.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guyuqi.backend.model.dto.picture.PictureBatchUploadRequest;
import com.guyuqi.backend.model.dto.picture.PictureQueryRequest;
import com.guyuqi.backend.model.dto.picture.PictureReviewRequest;
import com.guyuqi.backend.model.dto.picture.PictureUploadByBatchRequest;
import com.guyuqi.backend.model.dto.picture.PictureUploadRequest;
import com.guyuqi.backend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guyuqi.backend.model.entity.User;
import com.guyuqi.backend.model.vo.picture.PictureVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author guyuqi
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2026-06-09 08:44:37
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片（可重新上传）
     *
     * @param inputSource 输入流
     * @param pictureUploadRequest 图片上传请求对象
     * @param loginUser 登录用户信息
     * @return 图片视图对象
     */
    PictureVO uploadPicture(Object inputSource,
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

    /**
     * 图片审核
     *
     * @param pictureReviewRequest 图片审核请求对象
     * @param loginUser            登录用户信息
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 补充审核参数
     *
     * @param picture   图片
     * @param loginUser 当前登录用户
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest 批量图片抓取请求对象
     * @param loginUser 登录用户信息
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(
            PictureUploadByBatchRequest pictureUploadByBatchRequest,
            User loginUser
    );

    /**
     * 批量上传图片（本地文件）
     *
     * @param files 上传的图片文件数组
     * @param pictureBatchUploadRequest 批量上传请求对象
     * @param loginUser 登录用户信息
     * @return 成功上传的图片视图对象列表
     */
    List<PictureVO> uploadPictureByFiles(MultipartFile[] files,
                                         PictureBatchUploadRequest pictureBatchUploadRequest,
                                         User loginUser);

    /**
     * 图片清理
     *
     * @param oldPicture 旧图片数据
     */
    @Async
    void clearPictureFile(Picture oldPicture);
}

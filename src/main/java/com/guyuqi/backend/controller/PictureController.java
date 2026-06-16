package com.guyuqi.backend.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.guyuqi.backend.annotation.AuthCheck;
import com.guyuqi.backend.common.BaseResponse;
import com.guyuqi.backend.common.DeleteRequest;
import com.guyuqi.backend.common.ResultUtils;
import com.guyuqi.backend.constant.UserConstant;
import com.guyuqi.backend.exception.BusinessException;
import com.guyuqi.backend.exception.ErrorCode;
import com.guyuqi.backend.exception.ThrowUtils;
import com.guyuqi.backend.model.dto.picture.*;
import com.guyuqi.backend.manager.CosManager;
import com.guyuqi.backend.config.CosClientConfig;
import com.guyuqi.backend.model.entity.Picture;
import com.guyuqi.backend.model.entity.Space;
import com.guyuqi.backend.model.entity.User;
import com.guyuqi.backend.model.enums.PictureReviewStatusEnum;
import com.guyuqi.backend.model.vo.picture.PictureTagCategory;
import com.guyuqi.backend.model.vo.picture.PictureVO;
import com.guyuqi.backend.service.PictureService;
import com.guyuqi.backend.service.SpaceService;
import com.guyuqi.backend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 图片接口
 */
@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SpaceService spaceService;

    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();

    /**
     * 上传图片（可重新上传）
     *
     * @param multipartFile 上传的图片文件
     * @param pictureUploadRequest 图片上传请求对象
     * @param request HTTP 请求对象
     * @return 图片视图对象
     */
    @PostMapping("/upload")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过 URL 上传图片（可重新上传）
     *
     * @param pictureUploadRequest 图片上传请求对象
     * @param request HTTP 请求对象
     * @return 图片视图对象
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 批量抓取图片
     *
     * @param pictureUploadByBatchRequest 图片批量抓取请求对象
     * @param request HTTP 请求对象
     * @return 上传成功的图片数量
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                                      HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Integer uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }

    /**
     * 批量上传图片（本地文件），自动按规范命名
     * 命名规则：项目名称_上传人_日期_序号
     *
     * @param files 上传的图片文件数组
     * @param pictureBatchUploadRequest 批量上传请求对象
     * @param request HTTP 请求对象
     * @return 成功上传的图片视图对象列表
     */
    @PostMapping("/upload/batch/files")
    public BaseResponse<List<PictureVO>> uploadPictureByFiles(
            @RequestPart("files") MultipartFile[] files,
            PictureBatchUploadRequest pictureBatchUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<PictureVO> pictureVOList = pictureService.uploadPictureByFiles(files, pictureBatchUploadRequest, loginUser);
        return ResultUtils.success(pictureVOList);
    }

    /**
     * 删除图片
     *
     * @param deleteRequest 删除请求
     * @param request HTTP 请求对象
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        pictureService.deletePicture(id, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片（仅管理员可用）
     *
     * @param pictureUpdateRequest 图片更新请求对象
     * @return 更新结果
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 数据校验
        pictureService.validPicture(picture);
        // 判断是否存在
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取图片（仅管理员可用）
     *
     * @param id 图片 id
     * @param request HTTP 请求对象
     * @return 图片实体对象
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(picture);
    }

    /**
     * 根据 id 获取图片（封装类）
     *
     * @param id 图片 id
     * @param request HTTP 请求对象
     * @return 图片视图对象
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 空间校验权限
        Long spaceId = picture.getSpaceId();
        if(spaceId != null) {
            User loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser, picture);
        }
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }

    /**
     * 分页获取图片列表（仅管理员可用）
     *
     * @param pictureQueryRequest 图片查询请求对象
     * @return 图片分页列表
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 普通用户默认只能查看已过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（封装类）
     *
     * @param pictureQueryRequest 图片查询请求对象
     * @param request HTTP 请求对象
     * @return 图片视图对象分页
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        // 公开图库
        if (spaceId == null) {
            // 普通用户默认只能查看已过审的公开数据
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            // 私有空间
            User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
        }
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    /**
     * 使用缓存分页查询图片列表
     *
     * @param pictureQueryRequest 图片查询请求对象
     * @param request HTTP 请求对象
     * @param principal 当前用户信息
     * @return 图片视图对象分页
     */
    @PostMapping("/list/page/vo/cache")
    @Deprecated
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                      HttpServletRequest request, Principal principal) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 普通用户默认只能查看已经过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 构建缓存 Key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
//        String redisKey = "visualAssets:listPictureVOByPage:" + hashKey;
        String cacheKey = "listPictureVOByPage:" + hashKey;
//        // 从缓存中查询
//        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
//        String cachedValue = valueOps.get(redisKey);
        // 1. 先从本地缓存中查找
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if(cachedValue != null) {
            // 如果缓存命中，返回结果
            Page<PictureVO> cachePage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachePage);
        }
        // 2. 本地缓存未命中，查询 Redis 分布式缓存
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        cachedValue = opsForValue.get(cacheKey);
        if(cachedValue != null) {
            // 如果 Redis 缓存命中，存入本地缓存并返回结果
            LOCAL_CACHE.put(cacheKey, cachedValue);
            Page<PictureVO> cachePage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachePage);
        }
        // 3. 两层缓存都未命中，查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
//        // 存入 Redis 缓存
//        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        // 4. 更新缓存
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        // 5 - 10 分钟随机过期，防止雪崩
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
        opsForValue.set(cacheKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);
        // 写入本地缓存
        LOCAL_CACHE.put(cacheKey, cacheValue);
        // 返回结果
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 编辑图片（给用户使用）
     *
     * @param pictureEditRequest 图片编辑请求对象
     * @param request HTTP 请求对象
     * @return 编辑结果
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        pictureService.validPicture(picture);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 补充审核参数
        pictureService.fillReviewParams(picture, loginUser);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取图片标签和分类列表
     *
     * @return 图片标签和分类列表
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 图片审核（仅管理员可用）
     *
     * @param pictureReviewRequest 图片审核请求对象
     * @param request              HTTP 请求对象
     * @return 审核结果
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 上传图片以图搜图（临时文件，不入库）
     *
     * @param file      上传的图片文件
     * @param limit     返回结果数量上限，默认 10
     * @param threshold 最低相似度分数，默认 60
     * @param request   HTTP 请求对象
     * @return 匹配的图片列表
     */
    @PostMapping("/search/image")
    public BaseResponse<List<PictureVO>> searchByImageUpload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "60") int threshold,
            HttpServletRequest request) throws IOException {
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "图片文件不能为空");
        // 上传到 COS 临时目录
        String ext = FileUtil.getSuffix(file.getOriginalFilename());
        String tempKey = "_search_temp/" + System.currentTimeMillis() + "_" + RandomUtil.randomString(16) + "." + ext;
        File tempFile = File.createTempFile("search_", "." + ext);
        try {
            file.transferTo(tempFile);
            String tempUrl = cosManager.uploadTempSearchImage(tempKey, tempFile);
            // 以图搜图
            List<String> cosUris = cosManager.searchByImage(tempUrl, limit, threshold);
            if (cosUris.isEmpty()) {
                return ResultUtils.success(new java.util.ArrayList<>());
            }
            // 根据 URI 查询数据库
            String bucket = cosClientConfig.getBucket();
            List<PictureVO> result = new java.util.ArrayList<>();
            for (String uri : cosUris) {
                String path = uri.replace("cos://" + bucket + "/", "");
                Picture matchPicture = pictureService.lambdaQuery()
                        .like(Picture::getUrl, path)
                        .last("LIMIT 1")
                        .one();
                if (matchPicture != null) {
                    result.add(pictureService.getPictureVO(matchPicture, request));
                }
            }
            return ResultUtils.success(result);
        } finally {
            // 清理临时文件
            tempFile.delete();
            try {
                cosManager.deleteObject(tempKey);
            } catch (Exception e) {
                log.warn("清理临时搜图文件失败: {}", tempKey, e);
            }
        }
    }

    /**
     * 通过 URL 以图搜图（临时文件，不入库）
     *
     * @param imageUrl 图片 URL
     * @param limit    返回结果数量上限，默认 10
     * @param threshold 最低相似度分数，默认 60
     * @param request  HTTP 请求对象
     * @return 匹配的图片列表
     */
    @GetMapping("/search/image")
    public BaseResponse<List<PictureVO>> searchByImage(
            @RequestParam String imageUrl,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "60") int threshold,
            HttpServletRequest request) throws IOException {
        ThrowUtils.throwIf(StrUtil.isBlank(imageUrl), ErrorCode.PARAMS_ERROR, "图片 URL 不能为空");
        // 下载图片到临时文件，上传到 COS 临时目录
        String ext = FileUtil.getSuffix(imageUrl);
        if (StrUtil.isBlank(ext)) {
            ext = "jpg";
        }
        String tempKey = "_search_temp/" + System.currentTimeMillis() + "_" + RandomUtil.randomString(16) + "." + ext;
        File tempFile = File.createTempFile("search_", "." + ext);
        try {
            HttpUtil.downloadFile(imageUrl, tempFile);
            String tempUrl = cosManager.uploadTempSearchImage(tempKey, tempFile);
            // 以图搜图
            List<String> cosUris = cosManager.searchByImage(tempUrl, limit, threshold);
            if (cosUris.isEmpty()) {
                return ResultUtils.success(new java.util.ArrayList<>());
            }
            // 根据 URI 查询数据库
            String bucket = cosClientConfig.getBucket();
            List<PictureVO> result = new java.util.ArrayList<>();
            for (String uri : cosUris) {
                String path = uri.replace("cos://" + bucket + "/", "");
                Picture matchPicture = pictureService.lambdaQuery()
                        .like(Picture::getUrl, path)
                        .last("LIMIT 1")
                        .one();
                if (matchPicture != null) {
                    result.add(pictureService.getPictureVO(matchPicture, request));
                }
            }
            return ResultUtils.success(result);
        } finally {
            tempFile.delete();
            try {
                cosManager.deleteObject(tempKey);
            } catch (Exception e) {
                log.warn("清理临时搜图文件失败: {}", tempKey, e);
            }
        }
    }

    /**
     * 按照颜色搜索图片
     *
     * @param searchPictureByColorRequest 按照颜色搜索图片请求
     * @param request HTTP 请求
     * @return 图片VO 列表
     */
    @PostMapping("/search/color")
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
        String picColor = searchPictureByColorRequest.getPicColor();
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        User loginUser = userService.getLoginUser(request);
        List<PictureVO> result = pictureService.searchPictureByColor(spaceId, picColor, loginUser);
        return ResultUtils.success(result);
    }

}
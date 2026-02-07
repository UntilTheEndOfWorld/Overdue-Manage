package com.overdue.h5.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.overdue.manager.ums.domain.entity.Address;
import com.overdue.manager.ums.domain.entity.Feedback;
import com.overdue.manager.ums.mapper.AddressMapper;
import com.overdue.manager.ums.domain.dto.AddressDTO;
import com.overdue.manager.ums.service.FeedbackService;
import com.overdue.common.annotation.Log;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.common.core.redis.RedisService;
import com.overdue.common.enums.BusinessType;
import com.overdue.common.utils.OssUtils;
import com.overdue.common.service.FileUploadService;
import com.overdue.common.core.domain.dto.FileUploadResult;
import com.overdue.common.manager.FileUploadManager;
import java.util.concurrent.CompletableFuture;
import com.overdue.system.service.ISysConfigService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping
public class H5CommonController {

  @Autowired
  private OssUtils ossUtils;
  @Autowired
  private FileUploadService fileUploadService;
  @Autowired
  private FileUploadManager fileUploadManager;
  @Autowired
  private AddressMapper addressMapper;
  @Autowired
  private RedisService redisService;
  @Autowired
  private FeedbackService feedbackService;
  @Autowired
  private ISysConfigService sysConfigService;

  @ApiOperation("新增意见反馈")
  @Log(title = "意见反馈", businessType = BusinessType.INSERT)
  @PostMapping("/h5/feedback/create")
  public ResponseEntity<Integer> add(@RequestBody Feedback feedback) {
    return ResponseEntity.ok(feedbackService.insert(feedback));
  }

  @ApiOperation("获取用户反馈历史")
  @GetMapping("/h5/feedback/list")
  public AjaxResult getFeedbackList() {
    try {
      List<Feedback> feedbackList = feedbackService.selectListByMemberId();
      return AjaxResult.success(feedbackList);
    } catch (Exception e) {
      log.error("获取反馈历史失败", e);
      return AjaxResult.error("获取反馈历史失败");
    }
  }

  @GetMapping("/h5/area")
  public AjaxResult getAddressList() {
    String addresses = redisService.getAddressList();
    if (StringUtils.isNotEmpty(addresses)) {
      List<AddressDTO> addressDTOList = JSON.parseArray(addresses, AddressDTO.class);
      if (addressDTOList.size() > 0) {
        return AjaxResult.success(addressDTOList);
      }
    }
    QueryWrapper<Address> addressQueryWrapper = new QueryWrapper<>();
    addressQueryWrapper.in("level", Arrays.asList(0, 1, 2));
    List<Address> addressList = addressMapper.selectList(addressQueryWrapper);
    Map<Long, List<Address>> cityMap = addressList.stream().filter(it -> it.getLevel() == 1)
        .collect(Collectors.groupingBy(it -> it.getParentCode()));
    Map<Long, List<Address>> districtMap = addressList.stream().filter(it -> it.getLevel() == 2)
        .collect(Collectors.groupingBy(it -> it.getParentCode()));
    List<AddressDTO> result = new ArrayList<>();
    addressList.stream().filter(it -> it.getLevel() == 0).forEach(it -> {
      AddressDTO dto = new AddressDTO();
      dto.setId(it.getCode());
      dto.setLevel("province");
      dto.setName(it.getName());
      dto.setPid(0L);
      // 获取城市列表
      List<AddressDTO> child = new ArrayList<>();
      if (cityMap.containsKey(it.getCode())) {
        cityMap.get(it.getCode()).forEach(city -> {
          AddressDTO cityDto = new AddressDTO();
          cityDto.setId(city.getCode());
          cityDto.setLevel("city");
          cityDto.setName(city.getName());
          cityDto.setPid(city.getParentCode());
          cityDto.setChildren(
              districtMap.containsKey(city.getCode()) ? districtMap.get(city.getCode()).stream().map(district -> {
                AddressDTO districtDto = new AddressDTO();
                districtDto.setId(district.getCode());
                districtDto.setLevel("district");
                districtDto.setName(district.getName());
                districtDto.setPid(district.getParentCode());
                return districtDto;
              }).collect(Collectors.toList()) : Collections.EMPTY_LIST);
          child.add(cityDto);
        });
      }
      dto.setChildren(child);
      result.add(dto);
    });
    redisService.setAddressList(JSON.toJSONString(result));
    return AjaxResult.success(result);
  }

  /**
   * 单文件上传
   * 
   * @param file     上传的文件
   * @param category 文件分类（可选）
   * @return 文件访问URL
   */
  @ApiOperation("单文件上传")
  @PostMapping("/h5/file/upload")
  public AjaxResult uploadFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "category", required = false) String category) {
    try {
      String url = ossUtils.uploadOneFile(file, category);
      return AjaxResult.successData(url);
    } catch (Exception e) {
      return AjaxResult.error("文件上传失败: " + e.getMessage());
    }
  }

  /**
   * 多文件上传
   * 
   * @param files    上传的文件数组
   * @param category 文件分类（可选）
   * @return 文件访问URL列表
   */
  @ApiOperation("多文件上传")
  @PostMapping("/h5/file/upload-multiple")
  public AjaxResult uploadMultipleFiles(
      @RequestParam("files") MultipartFile[] files,
      @RequestParam(value = "category", required = false) String category) {
    try {
      List<String> urls = ossUtils.uploadArrayFile(files, category);
      return AjaxResult.successData(urls);
    } catch (Exception e) {
      return AjaxResult.error("文件上传失败: " + e.getMessage());
    }
  }

  /**
   * 图片上传（专用于图片文件）
   * 
   * @param file 图片文件
   * @return 图片上传详细信息
   */
  @ApiOperation("图片上传")
  @PostMapping("/h5/file/upload-image")
  public AjaxResult uploadImage(@RequestParam("file") MultipartFile file) {
    try {
      FileUploadResult result = fileUploadService.uploadImage(file);
      return AjaxResult.successData(result);
    } catch (Exception e) {
      return AjaxResult.error("图片上传失败: " + e.getMessage());
    }
  }

  /**
   * 文档上传（专用于文档文件）
   * 
   * @param file 文档文件
   * @return 文档上传详细信息
   */
  @ApiOperation("文档上传")
  @PostMapping("/h5/file/upload-document")
  public AjaxResult uploadDocument(@RequestParam("file") MultipartFile file) {
    try {
      FileUploadResult result = fileUploadService.uploadDocument(file);
      return AjaxResult.successData(result);
    } catch (Exception e) {
      return AjaxResult.error("文档上传失败: " + e.getMessage());
    }
  }

  /**
   * 视频上传（专用于视频文件）
   * 
   * @param file 视频文件
   * @return 视频上传详细信息
   */
  @ApiOperation("视频上传")
  @PostMapping("/h5/file/upload-video")
  public AjaxResult uploadVideo(@RequestParam("file") MultipartFile file) {
    try {
      FileUploadResult result = fileUploadService.uploadVideo(file);
      return AjaxResult.successData(result);
    } catch (Exception e) {
      return AjaxResult.error("视频上传失败: " + e.getMessage());
    }
  }

  /**
   * 高级文件上传（返回详细信息）
   * 
   * @param file     上传的文件
   * @param category 文件分类（可选）
   * @return 文件上传详细信息
   */
  @ApiOperation("高级文件上传")
  @PostMapping("/h5/file/upload-advanced")
  public AjaxResult uploadFileAdvanced(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "category", required = false) String category) {
    try {
      FileUploadResult result = fileUploadService.uploadFileWithDetails(file, category);
      return AjaxResult.successData(result);
    } catch (Exception e) {
      return AjaxResult.error("文件上传失败: " + e.getMessage());
    }
  }

  /**
   * 删除文件
   * 
   * @param fileUrl 文件URL
   * @return 删除结果
   */
  @ApiOperation("删除文件")
  @PostMapping("/h5/file/delete")
  public AjaxResult deleteFile(@RequestParam("fileUrl") String fileUrl) {
    try {
      boolean success = ossUtils.deleteFile(fileUrl);
      if (success) {
        return AjaxResult.success("文件删除成功");
      } else {
        return AjaxResult.error("文件删除失败");
      }
    } catch (Exception e) {
      return AjaxResult.error("文件删除失败: " + e.getMessage());
    }
  }

  /**
   * 批量删除文件
   * 
   * @param fileUrls 文件URL列表
   * @return 删除结果
   */
  @ApiOperation("批量删除文件")
  @PostMapping("/h5/file/delete-multiple")
  public AjaxResult deleteMultipleFiles(@RequestBody List<String> fileUrls) {
    try {
      boolean success = ossUtils.deleteFiles(fileUrls);
      if (success) {
        return AjaxResult.success("文件删除成功");
      } else {
        return AjaxResult.error("文件删除失败");
      }
    } catch (Exception e) {
      return AjaxResult.error("文件删除失败: " + e.getMessage());
    }
  }

  /**
   * 检查文件是否存在
   * 
   * @param fileUrl 文件URL
   * @return 文件是否存在
   */
  @ApiOperation("检查文件是否存在")
  @GetMapping("/h5/file/exists")
  public AjaxResult fileExists(@RequestParam("fileUrl") String fileUrl) {
    try {
      boolean exists = ossUtils.fileExists(fileUrl);
      return AjaxResult.successData(exists);
    } catch (Exception e) {
      return AjaxResult.error("检查文件失败: " + e.getMessage());
    }
  }

  /**
   * 智能文件上传（自动分类）
   * 
   * @param file 上传的文件
   * @return 文件上传详细信息
   */
  @ApiOperation("智能文件上传")
  @PostMapping("/h5/file/upload-smart")
  public AjaxResult smartUpload(@RequestParam("file") MultipartFile file) {
    try {
      FileUploadResult result = fileUploadManager.smartUpload(file);
      return AjaxResult.successData(result);
    } catch (Exception e) {
      return AjaxResult.error("智能上传失败: " + e.getMessage());
    }
  }

  /**
   * 异步文件上传
   * 
   * @param file     上传的文件
   * @param category 文件分类（可选）
   * @return 上传任务ID
   */
  @ApiOperation("异步文件上传")
  @PostMapping("/h5/file/upload-async")
  public AjaxResult uploadFileAsync(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "category", required = false) String category) {
    try {
      CompletableFuture<FileUploadResult> future = fileUploadManager.uploadFileAsync(file, category);

      // 这里可以返回任务ID，前端可以通过任务ID查询上传状态
      String taskId = "task_" + System.currentTimeMillis();

      // 异步处理完成后的回调（实际项目中可以通过WebSocket通知前端）
      future.whenComplete((result, throwable) -> {
        if (throwable != null) {
          log.error("异步上传失败: {}", file.getOriginalFilename(), throwable);
        } else {
          log.info("异步上传成功: {} -> {}", file.getOriginalFilename(), result.getUrl());
        }
      });

      return AjaxResult.successData(taskId);
    } catch (Exception e) {
      return AjaxResult.error("异步上传启动失败: " + e.getMessage());
    }
  }

  /**
   * 获取存储统计信息
   * 
   * @return 存储统计
   */
  @ApiOperation("获取存储统计信息")
  @GetMapping("/h5/file/storage-stats")
  public AjaxResult getStorageStats() {
    try {
      FileUploadManager.StorageStats stats = fileUploadManager.getStorageStats();
      return AjaxResult.successData(stats);
    } catch (Exception e) {
      return AjaxResult.error("获取存储统计失败: " + e.getMessage());
    }
  }

  @GetMapping("/no-auth/config/get")
  public AjaxResult getSysConfig(String configKey) {
    String s = sysConfigService.selectConfigByKey(configKey);
    return AjaxResult.successData(s);
  }

}

package com.overdue.manager.ums.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.overdue.h5.domain.form.WechatLoginForm;
import com.overdue.h5.service.H5MemberService;
import com.overdue.manager.ums.convert.MemberWechatConvert;
import com.overdue.manager.ums.domain.entity.Member;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.overdue.manager.ums.mapper.MemberWechatMapper;
import com.overdue.manager.ums.domain.entity.MemberWechat;
import com.overdue.manager.ums.domain.query.MemberWechatQuery;
import org.springframework.web.client.RestTemplate;
import com.overdue.h5.domain.form.WechatPhoneRegisterForm;
import com.overdue.config.AESForWeixinGetPhoneNumber;
import com.overdue.common.utils.AesCryptoUtils;
import com.overdue.common.utils.PhoneUtils;
import com.overdue.common.constant.Constants;
import com.overdue.manager.ums.mapper.MemberMapper;
import com.overdue.h5.domain.form.WechatUserInfoLoginForm;
import com.overdue.h5.domain.form.WechatPhoneForm;
import com.overdue.common.core.domain.model.LoginMember;
import com.overdue.framework.web.service.TokenService;
import com.overdue.manager.ums.service.NewUserCouponService;

/**
 * 用户微信信息Service业务层处理
 *
 *
 * @author zcc
 */
@Slf4j
@Service
public class MemberWechatService {
    @Autowired
    private MemberWechatMapper memberWechatMapper;
    @Autowired
    private MemberWechatConvert memberWechatConvert;
    @Autowired
    private H5MemberService memberService;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private TokenService tokenService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private NewUserCouponService newUserCouponService;

    @Value("${wechat.miniProgramAppId}")
    private String miniProgramAppId;

    @Value("${wechat.miniProgramSecret}")
    private String miniProgramSecret;

    @Value("${aes.key}")
    private String aesKey;

    private static String LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session?appid=#{APPID}&secret=#{SECRET}&js_code=#{JSCODE}&grant_type=authorization_code";

    /**
     * 查询用户微信信息
     *
     * @param id 用户微信信息主键
     * @return 用户微信信息
     */
    public MemberWechat selectById(Long id) {
        return memberWechatMapper.selectById(id);
    }

    /**
     * 查询用户微信信息列表
     *
     * @param query 查询条件
     * @param page  分页条件
     * @return 用户微信信息
     */
    public List<MemberWechat> selectList(MemberWechatQuery query, Pageable page) {
        if (page != null) {
            PageHelper.startPage(page.getPageNumber() + 1, page.getPageSize());
        }
        QueryWrapper<MemberWechat> qw = new QueryWrapper<>();
        Long memberId = query.getMemberId();
        if (memberId != null) {
            qw.eq("member_id", memberId);
        }
        String unionid = query.getUnionid();
        if (!StringUtils.isEmpty(unionid)) {
            qw.eq("unionid", unionid);
        }
        String openid = query.getOpenid();
        if (!StringUtils.isEmpty(openid)) {
            qw.eq("openid", openid);
        }
        String routineOpenid = query.getRoutineOpenid();
        if (!StringUtils.isEmpty(routineOpenid)) {
            qw.eq("routine_openid", routineOpenid);
        }
        Integer groupid = query.getGroupid();
        if (groupid != null) {
            qw.eq("groupid", groupid);
        }
        String tagidList = query.getTagidList();
        if (!StringUtils.isEmpty(tagidList)) {
            qw.eq("tagid_list", tagidList);
        }
        Integer subscribe = query.getSubscribe();
        if (subscribe != null) {
            qw.eq("subscribe", subscribe);
        }
        Integer subscribeTime = query.getSubscribeTime();
        if (subscribeTime != null) {
            qw.eq("subscribe_time", subscribeTime);
        }
        String sessionKey = query.getSessionKey();
        if (!StringUtils.isEmpty(sessionKey)) {
            qw.eq("session_key", sessionKey);
        }
        String accessToken = query.getAccessToken();
        if (!StringUtils.isEmpty(accessToken)) {
            qw.eq("access_token", accessToken);
        }
        Integer expiresIn = query.getExpiresIn();
        if (expiresIn != null) {
            qw.eq("expires_in", expiresIn);
        }
        String refreshToken = query.getRefreshToken();
        if (!StringUtils.isEmpty(refreshToken)) {
            qw.eq("refresh_token", refreshToken);
        }
        LocalDateTime expireTime = query.getExpireTime();
        if (expireTime != null) {
            qw.eq("expire_time", expireTime);
        }
        return memberWechatMapper.selectList(qw);
    }

    /**
     * 新增用户微信信息
     *
     * @param memberWechat 用户微信信息
     * @return 结果
     */
    public int insert(MemberWechat memberWechat) {
        memberWechat.setCreateTime(LocalDateTime.now());
        return memberWechatMapper.insert(memberWechat);
    }

    /**
     * 修改用户微信信息
     *
     * @param memberWechat 用户微信信息
     * @return 结果
     */
    public int update(MemberWechat memberWechat) {
        return memberWechatMapper.updateById(memberWechat);
    }

    /**
     * 删除用户微信信息信息
     *
     * @param id 用户微信信息主键
     * @return 结果
     */
    public int deleteById(Long id) {
        return memberWechatMapper.deleteById(id);
    }

    public String login(WechatLoginForm form) {
        // 1. 使用小程序的jscode2session接口获取openid和session_key
        JSONObject sessionInfo = getSessionId(form.getCode());
        if (sessionInfo == null || sessionInfo.getInteger("errcode") != null) {
            log.error("获取小程序session失败: {}", sessionInfo);
            throw new RuntimeException("微信登录失败，请重试");
        }

        String openid = sessionInfo.getString("openid");
        String sessionKey = sessionInfo.getString("session_key");

        // 2. 查找用户是否存在, 若没有则创建
        log.info("检查微信openid是否已存在: {}", openid);
        LambdaQueryWrapper<MemberWechat> qw = new LambdaQueryWrapper<>();
        qw.eq(MemberWechat::getOpenid, openid);
        MemberWechat m = memberWechatMapper.selectOne(qw);
        if (m != null) {
            // 用户已存在，直接返回token
            log.info("微信用户已存在，直接返回token");
            return getToken(m.getMemberId());
        } else {
            // 用户不存在，创建新用户
            log.info("微信用户不存在，开始创建新用户");

            // 直接创建Member（ums_member表），不创建SysUser
            Member newMember = new Member();
            // 使用数据库自增ID
            newMember.setId(null);
            newMember.setNickname(StrUtil.isNotBlank(form.getNickname()) ? form.getNickname() : "微信用户");
            newMember.setAvatar(StrUtil.isNotBlank(form.getAvatarUrl()) ? form.getAvatarUrl() : "");
            newMember.setStatus(Constants.MEMBER_ACCOUNT_STATUS.NORMAL);
            newMember.setGender(0);
            newMember.setLevel(1); // 设置会员等级为普通会员
            newMember.setLevelName("普通会员"); // 设置会员等级名称
            newMember.setCreateTime(LocalDateTime.now());
            newMember.setUpdateTime(LocalDateTime.now());
            newMember.setCreateBy(null); // 临时用户，没有创建者
            newMember.setUpdateBy(null);

            // 插入到ums_member表
            int insertResult = memberMapper.insert(newMember);
            if (insertResult > 0) {
                log.info("创建Member成功，插入ums_member表: {}", newMember.getId());
            } else {
                log.error("创建Member失败，插入ums_member表失败");
                throw new RuntimeException("创建会员失败");
            }

            // 创建微信绑定记录
            MemberWechat w = new MemberWechat();
            w.setOpenid(openid);
            w.setRoutineOpenid(openid); // 小程序支付需要routineOpenid字段
            w.setMemberId(newMember.getId());
            w.setSessionKey(sessionKey); // 保存session_key用于后续解密
            w.setCreateTime(LocalDateTime.now());
            w.setUpdateTime(LocalDateTime.now());
            w.setExpireTime(LocalDateTime.now().plus(7200, ChronoUnit.SECONDS)); // 2小时过期
            memberWechatMapper.insert(w);

            // 3. 返回会员token（不是系统用户token）
            return getToken(newMember.getId());
        }
    }

    public JSONObject getSessionId(String code) {
        String url = LOGIN_URL.replace("#{APPID}", miniProgramAppId)
                .replace("#{SECRET}", miniProgramSecret)
                .replace("#{JSCODE}", code);
        log.info("获取openid，url：{}", url);
        try {
            ResponseEntity<String> res = restTemplate.getForEntity(url, String.class);
            String body = res.getBody();
            if (com.overdue.common.utils.StringUtils.isEmpty(body)) {
                throw new Exception("获取openid出错");
            }
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("获取openid报错", e);
            return null;
        }
    }

    public String getToken(Long memberId) {
        // 根据会员ID获取token
        try {
            Member member = memberService.selectById(memberId);
            if (member == null) {
                log.error("会员不存在，memberId: {}", memberId);
                return null;
            }

            String token = memberService.getLoginResponse(member.getId()).getToken();
            if (StrUtil.isBlank(token)) {
                log.error("获取token失败，memberId: {}", memberId);
                return null;
            }

            log.info("获取token成功，memberId: {}, token: {}", memberId, token);
            return token;
        } catch (Exception e) {
            log.error("获取token异常，memberId: {}", memberId, e);
            return null;
        }
    }

    public String getTokenByOpenId(String openId) {
        // 根据openid查找会员ID，然后获取token
        LambdaQueryWrapper<MemberWechat> qw = new LambdaQueryWrapper<>();
        qw.eq(MemberWechat::getOpenid, openId);
        MemberWechat memberWechat = memberWechatMapper.selectOne(qw);
        if (memberWechat == null) {
            return null;
        }
        return getToken(memberWechat.getMemberId());
    }

    /**
     * 微信登录后获取手机号并注册会员
     * 
     * @param form 包含手机号加密数据的表单
     * @return 注册结果
     */
    public String registerWithPhone(WechatPhoneRegisterForm form) {
        try {
            String openid;
            String sessionKey;

            // 1. 使用code获取session_key和openid
            if (StrUtil.isNotBlank(form.getCode())) {
                JSONObject sessionInfo = getSessionId(form.getCode());
                if (sessionInfo == null || sessionInfo.getInteger("errcode") != null) {
                    log.error("获取小程序session失败: {}", sessionInfo);
                    throw new RuntimeException("微信登录失败，请重试");
                }
                openid = sessionInfo.getString("openid");
                sessionKey = sessionInfo.getString("session_key");
                log.info("通过code获取openid和session_key成功: {}", openid);
            } else {
                throw new RuntimeException("缺少必要的参数：code");
            }

            // 2. 解密手机号
            String phoneNumber = null;
            try {
                log.info("开始解密手机号，encryptedData长度: {}, sessionKey长度: {}, iv长度: {}",
                        form.getEncryptedData() != null ? form.getEncryptedData().length() : 0,
                        sessionKey != null ? sessionKey.length() : 0,
                        form.getIv() != null ? form.getIv().length() : 0);

                AESForWeixinGetPhoneNumber aes = new AESForWeixinGetPhoneNumber(
                        form.getEncryptedData(),
                        sessionKey,
                        form.getIv());
                JSONObject decryptResult = aes.decrypt();
                if (decryptResult != null) {
                    phoneNumber = decryptResult.getString("phoneNumber");
                    log.info("解密手机号成功: {}", phoneNumber);
                } else {
                    log.error("解密结果为空，可能是参数错误或解密失败");
                    throw new RuntimeException("手机号解密失败，请检查参数");
                }
            } catch (Exception e) {
                log.error("解密手机号失败，错误详情: {}", e.getMessage(), e);
                throw new RuntimeException("手机号解密失败，请重试");
            }

            // 3. 检查手机号是否已被注册
            String encryptedPhone = AesCryptoUtils.encrypt(aesKey, phoneNumber);
            log.info("检查手机号是否已注册，加密后: {}", encryptedPhone);

            LambdaQueryWrapper<Member> memberQuery = new LambdaQueryWrapper<>();
            memberQuery.eq(Member::getPhoneEncrypted, encryptedPhone);
            Member existingMember = memberMapper.selectOne(memberQuery);

            if (existingMember != null) {
                // 手机号已存在，直接返回token
                log.info("手机号已存在，直接登录: {}", phoneNumber);
                String token = getToken(existingMember.getId());
                if (StrUtil.isBlank(token)) {
                    log.error("手机号已存在但获取token失败");
                    throw new RuntimeException("登录失败，请重试");
                }
                return token;
            } else {
                log.info("手机号未注册，继续创建新用户");
            }

            // 4. 检查微信openid是否已存在
            log.info("检查微信openid是否已存在: {}", openid);
            LambdaQueryWrapper<MemberWechat> wechatQuery = new LambdaQueryWrapper<>();
            wechatQuery.eq(MemberWechat::getOpenid, openid);
            MemberWechat existingWechat = memberWechatMapper.selectOne(wechatQuery);

            if (existingWechat != null) {
                // 微信账号已存在，更新会员信息
                log.info("微信账号已存在，更新会员信息: {}", phoneNumber);

                // 更新会员信息
                Member member = memberMapper.selectById(existingWechat.getMemberId());
                if (member != null) {
                    member.setPhone(phoneNumber); // 保存完整手机号
                    member.setPhoneEncrypted(AesCryptoUtils.encrypt(aesKey, phoneNumber));
                    member.setPhoneHidden(PhoneUtils.hidePhone(phoneNumber));
                    if (StrUtil.isNotBlank(form.getNickname())) {
                        member.setNickname(form.getNickname());
                    }
                    if (StrUtil.isNotBlank(form.getAvatarUrl())) {
                        member.setAvatar(form.getAvatarUrl());
                    }
                    member.setUpdateTime(LocalDateTime.now());
                    memberMapper.updateById(member);
                    log.info("更新会员信息成功: {}", member.getId());
                }

                String token = getToken(existingWechat.getMemberId());
                if (StrUtil.isBlank(token)) {
                    log.error("微信账号已存在但获取token失败");
                    throw new RuntimeException("登录失败，请重试");
                }
                return token;
            } else {
                log.info("微信openid未存在，将创建新用户");
            }

            // 5. 创建新用户（只创建Member和MemberWechat，不创建SysUser）
            log.info("创建新会员，手机号: {}", phoneNumber);

            // 直接创建Member（ums_member表），不创建SysUser
            Member newMember = new Member();
            // 生成唯一的会员ID，避免与sys_user冲突
            newMember.setId(generateMemberId());
            newMember.setPhone(phoneNumber); // 保存完整手机号
            newMember.setPhoneEncrypted(AesCryptoUtils.encrypt(aesKey, phoneNumber));
            newMember.setPhoneHidden(PhoneUtils.hidePhone(phoneNumber));
            newMember.setNickname(StrUtil.isNotBlank(form.getNickname()) ? form.getNickname() : "微信用户");
            newMember.setAvatar(StrUtil.isNotBlank(form.getAvatarUrl()) ? form.getAvatarUrl() : "");
            newMember.setStatus(Constants.MEMBER_ACCOUNT_STATUS.NORMAL);
            newMember.setGender(0);
            newMember.setLevel(1); // 设置会员等级为普通会员
            newMember.setLevelName("普通会员"); // 设置会员等级名称
            newMember.setCreateTime(LocalDateTime.now());
            newMember.setUpdateTime(LocalDateTime.now());
            newMember.setCreateBy(newMember.getId()); // 自己创建自己
            newMember.setUpdateBy(newMember.getId());

            // 插入到ums_member表
            int insertResult = memberMapper.insert(newMember);
            if (insertResult > 0) {
                log.info("创建Member成功，插入ums_member表: {}", newMember.getId());
            } else {
                log.error("创建Member失败，插入ums_member表失败");
                throw new RuntimeException("创建会员失败");
            }

            // 创建微信关联记录
            MemberWechat newWechat = new MemberWechat();
            newWechat.setMemberId(newMember.getId());
            newWechat.setOpenid(openid);
            newWechat.setRoutineOpenid(openid); // 小程序支付需要routineOpenid字段
            newWechat.setSessionKey(sessionKey); // 保存session_key用于后续解密
            newWechat.setCreateTime(LocalDateTime.now());
            newWechat.setUpdateTime(LocalDateTime.now());
            newWechat.setExpireTime(LocalDateTime.now().plus(7200, ChronoUnit.SECONDS)); // 2小时过期

            int wechatInsertResult = memberWechatMapper.insert(newWechat);
            if (wechatInsertResult > 0) {
                log.info("创建微信关联记录成功: {}", newWechat.getId());
            } else {
                log.error("创建微信关联记录失败");
                throw new RuntimeException("创建微信关联记录失败");
            }

            // 6. 为新用户赠送优惠券
            try {
                newUserCouponService.giveNewUserCoupons(newMember.getId());
                log.info("新用户优惠券赠送成功，会员ID: {}", newMember.getId());
            } catch (Exception e) {
                log.error("新用户优惠券赠送失败，但不影响注册流程: {}", e.getMessage());
                // 优惠券赠送失败不影响用户注册
            }

            // 7. 返回会员token（不是系统用户token）
            String token = getToken(newMember.getId());
            log.info("生成会员token成功，会员ID: {}", newMember.getId());

            return token;

        } catch (Exception e) {
            log.error("微信手机号注册失败", e);
            throw new RuntimeException("注册失败: " + e.getMessage());
        }
    }

    /**
     * 微信用户信息登录（第一步：获取微信用户信息）
     * 
     * @param form 包含微信登录code和用户信息的表单
     * @return 登录token
     */
    public String userInfoLogin(WechatUserInfoLoginForm form) {
        try {
            log.info("开始微信用户信息登录，code: {}", form.getCode());

            // 1. 使用code获取session_key和openid
            JSONObject sessionInfo = getSessionId(form.getCode());
            if (sessionInfo == null || sessionInfo.getInteger("errcode") != null) {
                log.error("获取小程序session失败: {}", sessionInfo);
                throw new RuntimeException("微信登录失败，请重试");
            }

            String openid = sessionInfo.getString("openid");
            String sessionKey = sessionInfo.getString("session_key");
            log.info("通过code获取openid和session_key成功: {}", openid);

            // 2. 检查微信openid是否已存在
            log.info("检查微信openid是否已存在: {}", openid);
            LambdaQueryWrapper<MemberWechat> wechatQuery = new LambdaQueryWrapper<>();
            wechatQuery.eq(MemberWechat::getOpenid, openid);
            MemberWechat existingWechat = memberWechatMapper.selectOne(wechatQuery);

            if (existingWechat != null) {
                // 用户已存在，直接返回token
                log.info("微信用户已存在，直接登录: {}", openid);
                String token = getToken(existingWechat.getMemberId());
                if (StrUtil.isBlank(token)) {
                    log.error("用户已存在但获取token失败");
                    throw new RuntimeException("登录失败，请重试");
                }
                return token;
            } else {
                log.info("微信用户不存在，创建临时用户记录");
            }

            // 3. 创建临时会员记录（不包含手机号）
            // 直接创建Member（ums_member表），不创建SysUser
            Member newMember = new Member();
            // 使用数据库自增ID
            newMember.setId(null);
            newMember.setNickname(StrUtil.isNotBlank(form.getNickname()) ? form.getNickname() : "微信用户");
            newMember.setAvatar(StrUtil.isNotBlank(form.getAvatarUrl()) ? form.getAvatarUrl() : "");
            newMember.setStatus(Constants.MEMBER_ACCOUNT_STATUS.NORMAL);
            newMember.setGender(form.getGender() != null ? form.getGender() : 0);
            newMember.setLevel(1); // 设置会员等级为普通会员
            newMember.setLevelName("普通会员"); // 设置会员等级名称
            newMember.setCreateTime(LocalDateTime.now());
            newMember.setUpdateTime(LocalDateTime.now());
            newMember.setCreateBy(null); // 临时用户，没有创建者
            newMember.setUpdateBy(null);

            // 插入到ums_member表
            int insertResult = memberMapper.insert(newMember);
            if (insertResult > 0) {
                log.info("创建临时Member成功，插入ums_member表: {}", newMember.getId());
            } else {
                log.error("创建临时Member失败，插入ums_member表失败");
                throw new RuntimeException("创建会员失败");
            }

            // 创建微信关联记录
            MemberWechat newWechat = new MemberWechat();
            newWechat.setMemberId(newMember.getId());
            newWechat.setOpenid(openid);
            newWechat.setRoutineOpenid(openid); // 小程序支付需要routineOpenid字段
            newWechat.setSessionKey(sessionKey); // 保存session_key用于后续解密
            newWechat.setCreateTime(LocalDateTime.now());
            newWechat.setUpdateTime(LocalDateTime.now());
            newWechat.setExpireTime(LocalDateTime.now().plus(7200, ChronoUnit.SECONDS)); // 2小时过期

            int wechatInsertResult = memberWechatMapper.insert(newWechat);
            if (wechatInsertResult > 0) {
                log.info("创建微信关联记录成功: {}", newWechat.getId());
            } else {
                log.error("创建微信关联记录失败");
                throw new RuntimeException("创建微信关联记录失败");
            }

            // 4. 返回会员token（不是系统用户token）
            String token = getToken(newMember.getId());
            log.info("生成临时会员token成功，会员ID: {}", newMember.getId());

            return token;

        } catch (Exception e) {
            log.error("微信用户信息登录失败", e);
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }

    /**
     * 获取微信手机号（第二步：解密手机号）
     * 
     * @param form 包含手机号加密数据的表单
     * @return 手机号
     */
    public String getPhoneNumber(WechatPhoneForm form) {
        try {
            log.info("开始获取微信手机号，code: {}", form.getCode());

            // 1. 使用code获取session_key和openid
            JSONObject sessionInfo = getSessionId(form.getCode());
            if (sessionInfo == null || sessionInfo.getInteger("errcode") != null) {
                log.error("获取小程序session失败: {}", sessionInfo);
                throw new RuntimeException("微信登录失败，请重试");
            }

            String openid = sessionInfo.getString("openid");
            String sessionKey = sessionInfo.getString("session_key");
            log.info("通过code获取openid和session_key成功: {}", openid);

            // 2. 解密手机号
            String phoneNumber = null;
            try {
                AESForWeixinGetPhoneNumber aes = new AESForWeixinGetPhoneNumber(
                        form.getEncryptedData(),
                        sessionKey,
                        form.getIv());
                JSONObject decryptResult = aes.decrypt();
                if (decryptResult != null) {
                    phoneNumber = decryptResult.getString("phoneNumber");
                    log.info("解密手机号成功: {}", phoneNumber);
                } else {
                    throw new RuntimeException("手机号解密失败");
                }
            } catch (Exception e) {
                log.error("解密手机号失败", e);
                throw new RuntimeException("手机号解密失败，请重试");
            }

            return phoneNumber;

        } catch (Exception e) {
            log.error("获取微信手机号失败", e);
            throw new RuntimeException("获取手机号失败: " + e.getMessage());
        }
    }

    /**
     * 检查微信登录缓存状态
     * 
     * @param openid 微信openid
     * @return 登录状态信息
     */
    public JSONObject checkLoginStatus(String openid) {
        try {
            log.info("检查微信登录缓存状态，openid: {}", openid);

            // 1. 根据openid查找用户
            LambdaQueryWrapper<MemberWechat> wechatQuery = new LambdaQueryWrapper<>();
            wechatQuery.eq(MemberWechat::getOpenid, openid);
            MemberWechat memberWechat = memberWechatMapper.selectOne(wechatQuery);

            if (memberWechat == null) {
                log.info("微信用户不存在，openid: {}", openid);
                JSONObject result = new JSONObject();
                result.put("isLoggedIn", false);
                result.put("message", "用户未注册");
                return result;
            }

            // 2. 获取用户信息
            Member member = memberMapper.selectById(memberWechat.getMemberId());
            if (member == null) {
                log.error("会员信息不存在，memberId: {}", memberWechat.getMemberId());
                JSONObject result = new JSONObject();
                result.put("isLoggedIn", false);
                result.put("message", "用户信息不存在");
                return result;
            }

            // 3. 检查token是否有效
            String token = getToken(memberWechat.getMemberId());
            if (StrUtil.isBlank(token)) {
                log.info("token不存在或已过期，openid: {}", openid);
                JSONObject result = new JSONObject();
                result.put("isLoggedIn", false);
                result.put("message", "登录已过期");
                return result;
            }

            // 4. 验证token有效性
            LoginMember loginMember = tokenService.getLoginMemberByToken(token);
            if (loginMember == null) {
                log.info("登录信息不存在，openid: {}", openid);
                JSONObject result = new JSONObject();
                result.put("isLoggedIn", false);
                result.put("message", "登录已过期");
                return result;
            }

            // 5. 检查token是否即将过期（7天内）
            long currentTime = System.currentTimeMillis();
            long expireTime = loginMember.getExpireTime();
            long timeLeft = expireTime - currentTime;
            long sevenDays = 7 * 24 * 60 * 60 * 1000L; // 7天的毫秒数

            boolean needRefresh = timeLeft < sevenDays;

            log.info("微信登录状态检查完成，openid: {}, 剩余时间: {}ms, 需要刷新: {}",
                    openid, timeLeft, needRefresh);

            JSONObject result = new JSONObject();
            result.put("isLoggedIn", true);
            result.put("token", token);
            result.put("memberId", member.getId());
            result.put("nickname", member.getNickname());
            result.put("avatar", member.getAvatar());
            result.put("phone", member.getPhoneHidden());
            result.put("level", member.getLevel());
            result.put("levelName", member.getLevelName());
            result.put("expireTime", expireTime);
            result.put("timeLeft", timeLeft);
            result.put("needRefresh", needRefresh);

            return result;

        } catch (Exception e) {
            log.error("检查微信登录状态失败", e);
            JSONObject result = new JSONObject();
            result.put("isLoggedIn", false);
            result.put("message", "检查登录状态失败");
            return result;
        }
    }

    /**
     * 刷新微信登录token
     * 
     * @param openid 微信openid
     * @return 新的登录信息
     */
    public JSONObject refreshLoginToken(String openid) {
        try {
            log.info("刷新微信登录token，openid: {}", openid);

            // 1. 根据openid查找用户
            LambdaQueryWrapper<MemberWechat> wechatQuery = new LambdaQueryWrapper<>();
            wechatQuery.eq(MemberWechat::getOpenid, openid);
            MemberWechat memberWechat = memberWechatMapper.selectOne(wechatQuery);

            if (memberWechat == null) {
                throw new RuntimeException("用户不存在");
            }

            // 2. 获取用户信息
            Member member = memberMapper.selectById(memberWechat.getMemberId());
            if (member == null) {
                throw new RuntimeException("用户信息不存在");
            }

            // 3. 生成新的token
            String newToken = getToken(memberWechat.getMemberId());
            if (StrUtil.isBlank(newToken)) {
                throw new RuntimeException("生成新token失败");
            }

            // 4. 获取登录信息并刷新
            LoginMember loginMember = tokenService.getLoginMemberByToken(newToken);
            if (loginMember != null) {
                tokenService.refreshMemberToken(loginMember);
                log.info("刷新token成功，openid: {}", openid);
            }

            // 5. 构造返回结果
            JSONObject result = new JSONObject();
            result.put("token", newToken);
            result.put("memberId", member.getId());
            result.put("nickname", member.getNickname());
            result.put("avatar", member.getAvatar());
            result.put("phone", member.getPhoneHidden());
            result.put("level", member.getLevel());
            result.put("levelName", member.getLevelName());
            result.put("expireTime", loginMember != null ? loginMember.getExpireTime() : 0);
            result.put("message", "token刷新成功");

            return result;

        } catch (Exception e) {
            log.error("刷新微信登录token失败", e);
            throw new RuntimeException("刷新token失败: " + e.getMessage());
        }
    }

    private Long generateMemberId() {
        // 使用数据库自增ID，让数据库自动生成
        // 这里返回null，让MyBatis-Plus自动处理ID生成
        return null;
    }
}

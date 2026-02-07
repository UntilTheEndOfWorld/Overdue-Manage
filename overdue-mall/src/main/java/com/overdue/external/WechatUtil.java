package com.overdue.external;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.overdue.external.resp.AccessTokenResp;
import com.overdue.external.resp.BaseResp;
import com.overdue.external.resp.UserInfoResp;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class WechatUtil {

    @Value("${wechat.appId}")
    private String appId;

    @Value("${wechat.secret}")
    private String secret;

    public boolean validParam(String signature, String... arr) {
        Arrays.sort(arr);
        StringBuilder sb = new StringBuilder();
        String[] var2 = arr;
        int var3 = arr.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            String a = var2[var4];
            sb.append(a);
        }

        return signature.equals(DigestUtils.sha1Hex(sb.toString()));
    }

    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
    private static final String USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";

    public AccessTokenResp getAccessToken(String code) {
        String url = ACCESS_TOKEN_URL.replace("APPID", appId).replace("SECRET", secret).replace("CODE", code);
        String res = HttpUtil.get(url);
        AccessTokenResp resp = JSON.parseObject(res, AccessTokenResp.class);
        validResp(resp);
        return resp;
    }

    public UserInfoResp getUserInfo(String accessToken, String openid) {
        String url = USER_INFO_URL.replace("ACCESS_TOKEN", accessToken).replace("OPENID", openid);
        String res = HttpUtil.get(url);
        UserInfoResp resp = JSON.parseObject(res, UserInfoResp.class);
        validResp(resp);
        return resp;
    }

    public void validResp(BaseResp resp) {
        if (resp.getErrcode() != null) {
            throw new ExternalException(resp.getErrcode() + "", resp.getErrmsg());
        }
    }
}

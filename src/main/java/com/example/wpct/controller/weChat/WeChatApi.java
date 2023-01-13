package com.example.wpct.controller.weChat;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.wpct.config.WxPayConfig;
import com.example.wpct.entity.VillageDto;
import com.example.wpct.entity.WechatUser;
import com.example.wpct.mapper.VillageMapper;
import com.example.wpct.mapper.WechatUserMapper;
import com.example.wpct.service.BuildService;
import com.example.wpct.service.HousingInformationService;
import com.example.wpct.service.RoomService;
import com.example.wpct.service.WechatPayService;
import com.example.wpct.utils.ResultBody;
import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.exception.HttpCodeException;
import com.wechat.pay.contrib.apache.httpclient.exception.NotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "微信相关API")
@RestController
@RequestMapping("/weixin")
public class WeChatApi {
    @Resource
    WxPayConfig wxPayConfig;

    @Autowired
    WechatPayService wechatPayService;

    @Autowired
    WechatUserMapper wechatUserMapper;

    @Autowired
    VillageMapper villageMapper;

    @Autowired
    BuildService buildService;

    @Autowired
    RoomService roomService;

    @Autowired
    HousingInformationService housingInformationService;

    @ApiOperation("获取openid和昵称")
    @RequestMapping(value = "/getOpenid",method = RequestMethod.GET)
    public Object get(HttpServletResponse response, HttpServletRequest request) throws IOException {
        String code = request.getParameter("code");
        if(code == null){
            return ResultBody.fail("request中没携带code");
        }
        // String state = request.getParameter("state");

        /**
         * 获取code后，请求以下链接获取access_token：
         * https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
         */
        //2.通过code换取网页token
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                "appid=" + wxPayConfig.getAppid() +
                "&secret=" + wxPayConfig.getAppSecret() +
                "&code=" + code +
                "&grant_type=authorization_code";
        String s = HttpUtil.get(url);
        JSONObject object = JSON.parseObject(s);


        /**
         {
         "access_token":"ACCESS_TOKEN",
         "expires_in":7200,
         "refresh_token":"REFRESH_TOKEN",
         "openid":"OPENID",
         "scope":"SCOPE"
         }
         */
        String accessToken = object.getString("access_token");
        String openid = object.getString("openid");

        //3.根据openid和token获取用户基本信息
        /**
         * http：GET（请使用https协议）
         * https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN
         */
        String userUrl = "https://api.weixin.qq.com/sns/userinfo?" +
                "access_token=" + accessToken +
                "&openid=" + openid +
                "&lang=zh_CN";

        // String userRes = WeiXinUtil.httpRequest(userUrl, "GET", null);
        String userRes = HttpUtil.get(userUrl);

        //将openid返回给前端  10-19
        Gson gson = new Gson();
        HashMap getMap = gson.fromJson(userRes, HashMap.class);
        String getOpenid = (String) getMap.get("openid");
        String nickname = (String) getMap.get("nickname");
        Map<String,String> rMap = new HashMap<>();
        rMap.put("openid",getOpenid);
        rMap.put("nickname",nickname);
        JSONObject jsonObject = JSONObject.parseObject(userRes);
        /*WechatUser user = wechatUserService.query().eq("openid", openid).one();
        if (user == null) {   //TODO *一个人也可以绑定多个房屋信息 这里逻辑是一个openid（用户）只能绑定一套房屋
            //保存用户信息
            WechatUser wechatUser = new WechatUser();
            wechatUser.setOpenid(jsonObject.getString("openid"));
            wechatUser.setNickname(jsonObject.getString("nickname"));
            wechatUserService.save(wechatUser);
        }*/
//        if (user == null || user.getPid() == null) {
//            //TODO 跳转注册页面
//        }
//        // TODO 绑定身份证-> 跳转首页


        userUrl = "https://60z8193p42.goho.co//zqb/new.html" + "?openid=" + jsonObject.getString("openid");

        //TODO 如果没有授权登录 跳转注册页面  未完成
        //response.sendRedirect(userUrl);

        if (wechatPayService.getByOpenid(openid) == null || wechatPayService.getByOpenid(openid).size() == 0 ){
            rMap.put("isRegister",Boolean.FALSE.toString());
        }else {
            rMap.put("isRegister",Boolean.TRUE.toString());
        }
        return ResultBody.ok(rMap);
    }

    @ApiOperation("缴交物业费")
    @PostMapping("/property/pay")
    public Object wechatPay(@RequestParam String openid, @RequestParam int orderNo) throws Exception {
        String resultJson = wechatPayService.jsapiPay(openid, orderNo);
        return ResultBody.ok(resultJson);
    }

    /*@ApiOperation("物业费余额充值")
    @RequestMapping(value = "/property/balance/pay",method =RequestMethod.POST)
    public Object investProperty(@RequestParam String openid,@RequestParam int money,@RequestParam int hid) throws Exception {
        String resultJson = wechatPayService.investProperty(openid, money,hid);
        return ResultBody.ok(resultJson);
    }*/

    @ApiOperation("公摊费余额充值")
    @RequestMapping(value = "/share/balance/pay",method = RequestMethod.POST)
    public Object investShare(@RequestParam String openid,@RequestParam int money,@RequestParam int hid) throws Exception {
        String resultJson = wechatPayService.investShare(openid, money,hid);
        return ResultBody.ok(resultJson);
    }

    /**
     * 需要传入：nickname、openid、name、telephone、villageName、buildName、roomNum、relation
     */
    @ApiOperation(value = "微信用户绑定房屋信息",notes = "id、hid不传")
    @RequestMapping(value = "/bind",method = RequestMethod.POST)
    public Object bind(@RequestBody WechatUser wechatUser){
        if (housingInformationService.getByVbr(wechatUser.getVillageName(),wechatUser.getBuildName(),wechatUser.getRoomNum()) != null){
            int hid = (int) housingInformationService.getByVbr(wechatUser.getVillageName(),wechatUser.getBuildName(),wechatUser.getRoomNum()).getId();
        }else {
            return ResultBody.fail("房屋信息表中不存在给房屋");
        }
        wechatPayService.bind(wechatUser);
        return ResultBody.ok(null);
    }

    @ApiOperation("支付结果通知")
    @PostMapping("/jsapi/notify")
    public Object wechatPayNotify(HttpServletRequest request, HttpServletResponse response) throws GeneralSecurityException, NotFoundException, IOException, HttpCodeException {
        String notify = wechatPayService.payNotify(request, response);
        return ResultBody.ok(notify);
    }

    @ApiOperation("微信绑定房屋信息——返回树形结构楼栋")
    @RequestMapping(value = "/house/tree",method = RequestMethod.GET)
    public Object tree(){
        List<VillageDto> villages;
        //QueryWrapper queryWrapper = new QueryWrapper<>();
        villages = villageMapper.selectList(null);
        JSONArray tree = JSONArray.parseArray(JSON.toJSONString(villages));
        for (Object o : tree) {
            JSONObject village = ((JSONObject) o);
            JSONArray builds = JSONArray.parseArray(
                    JSON.toJSONString(buildService.listByVillage(village.getInteger("id")))
            );
            for (Object o1 : builds) {
                JSONObject build = ((JSONObject) o1);
                JSONArray rooms = JSONArray.parseArray(
                        JSON.toJSONString(roomService.listByBuild(build.getInteger("id")))
                );
                build.put("children",rooms);
            }
            village.put("children",builds);
        }
        JSONObject res = new JSONObject();
        res.put("tree",tree);
        return ResultBody.ok(res);
    }
}

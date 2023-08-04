package com.example.wpct.service.impl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.*;
import com.example.wpct.mapper.*;
import com.example.wpct.service.ExamineService;
import com.example.wpct.utils.ResultBody;
import com.example.wpct.utils.WxSendMsgUtil;
import com.example.wpct.utils.page.PageRequest;
import com.example.wpct.utils.page.PageResult;
import com.example.wpct.utils.page.PageUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.security.SecurityUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;


/**
 * @Author ZXX
 * @ClassName ExamineServiceImpl
 * @Description TODO
 * @DATE 2022/10/10 17:04
 */

@Slf4j
@Service
public class ExamineServiceImpl extends ServiceImpl<ExamineMapper, ExamineDto> implements ExamineService {
    public static Page page;
    @Autowired
    WechatUserMapper wechatUserMapper;
    @Autowired
    VillageMapper villageMapper;
//    @Autowired
//    RoomMapper roomMapper;
    @Autowired
    BuildMapper buildMapper;

    @Autowired
    HousingInformationMapper housingInformationMapper;

    @Resource
    private RestTemplate restTemplate;

    @Autowired
    WxSendMsgUtil wxSendMsgUtil;

    @Autowired
    RoleMapper roleMapper;

    @Autowired
    ExamineMapper examineMapper;
    @Override
    public ResultBody addExamine(String openid,String examineContent,int hid) {
        List<WechatUser> list = wechatUserMapper.getByOpenid(openid);
        Set set = new HashSet();
        for (WechatUser wechatUser : list){
            set.add(wechatUser.getName());
        }
        if (set == null || set.size() == 0) {
            return ResultBody.ok("用户不存在");
        } else {
            ExamineDto examineDto = new ExamineDto();
            examineDto.setOpenid(openid);
            examineDto.setCommitTime(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Calendar.getInstance().getTime()));
            examineDto.setExamineContent(examineContent);
            examineDto.setApprovalStatus("");
            examineDto.setResolveHandle("");
            examineDto.setHid(hid);
            baseMapper.insert(examineDto);
            return ResultBody.ok("新增成功");
        }
    }
    @Override
    public PageResult listExamine(int pageNum, int pageSize) {
        PageRequest pageRequest = new PageRequest(pageNum, pageSize);
        return PageUtil.getPageResult(getPageInfo(pageRequest),page);
    }
    @SneakyThrows
    private PageInfo<?> getPageInfo(PageRequest pageRequest) {
        int pageNum = pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize();
        //设置分页数据
        page = PageHelper.startPage(pageNum, pageSize);
        List<ExamineDto> res;
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser) subject.getPrincipal();
        if (user == null){
            throw new Exception("请先登录");
        }
        QueryWrapper queryWrapper2 = new QueryWrapper();
        queryWrapper2.select().orderByDesc("commit_time");
        res = examineMapper.selectList(queryWrapper2);

        //res = baseMapper.selectList(null);
        for (int i = 0; i < res.size(); i++) {
            ExamineDto examineDto = res.get(i);
            QueryWrapper queryWrapper = new QueryWrapper<>();
            queryWrapper.in("openid", examineDto.getOpenid());
           /* QueryWrapper queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.in("id", examineDto.getBid());
            QueryWrapper queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.in("id", examineDto.getRid());
            QueryWrapper queryWrapper3 = new QueryWrapper<>();
            queryWrapper3.in("id", examineDto.getVid());*/
            int hid = examineDto.getHid();
            QueryWrapper queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("id",hid);
            HousingInformationDto housingInformationDto = housingInformationMapper.selectOne(queryWrapper1);
            //examineDto.setWechatUser(wechatUserMapper.selectOne(queryWrapper));
            List<WechatUser> list = wechatUserMapper.getByOpenid(examineDto.getOpenid());
            if (list == null){
                examineDto.setWechatUser(null);
            }else {
                Set<WechatUser> set = new HashSet();
                for (WechatUser wechatUser : list){
                    set.add(wechatUser);
                }
                examineDto.setWechatUser((WechatUser) set.toArray()[0]);
            }
            if (housingInformationDto != null){
                examineDto.setVillageName(housingInformationDto.getVillageName());
                examineDto.setBuildName(housingInformationDto.getBuildNumber());
                examineDto.setRoomNum(housingInformationDto.getHouseNo());
            }
   //         examineDto.setRoomDto(roomMapper.selectOne(queryWrapper2));
        }
        return new PageInfo<>(res);
    }
    @SneakyThrows
    public String soluExamine(Integer id, String resolveMsg) {
        UpdateWrapper<ExamineDto> updateWrapper = new UpdateWrapper<>();
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser)subject.getPrincipal();
        if (user == null){
            throw new Exception("请先登录");
        }
        //根据openid和id锁定处理的信息
        updateWrapper.eq("id",id);
        ExamineDto examineDto = baseMapper.selectOne(updateWrapper);
        System.out.println(resolveMsg);
        examineDto.setResolveHandle(resolveMsg);
        examineDto.setUname(user.getName());
        baseMapper.update(examineDto,updateWrapper);
        return examineDto.getExamineContent();
    }

    public String approval(Integer id) {
        UpdateWrapper<ExamineDto> updateWrapper = new UpdateWrapper<>();
        //根据openid和id锁定处理的信息
        updateWrapper.eq("id",id);
        ExamineDto examineDto = baseMapper.selectOne(updateWrapper);
        examineDto.setApprovalStatus("是");
        baseMapper.update(examineDto,updateWrapper);
        return examineDto.getExamineContent();
    }

    public String noPass(Integer id) {
        UpdateWrapper<ExamineDto> updateWrapper = new UpdateWrapper<>();
        //根据openid和id锁定处理的信息
        updateWrapper.eq("id",id);
        ExamineDto examineDto = baseMapper.selectOne(updateWrapper);
        examineDto.setApprovalStatus("不通过");
        baseMapper.update(examineDto,updateWrapper);
        return examineDto.getExamineContent();
    }

    @Override
    public ResultBody userExamineList(String openid) {
        QueryWrapper<ExamineDto> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid",openid);
        List<ExamineDto> examineDto = baseMapper.selectList(queryWrapper);
        for (ExamineDto examineDto1 : examineDto){
            QueryWrapper queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("id",examineDto1.getHid());
            HousingInformationDto housingInformationDto = housingInformationMapper.selectOne(queryWrapper1);
            examineDto1.setVillageName(housingInformationDto.getVillageName());
            examineDto1.setBuildName(housingInformationDto.getBuildNumber());
            examineDto1.setRoomNum(housingInformationDto.getHouseNo());
            List<WechatUser> list = wechatUserMapper.getByOpenid(examineDto1.getOpenid());
            Set set = new HashSet();
            for (WechatUser wechatUser : list){
                set.add(wechatUser.getName());
            }
            WechatUser wechatUser = new WechatUser();
            wechatUser.setName((String) set.toArray()[0]);
            examineDto1.setWechatUser(wechatUser);
        }
        return ResultBody.ok(examineDto);
    }
    @SneakyThrows
    @Override
    public ResultBody examineHandle(Integer id,String resolveMsg){
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser)subject.getPrincipal();
        if (user == null){
            throw new Exception("请先登录");
        }
        if(roleMapper.getById(user.getRole()).getName().equals("超级管理员")){
            return ResultBody.ok(approval(id));
        }else{
            return ResultBody.ok(soluExamine(id,resolveMsg));
        }
    }

    public ResultBody sendMsg(int hid,String status,String time,String content,String openid,String uname) throws Exception{
        WxMsgConfig requestData = this.getMsgConfig(hid,status,time,content,openid,uname);

        log.info("推送消息请求参数：{}", JSON.toJSONString(requestData));

        String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + wxSendMsgUtil.getAccessToken();
        log.info("推送消息请求地址：{}", url);
        JSONObject responseData = postData(url, requestData);
        log.info("推送消息返回参数：{}", JSON.toJSONString(responseData));

        Integer errorCode = responseData.getInteger("errcode");
        String errorMessage = responseData.getString("errmsg");
        if (errorCode == 0) {
            log.info("推送消息发送成功");
            return    ResultBody.ok(responseData);
        } else {
            log.info("推送消息发送失败,errcode：{},errorMessage：{}", errorCode, errorMessage);
        }
        return ResultBody.ok(errorCode.toString());
    }

    @SneakyThrows
    public WxMsgConfig getMsgConfig(int hid,String status,String time,String content,String openid,String uname) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",hid);
        HousingInformationDto housingInformationDto = housingInformationMapper.selectOne(queryWrapper);
        String villageName = housingInformationDto.getVillageName();
        String buildName = housingInformationDto.getBuildNumber();
        String roomNum = housingInformationDto.getHouseNo();
        WxMsgTemplateHasten wxMsgTemplateHasten = new WxMsgTemplateHasten();
        wxMsgTemplateHasten.setFirst(villageName + "-"
                + buildName + "-"
                + roomNum +
                "反馈通知");
        //处理人
        wxMsgTemplateHasten.setKeyword1(uname);
        /*处理时间*/
        wxMsgTemplateHasten.setKeyword2(time);
        /*处理结果*/
        wxMsgTemplateHasten.setKeyword3(status);
        //处理描述
        wxMsgTemplateHasten.setKeyword4("已处理");

        //BigDecimal bigDecimal = new BigDecimal(100);
        //wxMsgTemplateHasten.setRemark(content);
        /*消息推送配置参数拼接*/
        WxMsgConfig wxMsgConfig = new WxMsgConfig();
        wxMsgConfig.setTouser(openid);
        wxMsgConfig.setTemplate_id("HGkWv87CQi5p0Ob6U6JOXuxNf0i8hErJf0-_UWPrplk");
        wxMsgConfig.setData(wxMsgTemplateHasten);
        return wxMsgConfig;
    }

    /**
     * 发送请求
     */
    public JSONObject postData(String url, WxMsgConfig param) {
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        HttpEntity<WxMsgConfig> httpEntity = new HttpEntity<>(param, headers);
        JSONObject jsonResult = restTemplate.postForObject(url, httpEntity, JSONObject.class);
        return jsonResult;
    }
}

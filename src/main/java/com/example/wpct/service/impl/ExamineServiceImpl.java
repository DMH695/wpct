package com.example.wpct.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.*;
import com.example.wpct.mapper.*;
import com.example.wpct.service.ExamineService;
import com.example.wpct.utils.ResultBody;
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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
            return ResultBody.ok("???????????????");
        } else {
            ExamineDto examineDto = new ExamineDto();
            examineDto.setOpenid(openid);
            examineDto.setCommitTime(LocalDateTime.now());
            examineDto.setExamineContent(examineContent);
            examineDto.setApprovalStatus("");
            examineDto.setResolveHandle("");
            examineDto.setHid(hid);
            baseMapper.insert(examineDto);
            return ResultBody.ok("????????????");
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
        //??????????????????
        page = PageHelper.startPage(pageNum, pageSize);
        List<ExamineDto> res;
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser) subject.getPrincipal();
        if (user == null){
            throw new Exception("????????????");
        }
        QueryWrapper queryWrapper2 = new QueryWrapper();
        queryWrapper2.select();
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
            throw new Exception("????????????");
        }
        //??????openid???id?????????????????????
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
        //??????openid???id?????????????????????
        updateWrapper.eq("id",id);
        ExamineDto examineDto = baseMapper.selectOne(updateWrapper);
        examineDto.setApprovalStatus("???");
        baseMapper.update(examineDto,updateWrapper);
        return examineDto.getExamineContent();
    }

    public String noPass(Integer id) {
        UpdateWrapper<ExamineDto> updateWrapper = new UpdateWrapper<>();
        //??????openid???id?????????????????????
        updateWrapper.eq("id",id);
        ExamineDto examineDto = baseMapper.selectOne(updateWrapper);
        examineDto.setApprovalStatus("?????????");
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
            throw new Exception("????????????");
        }
        if(roleMapper.getById(user.getRole()).getName().equals("???????????????")){
            return ResultBody.ok(approval(id));
        }else{
            return ResultBody.ok(soluExamine(id,resolveMsg));
        }
    }
}

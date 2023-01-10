package com.example.wpct.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.ExamineDto;
import com.example.wpct.entity.SysUser;
import com.example.wpct.entity.VillageDto;
import com.example.wpct.entity.WechatUser;
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
import java.util.List;


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
    @Autowired
    RoomMapper roomMapper;
    @Autowired
    BuildMapper buildMapper;
    @Override
    public ResultBody addExamine(String openid,String examineContent,int vid,int bid,int rid) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid", openid);
        WechatUser wechatUser = wechatUserMapper.selectOne(queryWrapper);
        if (wechatUser == null) {
            return ResultBody.ok("用户不存在");
        } else {
            ExamineDto examineDto = new ExamineDto();
            examineDto.setOpenid(openid);
            examineDto.setCommitTime(LocalDateTime.now());
            examineDto.setExamineContent(examineContent);
            examineDto.setApprovalStatus("否");
            examineDto.setBid(bid);
            examineDto.setVid(vid);
            examineDto.setRid(rid);
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
        List<ExamineDto> res;
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser) subject.getPrincipal();
        if (user == null){
            throw new Exception("请先登录");
        }
        int pageNum = pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize();
        //设置分页数据
        page = PageHelper.startPage(pageNum, pageSize);
        if (user.getRole().equals("超级管理员")) {
            QueryWrapper queryWrapper = new QueryWrapper<>();
            queryWrapper.isNotNull("uname");
            res = baseMapper.selectList(queryWrapper);
        } else {
            res = baseMapper.selectList(null);
        }
        for (int i = 0; i < res.size(); i++) {
            ExamineDto examineDto = res.get(i);
            QueryWrapper queryWrapper = new QueryWrapper<>();
            queryWrapper.in("openid", examineDto.getOpenid());
            QueryWrapper queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.in("id", examineDto.getBid());
            QueryWrapper queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.in("id", examineDto.getRid());
            QueryWrapper queryWrapper3 = new QueryWrapper<>();
            queryWrapper3.in("id", examineDto.getVid());
            examineDto.setWechatUser(wechatUserMapper.selectOne(queryWrapper));
            examineDto.setBuildDto(buildMapper.selectOne(queryWrapper1));
            examineDto.setVillageDto(villageMapper.selectOne(queryWrapper3));
            examineDto.setRoomDto(roomMapper.selectOne(queryWrapper2));
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

    @Override
    public ResultBody userExamineList(String openid) {
        QueryWrapper<ExamineDto> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid",openid);
        List<ExamineDto> examineDto = baseMapper.selectList(queryWrapper);
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
        if(user.getRole().equals("超级管理员")){
            return ResultBody.ok(approval(id));
        }else{
            return ResultBody.ok(soluExamine(id,resolveMsg));
        }
    }
}

package com.example.wpct.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.ExamineDto;
import com.example.wpct.entity.SysUser;
import com.example.wpct.entity.WechatUser;
import com.example.wpct.mapper.ExamineMapper;
import com.example.wpct.mapper.WechatUserMapper;
import com.example.wpct.service.ExamineService;
import com.example.wpct.utils.ResultBody;
import com.example.wpct.utils.page.PageRequest;
import com.example.wpct.utils.page.PageResult;
import com.example.wpct.utils.page.PageUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.security.SecurityUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    @Override
    public ResultBody addExamine(String openid,String examineContent) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid", openid);
        WechatUser wechatUser = wechatUserMapper.selectOne(queryWrapper);
        if (wechatUser == null) {
            return ResultBody.ok("用户不存在");
        } else {
            ExamineDto examineDto = new ExamineDto();
            examineDto.setOpenid(openid);
            examineDto.setName(wechatUser.getName());
            examineDto.setCommitTime(LocalDateTime.now());
            examineDto.setExamineContent(examineContent);
            examineDto.setApprovalStatus("否");
            baseMapper.insert(examineDto);
            return ResultBody.ok("新增成功");
        }
    }
    @Override
    public PageResult listExamine(int pageNum, int pageSize) {
        PageRequest pageRequest = new PageRequest(pageNum, pageSize);
        return PageUtil.getPageResult(getPageInfo(pageRequest),page);
    }
    private PageInfo<?> getPageInfo(PageRequest pageRequest) {
        int pageNum = pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize();
        //设置分页数据
        page = PageHelper.startPage(pageNum,pageSize);
        List<ExamineDto> res = baseMapper.listExamine();
        return new PageInfo<>(res);
    }

    public String soluExamine(Integer id, String openid, String resolveMsg) {
        UpdateWrapper<ExamineDto> updateWrapper = new UpdateWrapper<>();
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser)subject.getPrincipal();
        //根据openid和id锁定处理的信息
        updateWrapper.eq("id",id).eq("openid",openid);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("id",id);
        queryWrapper.eq("openid",openid);
        ExamineDto examineDto = baseMapper.selectOne(updateWrapper);
        examineDto.setResolveHandle(resolveMsg);
        examineDto.setUname(user.getName());
        baseMapper.update(examineDto,updateWrapper);
        return examineDto.getExamineContent();
    }

    public String approval(Integer id, String openid) {
        UpdateWrapper<ExamineDto> updateWrapper = new UpdateWrapper<>();
        //根据openid和id锁定处理的信息
        updateWrapper.eq("id",id).eq("openid",openid);
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
    @Override
    public ResultBody examineHandle(Integer id, String openid,String resolveMsg){
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser)subject.getPrincipal();
        if(user.getRole().equals("超级管理员")){
            return ResultBody.ok(approval(id,openid));
        }else{
            return ResultBody.ok(soluExamine(id,openid,resolveMsg));
        }
    }
}
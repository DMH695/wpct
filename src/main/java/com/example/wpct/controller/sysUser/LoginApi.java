package com.example.wpct.controller.sysUser;

import com.alibaba.fastjson.JSONObject;
import com.example.wpct.entity.LoginType;
import com.example.wpct.entity.SysUser;
import com.example.wpct.entity.UserToken;
import com.example.wpct.service.RoleService;
import com.example.wpct.service.SysUserService;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
@Api(tags = "系统用户登录")
@Slf4j
@RestController
@RequestMapping("/user")
public class LoginApi {
    @Autowired
    SysUserService sysUserService;

    @Autowired
    RoleService roleService;

    @ApiOperation("系统用户登录")
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public Object login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes attributes,
                        boolean rememberMe){
        SecurityUtils.getSubject().getSession().setTimeout(-1000L);
        SysUser user = new SysUser();
        user.setUsername(username);
        password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        user.setPassword(password);
        if(sysUserService.getByUserName(username) == null){
            return ResultBody.fail("用户不存在");
        }
        Subject subject = SecurityUtils.getSubject();
        UserToken token = new UserToken(username, user.getPassword(), true, LoginType.PASSWORD1);
        JSONObject res = new JSONObject();
        try {
            //进行验证，这里可以捕获异常，然后返回对应信息
            subject.login(token);
            String JSESSIONID = (String) subject.getSession().getId();
            res.put("JSESSIONID",JSESSIONID);
            res.put("role",roleService.getById(sysUserService.getByUserName(username).getRole()).getName());
            res.put("id",sysUserService.getByUserName(username).getId());
            res.put("name",sysUserService.getByUserName(username).getName());
            res.put("username",sysUserService.getByUserName(username).getUsername());
        } catch (UnknownAccountException e) {
            log.error("用户名不存在！", e);
            return ResultBody.fail("用户名不存在");
        } catch (AuthenticationException e) {
            log.error("账号或密码错误！", e);
            return ResultBody.fail("账号或密码错误！");
        } catch (AuthorizationException e) {
            log.error("没有权限！", e);
            return ResultBody.fail("没有权限！");
        }
        return ResultBody.ok(res);
    }
    @ApiOperation("退出登录")
    @RequestMapping(value = "/logout",method = RequestMethod.GET)
    public Object logout(){
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return ResultBody.ok("logout success");
    }

    @ApiOperation("测试")
    @RequestMapping(value = "test",method = RequestMethod.GET)
    @RequiresRoles("superadmin")
    public Object test(){
     return ResultBody.ok("6666");
    }

}

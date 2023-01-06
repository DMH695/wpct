package com.example.wpct.shiro;

import com.example.wpct.entity.SysUser;
import com.example.wpct.entity.UserToken;
import com.example.wpct.service.SysUserService;
import lombok.SneakyThrows;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;


public class CustomRealm extends AuthorizingRealm {
    @Autowired
    SysUserService sysUserService;

    /**
     * 在controller中添加注解的时候会调用该方法
     * @param principalCollection
     * @return 返回授权信息，触发shiro的授权机制
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
       /* if (principalCollection.toString().contains("patient")){
            return null;
        }else {
            //获取账号
            String account = (String) principalCollection.getPrimaryPrincipal();
            //通过用户名查找用户
            Doctor doctor = userService.getUserByAccount(account);
            //添加角色和权限，SimpleAuthorizationInfo：授权信息
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
            for(String role : userService.getRoleByUid(doctor.getId())){
                simpleAuthorizationInfo.addRole(role);
            }
            for(String permission : userService.getPermissionByUid(doctor.getId())){
                simpleAuthorizationInfo.addStringPermission(permission);
            }
            return simpleAuthorizationInfo;
        }*/
        SysUser user = (SysUser) principalCollection.getPrimaryPrincipal();
        //添加角色和权限，SimpleAuthorizationInfo：授权信息
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.addRole(user.getRole());
        return simpleAuthorizationInfo;
    }
    /**
     * 用户在登录的时候会调用的爱方法，实现登录验证
     * @param authenticationToken
     * @return 返回授权信息
     * @throws AuthenticationException
     */
    @SneakyThrows
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //获取用户的输入的账号
        UserToken userToken = (UserToken) authenticationToken;
        SysUser user = sysUserService.getByUserName(userToken.getUsername());
        if (user == null) {
            //没有返回登录用户名对应的SimpleAuthenticationInfo对象时,就会在LoginController中抛出UnknownAccountException异常
            throw new UnknownAccountException("用户不存在");
        }
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                user, //用户名
                user.getPassword(), //密码
                getName()  //realm name
        );
        return authenticationInfo;
        /*if (userToken.getLoginType() == LoginType.PASSWORD1) {
            //通过username从数据库中查找 User对象
            Doctor doctor = userService.getUserByAccount(userToken.getUsername());
            if (doctor == null) {
                //没有返回登录用户名对应的SimpleAuthenticationInfo对象时,就会在LoginController中抛出UnknownAccountException异常
                throw new UnknownAccountException("用户不存在");
            }
            SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                    doctor.getAccount(), //用户名
                    doctor.getPassword(), //密码
                    getName()  //realm name
            );
            return authenticationInfo;
        } else if (userToken.getLoginType() == LoginType.PASSWORD2) {
            Patient patient = patientService.getByTelephone(userToken.getUsername());
            if (patient == null) {
                //没有返回登录用户名对应的SimpleAuthenticationInfo对象时,就会在LoginController中抛出UnknownAccountException异常
                throw new UnknownAccountException("用户不存在");
            }
            SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                    patient,
                    patient.getPassword(), //密码
                    getName()  //realm name
            );
            return authenticationInfo;
        } else {
            String head = userToken.getAvatarUrl();
            String telephone = userToken.getTelephone();
            String openId = userToken.getOpenId();
            String sessionKey = userToken.getSessionKey();
            String username = userToken.getUsername();
            Patient patient = patientService.getByTelephone(telephone);
            if (patient == null){
                throw new UnknownAccountException("用户不存在");
            }
            //实现对数据的更新
            patientService.updateByTelephone(telephone, head, openId, sessionKey);
            return new SimpleAuthenticationInfo(patient, telephone, getName());
        }*/
    }
}

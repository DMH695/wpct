package com.example.wpct.config;

import com.example.wpct.filter.MyAuthenticationFilter;
import com.example.wpct.filter.MySessionManager;
import com.example.wpct.shiro.CustomRealm;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.eis.MemorySessionDAO;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    @Bean
    @ConditionalOnMissingBean //保证只有一个bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        //得到自动代理创建器
        DefaultAdvisorAutoProxyCreator defaultAAP = new DefaultAdvisorAutoProxyCreator();
        defaultAAP.setProxyTargetClass(true);
        return defaultAAP;
    }
    /**
     *  解决注解不生效的问题
     * @return
     */
    @Bean
    public static DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator(){
        DefaultAdvisorAutoProxyCreator defaultAAP = new DefaultAdvisorAutoProxyCreator();
        defaultAAP.setProxyTargetClass(true);
        return defaultAAP;
    }
    //将自己的验证方式加入容器,采用自定义域的方式触发shiro认证机制
    @Bean
    public CustomRealm myShiroRealm() {
        CustomRealm customRealm = new CustomRealm();
        return customRealm;
    }

    //权限管理，配置主要是Realm的管理认证
    @Bean
    public DefaultWebSecurityManager securityManager(Realm realm) {
        // 注意：这里的DefaultWebSecurityManager和我们之前的Demo使用的DefaultSecurityManager有区别
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 修改web环境下的默认sessionManager
        DefaultWebSessionManager sessionManager = new CustomerWebSessionManager();
        // 12小时(此设置会覆盖容器（tomcat）的会话过期时间设置)
        sessionManager.setGlobalSessionTimeout(3600000 * 12);
        securityManager.setSessionManager(sessionManager);
        // 禁用cookie来存sessionID(我们这里不用禁用，如果不传Token，则会使用原方式认证)
//        sessionManager.setSessionIdCookieEnabled(false);
        securityManager.setRealm(realm);
        return securityManager;
    }
    //Filter工厂，设置对应的过滤条件和跳转条件
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        //配置规则：anon必须放在前面,而且不能用HashMap来存储,因为它是无序的！！！
        Map<String, String> map = new LinkedHashMap<>();
        //登出
        //map.put("/logout", "logout");
        //对所有用户认证  authc:表示需要认证才能访问     anno:表示不需要认证就可以访问,直接注释掉下面的那一句就可以了
        shiroFilterFactoryBean.setLoginUrl("/user/login");
        map.put("/user/login/**","anon");
        //map.put("/swagger-ui.html","anon");
        map.put("/**", "anon");
        //首页
        shiroFilterFactoryBean.setSuccessUrl("/index");
        //错误页面，认证不通过跳转
        shiroFilterFactoryBean.setUnauthorizedUrl("https://www.baidu.com");
        Map<String, Filter> filters = shiroFilterFactoryBean.getFilters();
        // 将重写的Filter注入到factoryBean的filter中
        filters.put("authc", new MyAuthenticationFilter());
        shiroFilterFactoryBean.setFilters(filters);
        shiroFilterFactoryBean.setFilterChainDefinitionMap(map);
        return shiroFilterFactoryBean;
    }
    @Bean
    public ModularRealmAuthenticator modularRealmAuthenticator(){
        //自己重写的ModularRealmAuthenticator
        ModularRealmAuthenticator modularRealmAuthenticator = new ModularRealmAuthenticator();
        modularRealmAuthenticator.setAuthenticationStrategy(new AtLeastOneSuccessfulStrategy());
        return modularRealmAuthenticator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    @Bean
    public DefaultWebSessionManager getDefaultWebSessionManager() {
        MySessionManager defaultWebSessionManager = new MySessionManager();
        defaultWebSessionManager.setSessionDAO(new MemorySessionDAO());
        return defaultWebSessionManager;
    }
    /*@Bean
    public DefaultWebSessionManager sessionManager() {
        DefaultWebSessionManager defaultSessionManager = new CustomerWebSessionManager();
        //将sessionIdUrlRewritingEnabled属性设置成false，可以防止重定向携带 JSessionID
        defaultSessionManager.setSessionIdUrlRewritingEnabled(false);
        return defaultSessionManager;
    }*/
}

package com.example.wpct.aspect;

import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSONObject;
import com.example.wpct.annotation.SysLogAnnotation;
import com.example.wpct.entity.SysLogDto;
import com.example.wpct.entity.SysUser;
import com.example.wpct.mapper.SysLogMapper;
import com.example.wpct.utils.ResultBody;
import com.example.wpct.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Aspect
public class SysLogAspect {

    @Autowired
    private SysLogMapper logMapper;

    @Pointcut("@annotation(com.example.wpct.annotation.SysLogAnnotation)")
    public void opLogPointCut(){

    }
    /**
     * 记录操作日志
     * @param joinPoint 方法的执行点
     * @param result  方法返回值
     */
    @AfterReturning(returning = "result", value = "opLogPointCut()")
    public void saveOpLog(JoinPoint joinPoint, Object result) {

        // 获取RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 从获取RequestAttributes中获取HttpServletRequest的信息
        assert requestAttributes != null;
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
        try {
            SysLogDto sysLogDto = new SysLogDto();
            // 从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            //获取切入点所在的方法
            Method method = signature.getMethod();
            //获取操作
            SysLogAnnotation annotation = method.getAnnotation(SysLogAnnotation.class);
            if (annotation != null) {
                sysLogDto.setModel(annotation.opModel());
                sysLogDto.setType(annotation.opType());
                sysLogDto.setDescription(annotation.opDesc());
            }

            // 获取请求的类名
            String className = joinPoint.getTarget().getClass().getName();
            // 获取请求的方法名
            String methodName = method.getName();
            methodName = className + "." + methodName;
            sysLogDto.setMethod(methodName); // 类名.请求方法

            sysLogDto.setCreateDate(new Timestamp(System.currentTimeMillis())); //操作时间
            //操作用户 --登录时有把用户的信息保存在session中，可以直接取出
            assert request != null;
            String user = (String)request.getSession().getAttribute("username");
            if (!StringUtils.isNotEmpty(user)) {
                Subject subject = SecurityUtils.getSubject();
                SysUser sysUser = (SysUser) subject.getPrincipals();
                if (sysUser != null){
                    user = sysUser.getUsername();
                }
            }
            sysLogDto.setUsername(StringUtils.isNotEmpty(user) ? user : "null");

            sysLogDto.setIp(ServletUtil.getClientIP(request)); //操作IP
            sysLogDto.setUrl(request.getRequestURI()); // 请求URI

            // 方法请求的参数
            Map<String, String> rtnMap = convertMap(request.getParameterMap());
            // 将参数所在的数组转换成json
            String params = JSONObject.toJSONString(rtnMap);
            //获取json的请求参数
            if (rtnMap == null || rtnMap.size() == 0) {
                params = getJsonStrByRequest(request);
            }
            sysLogDto.setParams(params); // 请求参数
            ResultBody resultBody = (ResultBody) result;  //返回值信息
            //需要先判断返回值是不是Map <String, Object>，如果不是會拋異常，需要控制层的接口返回数据格式统一
            //如果嫌返回格式统一太麻烦建议日志保存时去掉操作结果
            String resultStr = JSONObject.toJSONString(resultBody);
            JSONObject object = JSONObject.parseObject(resultStr);
            if (resultStr.length() > 100){
                object.put("body","too long");
                resultStr = object.toJSONString();
            }
            sysLogDto.setResult(resultStr); //獲取方法返回值中的msg，如果上面的類型錯誤就拿不到msg就會拋異常

            //保存日志
            logMapper.insert(sysLogDto);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("日誌記錄異常，請檢查返回值是否是Map <String, Object>類型");
        }

    }



    /**
     * 转换request 请求参数
     *
     * @param paramMap request获取的参数数组
     */
    public Map<String, String> convertMap(Map<String, String[]> paramMap) {
        Map<String, String> rtnMap = new HashMap<>();
        for (String key : paramMap.keySet()) {
            rtnMap.put(key, paramMap.get(key)[0]);
        }
        return rtnMap;
    }



    /**
     * 获取json格式 请求参数
     */
    public String getJsonStrByRequest(HttpServletRequest request) {
        String param = null;
        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }

            JSONObject jsonObject = JSONObject.parseObject(responseStrBuilder.toString());
            param = jsonObject.toJSONString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return param;
    }


    /**
     * 转换异常信息为字符串
     *
     * @param exceptionName    异常名称
     * @param exceptionMessage 异常信息
     * @param elements         堆栈信息
     */
    public String stackTraceToString(String exceptionName, String exceptionMessage, StackTraceElement[] elements) {
        StringBuilder strBuff = new StringBuilder();
        for (StackTraceElement stet : elements) {
            strBuff.append(stet).append("\n");
        }
        return exceptionName + ":" + exceptionMessage + "\n\t" + strBuff;
    }



}

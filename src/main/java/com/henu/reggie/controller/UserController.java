package com.henu.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.henu.reggie.entity.Result;
import com.henu.reggie.entity.User;
import com.henu.reggie.service.UserService;
import com.henu.reggie.utils.SMSUtils;
import com.henu.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @PostMapping("/sendMsg")
    public Result<String> getRandomNum(HttpServletRequest request, @RequestBody User user){
        String phone = user.getPhone();

        if (!StringUtils.isNotEmpty(phone)){
            return Result.error("手机号不能为空");
        }

        //生成随机数
        String code = ValidateCodeUtils.generateValidateCode(4).toString();
        log.info("code:{}",code);

        //调用api发短信
        //SMSUtils.sendMessage("joker","SMS_462010462",phone,code);

        //将手机号和验证码存入redis
        redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
        //request.getSession().setAttribute(phone,code);
        return Result.success("验证码发送成功");
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String,String> map,HttpServletRequest request){
        String phone = map.get("phone");
        String code = map.get("code");
        //Object codeInSession = request.getSession().getAttribute(phone);
        //从redis中得到验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);

        if (codeInSession != null && codeInSession.equals(code)){
            //验证码正确，判断是否为新用户，是新用户自动注册
            LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
            lqw.eq(User::getPhone,phone);
            User user = userService.getOne(lqw);

            if(user == null){
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }

            request.getSession().setAttribute("user",user.getId());
            //登录成功删除redis中验证码
            redisTemplate.delete(phone);
            return Result.success(user);
        }
        return Result.error("验证码错误");
    }

    @PostMapping("/loginout")
    public Result<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("user");
        return Result.success("退出成功");
    }

}

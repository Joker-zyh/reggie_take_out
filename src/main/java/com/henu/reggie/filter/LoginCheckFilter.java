package com.henu.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.henu.reggie.common.BaseContext;
import com.henu.reggie.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.PathMatcher;

@Slf4j
@WebFilter(urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //1.获取请求的uri
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String uri = request.getRequestURI();
        log.info("请求的URI {}",uri);
        //2,3.判断请求是否需要处理
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };
        boolean check = check(urls, uri);
        if (check){
            filterChain.doFilter(request,response);
            return;
        }

        //4-1.判断登陆状态
        Long employeeId = (Long) request.getSession().getAttribute("employee");
        if (employeeId != null){
            BaseContext.setCurrentId(employeeId);
            filterChain.doFilter(request,response);
            return;
        }

        //4-2.判断登陆状态
        if (request.getSession().getAttribute("user") != null){
            Long userID = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userID);
            filterChain.doFilter(request,response);
            return;
        }

        //5.未登录，返回未登录结果,通过输出流的形式
        response.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));
        return;
    }

    private boolean check(String[] urls, String uri) {
        for(String url : urls){
            boolean match = PATH_MATCHER.match(url, uri);
            if(match){
                return true;
            }
        }
        return false;
    }


}

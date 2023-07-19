package com.henu.reggie.controller;


import com.henu.reggie.entity.Result;
import com.henu.reggie.utils.AliOSSUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class UploadController {
    @Autowired
    private AliOSSUtils aliOSSUtils;

    @PostMapping("/common/upload")
    public Result upload(MultipartFile file) throws IOException {
        String url = aliOSSUtils.upload(file);
        return Result.success(url);
    }

    @GetMapping("/common/download")
    public void downLoad(String name, HttpServletResponse response) throws IOException {
        response.getWriter().write(name);
    }

}

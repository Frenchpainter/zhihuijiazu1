package com.zhihui.zhihuijiazu.Controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.ParseException;

@Controller
public class DownloadController {

    @Value("${filePath.parentDir}")
    private String parentDirPath;

    @Value("${filePath.ip}")
    private String ip;


    @RequestMapping("/download/{filePath}/{fileName}")
    public void download(@PathVariable("filePath") String filePath,@PathVariable("fileName") String fileName, HttpServletResponse response) throws IOException, ParseException {
        if (filePath != null && !"".equals(filePath)) {
            filePath = parentDirPath + "/"+filePath+"/"+fileName;
            File file = new File(filePath);
            if (!file.exists()) {
                response.setContentType("text/html;charset=utf-8");
                response.getWriter().write("文件不存在");
                response.getWriter().close();
            } else {
                String suffix = filePath.substring(filePath.lastIndexOf(".") + 1);
                String filename = fileName;
                InputStream fis = null;
                byte[] buffer = null;
                try {
                    fis = new BufferedInputStream(new FileInputStream(filePath));
                    buffer = new byte[fis.available()];
                    fis.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fis != null) {
                        fis.close();
                    }
                }
                response.reset();
                // 先去掉文件名称中的空格,然后转换编码格式为utf-8,保证不出现乱码,这个文件名称用于浏览器的下载框中自动显示的文件名
                response.addHeader("Content-Disposition", "attachment;filename=" + toUtf8String(filename));
                response.addHeader("Content-Length", "" + file.length());
                OutputStream out = response.getOutputStream();
                response.setContentType("application/octet-stream");
                out.write(buffer);// 输出文件
                out.flush();
                out.close();
            }
        } else {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().write("文件不存在");
            response.getWriter().close();
        }
    }

    @RequestMapping("/view/{filePath}/{fileName}")
    public void view(@PathVariable("filePath") String filePath,@PathVariable("fileName") String fileName, HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=utf-8");
        if (filePath != null && filePath.length() != 0) {
            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
            File realFile = new File(parentDirPath + "/" + filePath+"/"+fileName);
            if (!realFile.exists()) {
                response.getWriter().write("文件不存在");
                response.getWriter().close();
            } else {
                ServletOutputStream output = response.getOutputStream();
                FileInputStream input = new FileInputStream(realFile);
                response.setContentType("image/" + fileType);
                boolean read = false;
                byte[] bytes = new byte[1024];

                int read1;
                while ((read1 = input.read(bytes)) != -1) {
                    output.write(bytes, 0, read1);
                }

                input.close();
                output.close();
            }
        } else {
            response.getWriter().write("文件不存在");
            response.getWriter().close();
        }
    }

    @RequestMapping("/fileview/{filePath}/{date}/{fileName}")
    public void view1(@PathVariable("filePath") String filePath,@PathVariable("date") String date,@PathVariable("fileName") String fileName, HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=utf-8");
        if (filePath != null && filePath.length() != 0) {
            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
            File realFile = new File(parentDirPath + "/" + filePath+"/"+date+"/"+fileName);
            if (!realFile.exists()) {
                response.getWriter().write("文件不存在");
                response.getWriter().close();
            } else {
                ServletOutputStream output = response.getOutputStream();
                FileInputStream input = new FileInputStream(realFile);
                response.setContentType("image/" + fileType);
                boolean read = false;
                byte[] bytes = new byte[1024];

                int read1;
                while ((read1 = input.read(bytes)) != -1) {
                    output.write(bytes, 0, read1);
                }

                input.close();
                output.close();
            }
        } else {
            response.getWriter().write("文件不存在");
            response.getWriter().close();
        }
    }

    @RequestMapping("/file/{fileName}")
    public void view(@PathVariable("fileName") String fileName, HttpServletResponse response) throws Exception {
        String filePath="C://project";
        response.setContentType("text/html;charset=utf-8");
        if (filePath != null && filePath.length() != 0) {
            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
            File realFile = new File( filePath+"/"+fileName);
            if (!realFile.exists()) {
                response.getWriter().write("文件不存在");
                response.getWriter().close();
            } else {
                ServletOutputStream output = response.getOutputStream();
                FileInputStream input = new FileInputStream(realFile);
                response.setContentType("image/" + fileType);
                boolean read = false;
                byte[] bytes = new byte[1024];

                int read1;
                while ((read1 = input.read(bytes)) != -1) {
                    output.write(bytes, 0, read1);
                }

                input.close();
                output.close();
            }
        } else {
            response.getWriter().write("文件不存在");
            response.getWriter().close();
        }
    }

    private static String toUtf8String(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0 && c <= 255) {
                sb.append(c);
            } else {
                byte[] b;
                try {
                    b = Character.toString(c).getBytes("utf-8");
                } catch (Exception ex) {
                    b = new byte[0];
                }
                for (int j = 0; j < b.length; j++) {
                    int k = b[j];
                    if (k < 0)
                        k += 256;
                    sb.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return sb.toString();
    }



}

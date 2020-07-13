package com.centify.boot.web.embedded.netty;

import io.netty.buffer.Unpooled;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
 *
 * <b>Author: tanlin [2020/6/2 14:38]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/6/2 14:38        tanlin            new file.
 * <pre>
 */
@RestController
@SpringBootApplication
public class NettyServletWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(NettyServletWebApplication.class, args);

    }

    @RequestMapping(value = "/test")
    public Map test(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Map result = new HashMap();
        result.put("name", "愚公");
        result.put("data", new Date());
        result.put("bigDecimal", new BigDecimal("120.000456").setScale(5, BigDecimal.ROUND_HALF_UP));
        result.put("longValue", new BigDecimal("120.000456").setScale(5, BigDecimal.ROUND_HALF_UP).longValue());
        result.put("floatValue", new BigDecimal("120.000456").setScale(5, BigDecimal.ROUND_HALF_UP).floatValue());
        result.put("intValue", new BigDecimal("120.000456").setScale(5, BigDecimal.ROUND_HALF_UP).intValue());
        result.put("map", new HashMap<>());
        result.put("age", 20);

        return result;
    }
    @GetMapping("/{a}/{b}")
    public Integer get(@PathVariable Integer a, @PathVariable Integer b) {
//        System.out.println("@PathVariable 参数");
        return a + b;
    }
    @GetMapping("/reqparam")
    public Map params(@RequestParam("id") Integer id, @RequestParam("name") String name, @RequestParam("inTime") String inTime){
        Map result = new HashMap();
        result.put("id",id);
        result.put("name",name);
        result.put("inTime",inTime);
        return result;
    }
    @PostMapping("/reqbody")
    public User getDefault(@RequestBody User user){
        return user;
    }
    @PostMapping("/form")
    public User getForm(User user){
        return user;
    }

    @PostMapping("/upload")
    public Object mult(@RequestParam("file") MultipartFile file){
        Map result = new HashMap();
        result.put("status","success");
        result.put("files",file.toString());
        result.put("getContentType",file.getContentType());
        result.put("getName",file.getOriginalFilename());

        try {
            RandomAccessFile file1=new RandomAccessFile("D:/AAAA"+file.getOriginalFilename(),"rw");
            FileChannel channel=file1.getChannel();
            channel.write(Unpooled.wrappedBuffer(file.getBytes()).nioBuffer());
            channel.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
    @PostMapping("/files")
    public Object mult(@RequestParam("files") MultipartFile[] files){
        Map result = new HashMap();
        result.put("status","success");
        Optional.ofNullable(files).ifPresent(items->{
            for(MultipartFile mu:files){
                result.put("file"+mu.getOriginalFilename(),mu.getOriginalFilename());
            }
        });

        return result;
    }
}

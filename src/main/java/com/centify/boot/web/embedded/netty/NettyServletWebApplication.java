package com.centify.boot.web.embedded.netty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
}

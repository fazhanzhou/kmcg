package com.greentech.kmcg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KmcgApplication {

    public static void main(String[] args) {
        try {
            //启动异常会在这里抛出，这里不打印异常，就不会在控制台输出异常信息
            SpringApplication.run(KmcgApplication.class, args);
        } catch (Exception e) {
            if(e.getClass().getName().contains("SilentExitException")) {
            } else {
                e.printStackTrace();
            }
        }

    }

}

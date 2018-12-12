package org.loopring.crawler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.loopring.crawler.config.JpaConfig;
import org.loopring.crawler.tasks.LoopringLinksTask;
import org.loopring.crawler.tasks.LoopringNewsTask;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class Main {

    private static Map<String, Object[]> appArgsMap = new HashMap<>();

    private static Map<String, String> appPropFilesMap = new HashMap<>();

    private static String args0;

    public static void main(String[] args) {
        System.out.println("args: " + Arrays.toString(args));

        Object[] appClasses = new Object[]{ LoopringLinksTask.class,
                                            LoopringNewsTask.class,
                                            JpaConfig.class };
        String configPropsFiles = "classpath:/config/loopring-all.yml";
        System.setProperty("spring.config.location", configPropsFiles);

        SpringApplication.run(appClasses, args);
    }

    public static String getArgs0() {
        return args0;
    }

}

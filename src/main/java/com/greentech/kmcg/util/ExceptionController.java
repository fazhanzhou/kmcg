package com.greentech.kmcg.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

/**
 * @ClassName ExceptionController
 * @Description: 错误统一处理类
 * @Author Administrator
 * @Date 2019/12/23
 * @Version V1.0
 **/
@Slf4j
@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(Exception.class)
    public JSONObject error(HttpServletRequest httpServletRequest, Exception e) {
        JSONObject jsonObject = new JSONObject();
        log.error(getMessage(e));
        Map map = httpServletRequest.getParameterMap();
        Iterator iterator = map.entrySet().iterator();
        Map.Entry entry;
        String value = "";
        StringBuilder stringBuilder = new StringBuilder();
        while (iterator.hasNext()) {
            entry = (Map.Entry) iterator.next();
            Object valueObj = entry.getValue();
            if (null == valueObj) {
                value = "";
            } else if (valueObj instanceof String[]) {
                String[] values = (String[]) valueObj;
                for (int i = 0; i < values.length; i++) {
                    value = values[i] + ",";
                }
                value = value.substring(0, value.length() - 1);
            } else {
                value = valueObj.toString();
            }
            stringBuilder.append(entry.getKey()).append("=").append(value).append("&");
        }
        log.error("url={},param:{}", httpServletRequest.getRequestURL(), e.getMessage());
        return jsonObject;
    }


    /**
     * 打印异常信息
     */
    private String getMessage(Exception e) {
        String swStr = null;
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
            swStr = sw.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage());
        }
        return swStr;
    }


}

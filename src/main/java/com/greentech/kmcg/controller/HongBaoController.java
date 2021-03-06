package com.greentech.kmcg.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.greentech.kmcg.bean.User;
import com.greentech.kmcg.repository.BaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @author: 周发展
 * @Date: 2020/5/22 14:41
 * @Description:
 */
@Slf4j
@Controller
public class HongBaoController {
    @Autowired
    IndexController indexController;
    @Autowired
    BaseRepository baseRepository;

    /**
     * 注册成功之后获取openid，然后跳转到问题页面
     *
     * @param ivtick
     * @param u_openid 用户openid
     * @param encdata
     * @param tel
     * @return
     */

    @Transactional
    @RequestMapping("/getOpenId")
    @ResponseBody
    public ModelAndView getOpenId(String ivtick, String u_openid, String encdata, String tel) {
        log.info("ivtick=" + ivtick);
        log.info("u_openid=" + u_openid);
        log.info("encdata=" + encdata);
        log.info("tel=" + tel);
        ModelAndView modelAndView = null;
        try {

            //检查openid是否存在

            String checkOpenIdSql = "select * from user_cg where openid='" + u_openid + "'";
            List<User> users = (List<User>) baseRepository.findBeansBySql(checkOpenIdSql, null, User.class);
            if (!CollectionUtils.isEmpty(users)) {
                log.info("已存在相同的openid=" + u_openid + "---tel=" + tel);

                String beforTel = users.get(0).getTel();
                if (!beforTel.equals(tel)) {
                    modelAndView = new ModelAndView("noopenid");
                    modelAndView.addObject("msg", "请不要更换手机号码登录，否则您将无法参加活动</br>使用原手机号重新登录");
                }else{
                    modelAndView = new ModelAndView("home1");
                }


                return modelAndView;
            }
            String updateSql = "update user_cg set openid='" + u_openid + "' where tel = " + tel;
            baseRepository.nativeSql(updateSql, null);
            modelAndView = indexController.question(tel, "111");
            modelAndView.addObject("openid", u_openid);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
        }
        return modelAndView;
    }

    @RequestMapping("/notice")
    public ModelAndView notice() {
        ModelAndView modelAndView = new ModelAndView("notice");
        return modelAndView;
    }


    /**
     * 发送红包
     */
    private JSONObject sendHongBao(int money, String openid) {
        System.out.println("发送红包");
        JSONObject json = new JSONObject();
        String getUrl = "http://www.yaoyaola.cn/index.php/exapi/SendRedPackToOpenid?";
        String uid = "24265";
        String type = "1";
        String orderid = orderId();
        String title = "呈贡区科普知识竞赛";
        String sendname = "呈贡区科协";
//        String wishing = "补发6.2日红包";
        String wishing = "补发两次10元，一共20元红包";
        String cburl = "https://njy.agri114.cn/hqt/json/hongBaoCallBack.action";
        Long reqtick = System.currentTimeMillis() / 1000;
        String appKey = "A82ED0D0E0155D3926E0A6B6B3EE60C4";
        String sign = encryption(uid + type + orderid + money + reqtick + openid + appKey);

        String dataString = "uid=" + uid + "&type=" + type + "&orderid=" + orderid +
                "&money=" + money + "&reqtick=" + reqtick + "&openid=" + openid +
                "&sign=" + sign + "&title=" + title + "&sendname=" + sendname + "&wishing=" + wishing;

        // 创建URL对象
        URL url;
        try {
            url = new URL(getUrl + dataString);
            // 打开连接 获取连接对象
            URLConnection connection = url.openConnection();
            InputStream in = connection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(in, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            StringBuilder buffer = new StringBuilder();
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            System.out.println(buffer.toString());
            JSONObject ticketJson = JSON.parseObject(buffer.toString());
            ticketJson.put("orderid", orderid);
            ticketJson.put("openid", openid);
            String errorMSG = (String) ticketJson.get("errmsg");
            saveToFile(ticketJson.toJSONString());
            if (null != errorMSG) {
                //发送失败
                log.debug(errorMSG);
                log.debug(ticketJson.toJSONString());
            } else {
                //发送成功
                log.debug(ticketJson.toJSONString());
            }

        } catch (Exception e2) {
            e2.printStackTrace();
        }

        return json;
    }


    public void saveToFile(String content) {
        try {
            String fileName = "D:\\hongbao\\60\\hongbao.txt";
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件

            FileWriter writer = new FileWriter(fileName, true);

            writer.write(content);
            writer.write("\r\n");

            writer.close();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }


    public static void main(String[] args) {
        HongBaoController hongBaoController = new HongBaoController();
        /*String openid = "orsKq0VXiv6LEVWsA4Otn6Gc6bPM";
        JSONObject jsonObject = hongBaoController.sendHongBao(2000, openid);
        log.debug(jsonObject.toJSONString());*/
        try {
            FileReader reader = new FileReader("D:\\hongbao\\60\\10.txt");
            BufferedReader bufferedReader = new BufferedReader(reader);
            String contentLine;
            while ((contentLine = bufferedReader.readLine()) != null) {
                JSONObject jsonObject = hongBaoController.sendHongBao(1000, contentLine);
                jsonObject.put("openid", contentLine);
                Thread.sleep(1000);
                log.debug(jsonObject.toJSONString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 生成订单号
    private String orderId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String first = sdf.format(new Date());
        Random r = new Random();
        int rr = r.nextInt(9999 - 1000 + 1) + 1000;
        return first + rr;
    }

    /**
     * 签名
     *
     * @param plainText
     * @return
     */
    private String encryption(String plainText) {
        String re_md5 = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }

            re_md5 = buf.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return re_md5;
    }
}
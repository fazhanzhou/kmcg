package com.greentech.kmcg.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.greentech.kmcg.bean.Score;
import com.greentech.kmcg.bean.User;
import com.greentech.kmcg.repository.BaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.beans.Transient;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @author: 周发展
 * @Date: 2020/5/8 13:41
 * @Description:
 */
@Slf4j
@Controller
public class ResultController {
    @Autowired
    BaseRepository baseRepository;
    @Autowired
    HttpServletRequest request;
    @Autowired
    IndexController indexController;

    /**
     * @param score   等分
     * @param tel     电话号码
     * @param useTime 总用时
     * @return
     */
    @Transactional
    @RequestMapping("/result")
    public ModelAndView addScore(String tel, Integer score, Long useTime) {
        ModelAndView modelAndView = new ModelAndView();

        //检查是否经过答题页面后到这里
        String verifyTel = (String) request.getSession().getAttribute("login");

        /*if (StringUtils.isBlank(verifyTel)) {
            modelAndView.setViewName("redirect:/home");
            System.out.println("请您先答题");
            return modelAndView;
        }*/
        if (null == score || StringUtils.isBlank(tel)) {
            modelAndView.setViewName("error1.html");
            modelAndView.addObject("msg", "缺少参数");
            return modelAndView;
        }
        User user = indexController.findUserByTel(tel);
        if (null == user) {
            modelAndView.setViewName("error1.html");
            modelAndView.addObject("msg", "用户不存在");
            return modelAndView;
        }

        //判断是否是呈贡区的
        String jiedao = user.getJiedao();
        String address = user.getAddress();
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        modelAndView.addObject("score", score);
        modelAndView.addObject("time", decimalFormat.format(Float.valueOf(useTime) / 1000));
        modelAndView.setViewName("score");
        if (!inPlace(address, jiedao)) {
            modelAndView.addObject("area", "本活动仅限呈贡区居民参与排名发红包");
        } else {
            //判断是不是最好的成绩，如果是更新成绩，不是不做任何操作

            //获取之前的成绩
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String scoreSql = "select * from score_cg where user_id=" + user.getId() + " and Date(date) ='" + date+"'";
            List<Score> scores = (List<Score>) baseRepository.findBeansBySql(scoreSql, null, Score.class);
            Score score1 = new Score();
            score1.setScore(score);
            score1.setTime(Long.valueOf(useTime));
            score1.setUserId(user.getId());
            score1.setDate(new Date());
            if (null == scores || scores.size() <= 0) {
                //之前没有成绩，直接存储成绩
                try {
                    baseRepository.merge(score1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                //之前有成绩，判断现在的成绩是否比之前好，如果好更新成绩，如果不好不做任何操作
                Score scoreOld = scores.get(0);
                int oldScore = scoreOld.getScore();
                long oldTime = scoreOld.getTime();
                if (score >= oldScore && useTime < oldTime) {
                    try {
                        score1.setId(scoreOld.getId());
                        baseRepository.merge(score1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        return modelAndView;
    }

    private boolean inPlace(String address, String jiedao) {
        //昆明市呈贡区+龙城、斗南、乌龙、洛龙、吴家营、雨花、马金铺、大渔、七甸、洛洋街道
        List<String> placeList = new ArrayList<>();
        placeList.add("龙城街道");
        placeList.add("斗南街道");
        placeList.add("乌龙街道");
        placeList.add("洛龙街道");
        placeList.add("吴家营街道");
        placeList.add("雨花街道");
        placeList.add("马金铺街道");
        placeList.add("大渔街道");
        placeList.add("七甸街道");
        placeList.add("洛洋街道");
        placeList.add("龙城");
        placeList.add("斗南");
        placeList.add("乌龙");
        placeList.add("洛龙");
        placeList.add("吴家营");
        placeList.add("雨花");
        placeList.add("马金铺");
        placeList.add("大渔");
        placeList.add("七甸");
        placeList.add("洛洋");
        if ("云南省-昆明市-呈贡区".equals(address)) {
            return placeList.contains(jiedao) ? true : false;
        } else {
            return false;
        }


    }


    @RequestMapping("/nostart")
    public ModelAndView noStart() {
        ModelAndView modelAndView = new ModelAndView();
        return modelAndView;
    }

    public static void main(String[] args) {
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        String re = decimalFormat.format((float) 23456787 / 1000);
    }

    /**
     * 验证手机号
     *
     * @param tel 手机号
     * @param yzm 验证码
     * @return
     */
    @RequestMapping("/verify")
    @ResponseBody
    public JSONObject verify(String tel, String yzm) {
        HttpSession session = request.getSession();
        JSONObject jsonObject = new JSONObject();
        if (StringUtils.isBlank(tel) || StringUtils.isBlank(yzm)) {
            jsonObject.put("code", 0);
            jsonObject.put("msg", "参数错误");
            return jsonObject;
        }
        String sessionYzm = (String) session.getAttribute(tel);
        System.out.println("sessionYzm=" + sessionYzm);
        System.out.println("yzm=" + yzm);
        if (!yzm.equals(sessionYzm)) {
            jsonObject.put("code", 0);
            jsonObject.put("msg", "验证码错误");
            return jsonObject;
        }
        //校验score参数，如果直接请求时没有这个参数的
        String score = (String) session.getAttribute("score");
        System.out.println("Score=" + score);
        if (StringUtils.isBlank(score)) {
            jsonObject.put("code", 0);
            jsonObject.put("msg", "检验失败，不符合规则");
            return jsonObject;
        }
        //获取ticket
        JSONObject jsonObject1 = sendHongBao(session, 1);
        int code = jsonObject1.getInteger("code");
        if (code == 1) {
            String ticket = jsonObject1.getString("ticket");
            jsonObject.put("code", 1);
            jsonObject.put("ticket", ticket);
        } else {
            jsonObject.put("msg", jsonObject1.getString("msg"));
            jsonObject.put("code", 0);
        }
        return jsonObject;
    }

    /**
     * 发送红包
     */
    private JSONObject sendHongBao(HttpSession session, int money) {
        System.out.println("发送红包");
        JSONObject json = new JSONObject();
        String getUrl = "http://www.yaoyaola.cn/index.php/exapi/hbticket?";
        String uid = "24265";
        String type = "0";
        String orderid = tradeNO();
        String title = "呈贡区科普知识竞赛";
        String sendname = "呈贡区科协";
        String wishing = "恭喜您获得红包";
        String cburl = "http://njy.agri114.cn/hqt/json/hongBaoCallBack.action";
        Long reqtick = System.currentTimeMillis() / 1000;
        String sign = encryption(uid + type + orderid + money + reqtick + "A82ED0D0E0155D3926E0A6B6B3EE60C4");
        String dataString = "uid=" + uid + "&type=" + type + "&orderid=" + orderid + "&money=" + money + "&reqtick=" + reqtick + "&sign=" + sign + "&act_name=" + title + "&sendname=" + sendname
                + "&wishing=" + wishing + "&cburl=" + cburl;

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
            String errorMSG = (String) ticketJson.get("errmsg");

            if (errorMSG.equals("success")) {
                // 获取到ticket，标记验证码失效
                json.put("code", "1");
                json.put("msg", "success");
                String firstTicket = (String) ticketJson.get("ticket");
                json.put("ticket", firstTicket);
                // updateMoney(money);
            } else if (errorMSG.equals("no money")) {
                json.put("code", "0");
                json.put("msg", "余额不足");
            } else {
                // 未知错误
                json.put("code", "0");
                json.put("msg", "未知错误");
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }


        //红包领取之后清除session
        session.invalidate();
        return json;
    }


    // 生成订单号
    private String tradeNO() {
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
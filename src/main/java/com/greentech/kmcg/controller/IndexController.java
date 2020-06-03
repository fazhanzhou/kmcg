package com.greentech.kmcg.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.greentech.kmcg.bean.*;
import com.greentech.kmcg.repository.BaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Controller
public class IndexController {
    @Autowired
    private BaseRepository baseRepository;
    @Autowired
    HttpServletRequest request;
    int i = 0;

    /**
     * 主页面
     *
     * @return
     */
    @RequestMapping(value = "/home1")
    public String home() {
       /* log.info(Thread.currentThread().getName()+"---i="+i++);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        return "home1";
    }

    /**
     * 个人信息
     *
     * @return
     */
    @RequestMapping(value = "/info/{tel}")
    public ModelAndView info(@PathVariable String tel) {
        ModelAndView modelAndView = new ModelAndView("info");
        if (StringUtils.isBlank(tel)) {
            modelAndView.setViewName("error1");
            modelAndView.addObject("msg", "参数错误");
            return modelAndView;
        }
        List<User> users = (List<User>) baseRepository.findBeansBySql("select * from user_cg where tel=" + tel, null, User.class);
        if (null != users && users.size() > 0) {
            modelAndView.addObject("data", users.get(0));
        } else {
            modelAndView.setViewName("error1");
            modelAndView.addObject("msg", "不存在手机号码为" + tel + "的用户");
            return modelAndView;
        }
        return modelAndView;
    }


    /**
     * 登录页面
     *
     * @return
     */
    @RequestMapping(value = "/login")
    public String login() {
        return "login";
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

    public boolean checkTime() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        log.debug(month + "-" + day + "-" + hour);
        if (month + 1 == 6 && day >= 1 && day <= 10 && hour >= 1 && hour <= 22) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 答题页面
     *
     * @param tel 手机号
     * @return 页面
     */
    @RequestMapping(value = "/question")
    public ModelAndView question(String tel,String timestamp) {
        ModelAndView modelAndView = new ModelAndView();
        //判断时间是否已经开始，开始时间为 6.1-6.10，每天1:00 -- 23:00
        if (!checkTime()) {
            modelAndView.setViewName("error1.html");
            modelAndView.addObject("msg", "活动还未开始<br/><br/>活动开始时间为<br/>6.1-6.10日每天1:00到23:00<br/>");
            return modelAndView;
        }

        if (StringUtils.isNotBlank(tel)) {
            User user = findUserByTel(tel);
            if (null != user) {
                modelAndView.setViewName("question.html");
                List<Question> list = (List<Question>) baseRepository.findBeansBySql("select * from question ORDER BY  RAND() LIMIT 10", null, Question.class);
                for (Question question : list) {
                    JSONObject jsonObject = JSON.parseObject(question.getAnswer());
                    List<Answer> answerList = new ArrayList<>();
                    for (String str : jsonObject.keySet()) {
                        Answer answer = new Answer();
                        answer.setAnswer(str + ":" + jsonObject.get(str));
                        answerList.add(answer);
                    }
                    question.setAnswers(answerList);
                }
                //答题开始的时候设置一个标志，当请求result接口的时候验证这个标志
                request.getSession().setAttribute("login", tel);
                modelAndView.addObject("data", list);
            } else {
                modelAndView.setViewName("login.html");
            }
        } else {
            modelAndView.setViewName("login.html");
        }
        return modelAndView;
    }

    /**
     * 显示排行
     *
     * @param tel 用户电话
     * @return
     */
    @RequestMapping(value = "/showScore/{tel}")
    public ModelAndView showScore(@PathVariable String tel) {
        ModelAndView modelAndView = new ModelAndView("score");
        return modelAndView;
    }

    /**
     * 显示排行
     *
     * @param tel 用户电话
     * @return
     */
    @RequestMapping(value = "/paiming/{tel}/{pageNum}")
    public ModelAndView paiming(@PathVariable String tel, @PathVariable Integer pageNum) {
        ModelAndView modelAndView = new ModelAndView("paiming.html");
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        List<User> users = (List<User>) baseRepository.findBeansBySql("select * from user_cg where tel = " + tel, null, User.class);
        if (null == users || users.size() <= 0) {
            modelAndView.setViewName("error1");
            modelAndView.addObject("msg", "不存在该用户信息");
            return modelAndView;
        }

        User user = users.get(0);
        String address = user.getAddress();
        String jiedao = user.getJiedao();
        if (!inPlace(address, jiedao)) {
            modelAndView.setViewName("error1");
            modelAndView.addObject("msg", "本活动仅限呈贡区居民参与排名发红包");
            return modelAndView;
        }
//        String sql = "SELECT t.*, @rownum \\:= @rownum + 1 AS rownum FROM (SELECT @rownum \\:= 0) r, (SELECT * FROM `score_cg` GROUP BY user_id ORDER BY score desc,time asc) AS t ";
        String allPaiMingSql = "SELECT t.*, @rownum \\:= @rownum +1 AS rownum FROM (SELECT @rownum \\:= 0) r, (SELECT a.* from score_cg a LEFT JOIN score_cg b ON a.user_id=b.user_id WHERE Date(a.date)='" + date + "'  GROUP BY a.user_id   ORDER BY a.score desc,a.time asc) as t";
        //我的今日排名
        String myPaiMingSql = "select b.* from \n" +
                "(SELECT t.*, @rownum \\:= @rownum + 1 AS rownum\n" +
                "FROM (SELECT @rownum \\:= 0) r, (SELECT * FROM `score_cg`WHERE Date(date)='" + date + "' GROUP BY user_id ORDER BY score desc,time asc) AS t) as b where b.user_id=" + user.getId();
        //我的昨日排名
        Calendar yesCalendar = Calendar.getInstance();
        yesCalendar.add(Calendar.DATE, -1);
        String yesDate = new SimpleDateFormat("yyyy-MM-dd").format(yesCalendar.getTime());
        log.debug(yesDate);
        String myYesPaiMingSql = "select b.* from \n" +
                "(SELECT t.*, @rownum \\:= @rownum + 1 AS rownum\n" +
                "FROM (SELECT @rownum \\:= 0) r, (SELECT * FROM `score_cg`WHERE Date(date)='" + yesDate + "' GROUP BY user_id ORDER BY score desc,time asc) AS t) as b where b.user_id=" + user.getId();

        List<Map> maps = baseRepository.getMap(myPaiMingSql);
        List<Map> yesMaps = baseRepository.getMap(myYesPaiMingSql);
        //今天已经答题
        if (null != maps && maps.size() > 0) {
            Map myMap = maps.get(0);
            int time = (Integer) myMap.get("time");
            float floatTime = ((float) time) / 1000;
            Number o1 = (Number) myMap.get("rownum");
            myMap.put("rownum",o1.intValue());
            //服务器返回的rownum类型为 BigInteger，本地为Double类型
            myMap.put("time", floatTime);
            modelAndView.addObject("myScore", myMap);
        }
        //昨天参加过活动
        if (null != yesMaps && yesMaps.size() > 0) {
            Map myYesMap = yesMaps.get(0);
            //服务器返回的rownum类型为 BigInteger，本地为Double类型
            Number o1 = (Number) myYesMap.get("rownum");
            myYesMap.put("rownum", o1.intValue());

            int paiming = (Integer) myYesMap.get("rownum");
            String bao;
            if (paiming >= 1 && paiming <= 50) {
                bao = "获得20元红包";
            } else if (paiming >= 51 && paiming <= 100) {
                bao = "获得15元红包";
            } else if (paiming >= 101 && paiming <= 200) {
                bao = "获得10元红包";
            } else {
                bao = "未获得红包";
            }
            modelAndView.addObject("paiming", paiming + "");
            modelAndView.addObject("bao", bao);
        }


        MyPagination m = baseRepository.getPageMap(pageNum, 20, allPaiMingSql);

        List<Map> mapList = (List<Map>) m.getList();
        if (null != mapList && mapList.size() > 0) {
            for (int i = 0; i < mapList.size(); i++) {
                Map map = mapList.get(i);
                Integer time1 = (Integer) map.get("time");
                float floatTime1 = (time1.floatValue()) / 1000;
                map.put("time", floatTime1);
                Object o = map.get("rownum");
                //服务器返回的rownum类型为 BigInteger，本地为Double类型
                if (o instanceof BigInteger) {
                    BigInteger rownum = (BigInteger) o;
                    map.put("rownum", rownum.intValue());
                } else {
                    Double rownum = (Double) o;
                    map.put("rownum", rownum.intValue());
                }
                int userId = (Integer) map.get("user_id");
                User u = (User) baseRepository.findBeanById(User.class, userId);
                if (null != u) {
                    map.put("name", u.getName());
                } else {
                    map.put("name", "无");
                }
            }
        }
        modelAndView.addObject("allScore", m);
        return modelAndView;
    }

    @RequestMapping("/pai/{pageNum}")
    @ResponseBody
    public JSONObject paiming(@PathVariable Integer pageNum) {
        JSONObject jsonObject = new JSONObject();
        int rownum = (pageNum - 1) * 20;
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String sql = "SELECT t.*, @rownum \\:= @rownum +1 AS rownum FROM (SELECT @rownum \\:= " + rownum + ") r, (SELECT a.* from score_cg a LEFT JOIN score_cg b ON a.user_id=b.user_id WHERE Date(a.date)='" + date + "' GROUP BY a.user_id   ORDER BY a.score desc,a.time asc) as t where @rownum<=199";
        MyPagination m = baseRepository.getPageMap(pageNum, 20, sql);
        List<Map> mapList = (List<Map>) m.getList();
        if (null != mapList && mapList.size() > 0) {
            for (int i = 0; i < mapList.size(); i++) {
                Map map = mapList.get(i);
                int time1 = (Integer) map.get("time");
                float floatTime1 = ((float) time1) / 1000;
                map.put("time", floatTime1);
                int userId = (Integer) map.get("user_id");
                Object o = map.get("rownum");
                //服务器返回的rownum类型为 BigInteger，本地为Double类型
                if (o instanceof Number) {
                    Number rownum1 = (Number) o;
                    map.put("rownum", rownum1.intValue());
                }
                User u = null;
                u = (User) baseRepository.findBeanById(User.class, userId);
                if (null != u) {
                    map.put("name", u.getName());
                } else {
                    map.put("name", "无");
                }

            }
        }
        jsonObject.put("data", m);
        return jsonObject;
    }

    /**
     * 活动规则
     *
     * @return
     */
    @RequestMapping(value = "/rule")
    public ModelAndView rule() {
        ModelAndView modelAndView = new ModelAndView("rule");
        return modelAndView;
    }

    /**
     * 修改用户
     *
     * @param user 用 户
     * @param yzm  验证码
     * @return
     */
    @Transactional
    @RequestMapping(value = "/updateInfo")
    @ResponseBody
    public JSONObject updateInfo(User user, String yzm) {
        JSONObject jsonObject = new JSONObject();
        String sessionYzm = (String) request.getSession().getAttribute(user.getTel());
        if (null != sessionYzm && sessionYzm.equals(yzm)) {
            try {
                String tel = user.getTel();
                User sqlUser = findUserByTel(tel);
                if (null != sqlUser) {
                    user.setOpenid(sqlUser.getOpenid());
                    user.setId(sqlUser.getId());
                }
                User u = baseRepository.merge(user);
                log.info("修改用户成功="+u.toString());
                jsonObject.put("code", 1);
                jsonObject.put("msg", "修改成功");
                jsonObject.put("user", u);
            } catch (Exception e) {
                e.printStackTrace();
                jsonObject.put("code", 0);
                jsonObject.put("msg", "修改失败");
            }
        } else {
            jsonObject.put("code", 0);
            jsonObject.put("msg", "验证码错误");
        }
        return jsonObject;
    }

    /**
     * 新建用户
     *
     * @param user
     * @param yzm
     * @return
     */
    @Transactional
    @RequestMapping(value = "/saveUser")
    @ResponseBody
    public Map saveUser(User user, String yzm) {
        //返回的user
        User returnUser;
        Map<String, Object> map = new HashMap();
        if (user != null) {
            String sessionYzm = (String) request.getSession().getAttribute(user.getTel());
            if (null != sessionYzm && sessionYzm.equals(yzm)) {
                try {
                    String tel = user.getTel();
                    User sqlUser = findUserByTel(tel);
                    if (null != sqlUser) {
                        returnUser = sqlUser;
                    }else {
                        returnUser = baseRepository.merge(user);
                    }
                    map.put("code", 1);
                    map.put("data", returnUser);

                } catch (Exception e) {
                    e.printStackTrace();
                    map.put("code", 0);
                    map.put("msg", "保存失败，服务器出错");
                }
            } else {
                map.put("code", 0);
                map.put("msg", "验证码错误");
            }
        } else {
            map.put("code", 0);
            map.put("msg", "用户不存在");
        }
        return map;

    }

    /**
     * 根据电话获取用户
     *
     * @param tel 手机号
     * @return user
     */
    public User findUserByTel(String tel) {
        User user = null;
        Map<String, Object> queryMap = new HashMap();
        queryMap.put("1", tel);
        List<User> users = (List<User>) baseRepository.findBeansBySql("select * from user_cg where tel=?1", queryMap, User.class);
        if (users != null && users.size() > 0) {
            user = users.get(0);
        }
        return user;
    }


    /**
     * 验证码保存在session中，不能回传前端
     *
     * @return
     */
    @RequestMapping(value = "/getYzm")
    public @ResponseBody
    Map<String, Object> getYzmWithSession(String tel) {
        Map<String, Object> map = new HashMap<>();
        int yzm = new Random().nextInt(9000) + 1000;
        String content = "您的验证码是：" + yzm + "。请不要把验证码泄露给其他人。";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建一个get请求
        HttpGet httpget = new HttpGet("http://106.ihuyi.cn/webservice/sms.php?method=Submit&account=C41430914&password=855c59995ec45864dd997c1d71d88ea5&mobile=" + tel + "&content=" + content + "");
        //蔬菜协会接口
        //        HttpGet httpget = new HttpGet("http://106.ihuyi.cn/webservice/sms.php?method=Submit&account=C37125906&password=61456fb649324c6215324605279600ab&mobile=" + tel + "&content=" + content + "");
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpget);
            //判断状态码
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                //获取返回body
                HttpEntity httpEntity = response.getEntity();
                // 转成string
                String result = EntityUtils.toString(httpEntity, "UTF-8");
                System.out.println("result:" + result);
                Document doc = DocumentHelper.parseText(result);
                Element root = doc.getRootElement();
                String retCode = root.elementText("code");
                String msg = root.elementText("msg");
                String smsid = root.elementText("smsid");

                if (msg.equals("提交成功")) {
                    // 保存在session
                    request.getSession().setAttribute(tel, yzm + "");
                    map.put("code", 1);
                } else {
                    map.put("msg", msg);
                    map.put("code", 0);
                }
            }
        } catch (Exception e) {
            map.put("msg", "提交失败");
            map.put("code", 0);
        }
        return map;
    }
}

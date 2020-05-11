package com.greentech.kmcg.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.greentech.kmcg.bean.Answer;
import com.greentech.kmcg.bean.Question;
import com.greentech.kmcg.bean.Score;
import com.greentech.kmcg.bean.User;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Slf4j
@Controller
public class IndexController {
    @Autowired
    private BaseRepository baseRepository;
    @Autowired
    private AddressController addressController;
    @Autowired
    HttpServletRequest request;

    /**
     * 登录页面
     *
     * @return
     */
    @RequestMapping(value = "/login")
    public String login() {
        return "login";
    }

    /**
     * 主页面
     *
     * @return
     */
    @RequestMapping(value = "/index")
    public ModelAndView index() {
        log.debug("index");
        ModelAndView modelAndView = new ModelAndView("index.html");

        return modelAndView;
    }



   /* public static void main(String[] args) {
        IndexController indexController = new IndexController();
        indexController.checkTime();
    }*/

    /**
     * 答题页面
     *
     * @param tel 手机号
     * @return 页面
     */
    @RequestMapping(value = "/question")
    public ModelAndView question(String tel) {
        ModelAndView modelAndView = new ModelAndView();

        if (StringUtils.isNotBlank(tel)) {
            User user = findUserByTel(tel);
            if (null != user) {
                modelAndView.setViewName("questioncg.html");
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
                    if (null == sqlUser) {
                        baseRepository.save(user);
                        returnUser = findUserByTel(tel);

                    } else {
                        returnUser = sqlUser;
                    }
                    map.put("code", 1);
                    map.put("data", returnUser);

                } catch (Exception e) {
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
        HttpGet httpget = new HttpGet("http://106.ihuyi.cn/webservice/sms.php?method=Submit&account=C37125906&password=61456fb649324c6215324605279600ab&mobile=" + tel + "&content=" + content + "");
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

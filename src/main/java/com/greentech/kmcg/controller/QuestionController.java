package com.greentech.kmcg.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.greentech.kmcg.bean.Answer;
import com.greentech.kmcg.bean.Question;
import com.greentech.kmcg.bean.User;
import com.greentech.kmcg.repository.BaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author: 周发展
 * @Date: 2020/6/2 13:35
 * @Description:
 */
@Controller
@Slf4j
public class QuestionController {
    @Autowired
    BaseRepository baseRepository;
    @Autowired
    HttpServletRequest request;

    /**
     * 答题页面
     *
     * @param tel 手机号
     * @return 页面
     */
    @RequestMapping(value = "/questionVerify")
    public ModelAndView questionVerify(String tel) {
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
                modelAndView.setViewName("questionVerify.html");
                List<Question> list = (List<Question>) baseRepository.findBeansBySql("select * from question ORDER BY  RAND() LIMIT 10", null, Question.class);

                StringBuilder stringBuilder = new StringBuilder();

                for (Question question : list) {
                    //保存正确答案
                    stringBuilder.append(question.getRight()).append(",");
                    //清除正确答案，防止前台看到
                    question.setRight("");
                    JSONObject jsonObject = JSON.parseObject(question.getAnswer());
                    List<Answer> answerList = new ArrayList<>();
                    for (String str : jsonObject.keySet()) {
                        Answer answer = new Answer();
                        answer.setAnswer(str + ":" + jsonObject.get(str));
                        answerList.add(answer);
                    }
                    question.setAnswers(answerList);
                }
                String answers = stringBuilder.toString();
                String answersNew = answers.substring(0, answers.length() - 1);
                log.info("rightAnswer=" + answersNew);
                //将正确答案保存在session,用来跟用户提交的答案对比
                HttpSession session = request.getSession();
                session.setAttribute("rightAnswers", answersNew);
                //答题开始的时候设置一个标志，当请求result接口的时候验证这个标志
                session.setAttribute("login", tel);
                modelAndView.addObject("data", list);
            } else {
                modelAndView.setViewName("login.html");
            }
        } else {
            modelAndView.setViewName("login.html");
        }
        return modelAndView;
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
}
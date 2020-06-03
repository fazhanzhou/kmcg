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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
    @Autowired
    IndexController indexController;

    /**
     * 答题页面
     *
     * @param tel 手机号
     * @return 页面
     */
    @RequestMapping(value = "/questionVerify")
    public ModelAndView questionVerify(String tel,String timestamp) {
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


    /**
     * @param allAnswers 答案
     * @param tel        电话号码
     * @param useTime    总用时
     * @return
     */
    @Transactional
    @RequestMapping("/resultVerify")
    public ModelAndView addScore(String tel, String allAnswers, Long useTime) {
        ModelAndView modelAndView = new ModelAndView();
        String userAgent = request.getHeader("user-agent").toLowerCase();
        HttpSession session = request.getSession();
        String rightAnswers = (String) session.getAttribute("rightAnswers");
        if (StringUtils.isBlank(allAnswers) || StringUtils.isBlank(rightAnswers)) {
            modelAndView.setViewName("error1.html");
            log.info("异常,请重新答题=" + tel);
            modelAndView.addObject("msg", "异常,请重新答题");
            return modelAndView;
        }
        log.info(allAnswers);
        String[] allAnswersArray = allAnswers.split(",");
        String[] rightAnswersArray = rightAnswers.split(",");

        if (rightAnswersArray.length != 10 || allAnswersArray.length != 10) {
            modelAndView.setViewName("error1.html");
            log.info("异常,请重新答题=" + tel + "---rightAnswersArray长度不对=" + rightAnswersArray.length);
            modelAndView.addObject("msg", "异常,请重新答题");
            return modelAndView;
        }
        int right = 0;
        for (int i = 0; i < 10; i++) {
            String aa = allAnswersArray[i];
            String bb = rightAnswersArray[i];
            if (aa.equals(bb)) {
                right++;
            }
        }

        log.info("right=" + right);

        Cookie[] cookies = request.getCookies();
        JSONObject jsonObject = new JSONObject();
        for (Cookie cookie : cookies) {
            jsonObject.put(cookie.getName(), cookie.getValue());
        }

        boolean isWeiXin = userAgent == null || userAgent.indexOf("micromessenger") == -1 ? false : true;
        /*if (!isWeiXin) {
            modelAndView.setViewName("error1.html");
            log.info("请使用微信打开=" + tel);
            modelAndView.addObject("msg", "请使用微信打开");
            return modelAndView;
        }*/
        if (!checkTime()) {
            modelAndView.setViewName("error1.html");
            modelAndView.addObject("msg", "活动还未开始<br/><br/>活动开始时间为<br/>6.1-6.10日每天1:00到23:00<br/>");
            return modelAndView;
        }
        //检查是否经过答题页面后到这里
        String verifyTel = (String) request.getSession().getAttribute("login");

        if (StringUtils.isBlank(verifyTel)) {
            modelAndView.setViewName("redirect:/home1");
            log.info("请您先答题=" + tel);
            return modelAndView;
        }
        if (right > 10 || useTime < 10000) {
            modelAndView.setViewName("error1.html");
            log.info("异常,请重新答题=" + tel + "----score=" + right + "---time=" + useTime);
            modelAndView.addObject("msg", "异常,请重新答题");
            return modelAndView;
        }

        if (StringUtils.isBlank(tel)) {
            modelAndView.setViewName("error1.html");
            log.info("缺少参数=" + tel);
            modelAndView.addObject("msg", "异常,请重新答题");
            return modelAndView;
        }
        User user = indexController.findUserByTel(tel);
        if (null == user) {
            modelAndView.setViewName("error1.html");
            log.info("用户不存在=" + tel);
            modelAndView.addObject("msg", "用户不存在");
            return modelAndView;
        }

        //判断是否是呈贡区的
        String jiedao = user.getJiedao();
        String address = user.getAddress();
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        modelAndView.addObject("score", right);
        modelAndView.addObject("time", decimalFormat.format(Float.valueOf(useTime) / 1000));
        modelAndView.setViewName("score");
        if (!inPlace(address, jiedao)) {
            log.info("不是呈贡区居民=" + tel);
            modelAndView.addObject("area", "本活动仅限呈贡区居民参与排名发红包");
        } else {
            //判断是不是最好的成绩，如果是更新成绩，不是不做任何操作

            //获取之前的成绩
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String scoreSql = "select * from score_cg where user_id=" + user.getId() + " and Date(date) ='" + date + "'";
            List<Score> scores = (List<Score>) baseRepository.findBeansBySql(scoreSql, null, Score.class);
            Score score1 = new Score();
            score1.setScore(right);
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
                if (right > oldScore) {
                    try {
                        score1.setId(scoreOld.getId());
                        baseRepository.merge(score1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (right == oldScore && useTime < oldTime) {
                    try {
                        score1.setId(scoreOld.getId());
                        baseRepository.merge(score1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        request.getSession().removeAttribute("login");
        return modelAndView;
    }

    public static void main(String[] args) {
        String[] a = {"A", "B", "C", "C", "C", "C", "C", "C", "C", "C"};
        String[] b = {"A", "B", "C", "D", "C", "C", "C", "C", "C", "C"};

        int right = 0;
        for (int i = 0; i < 10; i++) {
            String aa = a[i];
            String bb = b[i];
            if (aa.equals(bb)) {
                right++;
            }
        }

        log.info("right=" + right);
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
            boolean is = false;
            for (int i = 0; i < placeList.size(); i++) {
                if (jiedao.contains(placeList.get(i))) {
                    is = true;
                    break;
                }
            }
            return is;
        } else {
            return false;
        }


    }
}
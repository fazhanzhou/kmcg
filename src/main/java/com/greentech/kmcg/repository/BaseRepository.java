package com.greentech.kmcg.repository;



import com.greentech.kmcg.bean.MyPagination;

import java.util.List;
import java.util.Map;

public interface BaseRepository {
    List<?> findBeansBySql(String sql, Map<String, Object> params, Class clazz);

    /**
     * 保存数据
     * @param c
     */
    void save(Object c);
    /**
     * 更新数据:有id更新数据，没有id插入数据
     *
     * @param o 修改的类
     * @return 新的类
     */
    <T> T merge(T o);
    /**
     * 执行sql
     * @param sql
     * @param params
     * @param clazz
     * @return
     */
    int nativeSql(String sql, Map<String, Object> params, Class clazz);
    void nativeSql(String sql, Map<String, Object> params);
    int countBeansBySql(String sql, Map<String, Object> params);

    Object findBeanById(Class c, Integer id);

    MyPagination findPageBeansBySql(Integer curPage, Integer pageSize, String sql, Map<String, Object> params, Class clazz);

    /**
     * 返回map类型的集合
     *
     * @param sql 原生sql
     * @return
     */
    List<Map> getMap(String sql);


    MyPagination getPageMap(Integer curPage, Integer pageSize, String sql);
}

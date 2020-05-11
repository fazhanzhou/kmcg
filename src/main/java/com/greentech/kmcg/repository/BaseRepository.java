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
     * 执行sql
     * @param sql
     * @param params
     * @param clazz
     * @return
     */
    int nativeSql(String sql, Map<String, Object> params, Class clazz);
    int countBeansBySql(String sql, Map<String, Object> params);

    Object findBeanById(Class c, Integer id);

    MyPagination findPageBeansBySql(Integer curPage, Integer pageSize, String sql, Map<String, Object> params, Class clazz);
}

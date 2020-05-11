package com.greentech.kmcg.repository;

import com.greentech.kmcg.bean.MyPagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

@Repository
public class BaseRepositoryImpl implements BaseRepository {
    @Autowired
    private EntityManager entityManager;

    @Override
    public List<?> findBeansBySql(String sql, Map<String, Object> params, Class clazz) {
        Query query = entityManager.createNativeQuery(sql, clazz);
        setParameters(query, params);
        List<?> list = query.getResultList();
        return list;
    }

    @Override
    public void save(Object c) {
        entityManager.persist(c);
    }

    @Override
    public int nativeSql(String sql, Map<String, Object> params, Class clazz) {
        Query query = entityManager.createNativeQuery(sql, clazz);
        setParameters(query, params);
        return  query.executeUpdate();
    }

    @Override
    public int countBeansBySql(String sql, Map<String, Object> params) {
        Query query = entityManager.createNativeQuery(sql);
        setParameters(query, params);
        List<?> list = query.getResultList();
        int num = ((Number) list.get(0)).intValue();
        return num;
    }

    @Override
    public Object findBeanById(Class c, Integer id) {
        return entityManager.find(c, id);
    }

    @Override
    public MyPagination findPageBeansBySql(Integer curPage, Integer pageSize, String sql, Map<String, Object> params, Class clazz) {
        // 查询结果总条数
        Query query = entityManager.createNativeQuery(sql, clazz);
        setParameters(query, params);
        long total = query.getResultList().size();
        MyPagination pagination = MyPagination.create(curPage, pageSize);
        long totalPage = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
        pagination.setTotalNum(total);
        pagination.setTotalPage(totalPage);
        if (total == 0) {
            return pagination;
        }
        query.setFirstResult(pagination.getCurPage()).setMaxResults(pagination.getPageSize());
        // 列表数据
        pagination.setList(query.getResultList());
        return pagination;
    }


    /**
     * 占位符参数处理
     *
     * @param query
     * @param params
     */
    private void setParameters(Query query, Map<String, Object> params) {
        if (null != params && !params.isEmpty()) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                int index = Integer.parseInt(entry.getKey());
                query.setParameter(index, entry.getValue());
            }
        }
    }
}

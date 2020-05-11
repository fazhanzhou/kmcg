package com.greentech.kmcg.bean;

import java.util.ArrayList;
import java.util.List;

public class MyPagination {
    /**
     * 当前页数
     **/
    private int curPage;
    /**
     * 总页数
     **/
    private long totalPage;
    /**
     * 每页条数
     **/
    private int pageSize;
    /**
     * 总条数
     **/
    private long totalNum;
    /**
     * 列表数据
     **/
    private List<?> list = new ArrayList<>();

    public MyPagination() {
    }

    public MyPagination(int curPage, int pageSize) {
        if (curPage >= 1 && pageSize >= 1) {
            this.curPage = curPage;
            this.pageSize = pageSize;
        } else {
            throw new RuntimeException("invalid curPage: " + curPage + " or pageSize: " + pageSize);
        }
    }

    public static MyPagination create(int curPage, int pageSize) {
        return new MyPagination(curPage, pageSize);
    }

    public int getCurPage() {
        return curPage;
    }

    public void setCurPage(int curPage) {
        this.curPage = curPage;
    }

    public long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(long totalPage) {
        this.totalPage = totalPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(long totalNum) {
        this.totalNum = totalNum;
    }

    public List<?> getList() {
        return list;
    }

    public void setList(List<?> list) {
        this.list = list;
    }
}

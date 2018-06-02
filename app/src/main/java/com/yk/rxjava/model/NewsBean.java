package com.yk.rxjava.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Silence on 2018/5/31.
 * 单次返回结果的数据结构
 */

public class NewsBean {

    private boolean error;
    private List<NewsResultBean> results = new ArrayList<>();

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public List<NewsResultBean> getResults() {
        return results;
    }

    public void setResults(List<NewsResultBean> results) {
        this.results = results;
    }


}

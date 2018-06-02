package com.yk.rxjava.net;


import com.yk.rxjava.model.LoginBean;
import com.yk.rxjava.model.LoginRequest;
import com.yk.rxjava.model.NewsBean;
import com.yk.rxjava.model.RegisterRequest;
import com.yk.rxjava.model.RegisterResponse;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface Api {

    @GET
    Observable<LoginBean> login(@Body LoginRequest request);

    @GET
    Observable<RegisterResponse> register(@Body RegisterRequest registerRequest);

    @GET("api/data/{category}/{count}/{page}")
    Observable<NewsBean> getNews(@Path("category") String category, @Path("count") int count,
                                 @Path("page") int page);
}

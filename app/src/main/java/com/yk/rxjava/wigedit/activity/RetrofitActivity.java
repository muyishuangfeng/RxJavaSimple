package com.yk.rxjava.wigedit.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.yk.rxjava.R;
import com.yk.rxjava.model.NewsBean;
import com.yk.rxjava.model.NewsResultBean;
import com.yk.rxjava.net.Api;
import com.yk.rxjava.net.RetrofitClient;
import com.yk.rxjava.wigedit.adapter.RetrofitAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * zip操作符
 *
 * 同步的时候它接收多个Observable，以及一个函数，该函数的形参为这些Observable发送的数据，
 * 并且要等所有的Observable都发射完会后才会回调该函数。
 * 异步的时候应该是多个Observable各自发送第一次后先组合起来发送给下游处理一次，然后再发第二次、
 * 第三次...一直到发送量最小的那个Observable发送完它的最后一个事件，下游便不再接受数据。
 */
public class RetrofitActivity extends AppCompatActivity {

    private int currentPage = 1;
    private List<NewsResultBean> mList = new ArrayList<>();
    private CompositeDisposable mDisposable=new CompositeDisposable();
    private RetrofitAdapter mAdapter;
    Button mBtnRefresh;
    RecyclerView mRlvRetrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrofit);
        initView();
    }

    private void initView() {
        mBtnRefresh = findViewById(R.id.btn_refresh);
        mBtnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // refresh(++currentPage);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(RetrofitActivity.this,
                                TimerActivity.class));
                    }
                },300);

            }
        });
        mRlvRetrofit = findViewById(R.id.rlv_retrofit);
        LinearLayoutManager mManager = new LinearLayoutManager(this);
        mRlvRetrofit.setLayoutManager(mManager);
        mAdapter = new RetrofitAdapter(this, mList);
        mRlvRetrofit.setAdapter(mAdapter);
        refresh(++currentPage);

    }

    private void refresh(final int page) {
        Observable<List<NewsResultBean>> observable = Observable.just(page)
                .subscribeOn(Schedulers.io())
                .flatMap(new Function<Integer, ObservableSource<List<NewsResultBean>>>() {
                    @Override
                    public ObservableSource<List<NewsResultBean>> apply(Integer integer) throws Exception {
                        Observable<NewsBean> androidNews = getObservable("Android", page);
                        Observable<NewsBean> iosNews = getObservable("iOS", page);
                        return Observable.zip(androidNews, iosNews, new BiFunction<NewsBean, NewsBean, List<NewsResultBean>>() {
                            @Override
                            public List<NewsResultBean> apply(NewsBean androidBean, NewsBean iOSBean) throws Exception {
                                List<NewsResultBean> result = new ArrayList<>();
                                result.addAll(androidBean.getResults());
                                result.addAll(iOSBean.getResults());
                                return result;
                            }
                        });
                    }
                });
        DisposableObserver<List<NewsResultBean>> observer = new DisposableObserver<List<NewsResultBean>>() {
            @Override
            public void onNext(List<NewsResultBean> newsResultBeans) {
                mList.clear();
                mList.addAll(newsResultBeans);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
        mDisposable.add(observer);
    }

    private Observable<NewsBean> getObservable(String category, int page) {
        Api api = new Retrofit.Builder()
                .baseUrl("http://gank.io")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(Api.class);
        return api.getNews(category, 10, page);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }
}

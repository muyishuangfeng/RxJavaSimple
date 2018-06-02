package com.yk.rxjava.wigedit.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.yk.rxjava.R;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FlowableActivity extends AppCompatActivity {

    public static final String TAG = "FlowableActivity";
    Subscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flowable);
        initView();
        //initData();
    }

    private void initView() {
        //创建连接
        //upStream.subscribe(subscriber);
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> emitter) {
                    Log.e(TAG, "emitter:" + emitter.requested());
                    emitter.onNext(1);
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.e(TAG, "onSubscribe");
                        mSubscription = s;
                        s.request(1000);//一开始就处理掉128个事件
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.e(TAG, "onNext: " + integer);
                        startActivity(new Intent(FlowableActivity.this,
                                BackgroundActivity.class));
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, "onError: ", t);
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete");
                    }
                });
    }

    Flowable<Integer> upStream = Flowable.create(new FlowableOnSubscribe<Integer>() {
        @Override
        public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
            Log.e(TAG, "emit 1");
            emitter.onNext(1);
            Log.e(TAG, "emit 2");
            emitter.onNext(2);
            Log.e(TAG, "emit 3");
            emitter.onNext(3);
            Log.e(TAG, "emit complete");
            emitter.onComplete();
        }
    }, BackpressureStrategy.ERROR);

    Subscriber<Integer> subscriber = new Subscriber<Integer>() {
        @Override
        public void onSubscribe(Subscription s) {
            Log.e(TAG, "onSubscribe");
            s.request(Long.MAX_VALUE);  //注意这句代码
        }

        @Override
        public void onNext(Integer s) {
            Log.e(TAG, "onNext: " + s);
        }

        @Override
        public void onError(Throwable t) {
            Log.e(TAG, "onError: ", t);
        }

        @Override
        public void onComplete() {
            Log.e(TAG, "onComplete");
        }
    };

    private void request(long n) {
        mSubscription.request(n);
    }

    private void initData(){
        Flowable.interval(1, TimeUnit.MICROSECONDS)
                .onBackpressureDrop()//加上背压策略
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.e(TAG, "onSubscribe");
                        mSubscription = s;
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        Log.e(TAG, "onNext: " + aLong);
                        try {
                            Thread.sleep(1000);  //延时1秒
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, "onError: ", t);
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete");
                    }
                });
    }
}

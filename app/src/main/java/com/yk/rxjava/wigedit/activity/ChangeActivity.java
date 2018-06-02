package com.yk.rxjava.wigedit.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.yk.rxjava.R;
import com.yk.rxjava.model.LoginBean;
import com.yk.rxjava.model.LoginRequest;
import com.yk.rxjava.model.RegisterRequest;
import com.yk.rxjava.model.RegisterResponse;
import com.yk.rxjava.net.Api;
import com.yk.rxjava.net.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public class ChangeActivity extends AppCompatActivity {

    public static final String TAG = "ChangeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change);
        initView();
        //initData();
        //initZip();
        //initHandler();
        //init();

    }

    private void initView() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            }
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Exception {
                return "This is result " + integer;
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {
                Log.e(TAG, s);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                startActivity(new Intent(ChangeActivity.this,
                        FlowableActivity.class));
            }
        });

    }

    private void initData() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
            }
        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                List<String> mList = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    mList.add("I'm value:" + integer);
                }
                return Observable.fromIterable(mList).delay(10, TimeUnit.MILLISECONDS);
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {
                Log.e(TAG, s);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void initNet() {
        final LoginRequest request = new LoginRequest();
        RegisterRequest mRequest = new RegisterRequest();
        final Api api = RetrofitClient.getRetrofit().create(Api.class);
        api.register(mRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<RegisterResponse>() {
                    @Override
                    public void accept(RegisterResponse registerResponse) throws Exception {
                        //先根据注册的响应结果去做一些操作
                    }
                }).observeOn(Schedulers.io())
                .flatMap(new Function<RegisterResponse, ObservableSource<LoginBean>>() {
                    @Override
                    public ObservableSource<LoginBean> apply(RegisterResponse registerResponse)
                            throws Exception {
                        return api.login(request);
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LoginBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(LoginBean loginBean) {
                        Toast.makeText(ChangeActivity.this, "登录成功",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(ChangeActivity.this, "登录失败",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    /**
     * zip组合操作符
     */
    private void initZip() {
        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer + s;
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e(TAG, "onSubscribe");
            }

            @Override
            public void onNext(String s) {
                Log.e(TAG, "onNext: " + s);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError");
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete ");
            }
        });

    }

    Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
        @Override
        public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onNext(4);
            emitter.onComplete();
        }
    }).subscribeOn(Schedulers.io());

    Observable<String> observable2 = Observable.create(new ObservableOnSubscribe<String>() {
        @Override
        public void subscribe(ObservableEmitter<String> emitter) throws Exception {
            emitter.onNext("A");
            emitter.onNext("B");
            emitter.onNext("C");
            emitter.onComplete();
        }
    }).subscribeOn(Schedulers.io());


    private void init() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                for (int i = 0; ; i++) {
                    emitter.onNext(i);
                    Thread.sleep(2000);  //每次发送完事件延时2秒
                }
            }
        }).subscribeOn(Schedulers.io())
                //.sample(2,TimeUnit.SECONDS)//这里用了一个sample操作符, 简单做个介绍,
                // 这个操作符每隔指定的时间就从上游中取出一个事件发送给下游.
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.e(TAG, "" + integer);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void initHandler() {
//        CustomThread customThread= new CustomThread(mHandler);
//        customThread.start();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                mHandler.sendEmptyMessage(0);
//            }
//        }).start();

        new Handler().postAtTime(new Runnable() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(0);
            }
        }, 1000);
    }

    Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0: {
                    Toast.makeText(ChangeActivity.this, "菜翼天下无敌了",
                            Toast.LENGTH_LONG).show();
                    break;
                }
            }
        }
    };
}

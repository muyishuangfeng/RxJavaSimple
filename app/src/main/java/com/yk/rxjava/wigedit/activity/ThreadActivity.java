package com.yk.rxjava.wigedit.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.yk.rxjava.R;
import com.yk.rxjava.model.LoginBean;
import com.yk.rxjava.model.LoginRequest;
import com.yk.rxjava.net.Api;
import com.yk.rxjava.net.RetrofitClient;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class ThreadActivity extends AppCompatActivity {

    public static final String TAG = "ThreadActivity";
    CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);
        initView();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void initView() {
        //建立连接
        // observable.subscribe(consumer);
//        observable.subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(consumer);

        observable.subscribeOn(Schedulers.newThread()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e(TAG, "After observeOn(mainThread), current thread is: " + Thread.currentThread().getName());
                    }
                }).observeOn(Schedulers.io())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e(TAG, "After observeOn(io), current thread is : " + Thread.currentThread().getName());
                        startActivity(new Intent(ThreadActivity.this, ChangeActivity.class));
                    }
                }).subscribe(consumer);
    }

    Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
        @Override
        public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
            Log.e(TAG, "Observable thread is : " + Thread.currentThread().getName());
            Log.e(TAG, "emit 1");
            emitter.onNext(1);
        }
    });

    Consumer<Integer> consumer = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Exception {
            Log.e(TAG, "Observer thread is :" + Thread.currentThread().getName());
            Log.e(TAG, "onNext: " + integer);
        }
    };

    private void initNet() {
        LoginRequest request = new LoginRequest();
        Api api = RetrofitClient.getRetrofit().create(Api.class);
        api.login(request)
                .subscribeOn(Schedulers.io())//在IO线程进行网络请求
                .observeOn(AndroidSchedulers.mainThread())  //回到主线程去处理请求结果
                .subscribe(new Observer<LoginBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposable.add(d);
                    }

                    @Override
                    public void onNext(LoginBean loginBean) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(ThreadActivity.this, "登录失败",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(ThreadActivity.this, "登录失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }
}

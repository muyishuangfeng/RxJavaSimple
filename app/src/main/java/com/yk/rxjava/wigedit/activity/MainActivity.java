package com.yk.rxjava.wigedit.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.yk.rxjava.R;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         initView();
        //initData();
    }

    /**
     *
     */
    private void initView() {
        //建立连接
        //observable.subscribe(observer);
        /**
         * 建立连接（链式调用）
         */
        Observable.create(new ObservableOnSubscribe<Integer>() {

            /**
             * ObservableEmitter 发射器
             * @param emitter
             * @throws Exception
             */
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onNext(4);
                emitter.onComplete();
            }
        }).subscribe(new Observer<Integer>() {
//            private Disposable mDisposable;
//            private int index;

            @Override
            public void onSubscribe(Disposable d) {
                Log.e(TAG, "subscribe");
               // mDisposable = d;
            }

            @Override
            public void onNext(Integer integer) {
                Log.e(TAG, "value:" + integer);
                //index++;
//                if (index == 4) {
//                    Log.e(TAG, "dispose");
//                    mDisposable.dispose();
//                    Log.e(TAG, "isDisposed : " + mDisposable.isDisposed());
//                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "error" + e.toString());
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "complete");
                startActivity(new Intent(MainActivity.this,ThreadActivity.class));
            }
        });


    }

    /**
     * 被观察者
     */
    Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
        @Override
        public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onNext(4);
            emitter.onComplete();
        }
    });


    /**
     * 观察者
     */
    Observer<Integer> observer = new Observer<Integer>() {
        @Override
        public void onSubscribe(Disposable d) {
            Log.e(TAG, "subscribe");
        }

        @Override
        public void onNext(Integer integer) {
            Log.e(TAG, "value:" + integer);
        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, "error");
        }

        @Override
        public void onComplete() {
            Log.e(TAG, "complete");
        }
    };

    /**
     * 初始化数据
     */
    private void initData(){
        Observable.create(new ObservableOnSubscribe<Integer>() {

            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.e(TAG, "onNext: " + integer);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Log.e(TAG, "error" );
            }
        });

    }
}

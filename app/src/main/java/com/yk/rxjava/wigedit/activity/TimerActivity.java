package com.yk.rxjava.wigedit.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yk.rxjava.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * 轮询操作
 */
public class TimerActivity extends AppCompatActivity {

    Button mBtnRepeat, mBtnInterval,mBtnRetry;
    CompositeDisposable mDisposable;
    private static final String TAG = "TimerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        initView();
    }

    private void initView() {
        mBtnInterval = findViewById(R.id.btn_interval);
        mBtnInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startInterval();
            }
        });
        mBtnRepeat = findViewById(R.id.btn_repeat);
        mBtnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRepeat();
            }
        });
        mDisposable = new CompositeDisposable();
        mBtnRetry=findViewById(R.id.btn_retry_next);
        mBtnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TimerActivity.this,
                        RepeatActivity.class));
            }
        });
    }

    /**
     * 固定时延轮询
     */
    private void startInterval() {
        Log.e(TAG, "startInterval");
        Observable<Long> observable = Observable
                /**
                 * 固定时延轮询
                 *
                 * 在轮询操作中一般会进行一些耗时的网络请求，因此我们选择在doOnNext进行处理，
                 * 它会在下游的onNext方法被回调之前调用，但是它的运行线程可以通过subscribeOn指定，
                 * 下游的运行线程再通过observerOn切换会主线程，通过打印对应的线程ID可以验证结果，
                 * 当要求的数据项都发送完毕之后，最后会回调onComplete方法。
                 *
                 * @params start 发送数据的起始值，为Long型
                 * @params count 数据总数，总共发送了多少数据
                 * @params initialDelay 发送第一个数据项时的起始时延,也就是说，第一个数据发送之后
                 *                       延时多长时间
                 * @params period 两项数据之间的间隔时间，这里为3000（毫秒）
                 * @params TimeUnit 时间单位,这里为毫秒，是上一个值的单位
                 */
                .intervalRange(0, 5, 0, 3000, TimeUnit.MILLISECONDS)
                .take(5)
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        doWork();//这里使用了doOnNext，因此DisposableObserver的onNext要等到该方法执行完才会回调。
                    }
                });
        DisposableObserver<Long> disposableObserver = getDisposableObserver();
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(disposableObserver);
        mDisposable.add(disposableObserver);
    }

    private void startRepeat() {
        Log.e(TAG, "startRepeat");
        Observable<Long> observable = Observable
                .just(0L)
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        doWork();
                    }
                })
                /**
                 * 之所以可以通过repeatWhen来实现轮询，是因为它为我们提供了重订阅的功能，
                 * 而重订阅有两点要素：
                 * 上游告诉我们一次订阅已经完成，这就需要上游回调onComplete函数。
                 * 我们告诉上游是否需要重订阅，通过repeatWhen的Function函数所返回的Observable确定，
                 * 如果该Observable发送了onComplete或者onError则表示不需要重订阅，结束整个流程；
                 * 否则触发重订阅的操作。
                 */
                .repeatWhen(
                        /**
                         * Function的输入是一个Observable<Object>，输出是一个泛型ObservableSource<?>。
                         *如果输出的Observable发送了onComplete或者onError则表示不需要重订阅，结束整个流程；
                         * 否则触发重订阅的操作。也就是说，它 仅仅是作为一个是否要触发重订阅的通知，
                         * onNext发送的是什么数据并不重要。
                         *对于每一次订阅的数据流 Function 函数只会回调一次，并且是在onComplete的时候触发，
                         * 它不会收到任何的onNext事件。
                         * 在Function函数中，必须对输入的 Observable<Object>进行处理
                         */
                        new Function<Observable<Object>, ObservableSource<Long>>() {
                            private long mRepeatCount;

                            @Override
                            public ObservableSource<Long> apply(Observable<Object> objectObservable) throws Exception {
                                /**
                                 * flatMap
                                 *  必须作出反应，这里是通过flatMap操作符。
                                 * 对于上游发送的每个事件它都会应用该函数，
                                 * 这个函数返回一个新的Observable，如果有多个Observable，
                                 * 那么他会发送合并后的结果
                                 */

                                return objectObservable.flatMap(new Function<Object, ObservableSource<Long>>() {
                                    @Override
                                    public ObservableSource<Long> apply(Object o) throws Exception {
                                        if (++mRepeatCount > 4) {
                                            // 发送onComplete消息,无法触发下游的onComplete回调。
                                            //return Observable.empty();
                                            //发送onError消息，可以触发下游的onError回调。
                                            /**
                                             * 返回Observable.empty()，发送onComplete消息，
                                             * 但是DisposableObserver并不会回调onComplete。
                                             * 返回Observable.error(new Throwable("repeat work finished"))，
                                             * DisposableObserver的onError会被回调，并接受传过去的错误信息。
                                             */
                                            return Observable.error(new Throwable("repeat work finished"));
                                        }
                                        Log.e(TAG, "startRepeat apply");
                                        return Observable
                                                /**
                                                 * time操作符，在订阅完成后，等待指定的时间才会发送消息
                                                 */
                                                .timer(3000 + mRepeatCount * 1000, TimeUnit.MILLISECONDS);
                                    }
                                });
                            }
                        });
        DisposableObserver<Long> disposableObserver = getDisposableObserver();
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(disposableObserver);
        mDisposable.add(disposableObserver);
    }

    private DisposableObserver<Long> getDisposableObserver() {
        return new DisposableObserver<Long>() {
            @Override
            public void onNext(Long aLong) {

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "DisposableObserver onError, threadId="
                        + Thread.currentThread().getId() + ",reason=" + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "DisposableObserver onComplete, threadId="
                        + Thread.currentThread().getId());
            }
        };
    }

    private void doWork() {
        long workTime = (long) (Math.random() * 500) + 500;
        try {
            Log.e(TAG, "doWork start,  threadId=" + Thread.currentThread().getId());
            Thread.sleep(workTime);
            Log.e(TAG, "doWork finished");
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }
}

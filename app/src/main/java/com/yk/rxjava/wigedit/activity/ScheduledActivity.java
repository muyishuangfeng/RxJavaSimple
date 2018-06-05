package com.yk.rxjava.wigedit.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.yk.rxjava.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * 调度
 */
public class ScheduledActivity extends AppCompatActivity {

    Button mBtnTimer, mBtnDelay, mBtnInterval, mBtnNext, mBtnIntervalSubmit,
            mBtnIntervalFive;
    private static final String TAG = ScheduledActivity.class.getSimpleName();
    private CompositeDisposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduled);
        initView();
    }

    private void initView() {
        mDisposable = new CompositeDisposable();
        mBtnTimer = findViewById(R.id.btn_timer);
        mBtnTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
            }
        });
        mBtnInterval = findViewById(R.id.btn_interval);
        mBtnInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startInterval();
            }
        });
        mBtnDelay = findViewById(R.id.btn_delay);
        mBtnDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDelay();
            }
        });
        mBtnIntervalSubmit = findViewById(R.id.btn_interval_submit);
        mBtnIntervalSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startIntervalSubmit();
            }
        });
        mBtnNext = findViewById(R.id.btn_next);
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScheduledActivity.this,
                        RotatingActivity.class));
            }
        });
        mBtnIntervalFive = findViewById(R.id.btn_interval_five);
        mBtnIntervalFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startIntervalFive();
            }
        });
    }

    /**
     * Timer 创建型操作符，用于延时执行任务
     * <p>
     * 在订阅之后，它会在等待一段时间之后发射一个0数据项，然后结束，
     * 因此它常常可以用来延时地发送时间
     */
    private void startTimer() {
        Log.e(TAG, "startTimer");
        DisposableObserver<Long> disposableObserver = getTimerObserver();
        Observable
                /*
                延时1秒发送
                 */
                .timer(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(disposableObserver);
        mDisposable.add(disposableObserver);
    }

    /**
     * 观察者
     *
     * @return
     */
    private DisposableObserver<Long> getTimerObserver() {
        return new DisposableObserver<Long>() {
            @Override
            public void onNext(Long aLong) {
                Log.e(TAG, "DisposableObserver onNext=" + aLong
                        + "threadId="
                        + Thread.currentThread().getId());
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "DisposableObserver onError, threadId="
                        + Thread.currentThread().getId() + ",reason=" + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "DisposableObserver,onComplete");
            }
        };
    }

    /**
     * 每隔一秒执行一次任务，第一次任务有1s的时间间隔，执行无限次
     * <p>
     * interval 创建型操作符，间隔一段时间发送一个数据
     * <p>
     * 每隔1s执行一次任务，第一次任务执行前有1s的间隔，执行无限次。这是因为，
     * 使用interval操作符时，默认第一个任务需要延时和指定间隔相同的时间
     */
    private void startInterval() {
        Log.e(TAG, "startInterval");
        DisposableObserver<Long> disposableObserver = getTimerObserver();
        Observable.interval(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(disposableObserver);
        mDisposable.add(disposableObserver);
    }

    /**
     * 每隔一秒执行一次任务，第一次任务没有时间间隔，执行无限次
     * <p>
     * interval 创建型操作符，间隔一段时间发送一个数据
     * <p>
     */
    private void startIntervalSubmit() {
        Log.e(TAG, "startInterval");
        DisposableObserver<Long> disposableObserver = getTimerObserver();
        Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(disposableObserver);
        mDisposable.add(disposableObserver);
    }

    /**
     * 每隔 1s 执行一次任务，立即执行第一次任务，只执行五次
     */
    private void startIntervalFive() {
        Log.e(TAG, "startInterval");
        DisposableObserver<Long> disposableObserver = getTimerObserver();
        Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                /*
                 * take操作符 执行的次数
                 *
                 * 表示我们只接受前n个数据项，这样和interval结合就可以实现固定间隔与固定次数的任务执行
                 */
                .take(5)
                .subscribeOn(Schedulers.io())
                .subscribe(disposableObserver);
        mDisposable.add(disposableObserver);
    }

    /**
     * 先执行一个任务，等待 1s，再执行另一个任务，然后结束
     * <p>
     * delay 辅助性操作符
     * <p>
     * 当它接受一个时间段时，每当原始的Observable发射了一个数据项时，
     * 它就启动一个定时器，等待指定的时间后再将这个数据发射出去，
     * 因此表现为发射的数据项进行了平移，但是它只会平移onNext/onComplete，
     * 对于onError，它会立即发射出去，并且丢弃之前等待发射的onNext事件
     */
    private void startDelay() {
        Log.e(TAG, "startDelay");
        DisposableObserver<Long> disposableObserver = getTimerObserver();
        Observable.just(0L).doOnNext(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                Log.e(TAG, "执行第一个任务");
            }
        }).delay(1000, TimeUnit.MILLISECONDS)
                .subscribe(disposableObserver);
        mDisposable.add(disposableObserver);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }
}

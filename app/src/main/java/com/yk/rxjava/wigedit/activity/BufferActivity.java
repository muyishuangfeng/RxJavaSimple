package com.yk.rxjava.wigedit.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yk.rxjava.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.PublishSubject;

public class BufferActivity extends AppCompatActivity {

    PublishSubject<Double> mPublishSubject;
    CompositeDisposable mDisposable;
    TextView mTxtContent;
    SourceHandler mHandler;
    Button mBtnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buffer);
        initView();
    }

    private void initView() {
        mTxtContent = findViewById(R.id.txt_content);
        mBtnNext = findViewById(R.id.btn_next);
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BufferActivity.this,
                        SearchActivity.class));
            }
        });

        mPublishSubject = PublishSubject.create();
        DisposableObserver<List<Double>> disposableObserver = new DisposableObserver<List<Double>>() {
            @Override
            public void onNext(List<Double> doubles) {
                double result = 0;
                if (doubles.size() > 0) {
                    for (Double d : doubles) {
                        result += d;
                    }
                    result = result / doubles.size();
                }
                Log.e("BufferActivity", "更新平均温度：" + result);
                mTxtContent.setText("过去3秒收到了" + doubles.size() + "个数据， 平均温度为："
                        + result);

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        mPublishSubject
                /**
                 * buffer操作符 是将上游所产生的消息先放到缓冲区中去，直到事件到了之后，再讲所有在这段
                 * 缓冲事件中放入到缓冲事件中的值，放入到list集合中，一起发送给下游。
                 *
                 * 这里的就是讲三秒钟所产生的数据，放入到list集合中去，然后三秒钟到了之后再统一传递给
                 * 下游中去
                 */
                .buffer(3000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(disposableObserver);
        mDisposable = new CompositeDisposable();
        mDisposable.add(disposableObserver);
        mHandler = new SourceHandler();
        mHandler.sendEmptyMessage(0);
    }


    private class SourceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            double temperature = Math.random() * 25 + 5;
            updateTemperature(temperature);
            //循环地发送。
            sendEmptyMessageDelayed(0,
                    250 + (long) (250 * Math.random()));

        }
    }

    private void updateTemperature(double temperature) {
        Log.e("BufferActivity", "温度测量结果：" + temperature);
        mPublishSubject.onNext(temperature);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mDisposable.clear();
    }
}

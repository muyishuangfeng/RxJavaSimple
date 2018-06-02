package com.yk.rxjava.wigedit.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yk.rxjava.R;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * 后台执行耗时操作
 */
public class BackgroundActivity extends AppCompatActivity {

    TextView mTxtProgress;
    Button mBtnDownLoad, mBtnStop;
    CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background);
        initView();
    }

    private void initView() {
        mTxtProgress = findViewById(R.id.txt_progress);
        mBtnDownLoad = findViewById(R.id.btn_download);
        mBtnStop = findViewById(R.id.btn_stop);
        mBtnDownLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload();
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BackgroundActivity.this,
                        BufferActivity.class));
            }
        });
    }

    private void startDownload() {
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(disposableObserver);
        mDisposable.add(disposableObserver);
    }

    Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
        @Override
        public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
            for (int i = 0; i < 100; i++) {
                if (i % 20 == 0) {
                    Thread.sleep(500);
                }
                emitter.onNext(i);
            }
            emitter.onComplete();
        }
    });

    DisposableObserver<Integer> disposableObserver = new DisposableObserver<Integer>() {
        @Override
        public void onNext(Integer integer) {
            Log.e("BackgroundActivity", "onNext=" + integer);
            mTxtProgress.setText("Current Progress=" + integer);
        }

        @Override
        public void onError(Throwable e) {
            Log.e("BackgroundActivity", "onError=" + e);
            mTxtProgress.setText("Download error");
        }

        @Override
        public void onComplete() {
            Log.e("BackgroundActivity", "onComplete");
            mTxtProgress.setText("onComplete");
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        mDisposable.clear();
    }


}

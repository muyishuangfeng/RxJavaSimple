package com.yk.rxjava.wigedit.activity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yk.rxjava.R;
import com.yk.rxjava.impl.IHolder;
import com.yk.rxjava.wigedit.fragment.WorkerFragment;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.observers.DisposableObserver;

/**
 * 屏幕旋转
 */
public class RotatingActivity extends AppCompatActivity implements IHolder {

    private static final String TAG = RotatingActivity.class.getName();
    Button mBtnStart,mBtnNet;
    TextView mTxtContent;
    CompositeDisposable mCompositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_rotating);
        initView();
    }

    private void initView() {
        mTxtContent = findViewById(R.id.tv_worker_result);
        mBtnStart = findViewById(R.id.btn_start_worker);
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWorker();
            }
        });
        mBtnNet = findViewById(R.id.btn_net);
        mBtnNet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(RotatingActivity.this,
                       NetActivity.class));
            }
        });
        mCompositeDisposable = new CompositeDisposable();
    }




    private void startWorker() {
        WorkerFragment worker = getWorkerFragment();
        if (worker == null) {
            addWorkerFragment();
        } else {
            Log.d(TAG, "WorkerFragment has attach");
        }
    }

    private void onWorkerFinished() {
        Log.d(TAG, "onWorkerFinished");
        removeWorkerFragment();
    }

    private void addWorkerFragment() {
        WorkerFragment workerFragment = new WorkerFragment();
        Bundle bundle = new Bundle();
        bundle.putString("task_name", "学习RxJava2");
        workerFragment.setArguments(bundle);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(workerFragment, WorkerFragment.TAG);
        transaction.commit();
    }

    private void removeWorkerFragment() {
        WorkerFragment workerFragment = getWorkerFragment();
        if (workerFragment != null) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.remove(workerFragment);
            transaction.commit();
        }
    }

    private WorkerFragment getWorkerFragment() {
        FragmentManager manager = getSupportFragmentManager();
        return (WorkerFragment) manager.findFragmentByTag(WorkerFragment.TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        mCompositeDisposable.clear();
    }

    @Override
    public void onWorkPrepared(ConnectableObservable<String> workFlow) {
        DisposableObserver<String> disposableObserver = new DisposableObserver<String>() {

            @Override
            public void onNext(String message) {
                mTxtContent.setText(message);
            }

            @Override
            public void onError(Throwable throwable) {
                onWorkerFinished();
                mTxtContent.setText("任务错误");
            }

            @Override
            public void onComplete() {
                onWorkerFinished();
                mTxtContent.setText("任务完成");
            }

        };
        workFlow.observeOn(AndroidSchedulers.mainThread()).subscribe(disposableObserver);
        mCompositeDisposable.add(disposableObserver);
    }
}

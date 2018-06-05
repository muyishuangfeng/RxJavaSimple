package com.yk.rxjava.wigedit.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.yk.rxjava.impl.IHolder;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Silence on 2018/6/5.
 * 在onCreate中创建了数据源，它每隔1s向下游发送数据，在onResume中，通过前面定义的接口向Activity传递
 * 一个ConnectableObservable用于监听。这里最关键的是需要调用setRetainInstance方法，
 * 最后别忘了，在onDetach中将mHolder置为空，否则它就会持有需要被重建的Activity示例，
 * 从而导致内存泄漏
 */

public class WorkerFragment extends Fragment {

    public static final String TAG = WorkerFragment.class.getName();

    private ConnectableObservable<String> mWorker;
    private IHolder mHolder;
    //用于Fragment向Activity一个ConnectableObservable，
    // 使得Activity可以监听到Fragment中后台任务的工作进度
    private Disposable mDisposable;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IHolder) {
            mHolder = (IHolder) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         *setRetainInstance方法
         * 如果给Fragment设置了该标志位，那么在屏幕旋转之后，虽然它依附的Activity被销毁了，
         * 但是该Fragment的实例会被保留，并且在Activity的销毁过程中，只会调用该Fragment的onDetach方法，
         * 而不会调用onDestroy方法。而在Activity重建时，会调用该Fragment实例的onAttach、
         * onActivityCreated方法，但不会调用onCreate方法。根据Fragment提供的这一特性，那么我们就可以
         * 将一些在屏幕旋转过程中，仍然需要运行的任务放在具有该属性的Fragment中执行
         */
        setRetainInstance(true);
        if (mWorker != null) {
            return;
        }
        Bundle bundle = getArguments();
        final String taskName = (bundle != null ? bundle.getString("task_name") : null);
        mWorker = Observable.create(new ObservableOnSubscribe<String>() {

            @Override
            public void subscribe(ObservableEmitter<String> observableEmitter) throws Exception {

                for (int i = 0; i < 10; i++) {
                    String message = "任务名称=" + taskName + ", 任务进度=" + i * 10 + "%";
                    try {
                        Log.e(TAG, message);
                        Thread.sleep(1000);
                        //如果已经抛弃，那么不再继续任务。
                        if (observableEmitter.isDisposed()) {
                            break;
                        }
                    } catch (InterruptedException error) {
                        if (!observableEmitter.isDisposed()) {
                            observableEmitter.onError(error);
                        }
                    }
                    observableEmitter.onNext(message);
                }
                observableEmitter.onComplete();
            }

        }).subscribeOn(Schedulers.io()).publish();
        mDisposable = mWorker.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mHolder != null) {
            mHolder.onWorkPrepared(mWorker);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposable.dispose();
        Log.e(TAG, "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mHolder = null;
    }
}

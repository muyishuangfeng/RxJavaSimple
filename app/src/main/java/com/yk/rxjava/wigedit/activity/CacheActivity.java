package com.yk.rxjava.wigedit.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.yk.rxjava.R;
import com.yk.rxjava.model.CacheBean;
import com.yk.rxjava.wigedit.adapter.CacheAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * 使用concat实现
 * 使用concatEager实现
 * 使用merge实现
 * 使用publish实现
 */
public class CacheActivity extends AppCompatActivity {

    Button mBtnConcat, mBtnConcatEager, mBtnMerge, mBtnPublish, mBtnScheduled;
    RecyclerView mRlvCache;
    CacheAdapter mAdapter;
    LinearLayoutManager mManager;
    List<CacheBean> mList = new ArrayList<>();
    private static final String TAG = CacheActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache);
        initView();
    }

    private void initView() {
        mBtnConcat = findViewById(R.id.btn_concat);
        mBtnConcat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConcat();
            }
        });

        mBtnConcatEager = findViewById(R.id.btn_concatEager);
        mBtnConcatEager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConcatEager();
            }
        });
        mBtnMerge = findViewById(R.id.btn_merge);
        mBtnMerge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMerge();
            }
        });
        mBtnPublish = findViewById(R.id.btn_publish);
        mBtnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPublish();
            }
        });
        mBtnScheduled = findViewById(R.id.btn_scheduled);
        mBtnScheduled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CacheActivity.this,
                        ScheduledActivity.class));
            }
        });

        mRlvCache = findViewById(R.id.rlv_cache);
        mManager = new LinearLayoutManager(this);
        mRlvCache.setLayoutManager(mManager);
        mAdapter = new CacheAdapter(this, mList);
        mRlvCache.setAdapter(mAdapter);
    }


    /**
     * 获取缓存数据
     *
     * @return
     */
    private Observable<List<CacheBean>> getCacheConcat(final long simulateTime) {
        return Observable.create(new ObservableOnSubscribe<List<CacheBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<CacheBean>> emitter) throws Exception {
                try {
                    Log.e(TAG, "开始加载缓存数据");
                    Thread.sleep(simulateTime);
                    List<CacheBean> mList = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        CacheBean mBean = new CacheBean();
                        mBean.setType("缓存");
                        mBean.setDesc("序号=" + i);
                        mList.add(mBean);
                    }
                    emitter.onNext(mList);
                    emitter.onComplete();
                    Log.e(TAG, "结束加载缓存数据");
                } catch (InterruptedException ex) {
                    if (!emitter.isDisposed()) {
                        emitter.onError(ex);
                    }
                }
            }
        });
    }

    /**
     * 获取网络数据
     */
    private Observable<List<CacheBean>> getNetConcat(final long simulateTime) {
        return Observable.create(new ObservableOnSubscribe<List<CacheBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<CacheBean>> emitter) throws Exception {
                try {
                    Log.e(TAG, "开始加载网络数据");
                    Thread.sleep(simulateTime);
                    List<CacheBean> mList = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        CacheBean entity = new CacheBean();
                        entity.setType("网络");
                        entity.setDesc("序号=" + i);
                        mList.add(entity);
                    }
                    emitter.onNext(mList);
                    emitter.onComplete();
                    Log.e(TAG, "结束加载网络数据");
                } catch (InterruptedException e) {
                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                    }
                }
            }
        });
    }

    /**
     * 获取网络数据
     */
    private Observable<List<CacheBean>> getNetPublish(final long simulateTime) {
        return Observable.create(new ObservableOnSubscribe<List<CacheBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<CacheBean>> emitter) throws Exception {
                try {
                    Log.e(TAG, "开始加载网络数据");
                    Thread.sleep(simulateTime);
                    List<CacheBean> mList = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        CacheBean entity = new CacheBean();
                        entity.setType("网络");
                        entity.setDesc("序号=" + i);
                        mList.add(entity);
                    }
                    //a.正常情况。
                    // emitter.onNext(mList);
                    // emitter.onComplete();
                    //b.发生异常
                    emitter.onError(new Throwable("netWork Error"));
                    Log.e(TAG, "结束加载网络数据");
                } catch (InterruptedException e) {
                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                    }
                }
            }
        }).onErrorResumeNext(new Function<Throwable, ObservableSource<? extends List<CacheBean>>>() {
            @Override
            public ObservableSource<? extends List<CacheBean>> apply(Throwable throwable) throws Exception {
                Log.e(TAG, "网络请求发生错误throwable=" + throwable);
                //表示一个永远不发送事件的上游
                return Observable.never();
            }
        });
    }

    /**
     * 观察者
     *
     * @return
     */
    private DisposableObserver<List<CacheBean>> getObserver() {
        return new DisposableObserver<List<CacheBean>>() {
            @Override
            public void onNext(List<CacheBean> cacheBeans) {
                mList.clear();
                mList.addAll(cacheBeans);
                Log.e(TAG, "SIZE:" + mList.size());
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "加载错误, e=" + e);
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "加载完成");
            }
        };
    }

    /**
     * concat操作符
     * 同时连接多个observable，并且必须等到前一个observable数据发送完之后，才会发送第二个observable
     * 的数据
     * </br>
     * 缺点：有点浪费时间，必须等待前一个完成下一个才加载
     */
    private void startConcat() {
        Observable<List<CacheBean>> observable = Observable.concat(
                getCacheConcat(500)
                        .subscribeOn(Schedulers.io()),
                getNetConcat(2000)
                        .subscribeOn(Schedulers.io()));
        DisposableObserver<List<CacheBean>> disposableObserver = getObserver();
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe(disposableObserver);
    }

    /**
     * concatEager操作符
     * <p>
     * 多个Observable可以同时开始发射数据，如果后一个Observable发射完成后，前一个Observable还没有发射
     * 完数据，那么它会将后一个Observable的数据先缓存起来，等到前一个Observable发射完毕后，
     * 将缓存的数据发射出去。
     * <p>
     * 缺点：在某些异常情况下，如果读取缓存的时间要大于网络请求的时间，那么就会导致出现
     * “网络请求的结果”等待“读取缓存”这一过程完成后才能传递给下游，白白浪费了一段时间。
     * 也就是说起一个observable如果加载的慢了，第二个必须等待第一个加载完之后才能执行后续的操作
     * 这个就有点坑了。比如买包子（前面的人速度很慢，而第二个人速度快并且有事，这种是不是就是很不爽，
     * 都想去抽第一个人了）
     */
    private void startConcatEager() {
        List<Observable<List<CacheBean>>> observables = new ArrayList<>();
        observables.add(getCacheConcat(2000).subscribeOn(Schedulers.io()));
        observables.add(getNetConcat(2000).subscribeOn(Schedulers.io()));
        Observable<List<CacheBean>> observable = Observable.concatEager(observables);
        DisposableObserver<List<CacheBean>> disposableObserver = getObserver();
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe(disposableObserver);
    }

    /**
     * Merge操作符
     * <p>
     * 和concatEager一样，会让多个Observable同时开始发射数据，但是它不需要Observable之间的互相等待，
     * 而是直接发送给下游
     * <p>
     * 缺点：
     * 如果缓存数据等待时间比较长，而网络数据等待时间比较短，会直接先加载出网络数据，但是当缓存数据
     * 加载出来之后，会用缓存数据覆盖网络数据（有时候会用旧数据覆盖新数据）
     */
    private void startMerge() {
        Observable<List<CacheBean>> observable = Observable.merge(
                getCacheConcat(2000)
                        .subscribeOn(Schedulers.io()),
                getNetConcat(500)
                        .subscribeOn(Schedulers.io()));
        DisposableObserver<List<CacheBean>> disposableObserver = getObserver();
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe(disposableObserver);

    }

    private void startPublish() {
        Observable<List<CacheBean>> observable = getNetPublish(2000)
                .subscribeOn(Schedulers.io())
                /*
                 *Publish操作符
                 * 调用merge和takeUntil会发生两次订阅，这时候就需要使用publish操作符，
                 * 它接收一个Function函数，该函数返回一个Observable，该Observable是对原Observable，
                 * 也就是上面网络源的Observable转换之后的结果，该Observable可以被takeUntil和merge
                 * 操作符所共享，从而实现只订阅一次的效果
                 */
                .publish(new Function<Observable<List<CacheBean>>, ObservableSource<List<CacheBean>>>() {
                    @Override
                    public ObservableSource<List<CacheBean>> apply(Observable<List<CacheBean>> listObservable) throws Exception {
                        return Observable
                                /*
                                 * merge操作符，让缓存源和网络源同时开始工作，去取数据
                                 */
                                .merge(listObservable, getCacheConcat(500)
                                        .subscribeOn(Schedulers.io())
                                        /*
                                         * takeUntil传入了另一个otherObservable，它表示
                                         * sourceObservable在otherObservable发射数据之后，
                                         * 就不允许再发射数据了，这就刚好满足了我们前面说的
                                         * “只要网络源发送了数据，那么缓存源就不应再发射数据”
                                         */
                                        .takeUntil(listObservable));
                    }
                });
        DisposableObserver<List<CacheBean>> disposableObserver = getObserver();
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe(disposableObserver);
    }

}

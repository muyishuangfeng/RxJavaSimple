package com.yk.rxjava.wigedit.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.yk.rxjava.R;
import com.yk.rxjava.model.WeatherBean;
import com.yk.rxjava.net.Api;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 检测网络状态
 */
public class NetActivity extends AppCompatActivity {

    private static final String TAG = NetActivity.class.getSimpleName();
    private static final long[] CITY_ARRAY = new long[]{
            101010100L,
            101010100L,
            101010100L,
            101030100L
    };

    private CompositeDisposable mCompositeDisposable;
    private TextView mTvNetworkResult;
    private PublishSubject<Boolean> mNetStatusPublish;
    private PublishSubject<Long> mCityPublish;
    private BroadcastReceiver mReceiver;
    private long mCacheCity = -1;
    private Thread mLocationThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net);
        mTvNetworkResult = findViewById(R.id.tv_net);
        mNetStatusPublish = PublishSubject.create();
        mCityPublish = PublishSubject.create();
        mCompositeDisposable = new CompositeDisposable();
        registerBroadcast();
        startUpdateLocation();
        startUpdateWeather();
    }

    private void startUpdateWeather() {
        Observable.merge(getCityPublish(), getNetStatusPublish())
                .flatMap(new Function<Long, ObservableSource<WeatherBean>>() {

                    @Override
                    public ObservableSource<WeatherBean> apply(Long aLong) throws Exception {
                        Log.e(TAG, "尝试请求天气信息=" + aLong);
                        return getWeather(aLong).subscribeOn(Schedulers.io());
                    }

                }).retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {

            @Override
            public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Exception {
                return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {

                    @Override
                    public ObservableSource<?> apply(Throwable throwable) throws Exception {
                        Log.e(TAG, "请求天气信息过程中发生错误，进行重订阅");
                        return Observable.just(0);
                    }

                });
            }

        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<WeatherBean>() {

            @Override
            public void onSubscribe(Disposable disposable) {
                mCompositeDisposable.add(disposable);
            }

            @Override
            public void onNext(WeatherBean weatherEntity) {
                WeatherBean.WeatherInfo info = weatherEntity.getWeatherinfo();
                if (info != null) {
                    Log.e(TAG, "尝试请求天气信息成功");
                    StringBuilder builder = new StringBuilder();
                    builder.append("城市名：").append(info.getCity()).append("\n")
                            .append("温度：").append(info.getTemp()).append("\n")
                            .append("风向：").append(info.getWD()).append("\n")
                            .append("风速：").append(info.getWS()).append("\n");
                    mTvNetworkResult.setText(builder.toString());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "尝试请求天气信息失败");
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "尝试请求天气信息结束");
            }
        });
    }

    private Observable<Long> getCityPublish() {
        return mCityPublish.distinctUntilChanged().doOnNext(new Consumer<Long>() {

            @Override
            public void accept(Long aLong) throws Exception {
                saveCacheCity(aLong);
            }

        });
    }

    private Observable<Long> getNetStatusPublish() {
        return mNetStatusPublish.filter(new Predicate<Boolean>() {

            @Override
            public boolean test(Boolean aBoolean) throws Exception {
                return aBoolean && getCacheCity() > 0;
            }

        }).map(new Function<Boolean, Long>() {

            @Override
            public Long apply(Boolean aBoolean) throws Exception {
                return getCacheCity();
            }

        }).subscribeOn(Schedulers.io());
    }

    private Observable<WeatherBean> getWeather(long cityId) {
        Api api = new Retrofit.Builder()
                .baseUrl("http://www.weather.com.cn/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build().create(Api.class);
        return api.getWeather(cityId);
    }

    //模拟定位模块的回调。
    private void startUpdateLocation() {
        mLocationThread = new Thread() {

            @Override
            public void run() {
                //示例一：
                while (true) {
                    try {
                        for (long cityId : CITY_ARRAY) {
                            if (isInterrupted()) {
                                break;
                            }
                            Log.e(TAG, "重新定位");
                            Thread.sleep(1000);
                            Log.d(TAG, "定位到城市信息=" + cityId);
                            mCityPublish.onNext(cityId);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //示例二：
                //mCityPublish.onNext(CITY_ARRAY[0]);
            }

        };
        mLocationThread.start();
    }


    private void registerBroadcast() {
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (mNetStatusPublish != null) {
                    mNetStatusPublish.onNext(isNetworkConnected());
                }
            }

        };
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
    }

    public void unRegisterBroadcast() {
        unregisterReceiver(mReceiver);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getApplication()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private Long getCacheCity() {
        return mCacheCity;
    }

    private void saveCacheCity(Long cacheCity) {
        mCacheCity = cacheCity;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationThread.interrupt();
        unRegisterBroadcast();
        mCompositeDisposable.clear();
    }
}

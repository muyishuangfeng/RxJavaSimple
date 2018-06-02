package com.yk.rxjava.wigedit.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.yk.rxjava.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class SearchActivity extends AppCompatActivity {

    EditText mEdtSearch;
    TextView mTxtSearch;
    CompositeDisposable mDisposable;
    PublishSubject<String> mPublishSubject;
    DisposableObserver<String> mObserver;
    Button mBtnRetrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initView();
    }

    private void initView() {
        mEdtSearch = findViewById(R.id.edt_search);
        mTxtSearch = findViewById(R.id.txt_search);
        mBtnRetrofit=findViewById(R.id.btn_retrofit);
        mBtnRetrofit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this,
                        RetrofitActivity.class));
            }
        });

        mEdtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                startSearch(s.toString());
            }
        });
        mPublishSubject = PublishSubject.create();
        mObserver = new DisposableObserver<String>() {
            @Override
            public void onNext(String s) {
                mTxtSearch.setText(s);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        /**
         * debounce操作符，当上游有消息的时候不会立刻传递到下游，会等待一段时间，当等待的这段时间
         * 上游没有消息的时候再传递消息到下游去。
         */
        mPublishSubject
                .debounce(200, TimeUnit.MILLISECONDS)
                /**
                 * filter操作符，用于过滤无用的消息，就是传入一个Predicate函数，
                 * 其参数为上游发送的事件，只有该函数返回true时，才会将事件发送给下游
                 * 这里用于过滤长度小于零的数据
                 */
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        return s.length() > 0;
                    }
                })
                /**
                 * switchMap 操作符 将上游的事件转换为一个或者多个observable，但是，当该节点收到新的
                 * 消息后，之前收到的observable还没有传递给下游，那么当前收到的observable也不会再传递了
                 * 即下游也不会再收到新的消息了
                 */
                .switchMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                return getSearchObservable(s);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(mObserver);
        mDisposable = new CompositeDisposable();
        mDisposable.add(mObserver);
    }

    /**
     * 开始查找
     */
    private void startSearch(String query) {
        mPublishSubject.onNext(query);
    }

    private Observable<String> getSearchObservable(final String query) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Log.e("SearchActivity", "开始请求，关键词为：" + query);
                try {
                    Thread.sleep(100 +(long) ( Math.random() * 500));
                }catch (InterruptedException ex){
                    ex.printStackTrace();
                }
                Log.e("SearchActivity", "结束请求，关键词为：" + query);
                emitter.onNext("完成搜索，关键词为：" + query);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }
}

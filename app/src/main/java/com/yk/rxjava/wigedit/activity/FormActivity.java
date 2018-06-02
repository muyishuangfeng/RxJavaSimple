package com.yk.rxjava.wigedit.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.yk.rxjava.R;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * combineLatest 操作符 表单验证
 * <p>
 * 该操作符接受多个Observable以及一个函数作为参数，并且函数的签名为这些Observable发射的数据类型。
 * 当上游发送的任意一个Observable发射数据之后，会去取其它Observable 最近一次发射的数据，
 * 回调到函数当中，但是该函数回调的前提是所有的Observable都至少发射过一个数据项
 * <p>
 * <p>（这个不理解可以看下面的解释）
 * zip和combineLatest的区别在于：zip必须发送完成才组合，而combinelastest是多个observable只要都至少
 * 发送一个数据之后都可以组合，不必是发送完成
 * <p>
 * <p> （这个如果还不不理解可以看下面的解释）
 * zip是在其中一个Observable发射数据项后，组合所有Observable最早一个未被组合的数据项，
 * 也就是说，组合后的Observable发射的第n个数据项，必然是每个源由Observable各自发射的第n个数据项构成的。
 * <p>
 * combineLatest则是在其中一个Observable发射数据项后，组合所有Observable所发射的最后一个数据项
 * （前提是所有的Observable都至少发射过一个数据项）
 * <p>
 * （这个是最终的解释）
 * zip在同步的时候等待所有的数据发送完成才组合然后交给下游处理。异步的时候是多个
 * observable都各自发送了一个之后然后组合处理，并且以数据最少的那个obserable为准。
 * combineLatest如果需要组合必须是多个observable必须要有一个发射的数据源，这样才可以组合处理。
 */
public class FormActivity extends AppCompatActivity {

    Button mBtnNext, mBtnLogin;
    EditText mEdtName, mEdtPass;
    CompositeDisposable mDisposable;
    //订阅
    PublishSubject<String> mNamePublish;
    PublishSubject<String> mPassPublish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        initView();
    }

    /**
     * 初始化
     * 在下面的例子中，我们首先创建了两个PublishSubject，分别用于用户名和密码的订阅，
     * 然后通过combineLatest对这两个PublishSubject进行组合。这样，
     * 当任意一个PublishSubject发送事件之后，就会回调combineLatest最后一个函数的apply方法，
     * 该方法会取到每个被观察的PublishSubject最后一次发射的数据，我们通过该数据进行验证。
     */
    private void initView() {
        mBtnLogin = findViewById(R.id.btn_login);
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mBtnNext = findViewById(R.id.btn_next);
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FormActivity.this,
                        CacheActivity.class));
            }
        });
        mEdtName = findViewById(R.id.edt_name);
        mEdtPass = findViewById(R.id.edt_password);
        mDisposable = new CompositeDisposable();
        mNamePublish = PublishSubject.create();
        mPassPublish = PublishSubject.create();
        mEdtPass.addTextChangedListener(new EditTextWatcher(mPassPublish));
        mEdtName.addTextChangedListener(new EditTextWatcher(mNamePublish));

        Observable<Boolean> observable = Observable.combineLatest(mNamePublish, mPassPublish,
                new BiFunction<String, String, Boolean>() {
                    @Override
                    public Boolean apply(String name, String pass) throws Exception {
                        int mNameLength = name.length();
                        int mPassLength = pass.length();
                        return mNameLength >= 2 && mNameLength <= 8 &&
                                mPassLength >= 4 && mPassLength <= 16;
                    }
                });

        DisposableObserver<Boolean> observer = new DisposableObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                mBtnLogin.setText(aBoolean ? "登录" : "用户名或者密码无效");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
        mDisposable.add(observer);

    }

    /**
     * 监听变化
     */
    private class EditTextWatcher implements TextWatcher {
        private PublishSubject<String> mPublishSubject;

        public EditTextWatcher(PublishSubject<String> publishSubject) {
            this.mPublishSubject = publishSubject;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mPublishSubject.onNext(s.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }
}

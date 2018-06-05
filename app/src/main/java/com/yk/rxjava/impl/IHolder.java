package com.yk.rxjava.impl;

import io.reactivex.observables.ConnectableObservable;

/**
 * Created by Silence on 2018/6/5.
 */

public interface IHolder {

    public void onWorkPrepared(ConnectableObservable<String> workFlow);
}

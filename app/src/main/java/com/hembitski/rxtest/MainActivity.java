package com.hembitski.rxtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.reactivestreams.Publisher;

import java.io.IOException;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private final static Object SYNC_OBJECT = new Object();

    private volatile int i = 1;
    private boolean bool;

    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onButtonClick(View view) {
        for (int j = 0; j < 10; j++) {
            go();
        }
    }

    public void go() {
        Single.just(1)
                .flatMap(integer -> request(getInteger()))
                .retryWhen(new Function<Flowable<Throwable>, Publisher<?>>() {
                    @Override
                    public Publisher<?> apply(Flowable<Throwable> throwableFlowable) throws Exception {
                        return throwableFlowable.flatMap(throwable -> refreshToken()
//                                .flatMap(Single::just)
                                .onErrorResumeNext(throwable1 -> {
                                    if(throwable1 instanceof IOException) {
                                        return Single.error(new RuntimeException("exit"));
                                    } else {
                                        return Single.error(throwable1);
                                    }
                                })
                                .toFlowable()); // resubscribe
//                        return throwableFlowable.flatMap(throwable -> Flowable.just("")); // resubscribe
//                        return throwableFlowable.flatMap(throwable -> Flowable.error(new IOException())); // not subscribe
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d("HMfilter", "onSubscribe");
                    }

                    @Override
                    public void onSuccess(Object o) {
                        Log.d("HMfilter", "onSuccess");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("HMfilter", "onError");
                    }
                });
    }

    private Integer getInteger() {
        Log.d("HMfilter", "getInteger");
        return i;
    }

    private Single<Object> request(int i) {
        if(i == 2) {
            Log.d("HMfilter", "requst object");
            return Single.just(new Object());
        } else {
            Log.d("HMfilter", "requst error");
            return Single.error(new RuntimeException());
        }
    }

    private Single<Integer> refreshToken() {
        return Single.just(1).flatMap(integer -> refresh());
    }

    private Single<Integer> refresh() {
        synchronized (SYNC_OBJECT) {
            if (i == 2) {
                Log.d("HMfilter", "refresh NOT update token");
                return Single.just(2);
            }
            try {
                Thread.sleep(5000);
                Log.d("HMfilter", "refresh UPDATE token");
                i = 2; // or IOException - RefreshTokenExeption
            } catch (InterruptedException e) {
                return Single.error(new IOException());
            }
            return Single.just(2);
        }
    }
}

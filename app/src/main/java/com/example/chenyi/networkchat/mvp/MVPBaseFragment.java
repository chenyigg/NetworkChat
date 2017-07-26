package com.example.chenyi.networkchat.mvp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.lang.reflect.ParameterizedType;

/**
 * Created by chenyi on 2017/5/6.
 */

public class MVPBaseFragment<V extends BaseView, P extends BasePresenterImpl<V>> extends Fragment implements BaseView {

    P mPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = getInstance(this, 1);
        if (mPresenter != null) {
            mPresenter.attachView((V) this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.detachView();
        }
    }

    @Override
    public Context getContext() {
        return super.getContext();
    }

    public  <P> P  getInstance(Object o, int i) {
        try {
            return ((Class<P>) ((ParameterizedType) (o.getClass()
                    .getGenericSuperclass())).getActualTypeArguments()[i])
                    .newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassCastException | java.lang.InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }
}

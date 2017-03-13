package com.akruglov.imagedownloaderhl;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by akruglov on 13.03.17.
 */

public abstract class SingleFragmentActivity extends AppCompatActivity {

    private static final String FRAGMENT_TAG =
            "com.akruglov.habrhabrhl.SingleFragmentActivity.main_fragment";

    protected abstract Fragment createFragment();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_fragment);

        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment = fm.findFragmentByTag(FRAGMENT_TAG);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment, FRAGMENT_TAG)
                    .commit();
        }
    }
}

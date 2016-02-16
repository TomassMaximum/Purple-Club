package com.project.tom.purpleclub;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import static com.project.tom.purpleclub.R.id.shot_detail_toolbar;

public class ShotDetailActivity extends AppCompatActivity {

    private static final String TAG = "ShotDetailActivity";

    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager shotViewPager;
    ShotPagerAdapter shotPagerAdapter;

    String shot_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shot_detail);

        //获取当前的shot id，向数据库请求相应的数据
        shot_id = getIntent().getStringExtra("shot_id");

        tabLayout = (TabLayout) findViewById(R.id.shot_detail_tab_layout);
        shotViewPager = (ViewPager) findViewById(R.id.shot_viewpager);
        shotPagerAdapter = new ShotPagerAdapter(getSupportFragmentManager(),shot_id);
        shotViewPager.setAdapter(shotPagerAdapter);

        tabLayout.setTabsFromPagerAdapter(shotPagerAdapter);
        tabLayout.setupWithViewPager(shotViewPager);

        shotViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        //将应用栏的文字设为当前shot的标题
        String title = getIntent().getStringExtra("title");
        toolbar = (Toolbar) findViewById(shot_detail_toolbar);
        if (toolbar != null){
            toolbar.setTitle(title);
        }
    }
}

class ShotPagerAdapter extends FragmentStatePagerAdapter{

    String shot_id;

    public ShotPagerAdapter(FragmentManager fm,String shot_id){
        super(fm);
        this.shot_id = shot_id;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position){
            case 0:
                fragment = ShotDetailFragment.newInstance(shot_id);
                break;
            case 1:
                fragment = ShotCommentFragment.newInstance(shot_id);
                break;
            default:
                break;
        }
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        switch (position){
            case 0:
                title = "详情";
                break;
            case 1:
                title = "评论";
                break;
        }
        return title;
    }

    @Override
    public int getCount() {
        return 2;
    }
}




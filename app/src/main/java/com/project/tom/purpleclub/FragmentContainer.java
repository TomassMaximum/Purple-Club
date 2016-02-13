package com.project.tom.purpleclub;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Tom on 2016/2/4.
 */
public class FragmentContainer extends Fragment {

    private static final String TAG = "FragmentContainer";
    TabLayout tabLayout;
    ViewPager viewPager;
    MyPagerAdapter myPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_container,container,false);
        Log.e(TAG, "Container被调用");

        tabLayout = (TabLayout) getActivity().findViewById(R.id.tab_layout);

        viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        myPagerAdapter = new MyPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(myPagerAdapter);
        viewPager.setOffscreenPageLimit(4);
        tabLayout.setTabsFromPagerAdapter(myPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        return rootView;
    }
}

class MyPagerAdapter extends FragmentStatePagerAdapter{

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public Fragment getItem(int position) {
        Log.e("MyPagerAdapter的getItem","position为" + position);

        return FragmentPage.newInstance(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String currentTab = "";
        switch (position){
            case 0:
                currentTab = "最受关注";
                break;
            case 1:
                currentTab = "最新发布";
                break;
            case 2:
                currentTab = "最受欣赏";
                break;
            case 3:
                currentTab = "最受议论";
                break;
            default:
                break;
        }
        return currentTab;
    }

    @Override
    public int getCount() {
        return 4;
    }
}

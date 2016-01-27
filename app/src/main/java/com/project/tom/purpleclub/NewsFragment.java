package com.project.tom.purpleclub;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Tom on 2016/1/25.
 */
public class NewsFragment extends Fragment {

    public final String TAG = "NewsFragment";

    protected RecyclerView recyclerView;
    protected RecyclerViewAdapter myAdapter;
    protected RecyclerView.LayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_news,container,false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        //为recyclerview添加分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));


        layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);

        myAdapter = new RecyclerViewAdapter();

        recyclerView.setAdapter(myAdapter);

        return rootView;
    }
}

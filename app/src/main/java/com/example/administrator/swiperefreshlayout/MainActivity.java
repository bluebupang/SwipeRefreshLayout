package com.example.administrator.swiperefreshlayout;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/2/28.
 */
public class MainActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener, RefreshLayout.OnLoadListener {


    private RefreshLayout swipeLayout;
    private QQListView listView;
    private MyAdapter adapter;
    private List<Integer> list = new ArrayList<Integer>();
    private View header;
    private int y = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        initView();
        setData();
        setListener();
    }

    private void initView() {
        swipeLayout = (RefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(android.R.color.holo_green_light, android.R.color.holo_blue_light, android.R.color.holo_green_dark);

    }

    private void setData() {
        list = new ArrayList<>();
        for (int i = 3; i < 12; i++) {
            list.add(i);
        }

        listView = (QQListView) findViewById(R.id.list);
        // listView.addHeaderView(header);
        adapter = new MyAdapter(this, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {

                Toast.makeText(getApplicationContext(),
                        "这是第" + String.valueOf(position + 1) + "项",
                        Toast.LENGTH_SHORT).show();

            }
        });

        listView.setDelButtonClickListener(new QQListView.DelButtonClickListener() {

            @Override
            public void clickHappend(int position) {
                list.remove(position);
                refresh();
            }
        });

    }

    private void setListener() {
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setOnLoadListener(this);
    }

    @Override
    public void onRefresh() {
        swipeLayout.postDelayed(new Runnable() {

            @Override
            public void run() {
                // 更新数据
                list.clear();
                y = 12;
                for (int i = 0; i < 13; i++) {
                    list.add(i);
                }
                refresh();

                // 更新完后调用该方法结束刷新
                swipeLayout.setRefreshing(false);
            }
        }, 1000);

    }

    @Override
    public void onLoad() {
        swipeLayout.postDelayed(new Runnable() {

            @Override
            public void run() {
                // 更新数据
                y++;
                list.add(y);
                refresh();
                // 更新完后调用该方法结束刷新
                swipeLayout.setLoading(false);
            }
        }, 1000);
    }

    private void refresh() {
        adapter.setList(list);
        adapter.notifyDataSetChanged();
    }
}

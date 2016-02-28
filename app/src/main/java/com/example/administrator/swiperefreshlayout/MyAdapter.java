package com.example.administrator.swiperefreshlayout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends BaseAdapter {
	public List<Integer> list;
	public Context context;
	public LayoutInflater layoutInflater;

	public MyAdapter(Context context, List<Integer> list) {
		this.context = context;
		this.list = list;
		layoutInflater = LayoutInflater.from(context);
	}

	public void setList(List<Integer> list) {
		this.list = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHolder holder = null;
		if (convertView == null) {
			view = layoutInflater.inflate(R.layout.item, null);

			holder = new ViewHolder();
			holder.txt = (TextView) view.findViewById(R.id.textView1);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		holder.txt.setText(String.valueOf(list.get(position)));

		return view;
	}

	static class ViewHolder {
		TextView txt;

	}

}

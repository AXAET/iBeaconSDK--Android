package com.axaet.demo.adpter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class CommonBaseAdpter<T> extends BaseAdapter {

	public List<T> list = new ArrayList<T>();
	public LayoutInflater inflater;
	public Resources resources;

	public CommonBaseAdpter(Context context) {
		inflater = LayoutInflater.from(context);
		resources = context.getResources();
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public T getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void addListData(List<T> list) {
		this.list.addAll(list);
		notifyDataSetChanged();
	}

	public void resetData(List<T> list) {
		this.list.clear();
		this.list = list;
		notifyDataSetChanged();
	}

	public void clearData() {
		this.list.clear();
		notifyDataSetChanged();
	}

	@Override
	abstract public View getView(int position, View convertView, ViewGroup parent);

}

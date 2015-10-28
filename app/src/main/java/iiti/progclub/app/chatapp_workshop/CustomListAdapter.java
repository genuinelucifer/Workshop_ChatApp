package iiti.progclub.app.chatapp_workshop;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CustomListAdapter extends BaseAdapter {
	private ArrayList<CustomListItem> listData;
	private LayoutInflater layoutInflater;

	public CustomListAdapter(Context aContext, ArrayList<CustomListItem> listData) {
		this.listData = listData;
		layoutInflater = LayoutInflater.from(aContext);
	}

	@Override
	public int getCount() {
		return listData.size();
	}

	@Override
	public Object getItem(int position) {
		return listData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.list_item_layout, null);
			holder = new ViewHolder();
			holder.nameView  = (TextView) convertView.findViewById(R.id.tvUsernameListItem);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		CustomListItem li = listData.get(position);
		holder.nameView.setText(li.getName());
		if(li.getHasNewMsgs())
        {
            holder.nameView.setTextColor(Color.parseColor("#121258"));
        }
		else
        {
            holder.nameView.setTextColor(Color.parseColor("#898989"));
        }
		return convertView;
	}

	static class ViewHolder {
		TextView nameView;
	}
}
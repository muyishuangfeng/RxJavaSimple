package com.yk.rxjava.wigedit.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yk.rxjava.R;
import com.yk.rxjava.model.CacheBean;
import com.yk.rxjava.model.NewsResultBean;

import java.util.List;

/**
 * Created by Silence on 2018/5/31.
 */

public class CacheAdapter extends RecyclerView.Adapter<CacheAdapter.CacheViewHolder> {

    List<CacheBean> mList;
    LayoutInflater mInflater;
    Context mContext;

    public CacheAdapter(Context context, List<CacheBean> list) {
        this.mContext = context;
        this.mList = list;
        mInflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public CacheViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_cache_layout, parent, false);
        CacheViewHolder mHolder = new CacheViewHolder(view);
        return mHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CacheViewHolder holder, int position) {
        holder.mTxtType.setText(mList.get(position).getType());
        holder.mTxtContent.setText(mList.get(position).getDesc());
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public class CacheViewHolder extends RecyclerView.ViewHolder {

        TextView mTxtType, mTxtContent;

        public CacheViewHolder(View itemView) {
            super(itemView);
            mTxtType = itemView.findViewById(R.id.txt_item_type);
            mTxtContent = itemView.findViewById(R.id.txt_item_content);
        }
    }


}

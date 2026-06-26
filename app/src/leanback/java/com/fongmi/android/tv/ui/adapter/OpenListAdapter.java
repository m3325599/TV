package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ItemOpenlistBinding;
import com.fongmi.android.tv.openlist.AListApi;
import com.fongmi.android.tv.ui.custom.CustomViewHolder;

import java.util.ArrayList;
import java.util.List;

public class OpenListAdapter extends RecyclerView.Adapter<CustomViewHolder<ItemOpenlistBinding>> {

    private final List<AListApi.AListFile> mItems = new ArrayList<>();
    private final OnClickListener mListener;

    public interface OnClickListener {
        void onItemClick(AListApi.AListFile item);
    }

    public OpenListAdapter(OnClickListener listener) {
        this.mListener = listener;
    }

    public void addAll(List<AListApi.AListFile> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomViewHolder<ItemOpenlistBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOpenlistBinding binding = ItemOpenlistBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CustomViewHolder<>(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder<ItemOpenlistBinding> holder, int position) {
        AListApi.AListFile item = mItems.get(position);
        holder.binding.name.setText(item.getName());
        holder.binding.icon.setImageResource(item.isFolder() ? R.drawable.ic_folder : R.drawable.ic_file);
        holder.itemView.setOnClickListener(view -> {
            if (mListener != null) mListener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
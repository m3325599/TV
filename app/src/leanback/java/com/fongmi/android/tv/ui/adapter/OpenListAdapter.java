package com.fongmi.android.tv.ui.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ItemOpenlistBinding;
import com.fongmi.android.tv.openlist.AListApi;

import java.util.ArrayList;
import java.util.List;

public class OpenListAdapter extends RecyclerView.Adapter<OpenListAdapter.ViewHolder> {

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
        for (AListApi.AListFile file : items) {
            if (file.isFolder() || file.isVideo() || file.isImage()) {
                mItems.add(file);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemOpenlistBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AListApi.AListFile item = mItems.get(position);
        holder.binding.name.setText(item.getName());
        StringBuilder info = new StringBuilder();
        if (item.isFolder()) {
            holder.binding.icon.setImageResource(R.drawable.ic_folder);
            info.append("文件夹");
        } else if (item.isVideo()) {
            holder.binding.icon.setImageResource(R.drawable.ic_file);
            info.append("视频");
        } else if (item.isImage()) {
            holder.binding.icon.setImageResource(R.drawable.ic_file);
            info.append("图片");
        } else {
            holder.binding.icon.setImageResource(R.drawable.ic_file);
        }
        if (!TextUtils.isEmpty(item.getSizeStr())) {
            info.append(" · ").append(item.getSizeStr());
        }
        holder.binding.info.setText(info.toString());
        holder.binding.getRoot().setOnClickListener(view -> {
            if (mListener != null) mListener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemOpenlistBinding binding;

        ViewHolder(@NonNull ItemOpenlistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
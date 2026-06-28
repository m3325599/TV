package com.fongmi.android.tv.ui.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.bean.Episode;
import com.fongmi.android.tv.databinding.DialogBatchDownloadBinding;
import com.fongmi.android.tv.databinding.ItemDownloadEpisodeBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class BatchDownloadDialog extends BaseAlertDialog {

    private DialogBatchDownloadBinding binding;
    private List<Episode> episodes;
    private List<Episode> selected;
    private OnConfirmListener listener;
    private EpisodeAdapter adapter;

    public interface OnConfirmListener {
        void onConfirm(List<Episode> episodes);
    }

    public static BatchDownloadDialog create() {
        return new BatchDownloadDialog();
    }

    public BatchDownloadDialog episodes(List<Episode> episodes) {
        this.episodes = episodes;
        this.selected = new ArrayList<>();
        return this;
    }

    public BatchDownloadDialog listener(OnConfirmListener listener) {
        this.listener = listener;
        return this;
    }

    public void show(FragmentActivity activity) {
        show(activity.getSupportFragmentManager(), null);
    }

    @Override
    protected ViewBinding getBinding() {
        return binding = DialogBatchDownloadBinding.inflate(getLayoutInflater());
    }

    @Override
    protected MaterialAlertDialogBuilder getBuilder() {
        return builder().setView(getBinding().getRoot());
    }

    @Override
    protected void initView() {
        setWidth(0.6f);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter = new EpisodeAdapter());
    }

    @Override
    protected void initEvent() {
        binding.selectAll.setOnClickListener(view -> toggleSelectAll());
        binding.cancel.setOnClickListener(view -> dismiss());
        binding.confirm.setOnClickListener(view -> onConfirm());
    }

    private void toggleSelectAll() {
        if (selected.size() == episodes.size()) {
            selected.clear();
        } else {
            selected.clear();
            selected.addAll(episodes);
        }
        adapter.notifyDataSetChanged();
        updateSelectAllText();
    }

    private void updateSelectAllText() {
        binding.selectAll.setText(selected.size() == episodes.size() ? "取消全选" : getString(R.string.download_select_all));
    }

    private void onConfirm() {
        if (listener != null && !selected.isEmpty()) {
            listener.onConfirm(new ArrayList<>(selected));
        }
        dismiss();
    }

    private class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemDownloadEpisodeBinding itemBinding = ItemDownloadEpisodeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Episode episode = episodes.get(position);
            holder.name.setText(episode.getName());
            holder.checkBox.setChecked(selected.contains(episode));
            holder.itemView.setOnClickListener(view -> {
                if (selected.contains(episode)) {
                    selected.remove(episode);
                } else {
                    selected.add(episode);
                }
                holder.checkBox.setChecked(selected.contains(episode));
                updateSelectAllText();
            });
        }

        @Override
        public int getItemCount() {
            return episodes == null ? 0 : episodes.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkBox;
            TextView name;

            ViewHolder(ItemDownloadEpisodeBinding binding) {
                super(binding.getRoot());
                checkBox = binding.checkBox;
                name = binding.name;
            }
        }
    }
}

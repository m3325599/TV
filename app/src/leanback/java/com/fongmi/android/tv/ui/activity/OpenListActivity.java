package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ActivityOpenlistBinding;
import com.fongmi.android.tv.openlist.AListApi;
import com.fongmi.android.tv.openlist.OpenListSetting;
import com.fongmi.android.tv.ui.adapter.OpenListAdapter;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.utils.Notify;

import java.util.ArrayList;
import java.util.List;

public class OpenListActivity extends BaseActivity implements OpenListAdapter.OnClickListener {

    private ActivityOpenlistBinding mBinding;
    private AListApi mApi;
    private OpenListAdapter mAdapter;
    private List<AListApi.AListFile> mHistory;
    private String currentPath;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, OpenListActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityOpenlistBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mHistory = new ArrayList<>();
        mAdapter = new OpenListAdapter(this);
        mBinding.recycler.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recycler.setAdapter(mAdapter);
        mApi = OpenListSetting.createApi();
        currentPath = OpenListSetting.getMountPath();
        loadFiles(currentPath);
    }

    @Override
    protected void initEvent() {
        mBinding.back.setOnClickListener(view -> onBackPressed());
    }

    private void loadFiles(String path) {
        mBinding.progress.setVisibility(View.VISIBLE);
        mBinding.recycler.setVisibility(View.GONE);
        mBinding.path.setText(path);
        currentPath = path;

        mApi.listFiles(path, new AListApi.ListCallback() {
            @Override
            public void onSuccess(List<AListApi.AListFile> files) {
                mBinding.progress.setVisibility(View.GONE);
                mBinding.recycler.setVisibility(View.VISIBLE);
                mAdapter.addAll(files);
                mBinding.recycler.requestFocus();
            }

            @Override
            public void onError(String error) {
                mBinding.progress.setVisibility(View.GONE);
                Notify.show("Error: " + error);
            }
        });
    }

    @Override
    public void onItemClick(AListApi.AListFile item) {
        if (item.isFolder()) {
            mHistory.add(new AListApi.AListFile());
            mHistory.get(mHistory.size() - 1).setPath(currentPath);
            loadFiles(item.getPath());
        } else if (item.isMedia()) {
            playFile(item);
        } else {
            Notify.show("Unsupported file type");
        }
    }

    private void playFile(AListApi.AListFile file) {
        String url = mApi.getFileUrl(file.getPath());
        String name = file.getName();
        VideoActivity.start(this, "openlist", url, name);
    }

    @Override
    public void onBackPressed() {
        if (mHistory.isEmpty()) {
            super.onBackPressed();
        } else {
            String path = mHistory.remove(mHistory.size() - 1).getPath();
            loadFiles(path);
        }
    }
}
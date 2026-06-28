package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.leanback.widget.VerticalGridView;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.api.SiteApi;
import com.fongmi.android.tv.databinding.ActivityOpenlistBinding;
import com.fongmi.android.tv.openlist.AListApi;
import com.fongmi.android.tv.openlist.OpenListSetting;
import com.fongmi.android.tv.ui.adapter.OpenListAdapter;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.ui.dialog.ImageViewerDialog;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.ResUtil;

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
        setRecyclerView();
        mApi = OpenListSetting.createApi();
        currentPath = OpenListSetting.getMountPath();
        loadFiles(currentPath);
    }

    private void setRecyclerView() {
        mBinding.recycler.setHasFixedSize(true);
        mBinding.recycler.setVerticalSpacing(ResUtil.dp2px(2));
        mBinding.recycler.setAdapter(mAdapter = new OpenListAdapter(this));
    }

    @Override
    protected void initEvent() {
        mBinding.back.setOnClickListener(view -> onBackInvoked());
    }

    private void loadFiles(String path) {
        mBinding.progressLayout.showProgress();
        mBinding.path.setText(path);
        currentPath = path;

        mApi.listFiles(path, new AListApi.ListCallback() {
            @Override
            public void onSuccess(List<AListApi.AListFile> files) {
                mAdapter.addAll(files);
                mBinding.progressLayout.showContent(true, mAdapter.getItemCount());
                mBinding.recycler.setSelectedPosition(0);
            }

            @Override
            public void onError(String error) {
                mBinding.progressLayout.showContent(false, 0);
                Notify.show("Error: " + error);
            }
        });
    }

    @Override
    public void onItemClick(AListApi.AListFile item) {
        if (item.isFolder()) {
            AListApi.AListFile history = new AListApi.AListFile();
            history.setPath(currentPath);
            mHistory.add(history);
            loadFiles(item.getPath());
        } else if (item.isVideo()) {
            playFile(item);
        } else if (item.isImage()) {
            viewImage(item);
        } else {
            Notify.show("Unsupported file type");
        }
    }

    private void viewImage(AListApi.AListFile file) {
        mApi.getFsUrl(file.getPath(), new AListApi.FsGetCallback() {
            @Override
            public void onSuccess(String rawUrl) {
                ImageViewerDialog.create().url(rawUrl).title(file.getName()).show(OpenListActivity.this);
            }

            @Override
            public void onError(String error) {
                Notify.show("图片加载失败: " + error);
            }
        });
    }

    private void playFile(AListApi.AListFile file) {
        mApi.getFsUrl(file.getPath(), new AListApi.FsGetCallback() {
            @Override
            public void onSuccess(String rawUrl) {
                VideoActivity.start(OpenListActivity.this, SiteApi.PUSH, rawUrl, file.getName());
            }

            @Override
            public void onError(String error) {
                Notify.show("获取播放地址失败: " + error);
            }
        });
    }

    @Override
    protected void onBackInvoked() {
        if (mHistory.isEmpty()) {
            super.onBackInvoked();
        } else {
            String path = mHistory.remove(mHistory.size() - 1).getPath();
            loadFiles(path);
        }
    }
}
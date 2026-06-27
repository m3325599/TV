package com.fongmi.android.tv.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ActivityFileBinding;
import com.fongmi.android.tv.ui.adapter.FileAdapter;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.PermissionUtil;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.catvod.utils.Path;

import java.io.File;

public class FileActivity extends BaseActivity implements FileAdapter.OnClickListener {

    public static final String EXTRA_PICK_DIR = "pick_dir";

    private ActivityFileBinding mBinding;
    private FileAdapter mAdapter;
    private File dir;
    private boolean pickDir;

    private boolean isRoot() {
        return Path.root().equals(dir);
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityFileBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        pickDir = getIntent().getBooleanExtra(EXTRA_PICK_DIR, false);
        setRecyclerView();
        if (pickDir) mBinding.selectDirBtn.setVisibility(View.VISIBLE);
        checkPermission();
    }

    @Override
    protected void initEvent() {
        if (pickDir) mBinding.selectDirBtn.setOnClickListener(v -> selectCurrentDir());
    }

    private void selectCurrentDir() {
        if (dir != null && dir.exists() && dir.isDirectory()) {
            setResult(RESULT_OK, new Intent().setData(Uri.fromFile(dir)));
            finish();
        } else {
            Notify.show(R.string.error_play_parse);
        }
    }

    private void setRecyclerView() {
        mBinding.recycler.setHasFixedSize(true);
        mBinding.recycler.setVerticalSpacing(ResUtil.dp2px(16));
        mBinding.recycler.setAdapter(mAdapter = new FileAdapter(this));
    }

    private void checkPermission() {
        PermissionUtil.requestFile(this, allGranted -> update(Path.root()));
    }

    private void update(File dir) {
        mBinding.recycler.setSelectedPosition(0);
        mAdapter.addAll(Path.list(this.dir = dir));
        mBinding.progressLayout.showContent(true, mAdapter.getItemCount());
    }

    @Override
    public void onItemClick(File file) {
        if (file.isDirectory()) {
            update(file);
        } else {
            setResult(RESULT_OK, new Intent().setData(Uri.fromFile(file)));
            finish();
        }
    }

    @Override
    protected void onBackInvoked() {
        if (isRoot()) {
            super.onBackInvoked();
        } else {
            update(dir.getParentFile());
        }
    }
}

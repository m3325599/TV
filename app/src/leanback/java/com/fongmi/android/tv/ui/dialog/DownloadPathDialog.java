package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.DialogDownloadPathBinding;
import com.fongmi.android.tv.setting.DownloadSetting;
import com.fongmi.android.tv.ui.custom.CustomTextListener;
import com.fongmi.android.tv.utils.FileChooser;
import com.fongmi.android.tv.utils.Notify;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

public class DownloadPathDialog extends BaseAlertDialog {

    private DialogDownloadPathBinding binding;

    public static DownloadPathDialog create() {
        return new DownloadPathDialog();
    }

    public void show(FragmentActivity activity) {
        for (Object f : activity.getSupportFragmentManager().getFragments()) if (f instanceof DownloadPathDialog) return;
        show(activity.getSupportFragmentManager(), null);
    }

    @Override
    protected ViewBinding getBinding() {
        return binding = DialogDownloadPathBinding.inflate(getLayoutInflater());
    }

    @Override
    protected MaterialAlertDialogBuilder getBuilder() {
        return builder().setView(getBinding().getRoot());
    }

    @Override
    protected void initView() {
        String path = DownloadSetting.getPath();
        binding.path.setText(path);
        binding.path.setSelection(TextUtils.isEmpty(path) ? 0 : path.length());
        updateSpaceInfo();
    }

    @Override
    protected void initEvent() {
        binding.choose.setOnClickListener(this::onChoose);
        binding.defaultBtn.setOnClickListener(this::onDefault);
        binding.positive.setOnClickListener(this::onPositive);
        binding.negative.setOnClickListener(this::onNegative);
        binding.path.addTextChangedListener(new CustomTextListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSpaceInfo(s.toString());
            }
        });
        binding.path.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) binding.positive.performClick();
            return true;
        });
    }

    private void updateSpaceInfo() {
        updateSpaceInfo(binding.path.getText().toString());
    }

    private void updateSpaceInfo(String pathStr) {
        try {
            File dir = new File(pathStr);
            if (dir.exists() || dir.getParentFile() != null && dir.getParentFile().exists()) {
                android.os.StatFs stat = new android.os.StatFs(dir.exists() ? dir.getAbsolutePath() : dir.getParent());
                long available = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
                binding.spaceInfo.setText(getString(R.string.download_available_space, DownloadSetting.formatSize(available)));
            } else {
                binding.spaceInfo.setText("");
            }
        } catch (Exception e) {
            binding.spaceInfo.setText("");
        }
    }

    private void onChoose(View view) {
        FileChooser.from(launcher).show();
    }

    private void onDefault(View view) {
        String defaultPath = DownloadSetting.getDefaultPath();
        binding.path.setText(defaultPath);
        binding.path.setSelection(defaultPath.length());
    }

    private void onPositive(View view) {
        String path = binding.path.getText().toString().trim();
        if (path.isEmpty()) {
            Notify.show(R.string.download_path_empty);
            return;
        }
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                Notify.show(R.string.download_path_create_failed);
                return;
            }
        }
        if (!dir.canWrite()) {
            Notify.show(R.string.download_path_no_write_permission);
            return;
        }
        DownloadSetting.putPath(path);
        Notify.show(R.string.download_path_set_success);
        dismiss();
    }

    private void onNegative(View view) {
        dismiss();
    }

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null || result.getData().getData() == null) return;
        String path = FileChooser.getPathFromUri(result.getData().getData());
        if (path != null) {
            File file = new File(path);
            if (file.isFile()) path = file.getParent();
            binding.path.setText(path);
            binding.path.setSelection(path.length());
        }
    });
}

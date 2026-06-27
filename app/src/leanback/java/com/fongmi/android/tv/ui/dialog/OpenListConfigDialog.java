package com.fongmi.android.tv.ui.dialog;

import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.DialogOpenlistConfigBinding;
import com.fongmi.android.tv.event.ServerEvent;
import com.fongmi.android.tv.openlist.OpenListSetting;
import com.fongmi.android.tv.server.Server;
import com.fongmi.android.tv.ui.custom.CustomTextListener;
import com.fongmi.android.tv.utils.QRCode;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class OpenListConfigDialog extends BaseAlertDialog {

    private DialogOpenlistConfigBinding binding;

    public static OpenListConfigDialog create() {
        return new OpenListConfigDialog();
    }

    public void show(FragmentActivity activity) {
        for (Object f : activity.getSupportFragmentManager().getFragments()) if (f instanceof OpenListConfigDialog) return;
        show(activity.getSupportFragmentManager(), null);
    }

    @Override
    protected ViewBinding getBinding() {
        return binding = DialogOpenlistConfigBinding.inflate(getLayoutInflater());
    }

    @Override
    protected MaterialAlertDialogBuilder getBuilder() {
        return builder().setView(getBinding().getRoot());
    }

    @Override
    protected void initView() {
        binding.url.setText(OpenListSetting.getServerUrl());
        binding.token.setText(OpenListSetting.getToken());
        binding.code.setImageBitmap(QRCode.getBitmap(Server.get().getAddress(3), 200, 0));
        binding.info.setText(ResUtil.getString(R.string.push_info, Server.get().getAddress()).replace("\uff0c", "\n"));
    }

    @Override
    protected void initEvent() {
        binding.positive.setOnClickListener(this::onPositive);
        binding.negative.setOnClickListener(this::onNegative);
        binding.url.addTextChangedListener(new CustomTextListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                detect(s.toString());
            }
        });
        binding.token.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) binding.positive.performClick();
            return true;
        });
    }

    private void detect(String s) {
        if ("h".equalsIgnoreCase(s)) {
            binding.url.append("ttp://");
        } else if ("f".equalsIgnoreCase(s)) {
            binding.url.append("ile://");
        }
    }

    private void onPositive(View view) {
        String url = binding.url.getText().toString().trim();
        String token = binding.token.getText().toString().trim();
        if (!url.isEmpty()) {
            OpenListSetting.putServerUrl(url);
        }
        OpenListSetting.putToken(token);
        dismiss();
    }

    private void onNegative(View view) {
        dismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServerEvent(ServerEvent event) {
        if (event.type() == ServerEvent.Type.SETTING || event.type() == ServerEvent.Type.OPENLIST) {
            binding.url.setText(event.text());
            binding.url.setSelection(binding.url.getText().length());
            if (event.name() != null) {
                binding.token.setText(event.name());
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setWidth(0.55f);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}

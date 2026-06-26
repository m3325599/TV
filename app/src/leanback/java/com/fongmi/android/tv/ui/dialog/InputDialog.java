package com.fongmi.android.tv.ui.dialog;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.databinding.DialogInputBinding;
import com.fongmi.android.tv.utils.ResUtil;

public class InputDialog extends BaseBottomSheetDialog {

    private DialogInputBinding binding;
    private String title;
    private String hint;
    private String defaultValue;
    private Listener listener;

    public static InputDialog create() {
        return new InputDialog();
    }

    public InputDialog title(String title) {
        this.title = title;
        return this;
    }

    public InputDialog hint(String hint) {
        this.hint = hint;
        return this;
    }

    public InputDialog value(String value) {
        this.defaultValue = value;
        return this;
    }

    public InputDialog listener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public void show(FragmentActivity activity) {
        for (Fragment f : activity.getSupportFragmentManager().getFragments()) if (f instanceof InputDialog) return;
        show(activity.getSupportFragmentManager(), null);
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return binding = DialogInputBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        if (title != null) binding.title.setText(title);
        if (hint != null) binding.input.setHint(hint);
        if (defaultValue != null) binding.input.setText(defaultValue);
    }

    @Override
    protected void initEvent() {
        binding.positive.setOnClickListener(this::onPositive);
        binding.input.setOnEditorActionListener(this::onDone);
    }

    private void onPositive(View view) {
        String value = binding.input.getText().toString().trim();
        if (listener != null) listener.onInput(value);
        dismiss();
    }

    private boolean onDone(TextView view, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) binding.positive.performClick();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(ResUtil.dp2px(300), -1);
    }

    public interface Listener {
        void onInput(String value);
    }
}
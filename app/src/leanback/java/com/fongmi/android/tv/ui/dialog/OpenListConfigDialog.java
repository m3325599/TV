package com.fongmi.android.tv.ui.dialog;

import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.DialogOpenlistConfigBinding;
import com.fongmi.android.tv.event.ServerEvent;
import com.fongmi.android.tv.openlist.AListApi;
import com.fongmi.android.tv.openlist.OpenListSetting;
import com.fongmi.android.tv.server.Server;
import com.fongmi.android.tv.ui.custom.CustomTextListener;
import com.fongmi.android.tv.utils.QRCode;
import com.fongmi.android.tv.utils.ResUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class OpenListConfigDialog extends BaseAlertDialog {

    private DialogOpenlistConfigBinding binding;
    private AListApi mApi;
    private List<String> mFolders;
    private ArrayAdapter<String> mAdapter;
    private String mSelectedPath;

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
    protected void initView() {
        mFolders = new ArrayList<>();
        mFolders.add("/");
        mApi = new AListApi("", "");
        mAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, mFolders);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.path.setAdapter(mAdapter);

        String url = OpenListSetting.getServerUrl();
        String token = OpenListSetting.getToken();
        mSelectedPath = OpenListSetting.getMountPath();
        binding.text.setText(url);
        binding.text.setSelection(TextUtils.isEmpty(url) ? 0 : url.length());
        binding.token.setText(token);
        binding.code.setImageBitmap(QRCode.getBitmap(getQrUrl(url, token, mSelectedPath), 200, 0));
        binding.info.setText(ResUtil.getString(R.string.push_info, Server.get().getAddress()).replace("\uff0c", "\n"));

        if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(token)) {
            fetchFolders(url, token);
        }
    }

    private void fetchFolders(String url, String token) {
        mApi.setServerUrl(url);
        mApi.setToken(token);
        mApi.listFolders("/", new AListApi.ListCallback() {
            @Override
            public void onSuccess(List<AListApi.AListFile> files) {
                mFolders.clear();
                mFolders.add("/");
                for (AListApi.AListFile file : files) {
                    mFolders.add(file.getPath());
                }
                mAdapter.notifyDataSetChanged();
                setSelectedPath(mSelectedPath);
            }

            @Override
            public void onError(String error) {
                // Keep default "/" on error
            }
        });
    }

    private void setSelectedPath(String path) {
        for (int i = 0; i < mFolders.size(); i++) {
            if (mFolders.get(i).equals(path)) {
                binding.path.setSelection(i);
                mSelectedPath = path;
                return;
            }
        }
    }

    private String getQrUrl(String url, String token, String path) {
        return Server.get().getAddress("action") + "?do=openlist" +
                "&url=" + Uri.encode(url) +
                "&token=" + Uri.encode(token) +
                "&path=" + Uri.encode(path);
    }

    @Override
    protected void initEvent() {
        binding.positive.setOnClickListener(this::onPositive);
        binding.negative.setOnClickListener(this::onNegative);
        binding.text.addTextChangedListener(new CustomTextListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                detect(s.toString());
            }
        });
        binding.text.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) binding.token.requestFocus();
            return true;
        });
        binding.token.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                String url = binding.text.getText().toString().trim();
                String token = binding.token.getText().toString().trim();
                if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(token)) {
                    fetchFolders(url, token);
                }
                binding.path.requestFocus();
            }
            return true;
        });
        binding.path.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < mFolders.size()) {
                    mSelectedPath = mFolders.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void detect(String s) {
        if ("h".equalsIgnoreCase(s)) {
            binding.text.append("ttp://");
        } else if ("f".equalsIgnoreCase(s)) {
            binding.text.append("ile://");
        }
    }

    private void onPositive(View view) {
        String url = binding.text.getText().toString().trim();
        String token = binding.token.getText().toString().trim();
        if (!url.isEmpty()) {
            OpenListSetting.putServerUrl(url);
        }
        OpenListSetting.putToken(token);
        OpenListSetting.putMountPath(mSelectedPath);
        dismiss();
    }

    private void onNegative(View view) {
        dismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServerEvent(ServerEvent event) {
        if (event.type() != ServerEvent.Type.OPENLIST) return;
        binding.text.setText(event.text());
        binding.token.setText(event.name());
        String path = event.path();
        if (!TextUtils.isEmpty(path)) {
            fetchFolders(event.text(), event.name());
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
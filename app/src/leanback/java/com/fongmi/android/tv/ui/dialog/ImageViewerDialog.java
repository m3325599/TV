package com.fongmi.android.tv.ui.dialog;

import android.view.View;
import android.widget.ProgressBar;

import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fongmi.android.tv.databinding.DialogImageViewerBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

public class ImageViewerDialog extends BaseAlertDialog {

    private DialogImageViewerBinding binding;
    private String imageUrl;
    private String title;

    public static ImageViewerDialog create() {
        return new ImageViewerDialog();
    }

    public ImageViewerDialog url(String url) {
        this.imageUrl = url;
        return this;
    }

    public ImageViewerDialog title(String title) {
        this.title = title;
        return this;
    }

    public void show(FragmentActivity activity) {
        show(activity.getSupportFragmentManager(), null);
    }

    @Override
    protected ViewBinding getBinding() {
        return binding = DialogImageViewerBinding.inflate(getLayoutInflater());
    }

    @Override
    protected MaterialAlertDialogBuilder getBuilder() {
        return builder().setView(getBinding().getRoot());
    }

    @Override
    protected void initView() {
        if (title != null) {
            binding.title.setText(title);
        } else {
            binding.title.setVisibility(View.GONE);
        }
        binding.progress.setVisibility(View.VISIBLE);
        Glide.with(requireContext())
                .load(imageUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                        binding.progress.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        binding.progress.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(binding.image);
    }
}

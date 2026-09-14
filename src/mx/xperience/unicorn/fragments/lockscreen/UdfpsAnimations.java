/*
    Copyright (C) 2022-2024 crDroid Android Project
    Copyright (C) 2011-2025 The XPerience Project
    SPDX-License-Identifier: Apache-2.0
*/

package mx.xperience.unicorn.fragments.lockscreen;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;

public class UdfpsAnimations extends Fragment {

    private static final String TAG = "UdfpsAnimations";
    private static final String UDFPS_RESOURCES_PACKAGE = "mx.xperience.udfps.animations";

    private RecyclerView mRecyclerView;
    private Resources mUdfpsResources;
    private String[] mAnimations;
    private String[] mAnimationPreviews;
    private String[] mTitles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().setTitle(R.string.themes_udfps_animation_title);
        loadResources();
    }

    private void loadResources() {
        try {
            PackageManager pm = requireActivity().getPackageManager();
            mUdfpsResources = pm.getResourcesForApplication(UDFPS_RESOURCES_PACKAGE);

            int animationsId = mUdfpsResources.getIdentifier(
                    "udfps_animation_styles", "array", UDFPS_RESOURCES_PACKAGE);
            int previewsId = mUdfpsResources.getIdentifier(
                    "udfps_animation_previews", "array", UDFPS_RESOURCES_PACKAGE);
            int titlesId = mUdfpsResources.getIdentifier(
                    "udfps_animation_titles", "array", UDFPS_RESOURCES_PACKAGE);

            if (animationsId == 0 || previewsId == 0 || titlesId == 0) {
                throw new Resources.NotFoundException("Missing UDFPS animation resources");
            }

            mAnimations = mUdfpsResources.getStringArray(animationsId);
            mAnimationPreviews = mUdfpsResources.getStringArray(previewsId);
            mTitles = mUdfpsResources.getStringArray(titlesId);
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            Log.w(TAG, "Unable to load UDFPS animation resources", e);
            mUdfpsResources = null;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        if (mUdfpsResources == null) {
            Toast.makeText(getContext(), "Animation resources not found.", Toast.LENGTH_LONG).show();
            return new View(requireContext());
        }

        View view = inflater.inflate(R.layout.udfps_picker, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        mRecyclerView.setAdapter(new UdfpsAnimationAdapter(requireContext()));
        return view;
    }

    private class UdfpsAnimationAdapter
            extends RecyclerView.Adapter<UdfpsAnimationAdapter.UdfpsAnimationViewHolder> {

        private final Context mContext;
        private int mSelectedPosition;

        UdfpsAnimationAdapter(Context context) {
            mContext = context;
            mSelectedPosition = Settings.System.getIntForUser(
                    context.getContentResolver(),
                    Settings.System.UDFPS_ANIM_STYLE,
                    0,
                    UserHandle.USER_CURRENT);

            if (mSelectedPosition < 0 || mSelectedPosition >= getItemCount()) {
                mSelectedPosition = 0;
            }
        }

        @NonNull
        @Override
        public UdfpsAnimationViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.udfps_animation_option, parent, false);
            return new UdfpsAnimationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(
                @NonNull UdfpsAnimationViewHolder holder, int position) {
            holder.itemView.setActivated(position == mSelectedPosition);
            holder.name.setText(position < mTitles.length ? mTitles[position] : "");

            Drawable current = holder.image.getDrawable();
            if (current instanceof AnimationDrawable) {
                ((AnimationDrawable) current).stop();
            }

            Drawable preview = position < mAnimationPreviews.length
                    ? getDrawable(holder.image.getContext(), mAnimationPreviews[position])
                    : null;
            holder.image.setImageDrawable(preview);

            holder.itemView.setOnClickListener(v -> selectAnimation(holder));
        }

        private void selectAnimation(UdfpsAnimationViewHolder holder) {
            int position = holder.getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }

            int oldPosition = mSelectedPosition;
            mSelectedPosition = position;

            if (oldPosition != position) {
                notifyItemChanged(oldPosition);
                notifyItemChanged(position);
            } else {
                holder.itemView.setActivated(true);
            }

            Settings.System.putIntForUser(
                    mContext.getContentResolver(),
                    Settings.System.UDFPS_ANIM_STYLE,
                    position,
                    UserHandle.USER_CURRENT);

            // Preview the actual animation once when the user selects it.
            Drawable current = holder.image.getDrawable();
            if (current instanceof AnimationDrawable) {
                ((AnimationDrawable) current).stop();
            }

            Drawable drawable = getDrawable(mContext, mAnimations[position]);
            holder.image.setImageDrawable(drawable);
            if (drawable instanceof AnimationDrawable) {
                AnimationDrawable animation = (AnimationDrawable) drawable;
                animation.setOneShot(true);
                holder.image.post(animation::start);
            }
        }

        @Override
        public int getItemCount() {
            return mAnimations == null ? 0 : mAnimations.length;
        }

        class UdfpsAnimationViewHolder extends RecyclerView.ViewHolder {
            final TextView name;
            final ImageView image;

            UdfpsAnimationViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.option_label);
                image = itemView.findViewById(R.id.option_thumbnail);
            }
        }
    }

    @Nullable
    private Drawable getDrawable(Context context, String drawableName) {
        if (drawableName == null || mUdfpsResources == null) {
            return null;
        }

        int resId = mUdfpsResources.getIdentifier(
                drawableName, "drawable", UDFPS_RESOURCES_PACKAGE);
        if (resId == 0) {
            Log.w(TAG, "Drawable not found: " + drawableName);
            return null;
        }

        try {
            Context packageContext = context.createPackageContext(
                    UDFPS_RESOURCES_PACKAGE, Context.CONTEXT_IGNORE_SECURITY);
            return packageContext.getDrawable(resId);
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            Log.w(TAG, "Unable to load drawable: " + drawableName, e);
            return null;
        }
    }
}

/*
    Copyright (C) 2022-2024 crDroid Android Project
    Copyright (C) 2011-2025 The XPerience Project
    SPDX-License-Identifier: Apache-2.0
*/

package mx.xperience.unicorn.fragments.lockscreen;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;

public class UdfpsIcons extends Fragment {

    private static final String TAG = "UdfpsIcons";
    private static final String UDFPS_RESOURCES_PACKAGE = "mx.xperience.udfps.animations";

    private RecyclerView mRecyclerView;
    private Resources mUdfpsResources;
    private String[] mIcons;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().setTitle(R.string.themes_udfps_icon_title);
        loadResources();
    }

    private void loadResources() {
        try {
            PackageManager pm = requireActivity().getPackageManager();
            mUdfpsResources = pm.getResourcesForApplication(UDFPS_RESOURCES_PACKAGE);
            int iconsId = mUdfpsResources.getIdentifier(
                    "udfps_icons", "array", UDFPS_RESOURCES_PACKAGE);
            if (iconsId == 0) {
                throw new Resources.NotFoundException("Missing UDFPS icon resources");
            }
            mIcons = mUdfpsResources.getStringArray(iconsId);
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            Log.w(TAG, "Unable to load UDFPS icon resources", e);
            mUdfpsResources = null;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        if (mUdfpsResources == null) {
            Toast.makeText(getContext(), "UDFPS icon resources not found.", Toast.LENGTH_LONG).show();
            return new View(requireContext());
        }

        View view = inflater.inflate(R.layout.udfps_picker, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        mRecyclerView.setAdapter(new UdfpsIconAdapter(requireContext()));
        return view;
    }

    private class UdfpsIconAdapter
            extends RecyclerView.Adapter<UdfpsIconAdapter.UdfpsIconViewHolder> {

        private final Context mContext;
        private int mSelectedPosition;

        UdfpsIconAdapter(Context context) {
            mContext = context;
            mSelectedPosition = Settings.System.getIntForUser(
                    context.getContentResolver(),
                    Settings.System.UDFPS_ICON,
                    0,
                    UserHandle.USER_CURRENT);

            if (mSelectedPosition < 0 || mSelectedPosition >= getItemCount()) {
                mSelectedPosition = 0;
            }
        }

        @NonNull
        @Override
        public UdfpsIconViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.udfps_icon_option, parent, false);
            return new UdfpsIconViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UdfpsIconViewHolder holder, int position) {
            holder.itemView.setActivated(position == mSelectedPosition);
            holder.image.setImageDrawable(getDrawable(mContext, mIcons[position]));
            holder.itemView.setOnClickListener(v -> selectIcon(holder));
        }

        private void selectIcon(UdfpsIconViewHolder holder) {
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
                    Settings.System.UDFPS_ICON,
                    position,
                    UserHandle.USER_CURRENT);
        }

        @Override
        public int getItemCount() {
            return mIcons == null ? 0 : mIcons.length;
        }

        class UdfpsIconViewHolder extends RecyclerView.ViewHolder {
            final ImageView image;

            UdfpsIconViewHolder(View itemView) {
                super(itemView);
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

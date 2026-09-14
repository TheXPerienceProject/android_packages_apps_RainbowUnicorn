/*
    Copyright (C) 2022-2024 crDroid Android Project
    Copyright (C) 2011-2025 The XPerience Project
    SPDX-License-Identifier: Apache-2.0
*/

package mx.xperience.unicorn.fragments.lockscreen;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsActivity;

import java.util.Arrays;

import androidx.fragment.app.Fragment;
import android.widget.Toast;
// The class extends from a normal Fragment to have full control over the layout.
public class UdfpsAnimations extends Fragment {

    private RecyclerView mRecyclerView;
    private String mPkg = "mx.xperience.udfps.animations";
    private AnimationDrawable animation;

    private Resources udfpsRes;

    private String[] mAnims;
    private String[] mAnimPreviews;
    private String[] mTitles;

    private UdfpsAnimAdapter mUdfpsAnimAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.themes_udfps_animation_title);

        loadResources();
    }

    private void loadResources() {
        try {
            PackageManager pm = requireActivity().getPackageManager();
            udfpsRes = pm.getResourcesForApplication(mPkg);
            // If the resources are obtained, then we load them.
            mAnims = udfpsRes.getStringArray(udfpsRes.getIdentifier("udfps_animation_styles",
                    "array", mPkg));
            mAnimPreviews = udfpsRes.getStringArray(udfpsRes.getIdentifier("udfps_animation_previews",
                    "array", mPkg));
            mTitles = udfpsRes.getStringArray(udfpsRes.getIdentifier("udfps_animation_titles",
                    "array", mPkg));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            udfpsRes = null; // We ensure that udfpsRes is null if the packet is not found.
            // It is not necessary to throw an exception, as the code is already prepared to handle a null resource.
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        // If the resources did not load, there is nothing to display.
        if (udfpsRes == null) {
            Toast.makeText(getContext(), "Animation resources not found.", Toast.LENGTH_LONG).show();
            // We return an empty view to avoid a crash.
            return new View(getContext());
        }

        View view = inflater.inflate(R.layout.item_view, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mUdfpsAnimAdapter = new UdfpsAnimAdapter(getActivity());
        mRecyclerView.setAdapter(mUdfpsAnimAdapter);

        return view;
    }

    public class UdfpsAnimAdapter extends RecyclerView.Adapter<UdfpsAnimAdapter.UdfpsAnimViewHolder> {
        Context context;
        String mSelectedAnim;
        String mAppliedAnim;

        public UdfpsAnimAdapter(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public UdfpsAnimViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_option, parent, false);
            return new UdfpsAnimViewHolder(v);
        }

        @Override
        public void onBindViewHolder(UdfpsAnimViewHolder holder, final int position) {
            String animName = mAnims[position];

            Glide.with(holder.image.getContext())
                    .load("")
                    .placeholder(getDrawable(holder.image.getContext(), mAnimPreviews[position]))
                    .into(holder.image);

            holder.name.setText(mTitles[position]);

            if (position == Settings.System.getInt(context.getContentResolver(),
                Settings.System.UDFPS_ANIM_STYLE, 0)) {
                mAppliedAnim = animName;
                if (mSelectedAnim == null) {
                    mSelectedAnim = animName;
                }
            }

            holder.itemView.setActivated(animName == mSelectedAnim);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateActivatedStatus(mSelectedAnim, false);
                    updateActivatedStatus(animName, true);
                    mSelectedAnim = animName;
                    holder.image.setBackgroundDrawable(getDrawable(v.getContext(), mAnims[position]));
                    animation = (AnimationDrawable) holder.image.getBackground();
                    animation.setOneShot(true);
                    animation.start();
                    Settings.System.putInt(getActivity().getContentResolver(),
                            Settings.System.UDFPS_ANIM_STYLE, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            if (mAnims == null) return 0;
            return mAnims.length;
        }

        public class UdfpsAnimViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            ImageView image;
            public UdfpsAnimViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.option_label);
                image = (ImageView) itemView.findViewById(R.id.option_thumbnail);
            }
        }

        private void updateActivatedStatus(String anim, boolean isActivated) {
            if (mAnims == null || mRecyclerView == null) return;
            int index = Arrays.asList(mAnims).indexOf(anim);
            if (index < 0) {
                return;
            }
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(index);
            if (holder != null && holder.itemView != null) {
                holder.itemView.setActivated(isActivated);
            }
        }
    }

    public Drawable getDrawable(Context context, String drawableName) {
        try {
            PackageManager pm = context.getPackageManager();
            Resources res = pm.getResourcesForApplication(mPkg);
            return res.getDrawable(res.getIdentifier(drawableName, "drawable", mPkg));
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}

package mx.xperience.unicorn.preferences;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PifDataPreference extends Preference {

    private static final String TAG = "PifDataPref";
    private static final String API_URL = "https://kota.klozz.dev/attest/gms_certified_props.json";
    
    private ActivityResultLauncher<Intent> mFilePickerLauncher;
    private ImageButton mDownloadButton;
    private ImageButton mDeleteButton;
    private boolean mIsDownloading = false;
    private TextView mSummaryView;

    public PifDataPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.pref_with_delete_and_download);
    }

    public void setFilePickerLauncher(ActivityResultLauncher<Intent> launcher) {
        this.mFilePickerLauncher = launcher;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        final Context ctx = getContext();
        final ContentResolver cr = ctx.getContentResolver();

        TextView title = (TextView) holder.findViewById(R.id.title);
        mSummaryView = (TextView) holder.findViewById(R.id.summary);
        mDeleteButton = (ImageButton) holder.findViewById(R.id.delete_button);
        mDownloadButton = (ImageButton) holder.findViewById(R.id.download_button);

        title.setText(getTitle());

        updateSummary();

        holder.itemView.setOnClickListener(v -> {
            if (mFilePickerLauncher != null) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/json", "text/json"});
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mFilePickerLauncher.launch(intent);
            }
        });

        mDeleteButton.setOnClickListener(v -> {
            if (!callChangeListener(Boolean.FALSE)) return;
            Settings.Secure.putString(cr, Settings.Secure.PIF_DATA, null);
            Settings.Secure.putString(cr, Settings.Secure.PIF_DATA_TIMESTAMP, null);
            Toast.makeText(ctx, ctx.getString(R.string.pif_toast_file_cleared), Toast.LENGTH_SHORT).show();
            notifyChanged();
            killPackages();
        });

        // Configure download button
        if (mDownloadButton != null) {
            mDownloadButton.setVisibility(View.VISIBLE);
            mDownloadButton.setOnClickListener(v -> downloadPifFromInternet());
        }
    }

    private void updateSummary() {
        final Context ctx = getContext();
        final ContentResolver cr = ctx.getContentResolver();

        if (mSummaryView == null) {
            return;
        }

        boolean hasData = Settings.Secure.getString(cr, Settings.Secure.PIF_DATA) != null;

        if (hasData) {
            String json = Settings.Secure.getString(cr, Settings.Secure.PIF_DATA);
            String pifTimestamp = Settings.Secure.getString(cr, Settings.Secure.PIF_DATA_TIMESTAMP);
            int propsCount = countPifProps(json);
            String ts = pifTimestamp != null ? pifTimestamp : new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            mSummaryView.setText(ctx.getString(R.string.pif_data_loaded_summary, propsCount, ts));
        } else {
            mSummaryView.setText(ctx.getString(R.string.pif_data_summary));
        }

        if (mDeleteButton != null) {
            mDeleteButton.setVisibility(hasData ? View.VISIBLE : View.GONE);
            mDeleteButton.setEnabled(hasData);
        }

        if (mDownloadButton != null) {
            mDownloadButton.setEnabled(!mIsDownloading);
            mDownloadButton.setAlpha(mIsDownloading ? 0.5f : 1.0f);
        }
    }

    private void downloadPifFromInternet() {
        if (mIsDownloading) {
            return;
        }

        if (!isInternetConnected()) {
            Toast.makeText(getContext(), 
                getContext().getString(R.string.pif_toast_no_internet), Toast.LENGTH_SHORT).show();
            return;
        }

        mIsDownloading = true;
        updateSummary();

        new DownloadPifTask().execute(API_URL);
    }

    private boolean isInternetConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        Network nw = cm.getActiveNetwork();
        if (nw == null) return false;
        NetworkCapabilities actNw = cm.getNetworkCapabilities(nw);
        return actNw != null
                && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                        || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
    }

    private class DownloadPifTask extends AsyncTask<String, Void, String> {
        private boolean mSuccess = false;

        @Override
        protected String doInBackground(String... urls) {
            if (urls.length == 0) {
                return null;
            }

            try {
                URL url = new URI(urls[0]).toURL();
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    urlConnection.setConnectTimeout(10000);
                    urlConnection.setReadTimeout(10000);

                    try (BufferedReader reader =
                            new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        mSuccess = true;
                        return response.toString();
                    }
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error downloading PIF from internet", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            mIsDownloading = false;
            final Context ctx = getContext();
            final ContentResolver cr = ctx.getContentResolver();

            if (mSuccess && result != null && !result.trim().isEmpty()) {
                try {
                    // Validate that it is valid JSON
                    String trimmed = result.trim();
                    if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                        if (!callChangeListener(Boolean.TRUE)) return;
                        Settings.Secure.putString(cr, Settings.Secure.PIF_DATA, result);
                        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                        Settings.Secure.putString(cr, Settings.Secure.PIF_DATA_TIMESTAMP, timestamp);
                        
                        Toast.makeText(ctx, 
                            ctx.getString(R.string.pif_toast_download_success), Toast.LENGTH_SHORT).show();
                        notifyChanged();
                        killPackages();
                    } else {
                        Toast.makeText(ctx, 
                            ctx.getString(R.string.pif_toast_invalid_json), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing downloaded PIF", e);
                    Toast.makeText(ctx, 
                        ctx.getString(R.string.pif_toast_download_error), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ctx, 
                    ctx.getString(R.string.pif_toast_download_error), Toast.LENGTH_SHORT).show();
            }
            
            updateSummary();
        }
    }

    public void handleFileSelected(Uri uri) {
        final Context ctx = getContext();
        final ContentResolver cr = ctx.getContentResolver();

        if (uri == null) {
            Toast.makeText(ctx,
                ctx.getString(R.string.pif_toast_invalid_file_selected), Toast.LENGTH_SHORT).show();
            return;
        }

        final String type = cr.getType(uri);
        boolean isJsonMime = "application/json".equals(type) || "text/json".equals(type);
        boolean hasJsonExt = (uri.getPath() != null && uri.getPath().toLowerCase().endsWith(".json"));
        if (!isJsonMime && !hasJsonExt) {
            Toast.makeText(ctx,
                ctx.getString(R.string.pif_toast_invalid_file_selected), Toast.LENGTH_SHORT).show();
            return;
        }

        try (InputStream inputStream = cr.openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8))) {

            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line).append('\n');
            }

            String json = jsonContent.toString();

            if (!callChangeListener(Boolean.TRUE)) return;
            Settings.Secure.putString(cr, Settings.Secure.PIF_DATA, json);
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            Settings.Secure.putString(cr, Settings.Secure.PIF_DATA_TIMESTAMP, timestamp);
            Toast.makeText(ctx,
                    ctx.getString(R.string.pif_toast_file_loaded), Toast.LENGTH_SHORT).show();
            notifyChanged();
            killPackages();
        } catch (IOException e) {
            Log.e(TAG, "Failed to read JSON file", e);
            Toast.makeText(ctx,
                ctx.getString(R.string.pif_toast_invalid_file_selected), Toast.LENGTH_SHORT).show();
        }
    }

    private int countPifProps(String json) {
        if (json == null || json.trim().isEmpty()) return 0;
        try {
            String trimmed = json.trim();
            if (trimmed.startsWith("{")) {
                JSONObject obj = new JSONObject(trimmed);
                // Prefer nested "props" object if present
                if (obj.has("props") && obj.opt("props") instanceof JSONObject) {
                    return ((JSONObject) obj.get("props")).length();
                }
                return obj.length();
            } else if (trimmed.startsWith("[")) {
                JSONArray arr = new JSONArray(trimmed);
                return arr.length();
            }
        } catch (JSONException ignore) {
        }
        return 0;
    }

    private void killPackages() {
        try {
            ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
            String[] packages = { "com.google.android.gms", "com.android.vending" };
            for (String pkg : packages) {
                am.getClass()
                  .getMethod("forceStopPackage", String.class)
                  .invoke(am, pkg);
                Log.i(TAG, pkg + " process killed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to kill packages", e);
        }
    }
}
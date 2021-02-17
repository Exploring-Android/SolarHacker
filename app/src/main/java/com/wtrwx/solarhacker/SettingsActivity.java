package com.wtrwx.solarhacker;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.wtrwx.solarhacker.utils.AppInfo;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        checkActivation();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SharedPreferences pref = this.createDeviceProtectedStorageContext()
                    .getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences.xml", MODE_PRIVATE);
        }
        setWorldReadable();
    }

    private void checkActivation() {
        AppInfo appInfo = AppInfo.getInstance();
        boolean isActive = appInfo.isXposedActive();

        if (!isActive) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle(R.string.dialog_activation_check_title)
                    .setMessage(R.string.dialog_activation_check_message)
                    .setNegativeButton(R.string.dialog_activation_check_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            builder.create().show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_apply:
                killSolarToApply();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        setWorldReadable();
    }

    private void killSolarToApply() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.killBackgroundProcesses("com.fenbi.android.solar");
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    @SuppressLint({"SetWorldReadable", "WorldReadableFiles"})
    private void setWorldReadable() {
        File dataDir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dataDir = new File(getApplicationInfo().deviceProtectedDataDir);
        } else {
            dataDir = new File(getApplicationInfo().dataDir);
        }

        File prefsDir = new File(dataDir, "shared_prefs");
        File prefsFile = new File(prefsDir, BuildConfig.APPLICATION_ID + "_preferences.xml");
        if (prefsFile.exists()) {
            for (File file : new File[]{dataDir, prefsDir, prefsFile}) {
                file.setReadable(true, false);
                file.setExecutable(true, false);
            }
        }
    }
}
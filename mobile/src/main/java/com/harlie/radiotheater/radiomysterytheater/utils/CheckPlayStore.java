package com.harlie.radiotheater.radiomysterytheater.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.harlie.radiotheater.radiomysterytheater.BaseActivity;
import com.harlie.radiotheater.radiomysterytheater.R;


public class CheckPlayStore {
    private final static String TAG = "LEE: <" + CheckPlayStore.class.getSimpleName() + ">";

    //noinspection ConstantConditions
    @SuppressWarnings("UnusedReturnValue")
    public static void upgradeToPaid(final BaseActivity activity) {

        //#IFDEF 'PAID'
        //Log.v(TAG, "already using PAID version.");
        //#ENDIF

        //#IFDEF 'TRIAL'
        if (activity.isPurchased() != true) {
            Log.v(TAG, "using TRIAL version.");
            String packageId = activity.getApplicationContext().getPackageName();
            packageId = packageId.replace(".radiomysterytheater", ".radiomysterytheater.paid");
            Log.v(TAG, "---> upgrade packageId="+packageId);
            try { // from: http://stackoverflow.com/questions/3239478/how-to-link-to-android-market-app
                String upgradeLink = "http://market.android.com/details?id=" + packageId;
                if (CheckPlayStore.isGooglePlayInstalled(activity.getApplicationContext())) {
                    upgradeLink = "market://details?id=" + packageId;
                }
                String message = activity.getResources().getString(R.string.playstore_upgrade);
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                Log.v(TAG, "---> upgrade Link=" + upgradeLink);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(upgradeLink));
                activity.startActivity(intent); //FIXME: log 'upgrade' web-click to Firebase
                activity.overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
            }
            catch (ActivityNotFoundException e) {
                noticeAppNotAvailableOnPlaystore(packageId, activity);
            }
        }
        //#ENDIF

    }

    public static void noticeAppNotAvailableOnPlaystore(String packageId, BaseActivity activity) {
        Log.e(TAG, "APP id='"+packageId+"' NOT FOUND ON PLAYSTORE!");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle(activity.getResources().getString(R.string.app_not_found));
        alertDialogBuilder
                .setMessage(activity.getResources().getString(R.string.not_on_playstore))
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        Log.v(TAG, "click!");
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        //FIXME: log 'upgrade' failure to Firebase
    }

    // from: http://stackoverflow.com/questions/15401748/how-to-detect-if-google-play-is-installed-not-market
    public static boolean isGooglePlayInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed;
        try
        {
            PackageInfo info = pm.getPackageInfo("com.android.vending", PackageManager.GET_ACTIVITIES);
            String label = (String) info.applicationInfo.loadLabel(pm);
            app_installed = (label != null && ! label.equals("Market"));
        }
        catch (PackageManager.NameNotFoundException e)
        {
            app_installed = false;
        }
        Log.v(TAG, "isGooglePlayInstalled=" + app_installed);
        return app_installed;
    }

}

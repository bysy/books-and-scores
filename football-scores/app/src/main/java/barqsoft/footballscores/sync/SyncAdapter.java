package barqsoft.footballscores.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import barqsoft.footballscores.R;

/**
 * Provide sync adapter. Modified from
 * http://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    // Used for widgets' intent-filter, must be same as in manifest
    public static final String ACTION_DATA_UPDATED = "barqsoft.footballscores.ACTION_DATA_UPDATED";

    private static final int HOUR_IN_SEC = 60 * 60;
    private static final int SYNC_INTERVAL = 3 * HOUR_IN_SEC;
    private static final int SYNC_FLEXTIME = 2 * HOUR_IN_SEC;
    private static final String TAG = SyncAdapter.class.getSimpleName();
    private static final String TEAMS_TIMESTAMP_KEY = "TEAMS_TIMESTAMP_KEY";
    private static final long TEAM_UPDATE_PERIOD_MILLIS =
            TimeUnit.MILLISECONDS.convert(60, TimeUnit.DAYS);
    private static final String LOG_TAG = SyncAdapter.class.getSimpleName();
    ContentResolver mContentResolver;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
            super(context, autoInitialize, allowParallelSyncs);

        mContentResolver = context.getContentResolver();
    }

    public static void initialize(Context context) {
        findOrCreateSyncAccount(context);
    }

    /**
     * Update teams from API if has been `TEAM_UPDATE_PERIOD_MILLIS` or longer
     * since the last update. Also does the initial pull if needed.
     */
    private static void updateTeamsMaybe(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final long lastUpdate = prefs.getLong(TEAMS_TIMESTAMP_KEY, 0);
        Log.d(LOG_TAG, "Last team update was at " + lastUpdate + " => " + getTimestamp(lastUpdate));
        final long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdate > TEAM_UPDATE_PERIOD_MILLIS) {
            if (FootballDataHelper.tryFetchTeams(context)) {
                prefs.edit().putLong(TEAMS_TIMESTAMP_KEY, currentTime).apply();
                Log.d(LOG_TAG, "Team update successful");
            } else {
                Log.w(LOG_TAG, "Team update failed");
            }
        }
    }

    private static String getTimestamp(long timeInMillis) {
        final GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(timeInMillis);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 'T' HH:mm:ss");
        sdf.setCalendar(gc);
        return sdf.format(gc.getTime());
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "Syncing data");
        final Context applicationContext = getContext().getApplicationContext();
        updateTeamsMaybe(applicationContext);
        FootballDataHelper.updateData(applicationContext);
        informWidgets();
    }

    private void informWidgets() {
        final Context context = getContext();
        context.sendBroadcast(
                new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName()));
    }

    // Helper methods adapted from Sunshine below

    public static Account findOrCreateSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            initializeAccount(context, newAccount);
        }
        return newAccount;
    }

    private static void initializeAccount(Context context, Account syncAccount) {
        configurePeriodicSync(context, syncAccount, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(syncAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    private static void configurePeriodicSync(Context context, Account account, int syncInterval, int flexTime) {
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(findOrCreateSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }
}

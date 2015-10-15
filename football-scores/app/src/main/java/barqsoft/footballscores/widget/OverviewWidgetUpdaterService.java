package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;

/**
 * Created by Benjamin on 10/11/15.
 */
public class OverviewWidgetUpdaterService extends IntentService {
    private static final String[] PROJECTION = { DatabaseContract.scores_table.HOME_GOALS_COL };
    private static final int COL_IX_HOME_GOALS = 0;  // to check if the game has a score already

    public OverviewWidgetUpdaterService() {
        super(OverviewWidgetUpdaterService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Find today's game stats
        final Date date = new Date(System.currentTimeMillis());
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final String selection = null;
        final String[] selectionArgs = {dateFormat.format(date)};
        final String sortOrder = null;
        Cursor cursor = getContentResolver().query(
                DatabaseContract.scores_table.DATE_URI,
                PROJECTION,
                selection,
                selectionArgs,
                sortOrder);
        int countAll = 0;
        int countWithScore = 0;
        if (cursor!=null) {
            try {
                countAll = cursor.getCount();
                if (countAll>0) {
                    cursor.moveToFirst();
                    do {
                        if (cursor.getInt(COL_IX_HOME_GOALS)>=0) {
                            countWithScore += 1;
                        }
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        // Update widgets
        final AppWidgetManager awm = AppWidgetManager.getInstance(this);
        final int[] widgetIds = awm.getAppWidgetIds(
                new ComponentName(this, OverviewWidgetProvider.class));
        final Intent in = new Intent(this, MainActivity.class);
        final PendingIntent pi = PendingIntent.getActivity(this, 0, in, 0);

        final RemoteViews rvs = new RemoteViews(getPackageName(), R.layout.widget_overview);
        rvs.setTextViewText(R.id.matches_today_textview, String.valueOf(countAll));
        rvs.setTextViewText(R.id.matches_finished_textview, String.valueOf(countWithScore));
        rvs.setOnClickPendingIntent(R.id.widget, pi);

        // TODO Content description
        awm.updateAppWidget(widgetIds, rvs);
    }
}

package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import barqsoft.footballscores.sync.SyncAdapter;

/**
 * Created by Benjamin on 10/11/15.
 */
public class OverviewWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        update(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SyncAdapter.ACTION_DATA_UPDATED)) {
            update(context);
        } else {
            super.onReceive(context, intent);
        }
    }

    private void update(Context context) {
        Log.d(OverviewWidgetProvider.class.getSimpleName(), "updating summary widget");
        context.startService(new Intent(context, OverviewWidgetUpdaterService.class));
    }
}

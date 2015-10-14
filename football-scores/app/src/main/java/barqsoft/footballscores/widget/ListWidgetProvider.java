package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import barqsoft.footballscores.R;
import barqsoft.footballscores.sync.SyncAdapter;

/**
 * Provide list widget.
 * Created by Benjamin on 10/14/15.
 */
public class ListWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // update widgets
        for (int awId : appWidgetIds) {
            final RemoteViews rvs = new RemoteViews(context.getPackageName(), R.layout.widget_list);
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
                rvs.setEmptyView(R.id.list_widget_listview, R.id.list_widget_empty_view);
            }
            final Intent widgetAdapterIntent = new Intent(context, ListWidgetRemoteViewsService.class);
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                rvs.setRemoteAdapter(R.id.list_widget_listview, widgetAdapterIntent);
            } else if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
                //noinspection deprecation since non-deprecated method isn't available on these versions
                rvs.setRemoteAdapter(awId, R.id.list_widget_listview, widgetAdapterIntent);
            }
            appWidgetManager.updateAppWidget(awId, rvs);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("List widget", "received intent " + intent);
        if (intent.getAction().equals(SyncAdapter.ACTION_DATA_UPDATED)) {
            Log.d("List widget", "updating widget after ACTION_DATA_UPDATED received");
            final AppWidgetManager awm = AppWidgetManager.getInstance(context);
            final int[] awIds = awm.getAppWidgetIds(new ComponentName(context, getClass()));
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
                awm.notifyAppWidgetViewDataChanged(awIds, R.id.list_widget_listview);
            }
        } else {
            super.onReceive(context, intent);
        }
    }
}

package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.DatabaseContract.scores_table;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Util;


/**
 * Create remote views for collection widget.
 * Created by Benjamin on 10/14/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ListWidgetRemoteViewsService extends RemoteViewsService {
    static final String[] PROJECTION = {
            scores_table._ID,
            scores_table.TIME_COL,
            scores_table.HOME_COL,
            scores_table.AWAY_COL,
            scores_table.HOME_GOALS_COL,
            scores_table.AWAY_GOALS_COL,
            scores_table.MATCH_ID };
    // indices tied to projection
    static final int COL_TIME = 1;
    static final int COL_HOME_NAME = 2;
    static final int COL_AWAY_NAME = 3;
    static final int COL_HOME_GOALS = 4;
    static final int COL_AWAY_GOALS = 5;
    static final int COL_MATCH_ID = 6;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory(getApplicationContext(), intent);
    }

    class RemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        Cursor mCursorMaybe;

        public RemoteViewsFactory(Context context, Intent intent) { }

        @Override
        public void onCreate() { }

        @Override
        public void onDataSetChanged() {
            if (mCursorMaybe!=null) {
                mCursorMaybe.close();
            }
            final long identityToken = Binder.clearCallingIdentity();
            try {
                final Date date = new Date(System.currentTimeMillis());
                final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                final Uri uri = scores_table.DATE_URI;
                final String sel = null;
                final String[] selarg = new String[]{dateFormat.format(date)};
                final String sort = null;
                mCursorMaybe = getContentResolver().query(uri, PROJECTION, sel, selarg, sort);
            } finally {
                Binder.restoreCallingIdentity(identityToken);
            }
        }

        @Override
        public void onDestroy() {
            if (mCursorMaybe!=null) {
                mCursorMaybe.close();
            }
        }

        @Override
        public int getCount() {
            return mCursorMaybe==null ? 0 : mCursorMaybe.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (mCursorMaybe==null || !mCursorMaybe.moveToPosition(position)) {
                return null;
            }
            final Cursor cur = mCursorMaybe;
            final RemoteViews rvs = new RemoteViews(getPackageName(), R.layout.widget_list_item);
            rvs.setTextViewText(R.id.home_name, cur.getString(COL_HOME_NAME));
            rvs.setTextViewText(R.id.away_name, cur.getString(COL_AWAY_NAME));
            final int homeGoals = cur.getInt(COL_HOME_GOALS);
            final int awayGoals = cur.getInt(COL_AWAY_GOALS);
            String scoreOrTime;
            if (homeGoals>=0 && awayGoals>=0) {  // in progress or finished
                scoreOrTime = Util.formatScore(homeGoals, awayGoals);
            } else {  // hasn't started yet
                scoreOrTime = getString(R.string.at_time_format, cur.getString(COL_TIME));
            }
            rvs.setTextViewText(R.id.score_or_time, scoreOrTime);
            return rvs;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}

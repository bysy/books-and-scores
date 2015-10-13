package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import barqsoft.footballscores.DatabaseContract.scores_table;

import static barqsoft.footballscores.DatabaseContract.getMatchStatus;


/**
 * Created by yehya khaled on 2/26/2015.
 */
public class ScoresAdapter extends CursorAdapter {
    /** Projection to be used with this adapter */
    public static final String[] PROJECTION = {
            scores_table._ID,
            scores_table.LEAGUE_COL,
            scores_table.TIME_COL,
            scores_table.HOME_COL,
            scores_table.AWAY_COL,
            scores_table.HOME_GOALS_COL,
            scores_table.AWAY_GOALS_COL,
            scores_table.MATCH_ID,
            scores_table.MATCH_DAY,
            scores_table.STATUS_COL };
    // Indices tied to projection:
    public static final int COL_LEAGUE = 1;
    public static final int COL_TIME = 2;
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 5;
    public static final int COL_AWAY_GOALS = 6;
    public static final int COL_MATCH_ID = 7;
    public static final int COL_MATCHDAY = 8;
    public static final int COL_STATUS = 9;

    private static final String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";

    /** Return a cursor loader suitable for this adapter */
    public static CursorLoader newCursorLoader(Context context, String date) {
        return new CursorLoader(
                context,
                DatabaseContract.scores_table.buildScoreWithDate(),
                ScoresAdapter.PROJECTION,
                null,
                new String[] {date},
                null);
    }

    public ScoresAdapter(Context context, Cursor cursor, int flags) {
        super(context,cursor,flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);
        //Log.v(FetchScoreTask.LOG_TAG,"new View inflated");
        return mItem;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder vh = (ViewHolder) view.getTag();
        vh.home_name.setText(cursor.getString(COL_HOME));
        vh.away_name.setText(cursor.getString(COL_AWAY));
        vh.time.setText(cursor.getString(COL_TIME));
        vh.score.setText(Util.formatScore(cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS)));
        vh.match_id = cursor.getLong(COL_MATCH_ID);
        vh.home_crest.setImageResource(Util.getTeamCrestByTeamName(
                cursor.getString(COL_HOME)));
        vh.away_crest.setImageResource(Util.getTeamCrestByTeamName(
                cursor.getString(COL_AWAY)
        ));
        //Log.v(FetchScoreTask.LOG_TAG,vh.home_name.getText() + " Vs. " + vh.away_name.getText() +" id " + String.valueOf(vh.match_id));
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(detail_match_id));
        final ViewGroup details_root = vh.details_root;
        if(vh.match_id == MainActivity.selected_match_id) {
            TextView match_day = (TextView) details_root.findViewById(R.id.matchday_textview);
            match_day.setText(Util.formatMatchDay(context,
                    cursor.getInt(COL_MATCHDAY), cursor.getInt(COL_LEAGUE)));
            TextView league = (TextView) details_root.findViewById(R.id.league_textview);
            league.setText(Util.getLeague(context, cursor.getInt(COL_LEAGUE)));
            TextView status = (TextView) details_root.findViewById(R.id.status_textview);
            status.setText(Util.getStatusString(context, getMatchStatus(cursor, COL_STATUS)));
            Button share_button = (Button) details_root.findViewById(R.id.share_button);
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(createShareMatchIntent(vh));
                }
            });
            ImageButton close_button = (ImageButton) details_root.findViewById(R.id.close_details_button);
            close_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.selected_match_id = MainActivity.INVALID_MATCH_ID;
                    notifyDataSetChanged();
                }
            });
            details_root.setVisibility(View.VISIBLE);
        } else {
            details_root.setVisibility(View.GONE);
        }
    }

    public Intent createShareMatchIntent(ViewHolder vh) {
        String matchText =
                vh.home_name.getText() + " " + vh.score.getText() + " " + vh.away_name.getText();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, matchText + " " + FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }

    static public class ViewHolder {
        public TextView home_name;
        public TextView away_name;
        public TextView score;
        public TextView time;
        public ImageView home_crest;
        public ImageView away_crest;
        public ViewGroup details_root;
        public long match_id;

        public ViewHolder(View view) {
            home_name = (TextView) view.findViewById(R.id.home_name);
            away_name = (TextView) view.findViewById(R.id.away_name);
            score     = (TextView) view.findViewById(R.id.score_textview);
            time = (TextView) view.findViewById(R.id.time_textview);
            home_crest = (ImageView) view.findViewById(R.id.home_crest);
            away_crest = (ImageView) view.findViewById(R.id.away_crest);
            details_root = (ViewGroup) view.findViewById(R.id.details_fragment_container);
        }
    }
}

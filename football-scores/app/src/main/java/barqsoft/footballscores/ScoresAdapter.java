package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import barqsoft.footballscores.DatabaseContract.scores_table;

import static barqsoft.footballscores.DatabaseContract.getMatchStatus;


/**
 * Created by yehya khaled on 2/26/2015.
 */
public class ScoresAdapter extends CursorAdapter {
    private static final String LOG_TAG = ScoresAdapter.class.getSimpleName();

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
            scores_table.STATUS_COL,
            scores_table.HOME_ID_COL,
            scores_table.AWAY_ID_COL };
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
    public static final int COL_HOME_ID = 10;
    public static final int COL_AWAY_ID = 11;

    private static final String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";

    // View types
    private static final int VT_CARD = 0;
    private static final int VT_CARD_WITH_TIME = 1;

    /** Return a cursor loader suitable for this adapter */
    public static CursorLoader newCursorLoader(Context context, String date) {
        return new CursorLoader(
                context,
                scores_table.DATE_URI,
                ScoresAdapter.PROJECTION,
                null,
                new String[] {date},
                null);
    }

    public ScoresAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final boolean hasHeader = shouldShowTime(cursor);
        final int layout = hasHeader ?
                R.layout.scores_list_item_with_time : R.layout.scores_list_item;
        View view = LayoutInflater.from(context).inflate(layout, parent, false);
        view.setTag(new ViewHolder(view, hasHeader));
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        return shouldShowTime(cursor) ? VT_CARD_WITH_TIME : VT_CARD;
    }

    /**
     * Return true for first card of each group
     * of games that start at the same time.
     */
    private boolean shouldShowTime(Cursor cursor) {
        if (cursor.getPosition()==0) {
            return true;
        }
        final String time = cursor.getString(COL_TIME);
        cursor.moveToPrevious();
        final String prevTime = cursor.getString(COL_TIME);
        cursor.moveToNext();
        return !time.equals(prevTime);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder vh = (ViewHolder) view.getTag();
        final String homeName = cursor.getString(COL_HOME);
        final String awayName = cursor.getString(COL_AWAY);
        vh.home_name.setText(homeName);
        vh.away_name.setText(awayName);

        final String time = cursor.getString(COL_TIME);

        if (vh.maybe_time_header!=null) {
            vh.maybe_time_header.setText(time);
        }
        final int homeGoals = cursor.getInt(COL_HOME_GOALS);
        final int awayGoals = cursor.getInt(COL_AWAY_GOALS);
        final boolean haveScore = homeGoals>=0 && awayGoals>=0;
        String score = "";
        if (haveScore) {
            score = Util.formatScore(homeGoals, awayGoals);
            vh.score.setText(score);
            vh.score.setVisibility(View.VISIBLE);
            vh.versus.setVisibility(View.GONE);
        } else {
            vh.score.setVisibility(View.GONE);
            vh.versus.setVisibility(View.VISIBLE);
        }
        vh.match_id = cursor.getLong(COL_MATCH_ID);
        loadCrestInto(context, vh.home_crest, Util.getTeamCrestUrl(context, cursor.getInt(COL_HOME_ID)));
        loadCrestInto(context, vh.away_crest, Util.getTeamCrestUrl(context, cursor.getInt(COL_AWAY_ID)));

        String league = null;
        String status = null;
        String matchday = null;
        final ViewGroup details_root = vh.details_root;
        final boolean showDetails = vh.match_id==MainActivity.selected_match_id;
        if(showDetails) {
            TextView matchdayView = (TextView) details_root.findViewById(R.id.matchday_textview);
            final int leagueInt = cursor.getInt(COL_LEAGUE);
            matchday = Util.formatMatchDay(context, cursor.getInt(COL_MATCHDAY), leagueInt);
            matchdayView.setText(matchday);
            TextView leagueView = (TextView) details_root.findViewById(R.id.league_textview);
            league = Util.getLeague(context, cursor.getInt(COL_LEAGUE));
            leagueView.setText(league);
            TextView statusView = (TextView) details_root.findViewById(R.id.status_textview);
            status = Util.getStatusString(context, getMatchStatus(cursor, COL_STATUS));
            statusView.setText(status);
            TextView time_in_details = (TextView) view.findViewById(R.id.time_inside_textview);
            time_in_details.setText(time);
            Button share_button = (Button) details_root.findViewById(R.id.share_button);
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(createShareMatchIntent(context, vh, time));
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
        // Content description
        if (!showDetails) {
            if (haveScore) {
                final String contentFormat = context.getString(R.string.format_game_description);
                view.setContentDescription(String.format(contentFormat, homeName, awayName, score));
            } else {
                final String contentFormat = context.getString(R.string.format_future_game_description);
                view.setContentDescription(String.format(contentFormat, homeName, awayName, time));
            }
        } else {
            final String contentFormat = context.getString(R.string.format_game_with_details);
            view.setContentDescription(String.format(contentFormat,
                    homeName, awayName, score, league, matchday, status, time));
        }
    }

    private static void loadCrestInto(Context context, ImageView imageView, String teamCrestUrl) {
        if (teamCrestUrl==null || teamCrestUrl.isEmpty()) {
            Picasso.with(context).load(R.drawable.no_icon).into(imageView);
        } else {
            Log.v(LOG_TAG, "Loading crest from " + teamCrestUrl);
            Picasso.with(context).load(teamCrestUrl).error(R.drawable.no_icon).into(imageView);
        }
    }

    public Intent createShareMatchIntent(Context context, ViewHolder vh, String time) {
        String score = vh.score.getText().toString();
        final boolean haveScore = !score.isEmpty();
        final String versus = context.getString(R.string.versus_abbrev);
        final String matchText = vh.home_name.getText() + " " + versus + " " + vh.away_name.getText()
                        + " " + (haveScore ? score : time);
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
        public TextView versus;
        public TextView maybe_time_header;  // non-null for first card in group
        public ImageView home_crest;
        public ImageView away_crest;
        public ViewGroup details_root;
        public long match_id;

        public ViewHolder(View view, boolean tryFindHeader) {
            home_name = (TextView) view.findViewById(R.id.home_name);
            away_name = (TextView) view.findViewById(R.id.away_name);
            score = (TextView) view.findViewById(R.id.score_textview);
            versus = (TextView) view.findViewById(R.id.versus_textview);
            if (tryFindHeader) {
                maybe_time_header = (TextView) view.findViewById(R.id.time_outside_textview);
            }
            home_crest = (ImageView) view.findViewById(R.id.home_crest);
            away_crest = (ImageView) view.findViewById(R.id.away_crest);
            details_root = (ViewGroup) view.findViewById(R.id.details_fragment_container);
        }
    }
}

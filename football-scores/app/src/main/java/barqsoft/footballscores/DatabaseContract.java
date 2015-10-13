package barqsoft.footballscores;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.IntDef;
import android.util.Log;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class DatabaseContract
{
    public static final String SCORES_TABLE = "scores_table";
    public static final class scores_table implements BaseColumns
    {
        //Table data
        public static final String LEAGUE_COL = "league";
        public static final String DATE_COL = "date";
        public static final String TIME_COL = "time";
        public static final String HOME_COL = "home";
        public static final String AWAY_COL = "away";
        public static final String HOME_GOALS_COL = "home_goals";
        public static final String AWAY_GOALS_COL = "away_goals";
        public static final String MATCH_ID = "match_id";
        public static final String MATCH_DAY = "match_day";
        public static final String STATUS_COL = "match_status";

        //public static Uri SCORES_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH)
                //.build();

        //Types
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;

        // URIs

        public static Uri buildScoreWithLeague()
        {
            return BASE_CONTENT_URI.buildUpon().appendPath("league").build();
        }
        public static Uri buildScoreWithId()
        {
            return BASE_CONTENT_URI.buildUpon().appendPath("id").build();
        }
        public static Uri buildScoreWithDate()
        {
            return BASE_CONTENT_URI.buildUpon().appendPath("date").build();
        }
    }
    //URI data
    public static final String CONTENT_AUTHORITY = "barqsoft.footballscores";
    public static final String PATH = "scores";
    public static Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);

    // ** Status

    // http://api.football-data.org/images/blog/state_diagram_fixture.png
    public static final int STATUS_SCHEDULED = 0;
    public static final int STATUS_TIMED = 1;
    public static final int STATUS_IN_PLAY = 2;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_POSTPONED = 4;
    public static final int STATUS_CANCELLED = 5;

    @IntDef({STATUS_SCHEDULED,STATUS_TIMED,STATUS_IN_PLAY,STATUS_FINISHED,
            STATUS_POSTPONED,STATUS_CANCELLED})
    public @interface MatchStatus {}

    /** Helper function to propagate @MatchStatus information. */
    @MatchStatus
    public static int getMatchStatus(Cursor cursor, int statusColumnIndex) {
        @MatchStatus int status = cursor.getInt(statusColumnIndex);
        return status;
    }

    /** Convert from API status string to integer representation **/
    @MatchStatus
    public static int rawStatusToInt(String status) {
        switch (status) {
            case "SCHEDULED":
                return STATUS_SCHEDULED;
            case "TIMED":
                return STATUS_TIMED;
            case "IN_PLAY":
                return STATUS_IN_PLAY;
            case "FINISHED":
                return STATUS_FINISHED;
            case "POSTPONED":
                return STATUS_POSTPONED;
            case "CANCELED":
            case "CANCELLED":
                return STATUS_CANCELLED;
            default:
                Log.w("DBContract", "unknown status encountered: " + status);
                return STATUS_CANCELLED;
        }
    }
}

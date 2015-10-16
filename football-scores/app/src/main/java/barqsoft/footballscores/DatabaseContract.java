package barqsoft.footballscores;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.IntDef;

/**
 * Define database tables, URIs, and associated types.
 * Created by yehya khaled on 2/25/2015.
 */
public class DatabaseContract  {
    // ** URI data
    public static final String CONTENT_AUTHORITY = "barqsoft.footballscores";
    public static final String SCORES_PATH = "scores";
    public static final String TEAMS_PATH = "teams";
    public static final String SCORES_BY_LEAGUE_PATH = SCORES_PATH + "/league";
    public static final String SCORE_BY_ID_PATH = SCORES_PATH + "/id";
    public static final String SCORES_BY_DATE_PATH = SCORES_PATH + "/date";
    public static Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);

    // ** Status

    // http://api.football-data.org/images/blog/state_diagram_fixture.png
    public static class MatchInfo {
        public static final int STATUS_SCHEDULED = 0;
        public static final int STATUS_TIMED = 1;
        public static final int STATUS_IN_PLAY = 2;
        public static final int STATUS_FINISHED = 3;
        public static final int STATUS_POSTPONED = 4;
        public static final int STATUS_CANCELLED = 5;

        @IntDef({STATUS_SCHEDULED, STATUS_TIMED, STATUS_IN_PLAY, STATUS_FINISHED,
                STATUS_POSTPONED, STATUS_CANCELLED})
        public @interface Status {
        }
    }
    /** Helper function to propagate @MatchInfo.Status information. */
    @MatchInfo.Status
    public static int getMatchStatus(Cursor cursor, int statusColumnIndex) {
        @MatchInfo.Status int status = cursor.getInt(statusColumnIndex);
        return status;
    }

    // ** scores_table

    public static final String SCORES_TABLE = "scores_table";
    public static final class scores_table implements BaseColumns {
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
        public static final String HOME_ID_COL = "home_id";
        public static final String AWAY_ID_COL = "away_id";

        public static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(SCORES_PATH).build();

        //Types
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + SCORES_PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + SCORES_PATH;

        // URIs

        // Note (Benjamin)
        // This app doesn't encode the argument / query in the Uri, perhaps to save some redundant
        // string operations. So when calling query() and friends, pass the argument inside the
        // `selectionArgs` parameter. I replaced the confusing helper methods with Uri constants
        // and changed the paths to use this table`s `CONTENT_URI`.

        // While we might like to implement Uri's with embedded arguments, the app works fine without it.

        public static final Uri LEAGUE_URI = CONTENT_URI.buildUpon().appendPath("league").build();
        public static final Uri MATCH_URI = CONTENT_URI.buildUpon().appendPath("id").build();
        public static final Uri DATE_URI = CONTENT_URI.buildUpon().appendPath("date").build();

        // This is what the helper methods might look like if we did encode the value in the uri.
        /*
        public static Uri buildScoreWithLeague(String league) {
            return LEAGUE_URI.buildUpon().appendPath(league).build();
        }
        public static Uri buildScoreWithId(int matchId) {
            return MATCH_URI.buildUpon().appendPath(String.valueOf(matchId)).build();
        }
        public static Uri buildScoreWithDate(String date) {
            return DATE_URI.buildUpon().appendPath(date).build();
        }*/
    }

    public static final String TEAMS_TABLE = "teams_table";
    public static final class teams_table implements BaseColumns {
        public static final String COL_NAME = "name";
        public static final String COL_SHORT_NAME = "short_name";
        public static final String COL_CREST_URL = "crest_url";
        public static final String COL_API_ID = "api_id";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(TEAMS_PATH).build();

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TEAMS_PATH;
    }
}

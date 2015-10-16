package barqsoft.footballscores;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import barqsoft.footballscores.DatabaseContract.scores_table;
import barqsoft.footballscores.DatabaseContract.teams_table;

/**
 * Create and upgrade database.
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresDBHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "Scores.db";
    private static final int DATABASE_VERSION = 4;

    public ScoresDBHelper(Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        final String createTeamsTable = "CREATE TABLE " + DatabaseContract.TEAMS_TABLE + " ("
                + teams_table._ID + " INTEGER PRIMARY KEY,"
                + teams_table.COL_NAME + " TEXT NOT NULL,"
                + teams_table.COL_SHORT_NAME + " TEXT NOT NULL,"
                + teams_table.COL_CREST_URL + " TEXT NOT NULL,"
                + teams_table.COL_API_ID + " INTEGER NOT NULL,"
                + " UNIQUE (" + teams_table.COL_API_ID + ") ON CONFLICT REPLACE"
                + " );";
        db.execSQL(createTeamsTable);

        final String CreateScoresTable = "CREATE TABLE " + DatabaseContract.SCORES_TABLE + " ("
                + scores_table._ID + " INTEGER PRIMARY KEY,"
                + scores_table.DATE_COL + " TEXT NOT NULL,"
                + scores_table.TIME_COL + " INTEGER NOT NULL,"
                + scores_table.HOME_COL + " TEXT NOT NULL,"
                + scores_table.AWAY_COL + " TEXT NOT NULL,"
                + scores_table.LEAGUE_COL + " INTEGER NOT NULL,"
                + scores_table.HOME_GOALS_COL + " TEXT NOT NULL,"
                + scores_table.AWAY_GOALS_COL + " TEXT NOT NULL,"
                + scores_table.MATCH_ID + " INTEGER NOT NULL,"
                + scores_table.MATCH_DAY + " INTEGER NOT NULL,"
                + scores_table.STATUS_COL + " INTEGER NOT NULL,"
                + scores_table.HOME_ID_COL + " INTEGER NOT NULL,"
                + scores_table.AWAY_ID_COL + " INTEGER NOT NULL,"
                + "FOREIGN KEY (" + scores_table.HOME_ID_COL + ") REFERENCES "
                        + DatabaseContract.TEAMS_TABLE + " (" + teams_table.COL_API_ID + "),"
                + "FOREIGN KEY (" + scores_table.AWAY_ID_COL + ") REFERENCES "
                        + DatabaseContract.TEAMS_TABLE + " (" + teams_table.COL_API_ID + "),"
                + " UNIQUE ("+scores_table.MATCH_ID+") ON CONFLICT REPLACE"
                + " );";
        db.execSQL(CreateScoresTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Remove old values when upgrading.
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.SCORES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.TEAMS_TABLE);
    }
}

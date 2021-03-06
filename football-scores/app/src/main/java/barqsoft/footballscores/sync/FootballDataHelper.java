package barqsoft.footballscores.sync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Patterns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.DatabaseContract.MatchInfo;
import barqsoft.footballscores.DatabaseContract.teams_table;
import barqsoft.footballscores.R;

/**
 * Update content provider with fresh data from football-data.org
 * Created by yehya khaled on 3/2/2015.
 */
public class FootballDataHelper {
    public static final String LOG_TAG = FootballDataHelper.class.getSimpleName();

    // API constants
    private static final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
    private static final String TEAM_LINK = "http://api.football-data.org/alpha/teams/";
    private static final String FIXTURES = "fixtures";
    private static final String LINKS = "_links";
    private static final String SELF = "self";

    // This set of league codes is for the 2015/2016 season. In fall of 2016, they will need to
    // be updated. Feel free to use the codes
    private static final String BUNDESLIGA1 = "394";
    private static final String BUNDESLIGA2 = "395";
    private static final String LIGUE1 = "396";
    private static final String LIGUE2 = "397";
    private static final String PREMIER_LEAGUE = "398";
    private static final String PRIMERA_DIVISION = "399";
    private static final String SEGUNDA_DIVISION = "400";
    private static final String SERIE_A = "401";
    private static final String PRIMERA_LIGA = "402";
    private static final String BUNDESLIGA3 = "403";
    private static final String EREDIVISIE = "404";
    private static final String CHAMPIONS_LEAGUE = "405";

    private static final String[] SUPPORTED_LEAGUES = {
            BUNDESLIGA1,
            BUNDESLIGA2,
            PREMIER_LEAGUE,
            PRIMERA_DIVISION,
            SERIE_A,
            CHAMPIONS_LEAGUE };  // TODO Make leagues configurable, requires querying by stable codes (PL etc)

    public static void updateData(Context context) {
        // Additional error case: App didn't request data for last day displayed.
        // Cause: off-by-one. Fix: change to "n3"

        // Changed to one week for testing since more matches are coming up then.

        final boolean isReal = true;
        processFixtures(context, getFixtures(context, "n8"), isReal);  // today -> one week from today, inclusive
        processFixtures(context, getFixtures(context, "p2"), isReal);
    }

    /** Convert from API status string to integer representation **/
    @MatchInfo.Status
    public static int rawStatusToInt(String status) {
        switch (status) {
            case "SCHEDULED":
                return MatchInfo.STATUS_SCHEDULED;
            case "TIMED":
                return MatchInfo.STATUS_TIMED;
            case "IN_PLAY":
                return MatchInfo.STATUS_IN_PLAY;
            case "FINISHED":
                return MatchInfo.STATUS_FINISHED;
            case "POSTPONED":
                return MatchInfo.STATUS_POSTPONED;
            case "CANCELED":
            case "CANCELLED":
                return MatchInfo.STATUS_CANCELLED;
            default:
                Log.w(LOG_TAG, "unknown status encountered: " + status);
                return MatchInfo.STATUS_CANCELLED;
        }
    }

    @Nullable
    private static JSONArray getFixtures(Context context, String timeFrame) {
        final String BASE_URL = "http://api.football-data.org/alpha/fixtures"; //Base URL
        final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days
        final Uri apiUri = Uri.parse(BASE_URL).buildUpon().
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();
        final String apiKey = context.getString(R.string.api_key);
        JSONObject data = queryApi(apiUri, apiKey);
        if (data==null) {
            return null;
        }
        try {
            return data.getJSONArray(FIXTURES);
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Return the JSONObject corresponding to the passed-in uri.
     */
    @Nullable
    private static JSONObject queryApi(@NonNull Uri apiUri, @Nullable String authenticationToken) {
        if (authenticationToken==null) authenticationToken = "";
        //Log.v(LOG_TAG, "The url we are looking at is: "+apiUri.toString()); //log spam
        HttpURLConnection m_connection = null;
        BufferedReader reader = null;
        String JSON_data = null;
        //Opening Connection
        try {
            URL fetch = new URL(apiUri.toString());
            m_connection = (HttpURLConnection) fetch.openConnection();
            m_connection.setRequestMethod("GET");
            m_connection.addRequestProperty("X-Auth-Token", authenticationToken);
            m_connection.connect();

            // Read the input stream into a String
            InputStream inputStream = m_connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            JSON_data = buffer.toString();

        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception here" + e.getMessage());

        } finally {
            if (m_connection != null) {
                m_connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e)
                {
                    Log.e(LOG_TAG,"Error Closing Stream");
                }
            }
        }

        try {
            if (JSON_data != null) {
                return new JSONObject(JSON_data);
            } else {
                //Could not Connect
                Log.d(LOG_TAG, "Could not connect to server.");
            }
        } catch(Exception e) {
            Log.e(LOG_TAG,e.getMessage());
        }
        return null;
    }

    private static void processFixtures(Context context, @Nullable JSONArray fixtures, boolean isReal) {
        if (fixtures==null || fixtures.length()==0) {
            return;
        }
        // This set of league codes is for the 2015/2016 season. In fall of 2016, they will need to
        // be updated. Feel free to use the codes
        final String BUNDESLIGA1 = "394";
        final String BUNDESLIGA2 = "395";
        final String LIGUE1 = "396";
        final String LIGUE2 = "397";
        final String PREMIER_LEAGUE = "398";
        final String PRIMERA_DIVISION = "399";
        final String SEGUNDA_DIVISION = "400";
        final String SERIE_A = "401";
        final String PRIMERA_LIGA = "402";
        final String BUNDESLIGA3 = "403";
        final String EREDIVISIE = "404";
        final String CHAMPIONS_LEAGUE = "405";


        final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
        final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
        final String SOCCER_SEASON = "soccerseason";
        final String MATCH_DATE = "date";
        final String STATUS = "status";
        final String HOME_TEAM = "homeTeamName";
        final String AWAY_TEAM = "awayTeamName";
        final String HOME_TEAM_ID = "homeTeam";
        final String AWAY_TEAM_ID = "awayTeam";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";

        //Match data
        String league;
        String date;
        String time;
        String home_team;
        String away_team;
        int home_team_id;
        int away_team_id;
        String home_goals;
        String away_goals;
        String match_id;
        String match_day;
        int match_status;

        try {
            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector<>(fixtures.length());
            for (int i = 0;i < fixtures.length();i++) {

                JSONObject match_data = fixtures.getJSONObject(i);
                league = match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString("href");
                league = league.replace(SEASON_LINK,"");
                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.

                // Additional error case: About says Champions League is included but wasn't
                if (    league.equals(PREMIER_LEAGUE)      ||
                        league.equals(SERIE_A)             ||
                        league.equals(BUNDESLIGA1)         ||
                        league.equals(BUNDESLIGA2)         ||
                        league.equals(PRIMERA_DIVISION)    ||
                        league.equals(CHAMPIONS_LEAGUE) ) {

                    match_id = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                            getString("href");
                    match_id = match_id.replace(MATCH_LINK, "");
                    if (!isReal) {
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        match_id=match_id+Integer.toString(i);
                    }

                    date = match_data.getString(MATCH_DATE);
                    time = date.substring(date.indexOf("T") + 1, date.indexOf("Z"));
                    date = date.substring(0,date.indexOf("T"));
                    SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                    match_date.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date parseddate = match_date.parse(date+time);
                        SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                        new_date.setTimeZone(TimeZone.getDefault());
                        date = new_date.format(parseddate);
                        time = date.substring(date.indexOf(":") + 1);
                        date = date.substring(0,date.indexOf(":"));

                        if (!isReal) {
                            //This if statement changes the dummy data's date to match our current date range.
                            Date fragmentdate = new Date(System.currentTimeMillis()+((i-2)*86400000));
                            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                            date=mformat.format(fragmentdate);
                        }
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "error here!");
                        Log.e(LOG_TAG,e.getMessage());
                    }
                    match_status = rawStatusToInt(match_data.getString(STATUS));
                    home_team = match_data.getString(HOME_TEAM);
                    away_team = match_data.getString(AWAY_TEAM);
                    JSONObject links = match_data.getJSONObject(LINKS);
                    home_team_id = getTeam(context, parseTeamApiId(links.getJSONObject(HOME_TEAM_ID)));
                    away_team_id = getTeam(context, parseTeamApiId(links.getJSONObject(AWAY_TEAM_ID)));
                    //Log.d(LOG_TAG, "Teams are " + home_team_id + " and " + away_team_id);
                    home_goals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                    away_goals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                    match_day = match_data.getString(MATCH_DAY);


                    ContentValues match_values = new ContentValues();
                    match_values.put(DatabaseContract.scores_table.MATCH_ID,match_id);
                    match_values.put(DatabaseContract.scores_table.DATE_COL,date);
                    match_values.put(DatabaseContract.scores_table.STATUS_COL, match_status);
                    match_values.put(DatabaseContract.scores_table.TIME_COL,time);
                    match_values.put(DatabaseContract.scores_table.HOME_COL,home_team);
                    match_values.put(DatabaseContract.scores_table.AWAY_COL,away_team);
                    match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL,home_goals);
                    match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL,away_goals);
                    match_values.put(DatabaseContract.scores_table.LEAGUE_COL,league);
                    match_values.put(DatabaseContract.scores_table.MATCH_DAY,match_day);
                    match_values.put(DatabaseContract.scores_table.HOME_ID_COL, home_team_id);
                    match_values.put(DatabaseContract.scores_table.AWAY_ID_COL, away_team_id);

                    values.add(match_values);
                }
            }

            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);

            // Changed Uri. See note in DatabaseContract.
            //int num_inserted =
            context.getContentResolver().bulkInsert(
                    DatabaseContract.scores_table.CONTENT_URI,insert_data);

            //Log.v(LOG_TAG,"Successfully Inserted : " + String.valueOf(num_inserted));
        } catch (JSONException e) {
            Log.e(LOG_TAG,e.getMessage());
        }
    }

    private static int parseTeamApiId(JSONObject teamObject) {
        if (teamObject==null) {
            return -1;
        }
        try {
            return Integer.valueOf(teamObject.getString("href").replace(TEAM_LINK, ""));
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getTeam(Context context, int teamApiId) {
        Cursor cursor = context.getContentResolver().query(
                teams_table.CONTENT_URI,
                null,
                teams_table.COL_API_ID + "=" + teamApiId,
                null,
                null);
        if (cursor==null || !cursor.moveToFirst()) {
            // We should have all teams in the DB already, but this one is missing!
            Log.w(LOG_TAG, "Team " + teamApiId + " (API ID) not found in DB");
            return createTeamEntry(context, teamApiId);
        }
        try {
            return cursor.getInt(0);
        } finally {
            cursor.close();
        }
    }

    private static int createTeamEntry(Context context, int teamApiId) {
        final Uri uri = Uri.withAppendedPath(
                Uri.parse(TEAM_LINK), String.valueOf(teamApiId));
        final String apiKey = context.getString(R.string.api_key);
        final JSONObject team = queryApi(uri, apiKey);
        return processTeam(context, team, teamApiId);
    }

    /**
     * Add team described by JSONObject to the DB.
     *
     * @param apiId     The ID used by football-data.org
     * @return Returns internal ID (not identical to apiId) or
     *         `-1` if unsuccessful.
     */
    private static int processTeam(Context context, JSONObject team, int apiId) {
        if (team==null) {
            return -1;
        }
        final String NAME = "name";
        final String SHORT_NAME = "shortName";
        final String CREST_URL = "crestUrl";
        try {
            final String name = team.getString(NAME);
            final String shortName = team.getString(SHORT_NAME);
            String crestUrl = team.getString(CREST_URL);
            // Check validity
            if (!Patterns.WEB_URL.matcher(crestUrl).matches()) {
                crestUrl = "";
            }
            ContentValues row = new ContentValues();
            row.put(teams_table.COL_NAME, name);
            row.put(teams_table.COL_SHORT_NAME, shortName);
            row.put(teams_table.COL_CREST_URL, crestUrl);
            row.put(teams_table.COL_API_ID, apiId);
            Uri teamUri = context.getContentResolver().insert(teams_table.CONTENT_URI, row);
            if (teamUri==null) {
                return -1;
            }
            return Integer.valueOf(teamUri.getLastPathSegment());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean tryFetchTeams(Context context) {
        final String TEAMS = "teams";
        final Uri seasonBaseUri = Uri.parse(SEASON_LINK);
        final String apiKey = context.getString(R.string.api_key);
        for (String league : SUPPORTED_LEAGUES) {
            final Uri uri = seasonBaseUri.buildUpon().appendPath(league).appendPath(TEAMS).build();
            final JSONObject data = queryApi(uri, apiKey);

            if (data==null) {
                return false;
            }
            try {
                if (!tryProcessTeamArray(context, data.getJSONArray(TEAMS))) {
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private static boolean tryProcessTeamArray(Context context, JSONArray teams) {
        if (teams==null) {
            return false;
        }
        try {
            for (int i = 0; i < teams.length(); ++i) {
                JSONObject team = teams.getJSONObject(i);
                final int apiId = parseTeamApiId(team.getJSONObject(LINKS).getJSONObject(SELF));
                if (apiId==-1) {
                    return false;
                }
                final int dbId = processTeam(context, team, apiId);
                if (dbId==-1) {
                    return false;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}

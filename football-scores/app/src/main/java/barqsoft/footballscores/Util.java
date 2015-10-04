package barqsoft.footballscores;

import android.content.Context;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Util
{
    public static String getLeague(Context context, int league_num)
    {
        if (isBundesliga1(league_num)) { return context.getString(R.string.bundesliga_1); }
        else if (isBundesliga2(league_num)) { return context.getString(R.string.bundesliga_2); }
        else if (isChampionsLeague(league_num)) { return context.getString(R.string.champions_league); }
        else if (isPremierLeague(league_num)) { return context.getString(R.string.premier_league); }
        else if (isPrimeraDivision(league_num)) { return context.getString(R.string.primera_division); }
        else if (isSerieA(league_num)) { return context.getString(R.string.serie_a); }
        else {
            return context.getString(R.string.unknown_league);
        }
    }

    // Hardcode the numeric ids for now.
    // TODO Should really grab leagues from api, store in DB, and id via stable short codes (PL, BL1 etc)
    // http://api.football-data.org/alpha/soccerseasons

    public static boolean isBundesliga1(int apiId) {
        return apiId==394 || apiId==351;
    }
    public static boolean isBundesliga2(int apiId) {
        return apiId==395;
    }

    public static boolean isChampionsLeague(int apiId) {
        return apiId==405 || apiId==361;
    }

    public static boolean isPremierLeague(int apiId) {
        return apiId==398 || apiId==354;
    }

    public static boolean isPrimeraDivision(int apiId) {
        return apiId==399 || apiId==350;
    }

    public static boolean isSerieA(int apiId) {
        return apiId==401 || apiId==357;
    }

    /** Return localized match day string. */
    public static String formatMatchDay(Context context, int match_day, int league_num)
    {
        if (isChampionsLeague(league_num))
        {
            if (match_day <= 6)
            {
                // Additional error case: Incorrect match day (was always 6)
                return String.format(context.getString(R.string.group_stage_format), match_day);
            }
            else if(match_day == 7 || match_day == 8)
            {
                return context.getString(R.string.first_knockout_round);
            }
            else if(match_day == 9 || match_day == 10)
            {
                return context.getString(R.string.quarter_final);
            }
            else if(match_day == 11 || match_day == 12)
            {
                return context.getString(R.string.quarter_final);
            }
            else
            {
                return context.getString(R.string.final_text);
            }
        }
        else
        {
            final String formatStr = context.getString(R.string.matchday_format);
            return String.format(formatStr, match_day);
        }
    }

    public static String formatScore(int homeGoals, int awayGoals)
    {
        if(homeGoals < 0 || awayGoals < 0)
        {
            return " - ";
        }
        else
        {
            return String.valueOf(homeGoals) + " - " + String.valueOf(awayGoals);
        }
    }

    public static int getTeamCrestByTeamName (String teamName)
    {
        if (teamName==null){return R.drawable.no_icon;}
        switch (teamName)
        { //This is the set of icons that are currently in the app. Feel free to find and add more
            //as you go.
            case "Arsenal London FC" : return R.drawable.arsenal;
            case "Manchester United FC" : return R.drawable.manchester_united;
            case "Swansea City" : return R.drawable.swansea_city_afc;
            case "Leicester City" : return R.drawable.leicester_city_fc_hd_logo;
            case "Everton FC" : return R.drawable.everton_fc_logo1;
            case "West Ham United FC" : return R.drawable.west_ham;
            case "Tottenham Hotspur FC" : return R.drawable.tottenham_hotspur;
            case "West Bromwich Albion" : return R.drawable.west_bromwich_albion_hd_logo;
            case "Sunderland AFC" : return R.drawable.sunderland;
            case "Stoke City FC" : return R.drawable.stoke_city;
            default: return R.drawable.no_icon;
        }
    }
}

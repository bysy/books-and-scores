package barqsoft.footballscores;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
{
    public static final String PAGER_CURRENT = "Pager_Current";
    public static final String SELECTED_MATCH = "Selected_match";
    public static final String MY_MAIN_FRAGMENT = "my_main";
    public static final long INVALID_MATCH_ID = -1;

    public static long selected_match_id = INVALID_MATCH_ID;
    public static int current_fragment = 2;
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String SAVE_TEST = "Save Test";
    private PagerFragment my_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, "Reached MainActivity onCreate");

        // Make landscape view more useful by hiding the action bar.
        // Having a material-style transition would be better but this will do for now.
        if (getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE) {
            ActionBar ab = getSupportActionBar();
            if (ab!=null) ab.hide();
        }

        if (savedInstanceState == null) {
            my_main = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, my_main)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about)
        {
            Intent start_about = new Intent(this,AboutActivity.class);
            startActivity(start_about);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.v(LOG_TAG, SAVE_TEST + " will save" +
                "\nfragment: " + my_main.viewPager.getCurrentItem() +
                "\nselected id: " + selected_match_id);
        outState.putInt(PAGER_CURRENT, my_main.viewPager.getCurrentItem());
        outState.putLong(SELECTED_MATCH, selected_match_id);
        getSupportFragmentManager().putFragment(outState, MY_MAIN_FRAGMENT,my_main);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        Log.v(LOG_TAG, SAVE_TEST + " will retrieve" +
                "\nfragment: " + savedInstanceState.getInt(PAGER_CURRENT) +
                "\nselected id: " + savedInstanceState.getLong(SELECTED_MATCH));
        current_fragment = savedInstanceState.getInt(PAGER_CURRENT);
        selected_match_id = savedInstanceState.getLong(SELECTED_MATCH);
        my_main = (PagerFragment) getSupportFragmentManager()
                .getFragment(savedInstanceState, MY_MAIN_FRAGMENT);
        super.onRestoreInstanceState(savedInstanceState);
    }
}

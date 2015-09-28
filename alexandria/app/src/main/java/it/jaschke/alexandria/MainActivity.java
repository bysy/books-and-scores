package it.jaschke.alexandria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import it.jaschke.alexandria.api.Callback;


public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, Callback {
    private static final String CURRENT_TAG_KEY = "CURRENT_TAG_KEY";
    private static final String TITLE_KEY = "TITLE_KEY";
    private static final String BOOKS_TAG = "BOOKS_TAG";
    private static final String ADD_BOOK_TAG = "ADD_BOOK_TAG";
    private static final String ABOUT_TAG = "ABOUT_TAG";
    private static final String BOOK_DETAILS_TAG = "BOOK_DETAILS_TAG";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment navigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;
    public static boolean IS_TABLET = false;
    private BroadcastReceiver messageReciever;

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";
    private String mCurrentTag;
    private String mRootTag = BOOKS_TAG;  // TODO hardcoded root fragment
    private int mRootPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IS_TABLET = isTablet();
        if(IS_TABLET){
            setContentView(R.layout.activity_main_tablet);
        }else {
            setContentView(R.layout.activity_main);
        }

        messageReciever = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReciever,filter);

        navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        title = getTitle();

        // Set up the drawer.
        navigationDrawerFragment.setUp(R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_TAG_KEY, mCurrentTag);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentTag = savedInstanceState.getString(CURRENT_TAG_KEY, "");
        title = titleForTag(mCurrentTag);
        restoreActionBar();
    }

    private String titleForTag(String tag) {
        switch (tag) {
            case BOOKS_TAG:
            case BOOK_DETAILS_TAG:
                return getString(R.string.books);
            case ADD_BOOK_TAG:
                return getString(R.string.scan);
            case ABOUT_TAG:
                return getString(R.string.about);
            default:
                throw new IllegalArgumentException("Unknown tag");
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment nextFragment;

        String tag;
        switch (position) {
            default:
            case 0:
                tag = BOOKS_TAG;
                break;
            case 1:
                tag = ADD_BOOK_TAG;
                break;
            case 2:
                tag = ABOUT_TAG;
                break;
        }
        final String newTitle = titleForTag(tag);

        nextFragment = fragmentManager.findFragmentByTag(tag);
        if (nextFragment==null) {
            switch (position) {
                default:
                case 0:
                    nextFragment = new ListOfBooks();
                    break;
                case 1:
                    nextFragment = new AddBook();
                    break;
                case 2:
                    nextFragment = new About();
                    break;
            }
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, nextFragment, tag)
                .commit();
        mCurrentTag = tag;
        title = newTitle;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReciever);
        super.onDestroy();
    }

    @Override
    public void onItemSelected(String ean) {
        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);

        BookDetail fragment = new BookDetail();
        fragment.setArguments(args);

        int id = R.id.container;
        if(findViewById(R.id.right_container) != null){
            id = R.id.right_container;
        }
        mCurrentTag = BOOK_DETAILS_TAG;
        getSupportFragmentManager().beginTransaction()
                .replace(id, fragment)
                .commit();
    }
    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra(MESSAGE_KEY)!=null){
                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void goBack(View view){
        getSupportFragmentManager().popBackStack();
    }

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onBackPressed() {
        restoreActionBar();
        if (mCurrentTag.equals(BOOK_DETAILS_TAG)) {
            openBookList();
        } else if (!mCurrentTag.equals(mRootTag)) {
            openRootFragment();
        } else {
            super.onBackPressed();  // normal back-press to exit the app
        }
    }

    private void openBookList() {
        onNavigationDrawerItemSelected(0);
    }

    private void openRootFragment() {
        onNavigationDrawerItemSelected(mRootPosition);
    }
}

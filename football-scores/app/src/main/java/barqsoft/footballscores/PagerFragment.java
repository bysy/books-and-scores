package barqsoft.footballscores;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.TimeUtils;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by yehya khaled on 2/27/2015.
 */
public class PagerFragment extends Fragment
{
    public static final int NUM_PAGES = 10;
    private static final String DAY_KEY = "DAY_KEY";
    public ViewPager viewPager;
    private static long DAY_IN_MILLIS = DateUtils.DAY_IN_MILLIS;
    private MyPagerAdapter mPagerAdapter;
    private Calendar mCalendar;
    private int mDay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCalendar = GregorianCalendar.getInstance();
        if (savedInstanceState!=null) {
            mDay = savedInstanceState.getInt(DAY_KEY, -1);
            if (mDay==-1) mDay = mCalendar.get(Calendar.DATE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(DAY_KEY, mDay);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.pager_fragment, container, false);
        viewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new MyPagerAdapter(getChildFragmentManager());
        // Additional error case: Buggy use of FragmentStatePagerAdapter
        // This fragment used to create all the pageable fragments here.
        // This caused inconsistent state where the fragment manager
        // would restore the old fragments and we'd create new ones.
        // Fix: Create fragments on demand and let fragment manager and
        // pager adapter do their thing.

        // Additional error case: each loop iteration called currentTimeMillis()
        // so that the dates could be inconsistent when called just before midnight

        viewPager.setAdapter(mPagerAdapter);
        viewPager.setCurrentItem(MainActivity.current_fragment);
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
        tabLayout.setTabsFromPagerAdapter(mPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        return rootView;
    }

    private void updateDate() {
        final int latestDay = mCalendar.get(Calendar.DATE);
        if (latestDay!=mDay) {
            mDay = latestDay;
            if (mPagerAdapter!=null) {
                mPagerAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDate();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        @Override
        public Fragment getItem(int i) {
            final Calendar date = (Calendar) mCalendar.clone();
            date.add(Calendar.DAY_OF_MONTH, i - 2);
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setCalendar(date);
            MainScreenFragment f = new MainScreenFragment();
            f.setFragmentDate(dateFormat.format(date.getTime()));
            return f;
        }

        @Override
        public int getCount()
        {
            return NUM_PAGES;
        }

        public MyPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }
        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position)
        {
            return getDayName(getActivity(),System.currentTimeMillis()+((position-2)*DAY_IN_MILLIS));
        }
        public String getDayName(Context context, long dateInMillis) {
            // If the date is today, return the localized version of "Today" instead of the actual
            // day name.

            Time t = new Time();
            t.setToNow();
            int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
            int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
            if (julianDay == currentJulianDay) {
                return context.getString(R.string.today);
            } else if ( julianDay == currentJulianDay +1 ) {
                return context.getString(R.string.tomorrow);
            }
             else if ( julianDay == currentJulianDay -1)
            {
                return context.getString(R.string.yesterday);
            }
            else
            {
                Time time = new Time();
                time.setToNow();
                // Otherwise, the format is just the day of the week (e.g "Wednesday".
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                return dayFormat.format(dateInMillis);
            }
        }
    }
}

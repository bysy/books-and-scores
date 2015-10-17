package barqsoft.footballscores;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import barqsoft.footballscores.ScoresAdapter.ViewHolder;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = MainScreenFragment.class.getSimpleName();
    private static final java.lang.String DATE_KEY = "DATE_KEY";
    public ScoresAdapter mAdapter;
    public static final int SCORES_LOADER = 0;
    private String date;
    TextView mEmptyView;

    public void setFragmentDate(String date) {
        this.date = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView score_list = (ListView) rootView.findViewById(R.id.scores_list);
        mEmptyView = (TextView) rootView.findViewById(R.id.list_empty_view);
        score_list.setEmptyView(mEmptyView);
        mAdapter = new ScoresAdapter(getActivity(),null,0);
        score_list.setAdapter(mAdapter);
        // Additional error case: Crash due to null SQL selection argument
        // For example: open app, go to home, rotate device, go back to app -> crash
        // That happened when instances of this class were restored after
        // certain navigation patterns. See also note in PagerFragment.
        // Fix: save and restore date
        if (savedInstanceState!=null) {
            final String date = savedInstanceState.getString(DATE_KEY);
            if (date!=null) {
                this.date = date;
            }
        }
        getLoaderManager().initLoader(SCORES_LOADER,null,this);
        score_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder clicked = (ViewHolder) view.getTag();
                if (MainActivity.selected_match_id != clicked.match_id) {
                    MainActivity.selected_match_id = clicked.match_id;
                } else {
                    MainActivity.selected_match_id = MainActivity.INVALID_MATCH_ID;
                }
                mAdapter.notifyDataSetChanged();
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DATE_KEY, date);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (date==null) {
            Log.e(TAG, "Trying to create loader for empty date");
            return null;
        }
        return ScoresAdapter.newCursorLoader(getActivity(), date);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        //Log.v(FetchScoreTask.LOG_TAG,"loader finished");
        //cursor.moveToFirst();

        //int i = cursor.getCount();

        if (cursor.getCount()==0) {
            trySetEmptyView();
        }
        //Log.v(FetchScoreTask.LOG_TAG,"Loader query: " + String.valueOf(i));
        mAdapter.swapCursor(cursor);
        //mAdapter.notifyDataSetChanged();
    }

    private void trySetEmptyView() {
        if (mEmptyView!=null) {
            mEmptyView.setText(getContext().getString(R.string.empty_matches_list_text));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

}

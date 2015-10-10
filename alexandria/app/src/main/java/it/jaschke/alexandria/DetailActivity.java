package it.jaschke.alexandria;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DetailActivity extends AppCompatActivity {
    public static final String EAN_KEY = "EAN_KEY";
    public static final int INVALID_EAN = -1;
    BookDetail mFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        final long ean = getIntent().getLongExtra(EAN_KEY, INVALID_EAN);
        if (ean!=INVALID_EAN) {
            mFragment = BookDetail.newInstance(ean);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, mFragment, BookDetail.FRAGMENT_TAG)
                    .commit();
        }
    }
}

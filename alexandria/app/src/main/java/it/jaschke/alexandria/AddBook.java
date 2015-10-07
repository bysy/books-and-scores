package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private static long INVALID_EAN = -1;
    private EditText eanEditText;
    private long eanQuery = INVALID_EAN;
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT="eanContent";
    private static final String SCAN_FORMAT = "scanFormat";
    private static final String SCAN_CONTENTS = "scanContents";

    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";

    static String extractDigits(String s) {
        return s.replaceAll("[^\\d]", "");
    }

    static boolean startsWithIsbn13Prefix(String s) {
        return s.startsWith("978") || s.startsWith("979");
    }

    /**
     * Check if string contains a valid ISBN-13 value.
     * The string must not contain non-numeric digits.
     */
    static boolean isValidIsbn13(String s) {
        final int length = s.length();
        if (length!=13) return false;
        final int checkDigit = getIntAt(s, length-1);
        final int calculated = isbn13CheckDigit(s);
        return calculated==checkDigit;
    }

    static int isbn13CheckDigit(String ean) {
        // for a full ISBN-13, calculate for the first 12 digits, otherwise calculate for all digits
        final int last = ean.length()>=11 ? 11 : ean.length()-1;

        int sum = 0;
        for (int i = 0; i<=last; ++i) {
            final int digit = getIntAt(ean, i);
            final int value = (i%2==0) ? digit : 3*digit;
            sum += value;
        }
        return (10 - (sum % 10)) % 10;
    }

    static String isbn13FromIsbn10(String ean) {
        ean = ean.substring(0, 9);
        ean = "978" + ean;
        return ean + isbn13CheckDigit(ean);
    }

    static int getIntAt(String s, int i) {
        return Integer.parseInt(String.valueOf(s.subSequence(i, i+1)));
    }

    public AddBook(){
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(eanEditText!=null) {
            outState.putString(EAN_CONTENT, eanEditText.getText().toString());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        eanEditText = (EditText) rootView.findViewById(R.id.ean);

        eanEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Allow non-numerical input, like dashes, for better usability
                String ean = extractDigits(s.toString());

                //catch isbn10 numbers

                // Additional error case: IBSN-10 numbers have a different
                // check-digit from ISBN-13. Caused book fetching to fail.
                // Fix: Correct conversion
                if (ean.length() == 9 && !startsWithIsbn13Prefix(ean)) {
                    ean = isbn13FromIsbn10(ean);
                }

                if (ean.length() < 13) {
                    clearFields();
                    return;
                }
                if (!isValidIsbn13(ean)) {
                    return;  // TODO Show notice to user that ISBN appears to be wrong
                }
                handleEan(ean);
            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch scan activity
                IntentIntegrator integrator = IntentIntegrator.forSupportFragment(AddBook.this);
                integrator.setCaptureActivity(ScanActivity.class);
                integrator.setOrientationLocked(false);
                integrator.initiateScan();  // Will call us back with result
            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eanEditText.setText("");
            }
        });

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, String.valueOf(eanQuery));
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                eanEditText.setText("");
                eanQuery = INVALID_EAN;
            }
        });

        if(savedInstanceState!=null){
            eanEditText.setText(savedInstanceState.getString(EAN_CONTENT));
        }

        return rootView;
    }

    /** Search for ean and load result. */
    private void handleEan(String ean) {
        eanQuery = Long.parseLong(ean);
        if (!isConnected()) {
            Toast.makeText(getActivity(), "Not connected", Toast.LENGTH_LONG).show();
            restartLoader();  // in case the book is in the DB already
            return;
        }
        // TODO change to show multiple results per ISBN because top result may be wrong
        //Once we have an ISBN, start a book intent
        Intent bookIntent = new Intent(getActivity(), BookService.class);
        bookIntent.putExtra(BookService.EAN, ean);
        bookIntent.setAction(BookService.FETCH_BOOK);
        getActivity().startService(bookIntent);
        restartLoader();
    }

    private boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo!=null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        final String code = result.getContents();
        Log.d(TAG, "Scanned code is " + code);
        if (code!=null) {
            eanEditText.setText(code);
            handleEan(code);
        }
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(eanQuery==INVALID_EAN){
            return null;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(eanQuery),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        String[] authorsArr = authors.split(",");
        ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
        ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",","\n"));
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(!imgUrl.isEmpty()){
            final ImageView coverView = (ImageView) rootView.findViewById(R.id.bookCover);
            Picasso.with(getContext()).load(imgUrl).into(coverView);
            rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields(){
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

}

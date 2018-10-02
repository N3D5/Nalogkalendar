package ru.nalogkalendar;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class CalendarActivity extends AppCompatActivity {
    private CaldroidFragment caldroidFragment;
    private Context mContext = CalendarActivity.this;
    private static final int ACTIVITY_NUM = 1;
    String strforWebView;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        setupBottomNavigationView();

        final SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH); //January 1, 2017

        caldroidFragment = new CaldroidFragment();
        if (savedInstanceState != null) {
            caldroidFragment.restoreStatesFromKey(savedInstanceState,
                    "CALDROID_SAVED_STATE");
        }
        //Если создаётся первый раз
        else {
            Bundle args = new Bundle();
            Calendar cal = Calendar.getInstance();
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
            args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
            args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true);
            caldroidFragment.setArguments(args);
        }
        dbHelper = new DBHelper(this);
        setCustomResourceForDates();


        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar, caldroidFragment);
        t.commit();
        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                String[] columns = {"cdata"};
                String data = formatter.format(date).toLowerCase();
                String selection = "date = ?";
                String[] selectionArgs = new String[]{data};
                Cursor c = db.query("calendarhtml", columns, selection, selectionArgs, null, null, null);
                if (c != null) {
                    if (c.moveToFirst()) {

                        do {
                            strforWebView = "";
                            for (String cn : c.getColumnNames()) {
                                strforWebView = strforWebView.concat(c.getString(c.getColumnIndex(cn)));
                            }
                        } while (c.moveToNext());
                    }
                    c.close();
                    Intent intent = new Intent(mContext, AboutTaxeActivity.class);
                    intent.putExtra("cdata", strforWebView);
                    startActivity(intent);
                }
                Toast.makeText(getApplicationContext(), formatter.format(date).toLowerCase(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChangeMonth(int month, int year) {
                String text = "month: " + month + " year: " + year;
                Toast.makeText(getApplicationContext(), text,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClickDate(Date date, View view) {
                Toast.makeText(getApplicationContext(),
                        "Long click " + formatter.format(date),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCaldroidViewCreated() {
                if (caldroidFragment.getLeftArrowButton() != null) {
                    Toast.makeText(getApplicationContext(),
                            "Caldroid view is created", Toast.LENGTH_SHORT)
                            .show();
                }
            }

        };
        caldroidFragment.setCaldroidListener(listener);
    }

    //Красим календарь
    private void setCustomResourceForDates() {
        String dataStr;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query("calendarhtml", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            int dateColIndex = c.getColumnIndex("date");

            do {
                SimpleDateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
                dataStr = c.getString(dateColIndex);
                Date blueDate = null;
                try {
                    blueDate = format.parse(dataStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (caldroidFragment != null) {
                    ColorDrawable blue = new ColorDrawable(getResources().getColor(R.color.blue));
                    caldroidFragment.setBackgroundDrawableForDate(blue, blueDate);
                    caldroidFragment.setTextColorForDate(R.color.white, blueDate);
                }
            }
            while (c.moveToNext());
        }
        c.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);

        if (caldroidFragment != null) {
            caldroidFragment.saveStatesToKey(outState, "CALDROID_SAVED_STATE");
        }

    }

    private void setupBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavViewBar);
        BottomNafigationViewHelper.enableNavigation(mContext, bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

}

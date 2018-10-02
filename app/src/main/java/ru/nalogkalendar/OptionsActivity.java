package ru.nalogkalendar;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class OptionsActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private Context mContext = OptionsActivity.this;
    private static final int ACTIVITY_NUM = 2;
    private Button chooseTimeBtn, saveSettingsBtn;
    private EditText timeTv;
    private Switch statusSw;
    DBHelper dbHelper;
    private int status, hour, minute;

    //String[] data = {"1 день", "2 дня", "3 дня", "4 Дня", "5 Дней"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        dbHelper = new DBHelper(this);
        ClockPickerFragment clockPickerFragment = new ClockPickerFragment();
        setupBottomNavigationView();
        timeTv = (EditText) findViewById(R.id.timeTv);
        saveSettingsBtn = (Button) findViewById(R.id.savesettingsBtn);
        statusSw = (Switch) findViewById(R.id.statusSw);

        saveSettingsBtn.setOnClickListener(this);
        timeTv.setOnClickListener(this);
        statusSw.setOnCheckedChangeListener(this);
        readsettings();

       /* ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);*/


        /*Spinner spinner = (Spinner) findViewById(R.id.choose_days_spinner);
        spinner.setAdapter(adapter);
        spinner.setPrompt("Выберите количество дней");
        spinner.setSelection(4);*/

    }


    String[] time;

    void readsettings() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query("notifsettings", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            int statusCollIndex = c.getColumnIndex("status");
            int hourCollIndex = c.getColumnIndex("hour");
            int minuteCollIndex = c.getColumnIndex("minute");
            do {
                status = c.getInt(statusCollIndex);
                hour = c.getInt(hourCollIndex);
                minute = c.getInt(minuteCollIndex);
            }
            while (c.moveToNext());
        } else {
            // time = "Выберите";
            hour = 12;
            minute = 0;
            status = 0;
        }
        c.close();

        timeTv.setText(hour + ":" + minute);
        if (status == 1) {
            statusSw.setChecked(true);
        } else {
            statusSw.setChecked(false);
        }
    }

    String id = "1";// номер обновляемой строки в базе (нулевая)

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.timeTv:
                DialogFragment timepickerDialog = new ClockPickerFragment();
                timepickerDialog.show(getSupportFragmentManager(), "timePicker");
                break;
            case R.id.savesettingsBtn:
                stopService(new Intent(this, NotifyService.class));
                ContentValues cv = new ContentValues();
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor c = db.query("notifsettings", null, null, null, null, null, null);
                time = timeTv.getText().toString().split(":");
                hour = Integer.parseInt(time[0]);
                minute = Integer.parseInt(time[1]);
                if (c.getCount() == 0) {
                    cv.put("status", status);
                    cv.put("hour", hour);
                    cv.put("minute", minute);
                    db.insert("notifsettings", null, cv);
                }
                if (c.getCount() == 1) {
                    cv.put("status", status);
                    cv.put("hour", hour);
                    cv.put("minute", minute);
                    db.update("notifsettings", cv, "id=?", new String[]{id});
                }
                c.close();
                if (status == 1) {
                    startService(new Intent(this, NotifyService.class));
                }
                if (status == 0) {
                    stopService(new Intent(this, NotifyService.class));
                }
                Toast.makeText(getApplicationContext(),
                        "Saved",
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void setupBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavViewBar);
        BottomNafigationViewHelper.enableNavigation(mContext, bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        if (statusSw.isChecked()) {
            status = 1;
        }
        if (!statusSw.isChecked()) {
            status = 0;
        }
    }
}

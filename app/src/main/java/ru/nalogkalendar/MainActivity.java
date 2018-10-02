package ru.nalogkalendar;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView eventsLv;
    private Handler mHandler;
    private static final int ACTIVITY_NUM = 0;
    private Context mContext = MainActivity.this;
    SimpleAdapter adapter;

    DBHelper dbHelper;

    String urlStr = "https://www.nalog.ru/opendata/7707329152-kalendar/data-01012017-structure-02282014.xml";//Xml с Календарём за 01.01.2017

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setupBottomNavigationView();
        adapter = new SimpleAdapter(this, listEvent, android.R.layout.simple_list_item_2,
                new String[]{"text1", "text2"},
                new int[]{android.R.id.text1, android.R.id.text2});

        dbHelper = new DBHelper(this);
        mHandler = new Handler(Looper.myLooper());
        eventsLv = findViewById(R.id.message);
        try {
            parse();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mHandler.post(FillList);

    }


    @Override
    public void onClick(View view) {

    }

    String numStr,
            typeStr,
            cdataStr,
            yearStr,
            monthStr;
    String b = "";

    @RequiresPermission(Manifest.permission.INTERNET)
    public void parse() throws XmlPullParserException, IOException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ContentValues cv = new ContentValues();
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    Cursor c = db.query("calendarhtml", null, null, null, null, null, null);
                    int countRows = c.getCount();
                    if (countRows == 0) {
                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                        factory.setNamespaceAware(true);
                        XmlPullParser xpp = factory.newPullParser();
                        URL urlXML = new URL(urlStr);
                        InputStream stream = urlXML.openStream();
                        xpp.setInput(stream, null);

                        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                            switch (xpp.getEventType()) {
                                case XmlPullParser.START_DOCUMENT:
                                    break;
                                case XmlPullParser.START_TAG:
                                    if (xpp.getName().equals("year")) {
                                        yearStr = xpp.getAttributeValue(0);
                                    }
                                    if (xpp.getName().equals("month")) {
                                        monthStr = xpp.getAttributeValue(0);
                                    }
                                    if (xpp.getName().equals("day")) {
                                        numStr = xpp.getAttributeValue(0);
                                        if (xpp.getAttributeValue(1).equals("event")) {
                                            xpp.nextToken();
                                            cdataStr = xpp.getText();
                                            cv.put("date", monthStr + " " + numStr + ", " + yearStr);
                                            cv.put("cdata", cdataStr);
                                            db.insert("calendarhtml", null, cv);
                                        }
                                    }
                                    break;
                                case XmlPullParser.END_TAG:
                                    break;
                                case XmlPullParser.END_DOCUMENT:
                                    break;
                            }
                            xpp.next();
                        }
                    }

                    c.close();
                    mHandler.post(FillList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();


    }

    private ArrayList<HashMap<String, String>> listEvent = new ArrayList<>();//Адаптер для домашней страницы
    HashMap<String, String> map;

    Runnable FillList = new Runnable() {
        @Override
        public void run() {
            //public void fillListView(){
            listEvent.clear();
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor c = db.query("calendarhtml", null, null, null, null, null, null);
            if (c.moveToFirst()) {
                int dataCollIndex = c.getColumnIndex("date");

                String data;
                do {
                    data = c.getString(dataCollIndex);


                    map = new HashMap<>();

                    map.put("text1", data);
                    map.put("text2", data);
                    listEvent.add(map);
                }
                while (c.moveToNext());

                eventsLv.setAdapter(adapter);
            }
            c.close();
        }
        //}
    };

    //нижнее меню
    private void setupBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavViewBar);
        BottomNafigationViewHelper.enableNavigation(mContext, bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }


}
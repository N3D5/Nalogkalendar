package ru.nalogkalendar;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotifyService extends Service {
    DBHelper dbHelper;
    int hour, minute;
    int kostil;//не даёт выкинуть первое оповещение в обход AlarmManager'а

    @Override
    public void onCreate() {
        super.onCreate();
        kostil = 0;
        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cNotif = db.query("notifsettings", null, null, null, null, null, null);
        Calendar cur_cal = Calendar.getInstance();
        if (cNotif.moveToFirst()) {
            int hourCollIndex = cNotif.getColumnIndex("hour");
            int minuteCollIndex = cNotif.getColumnIndex("minute");
            do {
                hour = cNotif.getInt(hourCollIndex);
                minute = cNotif.getInt(minuteCollIndex);
            }
            while (cNotif.moveToNext());
            //Задаём время оповещений
            cur_cal.set(Calendar.HOUR_OF_DAY, hour);
            cur_cal.set(Calendar.MINUTE, minute);
            cur_cal.set(Calendar.SECOND, 0);
            cur_cal.set(Calendar.MILLISECOND, 0);
        }
        cNotif.close();
        dbHelper.close();
        Long alarmTime = cur_cal.getTimeInMillis();
        Intent intent = new Intent(this, NotifyService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000, pintent); //Повторящийся Аларм который вызывает  onStartCommand
    }

    String text = "Ваш налоговый календарь";

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (kostil > 0) {
            sendNotif();
        }
        kostil++;
        return super.onStartCommand(intent, flags, startId);
    }

    String dateEvent = "";

    void sendNotif() {
        final SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH); //January 1, 2017
        Date datenow = new Date(); // дата сейчас
        String datecur = formatter.format(datenow).toLowerCase();//дата сейчас преобразуем


        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {"date"}; //столбец который хотим получить в ответе
        String selection = "date = ?"; // условие
        String[] selectionArgs = new String[]{datecur}; // чему равно условие date = datecur
        Cursor cCalendar = db.query("calendarhtml", columns, selection, selectionArgs, null, null, null);
        if (cCalendar.moveToFirst()) {

            do {
                for (String cn : cCalendar.getColumnNames()) {
                    dateEvent = dateEvent.concat(cCalendar.getString(cCalendar.getColumnIndex(cn)));
                }
            }
            while (cCalendar.moveToNext());
            cCalendar.close();
            dbHelper.close();
        }

        if (datecur.equals(dateEvent)) {//Сравниваем с датами из базы если совпадает, то выдаём оповещение
            Intent resultIntent = new Intent(this, CalendarActivity.class);
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            NotificationCompat.Builder noti =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_dashboard_black_24dp)
                            .setContentTitle(text)
                            .setContentText("Пора платить налоги");
            noti.setContentIntent(resultPendingIntent);
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.

            mNotifyMgr.notify(1, noti.build());
        }
    }


    public IBinder onBind(Intent arg0) {
        return null;
    }
}
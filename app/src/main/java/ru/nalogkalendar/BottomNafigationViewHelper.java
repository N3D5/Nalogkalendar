package ru.nalogkalendar;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

//Меню навигации
public class BottomNafigationViewHelper {
    public static void setupBottomNavigationView(BottomNavigationView bottomNavigationView) {

    }

    public static void enableNavigation(final Context context, BottomNavigationView view) {
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        Intent intent1 = new Intent(context, MainActivity.class);
                        context.startActivity(intent1);
                        break;
                    case R.id.navigation_dashboard:
                        Intent intent2 = new Intent(context, CalendarActivity.class);
                        context.startActivity(intent2);
                        break;
                    case R.id.navigation_notifications:
                        Intent intent3 = new Intent(context, OptionsActivity.class);
                        context.startActivity(intent3);
                        break;
                }
                return false;
            }
        });
    }
}

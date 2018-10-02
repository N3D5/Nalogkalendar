package ru.nalogkalendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;


public class ClockPickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    int hour;
    int minute;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
         hour = c.get(Calendar.HOUR_OF_DAY);
         minute = c.get(Calendar.MINUTE);
        Dialog picker = new TimePickerDialog(getActivity(), this, hour, minute, true);
        return picker;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onTimeSet(TimePicker view, int hours, int minute) {
        EditText tv = (EditText) getActivity().findViewById(R.id.timeTv);
        tv.setText(hours+":"+minute); //Выводим пикнутую дату
        Toast.makeText( getActivity().getApplicationContext(),
                "Выбрано время:"+hours+":"+minute,
                Toast.LENGTH_SHORT).show();
    }

}
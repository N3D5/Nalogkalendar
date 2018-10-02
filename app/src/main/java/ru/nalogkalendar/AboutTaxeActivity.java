package ru.nalogkalendar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;

public class AboutTaxeActivity extends AppCompatActivity implements View.OnClickListener {
    private WebView aboutTaxeWV;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abouttaxe);
        aboutTaxeWV = findViewById(R.id.aboutTaxeInfo);

        String abouttaxeStr =
                "<html>"
                        + getIntent().getStringExtra("cdata")
                        + "</html>";
        aboutTaxeWV.loadDataWithBaseURL(null, abouttaxeStr, "text/html", "en_US", null); //Заполняем WebView
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        }
    }
}

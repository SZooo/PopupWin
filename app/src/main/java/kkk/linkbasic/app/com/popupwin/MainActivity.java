package kkk.linkbasic.app.com.popupwin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    PicturesPopupWindows popupWindows;
    View rootview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootview = findViewById(R.id.rootview);
        popupWindows = new PicturesPopupWindows(this, 1500, 1500, 1, 1, new PicturesPopupWindows.OnSelectPhotoListener() {
            @Override
            public void onSucceed(String path) {
                Toast.makeText(MainActivity.this, path, Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.btn_pop).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_pop:
                popupWindows.showAtLocation(rootview, Gravity.CENTER, 0, 0);
                break;

        }

    }
}

package com.example.pytorchandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

public class Result extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        Bitmap bitmap = (Bitmap) getIntent().getParcelableExtra("imagedata");
//        String pic = getIntent().getStringExtra("iamgedata");


        String pred = getIntent().getStringExtra("pred");

//        ImageView imageView = findViewById(R.id.image);
//        imageView.setImageResource(pic);

        TextView textView = findViewById(R.id.label);
        textView.setText(pred);

    }

}
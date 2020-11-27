package com.example.pytorchandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import static android.graphics.Color.*;

public class Result extends AppCompatActivity {

    private Button share1;
    private Button delete;

    private ArrayList<String> filePaths;
    private int position;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        position = getIntent().getIntExtra("imagedata", 0);

        filePaths = new ArrayList<String>();
        filePaths = getFilePaths();

        final int truePosition = filePaths.size()-1-position;
        Bitmap bmp = processFilePath(filePaths.get(truePosition));
        final File file = new File(filePaths.get(truePosition));

        //byte[] byteArray = getIntent().getByteArrayExtra("imagedata");
        //Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        final String pred = getIntent().getStringExtra("pred");

        imageView = findViewById(R.id.image);
        imageView.setImageBitmap(bmp);

        imageView.setRotation(90);
        TextView textView = findViewById(R.id.label);
        delete = findViewById(R.id.deleteButton);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change();
            }
        });
        String stringArray[] = pred.split(" ");
        String confidence = stringArray[stringArray.length-1];


        if (confidence.equals("(High)")) {
            textView.setTextColor(Color.GREEN);
        }
        else if (confidence.equals("(Med)")){
            textView.setTextColor(Color.BLUE);
        }
        else {
            textView.setTextColor(Color.RED);
        }

        textView.setText(pred);
        share1 = (Button)findViewById(R.id.share);
        share1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Log.d("Results.java", "Share button clicked");
                Uri uri = FileProvider.getUriForFile(Result.this, BuildConfig.APPLICATION_ID + ".provider", file);
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sharingIntent.putExtra(Intent.EXTRA_TITLE, pred);
                sharingIntent.setType("Image/*");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));

            }
        });

    }
    private void change(){
        imageView.setImageBitmap(null);
        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);

    }
    private static ArrayList<String> getFilePaths(){
        ArrayList<String> filePaths = new ArrayList<String>();

        File directory = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator, "MyCameraApp");

        // check for directory
        if (directory.isDirectory())
        {
            // getting list of file paths
            File[] listFiles = directory.listFiles();

            // Check for count
            if (listFiles.length > 0)
            {

                for (int i = 0; i < listFiles.length; i++)
                {

                    String filePath = listFiles[i].getAbsolutePath();
                    filePaths.add(filePath);

                }
            }
            else
            {
                // image directory is empty
                String emptyError = "Album is empty";
            }

        }
        return filePaths;
    }

    private static Bitmap processFilePath(String filePath){
        Bitmap bmp = BitmapFactory.decodeFile(filePath);
        return bmp;
    }

}
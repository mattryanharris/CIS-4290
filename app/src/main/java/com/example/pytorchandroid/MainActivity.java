package com.example.pytorchandroid;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    Preview preview;
    Button buttonClick;
    Camera camera;
    Activity act;
    Context ctx;
    Classifier classifier;
    private Handler handler = new Handler();
    private Runnable runnable;
    //Set up Camera variables
    private ArrayList<CameraItem> cameraList = new ArrayList<CameraItem>();
    private CameraAdapter cameraAdapter;
    private ListView listView;
    Bitmap bmp;
    String detail;
    private boolean isRunning;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        act = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        classifier = new Classifier(Utils.assetFilePath(this,"resnet-sm11-4-20.pt"));

        // Set up ListView and ArrayList
        listView = (ListView) findViewById(R.id.camera_list);
        ArrayList<String> filePaths = new ArrayList<String>();
        filePaths = getFilePaths();

//        Log.d(TAG, String.valueOf(filePaths.size() + " images in storage"));

        // Run each individual file paths to the classifier then added to the cameraList array
        for (int i = filePaths.size() - 1; i >= 0; i--){
            bmp = processFilePath(filePaths.get(i));
            detail = classifier.predict(bmp);
            cameraList.add(new CameraItem(bmp, detail));
        }

        //Now enter the ArrayList into the Adapter
        cameraAdapter = new CameraAdapter(this, cameraList);
        listView.setAdapter(cameraAdapter);

        preview = new Preview(this, (SurfaceView)findViewById(R.id.surfaceView));
        preview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        ((FrameLayout) findViewById(R.id.camera)).addView(preview);

        //add a textview to the surfaceview
        if (!filePaths.isEmpty()) {
            final int truePosition = filePaths.size() - 1;
            Bitmap ARbmp = processFilePath(filePaths.get(truePosition));
            String ARdetail = classifier.predict(ARbmp);
            TextView txt = (TextView) findViewById(R.id.txtOverSv);
            txt.setText(ARdetail);
            ((ViewGroup) txt.getParent()).removeView(txt);
            preview.addView(txt);
        }
        preview.setKeepScreenOn(true);

        runnable = new Runnable() {

            @Override
            public void run() {
                camera.takePicture(shutterCallback, rawCallback, mPicture);
                handler.postDelayed(this, 2000);
            }
        };

        //initial state should be false
        isRunning = false;
        preview.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!isRunning){
                    isRunning = true;
                    handler.post(runnable);
                } else {
                    isRunning = false;
                    handler.removeCallbacks(runnable);
                }

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                isRunning = false;
                handler.removeCallbacks(runnable);
                ImageView imageview = (ImageView) view.findViewById(R.id.imageview_array);
                TextView textTv = view.findViewById(R.id.textview_array);


                //ByteArrayOutputStream stream = new ByteArrayOutputStream();
                //Bitmap bitmap = ((BitmapDrawable) imageview.getDrawable()).getBitmap();
                //bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                //byte[] byteArray = stream.toByteArray();

                Intent resultView = new Intent(MainActivity.this, Result.class);

                resultView.putExtra("imagedata", position);
                resultView.putExtra("pred", textTv.getText().toString());

                startActivity(resultView);
            }
        });
//        Toast.makeText(ctx, getString(R.string.take_photo_help), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int numCams = Camera.getNumberOfCameras();
        if(numCams > 0){
            try{
                camera = Camera.open(0);
//                camera.setDisplayOrientation(90);
                camera.startPreview();
                preview.setCamera(camera);
            } catch (RuntimeException ex){
                Toast.makeText(ctx, getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        if(camera != null) {
            camera.stopPreview();
            preview.setCamera(null);
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    private void resetCam() {
        camera.startPreview();
        preview.setCamera(camera);
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            //			 Log.d(TAG, "onShutter'd");
        }
    };

    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //			 Log.d(TAG, "onPictureTaken - raw");
        }
    };

    PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                //refresh gallery
                refreshGallery(pictureFile);

                //reset camera
                resetCam();

                //convert image file to bitmap
                Bitmap bmp = BitmapFactory.decodeFile(String.valueOf(pictureFile));
                String detail = classifier.predict(bmp);
                cameraList.add(0, new CameraItem(bmp, detail));
                cameraAdapter.notifyDataSetChanged();

                TextView txt=(TextView)findViewById(R.id.txtOverSv);
                txt.setText(detail);
                ((ViewGroup)txt.getParent()).removeView(txt);
                preview.addView(txt);

                //Set image view
                //ImageView imageView = findViewById(R.id.imageTest);
                //imageView.setImageBitmap(bmp);
                //correct the image orientation
                //imageView.setRotation(90);

                //get image from the view
                //Bitmap bmRotated = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                //pass the rotated bitmap to the classifier to get predicted
                //String pred = classifier.predict(bmRotated);

                //Set text view
                //TextView tvPred = findViewById(R.id.predicted);
                //tvPred.setText(pred);

                //sent to arraylist in listview
                //create click event listen on list item and send data to results intent

                //IDEA FOR FINAL ITERATION OF THE CODE
                //   ArrayList<CameraItem> cameraList = new ArrayList<>();
                //For loop

                //   cameraList.add(new cameraItem(Bitmap, Label));

                //   CameraAdapter = new CameraAdapter(this, cameraList);
                //   listView.setAdapter(CameraAdapter);


            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }
        }
    };
    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
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
    // Still thinking about how to get around this issue.
    private static Bitmap processFilePath(String filePath){
        Bitmap bmp = BitmapFactory.decodeFile(filePath);
        return bmp;
    }



    @Override
    protected void onDestroy() {
        isRunning = false;
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }
}


//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import java.io.File;
//
//
//
//public class MainActivity extends AppCompatActivity {
//
//    int cameraRequestCode = 001;
//
//    Classifier classifier;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//
//        classifier = new Classifier(Utils.assetFilePath(this,"resnet-v2.pt"));
//
//        Button capture = findViewById(R.id.capture);
//
//        capture.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View view){
//
//                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//                startActivityForResult(cameraIntent,cameraRequestCode);
//
//            }
//
//
//        });
//
//    }
//
//    @Override
////    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
////
////        super.onActivityResult(requestCode, resultCode, data);
////        if (requestCode == cameraRequestCode && resultCode == RESULT_OK) {
////
////            Intent resultView = new Intent(this, Result.class);
////
////            resultView.putExtra("imagedata", data.getExtras());
////
////            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
////
////            String pred = classifier.predict(imageBitmap);
//            resultView.putExtra("pred", pred);
//
//            startActivity(resultView);
//
//        }
//
//    }
//
//}
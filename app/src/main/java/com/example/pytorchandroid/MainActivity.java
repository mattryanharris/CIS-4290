package com.example.pytorchandroid;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.os.Build;
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

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private RecyclerView recyclerView;
    Bitmap bmp;
    String detail;
    private boolean isRunning;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS= 7;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        act = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        checkAndroidVersion();
        initializeUiandCamera();

    }



    public void initializeUiandCamera() {
        setContentView(R.layout.activity_main);

        classifier = new Classifier(Utils.assetFilePath(this,"resnet-sm11-4-20.pt"));
        // Set up ListView and ArrayList
        recyclerView = (RecyclerView) findViewById(R.id.camera_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<String> filePaths = new ArrayList<String>();
        filePaths = getFilePaths();

        // Run each individual file paths to the classifier then added to the cameraList array
        //limits the list to 15 items
        int j = 0;
        int listLimit = 15;
        if(filePaths.size()>listLimit){
            j = filePaths.size()-15;
        }

        for (int i = j; i <= filePaths.size() - 1; i++){
            bmp = processFilePath(filePaths.get(i));
            detail = classifier.predict(bmp);
            cameraList.add(new CameraItem(bmp, detail));
        }

        //Now enter the ArrayList into the Adapter
        cameraAdapter = new CameraAdapter(this, cameraList);
        recyclerView.setAdapter(cameraAdapter);

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

        cameraAdapter.setOnItemClickListener(new CameraAdapter.OnClickListener() {
            @Override
            public void onClick(String text, int position) {
                isRunning = false;
                handler.removeCallbacks(runnable);

                Intent resultView = new Intent(MainActivity.this, Result.class);

                resultView.putExtra("imagedata", position);
                resultView.putExtra("pred", text);

                startActivity(resultView);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        initializeUiandCamera();
        int numCams = Camera.getNumberOfCameras();
        TextView txt=(TextView)findViewById(R.id.txtOverSv);
        txt.setText("");
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

    private void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();

        } else {
            // code for lollipop and pre-lollipop devices
        }

    }


    private boolean checkAndRequestPermissions() {
        int camera = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA);
        int wtite = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (wtite != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (read != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(MainActivity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d("in fragment on request", "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        initializeUiandCamera();
                        Log.d("in fragment on request", "CAMERA & WRITE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d("in fragment on request", "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            showDialogOK("Camera and Storage Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(MainActivity.this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }
}

package com.example.pytorchandroid;

import android.graphics.Bitmap;

public class CameraItem {
    //Storage for variables making it possible to have an Array List of CameraItem Object
    //Object of the image
    private Bitmap cameraImage;
    //Classifier of the object
    private String cameraClassified;

    //Constructor to create an instance of the CameraItem object
    public CameraItem(Bitmap cameraImage, String cameraClassified){
        this.cameraImage = cameraImage;
        this.cameraClassified = cameraClassified;

    }

    //Set up Getter() and Setter()
    public Bitmap getCameraImage(){
        return cameraImage;
    }

    public void setCameraImage(Bitmap cameraImage) {
        this.cameraImage = cameraImage;
    }

    public String getCameraClassified(){
        return cameraClassified;
    }

    public void setCameraClassified(String cameraClassified) {
        this.cameraClassified = cameraClassified;
    }
}

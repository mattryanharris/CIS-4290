package com.example.pytorchandroid;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CameraAdapter extends ArrayAdapter<CameraItem> {
    private static final String TAG = "CameraAdapter";
    private Context cContext;
    private List<CameraItem> cameraItemList = new ArrayList<>();

    public CameraAdapter (@NonNull Context context, ArrayList<CameraItem> list) {
        super (context, 0, list);
        cContext = context;
        cameraItemList = list;
    }

    //This should adapt the ArrayList to the images in the array_items_list
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){

//        Log.d(TAG, String.valueOf(cameraItemList.size()))
        View view;
        if(convertView == null){
            view = LayoutInflater.from(cContext).inflate(R.layout.array_items_list,parent,false);
        } else {
            view = convertView;
        }

        CameraItem currentItem = cameraItemList.get(position);

        //Sets the image to the imageview_array in the array_items_list
        ImageView image = view.findViewById(R.id.imageview_array);
        image.setImageBitmap(currentItem.getCameraImage());

        image.setRotation(90);

        //Sets the classifier details to the textview_array in the array_items_list
        TextView details = view.findViewById(R.id.textview_array);
        details.setText(currentItem.getCameraClassified());

        //Split by whitespace
        String [] splitDetail = currentItem.getCameraClassified().split("\\s+");
        if (splitDetail[splitDetail.length-1].equalsIgnoreCase("(high)")){
            details.setTextColor(Color.GREEN);
        } else if (splitDetail[splitDetail.length-1].equalsIgnoreCase("(med)")){
            details.setTextColor(Color.);
        } else {
            details.setTextColor(Color.RED);
        }

        Log.d("Camera Adapter", splitDetail[splitDetail.length-1]);

        return view;
    }

}
package com.example.pytorchandroid;

import android.content.Context;
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
    private Context cContext;
    private List<CameraItem> cameraItemList = new ArrayList<>();

    public CameraAdapter (@NonNull Context context, ArrayList<CameraItem> list) {
        super (context, 0, list);
        cContext = context;
        cameraItemList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){

        View listItem = convertView;

        if(listItem == null){
            listItem = LayoutInflater.from(cContext).inflate(R.layout.array_items_list,parent,false);
        }

        CameraItem currentItem = cameraItemList.get(position);

        //Sets the image to the imageview_array in the array_items_list
        ImageView image = listItem.findViewById(R.id.imageview_array);
        image.setImageBitmap(currentItem.getCameraImage());

        //Sets the classifier details to the textview_array in the array_items_list
        TextView details = listItem.findViewById(R.id.textview_array);
        details.setText(currentItem.getCameraClassified());

        return listItem;
    }

}

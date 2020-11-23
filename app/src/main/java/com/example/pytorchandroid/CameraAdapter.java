package com.example.pytorchandroid;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
/*
public class CameraAdapter extends RecyclerView<CameraItem> {
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

        return view;
    }
 */

    public class CameraAdapter extends RecyclerView.Adapter<CameraAdapter.ViewHolder> {
        private List<CameraItem> list;
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView details;
            ImageView imageView;
            ViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.imageview_array);
                details = view.findViewById(R.id.textview_array);
            }
        }
        public interface OnClickListener{
            public void onClick(String text, int position);
        }
        public CameraAdapter(Context context, List<CameraItem> list) {
            this.list = list;
        }
        OnClickListener listener;
        public void setOnItemClickListener(OnClickListener listener){
            this.listener = listener;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.array_items_list, parent, false);
            return new ViewHolder(itemView);
        }
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final CameraItem cameraItem = list.get(position);
            holder.imageView.setImageBitmap(cameraItem.getCameraImage());
            holder.imageView.setRotation(90);

            holder.details.setText(cameraItem.getCameraClassified());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(cameraItem.getCameraClassified(), position);
                }
            });

        }
        @Override
        public int getItemCount() {
            return list.size();
        }
    }

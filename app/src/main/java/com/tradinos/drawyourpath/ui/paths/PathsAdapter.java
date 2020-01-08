package com.tradinos.drawyourpath.ui.paths;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tradinos.drawyourpath.ImageViewActivity;
import com.tradinos.drawyourpath.Models.MyPath;
import com.tradinos.drawyourpath.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

public class PathsAdapter extends RecyclerView.Adapter<PathsAdapter.MyViewHolder> {

    //Callbacks
    private deletePathFromDatabaseCallback mDeletePathFromDatabaseCallback;
    private sharePathWithImageCallback mSharePathWithImageCallback;
    private sendSmsCallback mSendSmsCallback;


    //final attributes
    private final LayoutInflater mInflater;

    private List<MyPath> mPaths;
    private Context mContext;

    public PathsAdapter(Context context){
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mSendSmsCallback = (sendSmsCallback)context;
        mDeletePathFromDatabaseCallback = (deletePathFromDatabaseCallback)context;
        mSharePathWithImageCallback = (sharePathWithImageCallback) context;
    }

    public void setPaths(List<MyPath> paths){
        mPaths = paths;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_paths_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        if(mPaths != null){

            if(mPaths.get(position).getImageBase64() != null) {
                //convert base64 to Bitmap object
                byte[] decodedString1 = Base64.decode(mPaths.get(position).getImageBase64(), Base64.DEFAULT);
                Bitmap decodedByte1 = BitmapFactory.decodeByteArray(decodedString1, 0, decodedString1.length);

                holder.mMapImage.setImageBitmap(decodedByte1);
                holder.mMapImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(mContext,"Position: " + position, Toast.LENGTH_SHORT).show();
                        byte[] decodedString = Base64.decode(mPaths.get(position).getImageBase64(), Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/Share.png";
                        OutputStream out = null;
                        File file=new File(path);
                        try {
                            out = new FileOutputStream(file);
                            decodedByte.compress(Bitmap.CompressFormat.PNG, 100, out);
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Uri photoURI = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", file);

                        Intent intent = new Intent(mContext, ImageViewActivity.class);
                        intent.putExtra("image_url", photoURI.toString());
                        mContext.startActivity(intent);
                    }
                });
            }

            MyPath myPath = mPaths.get(position);
            holder.mFrom.setText(myPath.getFrom());
            holder.mTo.setText(myPath.getTo());
            holder.mDistance.setText(myPath.getDistanceAsString());
            holder.mDuration.setText(myPath.getDuration());

            holder.mSendSMS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSendSmsCallback.sendSmsAction(mPaths.get(position));
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        if(mPaths != null)
            return mPaths.size();
        return 0;
    }



    public class MyViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener {

        TextView mFrom, mTo, mDistance, mDuration;
        Button mSendSMS;
        ImageView mMapImage;

        MyViewHolder(View view) {
            super(view);
            mFrom = view.findViewById(R.id.from_textview);
            mTo = view.findViewById(R.id.to_textview);
            mDistance = view.findViewById(R.id.distance_textview);
            mDuration = view.findViewById(R.id.duration_textview);
            mSendSMS = view.findViewById(R.id.send_sms_button);
            mMapImage = view.findViewById(R.id.map_image);
            mMapImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setOnCreateContextMenuListener(this);

            itemView.setTag(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                        ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle("Select The Action");
            contextMenu.add(this.getAdapterPosition(), 1, 1, "Delete").
                    setOnMenuItemClickListener(onEditMenu);
            contextMenu.add(this.getAdapterPosition(), 2, 2, "Share").
                    setOnMenuItemClickListener(onEditMenu);

        }

        private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case 1:
                        mDeletePathFromDatabaseCallback.deletePathAction(mPaths.get(item.getGroupId()));
                        Toast.makeText(mContext, mPaths.get(item.getGroupId()).getDistanceAsString(), Toast.LENGTH_SHORT).show();
                        break;

                    case 2:
                        mSharePathWithImageCallback.sharePathWithImageAction(mPaths.get(item.getGroupId()));
                        Toast.makeText(mContext, "Share item", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        };

    }


    public interface sendSmsCallback{
        public void sendSmsAction(MyPath myPath);
    }
    public interface deletePathFromDatabaseCallback{
        public void deletePathAction(MyPath myPath);
    }
    public interface sharePathWithImageCallback{
        public void sharePathWithImageAction(MyPath myPath);
    }

}

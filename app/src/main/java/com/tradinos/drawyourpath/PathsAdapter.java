package com.tradinos.drawyourpath;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PathsAdapter extends RecyclerView.Adapter<PathsAdapter.MyViewHolder> {

    private final LayoutInflater mInflater;
    private List<MyPath> mPaths; // Cached copy of words
    private Context mContext;
    private sendSmsCallback mSendSmsCallback;
    private deletePathFromDatabaseCallback mDeletePathFromDatabaseCallback;

    public PathsAdapter(Context context){
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mSendSmsCallback = (sendSmsCallback)context;
        mDeletePathFromDatabaseCallback = (deletePathFromDatabaseCallback)context;
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
            MyPath myPath = mPaths.get(position);
            holder.mFrom.setText(myPath.getFrom());
            holder.mTo.setText(myPath.getTo());
            holder.mDistance.setText(myPath.getDistance());
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

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public TextView mFrom, mTo, mDistance, mDuration;
        public Button mSendSMS;

        //Todo add image view to the adapter

        public MyViewHolder(View view) {
            super(view);
            mFrom = view.findViewById(R.id.from_textview);
            mTo = view.findViewById(R.id.to_textview);
            mDistance = view.findViewById(R.id.distance_textview);
            mDuration = view.findViewById(R.id.duration_textview);
            mSendSMS = view.findViewById(R.id.send_sms_button);
            view.setOnCreateContextMenuListener(this);

        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
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
                        Toast.makeText(mContext, mPaths.get(item.getGroupId()).getDistance(), Toast.LENGTH_SHORT).show();
                        break;

                    case 2:
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

}

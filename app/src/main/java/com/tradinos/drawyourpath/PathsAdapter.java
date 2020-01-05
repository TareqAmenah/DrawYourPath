package com.tradinos.drawyourpath;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PathsAdapter extends RecyclerView.Adapter<PathsAdapter.MyViewHolder> {

    private final LayoutInflater mInflater;
    private List<MyPath> mPaths; // Cached copy of words
    private Context mContext;
    private sendSmsCallback mSendSmsCallback;

    public PathsAdapter(Context context){
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mSendSmsCallback = (sendSmsCallback)context;
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

    public class MyViewHolder extends RecyclerView.ViewHolder {
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

        }
    }

    public interface sendSmsCallback{
        public void sendSmsAction(MyPath myPath);
    }

}

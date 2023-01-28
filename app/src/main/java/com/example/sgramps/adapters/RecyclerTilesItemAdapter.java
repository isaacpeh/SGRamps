package com.example.sgramps.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sgramps.R;
import com.example.sgramps.models.RampsDAO;
import com.example.sgramps.models.RampsModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.List;

public class RecyclerTilesItemAdapter extends RecyclerView.Adapter<RecyclerTilesItemAdapter.ViewHolder> {
    private List<String> mBookmarks;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public RecyclerTilesItemAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mBookmarks = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.tiles_recycleritem, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String bookmarkRamp = mBookmarks.get(position);
        holder.myTextView.setText(bookmarkRamp);
        
        RampsDAO dbRamps = new RampsDAO();
        dbRamps.getRampByName(bookmarkRamp, new RampsDAO.SingleRampCallback() {
            @Override
            public void onCallBack(RampsModel ramp) {
                Picasso.get().load(ramp.getImg_url().get(0)).into(holder.myImageView);
            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mBookmarks.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        ShapeableImageView myImageView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.txtTileTitle);
            myImageView = itemView.findViewById(R.id.txtTileImg);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public String getItem(int id) {
        return mBookmarks.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}

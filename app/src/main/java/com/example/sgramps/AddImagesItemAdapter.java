package com.example.sgramps;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AddImagesItemAdapter extends RecyclerView.Adapter<AddImagesItemAdapter.ViewHolder> {
    ArrayList<Uri> images;
    LayoutInflater mInflater;
    ItemClickListener mClickListener;

    public AddImagesItemAdapter(Context context, ArrayList<Uri> images) {
        this.images = images;
        this.mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public AddImagesItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddImagesItemAdapter.ViewHolder holder, int position) {
        Uri imagesRamp = images.get(position);
        holder.mImageView.setImageURI(imagesRamp);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView mImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.locationImage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getAdapterPosition());
            }
        }

    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public Uri getItem(int position) {
        return images.get(position);
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}

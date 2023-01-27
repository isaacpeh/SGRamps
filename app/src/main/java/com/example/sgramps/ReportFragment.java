package com.example.sgramps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ReportFragment extends Fragment implements AddImagesItemAdapter.ItemClickListener{
    AddImagesItemAdapter adapter;
    RecyclerView mRecyclerView;
    ArrayList<Uri> imagesSource = new ArrayList<>();
    View view;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_report, container, false);
        Uri uri = Uri.parse("android.resource://com.example.sgramps/drawable/camerabtn");


        imagesSource.add(uri);
        showRecyclerView(imagesSource);
        return view;
    }

    //TODO : change to string for URI
    public void showRecyclerView(ArrayList<Uri> data){
        mRecyclerView = view.findViewById(R.id.recycler_view_report);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false));
        adapter = new AddImagesItemAdapter(getActivity(),data);
        Log.d("test","0000000000000000"+data);
        adapter.setClickListener(this);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.d("test",""+position);
        //adapter.getItem(position)
        if(position == 0){
            Log.d("test","aaaaaaaaaa");
            BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
            View bottomSheetView = getLayoutInflater().inflate(R.layout.import_image_popup,null);
            dialog.setContentView(bottomSheetView);
            TextView uploadCamera = dialog.findViewById(R.id.uploadCamera);

            uploadCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 1);
                }
            });

            dialog.show();

        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
        {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Log.d("test",""+photo);

            Uri uri = getImageUri(getActivity(),photo);
            Log.d("test","-----"+uri);

            imagesSource.add(uri);
            adapter.notifyItemInserted(imagesSource.size() - 1);

        }
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
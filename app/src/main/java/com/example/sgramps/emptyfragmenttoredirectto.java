package com.example.sgramps;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link emptyfragmenttoredirectto#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class emptyfragmenttoredirectto extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    TextView textView1;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment emptyfragmenttoredirectto.
     */
    // TODO: Rename and change types and number of parameters
    public static emptyfragmenttoredirectto newInstance(String param1, String param2) {
        emptyfragmenttoredirectto fragment = new emptyfragmenttoredirectto();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public emptyfragmenttoredirectto() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_emptyfragmenttoredirectto, container, false);
        // Change textview1 text
        textView1 = view.findViewById(R.id.textView1);
        FirebaseAuth fAuth = FirebaseAuth.getInstance();

        textView1.setText(fAuth.getCurrentUser().getEmail());
        return view;
    }
}
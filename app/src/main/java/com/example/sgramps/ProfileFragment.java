package com.example.sgramps;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sgramps.models.UserDAO;
import com.example.sgramps.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    String email;
    TextView txtEmail, txtName;
    ImageView imgProfile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.fade));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtName = view.findViewById(R.id.txtName);
        imgProfile = view.findViewById(R.id.imgProfilePic);

        Button btnEdit = view.findViewById(R.id.btnEdit);
        Button btnContributions = view.findViewById(R.id.btnContributions);
        Button btnLogoff = view.findViewById(R.id.btnLogoff);

        btnEdit.setOnClickListener(this);
        btnContributions.setOnClickListener(this);
        btnLogoff.setOnClickListener(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        email = user.getEmail();
        getUser(email);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnEdit:
                getParentFragmentManager().beginTransaction().detach(MainActivity.fragmentEdit).commit();
                getParentFragmentManager().beginTransaction().attach(MainActivity.fragmentEdit).commit();
                getParentFragmentManager().beginTransaction().hide(MainActivity.active).show(MainActivity.fragmentEdit).commit();
                MainActivity.active = MainActivity.fragmentEdit;
                break;
            case R.id.btnContributions:
                getParentFragmentManager().beginTransaction().detach(MainActivity.fragmentContribution).commit();
                getParentFragmentManager().beginTransaction().attach(MainActivity.fragmentContribution).commit();
                getParentFragmentManager().beginTransaction().hide(MainActivity.active).show(MainActivity.fragmentContribution).commit();
                MainActivity.active = MainActivity.fragmentContribution;
                break;
            case R.id.btnLogoff:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), StartUpActivity.class);
                getParentFragmentManager().beginTransaction().detach(MainActivity.fragmentHome).commitNow();
                startActivity(intent);
                getActivity().finish();
                break;
        }
    }

    public void getUser(String email) {
        UserDAO userDb = new UserDAO();
        userDb.getUser(email, new UserDAO.UserCallback() {
            @Override
            public void onCallBack(UserModel user) {
                txtName.setText(user.getName());
                txtEmail.setText(user.getEmail());
                Picasso.get().load(user.getImg_url()).into(imgProfile);
            }
        });
    }
}
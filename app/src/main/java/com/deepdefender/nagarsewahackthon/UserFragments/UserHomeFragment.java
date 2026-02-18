package com.deepdefender.nagarsewahackthon.UserFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.deepdefender.nagarsewahackthon.R;

public class UserHomeFragment extends Fragment {

    CardView cardRegister;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_home, container, false);

        // 🔹 Bind CardView
        cardRegister = view.findViewById(R.id.cardRegister);

        // 🔹 Click Listener → Open RegisterComplaintActivity
        cardRegister.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RegisterComplaintActivity.class);
            startActivity(intent);
        });

        return view;
    }
}

package com.example.braceletapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.*;
import java.util.List;

import androidx.fragment.app.Fragment;

public class SecondFragment extends Fragment {
    TextView connView;
    TextView capView;
    String current = "Scanning…";
    String captured = "Scanned Connections:";
    public SecondFragment(){
        // require a empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    public void update(boolean scanningEnd){
        if (!scanningEnd){
            current = "Scanning…";
        }
        if (getView() != null) {
            connView = (TextView) getView().findViewById(R.id.current_connections);
            connView.setText(current);
            capView = (TextView) getView().findViewById(R.id.available_conn);
            capView.setText(captured);
        }
    }
    @SuppressLint("SetTextI18n")
    public void setData(String name){
        if (getView() != null) {
            current = "currently connected with: " + name;
            connView = (TextView) getView().findViewById(R.id.current_connections);
            connView.setText(current);
        }
    }

    @SuppressLint("SetTextI18n")
    public void scanLog(List<String> names){
        if (getView() != null){
            captured = "Scanned Connections:";
            for (int i = 0; i < names.size(); i++) {
                captured += "\n" + names.get(i);
            }
            capView = (TextView) getView().findViewById(R.id.available_conn);
            capView.setText("");
            capView.append(captured);
        }
    }
}
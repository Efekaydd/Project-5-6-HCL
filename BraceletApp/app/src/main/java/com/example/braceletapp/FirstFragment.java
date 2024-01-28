package com.example.braceletapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.Objects;

public class FirstFragment extends Fragment {
    TextView heartRateView;
    TextView o2View;
    TextView motionView;
    TextView tempView;
    TextView logView;
    int heartRate = 0;
    int o2 = 0;
    int motion = 0;
    int temp = 0;
    String heartRateText = "Heart Rate: Unknown";
    String o2Text = "O2%: Unknown";
    String motionText = "Motion: Unknown";
    String tempText = "Temperature: Unknown";
    String logText = "";
    String logTextPrevious = "";
    public FirstFragment(){
        // require a empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void update(boolean scanningEnd) {
        if (!scanningEnd){
            heartRateText = "Heart Rate: Unknown";
            o2Text = "O2%: Unknown";
            motionText = "Motion: Unknown";
            tempText = "Temperature: Unknown";
        } else {
            heartRateText = "Heart Rate: " + heartRate;
            o2Text = "O2%: " + o2;
            motionText = "Motion: " + motion;
            tempText = "Temperature: " + temp;
        }
        if (getView() != null){
            heartRateView = (TextView) getView().findViewById(R.id.heartRateView);
            heartRateView.setText(heartRateText);
            o2View = (TextView) getView().findViewById(R.id.o2View);
            o2View.setText(o2Text);
            tempView = (TextView) getView().findViewById(R.id.tempView);
            tempView.setText(tempText);
            motionView = (TextView) getView().findViewById(R.id.motionView);
            motionView.setText(motionText);
            if(!Objects.equals(logTextPrevious, logText)){
                logView = (TextView) getView().findViewById(R.id.logView);
                logView.append(logText);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void setData(int heartRate, int o2, int motion, int temp, String log){
        if (getView() != null){
            this.heartRate = heartRate;
            this.o2 = o2;
            this.motion = motion;
            this.temp = temp;
            logTextPrevious = logText;
            logText = log;
        }
    }

    @SuppressLint("SetTextI18n")
    public void Startup(String log){
        if (getView() != null){
            this.logText = log;
            logView = (TextView) getView().findViewById(R.id.logView);
            logView.setText(logText);
        }
    }

}
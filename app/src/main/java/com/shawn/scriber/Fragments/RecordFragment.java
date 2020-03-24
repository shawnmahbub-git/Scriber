package com.shawn.scriber.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shawn.scriber.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment implements View.OnClickListener {


    //components of the fragments
    private NavController navController;
    private ImageButton listBtn;
    private ImageButton recordBtn;
    private TextView recordFileName;

    //flag for saving the state of recording
    private boolean isRecording=false;
    //permission
    private String recordPermission= Manifest.permission.RECORD_AUDIO;
    private static final int PERMISSION_CODE = 21;

    //media recorder
    private MediaRecorder mediaRecorder;
    private String recordFile;

    //chronometer
    private Chronometer chronometer;

    public RecordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController= Navigation.findNavController(view);
        listBtn=view.findViewById(R.id.record_list_btn);
        recordBtn=view.findViewById(R.id.record_btn);
        chronometer=view.findViewById(R.id.record_timer);
        recordFileName=view.findViewById(R.id.record_filename);

        listBtn.setOnClickListener(this);
        recordBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.record_list_btn:
                if (isRecording){
                    AlertDialog.Builder alertDialog=new AlertDialog.Builder(getContext());
                    alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            navController.navigate(R.id.action_recordFragment_to_recordListFragment);
                            isRecording=false;
                        }
                    });
                    alertDialog.setNegativeButton("Cancel", null);
                    alertDialog.setTitle("Audio is recording");
                    alertDialog.setMessage("Are you sure, you want to stop the recording?");
                    alertDialog.create().show();
                }else {
                    navController.navigate(R.id.action_recordFragment_to_recordListFragment);
                }
                break;

            case R.id.record_btn:
                if (isRecording){
                    //stop recording
                    stopRecording();

                    recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped,null));
                    isRecording=false;
                }else {
                    /*if the user permits the audio recording permission then the drawable will be set otherwise not
                    * we've to declare a check permission method for the user permission*/
                    if (checkPermissions()){
                        startRecording();
                        recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_recording,null));
                        isRecording=true;
                    }
                }
                break;
        }
    }

    private void startRecording() {
        //timer
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        Toast.makeText(getActivity(), "Recording...", Toast.LENGTH_SHORT).show();


        String recordPath=getActivity().getExternalFilesDir("/").getAbsolutePath();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault());
        Date now=new Date();
        recordFile="Recording_"+simpleDateFormat.format(now)+".3gp";

        recordFileName.setText("Recording File: "+recordFile);

        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath+"/"+recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        }catch (IOException e){
            e.printStackTrace();
        }
        mediaRecorder.start();
    }

    private void stopRecording() {
        //timer
        chronometer.stop();

        recordFileName.setText("Recorded File: "+recordFile);
        Toast.makeText(getActivity(), "Recording Saved", Toast.LENGTH_SHORT).show();

        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder=null;
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(getContext(),recordPermission)== PackageManager.PERMISSION_GRANTED){
            return true;
        }else {
            ActivityCompat.requestPermissions(getActivity(),new String []{recordPermission},PERMISSION_CODE);
            return false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isRecording){
            stopRecording();
        }
    }
}

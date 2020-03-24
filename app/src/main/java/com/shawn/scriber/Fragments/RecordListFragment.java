package com.shawn.scriber.Fragments;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.shawn.scriber.Adapter.AudioListAdapter;
import com.shawn.scriber.R;

import java.io.File;
import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordListFragment extends Fragment implements AudioListAdapter.onItemListClick{

    //component of the fragments
    private ConstraintLayout playerSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private RecyclerView audioList;

    //array for all recorded audio files
    private File[] allFiles;
    private AudioListAdapter audioListAdapter;

    private MediaPlayer mediaPlayer=null;
    private boolean isPlaying=false;

    private File fileToPlay;

    //ui elements of player sheet
    private ImageButton playBtn;
    private TextView playerHeader,playerFileName;
    private SeekBar seekBar;
    //to handle the seekBar in real time
    private Handler seekBarHandler;
    private Runnable updateSeekBar;

    public RecordListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //finding the components
        audioList=view.findViewById(R.id.audio_list_view);
        playerSheet=view.findViewById(R.id.player_sheet);
        bottomSheetBehavior=BottomSheetBehavior.from(playerSheet);

        String path=getActivity().getExternalFilesDir("/").getAbsolutePath();
        File directory=new File(path);
        allFiles=directory.listFiles();

        audioListAdapter=new AudioListAdapter(allFiles,this);
        audioList.setHasFixedSize(true);
        audioList.setLayoutManager(new LinearLayoutManager(getContext()));
        audioList.setAdapter(audioListAdapter);

        playBtn=view.findViewById(R.id.player_play_btn);
        playerHeader=view.findViewById(R.id.player_header_title);
        playerFileName=view.findViewById(R.id.player_fileName);
        seekBar=view.findViewById(R.id.player_seekBar);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState==BottomSheetBehavior.STATE_HIDDEN){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //we will do nothing in this method for this app
            }
        });

        //play button click listener
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying){
                    pauseAudio();
                }else {
                    if (fileToPlay==null){
                        resumeAudio();
                    }
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (fileToPlay !=null){
                    pauseAudio();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (fileToPlay !=null){
                    int progress=seekBar.getProgress();
                    mediaPlayer.seekTo(progress);
                    resumeAudio();
                }
            }
        });
    }

    @Override
    public void onClickListener(File file, int position) {
        fileToPlay=file;

        if (isPlaying){
            stopAudio();
            playAudio(fileToPlay);
        }else {
            playAudio(fileToPlay);
        }
    }

    private void pauseAudio(){
        mediaPlayer.pause();
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_play_btn, null));
        isPlaying=false;
        seekBarHandler.removeCallbacks(updateSeekBar);
    }

    private void resumeAudio(){
        mediaPlayer.start();
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_pause_btn, null));
        isPlaying=true;
        updateRunnable();
        seekBarHandler.postDelayed(updateSeekBar,0);
    }

    private void stopAudio() {
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_play_btn, null));
        playerHeader.setText("Stopped");
        isPlaying=false;
        mediaPlayer.stop();
        seekBarHandler.removeCallbacks(updateSeekBar);
    }

    private void playAudio(File fileToPlay) {
        mediaPlayer=new MediaPlayer();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_pause_btn, null));
        playerFileName.setText(fileToPlay.getName());
        playerHeader.setText("Playing");
        isPlaying=true;

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopAudio();
                playerHeader.setText("Finished");
            }
        });

        seekBar.setMax(mediaPlayer.getDuration());
        seekBarHandler=new Handler();
        updateRunnable();
        seekBarHandler.postDelayed(updateSeekBar,0);
    }

    private void updateRunnable() {
        updateSeekBar=new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                seekBarHandler.postDelayed(this,500);
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isPlaying){
            stopAudio();
        }
    }
}

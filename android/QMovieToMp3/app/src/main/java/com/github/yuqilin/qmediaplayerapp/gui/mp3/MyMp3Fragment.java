package com.github.yuqilin.qmediaplayerapp.gui.mp3;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.github.yuqilin.qmediaplayerapp.BaseFragment;
import com.github.yuqilin.qmediaplayerapp.IAudioEventHandler;
import com.github.yuqilin.qmediaplayerapp.IEventsHandler;
import com.github.yuqilin.qmediaplayerapp.R;
import com.github.yuqilin.qmediaplayerapp.gui.video.VideoListAdapter;
import com.github.yuqilin.qmediaplayerapp.gui.view.AutoFitRecyclerView;
import com.github.yuqilin.qmediaplayerapp.media.AudioLoader;
import com.github.yuqilin.qmediaplayerapp.media.AudioWrapter;
import com.github.yuqilin.qmediaplayerapp.media.VideoWrapper;

import java.util.ArrayList;

/**
 * Created by liwenfeng on 17/3/26.
 */

public class MyMp3Fragment extends BaseFragment implements IAudioEventHandler, AudioLoader.AudioLoaderListener{
    private static final String PAGE_TITLE = "MY MP3";
    public static final String TAG = "AudioFragment";

    protected AutoFitRecyclerView mGridView;
    private AudioListAdapter mAudioAdapter;
    private AudioLoader mAudioLoader;

    public static final int SCAN_START = 1;
    public static final int SCAN_FINISH = 2;
    public static final int SCAN_CANCEL = 3;
    public static final int SCAN_ADD_ITEM = 4;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_START:
                    mAudioLoader.scanStart();
                    break;
                case SCAN_FINISH:
                    mAudioAdapter.updateAudios(mAudioLoader.getAudios());
                    break;
                case SCAN_CANCEL:
                    break;
                case SCAN_ADD_ITEM:
                    mAudioAdapter.addAudio(msg.arg1, (AudioWrapter)msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_mp3;
    }

    @Override
    protected String getPageTitle() {
        return PAGE_TITLE;
    }

    protected void initView(View view, Bundle savedInstanceState) {
        mAudioLoader = new AudioLoader(this);
        mGridView = (AutoFitRecyclerView) view.findViewById(R.id.audio_grid);
        mAudioAdapter = new AudioListAdapter(this);

        mGridView.setAdapter(mAudioAdapter);

        mHandler.sendEmptyMessage(SCAN_START);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();
        updateViewMode();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioAdapter.clear();
    }

    public void notifyDataChanged() {
        if (!isVisible()) {
            Log.d(TAG, "notifyDataChanged but not visible");
            return;
        }
        if (mAudioAdapter != null) {
            mAudioAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v, int position, AudioWrapter item) {

    }

    @Override
    public boolean onLongClick(View v, int position, AudioWrapter item) {
        return false;
    }

    private void updateViewMode() {
        if (getView() == null || getActivity() == null) {
            Log.w(TAG, "Unable to setup the view");
            return;
        }

        Resources res = getResources();
        boolean listMode = res.getBoolean(R.bool.list_mode);
        listMode |= res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT &&
                PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("force_list_portrait", false);
        // Compute the left/right padding dynamically
        DisplayMetrics outMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(outMetrics);

        // Select between grid or list
        if (!listMode) {
            int thumbnailWidth = res.getDimensionPixelSize(R.dimen.grid_card_thumb_width);
            mGridView.setColumnWidth(mGridView.getPerfectColumnWidth(thumbnailWidth, res.getDimensionPixelSize(R.dimen.default_margin)));
            mAudioAdapter.setGridCardWidth(mGridView.getColumnWidth());
        }
        mGridView.setNumColumns(listMode ? 1 : -1);
        if (mAudioAdapter.isListMode() != listMode) {
            mAudioAdapter.setListMode(listMode);
        }
    }

    @Override
    public void onLoadItem(int position, AudioWrapter audio) {

    }

    @Override
    public void onLoadCompleted(ArrayList<AudioWrapter> audios) {
        mHandler.sendEmptyMessage(SCAN_FINISH);
    }
}
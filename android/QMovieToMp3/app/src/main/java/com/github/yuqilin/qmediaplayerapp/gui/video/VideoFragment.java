package com.github.yuqilin.qmediaplayerapp.gui.video;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.github.yuqilin.qmediaplayerapp.BaseFragment;
import com.github.yuqilin.qmediaplayerapp.IEventsHandler;
import com.github.yuqilin.qmediaplayerapp.R;
import com.github.yuqilin.qmediaplayerapp.VideoPlayerActivity;
import com.github.yuqilin.qmediaplayerapp.gui.tasks.TaskFragment;
import com.github.yuqilin.qmediaplayerapp.gui.view.AutoFitRecyclerView;
import com.github.yuqilin.qmediaplayerapp.media.MediaTask;
import com.github.yuqilin.qmediaplayerapp.media.VideoWrapper;
import com.github.yuqilin.qmediaplayerapp.media.VideoLoader;

import java.util.ArrayList;

/**
 * Created by yuqilin on 17/2/11.
 */

public class VideoFragment extends BaseFragment implements IEventsHandler, VideoLoader.VideoLoaderListener {
    public static final String TAG = "VideoFragment";

    private static final String PAGE_TITLE = "VIDEOS";

    public static final int SCAN_START = 1;
    public static final int SCAN_FINISH = 2;
    public static final int SCAN_CANCEL = 3;
    public static final int SCAN_ADD_ITEM = 4;

    protected AutoFitRecyclerView mGridView;
    private VideoListAdapter mVideoAdapter;

    private VideoLoader mVideoLoader;
    private TaskFragment taskFragment = null;

    public void setTaskFragment(TaskFragment taskFragment) {
        this.taskFragment = taskFragment;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_START:
                    mVideoLoader.scanStart();
                    break;
                case SCAN_FINISH:
                    mVideoAdapter.updateVideos(mVideoLoader.getVideos());
                    break;
                case SCAN_CANCEL:
                    break;
                case SCAN_ADD_ITEM:
                    mVideoAdapter.addVideo(msg.arg1, (VideoWrapper)msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        mVideoLoader = new VideoLoader(this);

        mGridView = (AutoFitRecyclerView) view.findViewById(R.id.video_grid);
//        mGridView.setHasFixedSize(true);

        mVideoAdapter = new VideoListAdapter(this);

        mGridView.setAdapter(mVideoAdapter);

        mHandler.sendEmptyMessage(SCAN_START);
    }

    @Override
    public void onStart() {
        super.onStart();
//        mFabPlay.setImageResource(R.drawable.ic_fab_play);
//        registerForContextMenu(mGridView);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        updateViewMode();
//        if (mMediaLibrary.isInitiated())
//            fillView();
//        else
//            setupMediaLibraryReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
//        unregisterForContextMenu(mGridView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putString(KEY_GROUP, mGroup);
    }

    @Override
    public void onDestroyView() {
//        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(messageReceiverVideoListFragment);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVideoAdapter.clear();
    }

    public void notifyDataChanged() {
        if (!isVisible()) {
            Log.d(TAG, "notifyDataChanged but not visible");
            return;
        }
        if (mVideoAdapter != null) {
            mVideoAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_video;
    }

    @Override
    protected String getPageTitle() {
        return PAGE_TITLE;
    }

    @Override
    public void onClick(View v, int position, VideoWrapper item) {
        jumpToPlayerActivity(item.filePath, item.duration);
    }

    @Override
    public boolean onLongClick(View v, int position, VideoWrapper item) {
        return false;
    }

    @Override
    public void onCtxClick(View v, int position, VideoWrapper item) {

    }

    @Override
    public void onUpdateFinished(RecyclerView.Adapter adapter) {

    }

    private void jumpToPlayerActivity(String videoPath, String duration) {
        Intent intent = new Intent(getContext(), VideoPlayerActivity.class);
        intent.putExtra("videoPath", videoPath);
        intent.putExtra("duration", duration);
        startActivityForResult(intent, 1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String videopath = data.getExtras().getString("videoPath");
        boolean vbr = data.getExtras().getBoolean("vbr",false);
        String type = data.getExtras().getString("type");
        String bits = data.getExtras().getString("bits");
        long duration = data.getExtras().getLong("duration");

//        TaskFragment taskFragment =(TaskFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.task_grid);
        if(taskFragment != null) {
            taskFragment.addTask(new MediaTask(videopath, vbr, type, bits,(int) duration));
        }

//        if (requestCode == 1 && resultCode == -1) {
//            getActivity().getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.task_grid, taskFragment, null)
//                    .addToBackStack(null)
//                    .commit();
//        }
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
            mVideoAdapter.setGridCardWidth(mGridView.getColumnWidth());
        }
        mGridView.setNumColumns(listMode ? 1 : -1);
        if (mVideoAdapter.isListMode() != listMode) {
//            if (listMode)
//                mGridView.addItemDecoration(mDividerItemDecoration);
//            else
//                mGridView.removeItemDecoration(mDividerItemDecoration);
            mVideoAdapter.setListMode(listMode);
        }
    }

    @Override
    public void onLoadItem(int position, VideoWrapper video) {
//        mHandler.sendMessage(Message.obtain(mHandler, SCAN_ADD_ITEM, position, 0, video));
    }

    @Override
    public void onLoadCompleted(ArrayList<VideoWrapper> videos) {
        mHandler.sendEmptyMessage(SCAN_FINISH);
    }

}

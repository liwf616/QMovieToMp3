//package com.github.yuqilin.qmediaplayerapp;
//
//import android.annotation.TargetApi;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.ActivityInfo;
//import android.content.res.Configuration;
//import android.media.AudioManager;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//import android.preference.PreferenceManager;
//import android.provider.Settings;
//import android.support.v7.app.AppCompatActivity;
//import android.text.format.DateFormat;
//import android.util.Log;
//import android.view.InputDevice;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.ImageView;
//import android.widget.Spinner;
//import android.widget.TextView;
//
//import com.github.yuqilin.qmediaplayer.IMediaController;
//import com.github.yuqilin.qmediaplayer.QMediaPlayerVideoView;
//import com.github.yuqilin.qmediaplayerapp.gui.view.RangeSeekBar;
//import com.github.yuqilin.qmediaplayerapp.util.AndroidDevices;
//import com.github.yuqilin.qmediaplayerapp.util.Permissions;
//import com.github.yuqilin.qmediaplayerapp.util.Util;
//
//import life.knowledge4.videotrimmer.utils.MediaInfo;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.Locale;
//
///**
// * Created by yuqilin on 17/1/6.
// */
//
//public class VideoPlayerActivity extends AppCompatActivity implements IMediaController {
//
//    private static final String TAG = "VideoPlayerActivity";
//
//    private static final int FADE_OUT = 1;
//    private static final int SHOW_PROGRESS = 2;
//    private static final int UPDATE_SYSTIME = 3;
//
//    private static final int DEFAULT_TIMEOUT = 3000;
//    private static final int SEEK_TO_POST_DELAY_MILLIS = 200;
//    private static final int FAST_REWIND_STEP = 5000;   // milliseconds
//    private static final int FAST_FORWARD_STEP = 15000; // milliseconds
//
//    private String mVideoPath;
//    private QMediaPlayerVideoView mVideoView;
//    private IMediaController.MediaPlayerControl mMediaPlayerControl;
//
//    // title view
//    View mTitleView;
//    ImageView mBack;
//    TextView mTitle;
//    ImageView mBattery;
//    TextView mSysTime;
//    ImageView mShowMore;
//
//    // bottom view
//    View mBottomView;
//    TextView mCurrentTime;
//    TextView mCenterTime;
//    TextView mTotalTime;
//    RangeSeekBar mSeekBar;
//    ImageView mPlayPause;
//
//    // center tools view
//    ImageView mTakeGif;
//    ImageView mLockCenter;
//
//    private AudioManager mAudioManager;
//    private int mAudioMax;
//    private boolean mMute = false;
//    private int mVolSave;
//    private float mVol;
//
//    private boolean mShowing;
//    private boolean mDragging;
//    private long mDuration;
//    private boolean mInstantSeeking = true;
//    private Runnable mLastSeekBarRunnable;
//    private boolean mDisableProgress = false;
//    private boolean mIsLocked = false;
//    private boolean mIsLoading;
//
//    //stick event
//    private static final int JOYSTICK_INPUT_DELAY = 300;
//    private long mLastMove;
//
//    //Touch Events
//    private static final int TOUCH_NONE = 0;
//    private static final int TOUCH_VOLUME = 1;
//    private static final int TOUCH_BRIGHTNESS = 2;
//    private static final int TOUCH_SEEK = 3;
//    private int mTouchAction = TOUCH_NONE;
//    private int mSurfaceYDisplayRange;
//    private float mInitTouchY, mTouchY =-1f, mTouchX=-1f;
//
//    // Brightness
//    private boolean mIsFirstBrightnessGesture = true;
//    private float mRestoreAutoBrightness = -1f;
//
//    private SharedPreferences mSettings;
//
//    //spinner
//    private ArrayList<String> mTypeArray;
//    private Spinner mTypeSpinner;
//    private ArrayList<String> mBitsArray;
//    private Spinner mBitsSpinner;
//
//    private int mVBR = 0;
//    private String mBitrateSelected;
//    private String mTypeSelected;
//
//    //
//    ImageView mConvertButton;
//
//    //
//    private int mStartPos;
//    private int mEndPos;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_video_player);
//
//        initView();
//
//        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
//
//        mVideoPath = getIntent().getStringExtra("videoPath");
//        mDuration = Integer.parseInt(getIntent().getStringExtra("duration"));
//        mVideoView.setVideoPath(mVideoPath);
//        mVideoView.start();
//
//        mTitle.setText(getFileName(mVideoPath));
//
//        File file = new File(mVideoPath);
//        if (!file.exists()) {
//            Log.w(TAG, "File not exitsts or can not read");
//        }
//
//        mSeekBar = (RangeSeekBar) findViewById(R.id.view_player_seekbar);
//        mSeekBar.setRange(0, mDuration / 1000);
//        mSeekBar.setOnRangeChangedListener(mSeekListener);
//
//        mStartPos = 0;
//        mEndPos = (int) mDuration / 1000;
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        mVideoView.pause();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mVideoView.resume();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
////        mVideoView.stopPlayback();
//        mVideoView.pause();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mVideoView.stopPlayback();
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration configuration) {
//        super.onConfigurationChanged(configuration);
//
//        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
////            Toast.makeText(MainActivity.this, "竖屏模式", 3000).show();
//        } else {
////            Toast.makeText(MainActivity.this, "横屏模式", 3000).show();
//        }
//
//    }
//
//    private String getFileName(String filePath) {
//        return filePath.substring(filePath.lastIndexOf('/')+1, filePath.lastIndexOf('.'));
//    }
//    private void updateSysTime() {
//        if (mSysTime != null) {
//            mSysTime.setText(DateFormat.getTimeFormat(this).format(new Date(System.currentTimeMillis())));
//        }
//    }
//
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    private void hideStatusBar() {
//        int systemUiVisibility = 0;
//        if (Build.VERSION.SDK_INT >= 14) {
//            systemUiVisibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        }
//
//        if (Build.VERSION.SDK_INT >= 16) {
//            systemUiVisibility |= View.SYSTEM_UI_FLAG_FULLSCREEN;
//            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
//            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
//        }
//
//        if (Build.VERSION.SDK_INT >= 18) {
//            systemUiVisibility |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//        }
//
//        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
//            @Override
//            public void onSystemUiVisibilityChange(int i) {
//                Log.i(TAG, "onSystemUiVisibilityChange, i=" + i);
//            }
//        });
//
//        getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
//    }
//
//    private void initView() {
//        mVideoView = (QMediaPlayerVideoView) findViewById(R.id.player_video_view);
//
//        // title view
//        mTitleView = findViewById(R.id.player_title_view);
//        mBack = (ImageView) findViewById(R.id.view_player_back);
//        mTitle = (TextView) findViewById(R.id.view_player_title);
//        mBattery = (ImageView) findViewById(R.id.view_player_battery);
//        mSysTime = (TextView) findViewById(R.id.view_player_systime);
//        mShowMore = (ImageView) findViewById(R.id.view_player_more);
//
//        // bottom view
//        mBottomView = findViewById(R.id.player_bottom_view);
//        mCurrentTime = (TextView) findViewById(R.id.view_player_current_time);
//        mCenterTime = (TextView) findViewById(R.id.view_player_center_time);
//        mTotalTime = (TextView) findViewById(R.id.view_player_total_time);
//        mSeekBar = (RangeSeekBar) findViewById(R.id.view_player_seekbar);
//        mPlayPause = (ImageView) findViewById(R.id.view_player_play_pause);
//
//        // center tools view
//        mLockCenter = (ImageView) findViewById(R.id.view_player_lock_center);
//
//        //transcode
//        mConvertButton = (ImageView) findViewById(R.id.view_player_convert_button);
//
//        //spinner
//        mTypeArray = new ArrayList<String>();
//
//        for (String format: MediaInfo.MEDIA_AUDIO_FORMAT) {
//            mTypeArray.add(format.toUpperCase());
//        }
//
//        mBitsArray = new ArrayList<String>();
//
//        for (int i = 0; i < MediaInfo.MEDIA_AAC_BITS.length; i++) {
//            mBitsArray.add(MediaInfo.getAACComment(i));
//        }
//
//        ArrayAdapter<String> typeAdapter =
//                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mTypeArray);
//        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        mTypeSpinner = (Spinner) findViewById(R.id.view_choose_format);
//        mTypeSpinner.setAdapter(typeAdapter);
//        mTypeSpinner.setSelection(0);
//
//        ArrayAdapter<String> bitsAdapter =
//                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mBitsArray);
//        bitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        mBitsSpinner = (Spinner) findViewById(R.id.view_choose_bit);
//        mBitsSpinner.setAdapter(bitsAdapter);
//        mBitsSpinner.setSelection(1);
//
//        mBack.setOnClickListener(mBackListener);
//        mShowMore.setOnClickListener(mShowMoreListener);
//        mPlayPause.setOnClickListener(mPlayPauseListener);
//        mLockCenter.setOnClickListener(mLockScreenListener);
//        mTypeSpinner.setOnItemSelectedListener(mOnSelectTypeListener);
//        mBitsSpinner.setOnItemSelectedListener(mOnSelectBitsListener);
//        mConvertButton.setOnClickListener(mStartConvertListener);
//
////        mSeekBar.setThumbOffset(1);
////        mSeekBar.setMax(1000);
////        mSeekBar.setEnabled(!mDisableProgress);
//
//        mVideoView.setMediaController(this);
//
//        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//
//        mHandler.sendEmptyMessage(UPDATE_SYSTIME);
//    }
//
//    @Override
//    public void setMediaPlayer(MediaPlayerControl playerControl) {
//        mMediaPlayerControl = playerControl;
//    }
//
//    @Override
//    public void show() {
//        show(0);
//    }
//
//    @Override
//    public void show(int timeout) {
//
//        if (!mShowing || mIsLocked) {
//            if (mPlayPause != null)
//                mPlayPause.requestFocus();
//            showOverlay();
//            mShowing = true;
//        }
//
//        if (!mIsLocked) {
//            updatePlayPause();
//            mHandler.sendEmptyMessage(SHOW_PROGRESS);
//        }
//
//        if (timeout != 0) {
//            mHandler.removeMessages(FADE_OUT);
//            mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT),
//                    timeout);
//        }
//    }
//
//    @Override
//    public void hide() {
////        if (mShowing || mIsLocked) {
////            mHandler.removeMessages(FADE_OUT);
////            mHandler.removeMessages(SHOW_PROGRESS);
////            hideOverlay();
////            mShowing = false;
////        }
//    }
//
//    @Override
//    public boolean isShowing() {
//        return mShowing;
//    }
//
//    @Override
//    public void setEnabled(boolean enabled) {
//
//    }
//
//    @Override
//    public void setAnchorView(View anchorView) {
//
//    }
//
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
//    public boolean dispatchGenericMotionEvent(MotionEvent event){
//        if (mIsLoading)
//            return  false;
//        //Check for a joystick event
//        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) !=
//                InputDevice.SOURCE_JOYSTICK ||
//                event.getAction() != MotionEvent.ACTION_MOVE)
//            return false;
//
//        InputDevice mInputDevice = event.getDevice();
//
//        float dpadx = event.getAxisValue(MotionEvent.AXIS_HAT_X);
//        float dpady = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
//        if (mInputDevice == null || Math.abs(dpadx) == 1.0f || Math.abs(dpady) == 1.0f)
//            return false;
//
//        float x = AndroidDevices.getCenteredAxis(event, mInputDevice,
//                MotionEvent.AXIS_X);
//        float y = AndroidDevices.getCenteredAxis(event, mInputDevice,
//                MotionEvent.AXIS_Y);
//        float rz = AndroidDevices.getCenteredAxis(event, mInputDevice,
//                MotionEvent.AXIS_RZ);
//
//        if (System.currentTimeMillis() - mLastMove > JOYSTICK_INPUT_DELAY){
//            if (Math.abs(x) > 0.3){
//                seekDelta(x > 0.0f ? 10000 : -10000);
//            } else if (Math.abs(y) > 0.3){
//                if (mIsFirstBrightnessGesture)
//                    initBrightnessTouch();
//                changeBrightness(-y / 10f);
//            } else if (Math.abs(rz) > 0.3){
//                mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                int delta = -(int) ((rz / 7) * mAudioMax);
//                int vol = (int) Math.min(Math.max(mVol + delta, 0), mAudioMax);
//                setAudioVolume(vol);
//            }
//            mLastMove = System.currentTimeMillis();
//        }
//        return true;
//    }
//
//    private void restoreBrightness() {
//        if (mRestoreAutoBrightness != -1f) {
//            int brightness = (int) (mRestoreAutoBrightness*255f);
//            Settings.System.putInt(getContentResolver(),
//                    Settings.System.SCREEN_BRIGHTNESS,
//                    brightness);
//            Settings.System.putInt(getContentResolver(),
//                    Settings.System.SCREEN_BRIGHTNESS_MODE,
//                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
//        }
//        // Save brightness if user wants to
//        if (mSettings.getBoolean("save_brightness", false)) {
//            float brightness = getWindow().getAttributes().screenBrightness;
//            if (brightness != -1f) {
//                SharedPreferences.Editor editor = mSettings.edit();
//                editor.putFloat("brightness_value", brightness);
//                Util.commitPreferences(editor);
//            }
//        }
//    }
//
//    private void initBrightnessTouch() {
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        float brightnesstemp = lp.screenBrightness != -1f ? lp.screenBrightness : 0.6f;
//        // Initialize the layoutParams screen brightness
//        try {
//            if (Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
//                if (!Permissions.canWriteSettings(this)) {
//                    Permissions.checkWriteSettingsPermission(this, Permissions.PERMISSION_SYSTEM_BRIGHTNESS);
//                    return;
//                }
//                Settings.System.putInt(getContentResolver(),
//                        Settings.System.SCREEN_BRIGHTNESS_MODE,
//                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
//                mRestoreAutoBrightness = android.provider.Settings.System.getInt(getContentResolver(),
//                        android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
//            } else if (brightnesstemp == 0.6f) {
//                brightnesstemp = android.provider.Settings.System.getInt(getContentResolver(),
//                        android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
//            }
//        } catch (Settings.SettingNotFoundException e) {
//            e.printStackTrace();
//        }
//        lp.screenBrightness = brightnesstemp;
//        getWindow().setAttributes(lp);
//        mIsFirstBrightnessGesture = false;
//    }
//
//    private void doBrightnessTouch(float y_changed) {
//        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_BRIGHTNESS)
//            return;
//        if (mIsFirstBrightnessGesture) initBrightnessTouch();
//        mTouchAction = TOUCH_BRIGHTNESS;
//
//        // Set delta : 2f is arbitrary for now, it possibly will change in the future
//        float delta = - y_changed / mSurfaceYDisplayRange;
//
//        changeBrightness(delta);
//    }
//
//    private void doVolumeTouch(float y_changed) {
//        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_VOLUME)
//            return;
//        float delta = - ((y_changed / mSurfaceYDisplayRange) * mAudioMax);
//        mVol += delta;
//        int vol = (int) Math.min(Math.max(mVol, 0), mAudioMax);
//        if (delta != 0f) {
//            setAudioVolume(vol);
//        }
//    }
//
//    private void setAudioVolume(int vol) {
//        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
//
//        /* Since android 4.3, the safe volume warning dialog is displayed only with the FLAG_SHOW_UI flag.
//         * We don't want to always show the default UI volume, so show it only when volume is not set. */
//        int newVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//        if (vol != newVol)
//            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_SHOW_UI);
//
//        mTouchAction = TOUCH_VOLUME;
//        vol = vol * 100 / mAudioMax;
//    }
//
//    private void mute(boolean mute) {
//        mMute = mute;
//    }
//
//    private void updateMute () {
//        mute(!mMute);
//    }
//
//    private void changeBrightness(float delta) {
//        // Estimate and adjust Brightness
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        float brightness =  Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1f);
//        setWindowBrightness(brightness);
//        brightness = Math.round(brightness * 100);
//    }
//
//    private void setWindowBrightness(float brightness) {
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.screenBrightness =  brightness;
//        // Set Brightness
//        getWindow().setAttributes(lp);
//    }
//
//    private void seekDelta(int delta) {
//        // unseekable stream
////        if(mVideoView.getDuration() <= 0 || !mVideoView.isSeekable()) return;
//
//        long position = getTime() + delta;
//        if (position < 0) position = 0;
//        mVideoView.seekTo(position);
////        showInfo(Strings.millisToString(mService.getTime())+"/"+Strings.millisToString(mService.getLength()), 1000);
//    }
//
//    private long getTime() {
//        return 0;
//    }
//
//    private Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message message) {
//            long pos;
//            switch (message.what) {
//                case FADE_OUT:
//                    hide();
//                    break;
//                case SHOW_PROGRESS:
//                    pos = setProgress();
//                    if (!mDragging && mShowing) {
//                        message = mHandler.obtainMessage(SHOW_PROGRESS);
//                        mHandler.sendMessageDelayed(message, 1000 - (pos % 1000));
//                        updatePlayPause();
//                    }
//                    break;
//                case UPDATE_SYSTIME:
//                    updateSysTime();
//                    message = mHandler.obtainMessage(UPDATE_SYSTIME);
//                    mHandler.sendMessageDelayed(message, 1000);
//                    break;
//            }
//            return true;
//        }
//    });
//
//    private RangeSeekBar.OnRangeChangedListener mSeekListener = new RangeSeekBar.OnRangeChangedListener() {
//        @Override
//        public void onRangeChanged(RangeSeekBar view, float min, float max, boolean fromUser) {
//            if (!fromUser)
//                return;
//
//            mEndPos = (int) max;
//
//            final long newCenterPosition = (long) (mEndPos * 1000);
//            String centertime = Util.generateTime(newCenterPosition);
//            if(mCenterTime != null) {
//                mCenterTime.setText(centertime);
//            }
//
//            if(mStartPos == (int) min) {
//                return;
//            }
//
//            Log.i(TAG, "mStartPos:" + mStartPos + "mEndPos:" + mEndPos);
//
//            mStartPos =(int) min;
//
//            final long newPosition = (long) (mStartPos * 1000);
//            String time = Util.generateTime(newPosition);
//
//            if (mInstantSeeking) {
//                mHandler.removeCallbacks(mLastSeekBarRunnable);
//                mLastSeekBarRunnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        mMediaPlayerControl.seekTo(newPosition);
//                    }
//                };
//                mHandler.postDelayed(mLastSeekBarRunnable, SEEK_TO_POST_DELAY_MILLIS);
//            }
//            if (mCurrentTime != null)
//                mCurrentTime.setText(time);
//        }
//
////        @Override
////        public void onStartTrackingTouch(SeekBar seekBar) {
////            mDragging = true;
////            show(3600000);
////            mHandler.removeMessages(SHOW_PROGRESS);
////            if (mInstantSeeking)
////                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
////        }
////
////        @Override
////        public void onStopTrackingTouch(SeekBar seekBar) {
////            if (!mInstantSeeking)
////                mMediaPlayerControl.seekTo(mDuration * seekBar.getProgress() / 1000);
////
////            show(DEFAULT_TIMEOUT);
////            mHandler.removeMessages(SHOW_PROGRESS);
////            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
////            mDragging = false;
////            mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 1000);
////        }
//    };
//
//    private View.OnClickListener mRewindListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            long pos = mMediaPlayerControl.getCurrentPosition();
//            pos -= FAST_REWIND_STEP;
//            mMediaPlayerControl.seekTo(pos);
//            setProgress();
//
//            show(DEFAULT_TIMEOUT);
//        }
//    };
//
//    private View.OnClickListener mForwardListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            long pos = mMediaPlayerControl.getCurrentPosition();
//            pos += FAST_FORWARD_STEP;
//            mMediaPlayerControl.seekTo(pos);
//            setProgress();
//
//            show(DEFAULT_TIMEOUT);
//        }
//    };
//
//    private View.OnClickListener mPlayPauseListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            doPauseResume();
////            show(DEFAULT_TIMEOUT);
//        }
//    };
//
//    private View.OnClickListener mDisplayRatioListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            mVideoView.toggleAspectRatio();
//        }
//    };
//
//    private View.OnClickListener mRotateScreenListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            rotateScreen();
//        }
//    };
//
//    private View.OnClickListener mBackListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            goBack();
//        }
//    };
//
//    private View.OnClickListener mShowMoreListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            showMore();
//        }
//    };
//
//    private View.OnClickListener mLockScreenListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            if (mIsLocked) {
//                unlockScreen();
//            } else {
//                lockScreen();
//            }
//        }
//    };
//
//    private View.OnClickListener mFloatScreenListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            floatScreen();
//        }
//    };
//
//    private View.OnClickListener mStartConvertListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            Log.i(TAG, "start convert");
//
//            Intent intent = new Intent();
//            intent.putExtra("videoPath", mVideoPath);
//            intent.putExtra("vbr", mVBR);
//            intent.putExtra("type", mTypeSelected);
//            intent.putExtra("bits", mBitrateSelected);
//            intent.putExtra("duration", mDuration);
//            intent.putExtra("startTime", mStartPos * 1000);
//            intent.putExtra("endTime", mEndPos * 1000);
//
//            setResult(RESULT_OK, intent);
//            finish();
//        }
//    };
//
//    private AdapterView.OnItemSelectedListener mOnSelectTypeListener = new AdapterView.OnItemSelectedListener () {
//        @Override
//        public void onItemSelected(AdapterView parent, View v, int position, long id) {
//            mTypeSelected = MediaInfo.MEDIA_AUDIO_FORMAT[position];
//        }
//
//        @Override
//        public void onNothingSelected(AdapterView parent) {
//        }
//    };
//
//    private AdapterView.OnItemSelectedListener mOnSelectBitsListener = new AdapterView.OnItemSelectedListener () {
//        @Override
//        public void onItemSelected(AdapterView parent, View v, int position, long id) {
//            mBitrateSelected = MediaInfo.MEDIA_AAC_BITS[position];
//            if( 7 >= position  && position >= 5) {
//                mVBR = position - 2;
//            }
//        }
//
//        @Override
//        public void onNothingSelected(AdapterView parent) {
//        }
//    };
//
//
//    private void updatePlayPause() {
//        if (mPlayPause == null)
//            return;
//
//        if (mMediaPlayerControl.isPlaying()) {
//            mPlayPause.setImageResource(R.drawable.ic_pause);
//        } else {
//            mPlayPause.setImageResource(R.drawable.ic_play);
//        }
//    }
//
//    private void doPauseResume() {
//        updatePlayPause();
//        if (mMediaPlayerControl.isPlaying())
//            mMediaPlayerControl.pause();
//        else
//            mMediaPlayerControl.start();
//    }
//
//    private void doPause() {
//        if (mPlayPause == null)
//            return;
//
//        if (mMediaPlayerControl.isPlaying()) {
//            mPlayPause.setImageResource(R.drawable.ic_pause);
//        }
//
//        if (mMediaPlayerControl.isPlaying())
//            mMediaPlayerControl.pause();
//    }
//
//    private long setProgress() {
//        if (mMediaPlayerControl == null || mDragging)
//            return 0;
//
//        long position = mMediaPlayerControl.getCurrentPosition();
//        long duration = mMediaPlayerControl.getDuration();
//
//        if(position > mEndPos * 1000) {
//            doPause();
//        }
//
//        if (mSeekBar != null) {
//            if (duration > 0) {
//                float pos = (float) position / (float) duration;
//                mSeekBar.setProgress( pos);
//            }
////            int percent = mMediaPlayerControl.getBufferPercentage();
////            mSeekBar.setSecondaryProgress(percent * 10);
//        }
//
//        mDuration = duration;
//
////        Log.i(TAG, "duration=" + duration + ", position=" + position);
//
//        if (mTotalTime != null)
//            mTotalTime.setText(Util.generateTime(mDuration));
//        if (mCurrentTime != null)
//            mCurrentTime.setText(Util.generateTime(position));
//
//        return position;
//    }
//
//
//    private void rotateScreen() {
//        int currentOrientation = getRequestedOrientation();
//        if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        } else {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        }
//    }
//
//    private void goBack() {
//        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        } else {
//            finish();
//        }
//    }
//
//    private void showMore() {
//
//    }
//
//    private void lockScreen() {
//        mLockCenter.setVisibility(View.VISIBLE);
//        hideOverlay();
//        mIsLocked = true;
//        show();
//    }
//
//    private void unlockScreen() {
//        mLockCenter.setVisibility(View.GONE);
//        mIsLocked = false;
//        showOverlay();
//        show();
//    }
//
//    private void floatScreen() {
//        Intent mIntent = new Intent();
//        mIntent.putExtra("playUrl", mVideoPath);
//        mIntent.setClass(VideoPlayerActivity.this, PlayBackService.class);
//        VideoPlayerActivity.this.startService(mIntent);
//        VideoPlayerActivity.this.finish();
//    }
//
//    /**
//     * Dim the status bar and/or navigation icons when needed on Android 3.x.
//     * Hide it on Android 4.0 and later
//     */
//    @TargetApi(Build.VERSION_CODES.KITKAT)
//    private void dimStatusBar(boolean dim) {
////        if (dim || mIsLocked)
////            mActionBar.hide();
////        else
////            mActionBar.show();
////        if (!AndroidUtil.isHoneycombOrLater() || mIsNavMenu)
////            return;
//        int visibility = 0;
//        int navbar = 0;
//
//        if (true) {
//            visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//            navbar = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
//        }
//        if (dim || mIsLocked) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            if (true)
//                navbar |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
//            else
//                visibility |= View.STATUS_BAR_HIDDEN;
//            if (true) {
//                navbar |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//                if (true)
//                    visibility |= View.SYSTEM_UI_FLAG_IMMERSIVE;
//                if (true)
//                    visibility |= View.SYSTEM_UI_FLAG_FULLSCREEN;
//            }
//        } else {
////            mActionBar.show();
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            if (true)
//                visibility |= View.SYSTEM_UI_FLAG_VISIBLE;
//            else
//                visibility |= View.STATUS_BAR_VISIBLE;
//        }
//
//        if (true)
//            visibility |= navbar;
//        getWindow().getDecorView().setSystemUiVisibility(visibility);
//    }
//
//    private void showOverlay() {
//        if (mIsLocked) {
//            mLockCenter.setVisibility(View.VISIBLE);
////            mTitleView.setVisibility(View.INVISIBLE);
////            mBottomView.setVisibility(View.INVISIBLE);
////            mToolsView.setVisibility(View.INVISIBLE);
//        } else {
////            mLockCenter.setVisibility(View.INVISIBLE);
//            mTitleView.setVisibility(View.VISIBLE);
//            mBottomView.setVisibility(View.VISIBLE);
////            mToolsView.setVisibility(View.VISIBLE);
//        }
//    }
//
//    private void hideOverlay() {
//        if (mIsLocked) {
//            mLockCenter.setVisibility(View.INVISIBLE);
//        } else {
//            mTitleView.setVisibility(View.INVISIBLE);
//            mBottomView.setVisibility(View.INVISIBLE);
////            mToolsView.setVisibility(View.INVISIBLE);
//        }
//
//    }
//}

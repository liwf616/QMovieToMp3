package com.github.yuqilin.qmediaplayerapp.media;

import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.github.yuqilin.qmediaplayerapp.QApplication;
import com.github.yuqilin.qmediaplayerapp.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liwenfeng on 17/4/20.
 */

public class AudioLoader {
    public static final String TAG = "AudioLoader";
    List<String> fileList = new ArrayList<String>();

    public interface AudioLoaderListener {
        void onLoadItem(int position, AudioWrapter video);
        void onLoadCompleted(ArrayList<AudioWrapter> videos);
    }

    private ArrayList<AudioWrapter> mAudios = new ArrayList<>();

    private AudioLoader.AudioLoaderListener mAduioLoaderListener;

    public AudioLoader(AudioLoader.AudioLoaderListener audioLoaderListener) {
        mAduioLoaderListener = audioLoaderListener;
    }

    public void scanStart() {
        QApplication.runBackground(new Runnable() {
            @Override
            public void run() {
                scanAudios();
                loadAduios();
                if (mAduioLoaderListener != null) {
                    mAduioLoaderListener.onLoadCompleted(mAudios);
                }
            }
        });
    }

    public void scanAudios() {
        String destPath = FileUtils.getStoragePath();
        File file = new File(destPath);
        if (file != null) {
            if (file.isDirectory()) {
                if (file.listFiles() != null) {
                    for (File childFile : file.listFiles()) {
                        fileList.add(childFile.getAbsolutePath());
                    }
                }
            }
        }
    }

    public void loadAduios() {
        String[] mediaColumns = new String[]{
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ARTIST
        };

        //首先检索SDcard上所有的audio
        Cursor cursor = QApplication.getAppContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mediaColumns, null, null, null);

        if(cursor.moveToFirst()){
            do{
                AudioWrapter media = new AudioWrapter();

                media.filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                media.mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                media.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                media.duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                media.fileSize = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                media.artlist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));

                //获取当前Video对应的Id，然后根据该ID获取其Thumb
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                String selection = MediaStore.Video.Thumbnails.VIDEO_ID +"=?";
                String[] selectionArgs = new String[]{
                        id+""
                };

                media.audioId = id;

                Log.d(TAG, "====Scanned : [" + mAudios.size() + "] " + media.filePath + " " + media.title + " " + media.audioId + " " + media.artlist);

                if(fileList.contains(media.filePath)) {
                    mAudios.add(media);
                }

                if (mAduioLoaderListener != null) {
                    mAduioLoaderListener.onLoadItem(mAudios.size() - 1, media);
                }
            } while(cursor.moveToNext());
        }
    }

    public ArrayList<AudioWrapter> getAudios() {
            return mAudios;
    }
}
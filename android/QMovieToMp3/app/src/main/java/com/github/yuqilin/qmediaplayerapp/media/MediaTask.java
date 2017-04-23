package com.github.yuqilin.qmediaplayerapp.media;

import android.os.Environment;

import com.github.yuqilin.qmediaplayerapp.VideoPlayerActivity;
import com.github.yuqilin.qmediaplayerapp.gui.tasks.Command;
import com.github.yuqilin.qmediaplayerapp.util.FileUtils;
import com.github.yuqilin.qmediaplayerapp.util.Strings;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by liwenfeng on 17/4/7.
 */

public class MediaTask {

    private String  videoPath; //绝对路径
    private boolean vbr;
    private String  type;
    private String  bits;
    private int    duration;
    private int     startTime;
    private int     endTime;

    private String  videoDstPath;

    public static String[] getMediaAudioFormat() {
        return MEDIA_AUDIO_FORMAT;
    }

    private int    process;

    private int     taskIndex;

    public int getTaskIndex() {
        return taskIndex;
    }

    public void setTaskIndex(int taskIndex) {
        this.taskIndex = taskIndex;
    }

    public void setProcess(int process) {
        this.process = process;
    }

    public int getProcess() {
        return  process;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isVbr() {
        return vbr;
    }

    public void setVbr(boolean vbr) {
        this.vbr = vbr;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getBits() {
        return bits;
    }

    public void setBits(String bits) {
        this.bits = bits;
    }

    public String getVideoDstPath() {
        return videoDstPath;
    }

    public void setVideoDstPath(String videoDstPath) {
        this.videoDstPath = videoDstPath;
    }

    public MediaTask(String videoPath, boolean vbr, String type, String bits, int duration,int startTime, int endTime) {
        this.videoPath = videoPath;
        this.vbr = vbr;
        this.type = type;
        this.bits = bits;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;

        String filename = getFileName(videoPath);
        String destName = null;
        if(filename != null) {
            Random random = new Random(100);

            destName  = filename + random.nextInt(100) + "_" + this.startTime+ "_" + this.endTime+"." + type;
        } else {
            return;
        }

        Date now = new Date();
        SimpleDateFormat simpleDate =  new SimpleDateFormat("yyyyMMdd");

        String date = simpleDate.format(now);

        String destPath = null;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            destPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + date + "/";
            if (destPath == null) {
                return;
            }
        }

        if (FileUtils.isFolderExists(destPath) == false) {
            return;
        }

        this.videoDstPath =  destPath + destName;
    }

    public static final String[] MEDIA_AUDIO_FORMAT = {
//            "mp3",
            "aac"
    };

    public static final String[] MEDIA_AUDIO_BITS= {
            "copy",
            "128k",
            "192k",
            "256k",
            "320k",
            "130k",
            "190k",
            "245k"
    };

    public static String getComment(int i) {
        if(i == 0) {
            return "copy (32kb/s)";
        } else  if (i <= 4) {
            return MEDIA_AUDIO_BITS[i] + " " + "CBR";
        } else {
            return MEDIA_AUDIO_BITS[i] + " " + "VBR(slow)";
        }
    }

    public String getFileName(String pathandname){
        int start=pathandname.lastIndexOf("/");
        int end=pathandname.lastIndexOf(".");
        if (start!=-1 && end!=-1) {
            return pathandname.substring(start+1, end);
        }
        else {
            return null;
        }
    }

    public String[] getCommand() {
        Command command = new Command();
        command.addCommand("ffmpeg");
        command.addCommand("-y");
        command.addCommand("-i", videoPath);
        command.addCommand("-c:a","aac");
        command.addCommand("-vn");
        command.addCommand("-ab");
        command.addCommand("32000");
        command.addCommand("-ar");
        command.addCommand("44100");
        command.addCommand("-ac");
        command.addCommand("2");
        command.addCommand(this.videoDstPath);

        List<String> comList =  command.getCommand();

        String[] commandStr = new String[comList.size()];

        commandStr =  comList.toArray(commandStr);

        return  commandStr;
    }

    public String getProcessText() {
        String process = VideoPlayerActivity.generateTime(getProcess());
        String duration = VideoPlayerActivity.generateTime(getDuration());

        return String.format("%s/%s", process, duration);
    }

    public int getProcessInt() {
        return (int) (process * 100 / duration);
    }
}

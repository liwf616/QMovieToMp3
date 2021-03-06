/**
 * Created by liwenfeng on 17/4/11.
 */

package com.github.yuqilin.qmediaplayerapp.gui.tasks;

import android.content.Context;
import android.widget.ProgressBar;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.yuqilin.qmediaplayerapp.R;
import com.github.yuqilin.qmediaplayerapp.gui.view.AutoFitRecyclerView;
import com.github.yuqilin.qmediaplayerapp.media.MediaTask;
import com.github.yuqilin.qmediaplayerapp.ITaskEventHandler;
import com.github.yuqilin.qmediaplayerapp.util.Util;

import java.util.ArrayList;
import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> implements TaskRunner.TaskRunnerListener{

    private AutoFitRecyclerView mAutoFitRecyclerView;
    private ITaskEventHandler mTaskEventsHandler;
    private ArrayList<MediaTask> mTasks = new ArrayList<>();

    private boolean taskIsRunning = false;

    public final static String TAG = "TaskListAdapter";

    private static final int UPDATE_PROCESS = 1;
    private static final int TASK_FINISHED = 2;

    public TaskListAdapter(ITaskEventHandler eventHandler, AutoFitRecyclerView autoFitRecyclerView) {
        super();

        mAutoFitRecyclerView = autoFitRecyclerView;
        mTaskEventsHandler = eventHandler;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case UPDATE_PROCESS:
                    updateProcessText((MediaTask)message.obj);
                    break;
                case TASK_FINISHED:
                    removeTask((MediaTask)message.obj);
                    nextTask();
                    break;
            }
            return true;
        }
    });

    private void updateProcessText(MediaTask task) {
        TaskViewHolder holder =(TaskViewHolder) mAutoFitRecyclerView.findViewHolderForLayoutPosition(mTasks.indexOf(task));

        if (holder != null) {
            holder.setmProcessText(task.getProcessText());
            holder.setmProcessBar(task.getProcessInt());
        }
    }

    public void onProcess(int seconds){
        Log.w(TAG, "onProcess : " + seconds );

        MediaTask media = mTasks.get(0);
        if (media == null) {
            return;
        }

        media.setProcess(seconds * 1000);

        Message message = Message.obtain();
        message.obj = media;
        message.what = UPDATE_PROCESS;

        mHandler.sendMessage(message);
    };


    public  void onCompleted() {
        Log.w(TAG, "onCompleted");

        taskIsRunning = false;

        MediaTask media = mTasks.get(0);
        if (media == null) {
            return;
        }

        mTaskEventsHandler.onTaskFinished(media);

        Message message = Message.obtain();
        message.obj = media;
        message.what = TASK_FINISHED;

        mHandler.sendMessage(message);
    };

    private void nextTask() {
        if (mTasks.size() > 0) {
            MediaTask mediaTask = mTasks.get(0);
            if(mediaTask != null) {
                TaskRunner runner = new TaskRunner(this);
                runner.convertStart(mediaTask.getCommand());
                taskIsRunning = true;
            }
        }
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder viewType " + viewType);

        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate( R.layout.item_task_list, parent, false);

        return new TaskViewHolder(v);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            MediaTask media = (MediaTask) view.getTag();
            mTaskEventsHandler.onClick(view, 0, media);
        }
    };

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder position " + position);

        MediaTask media = mTasks.get(position);
        if (media == null) {
            return;
        }

        holder.mFileName.setText(media.getVideoDstPath().substring(media.getVideoDstPath().lastIndexOf('/') + 1));
        holder.setmProcessText(String.format("%s/%s","00:00:00", Util.generateTime(media.getEndTime() - media.getStartTime())));
        holder.mListItem.setTag(media);
        holder.mListItem.setOnClickListener(mOnClickListener);

        Log.d(TAG, "position[" + position + "]: " + media.getVideoPath());
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            Log.d(TAG, "onBindViewHolder position " + position + ", payloads size " + payloads.size());
            onBindViewHolder(holder, position);
        }
    }

    public void onViewRecycled(TaskViewHolder holder) {
        Log.d(TAG, "onViewRecycled");
        super.onViewRecycled(holder);
    }

    public void addTask(MediaTask task) {
        int position = getItemCount();

        mTasks.add(position, task);

        notifyItemInserted(position);

        Log.d(TAG, "addTask position " + position);

        if (taskIsRunning == false) {
            nextTask();
        }

        Log.d(TAG, "addTask position " + position);
    }

    public void removeTask(MediaTask task) {
        int position = mTasks.indexOf(task);

        mTasks.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    @MainThread
    public void clear() {
        mTasks.clear();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnFocusChangeListener {
        private View mListItem;
        private ImageView mTaskStatusPic;
        private TextView mFileName;
        private TextView mProcessText;
        private ProgressBar mProgressBar;

        public TaskViewHolder(View v) {
            super(v);
            if(v == null) {
                return;
            }

            v.setOnFocusChangeListener(this);

            mListItem = v.findViewById(R.id.task_list_item_view);
            mTaskStatusPic = (ImageView) v.findViewById(R.id.item_task_list_status);
            mFileName = (TextView) v.findViewById(R.id.item_task_list_filename);
            mProcessText = (TextView) v.findViewById(R.id.item_video_task_process_text);
            mProgressBar = (ProgressBar) v.findViewById(R.id.bar);
        }

        public void setmProcessText(String processText) {
            mProcessText.setText(processText);
        }

        public void setmProcessBar(int process) {
            mProgressBar.setProgress(process);
        }

        public void onClick(View v) {
            int position = getLayoutPosition();
            mTaskEventsHandler.onClick(v, position, mTasks.get(position));
        }

        public void onMoreClick(View v){
        }

        public boolean onLongClick(View v) {
            int position = getLayoutPosition();
            return mTaskEventsHandler.onLongClick(v, position, mTasks.get(position));
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
        }
    }
}

package com.udacity.stockhawk.util;

import android.os.AsyncTask;

/*
 * Base class for creating async tasks
 */
public class BaseAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    private ICallbackTask<Result> mCallbackTask;
    private ITask<Result> mTask;

    public interface ICallbackTask<Result>{
        void onStart();
        void onSuccess(Result result);
        void onError();
    }

    public interface ITask<Result>{
        Result task();
    }

    public BaseAsyncTask<Params, Progress, Result> setCallback(ICallbackTask<Result> callbackTask){
        mCallbackTask = callbackTask;
        return this;
    }

    public BaseAsyncTask<Params, Progress, Result> setTask(ITask<Result> task){
        mTask = task;
        return this;
    }

    @Override
    protected Result doInBackground(Params... params) {
        if(mTask != null){
            return  mTask.task();
        }
        return  null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(mCallbackTask != null) {
            mCallbackTask.onStart();
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        if(mCallbackTask != null) {
            if (result != null) {
                mCallbackTask.onSuccess(result);
            } else {
                mCallbackTask.onError();
            }
        }
    }
}
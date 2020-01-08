package com.tradinos.drawyourpath.sources;

import android.app.Application;

import com.tradinos.drawyourpath.Models.MyPath;

import java.util.List;

import androidx.lifecycle.LiveData;

public class PathRepository {

    private PathDao mPathDao;
    private LiveData<List<MyPath>> mAllPaths;

    PathRepository(Application application){
        PathRoomDatabase db = PathRoomDatabase.getDatabase(application);
        mPathDao = db.pathDao();
        mAllPaths = mPathDao.getAlWords();
    }

    public LiveData<List<MyPath>> getAllPaths(){
        return mAllPaths;
    }


    public void insertPath(MyPath myPath){
        PathRoomDatabase.databaseWriteExecutor.execute(() -> {
            mPathDao.insert(myPath);
        });
    }

    public void deletePath(MyPath myPath){
        PathRoomDatabase.databaseWriteExecutor.execute(() -> {
            mPathDao.delete(myPath);
        });
    }

}
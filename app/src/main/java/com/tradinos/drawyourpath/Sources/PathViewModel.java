package com.tradinos.drawyourpath.Sources;

import android.app.Application;

import com.tradinos.drawyourpath.Models.MyPath;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class PathViewModel extends AndroidViewModel {

    private PathRepository mRepository;
    private LiveData<List<MyPath>> mAllPaths;


    public PathViewModel(@NonNull Application application) {
        super(application);
        mRepository = new PathRepository(application);
        mAllPaths = mRepository.getAllPaths();
    }

    public LiveData<List<MyPath>> getAllPaths(){
        return mAllPaths;
    }

    public void insertPath(MyPath myPath){
        mRepository.insertPath(myPath);
    }

    public void deletePath(MyPath myPath){
        mRepository.deletePath(myPath);
    }

}

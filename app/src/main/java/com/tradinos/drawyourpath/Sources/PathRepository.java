package com.tradinos.drawyourpath.Sources;
import android.app.Application;
import com.tradinos.drawyourpath.Models.MyPath;
import java.util.List;
import androidx.lifecycle.LiveData;

class PathRepository {

    private PathDao mPathDao;
    private LiveData<List<MyPath>> mAllPaths;

    PathRepository(Application application){
        PathRoomDatabase db = PathRoomDatabase.getDatabase(application);
        mPathDao = db.pathDao();
        mAllPaths = mPathDao.getAlWords();
    }

    LiveData<List<MyPath>> getAllPaths(){
        return mAllPaths;
    }


    void insertPath(MyPath myPath){
        PathRoomDatabase.databaseWriteExecutor.execute(() -> {
            mPathDao.insert(myPath);
        });
    }

    void deletePath(MyPath myPath){
        PathRoomDatabase.databaseWriteExecutor.execute(() -> {
            mPathDao.delete(myPath);
        });
    }

}

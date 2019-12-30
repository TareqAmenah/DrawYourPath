package com.tradinos.drawyourpath;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {MyPath.class}, version = 1, exportSchema = false)
public abstract class PathRoomDatabase extends RoomDatabase {

    public abstract PathDao pathDao();

    private static volatile PathRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);


    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                // If you want to start with more words, just add them.
                PathDao dao = INSTANCE.pathDao();
//                dao.deleteAll();

//                MyPath path = new MyPath("Damascus", "Aleppo", "300km", "3 hour");
//                dao.insert(path);
//                path = new MyPath("Damascus", "Homs", "100km", "1 hour");
//                dao.insert(path);

            });
        }
    };



    static PathRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PathRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PathRoomDatabase.class, "path_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

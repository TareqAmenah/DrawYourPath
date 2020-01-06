package com.tradinos.drawyourpath;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface PathDao {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(MyPath path);

    @Query("DELETE FROM path_table")
    void deleteAll();

    @Query("SELECT * from path_table")
    LiveData<List<MyPath>> getAlWords();

    @Delete
    void delete(MyPath path);
}

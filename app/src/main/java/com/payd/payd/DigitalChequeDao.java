package com.payd.payd;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.payd.payd.core.DigitalCheque;

import java.util.List;

@Dao
public interface DigitalChequeDao {
    @Query("SELECT * FROM DigitalCheque")
    List<DigitalCheque> getAll();

    @Insert
    void insertAll(DigitalCheque ... digitalCheques);

    @Delete
    void delete(DigitalCheque digitalCheques);
    @Delete
    void deleteAll(List<DigitalCheque> digitalCheques);
}

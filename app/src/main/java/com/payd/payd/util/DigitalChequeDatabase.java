package com.payd.payd.util;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.payd.payd.DigitalChequeDao;
import com.payd.payd.core.DigitalCheque;

@Database(entities = {DigitalCheque.class}, version = 1)
public abstract class DigitalChequeDatabase extends RoomDatabase {
    public abstract DigitalChequeDao digitalChequeDao();
}

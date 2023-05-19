package com.payd.payd.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Transaction{
    String name;
    String date;
    Double amount;
    String imgUrl;

    public Transaction(String name,String date,Double amount,String imgUrl) {
        this.name = name;
        this.date = date;
        this.amount = amount;
        this.imgUrl = imgUrl;
    }

    public String getName(){
        return this.name;
    }

    public String getDate(){
        return this.date;
    }
    public Double getAmount(){
        return this.amount;
    }

    public String getImgUrl(){
        return this.imgUrl;
    }
}

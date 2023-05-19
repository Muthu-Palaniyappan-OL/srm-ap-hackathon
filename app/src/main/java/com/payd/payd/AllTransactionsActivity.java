package com.payd.payd;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.payd.payd.core.DigitalCheque;
import com.payd.payd.util.CustomTransactionAdapter;
import com.payd.payd.util.Transaction;
import com.payd.payd.util.TransactionStorageManager;
import com.payd.payd.util.UserSession;
import com.payd.payd.util.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AllTransactionsActivity extends AppCompatActivity {

    ArrayList<Transaction> PastTransactions;
    ListView listView;
    private static CustomTransactionAdapter adapter;
    TransactionStorageManager transactionStorageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_transactions);
        transactionStorageManager = new TransactionStorageManager(this);

        try {
            initTranHistory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    private void initTranHistory() throws Exception {
        this.listView=(ListView)findViewById(R.id.list);
        this.PastTransactions= new ArrayList<>();

        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            ArrayList<DigitalCheque> transactions = new ArrayList<DigitalCheque>(transactionStorageManager.getTransactions());

            Log.d("INit", "initTranHistory: "+transactions);
            try {
                for (DigitalCheque digitalCheque: transactions) {
                    if (digitalCheque.senderId.equals(UserSession.me.username)){
                        // spent
                        this.PastTransactions.add(new Transaction(digitalCheque.receiverId, formatDate(digitalCheque.timestamp), (double) (-1*digitalCheque.amount), ""));
                    } else {
                        // received
                        this.PastTransactions.add(new Transaction(digitalCheque.senderId, formatDate(digitalCheque.timestamp), (double) (digitalCheque.amount), ""));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        this.adapter=new CustomTransactionAdapter(PastTransactions,getApplicationContext());
        listView.setAdapter(adapter);

    }

    public String formatDate(long timeInMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return formatter.format(calendar.getTime());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        transactionStorageManager.close();
    }
}
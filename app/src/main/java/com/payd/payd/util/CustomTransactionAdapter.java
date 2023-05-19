package com.payd.payd.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.widget.TextViewCompat;

import com.google.android.material.snackbar.Snackbar;
import com.payd.payd.R;

import java.util.ArrayList;

public class CustomTransactionAdapter extends ArrayAdapter<Transaction> implements View.OnClickListener{

    private ArrayList<Transaction> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        TextView txtDate;
        TextView txtAmount;
        ImageView profile_img;
    }

    public CustomTransactionAdapter(ArrayList<Transaction> data, Context context) {
        super(context, R.layout.transaction_row_item, data);
        this.dataSet = data;
        this.mContext=context;

    }

    @Override
    public void onClick(View v) {
/*
        int position=(Integer) v.getTag();
        Object object= getItem(position);
        Transaction Transaction=(Transaction)object;

        switch (v.getId())
        {
            case R.id.ProfileImage:
                Snackbar.make(v, "Release date " +Transaction.getFeature(), Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
                break;
        }*/
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Transaction Transaction = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.transaction_row_item, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.NameText);
            viewHolder.txtDate = (TextView) convertView.findViewById(R.id.DateText);
            viewHolder.txtAmount = (TextView) convertView.findViewById(R.id.AmountText);
            viewHolder.profile_img = (ImageView) convertView.findViewById(R.id.ProfileImage);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;

        viewHolder.txtName.setText(Transaction.getName());
        viewHolder.txtDate.setText(Transaction.getDate());
        viewHolder.txtAmount.setText(Transaction.getAmount().toString());
        viewHolder.profile_img.setOnClickListener(this);
        viewHolder.profile_img.setTag(position);

        if(Transaction.getAmount()>0){
            TextViewCompat.setTextAppearance(viewHolder.txtAmount,R.style.DarkGreenText);
        }else{
            TextViewCompat.setTextAppearance(viewHolder.txtAmount,R.style.DarkRedText);
        }

        // Return the completed view to render on screen
        return convertView;
    }
}

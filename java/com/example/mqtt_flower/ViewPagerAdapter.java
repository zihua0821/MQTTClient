package com.example.mqtt_flower;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewPagerHolder> {
    Context context;
    ViewPager2 viewPager;


    public ViewPagerAdapter(Context context,ViewPager2 viewPager){
        this.context = context;
        this.viewPager = viewPager;
    }

    @NonNull
    @Override
    public ViewPagerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_main,parent,false);
        return new ViewPagerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPagerHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 3;
    }

    class ViewPagerHolder extends RecyclerView.ViewHolder {

        public ViewPagerHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

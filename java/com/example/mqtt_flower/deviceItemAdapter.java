package com.example.mqtt_flower;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class deviceItemAdapter extends RecyclerView.Adapter<deviceItemAdapter.deviceItemHolder> {

    Context context;
    ArrayList<InfoRootBean> items;

    public deviceItemAdapter(Context context,ArrayList<InfoRootBean> items){
        this.context = context;
        this.items = items;
    }
    @NonNull
    @Override
    public deviceItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_device_item,parent,false);
        return new deviceItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull deviceItemHolder holder, @SuppressLint("RecyclerView") final int position) {
        InfoRootBean item = items.get(position);
        holder.name.setText(item.Iname);
        holder.info.setText(item.Iinfo);
        holder.img.setImageResource(R.drawable.deviceicon_com);
        holder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,MainActivity.class);
                intent.putExtra("device",items.get(position).Iname);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    class deviceItemHolder extends RecyclerView.ViewHolder {
        TextView name,info;
        ImageView img;
        CardView content;

        public deviceItemHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            info = itemView.findViewById(R.id.info);
            img = itemView.findViewById(R.id.img);
            content = itemView.findViewById(R.id.content);
        }
    }
}

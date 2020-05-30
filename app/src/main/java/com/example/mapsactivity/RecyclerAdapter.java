package com.example.mapsactivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private final static String TAG = "RecyclerAdapter";

    private List<FStore> mStore = null;

    //메인에서 sp 받아와서 객체로 변환하는 함수 호출, 처음 mStore가 불릴 때 저장
    RecyclerAdapter(List<FStore> list){
        mStore = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView marker;
        TextView name;
        TextView addr;
        ImageView star;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 뷰 객체에 대한 참조. (hold strong reference)
            marker = itemView.findViewById(R.id.imageView) ;
            name = itemView.findViewById(R.id.storeName) ;
            addr = itemView.findViewById(R.id.storeAddress) ;
            star = itemView.findViewById(R.id.Favorites) ;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FStore item = mStore.get(position) ;
        holder.name.setText(item.getName());
        holder.addr.setText(item.getAddr());
        if(item.getFavorites()) holder.star.setBackgroundResource(R.drawable.ic_bookmark_black_24dp);
        else holder.star.setBackgroundResource(R.drawable.ic_bookmark_border_black_24dp);
        switch (item.getRemain_stat()) {
            case "plenty":
                holder.marker.setImageResource(R.drawable.ic_green_pin);
                break;
            case "some":
                holder.marker.setImageResource(R.drawable.ic_yellow_pin);
                break;
            case "few":
                holder.marker.setImageResource(R.drawable.ic_red_pin);
                break;
            default:
                holder.marker.setImageResource(R.drawable.ic_gray_pin);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mStore.size();
    }




}

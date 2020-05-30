package com.example.mapsactivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import static com.example.mapsactivity.PreferenceManager.getmStoreFromSP;
import static com.example.mapsactivity.PreferenceManager.setmStoretoSP;


public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private final static String TAG = "RecyclerAdapter";
    @SuppressLint("StaticFieldLeak")
    static Context mContext;
    @SuppressLint("StaticFieldLeak")


    private List<FStore> mStore;

    //메인에서 sp 받아와서 객체로 변환하는 함수 호출, 처음 mStore가 불릴 때 저장
    RecyclerAdapter(List<FStore> list){
        mStore = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout lv;
        ImageView marker;
        TextView name;
        TextView addr;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            marker = itemView.findViewById(R.id.imageView) ;
            name = itemView.findViewById(R.id.storeName) ;
            addr = itemView.findViewById(R.id.storeAddress) ;
            this.lv = itemView.findViewById(R.id.layoutItem);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder oDialog = new AlertDialog.Builder(mContext);
                    oDialog.setMessage("즐겨찾기를 해제하시겠습니까?")
                            .setTitle("즐겨찾기 해제")
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.e("Dialog", "취소");
                                }
                            })
                            .setNeutralButton("예", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mStore = getmStoreFromSP(mContext);
                                    Log.e(TAG, getAdapterPosition() + "");
                                    mStore.remove(getAdapterPosition());
                                    setmStoretoSP(mContext, mStore);
                                }
                            })
                            .setCancelable(false); // 백버튼으로 팝업창이 닫히지 않도록 한다.
                    AlertDialog dialog = oDialog.create();
                    dialog.show();

                    return false;
                }
            });
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
        if(mStore == null) return 0;
        return mStore.size();
    }
}

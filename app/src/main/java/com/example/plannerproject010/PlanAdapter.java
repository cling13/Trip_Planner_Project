package com.example.plannerproject010;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.ViewHolder>{
    List<PlanClass> items;
    AppCompatActivity context;
    SimpleAdapter totalPlanAdapter;
    private ItemClickListner itemClickListner;
    //static SupportMapFragment mapFragment;

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        RecyclerView placeList;
        Button addBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.dateText);
            placeList = itemView.findViewById(R.id.planList);
            addBtn = itemView.findViewById(R.id.addBtn);

            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(itemClickListner!=null)
                    {
                        int position=getAdapterPosition();
                        if(position!=RecyclerView.NO_POSITION)
                        {
                            itemClickListner.onItemBtnClick(position);
                        }
                    }
                }
            });
        }
    }

    public PlanClass getItem(int i)
    {
        return items.get(i);
    }

    public PlanAdapter(AppCompatActivity context, List<PlanClass> items, SupportMapFragment mapFragment, ItemClickListner itemClickListner)
    {
        this.context = context;
        this.items=items;
        this.itemClickListner=itemClickListner;
        //this.mapFragment = mapFragment;


    }

    void addList(listClass tmp)
    {
        totalPlanAdapter.addItem(tmp);
        totalPlanAdapter.notifyDataSetChanged();

        tmp.setBtnName("주변 검색");
        //totalPlanList.add(tmp);

        totalPlanAdapter.notifyDataSetChanged();
        notifyDataSetChanged();

        //latLngs.add(tmp.getlatLng());
        //mainGoogleMap.addMark(tmp.getlatLng(),tmp.getName());
        //mainGoogleMap.addPolyline(latLngs);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        Context context=parent.getContext();
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        view = inflater.inflate(R.layout.plan_list_obj, parent, false);
        PlanAdapter.ViewHolder vh=new PlanAdapter.ViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        PlanClass text = items.get(position);
        holder.textView.setText(text.getDate());
        holder.addBtn.setText(text.getBtn());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(holder.itemView.getContext());
        holder.placeList.setLayoutManager(layoutManager);
        totalPlanAdapter = new SimpleAdapter(items.get(position).planList, null);
        holder.placeList.setAdapter(totalPlanAdapter);
        totalPlanAdapter.notifyDataSetChanged();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public ArrayList<listClass> getAll()
    {
        return totalPlanAdapter.getAll();
    }
}

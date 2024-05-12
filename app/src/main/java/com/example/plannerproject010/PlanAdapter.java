package com.example.plannerproject010;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.ViewHolder>{
    List<PlanClass> items;
    AppCompatActivity context;
    SimpleAdapter totalPlanAdapter;
    ActivityResultLauncher<Intent> mStartForResult;
    ArrayList<listClass> tmpLIst = new ArrayList<>();
    //static SupportMapFragment mapFragment;



    class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        RecyclerView placeList;
        Button addBtn;
        ArrayList<listClass> totalPlanList = new ArrayList<>();

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.dateText);
            placeList = itemView.findViewById(R.id.planList);
            addBtn = itemView.findViewById(R.id.addBtn);

            addBtn.setOnClickListener(view -> {
                Intent intent = new Intent(context, SearchActivity.class);
                mStartForResult.launch(intent);
                for(listClass tmp:tmpLIst)
                    totalPlanList.add(tmp);

                totalPlanAdapter.notifyDataSetChanged();
                notifyDataSetChanged();
            });
        }

    }

    public PlanAdapter(AppCompatActivity context, List<PlanClass> items, SupportMapFragment mapFragment)
    {
        this.context = context;
        this.items=items;
        //this.mapFragment = mapFragment;

        mStartForResult = context.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // getResultCode가 0일 경우 세컨드 액티비티에서 넘어옴
                    if (result.getResultCode() == 1) {
                        ArrayList<listClass> tmp = new ArrayList<>();
                        tmp = (ArrayList<listClass>) result.getData().getSerializableExtra("data");
                        tmpLIst.clear();
                        for (int i = 0; i < tmp.size(); i++) {
                            addList(tmp.get(i));
                        }
                    }
                });
    }

    void addList(listClass tmp)
    {
        tmp.setBtnName("주변 검색");
        //totalPlanList.add(tmp);
        tmpLIst.add(tmp);
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
        totalPlanAdapter = new SimpleAdapter(holder.totalPlanList, null);
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

}

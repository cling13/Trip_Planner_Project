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

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.ViewHolder> implements OnMapReadyCallback {
    List<PlanClass> items;
    Context context;
    ArrayList<listClass> totalPlanList = new ArrayList<>();
    ActivityResultLauncher<Intent> mStartForResult;
    SimpleAdapter totalPlanAdapter;
    static MyGoogleMap mainGoogleMap;
    static ArrayList<LatLng> latLngs = new ArrayList<>();
    static SupportMapFragment mapFragment;

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mainGoogleMap = new MyGoogleMap(googleMap);
        mainGoogleMap.setgMap();
//
//        sqlDB= listDBHelper.getReadableDatabase();
//        String sql="select * from plantable WHERE date = '"+ localDate.toString() +"';";
//        Log.d("sql2",sql);
//        Cursor cursor = sqlDB.rawQuery(sql,null);
//         while(cursor.moveToNext()) {
//            totalPlanAdapter.addItemList(cursor.getString(1), MainActivity.this);
//        }
//        totalPlanAdapter.notifyDataSetChanged();
//        sqlDB.close();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        RecyclerView placeList;
        Button addBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.dateText);
            placeList = itemView.findViewById(R.id.planList);
            addBtn = itemView.findViewById(R.id.addBtn);

            addBtn.setOnClickListener(view -> {
                Intent intent = new Intent(context, SearchActivity.class);
                mStartForResult.launch(intent);
            });
        }
    }

    public PlanAdapter(AppCompatActivity context, List<PlanClass> items, SupportMapFragment mapFragment)
    {
        this.context = context;
        this.items=items;
        this.mapFragment = mapFragment;

        mStartForResult= context.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // getResultCode가 0일 경우 세컨드 액티비티에서 넘어옴
                    if (result.getResultCode() == 1) {
                        ArrayList<listClass> tmp = new ArrayList<>();
                        tmp = (ArrayList<listClass>) result.getData().getSerializableExtra("data");
                        for(int i=0 ; i<tmp.size(); i++) {
                            addList(tmp.get(i));
                        }
                    }
                });

        totalPlanAdapter = new SimpleAdapter(totalPlanList, null);
    }

    void addList(listClass tmp)
    {
        tmp.setBtnName("주변 검색");
        totalPlanList.add(tmp);
        totalPlanAdapter.notifyDataSetChanged();

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

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        vh.placeList.setLayoutManager(layoutManager);
        vh.placeList.setAdapter(totalPlanAdapter);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        PlanClass text = items.get(position);
        holder.textView.setText(text.getDate());
        holder.addBtn.setText(text.getBtn());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(holder.itemView.getContext());
        holder.placeList.setLayoutManager(layoutManager);
        SimpleAdapter simpleAdapter = new SimpleAdapter(text.getPlanList(), null);
        holder.placeList.setAdapter(simpleAdapter);
        simpleAdapter.notifyDataSetChanged();
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

package com.example.plannerproject010;

import static com.example.plannerproject010.MainActivity.context;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class PlanClass {
    ArrayList<listClass> planList = new ArrayList<>();
    TextView date;
    Button addPlan;
    RecyclerView recyclerView;
    SimpleAdapter simpleAdapter;

    PlanClass(Context context, String date)
    {
        LayoutInflater inflater = LayoutInflater.from(context);

        // XML 레이아웃 파일 인플레이트
        View view = inflater.inflate(R.layout.plan_list_obj, null);
        this.date=(TextView) view.findViewById(R.id.dateText);
        addPlan=(Button) view.findViewById(R.id.addBtn);
        recyclerView=(RecyclerView) view.findViewById(R.id.planList);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        simpleAdapter = new SimpleAdapter(planList,null);
        recyclerView.setAdapter(simpleAdapter);

        this.date.setText(date);
    }

    public String getDate()
    {
        return date.getText().toString();
    }
    public String getBtn()
    {
        return addPlan.getText().toString();
    }
    public ArrayList<listClass> getPlanList()
    {
        return planList;
    }
}

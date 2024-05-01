package com.example.plannerproject010;

import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class PlanClass {
    ArrayList<listClass> planList = new ArrayList<>();
    TextView date;
    Button addPlan;

    PlanClass(ArrayList<listClass> planList,TextView date, Button addPlan)
    {
        this.planList=planList;
        this.date=date;
        this.addPlan=addPlan;
    }
}

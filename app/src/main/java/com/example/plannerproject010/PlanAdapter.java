package com.example.plannerproject010;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.ViewHolder> {
    List<PlanClass> items = new ArrayList<>();
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        RecyclerView placeList;
        Button addBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.dateText);
            placeList = itemView.findViewById(R.id.planList);
            addBtn = itemView.findViewById(R.id.addBtn);
        }
    }

    public PlanAdapter(List<PlanClass> items)
    {
        this.items=items;
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

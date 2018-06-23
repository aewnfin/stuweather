package com.example.helloworld;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.helloworld.weather.db.Area;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class SaveListActivity extends AppCompatActivity {

    private List<Area> areaList = new ArrayList<>();
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_list);
        areaList = DataSupport.findAll(Area.class);//从数据库导入数据
        recyclerView = (RecyclerView) findViewById(R.id.my_save_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);//创建线性布局
        recyclerView.setLayoutManager(layoutManager);//加载线性布局
        AreaAdapter adapter = new AreaAdapter(areaList);
        recyclerView.setAdapter(adapter);
    }

    /*
    内部类 我的收藏 适配器
     */
    class AreaAdapter extends RecyclerView.Adapter<AreaAdapter.ViewHolder> {

        private List<Area> mAreaList;

        class ViewHolder extends RecyclerView.ViewHolder {

            View areaView;
            TextView areaName;
            Button areaDel;

            ViewHolder(View itemView) {
                super(itemView);
                areaView = itemView;
                areaName = (TextView) itemView.findViewById(R.id.area_name);
                areaDel = (Button) itemView.findViewById(R.id.area_del);
            }
        }

        AreaAdapter(List<Area> AreaList) {
            this.mAreaList = AreaList;
        }

        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.save_list_item, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            holder.areaView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    Area area = mAreaList.get(position);
                    Intent intent = new Intent(v.getContext(), WeatherActivity.class);
                    intent.putExtra("weather_id", area.getAreaWeatherId());
                    v.getContext().startActivity(intent);
                    SaveListActivity.this.finish();
                }
            });
            holder.areaDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    Area area = mAreaList.get(position);
                    if (v.getTag() == null) {
                        String areaName = area.getAreaName();
                        List<Area> areas = DataSupport.where("AreaName = ?", areaName).find(Area.class);
                        for (Area areaN : areas) {
                            areaN.delete();
                        }
                        v.setBackgroundResource(R.drawable.ic_undel);
                        Toast.makeText(SaveListActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        v.setTag("del");
                    }else{
                        Area unArea=new Area();
                        unArea.setAreaName(area.getAreaName());
                        unArea.setAreaWeatherId(area.getAreaWeatherId());
                        unArea.save();
                        v.setBackgroundResource(R.drawable.ic_del);
                        Toast.makeText(SaveListActivity.this, "已找回收藏", Toast.LENGTH_SHORT).show();
                        v.setTag(null);
                    }
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Area area = mAreaList.get(position);
            holder.areaName.setText(area.getAreaName());
        }

        @Override
        public int getItemCount() {
            return mAreaList.size();
        }
    }

}

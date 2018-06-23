package com.example.helloworld.weather;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.helloworld.StartActivity;
import com.example.helloworld.base.LogUtil;
import com.example.helloworld.base.MyApplication;
import com.example.helloworld.R;
import com.example.helloworld.WeatherActivity;
import com.example.helloworld.weather.db.City;
import com.example.helloworld.weather.db.County;
import com.example.helloworld.weather.db.Province;
import com.example.helloworld.weather.util.HttpUtil;
import com.example.helloworld.weather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    private List<Province> provinceList;//省列表
    private List<City> cityList;//市列表
    private List<County> countyList;//县列表

    private Province selectedProvince;//选中的省份
    private City selectedCity;//选中的城市

    private int currentLevel;//当前选中级别


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        //获取布局组件ID
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        //创建适配器
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, dataList);
        //为滑动列表 加载 适配器
        listView.setAdapter(adapter);
        return view;
    }

    /*
    当所属活动创建时，加载碎片所需资源、点击事件
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //滑动列表项点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {//当前选中级别为 省
                    //获取选中项 省
                    selectedProvince = provinceList.get(position);
                    //重新加载列表项 市
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {//当前选中级别为 市
                    //获取选中项 市
                    selectedCity = cityList.get(position);
                    //重新加载列表项 县
                    queryCounties();
                }else if (currentLevel==LEVEL_COUNTY){//当前选中级别为 县
                    String weatherId=countyList.get(position).getWeatherId();
                    //页面跳转逻辑
                    if(getActivity() instanceof StartActivity) {
                        LogUtil.d("cityNo", weatherId);
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }
                }
            }
        });
        //返回按钮点击事件
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {//当前选中级别为 县
                    //重新加载列表项 市
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {//当前选中级别为 市
                    //重新加载列表项 省
                    queryProvinces();
                }
            }
        });
        //初次加载列表项的位置
        queryProvinces();
    }

    /*
    查询全国所有省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces() {
        titleText.setText("中国");
        //隐藏返回按钮
        backButton.setVisibility(View.GONE);
        //从数据库查询 所有省
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            //更新 滑动列表-适配器的数据源
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            //使滑动列表-适配器 重新加载 数据源，滑动列表-列表项 此时 刷新
            adapter.notifyDataSetChanged();
            //设置 滑动列表 默认选中第一项
            listView.setSelection(0);
            //滑动列表-列表项 已随 滑动列表-适配器 刷新，当前选择级别 置为 省
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            //去向服务器查询,更新表 省"province"
            queryFromServer(address, "province");
        }
    }

    /*
   查询全国所有市，优先从数据库查询，如果没有查询到再去服务器上查询
    */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceId = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /*
   查询全国所有县，优先从数据库查询，如果没有查询到再去服务器上查询
    */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityId = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /*
    根据传入的地址和类型，从服务器上查询省市县数据
     */
    private void queryFromServer(String address, final String type) {
        //显示提示框 “正在加载...”
        showProgressDialog();
        //自定义类HttpUtil，向服务器请求回调
        HttpUtil.sendOkHttpRequest(address, new Callback() {

            @Override//“加载失败”报错
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread()方法 回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //关闭提示框
                        closeProgressDialog();
                        //显示 Toast土司“加载失败”
                        Toast.makeText(MyApplication.getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override//更新数据库
            public void onResponse(Call call, Response response) throws IOException {
                //拆解获取的返回数据包
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    //自定义类Utility，解析数据，更新数据库
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                //result=true 数据库更新成功，则更新列表项
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    /*
    显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /*
    关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}

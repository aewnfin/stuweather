package com.example.helloworld;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.helloworld.base.ActivityCollector;
import com.example.helloworld.service.AutoUpdateService;
import com.example.helloworld.base.BaseActivity;
import com.example.helloworld.base.LogUtil;
import com.example.helloworld.weather.db.Area;
import com.example.helloworld.weather.frag.ApiFragment;
import com.example.helloworld.weather.frag.ForecastFragment;
import com.example.helloworld.weather.frag.NowFragment;
import com.example.helloworld.weather.frag.SuggestionFragment;
import com.example.helloworld.weather.frag.WindFragment;
import com.example.helloworld.weather.gson.Weather;
import com.example.helloworld.weather.util.HttpUtil;
import com.example.helloworld.weather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends BaseActivity implements View.OnClickListener{
    //左拉菜单
    public DrawerLayout drawerLayout;
    //下拉刷新
    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;
    //滑动组件
    private ScrollView weatherLayout;
    //title
    private TextView titleCity;
    private TextView titleZones;
    private TextView titleUpdateTime;
    //碎片
    private NowFragment nowFragment;
    private ForecastFragment forecastFragment;
    private ApiFragment apiFragment;
    private SuggestionFragment suggestionFragment;
    private WindFragment windFragment;
    //背景
    private ImageView bingPicImg;
    //收藏地区事件 所需数据
    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //界面优化，如果Android版本大于5.0，拉伸应用界面，覆盖状态栏
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        //初始化下拉刷新进度条
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setProgressViewOffset(true, 50, 200);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        //初始化左拉菜单
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        //初始化本活动控件
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleZones = (TextView) findViewById(R.id.time_Zones);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        //初始化碎片
        nowFragment = (NowFragment) getSupportFragmentManager().findFragmentById(R.id.now_fragment);
        forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.forecast_fragment);
        apiFragment = (ApiFragment) getSupportFragmentManager().findFragmentById(R.id.aqi_fragment);
        suggestionFragment = (SuggestionFragment) getSupportFragmentManager().findFragmentById(R.id.suggestion_fragment);
        windFragment = (WindFragment) getSupportFragmentManager().findFragmentById(R.id.wind_fragment);
        //获取传递数据
        mWeatherId = getIntent().getStringExtra("weather_id");
        if (mWeatherId != null) {
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId,true);
        } else {
            //没有获取到传递数据，则从缓存获取数据
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String weatherPrefs = prefs.getString("weather", null);
            Weather weather = Utility.handleWeatherResponse(weatherPrefs);
            if (weather != null) {
                LogUtil.d("Pro1", "weather-OK");
                mWeatherId = weather.basic.weatherId;
                showWeatherInfo(weather);
            } else {
                //解析失败，默认城市为北京
                mWeatherId = "CN101010100";
            }
        }
        //下拉刷新逻辑
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId,false);
            }
        });
        //获取缓存数据,更新背景
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }

        //左拉菜单 默认选中
        navView.setCheckedItem(R.id.nav_find);
        //左拉菜单 点击监听
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_find:
                        Intent intent = new Intent(WeatherActivity.this, StartActivity.class);
                        intent.putExtra("choose", "add");
                        startActivity(intent);
                        break;
                    case R.id.nav_have:
                        Intent intent1 = new Intent(WeatherActivity.this, SaveListActivity.class);
                        startActivity(intent1);
                        break;
                    case R.id.nav_setting:
                        showSetDialog();
                        break;
                    case R.id.nav_pro:
                        Toast.makeText(WeatherActivity.this, "蒋博其 的 期末作业", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });

        //添加 左拉菜单home 监听器
        Button navButton = (Button) findViewById(R.id.nav_button);
        navButton.setOnClickListener(this);

        //添加 添加按钮 监听器
        Button addArea=(Button)findViewById(R.id.add_area);
        addArea.setOnClickListener(this);

        //添加 悬浮按钮 监听器
        Button killAll = (Button) findViewById(R.id.kill_all);
        killAll.setOnClickListener(this);
    }

    /*
    本活动点击事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.nav_button://左拉菜单home 按钮逻辑
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.add_area://添加按钮 点击事件
                List<Area> areas= DataSupport.where("AreaName = ?",cityName).find(Area.class);
                if(areas.isEmpty()){
                    Area area=new Area();
                    area.setAreaName(cityName);
                    area.setAreaWeatherId(mWeatherId);
                    area.save();
                    Toast.makeText(WeatherActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(WeatherActivity.this, "感谢您的支持", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.kill_all://悬浮按钮 点击事件
                ActivityCollector.finishAll();
                break;
            default:
                break;
        }
    }

    /*
    根据天气id请求城市天气数据
     */
    public void requestWeather(final String weatherId, final boolean request) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=8bf22b675e56480993fc36319212265c";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        if(request){
                            WeatherActivity.this.finish();//分支 应对请求新数据失败
                        }else {
                            swipeRefresh.setRefreshing(false);//分支 应对下拉刷新失败
                    }
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "解析天气信息失败", Toast.LENGTH_SHORT).show();
                            if(request){
                                WeatherActivity.this.finish();//分支 应对请求新数据失败
                            }
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    /*
    加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    /*
    处理并展示Weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather) {
        LogUtil.d("Pro1", "showWeatherInfo-start");
        cityName = weather.basic.cityName;
        String timeZones = "时区"+weather.basic.timeZones;
        String updateTime = weather.basic.update.updateTime2.split(" ")[1] + "~" + weather.basic.update.updateTime1.split(" ")[1];

        titleCity.setText(cityName);
        titleZones.setText(timeZones);
        titleUpdateTime.setText(updateTime);

        //碎片更新
        refreshFragment(weather);
        weatherLayout.setVisibility(View.VISIBLE);
        //判断是否开启自动更新服务
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean time = prefs.getBoolean("time",true);
        if(time){
            AutoUpdateService.serviceStart(WeatherActivity.this);
        }
        LogUtil.d("Pro1", "showWeatherInfo-end");
    }

    /*
    更新碎片
     */
    private void refreshFragment(Weather weather) {
        nowFragment.reFlash(weather);
        forecastFragment.reFlash(weather);
        suggestionFragment.reFlash(weather);
        apiFragment.reFlash(weather);
        windFragment.reFlash(weather);
    }

    //设置 设置按钮对话框显示
    private void showSetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(WeatherActivity.this)  //先得到对话框构造器
                .setTitle("设置") //设置标题
                .setMessage("是否开启后台自动更新?") //设置内容
                .setIcon(R.drawable.ic_time)
                .setPositiveButton("开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); //关闭dialog
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putBoolean("time", true);
                        editor.apply();
                        AutoUpdateService.serviceStart(WeatherActivity.this);//开启服务
                        Toast.makeText(WeatherActivity.this, "自动更新已开启", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("关闭", new DialogInterface.OnClickListener() { //设置取消按钮
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putBoolean("time", false);
                        editor.apply();
                        Intent intent=new Intent(WeatherActivity.this,AutoUpdateService.class);
                        stopService(intent);
                        Toast.makeText(WeatherActivity.this, "自动更新已关闭", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("忽略", new DialogInterface.OnClickListener() {//设置忽略按钮
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        //参数都设置完成了，创建并显示出来
        builder.create().show();
    }

}

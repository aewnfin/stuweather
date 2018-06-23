package com.example.helloworld.weather.db;


import org.litepal.crud.DataSupport;

public class Area extends DataSupport{
    private int id;
    private String AreaName;
    private String AreaWeatherId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Area(){}

    public Area(String areaName, String areaWeatherId) {
        AreaName = areaName;
        AreaWeatherId = areaWeatherId;
    }

    public String getAreaName() {
        return AreaName;
    }

    public void setAreaName(String areaName) {
        AreaName = areaName;
    }

    public String getAreaWeatherId() {
        return AreaWeatherId;
    }

    public void setAreaWeatherId(String areaWeatherId) {
        AreaWeatherId = areaWeatherId;
    }
}

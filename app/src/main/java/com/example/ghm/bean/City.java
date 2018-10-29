//用来定义City的数据
package com.example.ghm.bean;

public class City {
    private String province;
    private String city;
    private String number;
    private String firstPY;
    private String allPY;
    private String allFisrstPY;

    public City(String province, String city, String number, String firstPY, String allPY, String allFisrstPY){
        this.allFisrstPY = allFisrstPY;
        this.allPY = allPY;
        this.city = city;
        this.firstPY = firstPY;
        this.number= number;
        this.province = province;
    }

    public String getAllFisrstPY() {
        return allFisrstPY;
    }

    public String getAllPY() {
        return allPY;
    }

    public String getCity() {
        return city;
    }

    public String getFirstPY() {
        return firstPY;
    }

    public String getNumber() {
        return number;
    }

    public String getProvince() {
        return province;
    }

}

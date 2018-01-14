package com.exampl.android;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.exampl.android.db.City;
import com.exampl.android.db.County;
import com.exampl.android.db.Province;
import com.exampl.android.util.HttpUtil;
import com.exampl.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class ChooseAreaFragment extends Fragment {
    private static final String TAG = "ChooseAreaFragment";

    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;
    private ListView listView;
    private Button backButton;
    private TextView titleText;
    private ArrayAdapter<String> adapter;
    private List<String> datalist =new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;
    private ProgressDialog progressDialog;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
       adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,datalist);
       listView.setAdapter(adapter);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queeryProvinces();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "currentlevel"+currentLevel);
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    Log.d(TAG, "onItemClick: 1");
                    
                    queeryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queeryCounties();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel == LEVEL_COUNTY){

                    queeryCities();
                }else if(currentLevel == LEVEL_CITY){

                    queeryProvinces();
                }
            }
        });
    }

    private void queeryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            datalist.clear();
            for(Province province:provinceList){
                datalist.add(province.getProvinceName());

            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;

        }else {
            String adress = "http://guolin.tech/api/china";
            queeryFromServer(adress,"province");
        }

    }



    private void queeryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityId=?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            datalist.clear();
            for(County county:countyList){
                datalist.add(county.getCountyName());

            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;

        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String adress = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queeryFromServer(adress,"county");
        }

    }

    private void queeryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceId=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            Log.d(TAG, "queeryCities: 2");
            datalist.clear();
            for(City city:cityList){
                datalist.add(city.getCityName());

            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;

        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            String adress = "http://guolin.tech/api/china/"+provinceCode;
            Log.d(TAG, adress);
            queeryFromServer(adress,"city");
        }
    }

    private void queeryFromServer(String adress, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(adress, new Callback() {

            @Override
             public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if("province".equals(type)){
                   result = Utility.handleProvinceReponse(responseText);
                }else if ("city".equals(type)){
                    Log.d(TAG, "onResponse: ");
                    result = Utility.handleCityReponse(responseText,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountyReponse(responseText,selectedCity.getId());
                }
                if (result){
                    closePrograssDialog();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if("province".equals(type)){
                                queeryProvinces();
                            }else if ("city".equals(type)){
                                queeryCities();
                            }else if ("county".equals(type)){
                                queeryCounties();
                            }
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closePrograssDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });

            }


        });
    }
    private void showProgressDialog(){
        if(progressDialog==null){
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("正在加载中........");
        progressDialog.setCanceledOnTouchOutside(false);

        }
        progressDialog.show();
    }

    private void closePrograssDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }
}

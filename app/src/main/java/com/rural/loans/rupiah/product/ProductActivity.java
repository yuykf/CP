package com.rural.loans.rupiah.product;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.rural.loans.rupiah.R;
import com.rural.loans.rupiah.bean.InitBean;
import com.rural.loans.rupiah.bean.ProductBean;
import com.rural.loans.rupiah.bean.ResponseBean;
import com.rural.loans.rupiah.global.GlobalApi;
import com.rural.loans.rupiah.global.GlobalData;
import com.rural.loans.rupiah.global.GlobalUpTotal;
import com.rural.loans.rupiah.main.MainActivity;
import com.rural.loans.rupiah.ui.LoadingDialog;
import com.rural.loans.rupiah.util.AppUtils;
import com.rural.loans.rupiah.util.DesUtils;
import com.rural.loans.rupiah.util.DeviceUtil;
import com.rural.loans.rupiah.util.StringUtil;
import com.rural.loans.rupiah.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductActivity extends AppCompatActivity {

    private ImageView mIvBack;
    private TextView mTvProductName;
    private ImageView mIvLogo;
    private ScrollView mScrollView;
    private TextView mTvScore;
    private TextView mTvPayAmount;
    private TextView mTvLoanAmount;
    private TextView mTvRateAmount;
    private AppCompatSpinner mSpAmount;
    private AppCompatSpinner mSpDay;
    private TextView mTvTiaojian;
    private TextView mTvCailiao;
    private TextView mTvShuoming;
    private TextView mTvDown;

    private String productId;

    private ProductBean mProductBean;

    private List<String> amountList = new ArrayList<>();
    private String amount;
    private List<String> dayList = new ArrayList<>();
    private String day;
    private ArrayAdapter amountAdapter;
    private ArrayAdapter dayAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        initActivity();
    }


    private void initActivity(){

        mIvBack = findViewById(R.id.iv_back);
        mTvProductName = findViewById(R.id.tv_product_name);
        mIvLogo = findViewById(R.id.iv_logo);
        mScrollView = findViewById(R.id.scrollView);
        mTvScore = findViewById(R.id.tv_score);
        mTvPayAmount = findViewById(R.id.tv_pay_amount);
        mTvLoanAmount = findViewById(R.id.tv_loan_amount);
        mTvRateAmount = findViewById(R.id.tv_rate_amount);
        mSpAmount = findViewById(R.id.sp_amount);
        mSpDay = findViewById(R.id.sp_day);
        mTvTiaojian = findViewById(R.id.tv_tiaojian);
        mTvCailiao = findViewById(R.id.tv_cailiao);
        mTvShuoming = findViewById(R.id.tv_shuoming);
        mTvDown = findViewById(R.id.tv_down);
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTvDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadApp();
            }
        });

        productId = getIntent().getStringExtra("productId");

        GlobalUpTotal.upProducAccess(ProductActivity.this, productId);

        pullDetail();

    }


    private void pullDetail(){

        LoadingDialog.show(ProductActivity.this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GlobalApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ProductApi productApi = retrofit.create(ProductApi.class);

        String iv = GlobalData.getIV();
        String data = GlobalData.getParams(iv)
                .setParams("id", productId)
                .build();

        productApi.pullProductDetail(iv, data).enqueue(new Callback<ResponseBean>() {
            @Override
            public void onResponse(Call<ResponseBean> call, Response<ResponseBean> response) {
                LoadingDialog.dismiss(ProductActivity.this);
                if(null != response.body()){
                    if(response.body().getCode() == 0){
                        String data = response.body().getData();
                        try{
                            data = DesUtils.decode(data, response.body().getIv());
                            mProductBean = new Gson().fromJson(data, ProductBean.class);
                            setUI();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else{
                        ToastUtil.showCenter(ProductActivity.this, response.body().getMsg());
                    }
                }else{
                    ToastUtil.showCenter(ProductActivity.this, R.string.server_unconnect);
                }
            }

            @Override
            public void onFailure(Call<ResponseBean> call, Throwable t) {
                LoadingDialog.dismiss(ProductActivity.this);
                ToastUtil.showCenter(ProductActivity.this, R.string.server_unconnect);
            }
        });

    }

    private void setUI(){

        if(null != mProductBean){

            mScrollView.setVisibility(View.VISIBLE);
            mTvDown.setVisibility(View.VISIBLE);

            mTvProductName.setText(StringUtil.getText(mProductBean.getName()));
            Glide.with(ProductActivity.this)
                    .load(mProductBean.getLogo())
                    .into(mIvLogo);

            mTvScore.setText(getString(R.string.detail_score,
                    StringUtil.getText(mProductBean.getLulus_score()),
                    StringUtil.getText(mProductBean.getKecepatan_score()),
                    StringUtil.getText(mProductBean.getPenagihan_score())));
            mTvTiaojian.setText(StringUtil.getText(mProductBean.getConditions_apply()));
            mTvCailiao.setText(StringUtil.getText(mProductBean.getMaterial_requested()));
            mTvShuoming.setText(StringUtil.getText(mProductBean.getDeclare()));

            try {
                int maxAmount = Integer.parseInt(mProductBean.getLoan_amount_max());
                int minAmout = Integer.parseInt(mProductBean.getLoan_amount_min());

                int section = maxAmount - minAmout;
                if (section > 0) {
                    int interval;
                    if (minAmout < 2000000) {
                        if (maxAmount < 2000000) {
                            interval = 100000;
                        } else if (maxAmount > 2000000 && maxAmount < 10000000) {
                            interval = 500000;
                        } else {
                            interval = 1000000;
                        }
                    } else if (minAmout > 2000000 && minAmout < 10000000) {
                        if (maxAmount < 10000000) {
                            interval = 500000;
                        } else {
                            interval = 1000000;
                        }
                    } else {
                        interval = 1000000;
                    }
                    int count = (section / interval) - (section % interval == 0 ? 1 : 0);
                    amountList.clear();
                    amountList.add(getString(R.string.amount, String.valueOf(minAmout)));
                    if (count > 0) {
                        for (int i = 1; i <= count; i++) {
                            amountList.add(getString(R.string.amount, String.valueOf((i * interval + minAmout))));
                        }
                    }
                }

                amountList.add(getString(R.string.amount, String.valueOf(maxAmount)));
                amount = amountList.get(0).substring(3, amountList.get(0).length());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                mScrollView.setVisibility(View.GONE);
                mTvDown.setVisibility(View.GONE);
            }

            amountAdapter = new ArrayAdapter<>(ProductActivity.this,
                    R.layout.simple_spinner_item, amountList);
            amountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpAmount.setAdapter(amountAdapter);
            mSpAmount.setPrompt(amountList.get(0));

            if (null != mProductBean.getLoan_days() && mProductBean.getLoan_days().size() > 0) {
                dayList.clear();
                for (int i = 0; i < mProductBean.getLoan_days().size(); i++) {
                    dayList.add(getString(R.string.times, mProductBean.getLoan_days().get(i)));
                }
            }
            day = dayList.get(0).substring(0, dayList.get(0).length() - 5);
            dayAdapter = new ArrayAdapter<>(ProductActivity.this,
                    R.layout.simple_spinner_item, dayList);
            dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpDay.setAdapter(dayAdapter);
            mSpDay.setPrompt(dayList.get(0));

            setData();

            mSpAmount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    amount = amountList.get(i).substring(3, amountList.get(i).length());
                    mSpAmount.setPrompt(amountList.get(i));
                    setData();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            mSpDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    day = dayList.get(i).substring(0, dayList.get(i).length() - 5);
                    mSpDay.setPrompt(dayList.get(i));
                    setData();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

    }

    private void setData() {
        float rate_interest = Float.valueOf(mProductBean.getRate_interest()) / 100;
        int interest = (int) (Integer.parseInt(amount) * rate_interest *
                Integer.parseInt(day));
        mTvPayAmount.setText(getString(R.string.amount,
                StringUtil.formatString(Integer.parseInt(amount) + interest)));
        mTvLoanAmount.setText(getString(R.string.amount,
                StringUtil.formatString(Integer.parseInt(amount))));
        mTvRateAmount.setText(getString(R.string.amount, StringUtil.formatString(interest)));
    }


    private void downloadApp(){
        LoadingDialog.show(ProductActivity.this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GlobalApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ProductApi productApi = retrofit.create(ProductApi.class);

        String iv = GlobalData.getIV();
        String data = GlobalData.getParams(iv)
                .setParams("id", productId)
                .setParams("loan_amount", amount)
                .setParams("loan_days", day)
                .build();
        productApi.applyProduct(iv, data).enqueue(new Callback<ResponseBean>() {
            @Override
            public void onResponse(Call<ResponseBean> call, Response<ResponseBean> response) {
                LoadingDialog.dismiss(ProductActivity.this);
                if(null != response.body()){
                    if(response.body().getCode() == 0){
                        Intent intent = new Intent(ProductActivity.this, WebViewActivity.class);
                        if (!TextUtils.isEmpty(mProductBean.getAppsflyer_url())) {
                            String clickId = System.currentTimeMillis() / 1000 + "_" +
                                    StringUtil.getRandoms(4);
                            intent.putExtra("title", mProductBean.getName());
                            intent.putExtra("packageName", mProductBean.getPackage_name());
                            intent.putExtra("loadUrl", mProductBean.getAppsflyer_url()
                                    + "&clickid=" + clickId
                                    + "&&af_siteid=" + GlobalData.APP_NUMBER
                                    + "&advertising_id=" + DeviceUtil.getUniqueId(ProductActivity.this));
                            startActivity(intent);
                        } else {
                            if (!TextUtils.isEmpty(mProductBean.getUrl())) {
                                if (mProductBean.getType().equals("1")) {
                                    AppUtils.jumpAppStore(ProductActivity.this,
                                            mProductBean.getUrl());
                                } else {
                                    intent.putExtra("title", mProductBean.getName());
                                    intent.putExtra("loadUrl", mProductBean.getUrl());
                                    intent.putExtra("productId", mProductBean.getId());
                                    startActivity(intent);
                                }
                            }
                        }
                    }else{
                        ToastUtil.showCenter(ProductActivity.this, response.body().getMsg());
                    }
                }else{
                    ToastUtil.showCenter(ProductActivity.this, R.string.server_unconnect);
                }
            }

            @Override
            public void onFailure(Call<ResponseBean> call, Throwable t) {
                LoadingDialog.dismiss(ProductActivity.this);
                ToastUtil.showCenter(ProductActivity.this, R.string.server_unconnect);
            }
        });
    }

}
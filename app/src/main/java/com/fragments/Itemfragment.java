package com.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.adapter.files.ItemAvailabilityRecycleAdapter;
import com.luis.store.R;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class Itemfragment extends Fragment  implements ItemAvailabilityRecycleAdapter.OnItemClickListener{

    View view;
    public GeneralFunctions generalFunc;
    public String userProfileJson;
    MTextView titleTxt;
    ImageView backImgView;

    ProgressBar loading_history;

    boolean mIsLoading = false;
    boolean isNextPageAvailable = false;

    MTextView noOrdersTxt;

    RecyclerView historyRecyclerView;
    ErrorView errorView;

    String next_page_str = "";

    ItemAvailabilityRecycleAdapter itemAvailabilityRecyclerAdapter;

    ArrayList<HashMap<String, String>> listData = new ArrayList<>();

    String previousHeaderCategory = "";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_item_availability, container, false);
        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

        titleTxt = (MTextView) view.findViewById(R.id.titleTxt);
        backImgView = (ImageView) view.findViewById(R.id.backImgView);
        backImgView.setVisibility(View.GONE);

        loading_history = (ProgressBar) view.findViewById(R.id.loading_history);
        historyRecyclerView = (RecyclerView) view.findViewById(R.id.historyRecyclerView);
        noOrdersTxt = (MTextView) view.findViewById(R.id.noOrdersTxt);
        errorView = (ErrorView) view.findViewById(R.id.errorView);

        itemAvailabilityRecyclerAdapter = new ItemAvailabilityRecycleAdapter(getActContext(), listData, generalFunc, true);
        historyRecyclerView.setAdapter(itemAvailabilityRecyclerAdapter);
        itemAvailabilityRecyclerAdapter.setOnItemClickListener(this);


        historyRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int firstVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                int lastInScreen = firstVisibleItemPosition + visibleItemCount;
                if ((lastInScreen == totalItemCount) && !(mIsLoading) && isNextPageAvailable == true) {

                    mIsLoading = true;
                    itemAvailabilityRecyclerAdapter.addFooterView();

                    getPastOrders(true);

                } else if (isNextPageAvailable == false) {
                    itemAvailabilityRecyclerAdapter.removeFooterView();
                }
            }
        });

        setLabels();

        getPastOrders(false);

        return  view;
    }

    public Context getActContext() {
        return getActivity();
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ITEMS"));
    }



    @Override
    public void onItemClickList(int position) {

    }

    @Override
    public void switchOnlineOffline(boolean isOnlineAvoid, int position) {
        switchOnOff(listData.get(position).get("iMenuItemId"), listData.get(position).get("eAvailable"), isOnlineAvoid);
    }


    private void switchOnOff(String iMenuItemId, String eAvailable, boolean isOnline) {
        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateFoodMenuItemForRestaurant");
        parameters.put("iMenuItemId", iMenuItemId);
        parameters.put("eAvailable", isOnline == true ? "Yes" : "No");

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseObj=generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {

                closeLoader();

                boolean isDataAvail = generalFunc.checkDataAvail(Utils.action_str, responseObj);
                if (isDataAvail == true) {
                    getPastOrders(false);
                }
                generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void getPastOrders(boolean isLoadMore) {

        if (isLoadMore == false) {
            listData.clear();
            previousHeaderCategory = "";
            itemAvailabilityRecyclerAdapter.notifyDataSetChanged();
            isNextPageAvailable = false;
            mIsLoading = true;
        }

        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (loading_history.getVisibility() != View.VISIBLE && isLoadMore == false) {
            loading_history.setVisibility(View.VISIBLE);
        }

        noOrdersTxt.setVisibility(View.GONE);

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "ManageFoodItem");
        parameters.put("iGeneralUserId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);
        if (isLoadMore == true) {
            parameters.put("page", next_page_str);
        }

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {
            noOrdersTxt.setVisibility(View.GONE);
            JSONObject responseObj=generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {

                closeLoader();

                if (generalFunc.checkDataAvail(Utils.action_str, responseObj) == true) {
                    String nextPage = generalFunc.getJsonValueStr("NextPage", responseObj);
                    JSONArray arr_orders = generalFunc.getJsonArray(Utils.message_str, responseObj);

                    if (arr_orders != null) {
                        for (int i = 0; i < arr_orders.length(); i++) {
                            JSONObject obj_temp = generalFunc.getJsonObject(arr_orders, i);


                            String CategoryName = generalFunc.getJsonValueStr("CategoryName", obj_temp);

                            if (!previousHeaderCategory.equalsIgnoreCase(CategoryName)) {

                                HashMap<String, String> mapHeader = new HashMap<String, String>();
                                mapHeader.put("CategoryName", CategoryName);
                                mapHeader.put("TYPE", "" + ItemAvailabilityRecycleAdapter.TYPE_HEADER);

                                listData.add(mapHeader);
                                previousHeaderCategory = CategoryName;
                            }


                            JSONArray arr_date = generalFunc.getJsonArray(Utils.data_str, obj_temp);
                            for (int j = 0; j < arr_date.length(); j++) {
                                HashMap<String, String> map = new HashMap<String, String>();

                                JSONObject obj_date_temp = generalFunc.getJsonObject(arr_date, j);
                                String MenuItemName=generalFunc.getJsonValueStr("MenuItemName", obj_date_temp);
                                String fPrice=generalFunc.getJsonValueStr("fPrice", obj_date_temp);
                                map.put("MenuItemName", MenuItemName);
                                map.put("MenuItemNameConverted", generalFunc.convertNumberWithRTL(MenuItemName));

                                map.put("iMenuItemId", generalFunc.getJsonValueStr("iMenuItemId", obj_date_temp));

                                map.put("fPrice", fPrice);
                                map.put("fPriceConverted", generalFunc.convertNumberWithRTL(fPrice));

                                map.put("eAvailable", generalFunc.getJsonValueStr("eAvailable", obj_date_temp));
                                map.put("TYPE", "" + ItemAvailabilityRecycleAdapter.TYPE_ITEM);
                                map.put("LBL_IN_STOCK",generalFunc.retrieveLangLBl("","LBL_IN_STOCK"));
                                map.put("LBL_NOT_AVAILABLE",generalFunc.retrieveLangLBl("","LBL_NOT_AVAILABLE"));
                                map.put("vService_BG_color", generalFunc.getJsonValueStr("vService_BG_color", obj_date_temp));
                                map.put("vService_TEXT_color", generalFunc.getJsonValueStr("vService_TEXT_color", obj_date_temp));

                                listData.add(map);
                            }
                        }
                    }
                    if (!nextPage.equals("") && !nextPage.equals("0")) {
                        next_page_str = nextPage;
                        isNextPageAvailable = true;
                    } else {
                        removeNextPageConfig();
                    }

                    itemAvailabilityRecyclerAdapter.notifyDataSetChanged();

                } else {
                    if (listData.size() == 0) {
                        removeNextPageConfig();
                        noOrdersTxt.setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
                        noOrdersTxt.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (isLoadMore == false) {
                    removeNextPageConfig();
                    generateErrorView();
                }

            }
            mIsLoading = false;

        });
        exeWebServer.execute();
    }

    public void removeNextPageConfig() {
        next_page_str = "";
        isNextPageAvailable = false;
        mIsLoading = false;
        itemAvailabilityRecyclerAdapter.removeFooterView();
    }

    public void closeLoader() {
        if (loading_history.getVisibility() == View.VISIBLE) {
            loading_history.setVisibility(View.GONE);
        }
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(() -> getPastOrders(false));
    }


}

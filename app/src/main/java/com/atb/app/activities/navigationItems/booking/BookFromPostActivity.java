package com.atb.app.activities.navigationItems.booking;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.applikeysolutions.cosmocalendar.listeners.OnMonthChangeListener;
import com.applikeysolutions.cosmocalendar.model.Month;
import com.applikeysolutions.cosmocalendar.selection.BaseSelectionManager;
import com.applikeysolutions.cosmocalendar.selection.OnDaySelectedListener;
import com.applikeysolutions.cosmocalendar.selection.SingleSelectionManager;
import com.applikeysolutions.cosmocalendar.settings.SettingsManager;
import com.applikeysolutions.cosmocalendar.settings.appearance.ConnectedDayIconPosition;
import com.applikeysolutions.cosmocalendar.settings.lists.connected_days.ConnectedDays;
import com.applikeysolutions.cosmocalendar.utils.SelectionType;
import com.applikeysolutions.cosmocalendar.view.CalendarView;
import com.atb.app.R;
import com.atb.app.activities.navigationItems.BookingActivity;
import com.atb.app.activities.navigationItems.PurchasesActivity;
import com.atb.app.activities.newsfeedpost.NewsDetailActivity;
import com.atb.app.adapter.BookingListAdapter;
import com.atb.app.api.API;
import com.atb.app.application.AppController;
import com.atb.app.base.CommonActivity;
import com.atb.app.commons.Commons;
import com.atb.app.commons.Constants;
import com.atb.app.commons.Helper;
import com.atb.app.dialog.ConfirmBookingDialog;
import com.atb.app.dialog.ConfirmDialog;
import com.atb.app.dialog.DepositDialog;
import com.atb.app.dialog.PaymentSuccessDialog;
import com.atb.app.dialog.ServiceBookingSuccessDialog;
import com.atb.app.model.BookingEntity;
import com.atb.app.model.NewsFeedEntity;
import com.atb.app.model.UserModel;
import com.atb.app.model.VariationModel;
import com.atb.app.model.submodel.DisableSlotModel;
import com.atb.app.model.submodel.HolidayModel;
import com.atb.app.util.RoundedCornersTransformation;

import com.braintreepayments.api.models.VenmoAccountNonce;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

public class BookFromPostActivity extends CommonActivity implements View.OnClickListener , OnDaySelectedListener {
    NewsFeedEntity newsFeedEntity = new NewsFeedEntity();
    ImageView imv_back,imv_selector;
    LinearLayout lyt_selector;
    ListView list_booking;
    int EndDate = 0,day = -1,year,month;
    ArrayList<BookingEntity>bookingEntities = new ArrayList<>();
    ArrayList<BookingEntity>AllbookingEntities = new ArrayList<>();

    ArrayList<ArrayList<String>> bookingSlot  = new ArrayList<>();
    String[] months;
    BookingListAdapter bookingListAdapter ;
    HashMap<String,BookingEntity>hashMap = new HashMap<>();
    ArrayList<String> selected_bookingSlot  = new ArrayList<>();
    TextView txv_booking;
    CalendarView calendarView;
    UserModel userModel = new UserModel();
    Map<String, String> payment_params = new HashMap<>();
    BookingEntity bookingEntity = new BookingEntity();
    TextView txv_name,txv_title;
    ImageView imv_image;
    long today = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_from_post);
        if (getIntent() != null) {
            Bundle bundle = getIntent().getBundleExtra("data");
            if (bundle != null) {
                String feed= bundle.getString("newsFeedEntity");
                Gson gson = new Gson();
                newsFeedEntity = gson.fromJson(feed, NewsFeedEntity.class);
                userModel = newsFeedEntity.getUserModel();
            }
        }

        imv_back = findViewById(R.id.imv_back);
        imv_selector = findViewById(R.id.imv_selector);
        lyt_selector = findViewById(R.id.lyt_selector);
        list_booking = findViewById(R.id.list_booking);
        calendarView = findViewById(R.id.calendar_view);
        txv_booking = findViewById(R.id.txv_booking);
        imv_image = findViewById(R.id.imv_image);
        txv_name = findViewById(R.id.txv_name);
        txv_title = findViewById(R.id.txv_title);

        txv_booking.setOnClickListener(this);
        imv_back.setOnClickListener(this);
        lyt_selector.setOnClickListener(this);
        bookingListAdapter = new BookingListAdapter(this,1);
        list_booking.setAdapter(bookingListAdapter);
        list_booking.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BookingEntity bookingEntity = hashMap.get(selected_bookingSlot.get(position));
            }
        });

        calendarView.setSelectionType(SelectionType.SINGLE);
        calendarView.setOnMonthChangeListener(new OnMonthChangeListener() {
            @Override
            public void onMonthChanged(Month month) {
                String mon = month.getMonthName().split(" ")[0];
                int year1 = Integer.parseInt(month.getMonthName().split(" ")[1]);
                Calendar c = Calendar.getInstance();
                int month_int = Commons.getMonthnumber(mon);
                loadBooking(year1, month_int);
                day = -1;
            }
        });
        calendarView.setSelectionManager(new SingleSelectionManager(this));
        loadLayout();

        getAllBookings();
    }



    void loadLayout(){
        months = new DateFormatSymbols(Locale.ENGLISH).getShortMonths();
        Calendar c = Calendar.getInstance();
        int year=c.get(Calendar.YEAR);
        int month=c.get(Calendar.MONTH);
        EndDate = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        txv_title.setText(newsFeedEntity.getTitle()+ " at ");
        txv_name.setText(newsFeedEntity.getUserModel().getBusinessModel().getBusiness_name());
        Glide.with(_context).load(newsFeedEntity.getPostImageModels().get(0).getPath()).placeholder(R.drawable.image_thumnail).into(imv_image);
        today= c.getTimeInMillis()-24*3600000;
        loadBooking(year,month);
    }
    void loadBooking(int year,int month){
        imv_selector.setEnabled(false);
        this.year =year;
        this.month =month;
        bookingListAdapter.init();
        Helper.getListViewSize(list_booking);
        Set<Long> disabledDaysSet = new HashSet<>();
        ArrayList<ArrayList<String>> slots  = userModel.getBusinessModel().getSlots();
        bookingSlot.clear();
        for(int i =1;i<=EndDate;i++){
            Calendar c = Calendar.getInstance();
            c.set(year,month,i);
            int weekDay = (c.get(Calendar.DAY_OF_WEEK)+7)%8;
            if(weekDay ==0)weekDay = 7;
            ArrayList<String>arrayList = new ArrayList<>();
            arrayList.clear();
            arrayList.addAll(slots.get(weekDay-1));
            bookingSlot.add(arrayList);
        }
        for(int i =0;i<userModel.getBusinessModel().getHolidayModels().size();i++){
            HolidayModel holidayModel = userModel.getBusinessModel().getHolidayModels().get(i);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(holidayModel.getDay_off()*1000);
            long start = holidayModel.getDay_off();
            for(int j =0;j<24*3600;j+=1800){

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis((holidayModel.getDay_off()+j)*1000);
                int _day = calendar.get(Calendar.DAY_OF_MONTH);
                for(int ii=0;ii<bookingSlot.get(_day-1).size();ii++){
                    int milion = (int)start+j;
                    if(Commons.gettimeFromMilionSecond(milion).equals(bookingSlot.get(_day-1).get(ii))){
                        bookingSlot.get(_day-1).remove(ii);
                        break;
                    }
                }
            }
        }
//        Set<Long> days = new HashSet<>();
//        for(int i =1;i<=EndDate;i++){
//            Calendar c = Calendar.getInstance();
//            c.set(year,month,i);
//            if(bookingSlot.get(i-1).size()==0) {
//                disabledDaysSet.add(c.getTimeInMillis());
//
//
//            }
//        }

        getBooking();
    }

    void getBooking(){
        showProgress();
        StringRequest myRequest = new StringRequest(
                Request.Method.POST,
                API.GET_BOOKING,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String json) {
                        closeProgress();
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            JSONArray arrayList = jsonObject.getJSONArray("extra");
                            bookingEntities.clear();
                            for(int i =0;i<arrayList.length();i++){
                                BookingEntity bookingEntity = new BookingEntity();
                                bookingEntity.initModel(arrayList.getJSONObject(i));
                                bookingEntities.add(bookingEntity);

                            }
                            if(day>=0)loadBookingByday(day);
//                            setConnectDay();

                        }catch (Exception e){

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        closeProgress();
                        //showToast(error.getMessage());

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("token", Commons.token);
                params.put("user_id", String.valueOf(userModel.getId()));
                params.put("is_business", "1");
                params.put("month", String.valueOf(year) +" " + String.valueOf(month+1));
                return params;
            }
        };
        myRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(myRequest, "tag");
    }

    void setConnectDay(){
        Set<Long> connectDay = new HashSet<>();
        int textColor = Color.parseColor("#C4000E");
        for(int i =0;i<AllbookingEntities.size();i++){
            connectDay.add(AllbookingEntities.get(i).getBooking_datetime()*1000l);
        }

        int selectedTextColor = Color.parseColor("#ffffff");
        int disabledTextColor = Color.parseColor("#ff8000");
        ConnectedDays connectedDays = new ConnectedDays(connectDay, textColor, selectedTextColor, disabledTextColor);
        SettingsManager settingsManager = calendarView.getSettingsManager();
        calendarView.addConnectedDays(connectedDays);
//        calendarView.setConnectedDayIconRes(R.drawable.dot_icon);   // Drawable
//        calendarView.setConnectedDayIconPosition(ConnectedDayIconPosition.BOTTOM);
        calendarView.update();


    }

    void loadBookingByday(int day){
        hashMap.clear();
        this.day = day;
        txv_booking.setVisibility(View.GONE);
        int allday_flag = -1;
        for(int i =0;i<bookingSlot.get(day).size();i++){
            BookingEntity bookingEntity = new BookingEntity();
            bookingEntity.setBooking_datetime(getMilonSecond(bookingSlot.get(day).get(i)));
            bookingEntity.setBookingDuration(Commons.gettimeFromMilionSecond(bookingEntity.getBooking_datetime()) +" - " + Commons.gettimeFromMilionSecond(bookingEntity.getBooking_datetime()+1800));
            int bookslot_id = slotBooked(bookingSlot.get(day).get(i));
            if(bookslot_id>=0){
                hashMap.put(bookingSlot.get(day).get(i), bookingEntities.get(bookslot_id));
                if(newsFeedEntity.getDuration().equals("99")){
                    allday_flag = bookslot_id;
                    break;
                }
            }
            else {
                int disableSlot_id = disableSlot(bookingSlot.get(day).get(i));
                // bookingEntity.setType(-1);
                if(disableSlot_id<0)
                    hashMap.put(bookingSlot.get(day).get(i), bookingEntity);
                else{
                    bookingEntity.setType(-1);
                    hashMap.put(bookingSlot.get(day).get(i), bookingEntity);
                }

            }
        }

        if(allday_flag!=-1){
            hashMap.clear();
            for(int i =0;i<bookingSlot.get(day).size();i++) {
                hashMap.put(bookingSlot.get(day).get(i), bookingEntities.get(allday_flag));
            }
        }
        selected_bookingSlot.clear();
        if(newsFeedEntity.getDuration().equals("99") && allday_flag ==-1){
            selected_bookingSlot.add(bookingSlot.get(day).get(0));
            hashMap.clear();
            BookingEntity bookingEntity = new BookingEntity();
            bookingEntity.setBooking_datetime(getMilonSecond(bookingSlot.get(day).get(0)));
            bookingEntity.setBookingDuration("All Day");
            hashMap.put(bookingSlot.get(day).get(0),bookingEntity);
        }else{
            selected_bookingSlot.addAll(bookingSlot.get(day));
        }
        bookingListAdapter.setRoomData(hashMap,selected_bookingSlot);

        Helper.getListViewSize(list_booking);
    }
    int disableSlot(String str){
        for(int i = 0; i< userModel.getBusinessModel().getDisableSlotModels().size(); i++){
            int milionSecond = getMilonSecond(str);
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = null;
            int time =0;
            try {
                d = df.parse(userModel.getBusinessModel().getDisableSlotModels().get(i).getStart());
                time = (int)d.getTime()/1000;
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Long disableMilionSecond = userModel.getBusinessModel().getDisableSlotModels().get(i).getDay_timestamp() + time;
            if(milionSecond == disableMilionSecond)
                return i;
        }

        return -1;
    }
    int slotBooked(String str){
        for(int i =0;i<bookingEntities.size();i++){
            if(bookingEntities.get(i).getState().equals("cancelled") || bookingEntities.get(i).getState().equals("complete"))continue;
            int milionSecond = getMilonSecond(str);
            int end_time = ( bookingEntities.get(i).getBooking_datetime() + 3600 * Integer.parseInt(bookingEntities.get(i).getNewsFeedEntity().getDuration()));
            if(bookingEntities.get(i).getNewsFeedEntity().getDuration().equals("99")){
                Date curDate = new Date( bookingEntities.get(i).getBooking_datetime()*1000l);
                curDate.setHours(24);
                end_time = (int) (curDate.getTime()/1000l);
            }

            if(milionSecond >= bookingEntities.get(i).getBooking_datetime() && milionSecond<end_time)
                return i;

        }

        return -1;
    }
    int getMilonSecond(String str ){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
        Date date = null;
        try {
            date = df.parse(str);

        }catch (Exception e){

        }
        calendar.set(year,month,day+1,date.getHours(),date.getMinutes(),0);
        int milionSecond = (int)(calendar.getTimeInMillis()/1000l);
        return milionSecond;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imv_back:
                finish(this);

                break;
            case R.id.lyt_selector:
                imv_selector.setEnabled(!imv_selector.isEnabled());
                selected_bookingSlot.clear();
                if(imv_selector.isEnabled()){
                    for(int i =0;i<bookingSlot.get(day).size();i++){
                        if(hashMap.get(bookingSlot.get(day).get(i)).getType()==0) {
                            selected_bookingSlot.add(bookingSlot.get(day).get(i));
                        }
                    }
                }else {
                    selected_bookingSlot.addAll(bookingSlot.get(day));
                }

                bookingListAdapter.setRoomData(hashMap,selected_bookingSlot);
                Helper.getListViewSize(list_booking);
                break;
            case R.id.txv_booking:
                if(Float.parseFloat(newsFeedEntity.getDeposit())>0){
                    DepositDialog depositDialog = new DepositDialog();
                    depositDialog.setOnConfirmListener(new DepositDialog.OnConfirmListener() {
                        @Override
                        public void onPurchase() {
                            ArrayList<String> selected_Variation = new ArrayList<>();
                            payment_params.clear();
                            payment_params.put("token",Commons.token);
                            payment_params.put("amount",String.valueOf(newsFeedEntity.getDeposit()));
                            payment_params.put("toUserId", String.valueOf(newsFeedEntity.getUser_id()));
                            payment_params.put("is_business","1");
                            payment_params.put("quantity","1");
                            payment_params.put("service_id",String.valueOf(newsFeedEntity.getService_id()));
                            payment_params.put("created_by","0");
                            payment_params.put("business_user_id",String.valueOf(newsFeedEntity.getUser_id()));
                            payment_params.put("booking_datetime", String.valueOf(bookingEntity.getBooking_datetime()));
                            payment_params.put("total_cost",String.valueOf(newsFeedEntity.getPrice()));
                            payment_params.put("is_reminder_enabled","0");

                            paymentProcessing(payment_params,0);
                        }
                    },newsFeedEntity);
                    depositDialog.show(getSupportFragmentManager(), "DeleteMessage");
                }else {
                    createBooking("");
                }
                break;
        }
    }

    @Override
    public void processPayment(String price, String clicent_id, String clicnet_token, NewsFeedEntity newsFeedEntity, int deliveryOption, ArrayList<String> selected_Variation) {
        payment_params.clear();
        payment_params.put("token",Commons.token);
        payment_params.put("customerId",Commons.g_user.getBt_customer_id());
        payment_params.put("amount",price);
        payment_params.put("toUserId", String.valueOf(newsFeedEntity.getUser_id()));
        payment_params.put("is_business","1");
        payment_params.put("quantity","1");
        payment_params.put("service_id",String.valueOf(newsFeedEntity.getService_id()));
//        DropInRequest dropInRequest = new DropInRequest()
//                .clientToken(clicnet_token)
//                .cardholderNameStatus(CardForm.FIELD_OPTIONAL)
//                .collectDeviceData(true)
//                .vaultManager(true);
//        startActivityForResult(dropInRequest.getIntent(this), Commons.REQUEST_PAYMENT_CODE);

    }




    @Override
    public void onDaySelected() {
        if(calendarView.getSelectedDates().get(0).getTimeInMillis()<today){
            showAlertDialog("Please choice futher day");
            hashMap.clear();
            selected_bookingSlot.clear();
            bookingListAdapter.setRoomData(hashMap,selected_bookingSlot);
            Helper.getListViewSize(list_booking);
            return;
        }
        loadBookingByday(calendarView.getSelectedDays().get(0).getDayNumber()-1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onResume();
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Commons.REQUEST_PAYMENT_CODE) {
//            if (resultCode == RESULT_OK) {
//                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
//                payment_params.put("paymentNonce", Objects.requireNonNull(result.getPaymentMethodNonce()).getNonce());
//                if(result.getPaymentMethodType().name().equals("PAYPAL")){
//                    payment_params.put("paymentMethod","Paypal");
//                }else {
//                    payment_params.put("paymentMethod","Card");
//                }
//                Log.d("aaaaa",payment_params.toString());
//
//                paymentProcessing(payment_params,0);
//
//                String deviceData = result.getDeviceData();
//                if (result.getPaymentMethodType() == PaymentMethodType.PAY_WITH_VENMO) {
//                    VenmoAccountNonce venmoAccountNonce = (VenmoAccountNonce) result.getPaymentMethodNonce();
//                    String venmoUsername = venmoAccountNonce.getUsername();
//                }
//                // use the result to update your UI and send the payment method nonce to your server
//            } else if (resultCode == RESULT_CANCELED) {
//                // the user canceled
//            } else {
//                // handle errors here, an exception may be available in
//                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
//                Log.d("error:", error.toString());
//            }
        }

    }

    @Override
    public void finishPayment(String transaction_id) {
        ServiceBookingSuccessDialog serviceBookingSuccessDialog = new ServiceBookingSuccessDialog();
        serviceBookingSuccessDialog.setOnConfirmListener(new ServiceBookingSuccessDialog.OnConfirmListener() {
            @Override
            public void onPurchase() {
                setResult(-200);
                goTo(BookFromPostActivity.this,MyBookingActivity.class,true);
            }
            @Override
            public void returnMyATB() {
                setResult(-200);
                finish(BookFromPostActivity.this);
            }
        },newsFeedEntity);
        serviceBookingSuccessDialog.show(getSupportFragmentManager(), "DeleteMessage");

    }

    void createBooking(String transaction_id){
        showProgress();
        StringRequest myRequest = new StringRequest(
                Request.Method.POST,
                API.CREATE_BOOKING,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String json) {
                        try {
                            closeProgress();
                            JSONObject jsonObject = new JSONObject(json);
                            if(jsonObject.getBoolean("result")) {
                                String booking_id = jsonObject.getJSONObject("extra").getString("id");
//                                updateTransaction(transaction_id,booking_id);
                                ServiceBookingSuccessDialog serviceBookingSuccessDialog = new ServiceBookingSuccessDialog();
                                serviceBookingSuccessDialog.setOnConfirmListener(new ServiceBookingSuccessDialog.OnConfirmListener() {
                                    @Override
                                    public void onPurchase() {
                                        setResult(-200);
                                        goTo(BookFromPostActivity.this,MyBookingActivity.class,true);
                                    }
                                    @Override
                                    public void returnMyATB() {
                                        setResult(-200);
                                        finish(BookFromPostActivity.this);
                                    }
                                },newsFeedEntity);
                                serviceBookingSuccessDialog.show(getSupportFragmentManager(), "DeleteMessage");
                            }
                        }catch (Exception e){

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        closeProgress();
                        //showToast(error.getMessage());

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("token", Commons.token);
                params.put("business_user_id", String.valueOf(userModel.getId()));
                params.put("service_id", String.valueOf(newsFeedEntity.getService_id()));
                params.put("booking_datetime", String.valueOf(bookingEntity.getBooking_datetime()));
                params.put("is_reminder_enabled", "0");
                params.put("total_cost", newsFeedEntity.getPrice());
                params.put("user_id", String.valueOf(Commons.g_user.getId()));
                return params;
            }
        };
        myRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(myRequest, "tag");
    }

    void checkout( Map<String, String> params){
        showProgress();
        StringRequest myRequest = new StringRequest(
                Request.Method.POST,
                API.MAKE_STRIPE_PAYMENT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String json) {
                        closeProgress();

                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            Log.d("aaaaa",jsonObject.toString());
                            if(jsonObject.getBoolean("result")){
                                ServiceBookingSuccessDialog serviceBookingSuccessDialog = new ServiceBookingSuccessDialog();
                                serviceBookingSuccessDialog.setOnConfirmListener(new ServiceBookingSuccessDialog.OnConfirmListener() {
                                    @Override
                                    public void onPurchase() {
                                        setResult(-200);
                                        goTo(BookFromPostActivity.this,MyBookingActivity.class,true);
                                    }
                                    @Override
                                    public void returnMyATB() {
                                        setResult(-200);
                                        finish(BookFromPostActivity.this);
                                    }
                                },newsFeedEntity);
                                serviceBookingSuccessDialog.show(getSupportFragmentManager(), "DeleteMessage");
                            }
                        }catch (Exception e){
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        closeProgress();
                        //showToast(error.getMessage());

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                return params;
            }
        };
        myRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(myRequest, "tag");
    }

    void updateTransaction(String transaction_id, String booking_id){
        showProgress();
        StringRequest myRequest = new StringRequest(
                Request.Method.POST,
                API.UPDATE_TRANSCATION,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String json) {
                        closeProgress();
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            if(jsonObject.getBoolean("result")){
                                ServiceBookingSuccessDialog serviceBookingSuccessDialog = new ServiceBookingSuccessDialog();
                                serviceBookingSuccessDialog.setOnConfirmListener(new ServiceBookingSuccessDialog.OnConfirmListener() {
                                    @Override
                                    public void onPurchase() {
                                        setResult(-200);
                                        goTo(BookFromPostActivity.this,MyBookingActivity.class,true);
                                    }
                                    @Override
                                    public void returnMyATB() {
                                        setResult(-200);
                                        finish(BookFromPostActivity.this);
                                    }
                                },newsFeedEntity);
                                serviceBookingSuccessDialog.show(getSupportFragmentManager(), "DeleteMessage");
                            }
                        }catch (Exception e){

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        closeProgress();
                        //showToast(error.getMessage());

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("token", Commons.token);
                params.put("booking_id", booking_id);
                params.put("transaction_id", transaction_id);
                return params;
            }
        };
        myRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(myRequest, "tag");
    }
    @Override
    public void disableSlot(int milionSecond,boolean flag) {
        int[] remove_id = {-1};
        showProgress();
        String api_link = API.CREATE_DISABLESLOT;
        if(flag)
            api_link = API.DELETE_DISABLESLOT;
        StringRequest myRequest = new StringRequest(
                Request.Method.POST,
                api_link,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String json) {
                        closeProgress();
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            if(jsonObject.getBoolean("result")) {
                                if (flag) {
                                    userModel.getBusinessModel().getDisableSlotModels().remove(remove_id[0]);
                                } else {
                                    JSONObject disable_slot = jsonObject.getJSONObject("extra");
                                    DisableSlotModel disableSlotModel = new DisableSlotModel();
                                    disableSlotModel.setId(disable_slot.getInt("id"));
                                    disableSlotModel.setUser_id(disable_slot.getInt("user_id"));
                                    disableSlotModel.setDay_timestamp(disable_slot.getLong("day_timestamp"));
                                    disableSlotModel.setStart(disable_slot.getString("start"));
                                    disableSlotModel.setEnd(disable_slot.getString("end"));
                                    disableSlotModel.setCreated_at(disable_slot.getLong("created_at"));
                                    disableSlotModel.setUpdated_at(disable_slot.getLong("updated_at"));
                                    userModel.getBusinessModel().getDisableSlotModels().add(disableSlotModel);
                                }
                            }
                            loadBookingByday(day);

                        }catch (Exception e){

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        closeProgress();
                        //showToast(error.getMessage());

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("token", Commons.token);
                if(!flag) {
                    int day_timestamp = milionSecond - Commons.getMilionSecond(Commons.gettimeFromMilionSecond(milionSecond));
                    params.put("day_timestamp", String.valueOf(day_timestamp));
                    params.put("start", Commons.getUTCtimeFromMilionSecond(milionSecond));
                    params.put("end", Commons.getUTCtimeFromMilionSecond(milionSecond + 3600));
                }else{
                    for(int i = 0; i< userModel.getBusinessModel().getDisableSlotModels().size(); i++){
                        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                        df.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date d = null;
                        int time =0;
                        try {
                            d = df.parse(userModel.getBusinessModel().getDisableSlotModels().get(i).getStart());
                            time = (int)d.getTime()/1000;
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Long disableMilionSecond = userModel.getBusinessModel().getDisableSlotModels().get(i).getDay_timestamp() + time;
                        if(milionSecond == disableMilionSecond) {
                            remove_id[0] = i;
                            params.put("slot_id", String.valueOf(userModel.getBusinessModel().getDisableSlotModels().get(i).getId()));
                            break;
                        }
                    }

                }
                return params;
            }
        };
        myRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(myRequest, "tag");

    }

    void getAllBookings(){
        StringRequest myRequest = new StringRequest(
                Request.Method.POST,
                API.GET_BOOKING,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String json) {
                        closeProgress();
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            JSONArray arrayList = jsonObject.getJSONArray("extra");
                            AllbookingEntities.clear();
                            for(int i =0;i<arrayList.length();i++){
                                BookingEntity bookingEntity = new BookingEntity();
                                bookingEntity.initModel(arrayList.getJSONObject(i));
                                AllbookingEntities.add(bookingEntity);

                            }
                            setConnectDay();

                        }catch (Exception e){

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        closeProgress();
                        //showToast(error.getMessage());

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("token", Commons.token);
                params.put("user_id", String.valueOf(userModel.getId()));
                params.put("is_business", "1");
//                params.put("month", String.valueOf(year) +" " + String.valueOf(month+1));
                return params;
            }
        };
        myRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(myRequest, "tag");
    }


    @Override
    public void selectBooking(int posstion) {
        txv_booking.setVisibility(View.VISIBLE);
        bookingEntity = hashMap.get(selected_bookingSlot.get(posstion));
        String daystr = getDaystr(day+1);
        txv_booking.setText("Book for " + daystr + " at " + Commons.monthNames[month] + " "+  selected_bookingSlot.get(posstion));
    }
}
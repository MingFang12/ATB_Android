package com.atb.app.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atb.app.R;
import com.atb.app.activities.navigationItems.BookingActivity;
import com.atb.app.activities.navigationItems.NotificationActivity;
import com.atb.app.base.CommonActivity;
import com.atb.app.commons.Commons;
import com.atb.app.model.BookingEntity;
import com.atb.app.model.NotiEntity;
import com.atb.app.util.RoundedCornersTransformation;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BookingListAdapter extends BaseAdapter {

    private Context _context;

    HashMap<String,BookingEntity> _roomDatas = new HashMap<>();
    ArrayList<String > bookingSlot = new ArrayList<>();
    int type =0;
    int select_posstion = -1;
    public BookingListAdapter(Context context, int type) {
        super();
        this._context = context;
        this.type = type;
    }
    public void setRoomData(HashMap<String,BookingEntity> data,ArrayList<String > bookingSlot) {
        select_posstion = -1;
        _roomDatas.clear();
        _roomDatas.putAll(data);
        this.bookingSlot.clear();
        this.bookingSlot.addAll(bookingSlot);
        notifyDataSetChanged();
    }
    public void init() {
        this._roomDatas.clear();
        this.bookingSlot.clear();
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return bookingSlot.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final CustomHolder holder;
        if (convertView == null) {
            holder = new CustomHolder();
            LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.futurebooking_item, parent, false);
            holder.lyt_addbooking = (LinearLayout) convertView.findViewById(R.id.lyt_addbooking);
            holder.lyt_booking = (LinearLayout) convertView.findViewById(R.id.lyt_booking);
            holder.txv_name = (TextView) convertView.findViewById(R.id.txv_name);
            holder.txv_id = (TextView) convertView.findViewById(R.id.txv_id);
            holder.imv_profile = (ImageView) convertView.findViewById(R.id.imv_profile);
            holder.txv_timeslot = (TextView) convertView.findViewById(R.id.txv_timeslot);
            holder.lyt_container = (LinearLayout)convertView.findViewById(R.id.lyt_container);
            holder.imv_disalbe = convertView.findViewById(R.id.imv_disalbe);
            holder.txv_disable = convertView.findViewById(R.id.txv_disable);
            holder.txv_add = convertView.findViewById(R.id.txv_add);
            holder.lyt_select = convertView.findViewById(R.id.lyt_select);
            holder.imv_selector = convertView.findViewById(R.id.imv_selector);
            holder.view_line = convertView.findViewById(R.id.view_line);
            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final BookingEntity bookingEntity =  _roomDatas.get(bookingSlot.get(position));
        if(type ==0 ) {
            //manualy booking
            holder.txv_timeslot.setText(bookingSlot.get(position));
            holder.imv_selector.setEnabled(true);
            if (bookingEntity.getType() == 0) {
                holder.lyt_addbooking.setVisibility(View.VISIBLE);
                holder.lyt_booking.setVisibility(View.GONE);
                holder.txv_add.setText("Add A Booking");
                holder.txv_disable.setVisibility(View.VISIBLE);
                holder.imv_disalbe.setImageDrawable(_context.getResources().getDrawable(R.drawable.leftdetail));
                holder.txv_add.setTextColor(_context.getResources().getColor(R.color.txt_color));
                holder.imv_disalbe.setColorFilter(_context.getResources().getColor(R.color.head_color), PorterDuff.Mode.SRC_IN);

            } else if (bookingEntity.getType() == 1) {
                holder.lyt_addbooking.setVisibility(View.GONE);
                holder.lyt_booking.setVisibility(View.VISIBLE);
            } else {
                holder.txv_add.setText("Disabled a slot");
                holder.txv_disable.setVisibility(View.GONE);
                holder.lyt_addbooking.setVisibility(View.VISIBLE);
                holder.lyt_booking.setVisibility(View.GONE);
                holder.txv_add.setTextColor(_context.getResources().getColor(R.color.txvcolor_trasnparent));
                holder.imv_disalbe.setImageDrawable(_context.getResources().getDrawable(R.drawable.icon_minus1));
                holder.imv_disalbe.setColorFilter(_context.getResources().getColor(R.color.txvcolor_trasnparent), PorterDuff.Mode.SRC_IN);
                holder.imv_selector.setEnabled(false);
            }
            holder.lyt_select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.imv_selector.setEnabled(!holder.imv_selector.isEnabled());
                    ((CommonActivity) (_context)).disableSlot(bookingEntity.getBooking_datetime(), holder.imv_selector.isEnabled());
                }
            });
            String imv_url = "";
            if (bookingEntity.getUser_id() >= 0) {
                imv_url = bookingEntity.getUserModel().getImvUrl();
            }
            Glide.with(_context).load(imv_url).placeholder(R.drawable.profile_pic).dontAnimate().apply(RequestOptions.bitmapTransform(
                    new RoundedCornersTransformation(_context, Commons.glide_radius, Commons.glide_magin, "#A6BFDE", Commons.glide_boder))).into(holder.imv_profile);
            holder.txv_id.setText(bookingEntity.getUserModel().getUserName());
            holder.txv_name.setText(bookingEntity.getNewsFeedEntity().getTitle());
        }else {
            holder.txv_timeslot.setText(bookingSlot.get(position));
            holder.lyt_select.setVisibility(View.GONE);
            holder.view_line.setVisibility(View.GONE);
            if (bookingEntity.getType() == 0) {
                holder.lyt_addbooking.setVisibility(View.VISIBLE);
                holder.lyt_booking.setVisibility(View.GONE);
                holder.txv_add.setText("Add A Booking");
                holder.txv_disable.setText(bookingEntity.getBookingDuration());
                holder.txv_disable.setVisibility(View.VISIBLE);
                holder.txv_disable.setTextColor(_context.getResources().getColor(R.color.signup_textcolor));
                holder.imv_disalbe.setImageDrawable(_context.getResources().getDrawable(R.drawable.leftdetail));
                holder.txv_add.setTextColor(_context.getResources().getColor(R.color.txt_color));
                holder.imv_disalbe.setColorFilter(_context.getResources().getColor(R.color.head_color), PorterDuff.Mode.SRC_IN);
                holder.lyt_addbooking.setBackground(_context.getResources().getDrawable(R.drawable.edit_rectangle_round));
                if(position==select_posstion){
                    holder.lyt_addbooking.setBackground(_context.getResources().getDrawable(R.drawable.button_rectangle_round));
                    holder.imv_disalbe.setImageDrawable(_context.getResources().getDrawable(R.drawable.icon_checked));
                    holder.imv_disalbe.setColorFilter(_context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
                    holder.txv_add.setTextColor(_context.getResources().getColor(R.color.white));
                    holder.txv_disable.setTextColor(_context.getResources().getColor(R.color.white));
                    holder.txv_add.setText("Selected");
                }
            } else{
                holder.txv_add.setText("Not Available");
                holder.txv_disable.setVisibility(View.GONE);
                holder.lyt_addbooking.setVisibility(View.VISIBLE);
                holder.lyt_booking.setVisibility(View.GONE);
                holder.txv_add.setTextColor(_context.getResources().getColor(R.color.white));
                holder.imv_disalbe.setImageDrawable(_context.getResources().getDrawable(R.drawable.icon_minus1));
                holder.imv_disalbe.setColorFilter(_context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
                holder.imv_selector.setEnabled(false);
                holder.lyt_addbooking.setBackground(_context.getResources().getDrawable(R.drawable.disable_rectangle_round));
            }

            holder.lyt_addbooking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(bookingEntity.getType()==0) {
                        ((CommonActivity) _context).selectBooking(position);
                        select_posstion = position;
                        notifyDataSetChanged();
                    }
                }
            });
        }
        return convertView;
    }


    public class CustomHolder {
        LinearLayout lyt_addbooking,lyt_booking,lyt_container,lyt_select;
        ImageView imv_profile,imv_disalbe,imv_selector;
        TextView txv_name,txv_id,txv_timeslot,txv_disable,txv_add;
        View view_line;
    }


    protected boolean isItemSelected(int position) {
        //return !selectedItems.isEmpty() && selectedItems.contains(getItem(position));
        return true;
    }
}



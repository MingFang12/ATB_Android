package com.atb.app.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atb.app.R;
import com.atb.app.api.API;
import com.atb.app.application.AppController;
import com.atb.app.base.CommonActivity;
import com.atb.app.commons.Commons;
import com.atb.app.commons.Constants;
import com.atb.app.commons.Helper;
import com.atb.app.model.NewsFeedEntity;
import com.atb.app.model.submodel.VotingModel;
import com.atb.app.util.RoundedCornersTransformation;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.internal.service.Common;
import com.volokh.danylo.video_player_manager.ui.SimpleMainThreadMediaPlayerListener;
import com.volokh.danylo.video_player_manager.ui.VideoPlayerView;


import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

import static com.atb.app.base.BaseActivity.closeProgress;
import static com.atb.app.base.BaseActivity.showProgress;
import static com.atb.app.base.BaseActivity.showToast;

public class NewsFeedItemAdapter extends RecyclerView.Adapter<NewsFeedItemAdapter.ViewHolder> {
    Context context;
    ArrayList<NewsFeedEntity>newsFeedEntities = new ArrayList<>();
    private   OnSelectListener listener;

    public NewsFeedItemAdapter(Context context,  ArrayList<NewsFeedEntity>newsFeedEntities, OnSelectListener listener) {
        this.newsFeedEntities = newsFeedEntities;
        this.listener = listener;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.newsfeed_item1, parent, false);

        return new ViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        NewsFeedEntity newsFeedEntity = newsFeedEntities.get(position);
        if(newsFeedEntity.getPoster_profile_type()==0) {
            Glide.with(context).load(newsFeedEntity.getProfile_image()).placeholder(R.drawable.profile_pic).dontAnimate()
                    .apply(RequestOptions.bitmapTransform(
                            new RoundedCornersTransformation(context, Commons.glide_radius, Commons.glide_magin, "#A8C3E7", Commons.glide_boder)))
                    .into(holder.imv_profile);
        }else {
            Glide.with(context).load(newsFeedEntity.getProfile_image()).placeholder(R.drawable.profile_pic).dontAnimate()
                    .apply(RequestOptions.bitmapTransform(
                            new RoundedCornersTransformation(context, Commons.glide_radius, Commons.glide_magin, "#FFFFFF", Commons.glide_boder)))
                    .into(holder.imv_profile);
        }
        holder.txv_name.setText(newsFeedEntity.getProfile_name());
        holder.txv_time.setText(newsFeedEntity.getRead_created());
        holder.txv_comment.setText(String.valueOf(newsFeedEntity.getComments()));
        holder.txv_heart.setText(String.valueOf(newsFeedEntity.getLikes()));
        holder.txv_description.setText(newsFeedEntity.getTitle());
        holder.txv_price.setText("£"+newsFeedEntity.getPrice() );
        if(newsFeedEntity.getPost_type() == 3)
            holder.txv_price.setText("Starting at £"+newsFeedEntity.getPrice() );
        holder.txv_deposit.setText(newsFeedEntity.getDescription());
        holder.imv_type.setImageResource(Constants.postType[newsFeedEntity.getPost_type()]);
        holder.imv_txv_type.setImageResource(Constants.postType[newsFeedEntity.getPost_type()]);
        holder.txv_description.setTextColor(context.getResources().getColor(R.color.txt_color));
        holder.imv_heart.clearColorFilter();
        holder.imv_heart.setColorFilter(context.getResources().getColor(R.color.txt_color), PorterDuff.Mode.SRC_IN);
        holder.imv_comment.clearColorFilter();
        holder.imv_comment.setColorFilter(context.getResources().getColor(R.color.txt_color), PorterDuff.Mode.SRC_IN);
        holder.txv_price.setTextColor(context.getResources().getColor(R.color.head_color));
        holder.txv_deposit.setTextColor(context.getResources().getColor(R.color.txt_color));
        holder.imv_txv_type.setVisibility(View.GONE);
        if(Commons.selectUsertype==-1)
            holder.lyt_profile.setVisibility(View.VISIBLE);
        else
            holder.lyt_profile.setVisibility(View.GONE);

        if(newsFeedEntity.getPoster_profile_type()==1){
            holder.imv_atb_approved.setVisibility(View.VISIBLE);
            holder.txv_name.setTextColor(context.getResources().getColor(R.color.white));
            holder.txv_time.setTextColor(context.getResources().getColor(R.color.white));
            holder.lyt_profile.setBackgroundColor(context.getResources().getColor(R.color.head_color));
            holder.lyt_content.setBackgroundColor(context.getResources().getColor(R.color.head_color));
            holder.txv_description.setTextColor(context.getResources().getColor(R.color.white));
            holder.txv_price.setTextColor(context.getResources().getColor(R.color.white));
            holder.txv_deposit.setTextColor(context.getResources().getColor(R.color.white));
            holder.txv_comment.setTextColor(context.getResources().getColor(R.color.white));
            holder.txv_heart.setTextColor(context.getResources().getColor(R.color.white));
            holder.imv_heart.clearColorFilter();
            holder.imv_heart.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            holder.imv_comment.clearColorFilter();
            holder.imv_comment.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            if(newsFeedEntity.getPostImageModels().size()==0){
                holder.imv_txv_type.setVisibility(View.VISIBLE);
                holder.imv_txv_type.clearColorFilter();
                holder.imv_txv_type.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            }
        }else {
            holder.imv_atb_approved.setVisibility(View.GONE);
            holder.txv_name.setTextColor(context.getResources().getColor(R.color.txt_color));
            holder.txv_time.setTextColor(context.getResources().getColor(R.color.txt_color));
            holder.lyt_profile.setBackgroundColor(context.getResources().getColor(R.color.white));
            holder.lyt_content.setBackgroundColor(context.getResources().getColor(R.color.white));
            if(newsFeedEntity.getPostImageModels().size()==0) {
                holder.imv_txv_type.setVisibility(View.VISIBLE);
                holder.imv_txv_type.clearColorFilter();
                holder.imv_txv_type.setColorFilter(context.getResources().getColor(R.color.head_color), PorterDuff.Mode.SRC_IN);
            }
        }

        if(newsFeedEntity.getPost_type()==2 || newsFeedEntity.getPost_type() ==3)
            holder.txv_price.setVisibility(View.VISIBLE);
        else
            holder.txv_price.setVisibility(View.GONE);

        if(newsFeedEntity.getIs_sold()== 1 ){
            holder.txv_price.setText("SOLD");
            holder.txv_price.setTextColor(context.getResources().getColor(R.color.discard_color));
        }
        if(newsFeedEntity.getPostImageModels().size()>0){

            holder.lyt_image.setVisibility(View.VISIBLE);
            Glide.with(context).load(newsFeedEntity.getPostImageModels().get(0).getPath()).placeholder(R.drawable.image_thumnail).dontAnimate().into(holder.imv_imageview);
            if(Commons.mediaVideoType(newsFeedEntity.getPostImageModels().get(0).getPath())) {
                holder.video_view.setVisibility(View.VISIBLE);
                holder.video_view.addMediaPlayerListener(new SimpleMainThreadMediaPlayerListener(){
                    @Override
                    public void onVideoPreparedMainThread() {
                        // We hide the cover when video is prepared. Playback is about to start
                        holder.imv_imageview.setVisibility(View.INVISIBLE);
                        holder.imv_videoplay.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onVideoStoppedMainThread() {

                        // We show the cover when video is stopped
                        holder.imv_imageview.setVisibility(View.VISIBLE);
                        holder.imv_videoplay.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onVideoCompletionMainThread() {
                        // We show the cover when video is completed
                        holder.imv_imageview.setVisibility(View.VISIBLE);
                        holder.imv_videoplay.setVisibility(View.VISIBLE);
                    }
                });

                if(Commons.video_flag == position) {
                    //Commons.mVideoPlayerManager.stopAnyPlayback();

                //    Commons.mVideoPlayerManager.playNewVideo(null, holder.video_view, newsFeedEntity.getPostImageModels().get(0).getPath());


                }

            }
            else {
                holder.imv_videoplay.setVisibility(View.GONE);
                holder.video_view.setVisibility(View.GONE);
            }

        }else {
            holder.lyt_image.setVisibility(View.GONE);
        }

        if(newsFeedEntity.getPost_type() ==4){
         holder.txv_deposit.setVisibility(View.GONE);
         VotingListAdapter votingListAdapter = new VotingListAdapter(context);
         holder.lyt_votelist.setAdapter(votingListAdapter);
         votingListAdapter.setRoomData(newsFeedEntity.getPoll_options());
         Helper.getListViewSize(holder.lyt_votelist);
         holder.lyt_votelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 VotingModel votingModel = newsFeedEntity.getPoll_options().get(position);
                 for(int i =0;i<newsFeedEntity.getPoll_options().size();i++) {
                     if (Commons.myVoting(newsFeedEntity.getPoll_options().get(i))) {
                         ((CommonActivity) context).showAlertDialog("You've aleady voted on this poll!");
                         return;
                     }
                 }

                 /// change poll event
                 addVoting(votingModel.getPost_id(),votingModel.getPoll_value(),position,newsFeedEntity);

             }
         });
        }else {
            holder.txv_deposit.setVisibility(View.VISIBLE);
        }

        holder.lyt_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onSelect( newsFeedEntities.get(position));
            }
        });

        holder.lyt_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSelectProfile( newsFeedEntities.get(position));
            }
        });



    }





    void  addVoting(int post_id, String poll_value,int posstion, NewsFeedEntity newsFeedEntity){
        showProgress();
        StringRequest myRequest = new StringRequest(
                Request.Method.POST,
                API.ADD_VOTE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String json) {
                        closeProgress();
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            if(jsonObject.getBoolean("result")){
                                newsFeedEntity.getPoll_options().get(posstion).getVotes().add(Commons.g_user.getId());
                                notifyDataSetChanged();
                            }
                        }catch (Exception e){
                            Log.d("Exception ",e.toString());
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
                params.put("post_id", String.valueOf(post_id));
                params.put("poll_value",poll_value);
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
    public int getItemCount() {
        return newsFeedEntities.size();
    }





    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txv_name,txv_time,txv_description,txv_price,txv_deposit,txv_heart,txv_comment;
        ImageView imv_profile,imv_imageview,imv_group,imv_type,imv_txv_type,imv_videoplay,imv_comment,imv_heart;
        LinearLayout lyt_container,lyt_approve,lyt_profile,lyt_content;
        FrameLayout lyt_image;
        CardView card_imv_group,card_imv_type,imv_atb_approved;
        ListView lyt_votelist;
        VideoPlayerView video_view;
        public ViewHolder(View itemView) {
            super(itemView);
            lyt_container=itemView.findViewById(R.id.lyt_container);
            txv_name = itemView.findViewById(R.id.txv_name);
            txv_time = itemView.findViewById(R.id.txv_time);
            txv_description = itemView.findViewById(R.id.txv_description);
            txv_price = itemView.findViewById(R.id.txv_price);
            imv_comment = itemView.findViewById(R.id.imv_comment);
            imv_heart = itemView.findViewById(R.id.imv_heart);
            txv_deposit = itemView.findViewById(R.id.txv_deposit);
            txv_heart = itemView.findViewById(R.id.txv_heart);
            txv_comment = itemView.findViewById(R.id.txv_comment);
            imv_profile = itemView.findViewById(R.id.imv_profile);
            imv_imageview = itemView.findViewById(R.id.imv_imageview);
            imv_atb_approved = itemView.findViewById(R.id.imv_atb_approved);
            imv_group = itemView.findViewById(R.id.imv_group);
            imv_type = itemView.findViewById(R.id.imv_type);
            lyt_image = itemView.findViewById(R.id.lyt_image);
            card_imv_group = itemView.findViewById(R.id.card_imv_group);
            card_imv_type = itemView.findViewById(R.id.card_imv_type);
            lyt_approve = itemView.findViewById(R.id.lyt_approve);
            imv_txv_type = itemView.findViewById(R.id.imv_txv_type);
            lyt_votelist = itemView.findViewById(R.id.lyt_votelist);
            imv_videoplay = itemView.findViewById(R.id.imv_videoplay);
            lyt_profile = itemView.findViewById(R.id.lyt_profile);
            video_view = itemView.findViewById(R.id.video_view);
            lyt_content = itemView.findViewById(R.id.lyt_content);
            if(getItemCount()>1){
                ViewGroup.LayoutParams layoutParams = lyt_container.getLayoutParams();
                DisplayMetrics metrics = new DisplayMetrics();
                ((CommonActivity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
                layoutParams.width = metrics.widthPixels - 200;
                lyt_container.setLayoutParams(layoutParams);

                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) lyt_container.getLayoutParams();
                p.setMargins(0, 0, 30, 0);
                lyt_container.requestLayout();

                card_imv_group.setVisibility(View.VISIBLE);
                txv_deposit.setMaxLines(1);

            }else {
                ViewGroup.LayoutParams layoutParams = lyt_container.getLayoutParams();
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lyt_container.setLayoutParams(layoutParams);
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) lyt_container.getLayoutParams();
                p.setMargins(0, 0, 0, 0);
                lyt_container.requestLayout();
                card_imv_group.setVisibility(View.GONE);
                txv_deposit.setMaxLines(2);
            }

        }
    }

    public interface OnSelectListener{
        void onSelect(NewsFeedEntity newsFeedEntity);
        void onSelectProfile(NewsFeedEntity newsFeedEntity);
    }

}

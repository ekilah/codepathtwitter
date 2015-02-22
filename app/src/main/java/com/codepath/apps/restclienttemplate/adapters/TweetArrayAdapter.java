package com.codepath.apps.restclienttemplate.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.Activities.TimelineActivity;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.RestClient;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.sql.Time;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by mekilah on 2/19/15.
 */
public class TweetArrayAdapter extends ArrayAdapter<Tweet>{
    private TimelineActivity timelineActivity;

    public TweetArrayAdapter(TimelineActivity callingActivity, List<Tweet> objects){
        super((Context)callingActivity, 0, objects);
        this.timelineActivity = callingActivity;
    }

    class TweetViewHolder{
        ImageView ivPosterAvatar;
        TextView tvUsername;
        TextView tvProfilename;
        TextView tvBody;
        TextView tvTimestamp;
        TextView tvReply;
        TextView tvRetweet;
        TextView tvFavorite;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){

        final TweetViewHolder holder;
        if(convertView == null){
            //inflate new view
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.timeline_item, parent, false);
            holder = new TweetViewHolder();
            holder.ivPosterAvatar = (ImageView) convertView.findViewById(R.id.ivTimelineItemPosterAvatar);
            holder.tvUsername = (TextView) convertView.findViewById(R.id.tvTimelineItemPosterUsername);
            holder.tvProfilename = (TextView) convertView.findViewById(R.id.tvTimelineItemPosterProfileName);
            holder.tvBody = (TextView) convertView.findViewById(R.id.tvTimelineItemTweetBody);
            holder.tvTimestamp = (TextView) convertView.findViewById(R.id.tvTimelineItemTimestamp);
            holder.tvReply = (TextView) convertView.findViewById(R.id.tvReply);
            holder.tvRetweet = (TextView) convertView.findViewById(R.id.tvRetweet);
            holder.tvFavorite = (TextView) convertView.findViewById(R.id.tvFavorite);
            convertView.setTag(holder);
        }else{
            holder = (TweetViewHolder)convertView.getTag();
        }

        Picasso.with(getContext()).load(getItem(position).getPoster().getAvatarURL()).into(holder.ivPosterAvatar);
        holder.tvUsername.setText("@" + getItem(position).getPoster().getUsername());
        holder.tvProfilename.setText(getItem(position).getPoster().getProfilename());
        holder.tvBody.setText(getItem(position).getBody());
        holder.tvTimestamp.setText(this.getRelativeTimeAgo(getItem(position).getTimestampString()));

        holder.tvRetweet.setText(NumberFormat.getNumberInstance(convertView.getResources().getConfiguration().locale).format(getItem(position).getRetweetCount()));
        holder.tvFavorite.setText(NumberFormat.getNumberInstance(convertView.getResources().getConfiguration().locale).format(getItem(position).getFavoriteCount()));


        double drawableScale = 0.7;

        Drawable retweet = this.getContext().getResources().getDrawable(android.R.drawable.ic_popup_sync);
        scaleDrawable(retweet, drawableScale);
        if(getItem(position).posterRetweeted()){
            //retweeting is not undoable
            retweet.setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
            holder.tvRetweet.setClickable(false);
        }else{
            retweet.setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
            holder.tvRetweet.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(final View v){

                    RestApplication.getRestClient().retweetTweetWithId(getItem(position).getTweetId(), new JsonHttpResponseHandler(){
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                            getItem(position).setPosterRetweeted(!getItem(position).posterRetweeted());
                            if(getItem(position).posterRetweeted()){
                                ((Drawable)v.getTag()).setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                                holder.tvRetweet.setClickable(false);//prevent attempts to retweet again
                            }else{
                                Log.e("TWITTER", "shouln't be able to un-retweet");
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse){
                            Toast.makeText(getContext(),getContext().getString(R.string.unable_to_retweet), Toast.LENGTH_SHORT).show();
                            if(errorResponse != null){
                                Log.e("TWITTER", "Failed to retweet. Error: " + errorResponse.toString(), throwable);
                            }
                        }
                    });


                }
            });
        }
        holder.tvRetweet.setCompoundDrawables(retweet, null, null, null);
        holder.tvRetweet.setTag(retweet);


        Drawable favoriteOff = this.getContext().getResources().getDrawable(android.R.drawable.btn_star_big_off);
        scaleDrawable(favoriteOff, drawableScale);
        Drawable favoriteOn = this.getContext().getResources().getDrawable(android.R.drawable.btn_star_big_on);
        scaleDrawable(favoriteOn, drawableScale);
        holder.tvFavorite.setTag(R.id.favoriteOff, favoriteOff);
        holder.tvFavorite.setTag(R.id.favoriteOn, favoriteOn);

        holder.tvFavorite.setCompoundDrawables((getItem(position).posterFavorited() ? (Drawable)holder.tvFavorite.getTag(R.id.favoriteOn) : (Drawable)holder.tvFavorite.getTag(R.id.favoriteOff)), null, null, null);
        holder.tvFavorite.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View v){

                final boolean isBeingFavorited = !getItem(position).posterFavorited();

                RestClient client = RestApplication.getRestClient();
                client.favoriteTweetWithId(getItem(position).getTweetId(), isBeingFavorited, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                        getItem(position).setPosterFavorited(isBeingFavorited);
                        ((TextView)v).setCompoundDrawables((isBeingFavorited ? (Drawable) v.getTag(R.id.favoriteOn) : (Drawable) v.getTag(R.id.favoriteOff)), null, null, null);
                        getItem(position).setFavoriteCount(getItem(position).getFavoriteCount() + (isBeingFavorited ? 1 : -1));
                        holder.tvFavorite.setText(String.valueOf(getItem(position).getFavoriteCount()));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse){
                        Toast.makeText(getContext(), getContext().getString(R.string.unable_to_favorite), Toast.LENGTH_SHORT);
                        if(errorResponse != null){
                            Log.e("TWITTER", "Failed to favorite tweet. Response: " + errorResponse.toString(), throwable);
                        }
                    }
                });

            }
        });

        Drawable reply = this.getContext().getResources().getDrawable(android.R.drawable.ic_menu_revert);
        scaleDrawable(reply, drawableScale);
        reply.setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
        holder.tvReply.setCompoundDrawables(reply, null, null, null);
        holder.tvReply.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                timelineActivity.ShowComposeTweetDialog(getItem(position));
            }
        });

        return convertView;
    }

    private static void scaleDrawable(Drawable drawable, double scale){
        drawable.setBounds(0,0,(int)(drawable.getIntrinsicWidth()*scale), (int) (drawable.getIntrinsicHeight()*scale));
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    private String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);
        long currentTime = System.currentTimeMillis();
        long tweetTime = -1;
        String relativeDate = "";
        try {
            tweetTime = sf.parse(rawJsonDate).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return getContext().getString(R.string.unknown);
        }

        if(tweetTime >= currentTime){
            //due to small differences in times, this probably means the tweet just happened.
            return getContext().getString(R.string.moments_ago);
        }

        relativeDate = DateUtils.getRelativeTimeSpanString(tweetTime, currentTime, DateUtils.SECOND_IN_MILLIS).toString();

        //convert "2 minutes ago" to "2m"
        int firstSpace = relativeDate.indexOf(' ');
        relativeDate = relativeDate.replace(" ", "");
        relativeDate = relativeDate.substring(0, firstSpace + 1);

        return relativeDate;
    }
}

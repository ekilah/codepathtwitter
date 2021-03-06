package com.codepath.apps.restclienttemplate.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApplication;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.fragments.HomeTimelineFragment;
import com.codepath.apps.restclienttemplate.fragments.TimelineFragment;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by mekilah on 2/19/15.
 */
public class TweetArrayAdapter extends ArrayAdapter<Tweet>{
    private TimelineFragment.TimelineFragmentListener timelineFragmentListener;

    public TweetArrayAdapter(Context context, TimelineFragment.TimelineFragmentListener callingActivity, List<Tweet> objects){
        super(context, R.layout.timeline_item, objects);
        this.timelineFragmentListener = callingActivity;
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
        LinearLayout llRetweetInfoBar;
        TextView tvRetweetedBy;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        //Log.e("TWITTER", "tweetArrayAdapter.getView(position=" + position + ")");
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
            holder.llRetweetInfoBar = (LinearLayout) convertView.findViewById(R.id.llRetweetInfoBar);
            holder.tvRetweetedBy = (TextView) convertView.findViewById(R.id.tvRetweetedBy);
            convertView.setTag(holder);
        }else{
            holder = (TweetViewHolder)convertView.getTag();
        }

        Picasso.with(getContext()).load(getItem(position).getPoster().getAvatarURL()).into(holder.ivPosterAvatar);
        holder.ivPosterAvatar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.i("TWITTER", "requesting profile for clicked image of user: @" + getItem(position).getPoster().getUsername());
                TweetArrayAdapter.this.timelineFragmentListener.ShowProfileForUser(getItem(position).getPoster());
            }
        });

        holder.tvUsername.setText("@" + getItem(position).getPoster().getUsername());
        holder.tvProfilename.setText(getItem(position).getPoster().getProfilename());
        holder.tvBody.setText(Html.fromHtml(getItem(position).getBody()));

        holder.tvTimestamp.setText(this.getRelativeTimeAgo(getItem(position).getTimestampString()));

        holder.tvRetweet.setText(NumberFormat.getNumberInstance(convertView.getResources().getConfiguration().locale).format(getItem(position).getRetweetCount()));
        holder.tvFavorite.setText(NumberFormat.getNumberInstance(convertView.getResources().getConfiguration().locale).format(getItem(position).getFavoriteCount()));

        if(getItem(position).isRetweet()){
            double drawableScaleForRetweetedBy = 0.4;
            Drawable retweetedBy = this.getContext().getResources().getDrawable(android.R.drawable.ic_popup_sync);
            retweetedBy.setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
            scaleDrawable(retweetedBy, drawableScaleForRetweetedBy);
            holder.tvRetweetedBy.setText(getItem(position).getPosterNotRetweeter().getProfilename() + " retweeted");
            holder.tvRetweetedBy.setCompoundDrawables(retweetedBy, null, null, null);
            holder.llRetweetInfoBar.setVisibility(View.VISIBLE);

        }else{
            holder.llRetweetInfoBar.setVisibility(View.GONE);
        }

        double drawableScaleForInteractionBarIcons = 0.7;

        Drawable retweet = this.getContext().getResources().getDrawable(android.R.drawable.ic_popup_sync);
        scaleDrawable(retweet, drawableScaleForInteractionBarIcons);
        if(getItem(position).posterRetweeted()){
            //retweeting is not undoable
            retweet.setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
            holder.tvRetweet.setClickable(false);
        }else{
            retweet.setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
            holder.tvRetweet.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(final View v){

                    TwitterClient client = TwitterApplication.getTwitterClient();
                    if(!client.isNetworkAvailable()){
                        Toast.makeText(getContext(), "No internet connection.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    client.retweetTweetWithId(getItem(position).getTweetId(), new JsonHttpResponseHandler(){
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                            getItem(position).setPosterRetweeted(!getItem(position).posterRetweeted());
                            if(getItem(position).posterRetweeted()){
                                ((Drawable) v.getTag()).setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                                getItem(position).setRetweetCount(getItem(position).getRetweetCount() + 1);
                                holder.tvRetweet.setText(String.valueOf(getItem(position).getRetweetCount()));
                                holder.tvRetweet.setClickable(false);//prevent attempts to retweet again
                            }else{
                                Log.e("TWITTER", "shouln't be able to un-retweet");
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse){
                            Toast.makeText(getContext(), getContext().getString(R.string.unable_to_retweet), Toast.LENGTH_SHORT).show();
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
        scaleDrawable(favoriteOff, drawableScaleForInteractionBarIcons);
        Drawable favoriteOn = this.getContext().getResources().getDrawable(android.R.drawable.btn_star_big_on);
        scaleDrawable(favoriteOn, drawableScaleForInteractionBarIcons);
        holder.tvFavorite.setTag(R.id.favoriteOff, favoriteOff);
        holder.tvFavorite.setTag(R.id.favoriteOn, favoriteOn);

        holder.tvFavorite.setCompoundDrawables((getItem(position).posterFavorited() ? (Drawable)holder.tvFavorite.getTag(R.id.favoriteOn) : (Drawable)holder.tvFavorite.getTag(R.id.favoriteOff)), null, null, null);
        holder.tvFavorite.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View v){

                final boolean isBeingFavorited = !getItem(position).posterFavorited();

                TwitterClient client = TwitterApplication.getTwitterClient();
                if(!client.isNetworkAvailable()){
                    Toast.makeText(getContext(), "No internet connection.", Toast.LENGTH_SHORT).show();
                    return;
                }
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
        scaleDrawable(reply, drawableScaleForInteractionBarIcons);
        reply.setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
        holder.tvReply.setCompoundDrawables(reply, null, null, null);
        holder.tvReply.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                timelineFragmentListener.ShowComposeTweetDialog(getItem(position));
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

        long numHrs = (currentTime - tweetTime)/1000/3600;

        if(tweetTime >= currentTime){
            //due to small differences in times, this probably means the tweet just happened.
            return getContext().getString(R.string.moments_ago);
        }else if(numHrs  > 24 &&  numHrs < 48){
            Log.w("TWITTER", "timestamp for yesterday being set=1d");
            return "1d";
        }

        relativeDate = DateUtils.getRelativeTimeSpanString(tweetTime, currentTime, DateUtils.SECOND_IN_MILLIS).toString();

        //convert "2 minutes ago" to "2m"
        int firstSpace = relativeDate.indexOf(' ');
        relativeDate = relativeDate.replace(" ", "");
        relativeDate = relativeDate.substring(0, firstSpace + 1);

        return relativeDate;
    }
}

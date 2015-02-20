package com.codepath.apps.restclienttemplate.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by mekilah on 2/19/15.
 */
public class TweetArrayAdapter extends ArrayAdapter<Tweet>{

    public TweetArrayAdapter(Context context, List<Tweet> objects){
        super(context, 0, objects);
    }

    class TweetViewHolder{
        ImageView ivPosterAvatar;
        TextView tvUsername;
        TextView tvProfilename;
        TextView tvBody;
        TextView tvTimestamp;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        TweetViewHolder holder;
        if(convertView == null){
            //inflate new view
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.timeline_item, parent, false);
            holder = new TweetViewHolder();
            holder.ivPosterAvatar = (ImageView) convertView.findViewById(R.id.ivTimelineItemPosterAvatar);
            holder.tvUsername = (TextView) convertView.findViewById(R.id.tvTimelineItemPosterUsername);
            holder.tvProfilename = (TextView) convertView.findViewById(R.id.tvTimelineItemPosterProfileName);
            holder.tvBody = (TextView) convertView.findViewById(R.id.tvTimelineItemTweetBody);
            holder.tvTimestamp = (TextView) convertView.findViewById(R.id.tvTimelineItemTimestamp);
            convertView.setTag(holder);
        }else{
            holder = (TweetViewHolder)convertView.getTag();
        }

        Picasso.with(getContext()).load(getItem(position).getPoster().getAvatarURL()).into(holder.ivPosterAvatar);
        holder.tvUsername.setText("@" + getItem(position).getPoster().getUsername());
        holder.tvProfilename.setText(getItem(position).getPoster().getProfilename());
        holder.tvBody.setText(getItem(position).getBody());
        holder.tvTimestamp.setText(this.getRelativeTimeAgo(getItem(position).getTimestampString()));

        return convertView;
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    private String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //convert "2 minutes ago" to "2m"
        int firstSpace = relativeDate.indexOf(' ');
        relativeDate = relativeDate.replace(" ", "");
        relativeDate = relativeDate.substring(0, firstSpace + 1);

        return relativeDate;
    }
}

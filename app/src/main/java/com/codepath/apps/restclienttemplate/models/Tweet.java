package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.codepath.apps.restclienttemplate.helpers.TwitterApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@Table(name = "Tweets")
public class Tweet extends Model{

    //@Column (etc) notation for active android
    @Column(name = "timestampString")
    private String timestampString;

    @Column(name = "body")
    private String body;

    @Column(name = "poster")
    private User poster;

    @Column(name = "posterFavorited")
    private boolean posterFavorited;

    @Column(name = "posterRetweeted")
    private boolean posterRetweeted;

    @Column(name = "favoriteCount")
    private long favoriteCount;

    @Column(name = "retweetCount")
    private long retweetCount;

    @Column(name = "tweetId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long tweetId;

    @Column(name = "forTimeline", notNull = true)
    private String timelineType = "home";

    //required empty constructor for activeandroid
    public Tweet(){
        super();
    }

    /**
     * Construct tweet object from JSON response
     * @param tweetObj JSON object from API representing a tweet
     */
    public Tweet(JSONObject tweetObj){
        //req'd by activeandroid
        super();

        updateFromJson(tweetObj);
    }

    /**
     * Construct tweet object from JSON response
     * @param tweetObj JSON object from API representing a tweet
     * @param timelineType The timeline this tweet is associated with (default='home') - used for separating timelines
     */
    public Tweet(JSONObject tweetObj, String timelineType){
        //req'd by activeandroid
        super();

        if(timelineType != null && timelineType.length() > 0){
            this.timelineType = timelineType;
        }
        updateFromJson(tweetObj);
    }

    public void updateFromJson(JSONObject tweetObj){
        try {
            this.timestampString = tweetObj.getString(TwitterApi.RESPONSE_KEY_TIMESTAMP);
            this.body = tweetObj.getString(TwitterApi.RESPONSE_KEY_TEXT);
            this.posterFavorited = tweetObj.getBoolean(TwitterApi.RESPONSE_KEY_USER_FAVORITED);
            this.posterRetweeted = tweetObj.getBoolean(TwitterApi.RESPONSE_KEY_USER_RETWEETED);
            this.favoriteCount = tweetObj.getLong(TwitterApi.RESPONSE_KEY_FAVORITE_COUNT);
            this.retweetCount = tweetObj.getLong(TwitterApi.RESPONSE_KEY_RETWEET_COUNT);
            this.tweetId = tweetObj.getLong(TwitterApi.RESPONSE_KEY_ID);

            JSONObject userObj = tweetObj.getJSONObject(TwitterApi.RESPONSE_KEY_USER);
            this.poster = User.findOrCreateFromJson(userObj);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Tweet> fromJsonArray(JSONArray jsonArray, String timelineType){
        ArrayList<Tweet> tweets = new ArrayList<Tweet>(jsonArray.length());

        for (int i=0; i < jsonArray.length(); i++) {
            JSONObject tweetJson;
            try {
                tweetJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            Tweet tweet = new Tweet(tweetJson, timelineType);
            tweet.save();
            tweets.add(tweet);
        }

        return tweets;
    }

    public static ArrayList<Tweet> fromJsonArray(JSONArray jsonArray) {
        ArrayList<Tweet> tweets = new ArrayList<Tweet>(jsonArray.length());

        for (int i=0; i < jsonArray.length(); i++) {
            JSONObject tweetJson;
            try {
                tweetJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            Tweet tweet = new Tweet(tweetJson); if(tweet.getBody().contains("RT")){
                Log.e("TWITTER", "RT'd tweet (timestamp str = " + tweet.getTimestampString() + ")body: ");
                for(int k=0; k < tweetJson.toString().length(); k+=4000){
                    Log.w("TWITTER", tweetJson.toString().substring(k, (k+4000 > tweetJson.toString().length() ? tweetJson.toString().length() : k + 4000)));
                }
            }
            tweet.save();
            tweets.add(tweet);
        }

        return tweets;
    }

    public String getTimestampString(){
        return timestampString;
    }

    public void setTimestampString(String timestamp){
        this.timestampString = timestamp;
    }

    public String getBody(){
        return body;
    }

    public void setBody(String body){
        this.body = body;
    }

    public User getPoster(){
        return poster;
    }

    public void setPoster(User poster){
        this.poster = poster;
    }

    public boolean posterFavorited(){
        return posterFavorited;
    }

    public void setPosterFavorited(boolean posterFavorited){
        this.posterFavorited = posterFavorited;
    }

    public boolean posterRetweeted(){
        return posterRetweeted;
    }

    public void setPosterRetweeted(boolean posterRetweeted){
        this.posterRetweeted = posterRetweeted;
    }

    public long getFavoriteCount(){
        return favoriteCount;
    }

    public void setFavoriteCount(long favoriteCount){
        this.favoriteCount = favoriteCount;
    }

    public long getRetweetCount(){
        return retweetCount;
    }

    public void setRetweetCount(long retweetCount){
        this.retweetCount = retweetCount;
    }


    public long getTweetId(){
        return tweetId;
    }

    public void setTweetId(long tweetId){
        this.tweetId = tweetId;
    }
}

package com.codepath.apps.restclienttemplate.models;

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

        try {
            this.timestampString = tweetObj.getString(TwitterApi.RESPONSE_KEY_TIMESTAMP);
            this.body = tweetObj.getString(TwitterApi.RESPONSE_KEY_TEXT);

            JSONObject userObj = tweetObj.getJSONObject(TwitterApi.RESPONSE_KEY_USER);
            this.poster = User.findOrCreateFromJson(userObj);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Tweet> fromJson(JSONArray jsonArray) {
        ArrayList<Tweet> tweets = new ArrayList<Tweet>(jsonArray.length());

        for (int i=0; i < jsonArray.length(); i++) {
            JSONObject tweetJson;
            try {
                tweetJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            Tweet tweet = new Tweet(tweetJson);
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
}

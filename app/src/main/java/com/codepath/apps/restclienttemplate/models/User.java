package com.codepath.apps.restclienttemplate.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Select;
import com.codepath.apps.restclienttemplate.helpers.TwitterApi;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mekilah on 2/19/15.
 */
public class User extends Model{

    @Column(name = "userId", unique = true)
    private String userId;

    @Column(name = "username")
    private String username;

    @Column(name = "profileName")
    private String profilename;

    @Column(name = "avatarURL")
    private String avatarURL;

    public User(){
        super();
    }

    private User(JSONObject userObj){
        try{
            this.userId = User.getUserIdFromUserJson(userObj);
            this.username = userObj.getString(TwitterApi.RESPONSE_KEY_USERNAME);
            this.profilename = userObj.getString(TwitterApi.RESPONSE_KEY_PROFILENAME);
            this.avatarURL = userObj.getString(TwitterApi.RESPONSE_KEY_PROFILE_IMAGE_URL);

            //get bigger image from twitter
            this.avatarURL = this.avatarURL.replace("normal.", "bigger.");

        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public static String getUserIdFromUserJson(JSONObject jsonObject) throws JSONException{
        return jsonObject.getString(TwitterApi.RESPONSE_KEY_ID_STR);
    }

    public static User findOrCreateFromJson(JSONObject json) {
        String userId = null;
        try{
            userId = User.getUserIdFromUserJson(json);

        }catch(JSONException e){
            e.printStackTrace();
        }

        if(userId != null){
            User existingUser = new Select().from(User.class).where("userId = ?", userId).executeSingle();

            if (existingUser != null) {
                // found and return existing user to avoid dupes
                return existingUser;
            }
        }

        // create and return new if the above didn't work
        User user = new User(json);
        user.save();
        return user;
    }

    public String getUserId(){
        return userId;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getProfilename(){
        return profilename;
    }

    public void setProfilename(String profilename){
        this.profilename = profilename;
    }


    public String getAvatarURL(){
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL){
        this.avatarURL = avatarURL;
    }
}

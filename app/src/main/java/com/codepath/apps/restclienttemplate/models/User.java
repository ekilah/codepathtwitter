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

    public static User authenticatedUser;

    @Column(name = "userId", unique = true)
    private String userId;

    @Column(name = "username")
    private String username;

    @Column(name = "profileName")
    private String profilename;

    @Column(name = "avatarURL")
    private String avatarURL;

    @Column(name = "backgroundURL")
    private String backgroundURL;

    @Column(name = "postsCount")
    private String postsCount;

    @Column(name = "followersCount")
    private String followersCount;

    @Column(name = "followingCount")
    private String followingCount;


    public User(){
        super();
    }

    private User(JSONObject userObj){
        try{
            this.userId = User.getUserIdFromUserJson(userObj);
            this.username = userObj.getString(TwitterApi.RESPONSE_KEY_USERNAME);
            this.profilename = userObj.getString(TwitterApi.RESPONSE_KEY_PROFILENAME);
            this.avatarURL = userObj.getString(TwitterApi.RESPONSE_KEY_USER_PROFILE_IMAGE_URL);
            this.postsCount = userObj.getString(TwitterApi.RESPONSE_KEY_USER_COUNT_POSTS);
            this.followersCount = userObj.getString(TwitterApi.RESPONSE_KEY_USER_COUNT_FOLLOWERS);
            this.followingCount = userObj.getString(TwitterApi.RESPONSE_KEY_USER_COUNT_FOLLOWING);

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

    public String getUsername(){
        return username;
    }

    public String getProfilename(){
        return profilename;
    }

    public String getAvatarURL(){
        return avatarURL;
    }

    public String getPostsCount(){
        return postsCount;
    }

    public String getFollowersCount(){
        return followersCount;
    }

    public String getFollowingCount(){
        return followingCount;
    }

    public String getBackgroundURL(){
        return backgroundURL;
    }

    public void setBackgroundURL(String backgroundURL){
        this.backgroundURL = backgroundURL;
        this.save();
    }
}

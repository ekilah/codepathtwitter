package com.codepath.apps.restclienttemplate.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApplication;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.fragments.TimelineFragment;
import com.codepath.apps.restclienttemplate.fragments.UserTimelineFragment;
import com.codepath.apps.restclienttemplate.helpers.TwitterApi;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ProfileActivity extends ActionBarActivity implements TimelineFragment.TimelineFragmentListener{

    public static String INTENT_EXTRA_USERID = "userId";

    User user;
    Target backgroundImageTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //replace actionbar with toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.ic_launcher);
        setSupportActionBar(toolbar);

        String userId = getIntent().getStringExtra(ProfileActivity.INTENT_EXTRA_USERID);
        this.user = new Select().from(User.class).where("userId = ?", userId).executeSingle();

        if(user == null){
            Log.e("TWITTER", "Can't show profile for userId=" + userId + ", does not exist in db");
            finish();
            return;
        }

        //successfully retrieved user object, populate profile
        Picasso.with(getContext()).load(this.user.getAvatarURL()).into((ImageView)findViewById(R.id.ivProfileAvatar));
        backgroundImageTarget = new Target(){
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from){
                RelativeLayout rlBioArea = (RelativeLayout) findViewById(R.id.rlProfileBioArea);
                rlBioArea.setBackground(new BitmapDrawable(getResources(), bitmap));
                Log.w("TWITTER", "onBitmapLoaded in profileactivity for username="+ProfileActivity.this.user.getUsername());

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable){
                Log.e("TWITTER", "target bitmap load failed in profileactivity for username" + ProfileActivity.this.user.getUsername());
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable){
                Log.w("TWITTER", "onprepareload target in profileactivity for username="+ProfileActivity.this.user.getUsername());
            }
        };

        //fetch backround image url and populate view
        this.fetchProfileBackground();

        TextView tvDisplayName = (TextView) findViewById(R.id.tvProfileDisplayName);
        TextView tvUsername = (TextView) findViewById(R.id.tvProfileUsername);
        TextView tvTweetCount = (TextView) findViewById(R.id.tvProfileCounterTweetsCount);
        TextView tvFollowingCount = (TextView) findViewById(R.id.tvProfileCounterFollowingCount);
        TextView tvFollowersCount = (TextView) findViewById(R.id.tvProfileCounterFollowersCount);

        tvDisplayName.setText(this.user.getProfilename());
        tvUsername.setText("@" + this.user.getUsername());
        tvTweetCount.setText(this.user.getPostsCount());
        tvFollowingCount.setText(this.user.getFollowingCount());
        tvFollowersCount.setText(this.user.getFollowersCount());

        UserTimelineFragment userTimelineFragment = UserTimelineFragment.newInstance(user.getUsername());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flProfileActivityFrame, userTimelineFragment);
        transaction.commit();

    }

    public void fetchProfileBackground(){
        TwitterClient client = TwitterApplication.getTwitterClient();
        if(!client.isNetworkAvailable()){
            Log.e("TWIITER", "can't fetch background image - network unavailable");
            return;
        }

        client.getUserProfileBannerByUsername(this.user.getUsername(), new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                try{
                    JSONObject sizesObj = response.getJSONObject(TwitterApi.RESPONSE_KEY_USER_PROFILE_BANNER_IMAGE_SIZES);
                    JSONObject mobileImageObj = sizesObj.getJSONObject(TwitterApi.RESPONSE_KEY_USER_PROFILE_BANNER_IMAGE_MOBILE);
                    ProfileActivity.this.user.setBackgroundURL(mobileImageObj.getString(TwitterApi.RESPONSE_KEY_USER_PROFILE_BANNER_IMAGE_URL));
                    Picasso.with(getContext()).load(ProfileActivity.this.user.getBackgroundURL()).into(backgroundImageTarget);
                    Log.w("TWITTER", "background URL being used for profile: " + ProfileActivity.this.user.getBackgroundURL());
                }catch(JSONException e){
                    Log.e("TWITTER", "error parsing JSON for successful getUserProfileBannerByUsername request. repsonse=" + (response==null ? "null" : response.toString()));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable){
                Log.e("TWITTER", "failed to get user profile banner. response string: " + (responseString == null ? "(null)" : responseString));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse){
                Log.e("TWITTER", "(HTTP " + statusCode + ")failed to get user profile banner. response string: " + (errorResponse == null ? "(null)" : errorResponse.toString()));

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void ShowComposeTweetDialog(Tweet inReplyToTweet){
        //can't do this here.
        Log.e("TWITTER", "trying to show compose tweet dialog in profile activity.");
    }

    @Override
    public void ShowOfflineModeToast(boolean repeat){
        Toast.makeText(this, "Offline mode.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void ShowProfileForUser(User user){
        Intent i = new Intent(this, ProfileActivity.class);
        i.putExtra(ProfileActivity.INTENT_EXTRA_USERID, user.getUserId());
        startActivity(i);
    }

    @Override
    public Context getContext(){
        return this;
    }
}

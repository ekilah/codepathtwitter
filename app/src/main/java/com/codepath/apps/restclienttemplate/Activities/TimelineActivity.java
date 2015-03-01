package com.codepath.apps.restclienttemplate.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.restclienttemplate.TwitterApplication;
import com.codepath.apps.restclienttemplate.fragments.ComposeTweetFragment;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.fragments.HomeTimelineFragment;
import com.codepath.apps.restclienttemplate.fragments.MentionsTimelineFragment;
import com.codepath.apps.restclienttemplate.fragments.TimelineFragment;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;

public class TimelineActivity extends ActionBarActivity implements HomeTimelineFragment.TimelineFragmentListener{

    HomeTimelineFragment homeTimelineFragment;
    MentionsTimelineFragment mentionsTimelineFragment;
    ArrayList<TimelineFragment> fragments;
    ViewPager viewPager;
    TimelinePagerAdapter timelinePagerAdapter;

    private class TimelinePagerAdapter extends FragmentPagerAdapter{

        private TimelinePagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position){
            return TimelineActivity.this.fragments.get(position);
        }

        @Override
        public int getCount(){
            return TimelineActivity.this.fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position){
            return TimelineActivity.this.fragments.get(position).visibleTitle;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        //replace actionbar with toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.ic_launcher);
        setSupportActionBar(toolbar);

        homeTimelineFragment = HomeTimelineFragment.newInstance();
        homeTimelineFragment.visibleTitle = "Home";
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.flTimelineActivityFrame, homeTimelineFragment);
//        transaction.commit();

        mentionsTimelineFragment = MentionsTimelineFragment.newInstance();
        mentionsTimelineFragment.visibleTitle = "Mentions";

        fragments = new ArrayList<>(2);
        fragments.add(homeTimelineFragment);
        fragments.add(mentionsTimelineFragment);

        viewPager = (ViewPager) findViewById(R.id.vpPager);
        timelinePagerAdapter = new TimelinePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(timelinePagerAdapter);

        PagerSlidingTabStrip pagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pagerSlidingTabStrip.setViewPager(viewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_compose){
            ShowComposeTweetDialog(null);
            return true;
        }
        if(id == R.id.action_profile){
            ShowAuthenticatedUserProfile();
        }

        return super.onOptionsItemSelected(item);
    }

    public void ShowAuthenticatedUserProfile(){
        if(User.authenticatedUser == null){
            //we don't know the data about the auth'd user yet. fetch and remember
            TwitterClient client = TwitterApplication.getTwitterClient();
            if(!client.isNetworkAvailable()){
                notifyNetworkUnavailable();
                return;
            }
            client.getUserAccountInfo(new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                    Log.d("TWITTER", "user credentials: " + response.toString());

                    User.authenticatedUser = User.findOrCreateFromJson(response);
                    ShowAuthenticatedUserProfileAfterAuthenticatedUserVerified();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse){
                    if(errorResponse != null){
                        Log.e("TWITTER", "API Failure (user): " + errorResponse.toString(), throwable);
                    }
                    Toast.makeText(TimelineActivity.this, "API failure (user).", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            ShowAuthenticatedUserProfileAfterAuthenticatedUserVerified();
        }
    }

    private void ShowAuthenticatedUserProfileAfterAuthenticatedUserVerified(){
        ShowProfileForUser(User.authenticatedUser);
    }

    public void ShowComposeTweetDialog(final Tweet inReplyToTweet){

        if(User.authenticatedUser == null){
            //we don't know the data about the auth'd user yet. fetch and remember
            TwitterClient client = TwitterApplication.getTwitterClient();
            if(!client.isNetworkAvailable()){
                notifyNetworkUnavailable();
                return;
            }
            client.getUserAccountInfo(new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                    Log.d("TWITTER", "user credentials: " + response.toString());

                    User.authenticatedUser = User.findOrCreateFromJson(response);
                    ShowComposeTweetDialogAfterAuthenticatedUserVerified(inReplyToTweet);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse){
                    if(errorResponse != null){
                        Log.e("TWITTER", "API Failure (user): " + errorResponse.toString(), throwable);
                    }
                    Toast.makeText(TimelineActivity.this, "API failure (user).", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            ShowComposeTweetDialogAfterAuthenticatedUserVerified(inReplyToTweet);
        }
    }

    private void ShowComposeTweetDialogAfterAuthenticatedUserVerified(final Tweet inReplyToTweet){
        ComposeTweetFragment composeTweetFragment = ComposeTweetFragment.newInstance(inReplyToTweet);
        composeTweetFragment.addComposeTweetFragmentListener(new ComposeTweetFragment.ComposeTweetFragmentListener(){
            @Override
            public void onSendTweet(String tweetBody){
                TimelineActivity.this.SendTweet(tweetBody, inReplyToTweet);
            }
        });
        composeTweetFragment.show(getSupportFragmentManager(), "fragment_compose_tweet");
    }

    public void SendTweet(final String tweetBody, Tweet inReplyToTweet){
        TwitterClient client = TwitterApplication.getTwitterClient();
        if(!client.isNetworkAvailable()){
            notifyNetworkUnavailable();
            return;
        }
        client.postTweet(tweetBody, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                //response is a Tweet object representing the tweet we sent out
                Tweet post = new Tweet(response);
                TimelineActivity.this.homeTimelineFragment.newTweetPostedByUser(post);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable){
                Toast.makeText(TimelineActivity.this, "Tweet failed to post", Toast.LENGTH_SHORT);
            }
        }, (inReplyToTweet == null ? -1 : inReplyToTweet.getTweetId()));
    }

    public void notifyNetworkUnavailable(){
        Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
    }

    boolean haveShownOfflineModeAlready = false;

    /**
     * Show a toast message saying we're in offline mode.
     * Will only show one per lifetime of the activity unless repeat is set to true.
     * @param repeat Whether to show it regardless of if it's been shown before or not.
     */
    @Override
    public void ShowOfflineModeToast(boolean repeat){
        if(!haveShownOfflineModeAlready || repeat){
            Toast.makeText(this, "Offline mode.", Toast.LENGTH_SHORT).show();
        }
        this.haveShownOfflineModeAlready = true;
    }

    @Override
    public Context getContext(){
        return this;
    }

    @Override
    public void ShowProfileForUser(User user){
        Intent i = new Intent(this, ProfileActivity.class);
        i.putExtra(ProfileActivity.INTENT_EXTRA_USERID, user.getUserId());
        startActivity(i);
    }
}

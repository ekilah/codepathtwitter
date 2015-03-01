package com.codepath.apps.restclienttemplate.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.adapters.TweetArrayAdapter;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mekilah on 2/28/15.
 */
public abstract class TimelineFragment extends Fragment{

    //req'd empty constructor
    public TimelineFragment(){super();}

    public abstract void fetchMoreTweets();
    public abstract void fetchNewerTweets();

    public String visibleTitle;

    protected ArrayList<Tweet> tweets;
    protected TweetArrayAdapter tweetArrayAdapter;
    protected ListView lvFeed;

    protected SwipeRefreshLayout swipeContainer;

    public interface TimelineFragmentListener{
        public void ShowComposeTweetDialog(Tweet inReplyToTweet);
        public void ShowOfflineModeToast(boolean repeat);
        public void ShowProfileForUser(User user);
        public Context getContext();
    }

    public TimelineFragmentListener timelineFragmentListener;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        Log.e("TWEETS", "onAttach TimelineFragment");

        if(activity instanceof TimelineFragmentListener){
            this.timelineFragmentListener = (TimelineFragmentListener) activity;
        }else{
            throw new ClassCastException(activity.toString() + " must implement TimelineFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    public void notifyNetworkUnavailable(){
        Toast.makeText(this.timelineFragmentListener.getContext(), "No internet connection!", Toast.LENGTH_SHORT).show();
    }

    public void setupSwipeToRefresh(View v){
        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchNewerTweets();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.TwitterColor, android.R.color.darker_gray);

    }

}

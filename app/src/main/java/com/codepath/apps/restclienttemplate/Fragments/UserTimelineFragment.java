package com.codepath.apps.restclienttemplate.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApplication;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.adapters.TweetArrayAdapter;
import com.codepath.apps.restclienttemplate.helpers.EndlessScrollListener;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by mekilah on 2/26/15.
 */
public class UserTimelineFragment extends TimelineFragment{

    public static final String TWEET_TIMELINE_TYPE = "user";

    private long maxId = -1;
    private long sinceId = -1;

    private String username;

    //req'd empty constructor
    public UserTimelineFragment(){
        super();
    }

    public static UserTimelineFragment newInstance(String username){
        UserTimelineFragment userTimelineFragment = new UserTimelineFragment();

        //bundle
        //bundle.putInt
        //fragment.setarguments(bundle)
        Bundle args = new Bundle();
        args.putString("username", username);
        userTimelineFragment.setArguments(args);

        return userTimelineFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        this.username = getArguments().getString("username", null);
        if(this.username == null){
            Log.e("TWITTER", "null username received in onCreate of UserTimelineFragment.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_timeline, container, false);


        lvFeed = (ListView)v.findViewById(R.id.lvTimelineFeed);
        tweets = new ArrayList<>();
        tweetArrayAdapter = new TweetArrayAdapter(getActivity(),this.timelineFragmentListener, tweets);
        lvFeed.setAdapter(tweetArrayAdapter);


        lvFeed.setOnScrollListener(new EndlessScrollListener(){
            @Override
            public void onLoadMore(int page, int totalItemsCount){
                fetchMoreTweets();
            }
        });

        setupSwipeToRefresh(v);

        return v;
    }

    @Override
    public void onStart(){
        super.onStart();
        fetchMoreTweets();
    }

    /**
     * uses sinceId to get newer tweets
     */
    @Override
    public void fetchNewerTweets(){
        Log.d("TWITTER", "fetchNewerTweets: sinceId on entry=" + sinceId);
        TwitterClient client = TwitterApplication.getTwitterClient();
        if(!client.isNetworkAvailable()){
            notifyNetworkUnavailable();
            swipeContainer.setRefreshing(false);
            return;
        }
        client.getUserTimelineByUsername(this.username, -1, sinceId, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response){
                Log.d("TWITTER", "user profile timeline: " + response.toString());

                ArrayList<Tweet> newList = Tweet.fromJsonArray(response);
                if(newList.size() <= 0){
                    Log.d("TWITTER", "fetchNewerTweets: no new tweets. response=" + response.toString());
                    Toast.makeText(UserTimelineFragment.this.timelineFragmentListener.getContext(), "No new tweets to show.", Toast.LENGTH_SHORT).show();
                    swipeContainer.setRefreshing(false);
                    return;
                }
                sinceId = newList.get(0).getTweetId();
                Log.d("TWITTER", "fetchNewerTweets: sinceId after fetch=" + sinceId);

                tweetArrayAdapter.notifyDataSetChanged();
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse){
                swipeContainer.setRefreshing(false);
                if(errorResponse != null){
                    Log.e("TWITTER", "API Failure (newer): " + errorResponse.toString(), throwable);
                }
                Toast.makeText(UserTimelineFragment.this.timelineFragmentListener.getContext(), "API failure (newer).", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * uses maxId to get older tweets (pagination)
     */
    @Override
    public void fetchMoreTweets(){
        TwitterClient client = TwitterApplication.getTwitterClient();
        if(!client.isNetworkAvailable()){
            Log.e("TWITTER","network unavailable i user:fetchMore. sinceId=" + sinceId);
            if(sinceId == -1){
                //first try. load from db?
                showOfflineTweets();
            }else{
                notifyNetworkUnavailable();
            }
            return;
        }
        client.getUserTimelineByUsername(this.username, maxId, -1, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response){
                Log.d("TWITTER", "user profile timeline: " + response.toString());

                ArrayList<Tweet> newList = Tweet.fromJsonArray(response);
                if(newList.size() <= 0){
                    Log.e("TWITTER", "Error: new list of tweets on fetchMoreTweets has bad size.");
                    Toast.makeText(UserTimelineFragment.this.timelineFragmentListener.getContext(), "Error fetching more tweets.", Toast.LENGTH_SHORT).show();
                    return;
                }
                maxId = newList.get(newList.size() - 1).getTweetId() - 1;
                tweets.addAll(newList);
                tweetArrayAdapter.notifyDataSetChanged();

                if(sinceId == -1){
                    //first update. store id of newest tweet to avoid extra data usage
                    sinceId = newList.get(0).getTweetId();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse){
                if(errorResponse != null){
                    Log.e("TWITTER", "API Failure (more): " + errorResponse.toString(), throwable);
                }

                if(sinceId == -1){
                    //first try. load from db?
                    showOfflineTweets();
                }else{
                    Toast.makeText(UserTimelineFragment.this.timelineFragmentListener.getContext(), "API failure (more).", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showOfflineTweets(){
        List<Tweet> list = new Select().from(Tweet.class).where("forTimeline='" + UserTimelineFragment.TWEET_TIMELINE_TYPE+"'").orderBy("tweetId DESC").execute();

        if(list.size() > 0){
            tweetArrayAdapter.addAll(list);
            sinceId = list.get(0).getTweetId();
            this.timelineFragmentListener.ShowOfflineModeToast(false);
        }else{
            Toast.makeText(this.timelineFragmentListener.getContext(), "No offline tweets stored.", Toast.LENGTH_SHORT).show();
        }
    }
}

package com.codepath.apps.restclienttemplate.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.RestClient;
import com.codepath.apps.restclienttemplate.activities.TimelineActivity;
import com.codepath.apps.restclienttemplate.adapters.TweetArrayAdapter;
import com.codepath.apps.restclienttemplate.helpers.EndlessScrollListener;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
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
public class HomeTimelineFragment extends Fragment{

    public interface TimelineFragmentListener{
        public void ShowComposeTweetDialog(Tweet inReplyToTweet);
        public Context getContext();
    }

    public String visibleTitle;

    public TimelineFragmentListener timelineFragmentListener;

    private ArrayList<Tweet> tweets;
    private TweetArrayAdapter tweetArrayAdapter;
    private ListView lvFeed;
    private static long maxId = -1;
    private static long sinceId = -1;

    /**
     * Array of tweets posted by the user in this app's lifetime.
     * Need this so on requests where we pull the newest tweets, we remove those tweets from the tweets array so they don't show twice.
     */
    private Map<Long, Tweet> userPostedTweetIds;


    //req'd empty constructor
    public HomeTimelineFragment(){
        super();
    }

    public static HomeTimelineFragment newInstance(){
        HomeTimelineFragment homeTimelineFragment = new HomeTimelineFragment();

        //bundle
        //bundle.putInt
        //fragment.setarguments(bundle)

        return homeTimelineFragment;
    }


    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        Log.e("TWEETS", "onAttach HomeTimelineFragment");

        if(activity instanceof  TimelineFragmentListener){
            this.timelineFragmentListener = (TimelineFragmentListener) activity;
            Log.e("TWEETS", "onAttach success HomeTimelineFragment");
        }else{
            throw new ClassCastException(activity.toString() + " must implement TimelineFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.e("TWEETS", "onCreate HomeTimelineFragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_timeline_home, container, false);

        Log.e("TWEETS", "onCreateView HomeTimelineFragment");
        userPostedTweetIds = new HashMap<Long, Tweet>();

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

        return v;
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.e("TWITTER", "onStart in HomeTimelineFragment");
        fetchMoreTweets();
    }

    public void newTweetPostedByUser(Tweet post){
        try{
            userPostedTweetIds.put(post.getTweetId(), post);
            tweets.add(0, post);
            tweetArrayAdapter.notifyDataSetChanged();
        }catch(Exception e){
            Log.e("TWITTER", "error posting tweet", e);
            Toast.makeText(this.timelineFragmentListener.getContext(), "Error with posted tweet", Toast.LENGTH_SHORT);
            e.printStackTrace();
        }
    }


    /**
     * uses sinceId to get newer tweets
     */
    private void fetchNewerTweets(){
        Log.d("TWITTER", "fetchNewerTweets: sinceId on entry=" + sinceId);
        RestClient client = RestApplication.getRestClient();
        if(!client.isNetworkAvailable()){
            notifyNetworkUnavailable();
            return;
        }
        client.getHomeTimeline(-1, sinceId, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response){
                Log.d("TWITTER", "timeline: " + response.toString());

                ArrayList<Tweet> newList = Tweet.fromJson(response);
                if(newList.size() <=0){
                    Log.d("TWITTER", "fetchNewerTweets: no new tweets. response=" + response.toString());
                    Toast.makeText(HomeTimelineFragment.this.timelineFragmentListener.getContext(), "No new tweets to show.", Toast.LENGTH_SHORT).show();
                    return;
                }
                sinceId = newList.get(0).getTweetId();
                Log.d("TWITTER", "fetchNewerTweets: sinceId after fetch=" + sinceId);

                for(int i=newList.size()-1; i >= 0; --i){
                    //add tweets to beginning of list
                    Log.d("TWITTER", "fetchNewerTweets: tweetId of item #" + i + " =" + newList.get(i).getTweetId());

                    Map.Entry<Long, Tweet> duplicatedTweet = null;
                    Set<Map.Entry<Long, Tweet>> entrySet = userPostedTweetIds.entrySet();

                    for(Map.Entry<Long, Tweet> entry: entrySet){
                        if(entry.getKey().longValue() == newList.get(i).getTweetId()){
                            Log.w("TWITTER", "userPostedTweetId entry " + entry.getKey().longValue() + " is the same as newList[" + i + "]. removing old instance");
                            duplicatedTweet = entry;
                            break;
                        }
                    }

                    if(duplicatedTweet != null){
                        boolean removalSuccess = tweets.remove(duplicatedTweet.getValue());
                        Log.w("TWITTER", "Removal from tweets success: " + removalSuccess);

                        entrySet.remove(duplicatedTweet);
                        Log.w("TWITTER", "Removal from userPostedTweetIds success: " + removalSuccess);
                    }

                    tweets.add(0, newList.get(i));
                }
                tweetArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse){
                if(errorResponse != null){
                    Log.e("TWITTER", "API Failure (newer): " + errorResponse.toString(), throwable);
                }
                Toast.makeText(HomeTimelineFragment.this.timelineFragmentListener.getContext(), "API failure (newer).", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * uses maxId to get older tweets (pagination)
     */
    private void fetchMoreTweets(){
        RestClient client = RestApplication.getRestClient();
        if(!client.isNetworkAvailable()){
            notifyNetworkUnavailable();
            return;
        }
        client.getHomeTimeline(maxId, -1, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response){
                Log.d("TWITTER", "timeline: " + response.toString());

                ArrayList<Tweet> newList = Tweet.fromJson(response);
                if(newList.size() <=0){
                    Log.e("TWITTER", "Error: new list of tweets on fetchMoreTweets has bad size.");
                    Toast.makeText(HomeTimelineFragment.this.timelineFragmentListener.getContext(), "Error fetching more tweets.", Toast.LENGTH_SHORT).show();
                    return;
                }
                maxId = newList.get(newList.size() -1).getTweetId() - 1;
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
                    List<Tweet> list = new Select().from(Tweet.class).orderBy("tweetId DESC").execute();
                    tweetArrayAdapter.addAll(list);
                    sinceId = list.get(0).getTweetId();
                    Toast.makeText(HomeTimelineFragment.this.timelineFragmentListener.getContext(), "Offline mode.", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(HomeTimelineFragment.this.timelineFragmentListener.getContext(), "API failure (more).", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void notifyNetworkUnavailable(){
        Toast.makeText(HomeTimelineFragment.this.timelineFragmentListener.getContext(), "No internet connection.", Toast.LENGTH_SHORT).show();
    }

}

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
public class MentionsTimelineFragment extends TimelineFragment{

    public static final String TWEET_TIMELINE_TYPE = "mentions";


    private static long maxId = -1;
    private static long sinceId = -1;

    /**
     * Array of tweets posted by the user in this app's lifetime.
     * Need this so on requests where we pull the newest tweets, we remove those tweets from the tweets array so they don't show twice.
     */
    private Map<Long, Tweet> userPostedTweetIds;

    public static MentionsTimelineFragment newInstance(){
        MentionsTimelineFragment mentionsTimelineFragment = new MentionsTimelineFragment();

        //bundle
        //bundle.putInt
        //fragment.setarguments(bundle)

        return mentionsTimelineFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_timeline_home, container, false);

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
        fetchMoreTweets();
    }

    public void newTweetPostedByUser(Tweet post){
        try{
            userPostedTweetIds.put(post.getTweetId(), post);
            tweets.add(0, post);
            tweetArrayAdapter.notifyDataSetChanged();
        }catch(Exception e){
            Log.e("TWITTER", "mentions error posting tweet", e);
            Toast.makeText(this.timelineFragmentListener.getContext(), "Error with posted tweet", Toast.LENGTH_SHORT);
            e.printStackTrace();
        }
    }


    /**
     * uses sinceId to get newer tweets
     */
    @Override
    public void fetchNewerTweets(){
        Log.d("TWITTER", "mentions fetchNewerTweets: sinceId on entry=" + sinceId);
        TwitterClient client = TwitterApplication.getTwitterClient();
        if(!client.isNetworkAvailable()){
            notifyNetworkUnavailable();
            return;
        }
        client.getMentionsTimeline(-1, sinceId, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response){
                Log.d("TWITTER", "mentions timeline: " + response.toString());

                ArrayList<Tweet> newList = Tweet.fromJsonArray(response, MentionsTimelineFragment.TWEET_TIMELINE_TYPE);
                if(newList.size() <= 0){
                    Log.d("TWITTER", "mentions fetchNewerTweets: no new tweets. response=" + response.toString());
                    Toast.makeText(MentionsTimelineFragment.this.timelineFragmentListener.getContext(), "No new tweets to show.", Toast.LENGTH_SHORT).show();
                    return;
                }
                sinceId = newList.get(0).getTweetId();
                Log.d("TWITTER", "mentions fetchNewerTweets: sinceId after fetch=" + sinceId);

                for(int i = newList.size() - 1; i >= 0; --i){
                    //add tweets to beginning of list
                    Log.d("TWITTER", "mentions fetchNewerTweets: tweetId of item #" + i + " =" + newList.get(i).getTweetId());

                    Map.Entry<Long, Tweet> duplicatedTweet = null;
                    Set<Map.Entry<Long, Tweet>> entrySet = userPostedTweetIds.entrySet();

                    for(Map.Entry<Long, Tweet> entry : entrySet){
                        if(entry.getKey().longValue() == newList.get(i).getTweetId()){
                            Log.w("TWITTER", "mentions userPostedTweetId entry " + entry.getKey().longValue() + " is the same as newList[" + i + "]. removing old instance");
                            duplicatedTweet = entry;
                            break;
                        }
                    }

                    if(duplicatedTweet != null){
                        boolean removalSuccess = tweets.remove(duplicatedTweet.getValue());
                        Log.w("TWITTER", "mentions Removal from tweets success: " + removalSuccess);

                        entrySet.remove(duplicatedTweet);
                        Log.w("TWITTER", "mentions Removal from userPostedTweetIds success: " + removalSuccess);
                    }

                    tweets.add(0, newList.get(i));
                }
                tweetArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse){
                if(errorResponse != null){
                    Log.e("TWITTER", "mentions API Failure (newer): " + errorResponse.toString(), throwable);
                }
                Toast.makeText(MentionsTimelineFragment.this.timelineFragmentListener.getContext(), "API failure (newer mentions).", Toast.LENGTH_SHORT).show();
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
            if(sinceId == -1){
                //first try. load from db?
                showOfflineTweets();
            }else{
                notifyNetworkUnavailable();
            }
            return;
        }
        client.getMentionsTimeline(maxId, -1, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response){
                Log.d("TWITTER", "mentions timeline: " + response.toString());

                ArrayList<Tweet> newList = Tweet.fromJsonArray(response, MentionsTimelineFragment.TWEET_TIMELINE_TYPE);
                if(newList.size() <= 0){
                    Log.e("TWITTER", "mentions Error: new list of tweets on fetchMoreTweets has bad size.");
                    Toast.makeText(MentionsTimelineFragment.this.timelineFragmentListener.getContext(), "Error fetching more mentions tweets.", Toast.LENGTH_SHORT).show();
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
                    Log.e("TWITTER", "API Failure (more mentions): " + errorResponse.toString(), throwable);
                }

                if(sinceId == -1){
                    //first try. load from db?
                    showOfflineTweets();
                }else{
                    Toast.makeText(MentionsTimelineFragment.this.timelineFragmentListener.getContext(), "API failure (more mentions).", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showOfflineTweets(){
        List<Tweet> list = new Select().from(Tweet.class).where("forTimeline='" + MentionsTimelineFragment.TWEET_TIMELINE_TYPE+"'").orderBy("tweetId DESC").execute();

        if(list.size() > 0){
            tweetArrayAdapter.addAll(list);
            sinceId = list.get(0).getTweetId();
            Toast.makeText(this.timelineFragmentListener.getContext(), "Offline mode.", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this.timelineFragmentListener.getContext(), "No offline tweets stored.", Toast.LENGTH_SHORT).show();
        }
    }
}

package com.codepath.apps.restclienttemplate.Activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.codepath.apps.restclienttemplate.Fragments.ComposeTweetFragment;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.RestClient;
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

public class TimelineActivity extends ActionBarActivity{


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

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        userPostedTweetIds = new HashMap<Long, Tweet>();

        //replace actionbar with toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setTitle("Home");
        //toolbar.setTitleTextColor(Color.WHITE);
        Button toolbarTitle = (Button)toolbar.findViewById(R.id.btnToolbarTitle);
        toolbarTitle.setText("Home");
        toolbarTitle.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                fetchNewerTweets();
            }
        });
        toolbar.setLogo(R.drawable.ic_launcher);
        setSupportActionBar(toolbar);

        lvFeed = (ListView)findViewById(R.id.lvTimelineFeed);
        tweets = new ArrayList<>();
        tweetArrayAdapter = new TweetArrayAdapter(TimelineActivity.this, tweets);
        lvFeed.setAdapter(tweetArrayAdapter);

        fetchMoreTweets();

        lvFeed.setOnScrollListener(new EndlessScrollListener(){
            @Override
            public void onLoadMore(int page, int totalItemsCount){
                fetchMoreTweets();
            }
        });
    }

    /**
     * uses sinceId to get newer tweets
     */
    private void fetchNewerTweets(){
        Log.d("TWITTER", "fetchNewerTweets: sinceId on entry=" + sinceId);
        RestClient client = RestApplication.getRestClient();
        client.getHomeTimeline(-1, sinceId, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response){
                Log.d("TWITTER", "timeline: " + response.toString());

                ArrayList<Tweet> newList = Tweet.fromJson(response);
                if(newList.size() <=0){
                    Log.d("TWITTER", "fetchNewerTweets: no new tweets. response=" + response.toString());
                    Toast.makeText(TimelineActivity.this, "No new tweets to show.", Toast.LENGTH_SHORT).show();
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
                            Log.e("TWITTER", "userPostedTweetId entry " + entry.getKey().longValue() + " is the same as newList[" + i + "]. removing old instance");
                            duplicatedTweet = entry;
                            break;
                        }
                    }

                    if(duplicatedTweet != null){
                        boolean removalSuccess = tweets.remove(duplicatedTweet.getValue());
                        Log.e("TWITTER", "Removal from tweets success: " + removalSuccess);

                        entrySet.remove(duplicatedTweet);
                        Log.e("TWITTER", "Removal from userPostedTweetIds success: " + removalSuccess);
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
                Toast.makeText(TimelineActivity.this, "API failure (newer).", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * uses maxId to get older tweets (pagination)
     */
    private void fetchMoreTweets(){
        RestClient client = RestApplication.getRestClient();
        client.getHomeTimeline(maxId, -1, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response){
                Log.d("TWITTER", "timeline: " + response.toString());

                ArrayList<Tweet> newList = Tweet.fromJson(response);
                if(newList.size() <=0){
                    Log.e("TWITTER", "Error: new list of tweets on fetchMoreTweets has bad size.");
                    Toast.makeText(TimelineActivity.this, "Error fetching more tweets.", Toast.LENGTH_SHORT).show();
                    return;
                }
                maxId = newList.get(newList.size() -1).getTweetId() - 1;
                tweetArrayAdapter.addAll(newList);

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
                    Toast.makeText(TimelineActivity.this, "Offline mode.", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(TimelineActivity.this, "API failure (more).", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

        return super.onOptionsItemSelected(item);
    }

    public void ShowComposeTweetDialog(final Tweet inReplyToTweet){

        if(User.authenticatedUser == null){
            //we don't know the data about the auth'd user yet. fetch and remember
            RestApplication.getRestClient().getUserAccountInfo(new JsonHttpResponseHandler(){
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
        RestApplication.getRestClient().postTweet(tweetBody, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                //response is a Tweet object representing the tweet we sent out
                Tweet post = new Tweet(response);
                try{
                    userPostedTweetIds.put(post.getTweetId(), post);
                    tweets.add(0, post);
                    tweetArrayAdapter.notifyDataSetChanged();
                }catch(Exception e){
                    Log.e("TWITTER", "error posting tweet", e);
                    Toast.makeText(TimelineActivity.this, "Error with posted tweet", Toast.LENGTH_SHORT);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable){
                Toast.makeText(TimelineActivity.this, "Tweet failed to post", Toast.LENGTH_SHORT);
            }
        }, (inReplyToTweet == null ? -1 : inReplyToTweet.getTweetId()));
    }
}

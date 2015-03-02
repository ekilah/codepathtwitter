package com.codepath.apps.restclienttemplate;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.FlickrApi;
import org.scribe.builder.api.TwitterApi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class;
	public static final String REST_URL = "https://api.twitter.com/1.1";
	public static final String REST_CONSUMER_KEY = "MV4fo7uWKQH2iUemkGiRINokW";
	public static final String REST_CONSUMER_SECRET = "XPwpZZeUGI6uhWFx84MexglYfSYXRd36le1C3o27pWyPb9pDMb";
	public static final String REST_CALLBACK_URL = "oauth://mekilahcodepathtwitter";

	public TwitterClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

    public void getHomeTimeline(long maxId, long sinceId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/home_timeline.json");
        RequestParams params = new RequestParams();

        if(maxId >=0){
            params.put("max_id", maxId);
        }
        if(sinceId >=0){
            params.put("since_id", sinceId);
        }

        getClient().get(apiUrl, params, handler);
    }

    public void getUserTimelineByUsername(String username, long maxId, long sinceId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/user_timeline.json");
        RequestParams params = new RequestParams();

        params.put("screen_name", username);

        if(maxId >=0){
            params.put("max_id", maxId);
        }
        if(sinceId >=0){
            params.put("since_id", sinceId);
        }

        getClient().get(apiUrl, params, handler);
    }

    public void getUserProfileBannerByUsername(String username, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("users/profile_banner.json");
        RequestParams params = new RequestParams();

        params.put("screen_name", username);

        getClient().get(apiUrl, params, handler);
    }

    public void getMentionsTimeline(long maxId, long sinceId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/mentions_timeline.json");
        RequestParams params = new RequestParams();
        Log.e("TWITTER", "mentions: maxId=" + maxId + ", sinceId=" + sinceId);
        if(maxId >=0){
            params.put("max_id", maxId);
        }
        if(sinceId >=0){
            params.put("since_id", sinceId);
        }

        getClient().get(apiUrl, params, handler);
    }

    public void getUserAccountInfo(AsyncHttpResponseHandler handler){
        String apiUrl = getApiUrl("account/verify_credentials.json");
        getClient().get(apiUrl, handler);
    }

    /**
     * @param body Tweet text to send via the API
     * @param handler The response handler. This endpoint returns a {Tweet}
     */
    public void postTweet(String body, AsyncHttpResponseHandler handler){
         postTweet(body, handler, -1);
    }

    /**
     * @param body Tweet text to send via the API
     * @param handler The response handler. This endpoint returns a {Tweet}
     * @param inReplyToTweetId The id that this tweet is in response to. Negative numbers can be used to ignore this param
     */
    public void postTweet(String body, AsyncHttpResponseHandler handler, long inReplyToTweetId) {
        if(body == null || body.length() == 0){
            Toast.makeText(context, "Can't post empty tweet.", Toast.LENGTH_SHORT).show();
            return;
        }
        String apiUrl = getApiUrl("statuses/update.json");
        RequestParams params = new RequestParams();
        params.put("status", body);
        if(inReplyToTweetId >=0){
            params.put("in_reply_to_status_id", inReplyToTweetId);
        }
        getClient().post(apiUrl, params, handler);
    }

    public void favoriteTweetWithId(long tweetId, boolean favorite, AsyncHttpResponseHandler handler){
        String apiUrl = getApiUrl("favorites/" + (favorite ? "create" : "destroy") +".json");
        RequestParams params = new RequestParams();
        params.put("id", tweetId);
        getClient().post(apiUrl, params, handler);
    }

    public void retweetTweetWithId(long tweetId, AsyncHttpResponseHandler handler){
        String apiUrl = getApiUrl("statuses/retweet/" + (String.valueOf(tweetId)) +".json");
        getClient().post(apiUrl, handler);
    }

    public Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
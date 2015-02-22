package com.codepath.apps.restclienttemplate.Fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.helpers.TwitterApi;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by mekilah on 2/21/15.
 */
public class ComposeTweetFragment extends DialogFragment{

    private Tweet inReplyToTweet;

    public interface ComposeTweetFragmentListener{
        public void onSendTweet(String tweetBody);
    }

    private ArrayList<ComposeTweetFragmentListener> composeTweetFragmentListeners;

    public void addComposeTweetFragmentListener(ComposeTweetFragmentListener listener){
        if(listener == null){
            return;
        }

        this.composeTweetFragmentListeners.add(listener);
    }

    public void removeComposeTweetFragmentListener(ComposeTweetFragmentListener listener){
        this.composeTweetFragmentListeners.remove(listener);
    }

    public ComposeTweetFragment(){}

    public static ComposeTweetFragment newInstance(){
        ComposeTweetFragment compose = new ComposeTweetFragment();
        compose.composeTweetFragmentListeners = new ArrayList<>();
        return compose;
    }

    public static ComposeTweetFragment newInstance(Tweet inReplyToTweet){
        ComposeTweetFragment composeTweetFragment = ComposeTweetFragment.newInstance();
        composeTweetFragment.inReplyToTweet = inReplyToTweet;
        return composeTweetFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_compose_tweet, container);

        Picasso.with(v.getContext()).load(User.authenticatedUser.getAvatarURL()).into((ImageView) v.findViewById(R.id.ivTimelineItemPosterAvatar));
        ((TextView)v.findViewById(R.id.tvTimelineItemPosterUsername)).setText("@" + User.authenticatedUser.getUsername());
        ((TextView)v.findViewById(R.id.tvTimelineItemPosterProfileName)).setText(User.authenticatedUser.getProfilename());

        final TextView tvCharacterCount = (TextView) v.findViewById(R.id.tvTimelineItemCharacterCount);
        tvCharacterCount.setText(String.valueOf(TwitterApi.TWEET_CHARACTER_LIMIT));
        final EditText etTweetBody = (EditText) v.findViewById(R.id.etTimelineItemTweetBody);
        final Button btnSubmitTweet = (Button) v.findViewById(R.id.btnSubmitCompose);
        final Button btnCancelTweet = (Button) v.findViewById(R.id.btnCancelCompose);

        btnCancelTweet.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ComposeTweetFragment.this.dismiss();
            }
        });

        btnSubmitTweet.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                for(ComposeTweetFragmentListener listener: composeTweetFragmentListeners){
                    listener.onSendTweet(etTweetBody.getText().toString());
                }

                ComposeTweetFragment.this.dismiss();
            }
        });

        btnSubmitTweet.setEnabled(false);

        etTweetBody.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){}
            @Override
            public void afterTextChanged(Editable s){
                tvCharacterCount.setText(String.valueOf(TwitterApi.TWEET_CHARACTER_LIMIT - s.length()));
                if(s.length() > TwitterApi.TWEET_CHARACTER_LIMIT || s.length() <=0){
                    btnSubmitTweet.setEnabled(false);
                    if(s.length() > 0){
                        tvCharacterCount.setTextColor(Color.RED);
                    }else{
                        tvCharacterCount.setTextColor(Color.BLACK);
                    }
                }else{
                    btnSubmitTweet.setEnabled(true);
                    tvCharacterCount.setTextColor(Color.BLACK);
                }
            }
        });

        if(this.inReplyToTweet != null){
            etTweetBody.setText("@" + this.inReplyToTweet.getPoster().getUsername() + getResources().getString(R.string.space));
            etTweetBody.setSelection(etTweetBody.getText().length());
        }

        etTweetBody.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return v;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return d;
    }
}

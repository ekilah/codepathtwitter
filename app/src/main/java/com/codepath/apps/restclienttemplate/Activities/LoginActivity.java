package com.codepath.apps.restclienttemplate.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.RestClient;
import com.codepath.oauth.OAuthLoginActionBarActivity;
import com.codepath.oauth.OAuthLoginActivity;
import com.squareup.picasso.Picasso;

public class LoginActivity extends OAuthLoginActionBarActivity<RestClient> {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
        ImageView ivBG = (ImageView) findViewById(R.id.ivLoginBG);
        Picasso.with(this).load("https://fbcdn-sphotos-d-a.akamaihd.net/hphotos-ak-frc3/v/t1.0-9/s720x720/1174546_10201962384487101_1256777977_n.jpg?oh=f1d347655783fcbd9e0fa07c74198676&oe=55938600&__gda__=1431422805_50b4a64d0a3296b95dc93fc18628bfea").into(ivBG);
        final Button btnLogin = (Button) this.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                loginToRest(v);
            }
        });
	}


	// Inflate the menu; this adds items to the action bar if it is present.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	// OAuth authenticated successfully, launch first authenticated activity
	@Override
	public void onLoginSuccess() {
        Log.e("TWITTER", "onLoginSuccess from LoginActivity");
		Intent i = new Intent(this, TimelineActivity.class);
		startActivity(i);
        this.finish();//prevent returning to this activity via the back button
	}

	// OAuth authentication flow failed, handle the error
	// i.e Display an error dialog or toast
	@Override
	public void onLoginFailure(Exception e) {
		e.printStackTrace();
        Toast.makeText(this, "Couldn't log in.", Toast.LENGTH_SHORT).show();
	}

	// Click handler method for the button used to start OAuth flow
	// Uses the client to initiate OAuth authorization
	// This should be tied to a button used to login
	public void loginToRest(View view) {
        if(!getClient().isNetworkAvailable()){
            Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
            return;
        }
		getClient().connect();
	}

}

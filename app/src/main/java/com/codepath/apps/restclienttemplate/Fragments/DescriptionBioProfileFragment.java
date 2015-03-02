package com.codepath.apps.restclienttemplate.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.models.User;
import com.squareup.picasso.Picasso;

/**
 * Created by mekilah on 3/1/15.
 */
public class DescriptionBioProfileFragment extends Fragment{
    User user;

    public static DescriptionBioProfileFragment newInstance(User user){
        DescriptionBioProfileFragment f = new DescriptionBioProfileFragment();
        f.user = user;

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        final View v = inflater.inflate(R.layout.fragment_profile_bio_more, container, false);

        TextView tvDescription = (TextView) v.findViewById(R.id.tvProfileDescription);
        TextView tvLocation = (TextView) v.findViewById(R.id.tvProfileLocation);

        tvDescription.setText(this.user.getDescription());
        tvLocation.setText(this.user.getLocation());

        return v;
    }
}

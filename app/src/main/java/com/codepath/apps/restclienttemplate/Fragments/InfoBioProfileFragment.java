package com.codepath.apps.restclienttemplate.fragments;

import com.codepath.apps.restclienttemplate.models.User;

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
import com.squareup.picasso.Picasso;

/**
 * Created by mekilah on 3/1/15.
 */
public class InfoBioProfileFragment extends Fragment{
    User user;
    public static InfoBioProfileFragment newInstance(User user){
        InfoBioProfileFragment f = new InfoBioProfileFragment();
        f.user = user;

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        final View v = inflater.inflate(R.layout.fragment_profile_bio, container, false);

        TextView tvDisplayName = (TextView) v.findViewById(R.id.tvProfileDisplayName);
        TextView tvUsername = (TextView) v.findViewById(R.id.tvProfileUsername);
        Picasso.with(getActivity()).load(this.user.getAvatarURL()).into((ImageView) v.findViewById(R.id.ivProfileAvatar));
        tvDisplayName.setText(this.user.getProfilename());
        tvUsername.setText("@" + this.user.getUsername());

        return v;
    }
}

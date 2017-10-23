package com.ville.ultimatechatclient;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/*
    A class for populating a GridView with ChatBubbles.
    @author Ville Lohkovuori
    10/2017
 */

public class UserListAdapter extends BaseAdapter {

    private Context context;
    private String[] users;

    public UserListAdapter(Context context, String[] users) {

        this.context = context;
        this.users = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String userName = users[position];
        Integer bgColor = Utils.userColors.get(userName);
        bgColor = bgColor != null ? bgColor : R.color.colorCUser; // either it's a random color or the own's user's color

        return new ChatBubble(this.context, userName, this.context.getResources().getColor(bgColor, null), 10.0f);
    } // end getView()

    // these are all needed to complete the class...
    @Override
    public int getCount() {

        return users.length;
    }

    @Override
    public long getItemId(int position) {

        return 0;
    }

    @Override
    public Object getItem(int position) {

        return null;
    }
} // end class
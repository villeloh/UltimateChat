package com.ville.ultimatechatclient;

import android.content.Context;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/*
    A class for defining a custom ChatBubble object (a modified TextView).
    I couldn't get the random colors to work with xml, so I opted to
    make a programmatic class instead.
    @author Ville Lohkovuori
    10/2017
 */

public class ChatBubble extends android.support.v7.widget.AppCompatTextView {

    private ShapeDrawable bgShape;

    public ChatBubble(Context context, String text, int color, float radius) {

        super(context);

        this.setText(text);
        this.setTextColor(Color.BLACK); // the text appears as grey without this, for some reason

        this.setElevation(10);

        // although it looks funny, it's needed due to the way the RoundRectShape works
        float[] corners = {radius, radius, radius, radius, radius, radius, radius, radius};

        this.bgShape = new ShapeDrawable(new RoundRectShape(corners, null, null));
        this.bgShape.getPaint().setColor(color);
        this.setBackground(bgShape);
    } // end constructor

    // adjusts the bubble's alignment and size in relation to the layout that it's part of.
    // NOTE: only works for LinearLayouts (which the standard bubbles are always part of)
    public void setParams(boolean isOwn, int margin, int minWidth, int maxWidth) {

        LinearLayout.LayoutParams bubbleParams = (LinearLayout.LayoutParams)this.getLayoutParams();
        bubbleParams.topMargin = margin;
        bubbleParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        bubbleParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        if(isOwn) {

            bubbleParams.gravity = Gravity.END;
        }
        else {
            bubbleParams.gravity = Gravity.START;
        }

        this.setLayoutParams(bubbleParams);
        this.setMinimumWidth(minWidth);
        this.setMaxWidth(maxWidth);
    } // end setParams()

    // needed to stop a nagging notification... the class works without them, but ehh, it doesn't hurt to have these
    public ChatBubble(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatBubble(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
    }
} // end class
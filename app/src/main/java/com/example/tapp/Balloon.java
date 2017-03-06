package com.example.tapp;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.AppCompatImageView;

public class Balloon extends AppCompatImageView
        implements View.OnTouchListener,
        Animator.AnimatorListener,
        ValueAnimator.AnimatorUpdateListener {

    public static final String TAG = "Balloon";

    private BalloonListener mListener;
    private boolean mPopped;

    public Balloon(Context context) {
        super(context);
    }

    public Balloon(Context context, int color, int rawHeight, int level) {
        super(context);

        this.mListener = (BalloonListener) context;

        this.setImageResource(R.drawable.circle);
        this.setColorFilter(color);

        int rawWidth = rawHeight / 2;

//      Calc balloon height and width as dp
        int dpHeight = pixelsToDp(rawHeight, context);
        int dpWidth = pixelsToDp(rawWidth, context);
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(dpWidth, dpHeight);
        setLayoutParams(params);

        setOnTouchListener(this);
    }

    private static int pixelsToDp(int px, Context context) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, px,
                context.getResources().getDisplayMetrics());
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if (!mPopped) {
            setY((Float) animation.getAnimatedValue());
        }
    }

    public interface BalloonListener {
        void popBalloon(Balloon balloon, boolean touched);
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
//      This means the balloon got to the top of the screen
        if (!mPopped) {
            mListener.popBalloon(this, false);
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
//      Call the activity's popBalloon() method
//      Cancel the animation so the ValueAnimator doesn't keep going
//      Flip the popped flag
        if (!mPopped && event.getAction() == MotionEvent.ACTION_DOWN) {
            mListener.popBalloon(this, true);
            mPopped = true;
        }
        return true;
    }
}
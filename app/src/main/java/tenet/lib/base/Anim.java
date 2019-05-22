package tenet.lib.base;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;

import java.util.ArrayList;
import java.util.Random;

import tenet.lib.base.utils.Utils;

public interface Anim {

    abstract class MultiAnim implements Animation.AnimationListener{
        View mView;

        public void setView(View view){
            mView = view;
        }

        public boolean start(){
            return start(mView);
        }

        public boolean start(View v){
            if(mView != null)
                mView.clearAnimation();
            mView = v;
            if(mView == null)
                return false;
            mView.clearAnimation();

            return playNextAnim(null) != null;
        }

        public View getView() {
            return mView;
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            animation.setAnimationListener(null);
            playNextAnim(animation);
        }

        Animation playNextAnim(Animation prev){
            if(mView == null)
                return null;
            Animation a = getNextAnimation(prev);
            if(a == null)
                return null;
            a.setAnimationListener(this);
            mView.startAnimation(a);
            return a;
        }

        protected abstract Animation getNextAnimation(Animation prev);
    }

    class ListAnimIds extends MultiAnim {
        ArrayList<Integer> mAnimIds = new ArrayList<>();
        int mCurPos = -1;

        public ListAnimIds add(int animId){
            mAnimIds.add(animId);
            return this;
        }

        @Override
        Animation playNextAnim(Animation prev) {
            Animation a = super.playNextAnim(prev);
            if(a == null)
                mCurPos = -1;
            return a;
        }

        @Override
        protected Animation getNextAnimation(Animation prev) {
            ++mCurPos;
            if(mCurPos>=mAnimIds.size() || mCurPos <0)
                return null;
            return AnimationUtils.loadAnimation(mView.getContext(),mAnimIds.get(mCurPos));
        }
    }

    enum Direction {
        LEFT,UP,RIGHT,DOWN;
        public boolean canChangeX(){
            return this == LEFT || this == RIGHT;
        }

        public boolean canChangeY(){
            return this == UP || this == DOWN;
        }

        public int moveX(int curX, int size){
            if(!canChangeX())
                return curX;
            return this == LEFT? curX - size : curX +size;
        }

        public int moveY(int curY, int size){
            if(!canChangeY())
                return curY;
            return this == LEFT? curY - size : curY +size;
        }
    }

    enum Corner{
        LEFT_TOP,
        CENTER_TOP,
        RIGHT_TOP,
        CENTER_RIGHT,
        RIGHT_BOTTOM,
        CENTER_BOTTOM,
        LEFT_BOTTOM,
        CENTER_LEFT,;

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    class FrameLayoutAnim extends MultiAnim
            implements ViewTreeObserver.OnWindowAttachListener{


        Point mMargins = new Point(0,0);
        Corner mCorner = Corner.LEFT_TOP;
        Random mRand = new Random();

        int getCenterY(){
            View parent = (View) mView.getParent();
            return parent.getHeight()/2 - mView.getHeight()/2;
        }

        int getCenterX(){
            View parent = (View) mView.getParent();
            return parent.getWidth()/2 - mView.getWidth()/2;
        }

        Point makeCoords(Corner corner, Point outPoint){
            if(outPoint == null)
                outPoint = new Point();
            View parent = (View) mView.getParent();
            switch (corner){
                case LEFT_TOP:
                    outPoint.set(mMargins.x,mMargins.y);
                    break;
                case RIGHT_TOP:
                    outPoint.set(parent.getWidth() - mView.getWidth() - mMargins.x,
                            mMargins.y);
                    break;
                case LEFT_BOTTOM:
                    outPoint.set(mMargins.x,
                            parent.getHeight() - mView.getHeight() - mMargins.y);
                    break;
                case RIGHT_BOTTOM:
                    outPoint.set(parent.getWidth() - mView.getWidth() - mMargins.x,
                            parent.getHeight() - mView.getHeight() - mMargins.y);
                    break;
                case CENTER_LEFT:
                    outPoint.set(mMargins.x,getCenterY());
                    break;
                case CENTER_RIGHT:
                    outPoint.set(parent.getWidth() - mView.getWidth() - mMargins.x,getCenterY());
                    break;
                case CENTER_TOP:
                    outPoint.set(getCenterX(),mMargins.y);
                    break;
                case CENTER_BOTTOM:
                    outPoint.set(getCenterX(),
                            parent.getHeight() - mView.getHeight() - mMargins.y);
                    break;
            }
            return outPoint;
        }

        public FrameLayoutAnim setCorner(Corner corner){

            mCorner = corner;
            return this;
        }

        public FrameLayoutAnim setMargins(int x, int y){
            mMargins.set(x,y);
            return this;
        }

        @Override
        public void onWindowAttached() {
        }

        @Override
        public void onWindowDetached() {
            mView.getViewTreeObserver().removeOnWindowAttachListener(this);
            mView = null;
        }

        Corner getRandomCorner(Corner cur){
            Corner corner = cur;
            int c = new Random().nextInt(Corner.values().length-2);
            for (int i = 0; i<=c; i++){
                int index = Utils.getNextPreviousIndex(true,corner.ordinal(),Corner.values().length);
                corner = Corner.values()[index];
            }
            return corner;
        }


        @Override
        protected Animation getNextAnimation(Animation prev) {
            Corner c = getRandomCorner(mCorner);
            MyLog.log("Anim from "+mCorner.name() +" to "+c.name());
            Point from = makeCoords(mCorner, null);
            Point to = makeCoords(c,null);
            mCorner = c;
            Animation a = new TranslateAnimation(from.x,to.x,from.y,to.y);
            a.setDuration(15000);
            a.setFillBefore(true);
            a.setFillAfter(true);
            return a;
        }
    }
}

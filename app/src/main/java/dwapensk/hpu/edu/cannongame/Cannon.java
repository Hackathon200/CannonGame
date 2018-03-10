package dwapensk.hpu.edu.cannongame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

/**
 * Created by obft1 on 2/23/2018.
 */

public class Cannon {
    private int mBaseRadius;
    private int mBarrelLength;
    private Point barrelEnd = new Point();
    private double barrelAngle;
    private Cannonball mCannonball;
    private Paint mPaint = new Paint();
    private CannonView mView;

    public Cannon(CannonView view, int baseRadius, int barrelLength, int barrelWidth) {
        this.mView = view;
        this.mBaseRadius = baseRadius;
        this.mBarrelLength = barrelLength;
        mPaint.setStrokeWidth(barrelWidth);
        mPaint.setColor(Color.BLACK);
        align(Math.PI/2);
    }

    public void align(double barrelAngle) {
        this.barrelAngle = barrelAngle;
        //Log.d("end", "x: " + (mBarrelLength * Math.cos(barrelAngle)) + ", y: " + (mBarrelLength * Math.sin(barrelAngle)));
        barrelEnd.x = (int) (mBarrelLength * Math.cos(barrelAngle)) + mView.getScreenWidth()/2;
        barrelEnd.y = (int) -(mBarrelLength * Math.sin(barrelAngle)) + mView.getScreenHeight()/2;
    }

    public void fireCannonball() { //p244
        int velocityX = (int) (CannonView.CANNONBALL_SPEED_PERCENT * mView.getScreenWidth() * Math.cos(barrelAngle));
        int velocityY = (int) (CannonView.CANNONBALL_SPEED_PERCENT * mView.getScreenWidth() * -Math.sin(barrelAngle));
        int radius = (int) (mView.getScreenHeight() * CannonView.CANNONBALL_RADIUS_PERCENT);
        mCannonball = new Cannonball(mView, Color.BLACK, CannonView.CANNON_SOUND_ID, barrelEnd.x,
                barrelEnd.y, radius, velocityX, velocityY);
        mCannonball.playSound();
    }

    public void draw(Canvas canvas) {
        canvas.drawLine(mView.getScreenWidth()/2, mView.getScreenHeight() / 2, barrelEnd.x, barrelEnd.y, mPaint);
        canvas.drawCircle(mView.getScreenWidth()/2, (int) mView.getScreenHeight() / 2, (int) mBaseRadius, mPaint);
    }

    public Cannonball getCannonball() {
        return mCannonball;
    }

    public void removeCannonball() {
        mCannonball = null;
    }

    public int getBarrelLength() {
        return mBarrelLength;
    }
}

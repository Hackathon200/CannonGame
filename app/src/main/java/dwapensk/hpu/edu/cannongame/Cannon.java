package dwapensk.hpu.edu.cannongame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

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
        barrelEnd.x = (int) (mBarrelLength * Math.sin(barrelAngle));
        barrelEnd.y = (int) (-mBarrelLength * Math.cos(barrelAngle)) + mView.getScreenHeight()/2;
    }

    public void fireCannonball() { //p244
        int velocityX = (int) (CannonView.CANNONBALL_SPEED_PERCENT * mView.getScreenWidth() * Math.sin(barrelAngle));
        int velocityY = (int) (CannonView.CANNONBALL_SPEED_PERCENT * mView.getScreenWidth() * -Math.cos(barrelAngle));
        int radius = (int) (mView.getScreenHeight() * CannonView.CANNONBALL_RADIUS_PERCENT);
        mCannonball = new Cannonball(mView, Color.BLACK, CannonView.CANNON_SOUND_ID, -radius, mView.getScreenHeight() / 2 - radius, radius, velocityX, velocityY);
        mCannonball.playSound();
    }

    public void draw(Canvas canvas) {
        canvas.drawLine(0, mView.getScreenHeight() / 2, barrelEnd.x, barrelEnd.y, mPaint);
        canvas.drawCircle(0, (int) mView.getScreenHeight() / 2, (int) mBaseRadius, mPaint);
    }

    public Cannonball getCannonball() {
        return mCannonball;
    }

    public void removeCannonball() {
        mCannonball = null;
    }
}

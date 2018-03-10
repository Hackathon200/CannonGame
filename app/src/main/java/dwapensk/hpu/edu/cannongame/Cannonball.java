package dwapensk.hpu.edu.cannongame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;

import java.io.File;

/**
 * Created by obft1 on 2/23/2018.
 */

public class Cannonball extends GameElement {
    private float velocityX;
    private boolean onScreen;

    public Cannonball(CannonView view, int color, int soundId, int x, int y, int radius, float velocityX, float velocityY) {
        super(view, color, soundId, x, y, 2*radius, 2*radius, velocityY);
        this.velocityX = velocityX;
        onScreen = true;
    }

    private int getRadius() {
        return (mShape.right - mShape.left) / 2;
    }

    public boolean collidesWith(GameElement element) {
        return (Rect.intersects(mShape, element.mShape));
    }

    public boolean isOnScreen() {
        return onScreen;
    }

    public void reverseVelocityX() {
        velocityX *= -1;
    }

    @Override
    public void update(double interval) {
        super.update(interval);
        mShape.offset((int) (velocityX * interval), 0);
        if (mShape.top < 0 || mShape.left < 0 || mShape.bottom > mView.getScreenHeight() || mShape.right > mView.getScreenWidth()) {
            onScreen = false;
        }
    }

    public void draw(Canvas canvas, Bitmap bitmap) {
        Bitmap b = Bitmap.createScaledBitmap(bitmap, getRadius()*2, getRadius()*2, true);
        canvas.drawBitmap(b, mShape.left + getRadius(), mShape.top + getRadius(), mPaint);
    }
}

package dwapensk.hpu.edu.cannongame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by obft1 on 2/23/2018.
 */

public class GameElement {
    protected CannonView mView;
    protected Paint mPaint = new Paint();
    protected Rect mShape;
    private float mVelocityY;
    private int mSoundId;

    public GameElement(CannonView mView, int color, int mSoundId, int x, int y, int width, int length, float mVelocityY) {
        this.mView = mView;
        mPaint.setColor(color);
        mShape = new Rect(x, y, x+width, y+length);
        this.mSoundId = mSoundId;
        this.mVelocityY = mVelocityY;
    }

    public void update(double interval) {
        mShape.offset(0, (int) (mVelocityY * interval));
        if (mShape.top < 0 && mVelocityY < 0 || mShape.bottom > mView.getScreenHeight() && mVelocityY > 0) {
            mVelocityY *= -1;
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(mShape, mPaint);
    }

    public void playSound() {
        mView.playSound(mSoundId);
    }
}


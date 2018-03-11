package dwapensk.hpu.edu.cannongame;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

import static android.content.ContentValues.TAG;

/**
 * Created by obft1 on 2/23/2018.
 */

public class CannonView extends SurfaceView implements SurfaceHolder.Callback {
    public static final int MISS_PENALTY = 2;
    public static final int HIT_REWARD = 3;
    public static final double CANNON_BASE_RADIUS_PERCENT = 3.0/40;
    public static final double CANNON_BARREL_WIDTH_PERCENT = 3.0/40;
    public static final double CANNON_BARREL_LENGTH_PERCENT = 1.0/10;
    public static final double CANNONBALL_RADIUS_PERCENT = 3.0/80;
    public static final double CANNONBALL_SPEED_PERCENT = 1.8/2;
    public static final double TARGET_WIDTH_PERCENT = 1.0/40;
    public static final double TARGET_LENGTH_PERCENT = 3.0/20;
    public static final double TARGET_FIRST_X_PERCENT = 3.0/5;
    public static final double TARGET_SPACING_PERCENT = 1.0/60;
    public static final double TARGET_PIECES = 9;
    public static final double TARGET_MIN_SPEED_PERCENT = 3.0/4;
    public static final double TARGET_MAX_SPEED_PERCENT = 6.0/4;
    public static final double BLOCKER_WIDTH_PERCENT = 1.0/40;
    public static final double BLOCKER_LENGTH_PERCENT = 1.0/4;
    public static final double BLOCKER_X_PERCENT = 1.0/2;
    public static final double BLOCKER_SPEED_PERCENT = 1.0;
    public static final double TEXT_SIZE_PERCENT = 1.0/18;

    private CannonThread mCannonThread;
    private SpawnNewTargetsThread mSpawnNewTargetsThread;
    private Activity mActivity;
    private boolean dialogIsDisplayed = false;
    private Cannon mCannon;
    private Blocker mBlocker;
    private ArrayList<Target> mTargets;
    private int mScreenWidth;
    private int mScreenHeight;
    private boolean mGameOver;
    private double mTimeLeft;
    private int mShotsFired;
    private double mTotalElapsedTime;
    private Random random = new Random();
    private int mNumTargets;

    public static final int TARGET_SOUND_ID = 0;
    public static final int CANNON_SOUND_ID = 1;
    public static final int BLOCKER_SOUND_ID = 2;
    private SoundPool mSoundPool;
    private SparseIntArray mSoundMap;
    private Paint mTextPaint;
    private Paint mBackgroundPaint;

    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (Activity) context;
        getHolder().addCallback(this);

        AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
        attrBuilder.setUsage(AudioAttributes.USAGE_GAME);

        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(1);
        builder.setAudioAttributes(attrBuilder.build());
        mSoundPool = builder.build();

        mSoundMap = new SparseIntArray(3);
        mSoundMap.put(TARGET_SOUND_ID, mSoundPool.load(context, R.raw.target_hit, 1));
        mSoundMap.put(CANNON_SOUND_ID, mSoundPool.load(context, R.raw.cannon_fire, 1));
        mSoundMap.put(BLOCKER_SOUND_ID, mSoundPool.load(context, R.raw.blocker_hit, 1));

        mTextPaint = new Paint();
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mScreenWidth = w;
        mScreenHeight = h;

        mTextPaint.setTextSize((int) (TEXT_SIZE_PERCENT * mScreenHeight));
        mTextPaint.setAntiAlias(true);
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }

    public void playSound(int soundId) {
        mSoundPool.play(mSoundMap.get(soundId), 1, 1, 1, 0 ,1f);
    }

    public void newGame() {
        mNumTargets = 2;
        mCannon = new Cannon(this, (int) (CANNON_BASE_RADIUS_PERCENT * mScreenHeight),
                (int) (CANNON_BARREL_LENGTH_PERCENT * mScreenWidth),
                (int) (CANNON_BARREL_WIDTH_PERCENT * mScreenHeight));

        mTargets = new ArrayList<>();
        spawnNewTarget();

        mBlocker = new Blocker(this, Color.BLACK, MISS_PENALTY,
                (int) (BLOCKER_X_PERCENT * mScreenWidth),
                (int) ((0.5-BLOCKER_LENGTH_PERCENT / 2) * mScreenHeight),
                (int) (BLOCKER_WIDTH_PERCENT * mScreenWidth),
                (int) (BLOCKER_LENGTH_PERCENT * mScreenHeight),
                (float) (BLOCKER_SPEED_PERCENT * mScreenHeight));

        mTimeLeft = 10;

        mShotsFired = 0;
        mTotalElapsedTime = 0.0;

        if (mGameOver) {
            mGameOver = false;
            mCannonThread = new CannonThread(getHolder());
            mCannonThread.start();
            mSpawnNewTargetsThread = new SpawnNewTargetsThread();
            mSpawnNewTargetsThread.start();
        }

        hideSystemBars();
    }

    private void spawnNewTarget() {
        int randX = random.nextInt(2);
        int randSide = random.nextInt(2);
        int targetX;
        int targetY;
        if (randX == 0 && randSide == 0) { //spawns on top and left
            targetX = random.nextInt(mScreenWidth / 2 - 230) + 10;
            targetY = 0;
        } else if (randX == 0 && randSide == 1) { //spawns on top and right
            targetX = random.nextInt(mScreenWidth / 2 - 230) + mScreenWidth/2 + 215;
            targetY = 0;
        } else if (randX == 1 && randSide == 0) { //spawns on bottom and left
            targetX = random.nextInt(mScreenWidth / 2 - 230) + 10;
            targetY = mScreenHeight;
        } else {
            targetX = random.nextInt(mScreenWidth / 2 - 230) + mScreenWidth/2 + 215;
            targetY = mScreenHeight;
        }

        double velocity = mScreenHeight * (random.nextDouble() *
                (TARGET_MAX_SPEED_PERCENT - TARGET_MIN_SPEED_PERCENT) + TARGET_MIN_SPEED_PERCENT);

        int color = random.nextInt(2);
        if (color == 0) {
            color = getResources().getColor(R.color.dark, getContext().getTheme());
        } else {
            color = getResources().getColor(R.color.light, getContext().getTheme());
        }

        velocity *= -1;

        mTargets.add(new Target(this, color, HIT_REWARD, targetX, targetY,
                (int) (TARGET_WIDTH_PERCENT * mScreenWidth),
                (int) (TARGET_LENGTH_PERCENT * mScreenHeight),
                (int) velocity));
    }

    private void updatePositions(double elapsedTimeMS) {
        double interval = elapsedTimeMS / 1000.0;

        if (mCannon.getCannonball() != null) {
            mCannon.getCannonball().update(interval);
        }

        mBlocker.update(interval);

        for (GameElement target : mTargets) {
            target.update(interval);
        }

        mTimeLeft -= interval;
        if (mTimeLeft <= 0) {
            mTimeLeft = 0.0;
            mGameOver = true;
            mCannonThread.setRunning(false);
            mSpawnNewTargetsThread.setThreadIsRunning(false);
            showGameOverDialog(R.string.lose);
        }
        /*if (mTargets.isEmpty()) {
            mCannonThread.setRunning(false);
            mSpawnNewTargetsThread.setThreadIsRunning(false);
            showGameOverDialog(R.string.win);
            mGameOver = true;
        }*/
    }

    public void alignAndFireCannonball(MotionEvent event) {
        Point touchPoint = new Point((int) event.getX(), (int) event.getY());
        double angle = 0;
        angle = Math.atan2(mScreenHeight/2-touchPoint.y,touchPoint.x-mScreenWidth/2);

        if (angle < 0) {
            angle += Math.PI*2;
        }
        mCannon.align(angle);

        if (mCannon.getCannonball() == null || !mCannon.getCannonball().isOnScreen()) {
            mCannon.fireCannonball();
            ++mShotsFired;
        }
    }

    private void showGameOverDialog(final int messageId) {
        final DialogFragment gameResult = new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle bundle) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(messageId));
                builder.setMessage(getResources().getString(R.string.results_format, mShotsFired, mTotalElapsedTime));
                builder.setPositiveButton(R.string.reset_game,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dialogIsDisplayed = false;
                                newGame();
                            }
                        });
                return builder.create();
            }
        };

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showSystemBars();
                dialogIsDisplayed = true;
                gameResult.setCancelable(false);
                gameResult.show(mActivity.getFragmentManager(), "results");
            }
        });
    }

    public void drawGameElements(Canvas canvas) {
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mBackgroundPaint);
        canvas.drawText(getResources().getString(R.string.time_remaining_format, mTimeLeft, mNumTargets-1), 50, 100, mTextPaint);
        mCannon.draw(canvas);

        if (mCannon.getCannonball() != null && mCannon.getCannonball().isOnScreen()) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cage);
            mCannon.getCannonball().draw(canvas, bitmap);
        }

        //mBlocker.draw(canvas);
        for (GameElement target : mTargets) {
            target.draw(canvas);
        }

    }

    public void testForCollisions() {
        if (mCannon.getCannonball() != null && mCannon.getCannonball().isOnScreen()) {
            for (int n = 0; n < mTargets.size(); n++) {
                if (mCannon.getCannonball().collidesWith(mTargets.get(n))) {
                    mTargets.get(n).playSound();
                    //mTimeLeft += mTargets.get(n).getHitReward();
                    mCannon.removeCannonball();
                    mTargets.remove(n);
                    --n;
                    break;
                }
            }
        } else {
            mCannon.removeCannonball();
        }

    }

    public void stopGame() {
        if (mCannonThread != null) {
            mCannonThread.setRunning(false);
        }
        if (mSpawnNewTargetsThread != null) {
            mSpawnNewTargetsThread.setThreadIsRunning(false);
        }
    }

    public void releaseResources() {
        mSoundPool.release();
        mSoundPool = null;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!dialogIsDisplayed) {
            newGame();
            mCannonThread = new CannonThread(holder);
            mCannonThread.setRunning(true);
            mCannonThread.start();
            mSpawnNewTargetsThread = new SpawnNewTargetsThread();
            mSpawnNewTargetsThread.setThreadIsRunning(true);
            mSpawnNewTargetsThread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        mCannonThread.setRunning(false);
        mSpawnNewTargetsThread.setThreadIsRunning(false);
        while (retry) {
            try {
                mCannonThread.join();
                mSpawnNewTargetsThread.join();
                retry = false;
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted", e);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            alignAndFireCannonball(event);
        }
        return true;
    }

    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE
            );
        }
    }

    private void showSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }

    private class CannonThread extends Thread {
        private SurfaceHolder mSurfaceHolder;
        private boolean mThreadIsRunning = true;

        public CannonThread(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            setName("CannonThread");
        }

        public void setRunning(boolean running) {
            mThreadIsRunning = running;
        }

        @Override
        public void run() {
            Canvas canvas = null;
            long previousFrameTime = System.currentTimeMillis();

            while(mThreadIsRunning) {
                try {
                    canvas = mSurfaceHolder.lockCanvas(null);

                    synchronized (mSurfaceHolder) {
                        long currentTime = System.currentTimeMillis();
                        double elapsedTimeMS = currentTime - previousFrameTime;
                        mTotalElapsedTime += elapsedTimeMS / 1000.0;
                        updatePositions(elapsedTimeMS);
                        testForCollisions();
                        drawGameElements(canvas);
                        previousFrameTime = currentTime;
                    }
                } finally {
                    if (canvas != null) {
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    private class SpawnNewTargetsThread extends Thread {
        private boolean mThreadIsRunning = true;

        public void setThreadIsRunning(boolean running) {
            mThreadIsRunning = running;
        }

        @Override
        public void run() {
            while (mThreadIsRunning) {
                try {
                    if (mTargets.isEmpty()) {
                        for (int i = 0; i < mNumTargets; i++) {
                            spawnNewTarget();
                            sleep(250);
                        }
                        mTimeLeft += 7;
                        mNumTargets++;
                    } else {
                        sleep(500);
                    }
                } catch (InterruptedException e) {
                    mTargets = new ArrayList<>();
                    spawnNewTarget();
                }
            }
        }
    }
}

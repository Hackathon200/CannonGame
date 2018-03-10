package dwapensk.hpu.edu.cannongame;

/**
 * Created by obft1 on 2/23/2018.
 */

public class Blocker extends GameElement {
    private int mMissPenalty;

    public Blocker(CannonView view, int color, int missPenalty, int x, int y, int width,
                   int length, float velocityY) {
        super(view, color, CannonView.BLOCKER_SOUND_ID, x, y, width, length, velocityY);
        this.mMissPenalty = missPenalty;
    }

    public int getMissPenalty() {
        return mMissPenalty;
    }
}

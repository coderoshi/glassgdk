package glass.partify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.MotionEvent;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardScrollView;

public class BalloonCountActivity
		extends Activity
		implements GestureDetector.BaseListener
{
    public static final String EXTRA_CURRENT_COUNT = "current_count";
	public static final String EXTRA_BALLOON_COUNT = "balloon_count";

	private AudioManager audioManager;
    private GestureDetector gestureDetector;
    private CardScrollView scrollView;
    private BalloonCountScrollAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        gestureDetector = new GestureDetector(this).setBaseListener(this);
        scrollView = new BalloonCountScrollView(this, gestureDetector);
        adapter = new BalloonCountScrollAdapter(this);
        scrollView.setAdapter(adapter);
        setContentView(scrollView);
    }

    @Override
    public void onResume() {
        super.onResume();
        scrollView.activate();
        scrollView.setSelection(getIntent().getIntExtra(EXTRA_CURRENT_COUNT, 3));
    }

    @Override
    public void onPause() {
        super.onPause();
        scrollView.deactivate();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return gestureDetector.onMotionEvent(event);
    }

	@Override
	public boolean onGesture(Gesture gesture) {
        if (gesture == Gesture.TAP) {
            // TODO: position, or image id/file name?
        	setResult(RESULT_OK, new Intent()
            	.putExtra(EXTRA_BALLOON_COUNT, scrollView.getSelectedItemPosition()) );
            audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
            finish();
            return true;
        }
		return false;
	}
}
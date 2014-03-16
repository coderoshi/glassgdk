package glass.partify;

import android.content.Context;
import android.view.MotionEvent;

import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardScrollView;

public class BalloonCountScrollView extends CardScrollView {
	// NOT a android.view.GestureDetector class
	private GestureDetector gestureDetector;

	public BalloonCountScrollView(Context context, GestureDetector gestureDetector) {
		super(context);
	}

    @Override
    public final boolean dispatchGenericFocusedEvent(MotionEvent event) {
        if (gestureDetector.onMotionEvent(event)) {
            return true;
        }
        return super.dispatchGenericFocusedEvent(event);
    }
}

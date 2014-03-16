package glass.partify;

import java.io.IOException;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.google.android.glass.timeline.TimelineManager;

public class PartyService extends Service {
	public final static String TAG = PartyService.class.getPackage().toString();

	private TimelineManager timelineManager;
	private LiveCard liveCard;
	private PartyDrawer drawer;
    private final GifferBinder binder = new GifferBinder();
    private int balloonCount = Balloon.DEFAULT_COUNT;

    /**
     * Binder giving access to this service.
     */
    public class GifferBinder extends Binder {
        public PartyService getService() {
            return PartyService.this;
        }
    }

    public void setImageFileName(String fileName) {
		Log.d(TAG, "setImageFileName " + fileName);
    	if( drawer != null ) {
    		Bitmap background = BitmapFactory.decodeFile(fileName);
    		drawer.setBackgroundImage(background);
    	}
    }

    public void setBalloonCount(int balloonCount) {
    	Log.d(TAG, "setBalloonCount " + balloonCount);
    	this.balloonCount = balloonCount;
    	if( drawer != null ) {
			try {
	    		drawer.reloadBalloons(balloonCount);
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
    	}
    }
    
    public int getBalloonCount() {
    	return balloonCount;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		timelineManager = TimelineManager.from(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (liveCard == null) {
			try {
				drawer = new PartyDrawer(this);
			} catch(IOException e) {
				throw new RuntimeException(e);
			}

			liveCard = timelineManager.createLiveCard(TAG);
			liveCard.setDirectRenderingEnabled(true)
					.getSurfaceHolder()
					.addCallback(drawer);
			liveCard.setAction(buildAction());
			liveCard.publish(PublishMode.REVEAL);
		}
		return START_STICKY;
	}

	private PendingIntent buildAction() {
		Intent menuIntent = new Intent(this, MenuActivity.class);
		menuIntent.addFlags(
				Intent.FLAG_ACTIVITY_NEW_TASK |
				Intent.FLAG_ACTIVITY_CLEAR_TASK);
		return PendingIntent.getActivity(this, 0, menuIntent, 0);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		if (liveCard != null && liveCard.isPublished()) {
			liveCard.getSurfaceHolder().removeCallback(drawer);
			liveCard.unpublish();
			// in case this isn't called by Android, kill the background thread
			drawer.surfaceDestroyed(liveCard.getSurfaceHolder());
			liveCard = null;
			drawer = null;
		}
		super.onDestroy();
	}
}

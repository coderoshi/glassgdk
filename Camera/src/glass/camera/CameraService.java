package glass.camera;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.google.android.glass.timeline.TimelineManager;

public class CameraService extends Service {
	public final static String TAG = CameraService.class.getName();

	private TimelineManager timelineManager;
	private LiveCard liveCard;
	private CameraDrawer cameraDrawer;

	@Override
	public void onCreate() {
		super.onCreate();
		timelineManager = TimelineManager.from(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if( liveCard == null ) {
		    liveCard = timelineManager.createLiveCard(TAG);
//		    liveCard.setViews( buildViews() );
		    cameraDrawer = new CameraDrawer();
		    liveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(cameraDrawer);
		    liveCard.setAction( buildAction() );
	        liveCard.publish(PublishMode.REVEAL);
		}

	    return START_STICKY;
	}

    @Override
    public void onDestroy() {
        if (liveCard != null && liveCard.isPublished()) {
        	liveCard.unpublish();
        	liveCard = null;
        }
        super.onDestroy();
    }

//	private RemoteViews buildViews() {
//		RemoteViews rv = new RemoteViews(this.getPackageName(), R.layout.stats);
//	    rv.setTextViewText(R.id.is_charging, "Charging");
//	    return rv;
//	}

	private PendingIntent buildAction() {
		Intent menuIntent = new Intent(this, MenuActivity.class);
		menuIntent.addFlags(
				Intent.FLAG_ACTIVITY_NEW_TASK |
				Intent.FLAG_ACTIVITY_CLEAR_TASK);
		return PendingIntent.getActivity(this, 0, menuIntent, 0);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}

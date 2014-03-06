package coderoshi.glass.qr;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.google.android.glass.timeline.TimelineManager;

/**
 * @author eric redmond
 * @twitter coderoshi
 */
public class QRCameraService extends Service {
	public final static String TAG = QRCameraService.class.getName();

	private TimelineManager timelineManager;
	private LiveCard liveCard;
	private QRCameraDrawer cameraDrawer;

	@Override
	public void onCreate() {
		super.onCreate();
		timelineManager = TimelineManager.from(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (liveCard == null) {
			cameraDrawer = new QRCameraDrawer(this);

			liveCard = timelineManager.createLiveCard(TAG);
			liveCard.setDirectRenderingEnabled(true).getSurfaceHolder()
					.addCallback(cameraDrawer);
			liveCard.setAction(buildAction());
			liveCard.publish(PublishMode.REVEAL);
		}

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		if(cameraDrawer != null) {
			cameraDrawer.releaseCamera();
		}
		if (liveCard != null && liveCard.isPublished()) {
			liveCard.getSurfaceHolder().removeCallback(cameraDrawer);
			liveCard.unpublish();
			liveCard = null;
			cameraDrawer = null;
		}
		super.onDestroy();
	}

	public TimelineManager getTimelineManager() {
		return timelineManager;
	}

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

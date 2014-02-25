package glass.stats;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.google.android.glass.timeline.TimelineManager;

public class StatsService extends Service {
	public final static String TAG = StatsService.class.getSimpleName();

	private TimelineManager timelineManager;
	private LiveCard liveCard;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "ON_CREATE");
		timelineManager = TimelineManager.from(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if( liveCard == null ) {
		    liveCard = timelineManager.createLiveCard(TAG);

		    liveCard.setViews( buildViews() );

	        Intent menuIntent = new Intent(this, MenuActivity.class);
	        menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
	        liveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

	        liveCard.publish(PublishMode.REVEAL);
		}
        
	    return START_STICKY;
	}

	private RemoteViews buildViews() {
		RemoteViews rv = new RemoteViews(this.getPackageName(), R.layout.stats);
	    rv.setTextViewText(R.id.is_charging, "Charging");
	    return rv;
	}

    @Override
    public void onDestroy() {
        if (liveCard != null && liveCard.isPublished()) {
        	liveCard.unpublish();
        	liveCard = null;
        }
        super.onDestroy();
    }

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}

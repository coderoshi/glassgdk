package glass.partify;

import java.io.File;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.glass.media.CameraManager;

public class MenuActivity extends Activity {

	public static final String TAG = GifferService.TAG;
    private static final int BALLOON_COUNT = 100;
    private static final int TAKE_PICTURE = 101;

    private FileObserver observer;
    private boolean takingPhoto;
    private boolean settingBalloonCount;
    private GifferService service;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            if (binder instanceof GifferService.GifferBinder) {
            	service = ((GifferService.GifferBinder) binder).getService();
                openOptionsMenu();
            }
            // No need to keep the service bound.
            unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, GifferService.class), serviceConnection, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop:
            	stopService(new Intent(this, GifferService.class));
                return true;
            case R.id.take_picture:
            	Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(captureImageIntent, TAKE_PICTURE);
                takingPhoto = true;
                return true;
            case R.id.balloon_count:
                Intent balloonCountIntent = new Intent(this, BalloonCountActivity.class);
                balloonCountIntent.putExtra(
                        BalloonCountActivity.EXTRA_CURRENT_COUNT,
                        service.getBalloonCount());
                startActivityForResult(balloonCountIntent, BALLOON_COUNT);
                settingBalloonCount = true;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        openOptionsMenu();
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        if (!takingPhoto && !settingBalloonCount) {
        	finish();
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "onActivityResult");
    	if (resultCode != RESULT_OK) {
    		return;
    	}
    	switch( requestCode ) {
    	case TAKE_PICTURE:
    		String thumbnailFilePath = data.getStringExtra(CameraManager.EXTRA_THUMBNAIL_FILE_PATH);

            final File pictureFile = new File(thumbnailFilePath);
            final String pictureFileName = pictureFile.getName();

            // set up a file observer to watch this directory on sd card
            // FileObserver is an Android hook into the Linux kernel's inotify change notification subsystem
            observer = new FileObserver(pictureFile.getParentFile().getAbsolutePath()) {
                @Override
                public void onEvent(int event, String file) {
                	Log.d(TAG, "File "+event +" [" + file + "] " + pictureFileName);
                    if(event == FileObserver.CLOSE_WRITE && file.equals(pictureFileName)) {
                    	Log.d(TAG, "Image file written " + file);
                    	service.setImageFileName(pictureFile.getAbsolutePath());
                    	stopWatching();
                    	MenuActivity.this.takingPhoto = false;
                    	MenuActivity.this.finish();
                    }
                }
            };
            // if we can decode the file, no need observe
            if(BitmapFactory.decodeFile(thumbnailFilePath) != null) {
            	service.setImageFileName(pictureFile.getAbsolutePath());
            	takingPhoto = false;
            	finish();
            	return;
            }
            observer.startWatching();

            takingPhoto = false;
    		return;
    	case BALLOON_COUNT:
        	int balloonCount = data.getIntExtra(BalloonCountActivity.EXTRA_BALLOON_COUNT, 0);
        	service.setBalloonCount(balloonCount);
        	settingBalloonCount = false;
        	finish();
        	return;
        default:
        	finish();
        }
    }

}

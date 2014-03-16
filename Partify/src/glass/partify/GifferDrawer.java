package glass.partify;

import java.io.IOException;
import java.util.Random;

import android.app.Service;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.android.glass.timeline.DirectRenderingCallback;

public class GifferDrawer implements DirectRenderingCallback {
	private static final String TAG = GifferService.TAG;
	private static final int FPS = 24;

	private Service service;
	private SurfaceHolder holder;
    private boolean isPlaying = false;
    private Bitmap backgroundBitmap;
    private Balloon[] balloons;

    private final Runnable updateResults = new Runnable() {
    	public void run() {
	        Canvas canvas;
	        try {
	            canvas = holder.lockCanvas();
	        } catch (Exception e) {
	            return;
	        }
	        if( canvas != null ) {
        		canvas.drawColor(Color.BLACK);
	        	if( backgroundBitmap != null ) {
	        		canvas.drawBitmap(backgroundBitmap, 0, 0, null);    	        		
	        	}
	        	for (int i = 0; i < balloons.length; i++) {
	        		Balloon b = balloons[i];
					if(b.getBitmap() != null) {
						canvas.drawBitmap(b.getBitmap(), b.nextLeft(), b.nextTop(), null);
					}
				}
	            holder.unlockCanvasAndPost(canvas);
	        }
    	}
    };
    private final Handler handler = new Handler();
    // TODO: shove this into updateResults? or needs to be a thread?
    private Thread thread = new Thread(new Runnable() {
		public void run() {
			Log.d(TAG, "startRun");
			while( true ) {
				if( isPlaying && balloons != null ) {
					// TODO: is it even necessary to make a handler queue?
//					handler.post(updateResults);
					handler.postDelayed(updateResults, 1000/FPS);
				}

				try {
					Thread.sleep(1000/FPS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	});

    public void setBackgroundImage(Bitmap background) {
    	this.backgroundBitmap = background;
    	Log.d(TAG, "backgroundBitmap " + (this.backgroundBitmap != null));
    	if(handler != null) {
    		handler.removeMessages(0);
    	}
	}

    public void reloadBalloons(int balloonCount)
    	throws IOException
    {
    	isPlaying = false;
    	if(handler != null) {
    		handler.removeMessages(0);
    	}
		balloons = new Balloon[balloonCount];
		for (int i = 0; i < balloonCount; i++) {
			String color = Balloon.Color.getRandom().toString();
			Random rand = new Random();
			double percentSize = rand.nextDouble();
			int left = rand.nextInt(640);
			balloons[i] = new Balloon(service, color, percentSize, left, 360);
		}
		isPlaying = true;
    }
    
	public GifferDrawer(Service service)
		throws IOException
	{
		this.service = service;
		reloadBalloons(Balloon.DEFAULT_COUNT);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated");
		this.holder = holder;
		isPlaying = true;
		thread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(TAG, "surfaceChanged");
	}

	// TODO: surfaceDestroyed isn't guaranteed.
	// This is called immediately before a surface is being destroyed. After returning from this call, you should no longer try to access this surface. If you have a rendering thread that directly accesses the surface, you must ensure that thread is no longer touching the Surface before returning from this function.
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed");
		this.holder = null;
		isPlaying = false;
		handler.removeCallbacks(updateResults);
		try {
			thread.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void renderingPaused(SurfaceHolder surfaceholder, boolean pause) {
		Log.d(TAG, "renderingPaused " + pause);
		if(pause) {
			isPlaying = false;
//			handler.removeCallbacks(updateResults);
//			try {
//				thread.join();
//			} catch(InterruptedException e) {
//				e.printStackTrace();
//			}
		} else {
			isPlaying = true;
//			handler.post(updateResults);
//			thread.run();
			// thread.start();
		}
	}
}

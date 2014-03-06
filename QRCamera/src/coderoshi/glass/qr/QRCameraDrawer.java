package coderoshi.glass.qr;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.google.android.glass.app.Card;
import com.google.android.glass.timeline.DirectRenderingCallback;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

/**
 * @author eric redmond
 * @twitter coderoshi
 */
public class QRCameraDrawer implements DirectRenderingCallback {
	private static final String TAG = QRCameraDrawer.class.getName();

	private Camera camera;
	private QRCameraService service;
	private MultiFormatReader multiFormatReader;

	public QRCameraDrawer(QRCameraService service) {
		this.service = service;
		multiFormatReader = new MultiFormatReader();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated");
		camera = openCamera(holder);
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed");
		releaseCamera();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(TAG, "surfaceChanged");
		if (camera != null) {
			camera.startPreview();
		}
	}

	@Override
	public void renderingPaused(SurfaceHolder holder, boolean paused) {
		// there's no need to keep the camera around
		if (paused) {
			Log.d(TAG, "renderingPaused PAUSED");
			releaseCamera();
		} else {
			Log.d(TAG, "renderingPaused UNPAUSED");
			camera = openCamera(holder);
			camera.startPreview();
		}
	}

	public Context getContext() {
		return service;
	}

	public Camera getCamera() {
		return camera;
	}
	
	private Camera openCamera(SurfaceHolder holder) {
		if(camera != null) return camera;
		Camera camera = Camera.open();
		try {
			// Glass camera patch
			Parameters params = camera.getParameters();
			params.setPreviewFpsRange(30000, 30000);
			final DisplayMetrics dm = 
					getContext().getResources().getDisplayMetrics();
			params.setPreviewSize(dm.widthPixels, dm.heightPixels); // 640, 360
			camera.setParameters(params);

			camera.setPreviewDisplay(holder);

			camera.setPreviewCallback(new Camera.PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					scan(data, dm.widthPixels, dm.heightPixels);
				}
			});

		} catch (IOException e) {
			camera.release();
			camera = null;
			Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		return camera;
	}

	public synchronized void releaseCamera() {
		if (camera != null) {
			try {
				camera.setPreviewDisplay(null);
			} catch(IOException e) {
			}
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	// TODO: calling for every frame is insane. 5x / sec should suffice
	private void scan(byte[] data, int width, int height) {
		Result result = null;
		PlanarYUVLuminanceSource luminanceSource =
			new PlanarYUVLuminanceSource(
				data, width, height, 0, 0, width, height, false);
		if (luminanceSource != null) {
			Log.d(QRCameraDrawer.class.getName(), "source");
			BinaryBitmap bitmap = 
					new BinaryBitmap(new HybridBinarizer(luminanceSource));
			try {
				result = multiFormatReader.decodeWithState(bitmap);
			} catch (ReaderException re) {
				// nothing found to decode
			} finally {
				multiFormatReader.reset();
			}
		}

		if (result != null) {
			Toast.makeText(getContext(), result.getText(), Toast.LENGTH_LONG)
				.show();

			service.getTimelineManager().insert(createCard(result));

			// TODO: take a photo of the QR code?
			// camera.takePicture(shutter, raw, jpeg)

			// stop this service, let the service release the camera

			getContext().stopService(new Intent(getContext(), QRCameraService.class));
		}
	}

	private Card createCard(Result result) {
		Card card = new Card(getContext())
			.setText(result.getText())
			.setFootnote(R.string.qr_code);
		return card;
	}
}

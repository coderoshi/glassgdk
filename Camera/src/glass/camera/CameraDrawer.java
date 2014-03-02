package glass.camera;

import java.io.IOException;
import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;

public class CameraDrawer implements SurfaceHolder.Callback {
    private Camera camera;

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
    	camera = Camera.open();

        try {
        	// Glass XE11 camera fix
			Parameters params = camera.getParameters();
			params.setPreviewFpsRange( 30000, 30000 );
			params.setPreviewSize( 640, 360 );
			camera.setParameters( params );

			camera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            camera.release();
            camera = null;
            // TODO: add more exception handling logic here?
        }
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//      // Now that the size is known, set up the camera parameters and begin the preview.
      Camera.Parameters parameters = camera.getParameters();

      // no size changes in Glass
      List<Size> sizes = parameters.getSupportedPreviewSizes();
      Size optimalSize = getOptimalPreviewSize(sizes, width, height);
      parameters.setPreviewSize(optimalSize.width, optimalSize.height);

      camera.setParameters(parameters);

      if (camera != null)
      {
//      	if (intentCallback != null)
//      	{
//      		camera.setPreviewCallbackWithBuffer(intentCallback);
//      		Camera.Size size = parameters.getPreviewSize();
//      		byte[] data = new byte[size.width*size.height*
//      		                       ImageFormat.getBitsPerPixel(parameters.getPreviewFormat())/8];
//      		camera.addCallbackBuffer(data);
//      	}
      	camera.startPreview();
      }
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		camera.stopPreview();
		camera.release();
		camera = null;
	}

  private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
      final double ASPECT_TOLERANCE = 0.05;
      double targetRatio = (double) w / h;
      if (sizes == null) return null;

      Size optimalSize = null;
      double minDiff = Double.MAX_VALUE;

      int targetHeight = h;

      // Try to find an size match aspect ratio and size
      for (Size size : sizes) {
          double ratio = (double) size.width / size.height;
          if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
          if (Math.abs(size.height - targetHeight) < minDiff) {
              optimalSize = size;
              minDiff = Math.abs(size.height - targetHeight);
          }
      }

      // Cannot find the one match the aspect ratio, ignore the requirement
      if (optimalSize == null) {
          minDiff = Double.MAX_VALUE;
          for (Size size : sizes) {
              if (Math.abs(size.height - targetHeight) < minDiff) {
                  optimalSize = size;
                  minDiff = Math.abs(size.height - targetHeight);
              }
          }
      }
      return optimalSize;
  }
}

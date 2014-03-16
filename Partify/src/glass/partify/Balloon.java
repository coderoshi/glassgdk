package glass.partify;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import android.app.Service;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Balloon
{
	public static final int DEFAULT_COUNT = 3;
	
	enum Color {
		GREEN("green"),
		YELLOW("yellow"),
		RED("red");

	    private final String color;

	    private Color(final String color) {
	        this.color = color;
	    }

	    @Override
	    public String toString() {
	        return color;
	    }
	    
	    public static Color getRandom() {
	    	Random rand = new Random();
	    	return Color.values()[rand.nextInt(3)];
	    }
	}
	
	private Bitmap bitmap;
	private int left;
	private int top;
	private int initialTop;
	private int endTop;
	private int speed;

	public Balloon(Service service, String color, double percentSize, int initialLeft, int initialTop)
		throws IOException
	{
		this.left = initialLeft;
		this.top = this.initialTop = initialTop;

		String fileName = color + "_balloon.png";
		InputStream stream = service.getAssets().open(fileName);
		Bitmap image = BitmapFactory.decodeStream(stream);
		int width = (int)(image.getWidth() * percentSize);
		int height = (int)(image.getHeight() * percentSize);
		bitmap = Bitmap.createScaledBitmap(image, width, height, true);

		this.endTop = -height;
		
		// give the balloon a speed related to size
		this.speed = (int)(10 * percentSize)+1;
	}

	public Bitmap getBitmap() {
		return bitmap.isRecycled() ? null : bitmap;
	}

	public int nextLeft() {
		return left;
	}

	public int nextTop() {
		// move up until it's entire height passes 0, then restart to initial
		if( (top -= speed) <= endTop ) {
			top = initialTop;
		}
		return top;
	}
}

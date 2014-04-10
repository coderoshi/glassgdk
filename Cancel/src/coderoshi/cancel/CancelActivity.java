package coderoshi.cancel;

import android.app.Activity;
import android.os.Bundle;

/**
 * Does nothing, just closes.
 * @author Eric Redmond
 * @twitter coderoshi
 */
public class CancelActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    finish();
  }
}

package glass.camera;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MenuActivity extends Activity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stats, menu);
//    	MenuItem stop = menu.add(R.id.stop);
//    	MenuUtils.setDescription(stop, R.string.stop);
//    	MenuUtils.setInitialMenuItem(menu, stop);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop:
                return stopService(new Intent(this, CameraService.class));
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
        finish();
    }
}

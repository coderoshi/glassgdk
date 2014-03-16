package glass.partify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.android.glass.widget.CardScrollAdapter;

public class BalloonCountScrollAdapter extends CardScrollAdapter {
	public final static String TAG = BalloonCountScrollAdapter.class.getPackage().toString();
	public final static int MAX_BALLOONS = 10;

    private final Context context;

    public BalloonCountScrollAdapter(Context context) {
    	this.context = context;
    }

    @Override
    public int getCount() {
        return MAX_BALLOONS;
    }

    @Override
    public Object getItem(int position) {
        return Integer.valueOf(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.balloon_count, parent);
        }

        final TextView view = (TextView) convertView.findViewById(R.id.value);
        view.setText(position + " balloons");

        return setItemOnCard(this, convertView);
    }

    @Override
    public int findIdPosition(Object id) {
        if (id instanceof Integer) {
            int idInt = (Integer) id;
            if (idInt >= 0 && idInt < getCount()) {
                return idInt;
            }
        }
        return AdapterView.INVALID_POSITION;
    }

    @Override
    public int findItemPosition(Object item) {
        return findIdPosition(item);
    }
}

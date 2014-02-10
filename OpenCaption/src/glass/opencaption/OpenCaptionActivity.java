package glass.opencaption;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;

import com.google.android.glass.app.Card;
import com.google.android.glass.timeline.TimelineManager;

public class OpenCaptionActivity extends Activity
{
    private static final String TAG = "open_caption";

	private TimelineManager timelineManager;
	private int currentSpeechRequest = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        timelineManager = TimelineManager.from(this);
        startSpeechPrompt(currentSpeechRequest);
	}

	private void startSpeechPrompt(int speechRequest) {
	    Intent intent = new Intent( RecognizerIntent.ACTION_RECOGNIZE_SPEECH );
	    intent.putExtra( RecognizerIntent.EXTRA_PROMPT, "Talk to me" );
	    startActivityForResult(intent, speechRequest);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		// VoiceTriggers.EXTRA_INPUT_SPEECH
		if(requestCode == currentSpeechRequest) {
			Log.d(TAG, "RESULT CODE: " + resultCode);
		    if ( resultCode == RESULT_OK) {
		        List<String> results = intent.getStringArrayListExtra( RecognizerIntent.EXTRA_RESULTS );
		        String spokenText = results.get(0);

		        // Create a card with spokenText
		        Card card = new Card(this)
		        	.setText(spokenText)
		        	.setFootnote("Note " + (currentSpeechRequest + 1));
		        timelineManager.insert(card);
	
		        stopService(intent);
		        
				// we have the activity result, run recognizer again,
		        // increment the next request
		        startSpeechPrompt( ++currentSpeechRequest );
		    } else if (resultCode == RESULT_CANCELED ) {
		    	// end this activity
		    	finish();
		    }
	    }
		super.onActivityResult(requestCode, resultCode, intent);
	}
}

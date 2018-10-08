package com.kiowok.jam2app;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity implements OnSharedPreferenceChangeListener {
	public static final String NUM_TO_PRESENT = "NUM_TO_PRESENT";
	
	Model model;
	Button [] button;
	SeekBar volumeBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        prefs.registerOnSharedPreferenceChangeListener(this);
        
        String sNumToPresent = prefs.getString(NUM_TO_PRESENT, "3");
        int numToPresent = Integer.parseInt(sNumToPresent);
		
		setupModel(numToPresent);
		setupButtonArray();
		refreshView();
		
		volumeBar = (SeekBar) findViewById(R.id.volumeBar);
		volumeBar.setProgress(Model.DEFAULT_VOLUME_PROGRESS);
		model.setVolume(Model.DEFAULT_VOLUME_PROGRESS);
		
		volumeBar.setOnSeekBarChangeListener(
				new OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar arg0, int vol,
							boolean userAdjusted) {
						Log.v("*** AJ ***", "Volume: " + vol + ", user adjusted:  " + userAdjusted);
						model.setVolume(vol);
					}

					@Override
					public void onStartTrackingTouch(SeekBar arg0) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onStopTrackingTouch(SeekBar arg0) {
						// TODO Auto-generated method stub
						
					}
					
				}
				);
		
		
		Log.v("*** AJ ***", "onCreate: " + model.toString());
	}
	
	private void refreshView() {
		Resources res = getResources();
		String [] labels = res.getStringArray(R.array.dlabels);
		
		for (int i = 0; i < button.length; i++)
			button[i].setText(labels[i]);
		
		refreshStatus();
	}
	
	private void setupButtonArray() {
		button = new Button[model.getNumTones()];
		button[0] = (Button) findViewById(R.id.btn0);
		button[1] = (Button) findViewById(R.id.btn1);
		button[2] = (Button) findViewById(R.id.btn2);
		button[3] = (Button) findViewById(R.id.btn3);
		button[4] = (Button) findViewById(R.id.btn4);
		button[5] = (Button) findViewById(R.id.btn5);
		button[6] = (Button) findViewById(R.id.btn6);
		button[7] = (Button) findViewById(R.id.btn7);	
		
		for (int i = 0; i < button.length; i++) {
			button[i].setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View btn) {
					Log.v("*** AJ ***", "onClick: " + model.toString());
					if (model.getState() == Model.GAME_STATE.PLAYING) {
				    	PorterDuffColorFilter greenFilter = 
				    		    new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
				    	
				    	PorterDuffColorFilter redFilter = 
				    		    new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
				    	
				    	int index = findIndex((Button) btn);
				    	
				    	if (index >= 0) {
				    		if (model.guessing(MainActivity.this, index))
				    			btn.getBackground().setColorFilter(greenFilter);
				    		else
				    			btn.getBackground().setColorFilter(redFilter);
				    	}
					}
					else if (model.getState() == Model.GAME_STATE.HELP) {
						int index = findIndex((Button) btn);
						//PorterDuffColorFilter greenFilter =
						//		new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
						//btn.getBackground().setColorFilter(greenFilter);
						model.presentTone(MainActivity.this, index);
					}
					
					refreshStatus();
				}
			});
			
		}
	}
	
	private int findIndex(Button btn) {
		int index = -1;
		for (int i = 0; i < button.length; i++)
			if (button[i] == btn)
				index = i;
		
		return index;
	}
	
	private void setupModel() {
		Resources res = getResources();
		
		model = new Model( 
				res.getStringArray(R.array.dtones),
				res.getStringArray(R.array.dlabels)
				);
	}
	
	private void setupModel(int numToPresent) {
		Resources res = getResources();
		
		model = new Model( 
				res.getStringArray(R.array.dtones),
				res.getStringArray(R.array.dlabels),
				numToPresent
				);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
        	case R.id.settings:
                Intent intent = new Intent(this, Jam2PreferenceActivity.class);
                startActivity(intent);
	            return true;	    
	        case R.id.reset:
	            model.setState("PLAYING");
	            model.resetScore();
	    		resetButtonBackgrounds();
	    		model.newGame(this);
	            refreshStatus();
	            return true;	            
	        case R.id.help:
                resetButtonBackgrounds();
                model.setState("HELP");
	        	/*
            	for (int i = 0; i < model.noteIds.length; i++) {
        			try {
        	    		MediaPlayer player = MediaPlayer.create(this, model.noteIds[i]);
        	    		player.setVolume(Model.normalizeVolume(model.getVolume()), Model.normalizeVolume(model.getVolume()));
        				player.start();
        				Thread.sleep(333);
        				player.stop();
        				player.release();
        			}
        			catch (Exception e) {
        				Log.v("*** AJ ***", "Error in introduction: " + e);
        			}
            	} */
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void newGame(View view) {
        model.setState("PLAYING");
		resetButtonBackgrounds();
		model.newGame(this);
		refreshStatus();
		
		Log.v("*** AJ ***", "newGame: " + model.toString());
	}
	
	public void retry(View view) {
		resetButtonBackgrounds();
		model.retry(this);
		refreshStatus();
		
		Log.v("*** AJ ***", "retry: " + model.toString());
	}
	
	private void resetButtonBackgrounds() {
    	for (Button btn : button)
    		btn.getBackground().setColorFilter(null);
	}
	
	public void refreshStatus() {
		TextView status = (TextView) findViewById(R.id.status);
		TextView status2 = (TextView) findViewById(R.id.status2);
		
		status.setText(showGuesses());
		
		Resources res = getResources();
		int errorColor = res.getColor(R.color.translucent_red);
		int correctColor = res.getColor(R.color.translucent_green);
		
		if (model.getCurrentlyGuessing() == 0) 
			status.setBackgroundColor(android.graphics.Color.WHITE);
		else if (model.allCorrectSoFar())
			status.setBackgroundColor(correctColor);
		else
			status.setBackgroundColor(errorColor);
		
		status2.setText(
				String.format("%d/%d", model.getWins(), model.getAttempts()));
	}
	
	private String showGuesses() {
		String s = " ";
		int i = 0;
		
		for (i = 0; i < model.getCurrentlyGuessing(); i++) {
			s += "* ";
		}
		
		while (i < model.getNumPresented()) {
			s += ("" + (i + 1) + " ");
			i++;
		}
		
		return s;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String arg1) {
        String sNumToPresent = prefs.getString(NUM_TO_PRESENT, "3");
        int numToPresent = Integer.parseInt(sNumToPresent);
		setupModel(numToPresent);
		resetButtonBackgrounds();
		refreshView();
	}
}

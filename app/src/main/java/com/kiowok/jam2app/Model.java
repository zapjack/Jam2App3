package com.kiowok.jam2app;

import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.Log;

public class Model {
	public static final int DEFAULT_NUM_PRESENTED = 3;
	public static final int DEFAULT_VOLUME_PROGRESS = 50;
	
	private String [] tones;
	private String [] labels;
	public int [] noteIds;   // hard-coded at the moment
	
	private int numPresented;
	
	private int [] presented;
	private int [] guessed;

	private int curGuessing;
	
	private int attempts, wins;
	
	// volume, length of notes
	
	public enum GAME_STATE {PLAYING, DONE, HELP}

	GAME_STATE state = GAME_STATE.PLAYING;
	
	private Random rand;
	
	private int volume;
	
	public void setVolume(int vol) {
		this.volume = vol;
	}
	
	public int getVolume () {
		return volume;
	}
	
	public void resetScore() {
		attempts = 0;
		wins = 0;
	}
	
	@Override
	public String toString() {
		return  reportArray(presented)+ ", " + reportArray(guessed) + ", guessing: " + curGuessing;
	}
	
	public boolean allCorrectSoFar() {
		boolean correct = true;
		for (int i = 0; i < this.curGuessing; i++) {
			if (presented[i] != guessed[i])
				correct = false;
		}
		
		return correct;
	}
	
	private String reportArray(int [] arr) {
		StringBuffer sb = new StringBuffer("[");
		for (Integer i : arr) {
			sb.append(", " + i);
		}
		
		sb.append("]");
		
		return sb.toString();
	}
	
	public Model (String [] tones, String [] labels) {
		this(tones, labels, DEFAULT_NUM_PRESENTED, new int [DEFAULT_NUM_PRESENTED], new int [DEFAULT_NUM_PRESENTED],
				0, 0, 0);
	}
	
	public Model (String [] tones, String [] labels, int numPresented) {
		this(tones, labels, numPresented, new int [numPresented], new int [numPresented],
				0, 0, 0);
	}
	
	public Model (String [] tones, String [] labels, int numPresented, int [] presented, int [] guessed, 
			int curGuessing, int attempts, int wins) {
		this.tones = tones;
		this.labels = labels;
		this.presented = presented;
		this.guessed = guessed;
		this.numPresented = numPresented;
		this.curGuessing = curGuessing;
		this.attempts = attempts;
		this.wins = wins;
		
		noteIds = new int[tones.length];
		noteIds[0] = R.raw.d4;
		noteIds[1] = R.raw.e4;
		noteIds[2] = R.raw.fsharp4;
		noteIds[3] = R.raw.g4;
		noteIds[4] = R.raw.a4;
		noteIds[5] = R.raw.b4;
		noteIds[6] = R.raw.csharp5;
		noteIds[7] = R.raw.d5;
		
		rand = new Random();
	}
	
	public GAME_STATE getState() {
		if (this.state == GAME_STATE.HELP)
			return GAME_STATE.HELP;

		if (curGuessing < numPresented) 
			return GAME_STATE.PLAYING;
		else
			return GAME_STATE.DONE;
	}

	public void setState(String state) {
		if (state.equals("HELP")) {
			this.state = GAME_STATE.HELP;
		}
		else if (state.equals("PLAYING")) {
			this.state = GAME_STATE.PLAYING;
		}
	}
	
	public int getNumTones() {
		return this.tones.length;
	}
	
	public void setNumPresented(int num) {
		numPresented = num;
	}
	
	public int getNumPresented() {
		return numPresented;
	}
	
	public int getCurrentlyGuessing() {
		return this.curGuessing;
	}
	
	public int getAttempts() {
		return this.attempts;
	}
	
	public int getWins() {
		return this.wins;
	}
	
	public static float normalizeVolume(int vol) {
		return (float) (vol / 100.0);
	}
	
	private void playReferenceTone(Context context) {
		Context c = context.getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
     
        boolean playIt = prefs.getBoolean("REFERENCE_TONE", false);
        
        if (playIt) {
			try {
	    		MediaPlayer player = MediaPlayer.create(context, noteIds[0]);
	    		player.setVolume(normalizeVolume(getVolume()), normalizeVolume(getVolume()));
				player.start();

				Thread.sleep(1000);

				player.stop();
				player.release();
				
				Thread.sleep(500);
			}
			catch (Exception e) {
				Log.v("*** AJ ***", "Error in newGame: " + e);
			}
        }
	}
	
	public void newGame(Context context) {
		playReferenceTone(context);
		this.attempts++;
		curGuessing = 0;
		
		for (int i = 0; i < numPresented; i++) {
			presented[i] = rand.nextInt(tones.length);
			
			try {
	    		MediaPlayer player = MediaPlayer.create(context, noteIds[presented[i]]);
	    		player.setVolume(normalizeVolume(getVolume()), normalizeVolume(getVolume()));
				player.start();

				Thread.sleep(1000);

				player.stop();
				player.release();
			}
			catch (Exception e) {
				Log.v("*** AJ ***", "Error in newGame: " + e);
			}
		}
	}
	
	public void retry(Context context) {
		playReferenceTone(context);
		
		this.attempts++;
		curGuessing = 0;
		
		for (int i = 0; i < numPresented; i++) {
			try {
	    		MediaPlayer player = MediaPlayer.create(context, noteIds[presented[i]]);
	    		player.setVolume(normalizeVolume(getVolume()), normalizeVolume(getVolume()));
				player.start();

				Thread.sleep(1000);

				player.stop();
				player.release();
			}
			catch (Exception e) {
				Log.v("*** AJ ***", "Error in retry: " + e);
			}
		}
		
		
	}
	
	/*
	 * The user has just made a guess.  Record the guess.  If this was the last guess possible, end one game and update the game statistics.
	 * Return true if the current was correct, return false otherwise.
	 */
	
	public boolean guessing(Context context, int noteIndex) {
		boolean correct = false;

		guessed[curGuessing] = noteIndex;

		if (guessed[curGuessing] == presented[curGuessing])
			correct = true;

		try {
			MediaPlayer player = MediaPlayer.create(context, noteIds[guessed[curGuessing]]);
			player.setVolume(normalizeVolume(getVolume()), normalizeVolume(getVolume()));
			player.start();
			Thread.sleep(1000);
			player.stop();
			player.release();
		}
		catch (Exception e) {
			Log.v("*** AJ ***", "Error in introduction: " + e);
		}

		curGuessing++;

		if (getState() == GAME_STATE.DONE)
			if (correctSoFar())
				wins++;

		return correct;
	}

	public void presentTone(Context context, int noteIndex) {
		try {
			MediaPlayer player = MediaPlayer.create(context, noteIds[noteIndex]);
			player.setVolume(normalizeVolume(getVolume()), normalizeVolume(getVolume()));
			player.start();
			Thread.sleep(1000);
			player.stop();
			player.release();
		}
		catch (Exception e) {
			Log.v("*** AJ ***", "Error in introduction: " + e);
		}
	}
	
	private boolean correctSoFar() {
		boolean allCorrect = true;
		
		for (int i = 0; i < curGuessing; i++)
			if (guessed[i] != presented[i])
				allCorrect = false;
		
		return allCorrect;
	}
}

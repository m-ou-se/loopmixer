package nl.tudelft.ti1100a.audio;

/**
 * A class implementing Rhythm is something with a rhythm. The methods allow you to use this rhythm in your application.
 * 
 * @author Mara Bos (m-ou.se@m-ou.se)
 * @see RhythmListener
 */
public interface Rhythm {
	
	/**
	 * Add a {@link RhythmListener} to this Rhythm.
	 * 
	 * @param l The {@link RhythmListener} that will be listening.
	 * @see RhythmListener
	 */
	public void addRhythmListener(RhythmListener l);
	
	/**
	 * Remove a {@link RhythmListener} from this Beat.
	 * 
	 * @param l The {@link RhythmListener} to remove.
	 * @see RhythmListener
	 */
	public void removeRhythmListener(RhythmListener l);
	
	/**
	 * Returns the duration of one measure, in seconds.
	 */
	public float getMeasureDuration();
	
	/**
	 * Returns the time, in seconds, since the start of the current measure.
	 */
	public float getMeasurePosition();
	
	/**
	 * Returns the number of samples in one measure.
	 */
	public int getExactMeasureDuration();
	
	/**
	 * Returns the number of samples since the start of the current measure.
	 */
	public int getExactMeasurePosition();
	
	/**
	 * Returns the duration of one beat, in seconds.
	 */
	public float getBeatDuration();
	
	/**
	 * Returns the time, in seconds, since the start of the current beat.
	 */
	public float getBeatPosition();
	
	/**
	 * Returns the number of beats per measure.
	 */
	public int getBeatsPerMeasure();
	
	/**
	 * Returns the current tempo of the rhythm, in beats per minute.
	 */
	public float getBpm();
	
	/**
	 * Returns the sample rate.
	 */
	public float sampleRate();

	/**
	 * Returns whether the rhythm is currently playing or not.
	 */
	public boolean isPlaying();

}

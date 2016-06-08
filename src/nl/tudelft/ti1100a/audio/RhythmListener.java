package nl.tudelft.ti1100a.audio;

/**
 * A RhythmListener can 'listen' to a {@link Rhythm}. The {@link Rhythm} will let the listner know when a measure or a beat starts.
 * 
 * @author Mara Bos (m-ou.se@m-ou.se)
 * @see Rhythm
 */
public interface RhythmListener {

	/**
	 * This method is called at the start of a measure.
	 */
	public void measure();
	
	/**
	 * This method is called on every beat, except for the beat that starts a measure.
	 */
	public void beat();
	
	/**
	 * This method is called when the tempo changes.
	 * 
	 * @param factor The relative change in tempo. 
	 */
	public void tempoChanged(float factor);
	
	/**
	 * This method is called when the position in the current measure changes in a unusual way.
	 * 
	 * For example, when the current measure is reset.
	 */
	public void positionChanged();
	
	/**
	 * This method is called when the Rhythm starts or stops playing.
	 */
	public void isPlayingChanged();
}

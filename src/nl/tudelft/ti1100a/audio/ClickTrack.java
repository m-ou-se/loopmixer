package nl.tudelft.ti1100a.audio;

import java.util.ArrayList;
import java.util.List;

import ddf.minim.AudioOutput;
import ddf.minim.AudioSignal;
import ddf.minim.Minim;

/**
 * A ClickTrack is a simple implementation of {@link Rhythm} to be used with Minim.
 * 
 * @author Mara Bos (m-ou.se@m-ou.se)
 */
public class ClickTrack implements Rhythm {
	
	List<RhythmListener> rhythmListeners;
	
	int position;
	int duration;
	int beats;
	final float sampleRate;
	boolean muted;
	boolean running;
	
	/**
	 * The Minim interface used by this ClickTrack.
	 */
	public final Minim minim;
	
	private final AudioOutput out;
	
	/**
	 * Creates a new ClickTrack.
	 * 
	 * The sample rate will be set to the default value of 44100.
	 * 
	 * @param minim The Minim interface to use.
	 * @param bpm The (initial) tempo, in beats per minute.
	 * @param beatsPerMeasure The number of beats per measure.
	 */
	public ClickTrack(Minim minim, float bpm, int beatsPerMeasure) {
		this(minim, 44100, (int) (60/(bpm/beatsPerMeasure) * 44100), beatsPerMeasure);
	}
	
	/**
	 * Creates a new ClickTrack.
	 * 
	 * @param minim The Minim interface to use.
	 * @param sampleRate The sample rate.
	 * @param measureDuration The duration, in samples, of one measure.
	 * @param beatsPerMeasure The number of beats per measure.
	 */
	public ClickTrack(Minim minim, int sampleRate, int measureDuration, int beatsPerMeasure) {
		this.minim = minim;
		this.duration = measureDuration;
		this.beats = beatsPerMeasure;
		this.position = 0;
		this.sampleRate = sampleRate;
		this.muted = false;
		this.running = false;
		
		rhythmListeners = new ArrayList<RhythmListener>();
		
		out = minim.getLineOut(Minim.MONO, sampleRate / 45, sampleRate);
		
		out.addSignal(new AudioSignal() {
			@Override public void generate(float[] data) {
				if (!running){
					for(int i = 0; i < data.length; i++) data[i] = 0;
					return;
				}
				for(int i = 0; i < data.length; i++){
					if (position == 0){
						for(RhythmListener l : rhythmListeners) l.measure();
					} else if (position % (duration/beats) == 0){
						for(RhythmListener l : rhythmListeners) l.beat();
					}
					data[i] = 0.0f;
					if (!muted) {
						if (position < ClickTrack.this.sampleRate / 4500){
							data[i] = 1.0f;
						} else {
							for(int j = 1; j < getBeatsPerMeasure(); j++){
								int clickpos = j * duration/beats;
								if (position >= clickpos && position < clickpos + ClickTrack.this.sampleRate / 4500){
									data[i] = 0.2f;
									break;
								}
							}
						}
					}
					position++;
					position %= duration;
				}
			}
			@Override public void generate(float[] left, float[] right) {
				// AudioOutput out is set to mono, so it'll never call this method.
				assert(false);
			}
		});
	}
	
	/** {@inheritDoc} */
	@Override public void addRhythmListener(RhythmListener l) {
		rhythmListeners.add(l);
	}

	/** {@inheritDoc} */
	@Override public void removeRhythmListener(RhythmListener l) {
		rhythmListeners.remove(l);
	}

	/** {@inheritDoc} */
	@Override public int getBeatsPerMeasure() {
		return beats;
	}

	/** {@inheritDoc} */
	@Override public float getBpm() {
		return 60 / getBeatDuration();
	}

	/** {@inheritDoc} */
	@Override public float getMeasureDuration() {
		return duration / sampleRate;
	}

	/** {@inheritDoc} */
	@Override public float getMeasurePosition() {
		return position / sampleRate;
	}

	/** {@inheritDoc} */
	@Override public float getBeatDuration() {
		return duration / beats / sampleRate;
	}

	/** {@inheritDoc} */
	@Override public float getBeatPosition() {
		return (position % (duration / beats)) / sampleRate;
	}
	
	/** {@inheritDoc} */
	@Override public int getExactMeasureDuration() {
		return duration;
	}
	
	/** {@inheritDoc} */
	@Override public int getExactMeasurePosition() {
		return position;
	}
	
	/**
	 * Changes the tempo.
	 * 
	 * @param bpm The new tempo, in beats per minute.
	 */
	public void setBpm(float bpm) {
		setMeasureDuration(60/bpm * beats);
	}
	
	/**
	 * Changes the tempo.
	 * 
	 * @param duration The new duration, in seconds, of one measure.
	 */
	public void setMeasureDuration(float duration) {
		setExactMeasureDuration((int) (sampleRate * duration));
	}
	
	/**
	 * Changes the tempo.
	 * 
	 * @param duration The new duration, in samples, of one measure.
	 */
	public void setExactMeasureDuration(int duration) {
		float factor = this.duration/(float)duration;
		this.duration = duration;
		this.position /= factor;
		for(RhythmListener l : rhythmListeners) l.tempoChanged(factor);
	}
	
	/**
	 * Changes the tempo.
	 * 
	 * @param factor The factor by which the tempo will be changed.
	 */
	public void changeTempo(float factor) {
		setExactMeasureDuration((int) (duration / factor));
	}

	/** {@inheritDoc} */
	@Override public float sampleRate() {
		return sampleRate;
	}
	
	/**
	 * Starts the ClickTrack.
	 * 
	 * When paused, start() will continue where the ClickTrack was left.
	 */
	public void start() {
		running = true;
	}
	
	/**
	 * Pauses the ClickTrack.
	 */
	public void pause() {
		running = false;
	}
	
	/**
	 * Stops the ClickTrack.
	 */
	public void stop() {
		position = 0;
		running = false;
		for(RhythmListener l : rhythmListeners) l.positionChanged();
	}
	
	/**
	 * Starts the ClickTrack from the beginning.
	 */
	public void restart() {
		position = 0;
		running = true;
		for(RhythmListener l : rhythmListeners) l.positionChanged();
	}
	
	/**
	 * Resets the current measure.
	 */
	public void rewind() {
		position = 0;
		for(RhythmListener l : rhythmListeners) l.positionChanged();
	}
	
	/** {@inheritDoc} */
	@Override public boolean isPlaying() {
		return running;
	}
	
	/**
	 * Returns whether the ClickTrack is muted or not.
	 */
	public boolean isMuted() {
		return muted;
	}
	
	/**
	 * Stops the ClickTrack from producing hearable clicks.
	 */
	public void mute() {
		muted = true;
	}
	
	/**
	 * Allows the ClickTrack to make hearable clicks.
	 */
	public void unmute() {
		muted = false;
	}
	
	/**
	 * Closes all used resources of the ClickTrack.
	 * 
	 * Do not use the ClickTrack after it's closed.
	 */
	public void close() {
		out.close();
	}

}

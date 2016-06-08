package nl.tudelft.ti1100a.audio;


import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;

import ddf.minim.*;

/**
 * A LoopMixer allows you to mix sound loops together easily.
 * 
 * @author Mara Bos (m-ou.se@m-ou.se)
 */
public class LoopMixer implements Recordable, Effectable, Polyphonic {
	
	private AudioOutput out;
	
	protected List<Loop> loops;
	
	/**
	 * The Minim interface used by this LoopMixer.
	 */
	public final Minim minim;
	
	/**
	 * The {@link Rhythm} of this LoopMixer.
	 */
	public final Rhythm rhythm;
	
	/**
	 * Creates a new LoopMixer.
	 * 
	 * @param minim The Minim interface to use.
	 * @param rhythm The Rhythm to use.
	 */
	public LoopMixer(Minim minim, Rhythm rhythm) {
		this.minim = minim;
		this.rhythm = rhythm;
		loops = new ArrayList<Loop>();
		out = minim.getLineOut(Minim.STEREO, 1024, rhythm.sampleRate());
	}

	/**
	 * Closes all used resources.
	 * 
	 * Do not use the LoopMixer after it's closed.
	 */
	void close() {
		for(Loop l : loops) l.close();
		loops.clear();
	}

	/**
	 * A loop in a {@link LoopMixer}.
	 * 
	 * To create a new Loop, use {@code loopMixer.new Loop(...)}.
	 * 
	 * @author Mara Bos (m-ou.se@m-ou.se)
	 */
	public class Loop extends AbstractEffectable implements AudioSignal, Effectable, Recordable {
		
		private List<AudioListener> listeners;
		private float[] samplesLeft;
		private float[] samplesRight;
		private int position;
		private int duration;
		private int offset;
		private float volumeLeft;
		private float volumeRight;
		private boolean playing;
		private float measures;
		private RhythmListener rhythmListener;
		
		/**
		 * Creates a new Loop.
		 * 
		 * @param filename The filename of the audio fragment to use.
		 * @param beats The number of beats in that audio fragment.
		 */
		public Loop(String filename, float beats) {
			this(filename, beats, 0);
		}
		
		/**
		 * Creates a new Loop.
		 * 
		 * @param filename The filename of the audio fragment to use.
		 * @param measuresPerLoop The number of measures in that audio fragment.
		 * @param startPosition The position in the audio fragment, in seconds from the beginning, where the first measure begins.
		 */
		public Loop(String filename, float measuresPerLoop, float startPosition) {
			AudioSample audio = minim.loadSample(filename);
			
			if (audio.getFormat().getChannels() >= 2){
				samplesLeft = audio.getChannel(1);
				samplesRight = audio.getChannel(2);
			} else {
				samplesLeft = audio.getChannel(1);
				samplesRight = audio.getChannel(1);
			}

			position = 0;
			volumeLeft = volumeRight = 1.0f;
			playing = false;
			measures = measuresPerLoop;
			duration = (int) (measures * rhythm.getExactMeasureDuration());		
			offset = (int) ((startPosition / (audio.length()/1000.0f)) * duration);
			
			listeners = new ArrayList<AudioListener>();
			
			rhythmListener = new RhythmListenerAdapter() {
				@Override public void tempoChanged(float factor) {
					syncDuration();
				}
				@Override public void positionChanged() {
					syncPosition();
				}
			};
			
			LoopMixer.this.addSignal(this);
			
			rhythm.addRhythmListener(rhythmListener);
			
			loops.add(this);
		}
		
		/** {@inheritDoc} */
		@Override public void generate(float[] data) {
			if (!playing || !rhythm.isPlaying()){
				for(int i = 0; i < data.length; i++) data[i] = 0.0f;
			} else {
				float[] left = data;
				float[] right = new float[data.length];
				generateSignal(left,right);
				for(int i = 0; i < data.length; i++) data[i] = (left[i] + right[i]) / 2.0f;
				process(data);
			}
			for(AudioListener l : listeners) l.samples(data);
		}
		
		/** {@inheritDoc} */
		@Override public void generate(float[] left, float[] right) {
			if (!playing || !rhythm.isPlaying()){
				for(int i = 0; i < left.length; i++) left[i] = right[i] = 0.0f;
			} else {
				generateSignal(left,right);
				process(left,right);
			}
			for(AudioListener l : listeners) l.samples(left,right);
		}
		
		private void generateSignal(float[] left, float[] right){
			for(int i = 0; i < left.length; i++){
				int index = ((int) ((samplesLeft.length * ((position+offset)/(float)duration)))) % samplesLeft.length;
				left[i]  = samplesLeft[index] * volumeLeft;
				right[i] = samplesRight[index] * volumeRight;
				position++;
				position %= duration;
			}
		}
		
		/**
		 * Starts the loop.
		 * 
		 * The loop will start on the right place to let its first beat match the closest measure beat in the {@link Rhythm} of the {@link LoopMixer}.
		 * 
		 * If the loop is already playing, nothing happens.
		 */
		public void start() {
			if (!playing) restart();
		}
		
		/**
		 * Starts the loop in the specified measure.
		 * 
		 * The loop will start on the right place in the specified measure to match the {@link Rhythm} of the {@link LoopMixer}.
		 * Negative numbers can be used too. For example, -1 specifies the last measure.
		 * 
		 * If the loop is already playing, nothing happens.
		 * 
		 * @param measure The measure to start in.
		 */
		public void start(int measure){
			if (!playing) restart(measure);
		}
		
		/**
		 * (Re)starts the loop.
		 * 
		 * The loop will start on the right place to let its first beat match the closest measure beat in the {@link Rhythm} of the {@link LoopMixer}.
		 */
		public void restart(){
			restart(rhythm.getExactMeasurePosition() >= (rhythm.getExactMeasureDuration() / 2) ? -1 : 0);
		}
		
		/**
		 * (Re)starts the loop in the specified measure.
		 * 
		 * The loop will (re)start on the right place in the specified measure to match the {@link Rhythm} of the {@link LoopMixer}.
		 * Negative numbers can be used too. For example, -1 specifies the last measure.
		 * 
		 * @param measure The measure to start in.
		 */
		public void restart(int measure){
			int newPosition = measure * rhythm.getExactMeasureDuration();
			newPosition %= duration;
			if (newPosition < 0) newPosition += duration;
			int newMeasure = newPosition / rhythm.getExactMeasureDuration();
			if (!playing || measure != newMeasure){
				position = newPosition;
				syncPosition();
			}
			playing = true;
		}
		
		/**
		 * Stops the loop.
		 */
		public void stop() {
			playing = false;
		}
		
		/**
		 * Returns whether the loop is currently playing or not.
		 */
		public boolean isPlaying() {
			return playing;
		}

		/**
		 * Returns the number of measures per loop.
		 */
		public float getNumberOfMeasures() {
			return measures;
		}
		
		/**
		 * Returns which measure of the loop is currently playing.
		 */
		public int getCurrentMeasure() {
			return position / rhythm.getExactMeasureDuration();
		}
		
		/**
		 * Changes the speed.
		 * 
		 * @param measures The number of measures one loop will take.
		 */
		public void setNumberOfMeasures(float measures) {
			this.measures = measures;
			syncDuration();
			syncPosition();
		}

		/**
		 * Changes the volume of both the left and the right channel.
		 * 
		 * @param volume The new volume, as a factor of the original (ie. 1 for 100%).
		 */
		public void setVolume(float volume) {
			volumeLeft = volumeRight = volume < 0 ? 0 : volume;
		}

		/**
		 * Returns the current volume. (The average of the left and right volume.)
		 * @see #getVolumeLeft()
		 * @see #getVolumeRight()
		 */
		public float getVolume() {
			return (volumeLeft + volumeRight) / 2.0f;
		}

		/**
		 * Changes the volume of the left channel.
		 * 
		 * @param volume The new volume, as a factor of the original (ie. 1 for 100%).
		 */
		public void setVolumeLeft(float volume) {
			volumeLeft = volume < 0 ? 0 : volume;
		}

		/**
		 * Returns the current volume of the left channel, as a factor of the original (ie. 1 for 100%).
		 */
		public float getVolumeLeft() {
			return volumeLeft;
		}

		/**
		 * Changes the volume of the right channel.
		 * 
		 * @param volume The new volume, as a factor of the original (ie. 1 for 100%).
		 */
		public void setVolumeRight(float volume) {
			volumeRight = volume < 0 ? 0 : volume;
		}

		/**
		 * Returns the current volume of the right channel, as a factor of the original (ie. 1 for 100%).
		 */
		public float getVolumeRight() {
			return volumeRight;
		}
		
		/**
		 * Returns the duration of one loop, in samples. 
		 */
		public int getExactDuration() {
			return duration;
		}
		
		/**
		 * Returns the duration of one loop, in seconds. 
		 */
		public float getDuration() {
			return duration / sampleRate();
		}
		
		/**
		 * Returns the current position in the loop, in samples. 
		 */
		public int getExactPosition() {
			return position;
		}
		
		/**
		 * Returns the current position in the loop, in seconds. 
		 */
		public float getPosition() {
			return position / sampleRate();
		}
		
		/**
		 * Returns the offset of the loop, in seconds.
		 */
		public float getOffset(){
			return offset / sampleRate();
		}
		
		/**
		 * Returns the offset of the loop, in samples.
		 */
		public int getExactOffset(){
			return offset;
		}
		
		/**
		 * Changes the offset.
		 * 
		 * @param newOffset The new offset, in seconds.
		 */
		public void setOffset(float newOffset){
			setExactOffset((int) (newOffset * sampleRate()));
		}
		
		/**
		 * Changes the offset.
		 * 
		 * @param newOffset The new offset, in samples.
		 */
		public void setExactOffset(int newOffset){
			newOffset %= duration;
			if (newOffset < 0) newOffset += duration;
			offset = newOffset;
		}
		
		/**
		 * Changes the speed.
		 * 
		 * @param factor The factor by which the speed will be changed.
		 */
		public void changeSpeed(float factor){
			setNumberOfMeasures(this.measures / factor);
		}
		
		/**
		 * Remove the Loop from the {@link LoopMixer}.
		 * 
		 * Do not use the loop after it's removed.
		 */
		public void remove(){
			close();
			loops.remove(this);
		}
		
		protected void close(){
			LoopMixer.this.removeSignal(this);
			rhythm.removeRhythmListener(rhythmListener);
		}
		
		protected void syncDuration(){
			int newDuration = (int) (measures * rhythm.getExactMeasureDuration());
			if (newDuration != duration){
				float factor = this.duration / (float)newDuration;
				duration = newDuration;
				position /= factor;
				offset /= factor;
			}
		}
		
		protected void syncPosition(){
			position += -(position % rhythm.getExactMeasureDuration()) +(rhythm.getExactMeasurePosition() % duration); 
		}

		@Override public void addListener(AudioListener l)    { listeners.add(l);    }
		@Override public void removeListener(AudioListener l) { listeners.remove(l); }
		
		@Override public int bufferSize()        { return LoopMixer.this.bufferSize(); }
		@Override public AudioFormat getFormat() { return LoopMixer.this.getFormat();  }
		@Override public int type()              { return LoopMixer.this.type();       }
		@Override public float sampleRate()      { return LoopMixer.this.sampleRate(); }
		
	}
	
	// Effectable
	/** {@inheritDoc} */ @Override public void addEffect(AudioEffect e)     {        out.addEffect(e);     }
	/** {@inheritDoc} */ @Override public void clearEffects()               {        out.clearEffects();   }
	/** {@inheritDoc} */ @Override public void disableEffect(int e)         {        out.disableEffect(e); }
	/** {@inheritDoc} */ @Override public void disableEffect(AudioEffect e) {        out.disableEffect(e); }
	/** {@inheritDoc} */ @Override public int effectCount()                 { return out.effectCount();    }
	/** {@inheritDoc} */ @Override public void effects()                    {        out.effects();        }
	/** {@inheritDoc} */ @Override public void enableEffect(int e)          {        out.enableEffect(e);  }
	/** {@inheritDoc} */ @Override public void enableEffect(AudioEffect e)  {        out.enableEffect(e);  }
	/** {@inheritDoc} */ @Override public AudioEffect getEffect(int e)      { return out.getEffect(e);     }
	/** {@inheritDoc} */ @Override public boolean hasEffect(AudioEffect e)  { return out.hasEffect(e);     }
	/** {@inheritDoc} */ @Override public boolean isEffected()              { return out.isEffected();     }
	/** {@inheritDoc} */ @Override public boolean isEnabled(AudioEffect e)  { return out.isEnabled(e);     }
	/** {@inheritDoc} */ @Override public void noEffects()                  {        out.noEffects();      }
	/** {@inheritDoc} */ @Override public void removeEffect(AudioEffect e)  {        out.removeEffect(e);  }
	/** {@inheritDoc} */ @Override public AudioEffect removeEffect(int e)   { return out.removeEffect(e);  }
	
	// Recordable
	/** {@inheritDoc} */ @Override public void addListener(AudioListener l)    {        out.addListener(l);    }
	/** {@inheritDoc} */ @Override public void removeListener(AudioListener l) {        out.removeListener(l); }
	/** {@inheritDoc} */ @Override public AudioFormat getFormat()              { return out.getFormat();       }
	/** {@inheritDoc} */ @Override public int bufferSize()                     { return out.bufferSize();      }
	/** {@inheritDoc} */ @Override public float sampleRate()                   { return out.sampleRate();      }
	/** {@inheritDoc} */ @Override public int type()                           { return out.type();            }

	// Polyphonic
	/** {@inheritDoc} */ @Override public void addSignal(AudioSignal s)     {        out.addSignal(s);     }
	/** {@inheritDoc} */ @Override public void clearSignals()               {        out.clearSignals();   }
	/** {@inheritDoc} */ @Override public void disableSignal(int s)         {        out.disableSignal(s); }
	/** {@inheritDoc} */ @Override public void disableSignal(AudioSignal s) {        out.disableSignal(s); }
	/** {@inheritDoc} */ @Override public void enableSignal(int s)          {        out.enableSignal(s);  }
	/** {@inheritDoc} */ @Override public void enableSignal(AudioSignal s)  {        out.enableSignal(s);  }
	/** {@inheritDoc} */ @Override public AudioSignal getSignal(int s)      { return out.getSignal(s);     }
	/** {@inheritDoc} */ @Override public boolean hasSignal(AudioSignal s)  { return out.hasSignal(s);     }
	/** {@inheritDoc} */ @Override public boolean isEnabled(AudioSignal s)  { return out.isEnabled(s);     }
	/** {@inheritDoc} */ @Override public boolean isSounding()              { return out.isSounding();     }
	/** {@inheritDoc} */ @Override public void noSound()                    {        out.noSound();        }
	/** {@inheritDoc} */ @Override public void removeSignal(AudioSignal s)  {        out.removeSignal(s);  }
	/** {@inheritDoc} */ @Override public AudioSignal removeSignal(int s)   { return out.removeSignal(s);  }
	/** {@inheritDoc} */ @Override public int signalCount()                 { return out.signalCount();    }
	/** {@inheritDoc} */ @Override public void sound()                      {        out.sound();          }

}

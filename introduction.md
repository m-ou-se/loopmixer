# Introduction

This document is a short introduction to the `LoopMixer` library.
I assume you have (at least) experimented a bit with Processing.
If you want to know what the `LoopMixer` library can do,
take a look at the demo, which shows you the most important features.
If you didn't already do so, download the library
(from https://github.com/m-ou-se/loopmixer/releases),
extract it to your `libraries` folder inside your sketchbook,
and try out the examples.
The examples show you how to set up a `LoopMixer` and how to play some loops.

Try to add your own loop(s) to the last example. Here's a short guide:

## 0. Get some sound
There are lots of websites that provide free sound loops (for example: http://free-loops.com/).
Have fun Googling, listening and downloading.

## 1. Edit the loop
Open the loop in your favorite audio editor (I'd like to recommend `Audacity` or `Adobe SoundBooth`),
and make sure that the sample loops seamlessly.
(Shift click 'play' in Audacity to loop.)
If needed, remove any silence at the begin or end of the sample.
You might have to do some more advanced editing to make it loop seamlessly.
Good luck! `;)`

## 2. Count the number of measures
Carefully listen to the sample and try to match its rythm against somthing like this:

    beats
    1  2  3  4  1  2  3  4  1  2  3  4
    |--|--|--|--|--|--|--|--|--|--|--|--|--...
    |           |           |           |
    1           2           3           4
    measures

If you don't know what and what not to count as a measure,
you might want to take a look at the demo.
It shows the beats and measures of its three sample loops.
The number of measures does not have to be a whole number;
for example, the second loop in the demo has 0.5 measures per loop.

## 3. Save your loop
I have experienced some problems with exporting to `MP3`, unwanted silence was prepended to my samples.
Therefore I'd like to recommend you to use `WAV`, the most simple audio file format.
`LoopMixer` supports any sample rate and both mono and stereo files.

## 4. Use it in your code
I assume that you've already created a `LoopMixer` object.
(If you don't know how to do that, take a look at the examples.)
All you have to do now is create a variable to hold the `Loop` object:

    LoopMixer.Loop myAmazingLoop;

And set it up somewhere (you probably want to do that in your '`void setup(){...}`'):

    myAmazingLoop = mixer.new Loop("myamazingloop.wav", 4);

Where `"myamazingloop.wav"` is the file containing the audio,
and `4` is the number of measures you've counted before.
At this point, you can use the loop by calling its methods, such as `start` and `stop`:

    myAmazingLoop.start();

The last example shows you how to trigger such a method on the press of a key.

A complete overview of all methods can be found in the online documentation:
https://m-ou-se.github.io/loopmixer

_Have fun!_

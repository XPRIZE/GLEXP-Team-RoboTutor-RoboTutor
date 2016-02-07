
# **RoboTutor**

### Please clone directly from this repo - DO NOT FORK
### Do not push to Master on this repo. <br>Create a branch as described below.
### If in doubt on how GitHub works - create a public repo of your own and experiment.


## **Setup and Configuration:**

Install Android Studio
Install GitHub Desktop<br>



## **Usage:**

In Android Studio, open the project which is in the WritingComponent Subdirectory.

The main issues will be determining what will be sent to the recognizer (allowing for multipath characters e.g. H three strokes i,x,t,f two strokes), and potentially debouncing the input stream (caused when a student momentarily lifts there finger or the touch sensor reports an erroneous lift).  Note that it would be ideal if the recognizer could provide parital results to attempt to disambiguate the intent of the user.

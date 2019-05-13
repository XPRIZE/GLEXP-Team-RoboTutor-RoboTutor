# Localization of RoboTutor

Last updated: May 6, 2019 
## Overview
There are 9 key steps to localize RoboTutor as mentioned below:
1. **Analysis of game content:** Identify components in the application that require localization.
2. **Story creation:** Create stories in local language. This helps the child to read and understand sentences.
3. **Image creation:** To represent stories and content in an effective manner, identify the images required.
4. **Dialog creation:** To ensure the child learns how to effectively communicate in day-to-day life, create basic dialog..
5. **Time-stamp generation:** To synchronize the highlighting of words in sentences, generate time stamps for audio files.
6. **Speech recognition:** Create speech recognition models for localized languages.
7. **Handwriting recognition:** Create handwriting recognition models for handwritten characters in localized languages.
8. **Font:** Select a suitable font that can correctly render localized text in the application.
9. **Final build:** With all the assets now prepared, build RoboTutor in the new target language.

## Process
### 1. Stories
Stories section consists of 3 main components, as mentioned below:  
* Story audio
* Story content
* Story images

Stories can be localized as follows:  
1. Localize the story audio present in [English_StoriesAudio](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-EnglishAssets/tree/master/English_StoriesAudio/assets/audio/en/cmu/xprize/story_reading/quality_low).
2. Localize the story content by modifying the JSON files present [here](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-EnglishAssets/tree/master/English_StoriesAudio/assets/story/en).  
For example, use the code snippet from [storydata.json](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-EnglishAssets/blob/master/English_StoriesAudio/assets/story/en/1/1_1/storydata.json) file below and modify the value under the keys _word_, _utterances_ and _sentence_ to use localized text. Update the word's start and end timestamps by modifying the values for the keys _start_ and _end_ respectively.  

```json
{
      "image": "a_cow_is_my_friend_1_page_2_image_0001.jpg",
      "page": 2,
      "text": [
        [
          {
            "narration": [
              {
                "audio": "mother_cow_cow_i_like_you_cow.mp3",
                "from": 19,
                "segmentation": [
                  {
                    "end": 62,
                    "start": 19,
                    "word": "Mother"
                  },
                  {
                    "end": 95,
                    "start": 63,
                    "word": "cow"
                  },
                  {
                    "end": 123,
                    "start": 96,
                    "word": "cow"
                  },
                  {
                    "end": 195,
                    "start": 186,
                    "word": "I"
                  },
                  {
                    "end": 220,
                    "start": 196,
                    "word": "like"
                  },
                  {
                    "end": 244,
                    "start": 221,
                    "word": "you"
                  },
                  {
                    "end": 286,
                    "start": 245,
                    "word": "cow"
                  }
                ],
                "until": 286,
                "utterances": "Mother cow cow I like you cow"
              }
            ],
            "sentence": "Mother cow cow, I like you cow."
          }
        ]
      ]
    }
```

3. Replace the images in [story](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-EnglishAssets/tree/master/English_StoriesAudio/assets/story/en) folder with localized images.

### 2. Instructions
Replace the available instructions in the [English_TutorAudio](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-EnglishAssets/tree/master/English_TutorAudio/assets/audio/en) folder with localized audio.

### 3. Game content
Game content is available in the [assets/tutors](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-RoboTutor/tree/master/app/src/main/assets/tutors) folder. In order to localize the game content, the JSON files available in assets/tutor folder need to be updated.  

### 4. Speech Recognition
The PocketSphinx library is being used for speech recognition. Replace the speech recognition models available in the [models](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-RoboTutor/tree/master/comp_listener/src/main/assets/sync/models) folder with localized models. Languages supported by PocketSphinx can be found [here](https://cmusphinx.github.io/wiki/download/). Replace the contents of the [models](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-RoboTutor/tree/master/comp_listener/src/main/assets/sync/models) folder with a supported language.

### 5. Handwriting Recognition
The Lipi Toolkit (LipiTk) is being used for handwriting recognition.  Replace the modules in the [comp_ltkplus](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-RoboTutor/tree/master/comp_ltkplus) folder with localized modules. Languages supported by LipiTk can be found [here](http://lipitk.sourceforge.net/resources.htm). Replace the contents of [comp_ltkplus](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-RoboTutor/tree/master/comp_ltkplus) folder with a supported language.

### 6. Font
Replace the fonts in the [fonts](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-RoboTutor/tree/master/comp_ltkplus/src/main/assets/fonts) folder with the fonts that support your target localization language. Ensure that the new font files have the same name as the existing font file names so that the application can pick it up.

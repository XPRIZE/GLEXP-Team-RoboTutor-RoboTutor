To prepare a device for custom debugging you should create the required debugging launchers within the RoboTutor folder as described therein.

Once ready you can push the RoboTutor folder to the target device using ADB:

Either run the ADB_PUSH.bat 

or from the command line use:

adb push RoboTutor /sdcard/Download/


For Stories you must create a properly structured and encoded folder hierarchy.

For English the current structure is - The leaf node folders in quotes e.g. "1_37" are the current highest encoded folders - so at each level you should continue creating debug versions at high indices - e.g. "1_38"  as foind in the samples provided

\RoboTutor\tutors\story_reading\
								audio\
										en\cmu\xprize\story_reading\
																	1\
																		"1_37"
																	2\
																		"2_11"
										sw\cmu\xprize\story_reading\
																	1\
																		"1_10"
																	2\
																		"2_17"
																	3\
																		"3_15"
																	4\
																		"4_11"
																	5\
																		"5_1"
								story\
										en\
											1\
												"1_37"
											2\
												"2_11"
										sw\
											1\
												"1_10"
											2\
												"2_17"
											3\
												"3_15"
											4\
												"4_11"
											5\
												"5_1"											
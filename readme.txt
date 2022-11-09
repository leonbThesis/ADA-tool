This is the readme file for the ADA-tool

----------------------------------------------------
How to start the program:
----------------------------------------------------

Either open the project in Netbeans or start the program by executing the "ADAtool.jar" file, contained in the main folder

----------------------------------------------------
How to analyze a sample:
----------------------------------------------------

1) enter path location to sample files
	-> the location should be structed like the following example

		*some folder*
			hlv
				sample1-5123.json
				sample2-6123.json
				sample3-6345.json
			slv
				sample1-1242.json
				sample2-6247.json
				sample3-2183.json

	-> *some folder* must contain only! subfolders "hlv" and "slv" (type sensitive)
	-> equal samples must have an equal json report name and a unique UUID (default: *sha-256 of sample*-*UUID of report*.json) (see Thesis Chapter 4.5 for more information)
		> if a report is only in one folder, it is interpreted as crashing on other variant (and therefore possibly anti-vm)

2) Choose Simple or Full analysis mode (explained in more detail in thesis)
	-> If full analysis: intermediary steps of syscall analysis are logged in the console

3) View all samples/single samples or download the results to a csv

If anything is done wrong or in the wrong order, a detailed error message will appear in the status box at the bottom of the window

----------------------------------------------------
How to change the settings:
----------------------------------------------------

1) Click the settings button in the main window

2) Set the settings accordingly (if the new value is out of the accepted range, a notice is displayed in the status box at the bottom of the window after trying to save)

3) Save the settings

4) Restart the program

-> The settings are saved in the "settings.txt" file, contained in the main folder
	> structure of the file: lines starting with '*' are ignored; lines starting not with '*' will be interpreted as a certain settings value, respective to their order
	> if the settings file is not modified through the settings window of the program, errors can occur.


If any additional question arise / if anything is unclear: the source code contains extensive commenting

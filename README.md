# Swiper
Swiper Metadata Filter for Domino Designer Source Control

## Description
Swiper is a plugin for Domino Designer which is used in tandem with the Source Control Enablement.
When Domino Design Elements are exported for Source Control, they contain unnecessary metadata which makes Branching and Merging almost impossible due to pointless merge conflicts.
Swiper will filter this metadata from the exported files, allowing proper branching and merging.

Swiper is the sucessor to the Dora project. The Dora project was specifically built for use with Git, whereas Swiper can be used for Git, Mercurial, .
Installation is also much simpler.

## Installation

Swiper is installed to Domino Designer via the plugin installation mechanism 

Note: You do not need to install Swiper on the server. It is used at Design-Time only so it is only required to be installed to Domino Designer.

If you have never installed plugins to Designer, you may need to enabled 'Eclipse plugin install' in your preferences.

Once this is enabled you should see the:
File -> Applications -> Install

Option

Choose Search for new features to install

## Usage

Swiper is only designed to work with Projects that are set up for Source Control.

To enable Swiper for a Project, right-click the project and then choose 
Swiper -> Add Swiper

To remove Swiper from a Project, right-click the project and then choose 
Swiper -> Remove Swiper

When files are Synchronised From NSF to the On-Disk Project, Swiper will check for newly created metadata files, it will then filter them using the default filter including in the plugin.

When files are Synchronised from On-Disk Project to NSF, Swiper does not Run.

### Deliberately Filter a file

Usually you can just rely on Swiper to filter your files as you save them, however you may want to deliberately filter a file without opening and saving.

To force Swiper to Filter a file, Right-Click on it in the Application Navigator and choose
Swiper -> Filter Selected

Please note that this right-click menu does not work from the main list of design elements that shows up in the middle. e.g. If you double-click 'Viewes' and then try to right-click a View in the middle 'Views' pane, the option will not show. You need to do it over on the left in the Applications Navigator.

## Notes for Projects that previously used Dora

The default filter supplied with swiper is slightly different, and also the code library for filtering the XML is different. As such there is some minor differences in the output of the filtered metadata. If you encounter these 'changes' you can commit them and you should not be prompted again.

1. Inclusion of 'encoding' attribute on root element
2. Removal of 'maintenanceversion' attribute on Root DXL element
3. Difference in number of EOL characters at the end of the file

Due to these differences, if you are working with other developers on the same project, it is advised that you all switch to Swiper.
Alternatively, you can try to mimic Dora by setting some preferences in the Preferences -> Swiper section


## Known Bugs

* Sometimes when creating a new XPage or Custom Control, the system will say 'The contents on disk have changed do you want to reload?' I need to check into this, but just hit no

* The Swiper menu-option shows up in the normal Notes Client. I plan to put a fix in to prevent this.

## Logging

In case we need to figure out what is going on, there is a logger that can be turned on. It will log to a file in your %HOME%/.swiper directory
To turn on, Click 'File -> Swiper -> Start Logging'
To turn off, Click 'File -> Swiper -> Stop Logging'

# Support from YourKit

YourKit have generously granted the Swiper project an Open Source licence to use their excellent Java Profiler.

![YourKit Logo](https://www.yourkit.com/images/yklogo.png "YourKit Logo")

YourKit supports open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of <a href="https://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a>
and <a href="https://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>,
innovative and intelligent tools for profiling Java and .NET applications.

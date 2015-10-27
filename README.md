# Swiper
Swiper Metadata Filter for Domino Designer Source Control

## Description
Swiper is a plugin for Domino Designer which is used in tandem with the Source Control Enablement.
When Domino Design Elements are exported for Source Control, they contain unnecessary metadata which makes Branching and Merging almost impossible due to pointless merge conflicts.
Swiper will filter this metadata from the exported files, allowing proper branching and merging.

Swiper is the sucessor to the Dora project. The Dora project was specifically built for use with Git, whereas Swiper can be used for Git, Mercurial, .
Installation is also much simpler.

Swiper will be released for alpha testing very soon…


# Saving and Restoring Database Properties

Swiper contains functionality to Save and Restore the Database Properties, Database Icon and XPages Properties.

When using Domino Source Control, my recommendation is NOT to commit Database Property files such as the IconNote, Database Properties, XPages Properties etc.

Why do I recommend this? A few reasons.

Every developer on the project should be able to change his Database Title , ACL, XPage Properties etc without risk of affecting the standard settings.
By Ignoring these files from the Repository, she/he has the freedom to 

The Database Settings for the 'Production' deployment can be saved to a safe place, the Database Settings for the 'Testing' deployment can also be saved.
The Development team can be confident that their settings that are specific to their environment will not be affected by commits made by other team members.

They always have the option to 'restore' the standard settings, make changes and then Save them back to the Saved settings folder.

During Deployment, one of the steps would be to restore the appropriate database properties.

Current Limitations!!!
I Need to fix the logic, currently the restore process only works if there is the existng notes element to restore against.



# Which Files to Ignore from Your Repository?


ODP = On-Disk Project Folder

ODP/Resources/IconNote
ODP/Resources/Images/$DBIcon
ODP/Resources/Images/$DBIcon.metadata
ODP/AppProperties/$DBIcon
ODP/AppProperties/database.properties
ODP/AppProperties/IconNote

I'm not 100% certain about xsp.proprties, there are some settings in there that you 

ODP/WebContent/WEB-INF/xsp.properties



# How do I Stop Tracking a File in my repository using Sourcetree?

.gitignore entries only work for files that have not yet been committed to the repository.
If you have a file that you want to stop tracking, it is easy to stop tracking it using Sourcetree, just right-click and choose stop-tracking.

If you are not using Sourcetree then I dare say you are hardcore enough to know how to remove a file from the repositoyr.

# Support from YourKit

YourKit have generously granted the Swiper project an Open Source licence to use their excellent Java Profiler.

![YourKit Logo](https://www.yourkit.com/images/yklogo.png "YourKit Logo")

YourKit supports open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of <a href="https://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a>
and <a href="https://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>,
innovative and intelligent tools for profiling Java and .NET applications.

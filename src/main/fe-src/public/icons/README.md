# Icons

---

## GHS Icon Sets

One challenge we faced early on in development was finding an icon set that was well-suited to our needs.
While there were some solutions on offer (namely [devicon](https://devicon.dev/) and [octicons](https://github.com/primer/octicons)), they had their own limitations.
Unsatisfied with what was on offer, we decided to craft our own, by reusing icons of existing sets.
This led to the creation of two new ones:

- **languages**: A collection of icons for each programming language supported by the app.
- **octicons-reduced**: A fraction of the octicons set, limited to icons deemed useful within the context of the project.

These sets were created by importing the icon SVGs into [Icomoon](https://icomoon.io/#home).
In this way, we can link each icon to a CSS class, and easily integrate them into our webpage.

## Setting Up the GHS Icomoon Project

To modify the existing icon sets, one must first set up the corresponding Icomoon project that contains them.
This first step involves navigating to the [icomoon project selection](https://icomoon.io/app/#/projects) page.
While there, you will be presented with the option to **Import Project**.
In the file selection pop-up, choose `GHS.json` and click open.
The list of projects should now contain **GHS**.
Click **Load**, and you will be taken to a page containing the two aforementioned icon sets.

## Modifying Icon Sets

After loading the project, Icomoon displays previews of each set and its contents.

To add new icons to a set, click on the set options (the three parallel lines in the top right corner of a set: ≡) and choose **Import to Set**.
Now simply select the SVG(s) you want to add to this set.

Removing icons from a set is also straight forward.
Press and hold ⌘ + ⌥ and click on the icon you want to remove.

If you want to edit the icon set metadata (i.e. information related to the designer, project, license, ...), click on **Properties** in the set options.

## Exporting an Icon Set

The first step of exporting involves selecting the set icons.
In the set options, choose **Select All**.
The icons of the set should all be marked in yellow now.
You can also manually specify which set icons to include/exclude by simply clicking on each individual icon.
Once you are satisfied with your selection, click **Generate Font** in the lower right corner.

You will be taken to a new page, where you may be prompted about "strokes being ignored when generating fonts".
Just click **continue**, and you will be greeted with a set summary page, displaying all the icons.
Take this opportunity to modify the name of each individual icon to your liking.
Before downloading the set however, click the **small gear icon** in the lower right corner.
Make sure that the **Font Name** and **Class Prefix** are set appropriately (`languages` should use `lang-`, while `octicons-reduced` should use `icon-`).
You can also optionally set the icon set version at the very bottom of this menu.
Once done, hit **Download** and you will receive an archive containing your new set of icons.

## Importing the Icons Into GHS

Extract the contents of the downloaded archive.
You should have something like:

```
/icon-set-name
├── Read Me.txt
├── demo-files
│     ├── demo.css
│     └── demo.js
├── demo.html
├── fonts
│     ├── icon-set-name.eot
│     ├── icon-set-name.svg
│     ├── icon-set-name.ttf
│     └── icon-set-name.woff
├── selection.json
└── style.css
```

We only really care about `selection.json`, `style.css`, and the contents of `fonts`.
Copy these aforementioned files into the corresponding icon set directory of `src/main/fe-src/public/icons`.

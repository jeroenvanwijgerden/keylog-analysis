I started using a split ergonomic keyboard (![the Kyria model](https://blog.splitkb.com/introducing-the-kyria/)). Such keyboards have many fewer keys than regular keyboards. So, part of the rite of passage is to create your own keymap. While doing research on keymaps I noticed that the quality of keymaps was argued by analyzing long texts. For example: the letter `e` occurs most often in text, so the button for the letter `e` should be placed in the most convenient location. However, this approach felt wrong to me, for my use-case, for two reasons.

Firstly, as a programmer, I type more special symbols than are present in these analyzed texts.

Secondly, such analyses do not measure keys that are not visible in a final text. Think of keys for hotkeys, or even simple arrow keys for navigation while writing.

With the help of AI I wrote ![](keylog.sh) (because life is too short to learn Bash) that dumps all keypresses in a file, which I could then analyze to my heart's content.

# SECURITY WARNING

If you want to analyze your own keypresses, be aware that storing your keypresses enables a dedicated attacker to reconstruct your passwords and love letters. Never upload upload a log with potentially senstive information to the public internet. Notice the `resources/sensitive` in the `.gitignore` file.

# Using keylog.sh

Might only work on Linux running X.

Read the comment in the script about changing keyboard name.

You can specify a file name. For example, to dump the log in a new file `foo` in the current directory:

```
./keylog.sh foo
```

```
<no output>
```

Without a filename it generates a file name based on the current date and time (in the current directory):

```
./keylog.sh
```

```
No output filename provided. Using default filename: keylog_20231202-130321.log
```

The script keeps running until you stop it.

## Details

The `4` on line 28 in `keylog.sh` determines the time granularity. `4` means a second with four decimals, so the granularity is 0.1 millisecond. 

See ![](resources/gobbo.log) for an example of the output. Each key press/release appears on a line, in chronological order.

# Analyzing

Use ![](src/keylog_analysis/core.clj) in a REPL.

# My analysis

I spat ('`clojure.core/spit`ted') some results in ![](doc/), one file containing the most common presses and chords for three modes of work: working on a Java backend in IntelliJ (![](doc/java.edn)), writing the code for this project in VSCode (![](doc/clj.edn)) and writing a small article in a markup language I'm working on in VSCode, so mostly prose (![](doc/gobbo.edn)).

Note how the most common keys and chords differ between the modes of work, but that arrow keys and navigation chords are always very common.

I ended up choosing ![Colemak-DH](https://colemakmods.github.io/mod-dh/) as the base of my keymap, but I made sure that the arrow keys are easily accessible. With all five fingers of my right hand all on their home position, by default the buttons from index finger to pinky are `N E I O`, but if I hold down the thumb as well, this changes to `Left Down Up Right`. Tapping, not holding, the thumb is `space`. Modifier keys are handled by my left hand. I've been using this for a couple of weeks now and I'm very happy with it.
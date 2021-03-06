   ┌──────────────────────────────┐
═══╡ INSTALLATION / CONFIGURATION ╞═════════════════════════════════════════════
   └──────────────────────────────┘

Unzip the contents of the zip file somewhere. Running from a zip will likely
cause problems.

Before running the randomizer, it must be linked to your Knytt Stories directory
(that is, the folder containing Knytt Stories.exe). There are 4 ways to do this:

	A) Place the randomizer folder in your Knytt Stories directory.
	B) Create a symbolic link called "KS" that links to your Knytt Stories
	   directory.
	C) Create a Windows shortcut called "KS" (formally "KS.lnk") that links
	   to your Knytt Stories directory.
	D) Edit the KS.txt text file: Replace the example directory with your
	   Knytt Stories directory.


   ┌─────────────┐
═══╡ RANDOMIZING ╞══════════════════════════════════════════════════════════════
   └─────────────┘

Run the file KSRandomizer.bat. This will open a command prompt window. If it
immediately closes, ensure that you have java installed and try again.

On a first run, the randomizer will ask you to specify some settings. The level
selection should be self-explanatory, but note that you do not have to be on the
same page as a level in order to enter its number.

On subsequent runs, the randomizer will automatically load your previous
settings. You may then re-randomize the same level in the same way or change the
world, seed, or rules.


   ┌────────────────────┐
═══╡ RANDOMIZATION TYPE ╞═══════════════════════════════════════════════════════
   └────────────────────┘

There are four types of randomization, which specify what the randomizer
considers when deciding what object counts / arrangements to consider. Suppose
that we have a powerup set [■, ▲, ●, ♠, ♥, ♦, ♣]. Then the randomizer types on a
sample map with 5 powerup locations and 2 powerups could look like this:

	Unrandomized	■ ■ ● ● ●

	Permute		● ● ■ ■ ■
	Shuffle		● ■ ● ● ■
	Transform	♣ ♣ ▲ ▲ ▲
	True random	♠ ♦ ♣ ♦ ●

			  ┌─────────────────────┬────────────────────────┐
			  │ Objects of the same │ Objects are randomized │
			  │ type get randomized │ independent of similar │
			  │ together            │ objects                │
┌─────────────────────────┼─────────────────────┼────────────────────────┤
│        Only use objects │       Permute       │        Shuffle         │
│    present in the level │			│			 │
├─────────────────────────┼─────────────────────┼────────────────────────┤
│    Use any objects from │      Transform      │      True random       │
│ the randomization group │			│			 │
└─────────────────────────┴─────────────────────┴────────────────────────┘


   ┌─────────────────────┐
═══╡ RANDOMIZATION RULES ╞══════════════════════════════════════════════════════
   └─────────────────────┘

The randomization key is based off object classes defined in

	resources/ObjectClasses.txt

The object classes file comes with 27 classes by default:

	A:all (except for gamebreaking stuff)
	B:big hologram enemies
	C:common enemies
	D:decoration
	E:all enemies
	F:friendlies
	G:ghost
	H:hologram enemies
	I:invisible
	J:key locks
	K:knytt
	L:lasers
	M:solid blocks
	O:ouchies (anything that harms Juni)
	P:powerups
	Q:powerups without climb
	S:save points
	U:liquids
	V:liquid surface
	W:waterfalls
	X:spikes
	Y:golden creatures:ks+
	Z:KS+ collectables:ks+
	_:empty space
	%:often mandatory to reach certain areas
	$:these objects are essential for some maps to run properly
	!:literally everything

Each object class is defined by a single character and contains several objects
that are similar in one way or another. The randomizer will ask for
randomization rules. A rule is made of several object groups, combined using
various operators. In order to randomize all the critters (the enemies, the
friendlies, and the knytt), use the randomization key:

	EFK

Note that this will randomize the enemies, friendlies, and knytt all in the same
group. In order for the randomizer to keep them in different groups (so that
enemies don't replace knytt and knytt don't replace friendlies), define three
separate rules instead:

	E
	F
	K

Randomizer rules support the following operators (ordered by lowest to highest
precedence):

	AB	Union		All objects that are in either A or B.
	A,B	Union		Same as AB.
	A&B	Intersection	All objects that are in both A and B.
	A-B	Difference	All objects that are in A but not in B.
	A+B	Union		Same as AB. Useful for its high precedence.
	(A)	Parenthesis	Evaluate this expression fist.
	A->B	Arrow		Take all the objects in the level of type A and
				turn them into objects of type B.

Here are some examples of using the above operators. Note the last example in
particular, and how it differs from the others due to order of operations.

	E&G		Ghost enemies (all Enemies that are also Ghosts)
	I-O		Invisible blocks (all non-harmful invisible objects)
	E&G->I-O	Randomize all ghost enemies into invisible blocks.
	(E&G)(I-O)	Randomize ghost enemies and invisible blocks together.
	E&G+I-O		Another way to write the above (using the + operator).
	E&G,I-O		An empty group (all enemies that are either ghosts or
			invisible, and also are not harmful)

What follows are some more examples of common randomization rules. Both the
object classes file and the randomization rule are fully customizable, so the
only limitation is the number of objects in the game.

	P		powerups
	E		enemies
	CG		enemies that are not likely to cause softlocks
	CFGHK		all critters
	CFGHKP		all critters, with powerups mixed in
	D->DEF		decoration can become enemies
	_E		(on Shuffle), rearrange the enemies in a level
	A		absolute chaos
	!		god help you


   ┌───────────────────────────┐
═══╡ CHANGED FILES / REVERTING ╞════════════════════════════════════════════════
   └───────────────────────────┘

The randomizer will replace Map.bin. Playing the randomized level is as simple
as launching Knytt Stories.exe (or your mod of choice) and loading the level as
normal. The old Map.bin file is backed up at

	[your KS directory]/Author - Level Name/MapBackup.rando.bin

Running the file restore.bat will attempt to restore all randomized levels to
their default versions. Alternately, individual levels can be restored by
renaming the MapBackup.rando.bin file back to Map.bin.


   ┌─────────────┐
═══╡ SOURCE CODE ╞══════════════════════════════════════════════════════════════
   └─────────────┘

Knytt Stories Randomizer is open source, and the code can be found at

	https://github.com/Gliperal/Knytt-Stories-Randomizer

It is licensed under a Creative Commons BY-NC-SA 4.0 license, plus the licencing
of dependencies, all of which can be found in:

	LICENSE.txt

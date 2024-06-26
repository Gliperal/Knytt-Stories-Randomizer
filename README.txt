   ┌──────────────────────────────┐
═══╡ INSTALLATION / CONFIGURATION ╞═════════════════════════════════════════════
   └──────────────────────────────┘

Unzip the contents of the zip file somewhere. Running from a zip will likely
cause problems. For basic installation, move or extract the files so that the
two excecutables sit alongside each other:

    Knytt Stories.exe
    Knytt Stories Plus 1.3.6 for Randomizer.exe

You will also need to install Java. The randomizer is guaranteed to work on the
latest release, and is currently backwards compatable with Java 8 and above.
Running on any versions older than that may cause problems. Java can be
downloaded from:

    https://java.com/download/

Assuming everything was installed in the correct place, you will find the
Randomizer files in a folder within your Knytt Stories directory. If you wish
for the randomizer to live elsewhere, you will have to manually link it to your
Knytt Stories directory. Instructions on doing that are included in the section
titled SEPARATE INSTALLATION.


   ┌─────────────┐
═══╡ RANDOMIZING ╞══════════════════════════════════════════════════════════════
   └─────────────┘

Run the file KSRandomizer.bat. This will open a command prompt window.

On a first run, the randomizer will ask you to specify some settings. The level
selection should be self-explanatory, but note that you do not have to be on the
same page as a level in order to enter its number.

On subsequent runs, the randomizer will automatically load your previous
settings. You may then re-randomize the same level in the same way or change the
world, seed, or rules.


   ┌─────────┐
═══╡ PRESETS ╞══════════════════════════════════════════════════════════════════
   └─────────┘

For users unfamiliar with the randomizer, presets are a great place to start.
They can be accessed using the P option in the randomizer menu, and contain the
following already created sets of randomization rules. Unlimited numbers of
presets may also be combined, by loading one and then adding the other presets
to it. Note that depending on the presets used, the order in which the rules are
added may cause different effects.


-DonDoli's Megagamer Ruleset Preset Superset ft. Henna from The Machine-:
        If you have been watching DonDoli's Randomizer content, this is probably
        the preset you're most used to seeing, and a great starting point for
        any level.
Add Coins
Add Collectables
Add Enemies
Add Platforms & Wind
Add Saves
        Theses five all randomly add things scattered throughout the world. Add
        Saves is particularly useful for adjusting the difficulty, and it can be
        added more than once.
Basic Permute
Basic Shuffle
Basic Transform
Basic True Random
        Performs one of the four basic randomization types to the level, while
        keeping things relatively consistent (the same types of enemies will all
        be randomized together, for example). A good starting place if you're
        looking for something that still resembles a level.
Dangerous Decoration
        A callback to the old hardcore randomizer style. Be prepared to learn
        precisely which screens are the most "decorated."
Do Not Pick Up The Eye
        ...even if the eye really wants you to.
Free Powerups
        Combine with any other ruleset to get free powerups at the start.
        Recommended to use last in the rules, as it will not leave much space
        for other randomization effects to do anything.
In Search of Civilization
        Do you know where knytts can be found in the world? Get rewarded for
        that knowledge.
Popcorn Madness
        Do you know which enemies we like to affectionately call the popcorn
        makers? You will soon...
Remove Enemies
Remove Spikes, Lasers & Death Tiles
        These two remove dangerous things from the level. Recommended to load
        these presets first, before adding others on top.
Stormy Waters
        The knytt world is experiencing quite the storm... Can add a different
        kind of challenge to any of the other presets. Recommended to use last
        in the rules, as it will not leave much space for other randomization
        effects to do anything.
Waterfall GC's
        Waterfalls become golden creatures. Can you collect them all?
Where's Specko?
        Find the golden speck amongst a myriad of his bretheren.


   ┌────────────────────┐
═══╡ RANDOMIZATION TYPE ╞═══════════════════════════════════════════════════════
   └────────────────────┘

There are four types of randomization, which specify what the randomizer
considers when deciding what object counts / arrangements to consider. Suppose
that we have a powerup set [■, ▲, ●, ♠, ♥, ♦, ♣]. Then the randomizer types on a
sample map with 5 powerup locations and 2 powerups could look like this:

    Unrandomized    ■ ■ ● ● ●

    Permute         ● ● ■ ■ ■
    Shuffle         ● ■ ● ● ■
    Transform       ♣ ♣ ▲ ▲ ▲
    True random     ♠ ♦ ♣ ♦ ●

                          ┌─────────────────────┬────────────────────────┐
                          │ Objects of the same │ Objects are randomized │
                          │ type get randomized │ independent of similar │
                          │ together            │ objects                │
┌─────────────────────────┼─────────────────────┼────────────────────────┤
│        Only use objects │       Permute       │        Shuffle         │
│    present in the level │                     │                        │
├─────────────────────────┼─────────────────────┼────────────────────────┤
│    Use any objects from │      Transform      │      True random       │
│ the randomization group │                     │                        │
└─────────────────────────┴─────────────────────┴────────────────────────┘


   ┌─────────────────────┐
═══╡ RANDOMIZATION RULES ╞══════════════════════════════════════════════════════
   └─────────────────────┘

The randomization key is based off object classes defined in

    resources/ObjectClasses.txt

The object classes file comes with 75 classes by default, categorized into 8
superclasses:

    POWERUPS
    ENEMIES
    LIQUIDS
    DECORATIVE, which also includes things like nature effects, critters, and
                knytt.
    WORLD, which contains various essential objects for KS levels. Of particular
           note are the Sa save points and the _ empty space.
    ADVANCED, which is generally not useful for randomizing.
    KS+, which contains all of the objects exclusive to the Knytt Stories Plus
         mod. Of particular note are the !a !c !g classes, which contain the
         artifacts, coins, and golden creatures, respectively.
    PATTERNS (see the section on Patterns)

Each object class is defined by one or more characters (usually 2) and contains
several objects that are similar in one way or another. The randomizer will ask
for randomization rules. A rule is made of several object groups, combined using
various operators. In order to randomize the decoration (Da), the enemies (Ea),
and the powerups (Pa), use the randomization key:

    DaEaPa

Note that this will randomize the decoration, the enemies, and the powerups all
in the same group. In order for the randomizer to keep them in different groups
(so that enemies don't replace decoration and decoration doesn't replace
powerups), define three separate rules instead:

    Da
    Ea
    Pa

Randomizer rules support the following operators (ordered by lowest to highest
precedence):

    AB      Union           All objects that are in either A or B.
    A,B     Union           Same as AB.
    A&B     Intersection    All objects that are in both A and B.
    A-B     Difference      All objects that are in A but not in B.
    A+B     Union           Same as AB. Useful for its high precedence.
    (A)     Parenthesis     Evaluate this expression fist.
    A->B    Arrow           Take all the objects in the level of type A and
                            turn them into objects of type B.

Here are some examples of using the above operators. Note the last example in
particular, and how it differs from the others due to order of operations.

    Dk&Dg           Grounded knytt (all knytt that are also grounded fluff)
    Da-Dr           Inanimate decoration (all decoration that is not critters)
    Dk&Dg->Da-Dr    Randomize all grounded knytt into inanimate decoration.
    (Dk&Dg)(Da-Dr)  Randomize grounded knytt and inanimate decoration together.
    Dk&Dg+Da-Dr     Another way to write the above (using the + operator).
    Dk&Dg,Da-Dr     An empty group (all knytt that are either grounded fluff or
                    decoration, and also are not critters)

What follows are some more examples of common randomization rules. Both the
object classes file and the randomization rule are fully customizable, so the
only limitation is the number of objects in the game.

    Pa        powerups
    Ea        enemies
    Eb        enemies that are not likely to cause softlocks
    DrEa      all critters
    DrEaPa    all critters, with powerups mixed in
    Da->DaEa  decoration can become enemies
    _Ea       (on Shuffle), rearrange the enemies in a level
              absolute chaos
              god help you


   ┌──────────┐
═══╡ PATTERNS ╞═════════════════════════════════════════════════════════════════
   └──────────┘

Patterns are a more advanced form of object class, specifying not the type of
object, but rather information about where they are found and what surrounds
them. Patterns can either be defined in the ObjectClasses file or in the rules
themselves, but either way they are expressed using a json format. There is
currently only one type of pattern available:

{
    "type": "tile",     TILE type patterns specify information about the tile
    "x": 1,             specifies the x-offset of the tile to check (default 0)
    "y": 1,             specifies the y-offset of the tile to check (default 0)
    "solid": false,     specifies whether the pattern should only match solid or
                        non-solid tiles (default true)
}

Patterns can be combined using all the same operators as traditional object
classes can. For example, a pattern that matches objects standing on the ground
(non-solid tile above a solid tile) could be expressed as follows:

    {"type": "tile", "solid": false} & {"type": "tile", "y": 1}

The & is important, because otherwise it would match any object that is either
on a non-solid tile OR above a solid tile, including those in the air and in
walls.

Patterns can be used on both the left and right side of a rule. When used on the
right side of the arrow, they will first be simplified down into object groups
based on the map being randomized. This may potentially produce unexpected
results, so use with caution. For example, the rules:

    Dr->Ea&Xe
    Dr->Ea-Xf

may appear functionally identical at first (turning friendlies into either
enemies that are airborn or enemies that are not on the ground). But in fact,
the first rule will turn friendlies into any enemy type that appears airborn
anywhere in the level. The second rule will turn friendlies into any enemy type
that never appears on the ground. That is to say, if a type of enemy (green
bouncer for example), sometimes appears one tile above the ground and sometimes
two, then it will show up in the first randomization, but not the second.


   ┌────────────────────────┐
═══╡ DEFAULT OBJECT CLASSES ╞═══════════════════════════════════════════════════
   └────────────────────────┘

_: empty space: Any tile that is empty, i.e. does not have any object there.
                Most tiles on most maps contain four instances of empty space
                (one per layer).
!A: artifacts: The 49 KS+ artifacts.
!B: crumble block: The KS+ crumble blocks that disappear when stood on.
!C: coins: The 100 KS+ coins.
!G: golden creatures: The 50 KS+ golden creatures.
!L: gc locks: The KS+ locks that open on a certain number of golden creatures.
!M: key press mimics: The hidden KS+ objects that enforce a key press while Juni
                      is touching them.
!O: one-way platform: The KS+ platforms that can be jumped through.
!P: purple block: The KS+ purple blocks that disappear when touched.
!R: enemy restrictors: The KS+ blocks that block the movement of certain
                       creatures.
!T: two-way platform: The KS+ platforms that can be jumped and dropped through.
!U: underwater screen: The KS+ effect tile that makes a screen use underwater
                       physics.
!W: underwater tiles: The KS+ effect tile that makes a specific tile or
                      half-tile use underwater physics.
Ac: code stuff: The code buttons, all other buttons that can also open code
               gates, and anything that can be opened by said buttons.
Ae: eye walls: The hidden object that allows certain blocks to become non-solid
              when the eye powerup is collected.
Ag: ghost blocks: The transparent blocks that appear when the eye powerup is
                 collected.
Ar: restrictors: The hidden objects that enable no-climb walls, sticky floors,
                 and no-jump zones.
At: technical: Objects that are required for certain maps to function correctly,
               including shifts, warps, KS+ triggers, KS+ allow holo area, and
               KS+ block user tiles.
Ax: text: The hidden objects that trigger sign popups. Note that most screens
          have no sign data written into them, so any sign objects encountered
          will display a blank textbox.
Ay: template objects: The KS+ custom objects that emulate the behavior of a
                      specific object.
Az: custom objects: Custom objects.
Da: all decorative: All of the decoration, including nature effects, man-made
                    effects, non-harmful creatures, and even knytt.
Dc: clouds: The invisible objects that create cloud particles when Juni touches
           that floor or wall.
Dd: decoration: Other types of decoration, including machinery, torches,
                sparkles, and non-lethal explosions.
De: everywhere fluff: Non-harmful creatures, including winged knytt, that move
                      about the air.
Df: fish: Things that appear underwater, including fish and bubbles.
Dg: grounded fluff: Non-harmful creatures, including knytt, that sit or walk
                    around on the ground.
Dk: knytt: Knytt, including winged knytt and babies, but not including ghosts.
Dn: nature: Nature effects, including flies, fireflies, leaves, snow, rain,
            dust, and rays of sunlight.
Dr: friendlies: Non-harmful creatures, including ghosts.
Ds: shine: The invisible object that creates a shimmering effect on one tile.
Dx: detection: The invisible objects that make Juni glow red or cyan.
Dz: effects: Effects that adjust or mute the music on certain conditions. Also
            includes the KS+ effect that displays a title.
Ea: all enemies: All dangerous entities, including those non-living, but
                 excluding lasers, spikes, and death tiles.
Eb: all passable enemies: All enemies that are universally deemed safe to place
                          randomly on any screen without generally impeding
                          progress. Edge cases apply; your results may vary.
Ec: solid enemies: All enemies that are deemed as potentially blocking if they
                   happen to spawn in a inopportune location.
Ee: ceiling enemies: Passable enemies that lurk on the ceiling.
Ef: flyers: Passable enemies that move about in the air.
Eg: ground enemies: Passable enemies that sit or walk along the ground.
Eh: hologram enemies: Enemies that can be used as a way to require hologram to
                      pass a large area, including elementals, large spikers,
                      and the fire flower.
Ei: solid flyers: Non-passable enemies that move about in the air. Does not
                  include non-creatures like labyrinth spikes.
El: lasers: Lasers.
Em: right wall enemies: Passable enemies that live on left-facing walls.
En: left wall enemies: Passable enemies that live on right-facing walls.
Es: spikes: Pop-up wall, floor, and ceiling spikes, and their static KS+
            counterparts.
Et: death tiles: Invisible objects that kill Juni when she touches them.
Ew: water enemies: The purple and black enemies that randomly pop up from
                   liquids to attack. Included in a separate category because
                   several are frequently places on one screen in order for them
                   to function to their fullest potential.
Ez: chasers: The three homing shapes and the horizontally-moving ghost enemy.
La: all liquids: Everything in the liquids bank, including the liquid surfaces,
                the solid lower layers of the liquids, and various waterfall
                effects.
Lb: liquid bases: The solid blocks that make up the lower layers of liquid.
Ll: harmful liquids: The surfaces of the liquids that actually kill you.
Lt: waterfall tops: The top foamy layer of the cosmetic waterfalls.
Lw: waterfalls: The cosmetic waterfall tiles.
Pa: all powerups: All powerups, including keys and the KS+ map powerup.
Pc: climb: The climb powerup.
Pd: double jump: The double jump powerup.
Pe: eye: The eye powerup.
Ph: hi-jump: The hi-jump powerup.
Pk: keys: The four keys. To target an individual key, use 0:21 for red, 0:22 for
          yellow, 0:23 for blue, or 0:24 for purple.
Pm: map: The KS+ map powerup.
Po: hologram: The hologram powerup.
Pr: run: The run powerup.
Pt: detector: The detector powerup.
Pu: umbrella: The umbrella powerup.
Sa: save points: The glowing floor spots Juni can use to save.
Wb: blocks: All objects that functions as solid blocks, including liquid bases
           and the orange block that can be opened with a code, but excluding
           the white gates that can be opened with a code, and including the
           default invisible blocks and exactly one of the KS+ invisible blocks.
Wi: win tile: The hidden object that triggers the ending cutscene. Note that
              some maps use shifts to trigger the end cutscene and some maps
              have no default ending cutscene at all.
Wl: locks: The four key locks. To target an individual lock, use 15:27 for red,
          15:28 for yellow, 15:29 for blue, or 15:30 for purple.
Ws: springs: The invisible spring object that bounces Juni upwards.
Ww: wind: The wind object that allows Juni to ascend with the umbrella.
Xb: one tile bridge: Matches any solid tile that has air above and below.
Xe: in the air: Matches any non-solid tile that has air below.
Xf: on the ground: Matches any non-solid tile that has a solid tile below.
Xl: left dropoff: Matches the tile one up and to the left of a platform edge.
Xr: right dropoff: Matches the tile one up and to the right of a platform edge.


   ┌───────────────────────────┐
═══╡ CHANGED FILES / REVERTING ╞════════════════════════════════════════════════
   └───────────────────────────┘

The randomizer will replace Map.bin. Playing the randomized level is as simple
as launching Knytt Stories.exe (or your mod of choice) and loading the level as
normal. The old Map.bin file is backed up at

    [your KS directory]/Author - Level Name/MapBackup.bin

Running the file restore.bat will attempt to restore all randomized levels to
their default versions. Alternately, individual levels can be restored by
renaming the MapBackup.bin file back to Map.bin.


   ┌───────────────────────┐
═══╡ SEPARATE INSTALLATION ╞════════════════════════════════════════════════════
   └───────────────────────┘

Before running the randomizer, it must be linked to your Knytt Stories directory
(that is, the folder containing Knytt Stories.exe). There are 4 ways to do this:

    A) Place the randomizer folder in your Knytt Stories directory or a
       subfolder of your Knytt Stories directory (3rd Party Tools for example).
    B) Create a symbolic link called "KS" that links to your Knytt Stories
       directory.
    C) Create a Windows shortcut called "KS" (formally "KS.lnk") that links
       to your Knytt Stories directory.
    D) Edit the KS.txt text file: Replace the example directory with your
       Knytt Stories directory.


   ┌─────────────┐
═══╡ SOURCE CODE ╞══════════════════════════════════════════════════════════════
   └─────────────┘

Knytt Stories Randomizer is open source, and the code can be found at

    https://github.com/Gliperal/Knytt-Stories-Randomizer

It is licensed under a Creative Commons BY-NC-SA 4.0 license, plus the licencing
of dependencies, all of which can be found in:

    LICENSE.txt

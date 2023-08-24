# Capture the Flag 2.0

## Table of Contents
1. Overview
   1. What is this project?
   2. How does the game work?
   3. What's unique about CTF 2.0
2. Custom Frameworks
   1. SQL & Data Management
      1. Standardized SQL Statements
      2. Wrapped Statements
      3. Data Structures
      4. Stat System
   2. Packets
      1. Packet Wrapping
      2. Packet Rendering
   3. Threading
      1. Worker Threads
      2. Thread Pooling
      3. Custom Async Tasks
   4. Async Game Core
      1. Dual-Threaded Approach
   5. Event Framework
      1. Asynchronous Events
      2. Event Pre- and Post-Processing
      5. Advanced Event Handling w/ @EventReact 
         1. Instance-Friendly Registration **(WIP)**
         2. Thread Control
         3. Stage-Dependent Event Handling
         4. Intuitive Event Priority **(WIP)**
   6. Player
      1. Wrapper Classes
      2. Inventory Profile System
   7. Text Display & Messaging
      1. Text-Building Utility
      2. Constant-Based Framework
         1. Messages
         2. Titles
      3. Scoreboards
   8. GUIs
      1. Page Support
      2. Drawability/Backend Visualization
   9. Entity Framework
      1. Spawn or Render Support
      2. Real-time Updating/Refreshing
      3. Holograms
         1. Advanced Holograms & Display Nodes
3. Other Features
   1. Connection Pooling
   2. Packet Event & Listener
   3. Basic Rate Limiter

### 1. Overview

***What is this project?***

Capture the Flag 2.0 is a grand revision of an earlier project with its same name.
This document's purpose is to serve as a documentation of the most
noteworthy systems/frameworks that I've written into CTF 2.0.

In this rewrite, I sought to remedy an almost indescribably long list of issues with the previous project. CTF 1.0
was messy, buggy, inefficient in many areas, and woefully thread unsafe. Its database interaction
was amateur and at high risk of memory/resource leaks. It used config files to save maps. The classes
where it handled game function, such as ability effects when events fired, were miserably long, unintuitive,
and an absolute nightmare to edit or read... and ten thousand more specific issues.

And as far as unspecific issues go, the way in which it was written was simply... poor. Over-use of nested ifs, an
agonizing pattern of copy-and-pasting bloated and nearly identical code blocks, unnecessary memory
waste in creating variables & references where it wasn't necessary, and a rather obvious display
of ineptitude with just the sheer number of cases where primitive & basic code could've been replaced by
using a cleaner and more advanced solution -- but I simply wasn't aware of that solution at the time.

In addition to the knowledge & understanding that I gained from rewriting CTF from the ground up, 
the process caused me to reflect on a shift in my own approach to programming. Previously,
my priority when writing code had always been to achieve whatever I was building, to make it work
in-game and look cool in-game and be fun to use in-game. 'In-game' is the key there. What was frankly
abject laziness on display in my older projects was a result of more short-sighted priorities -- I had
a cool idea in my head, and I wanted it to exist in a Minecraft world so that I could show it off
to my friends. What exactly was happening under the hood was unimportant, so long as my terminal wasn't
overflowing with stracktraces and so long as the lobby's FPS wasn't kneecapped into the single digits
by some strange bug.

But, as I returned to the project seriously for the first time in years (I did so unseriously a year
ago, but with a lens that was insufficiently critical), it became soberingly apparent to me just
how, for a lack of a better word, *adolescent*, my project was. And the more I dug through it,
the more amazed I became that I had ever used words like *good* or *impressive* to describe it.
Indeed, it became actively painful to continue poring through. But I persevered... and decided
to remake the entire thing from the ground up, no matter how long it took, one class at a time.

***How does the game work?***

As far as how the game works, that being how it plays/actually
functions, CTF 2.0 is not much of a departure from a traditional
understanding of Capture the Flag, nor is it meant to be. In designing
it, I was looking to emulate the basic Capture the Flag experience,
and translate it into a sleek, modern-feeling iteration.

Broadly, its design philosophy is the prioritization of simplicity and sleekness on
the frontend, with very smooth & straightforward flag interaction
and menu usage. Tack onto that an Ability & Perk system for an
element of progression to keep players playing, and that's the
core of my take on Capture the Flag.

Here's a basic rundown of the rules:
- Players start with a basic kit composed of a sword, bow, limited
arrows per life, and basic mixed leather/iron armor
- Players gain gold & xp for kills, assists, capturing flags,
winning games and participation in general
- Gold is used to buy Abilities, xp simply levels you up
- The first team to capture three flags wins automatically
- If the timer reaches a certain point without a team winning,
deathmatch begins, during which the first team to capture
a flag wins no matter what
- If the timer reaches zero without the deathmatch ending organically,
a winner is selected based off of raw team stats, such as flags captured
and total kills

***What's unique about CTF 2.0?***

Or more specifically, what's unique about the way in which it's
built? Because, as the last explained, the way that CTF 2.0 
*plays* is intentionally streamlined and uncomplicated.

Quite simply, CTF 2.0 should not be thought of as merely a minigame. Rather,
it is a sandbox of sorts in which numerous systems and frameworks that I conceptualized
to solve certain limitations were given form.

I will say that many of the frameworks contained herein are, in
many ways, essentially 'standalone'. That is to say, they could
absolutely be extracted, expanded a little, and made into
separate third-party libraries to be applied to many different
minigames or even plugins broadly.

## 2. Custom Frameworks

### SQL & Data Management

CTF 2.0 uses its own SQL Integration system, one that is designed with
usability and thread-safety as priorities.

**Standardized SQL Statements**

All as subclasses of the class ```SQLStatement```, each variety
of SQL statement is made for easy, routine and foolproof use.
In CTF 2.0 currently, only the statements that the minigame
would reasonably use are implemented, but conceivably any SQL
statement could be easily implemented using the same framework.

The benefits offered by this approach are right there in the name:
standardization. By making each SQL Statement a class, in which
the actual SQL is all composed completely under the hood, the risk
of human error when writing SQL statements as concatenated, and potentially
overcomplicated and unreadable, strings, is reduced significantly. It
also ensures that silly, but potentially hard to miss on a larger scale,
mistakes, such as forgetting a semicolon at the end of the statement,
never happen.

On top of this, inserting and formatting parameters and conditions
both into statements is made far easier, and becomes a process as
simply as passing an array or collection of values to a method and
letting the system do the rest. It will determine the type of a
given variable and format it accordingly, whether that be by
putting quotes around a string value or converting a UUID into
SQL-friendly byte format. It will also handle the insertion of
proper SQL type identifiers, e.g. ```BOOL```, ```BIGINT(0)``` or
```VARCHAR(0)```.

**Wrapped Statements**

The class ```WrappedStatement``` is what you actually work with
when creating a SQL Statement, and then getting results from it
if desired/applicable. This object handles sending the SQL Statement
in a controlled manner, holds info regarding the state of the
statement, and completely handles the reading & processing of
results, if applicable.

From a Wrapped Statement, you can easily test if the statement
has finished, and if it was successful (resolved without
an exception). In the event of the statement being a query,
results are unpacked immediately from a ```ResultSet```, formatted
into usable data (mostly, the reconversion of UUIDs from byte format
to Java UUID), and stored in a collection to be accessed when needed.
This is done regardless of whether the data is requested elsewhere
in the code upon completion of the statement, or even if all of
the data is ever used -- ```ResultSet``` is closed as soon
as possible after use so as to avoid resource and memory leaks, 
and database locking.

Additionally, Wrapped Statements are designed with multithreading in mind.
So first and foremost, there is no risk of the thread from which the
```WrappedStatement``` is made and sent being locked or forced to wait for the statement to finish.
Secondly, Wrapped Statements are able to offer a feature that facilitate
thread interaction, that being the ```waitForFinish()``` method, which
can be used to have a different thread wait until the SQL Statement has finished,
whether that be through successful completion or throwing an exception.

**Data Structures**

In order to maximize smoothness of function on the part of database interaction,
CTF 2.0 uses a sort of 'data buffer'/formatter class called ```WrappedData``` 
in order to handle all values that are ever written to or read from the
database. This wrapper class is ambiguous type-wise, and simply holds 
a value of any type.

```WrappedData``` will handle determining the object value's type and translating that
into a SQL data type, as well as determining what the default value should be
based on the value's type. This allows for a level of convenience and safety when
working with the database, such that variables can be saved without
a default being explicitly defined, and without there being any risk of
wayward null values being written into the database. ```WrappedData``` also saves
names, corresponding to columns in a SQL table that should hold that data.

Additionally, the enumerator ```Table``` is created as an object, where any
tables that CTF will need to use can be explicitly defined. Currently, those
are only two: one to store all player data, the other to store maps. In both,
each entry in the table will be uniquely identified by a UUID -- for players, that's
whatever their UUID is, for Maps, a new UUID is generated when a new Map is created.

The ```Table``` constant provide templates for all of the data storage that CTF 2.0 does, allowing for
said tables to be automatically created at plugin startup, with all of the proper
columns set to the proper expected data type, in the event that they don't already exist.

**The Stat System**

The Stat system also uses an enumerator, ```Stat```, and essentially provides a more detailed
template for all player stats that exist. This makes it very easy for the code
to know what values it should be searching for when loading a player's data, and it
also makes adding new stats almost effortless. Simply create a new enum for the stat,
and when players join who don't have that stat as a field in their row yet, it
will simply be created for them with whatever that stat's default value is.

Note that CTF 2.0 uses an efficient model for loading player data. When a player
logins in, their data is loaded and copied to cached values on the server, all
changes to that players data are made to the cached values, and then those values
are saved when the player leaves. There is also an Autosave feature that runs at
a configureable interval, saving the data of players online to the database 
to prevent as much data loss as possible in the event of the server crashing.

The design of ```Stat``` also garners the benefit of centralizing the fetching
and writing of player data to two methods, methods that simply take the ```Stat```
enum value, and the value to which it should be set.

### Packets
**Packet Wrapping**

Inspired by Wrapped SQL Statements, I created wrapped/mutable packets to 
levy some of the same ease and convenience onto packet usage. There are various
classes, all sublasses of ```GamePacket```, each created to represent a type of packet
that CTF 2.0 uses.

The design of each ```GamePacket``` is decentralized. Their constructor
takes more-or-less identical arguments to what the packet itself takes, though
optimized/made more intuitive in some cases, and then saves them to fields. 
Only once the packet is actually ready to be sent is the method ```get()``` run,
which builds the packet's fields into an actual NMS packet to be sent. This allows 
for packets to be mutable after instantiation, to have their fields be tweaked and set.
*Packet Rendering* also makes this even more useful than it might seem (next section).

A packet can easily have the players who are allowed to be sent/'see' it
controlled. They are called 'receivers', and are stored as a Set in each ```GamePacket```.
Simply put, players in the Set are the only players that will be sent it, though
likewise, if the Set is empty aka if no specific receivers have been defined, the packet
will be treated as ubiquitous and be sent to the entire lobby by default.

Additionally, applicable packets implement the interface ```Revertable```, meaning
that they receive in-built ability to be 'reverted'. For example, if it's a packet that spawns
an entity, the packet reverting it will be one that destroys that very same entity.

**Packet Rendering**

In the past, when I've worked with packets, I've been troubled by a couple points of 
difficulty & inefficiency that just seemed inherent to packet usage.

1. **Packet Impersistence**

   A packet, once sent to a client, essentially ceases to exist. The client
   will continue to behave however it's supposed to behave when receiving that packet,
   anything from 'spawning' an entity to processing a scoreboard team as though
   it exists, but there are so many limitations to this.
   1. Once a client's connection is terminated, the behavior caused by the packet
   ceases. So if a player leaves and rejoins, they will no longer see a packet.
   2. If a player joins even a millisecond after a packet is sent, they won't
   see the same packet everyone else now does.


2. **Locational Relevance**

   The second piece is the locational relevance of packets. Many packets,
   by simply virtue of their effect, are locational in nature. If they spawn
   a specific entity, that entity is spawned at a specific location. If they
   change what a player appears to be wearing, they're essentially only affecting
   whatever that player's location is. What I mean by this: packets of the sort
   that I just described only matter, only really exist, if the client receiving
   them is within render distance of what it is that's being targeted.

So, enter Packet Rendering. I wanted packets to become dynamic, persistent,
and usable. I didn't want to have to manually resend necessary packets to players
who just joined or left and rejoined, and I didn't want to be sending
packets to players in a lobby who were too far away for that packet to be
anything but a slight extra burden on latency.

The Packet Rendering system is centered around the ```PacketRegistry``` class,
a static class that makes use of a concurrent Map to track which packets are 
rendered and for whom, and that provides a basic tranche of usable and intuitive
'render', 'derender' and 'update' methods to interact with rendered packets.

Packet Rendering enables the following specific behaviors for packets:
1. Upon joining, a player will be automatically shown all ubiquitous packets
(packets with no receiver set). In addition, they will be sent any packets
for which they are already a receiver (if they were set as one, then left and rejoined),
or for any packets that have a *pending receiver source* into which they 
fall, such as a certain team.
2. Packets that are denoted as being locational (details on that in a second)
will be automatically 'maintained' for all players who are found to be within
a configureable chunk distance threshold of whatever it is that packet is
targeting. If a player leaves that threshold, the packet will be derendered,
if a player enters the threshold, the packet will be (re)rendered for them. This
is built to include unorthodox methods of transporting between chunks, such
as teleportation, and also works for players who join inside the necessary
radius instead of moving into it.
3. Packets can be 'refreshed'/updated with new changes. Say you have an instance
of ```GamePacket.SpawnEntity```, it's rendered for players, and you decide to
change that entity's display name. Now, you simply have to run the ```update()```
method for the packet, and all players who were seeing that entity (and still should)
will see it immediately update with its new name. For applicable packets, this makes
use of the packet reversion behavior mentioned in the previous section, which is
important in order to avoid buggy scenarios such as clients being shown an entity
with the same entity id twice. ```update()``` will call the reverted version of a packet
before re-sending the packet in its new form to avoid this, and likewise, packets
have a failsafe that prevents them from being rendered twice without first being
derendered.

So what about packets for which rendering doesn't make any sense, or only makes
partial sense? The ```@PacketPolicy``` annotation addresses this, allowing for
a packet's 'render policy' to be set to ```SEND_ONLY```, ```STATIC``` or ```LOCATIONAL```.

```SEND_ONLY```: These are packets like particle packets that are only sent once
and then are finished, or packets that update a line of a player's scoreboard.
These packets flatly cannot be rendered, as no additional functionality
is enabled to them by rendering.

```STATIC```: This is for packets that should be rendered/maintained, but that
are not locational and effect all players to which they're sent equally, regardless
of location. A good example is packets that create a scoreboard, or emulate
the existence of a scoreboard team in order to change the color of players'
nametags. These packets are 'half-rendered', in that the Packet Registry will
ensure all players who should see them always do, but nothing beyond that.

```LOCATIONAL```: Finally, locational packets will be specifically maintained 
only for players who are within their vicinity. Even if a player is designated
as a receiver, they must also be within the packet's vicinity. This uses
a custom event that tracks only when players move from one chunk to another,
rather than Bukkit's PlayerMoveEvent which fires even when a player's head
rotation changes.

### Threading

CTF 2.0 uses its own custom threading system, mostly for finer thread
control than is offered by Bukkit's asynchronous threads, and also to have
access to more and different threads than merely those four threads.

**Worker Threads**

Worker Threads are a custom version of Thread created for CTF 2.0, largely
with enhanced control and inter-thread communication in mind. The primary
feature that Worker Threads offer over a generic thread is the ability to 
receive, hold and consider certain messages. This is useful for waking a Worker
Thread with a specific message, thus allowing it to proceed with more
contextual information if there's code that wakes it in multiple different
contexts.

Additionally, Worker Threads are able to wait on specific messages -- that
is to say, they will wait until notified with the correct message. They can also
wait until receiving *any* message, and have access to a hybrid method
which mixes the Java timed ```wait(long timeout)``` with waiting on a message -- forcing the thread
to wait until either it receives a message *or* a certain amount of time passes,
whichever comes sooner.

**Thread Pooling**

CTF 2.0 makes use of Java's ```ExecutorService``` to maintain a cached thread pool,
one that expands and shrinks dynamically based on demand and is not of fixed size.
In constructing this pool, a basic custom ```ThreadFactory``` is used so that the threads
created by the pool are Worker Threads.

**Custom Async Tasks**

The class ```AsyncExecutor``` is modeled somewhat after ```BukkitRunnable```, in
that it is a subclass of ```Runnable``` and allows for the creation/assignment of
tasks. It is also self-referential and therefor has self-terminating capabilities. 
As you might imagine, ```AsyncExecutor``` delegates any tasks to an available Worker
Thread, it also offers support for immediately executing a provided task, executing
a repeating task that repeats every interval of a given ```TimeUnit```, as well as
delayed/postponed tasks.

```AsyncExecutor``` also offers the method ```doTaskWithFuture()```, allowing
a task (a ```Supplier<>``` specifically) to be returned immediately as a ```Future<>```
for the purposes of any desired asynchronous programming.

### Async Game Core

**Dual-Threaded Approach**

Using CTF 2.0's threading system, I built the game's 'core' (phases, countdowns, timers, etc.)
as a fully asynchronous and multithreaded system. The broader cycle that the game
follows, from the lobby to counting down to playing to ending to lobby again (roughly), is
asynchronous. As is all of the logic that it uses to maintain itself.

The dual-threaded nature of the system comes in at how it handles repetitive tasks
like counting down a timer. There is a main game thread, that's the thread responsible
for executing a lot of the game's logic, for transitioning between game phases, and for
managing the second the thread.

The second thread isn't even really a second thread, instead, a thread is fetched from 
the thread pool the second the game finds itself needing a recurring, once-per-second task. 
Once that task has finished, usually when the game's phase is transitioning, the thread is 
fully released back into the pool. And while this second thread is repeating a task, the
main game thread is idle, waiting for a message either from the second thread that the game's 
ready to move on some way, or from, for example, a method that reacts to PlayerQuitEvent if 
after a player just quit, the game is now forced to terminate as a result of having too few players online.

Note that nowhere in this process is the main thread ever involved, at least not
in the sense that it's forced to calculate some sort of logic or wait on another
thread in any way (thus freezing it). The main thread's only involvement is entirely
relegated to certain code that's run when the game reaches a certain state, such
as players being teleported and given items when the game phase ```InGame``` begins.

### Event Framework

CTF 2.0 has its own Event Framework, one that was made with the intention
of making event handling more versatile & usable, better adapting it for a multithreaded
environment, and allowing for more control over it.

**Asynchronous Events**

First and foremost, the Event Framework entails ```AsyncEvents```, which are
custom events that represent either a CTF-specific event (like capturing a flag),
or an event that expands in some way on a Bukkit event in a thread-safe manner (no modification
of that Bukkit event's fields on an alternate thread, for starters). An example of this is the
aforementioned ```AsyncPlayerMoveChunkEvent``` used in Packet Rendering.

The purpose of Asynchronous Events is to allow a certain variety of events to be fired
and reacted to, all asynchronously, without involving the main thread in any step of the
way. Among other things, this helps keep more complex logic and just cumbersome code
that pertains to the likes of, especially, flags, off of the main thread.

**Event Pre- and Post-Handling**

Included in the ```AsyncEvent``` superclass is the ability to override
the corresponding methods and implement Pre-Processing and Post-Processing
code -- with Pre-Processing being run just after the constructor finishes
initializing and before any event handling of it can be done, and Post-Processing
being after the last event handling has finished.

So far, the main use for this that I've found is baking certain behavior and logic 
into events such as those concerning player kills & assists. For example, take
```AsyncPlayerKillEvent```, which fires whenever a player gets a kill. In the event class
itself, two instances of ```AtomicInteger``` (to safely edit & read it across threads) represent
the gold & xp rewards that the player will receive for this kill. At initialization, those
are set to be whatever the (configurable) default reward for gold & xp is for kills. 

However, a player gets twice as much *base* rewards if the player they killed was holding a flag. Thus,
this is handled in the ```preProcess()``` method. Then, any handling of the event is allowed
to go through, which, currently, is only by the Royalty perk -- a passive ability that
increases the amount of gold the user and all of their team members get from most
sources. This is accomplished by reacting to and handling the ```AsyncPlayerKillEvent```,
and then adding to the gold/xp reward.

Finally, the ```postProcess()``` method handles adding the totaled rewards to the player's
stats, once any possible desired modifications have been finished. The appropriate messages
are then also sent out. This solidifies and protects vital aspects of event processes
from disorderly meddling, and ensures that sensitive operations like adding stats are
always done with the correct, final values.

Lastly, baking these processes into the events themselves prevents the need to perform
them instead in redundant listener classes that end up being essentially clutter.

**Advanced Event Handling w/ ```@EventReact```**

CTF 2.0's Event Framework includes the ```@EventReact``` annotation, an improved
version of Bukkit's ```@EventHandler```. Similar to ```@EventHandler```, the annotation
is only useful in a class that implements the correct interface, in this case, ```EventReactor```,
and it should be placed on a method the first and only parameter of which is a subclass of
```Event```.

At plugin startup, Reflection is then used to load all methods annotated in this way into a
cache, which is iterated over whenever any event, async or otherwise, is fired. So far, sounds
quite similar to Bukkit's system. Outlined below are the key differences.

Note: ```@EventReact``` can be used on Bukkit events to just as great effect as CTF 2.0's
events.

*Instance-Friendly Registration*

Firstly, unlike Bukkit's system, instance-heavy classes, that is to say classes
of which you expect there to be a plurality, are compatible with this Event Framework.
Take ```PlayerState.class``` for example, it's one of the two wrapper classes that exist
for players, and an instance is created for every online player. Likewise, ```PlayerState```
implements ```EventReactor```.

CTF 2.0 Event Framework supports registering new instances
of Event Reactors in real-time, and while smoothly handle loading and caching the relevant
methods as quickly as possible. The only catch is that said classes have to be
annotated with the ```@Reactor``` annotation, and have their ```ReactorPersistence```
set to ```IMPERSISTENT```. If this is done, cached methods belonging to this
instance be bound to a ```WeakReference```, and thus garbage collected once the
```PlayerState``` is no longer in use -- aka, once the player leaves the server. **(WIP)**

*Thread Control*

As a component of the ```@EventReact``` annotation, the thread upon which handling
of the given event should be done can be easily decided, regardless of the type
of event. Handling of Bukkit events can be async (generally inadvisable, but sometimes
acceptable depending on what that handling entails), while handling of Asynchronous Events
can be done on the main thread. Likewise, the value of `thread` can also be set to
`BUKKIT_ASYNC`, if desired for whatever reason, which will delegate the handling to
an asynchronous Bukkit task.

*Stage-Dependent Event Handling*

By providing one or more values of the `GameStage` enum, you can control
when the reacting method in question is even allowed to be run. This is especially
useful for the numerous event-based ability and other in-game methods that exist,
which otherwise are constantly run even during lobby phases when they're not
relevant. This, in addition to some marginal optimization benefits, helps
reduce the tedium of including checks in every event handling method to make
sure that it's the correct stage of the game.

*Intuitive Event Priority*

Lastly, the Event Framework includes its own system of Event Priority, one that,
as a result of personal taste, is an inverted version of Bukkit's (which I simply
found to be unintuitive). Currently, there are three stages of priority, `HIGH`, 
`MEDIUM` and `LOW`. And, quite simply, methods marked as `HIGH` will run first, `LOW`
last, and `MDEDIUM` in between (`MEDIUM` is also the default value if no other is specified). **(WIP)**

### Player

**Wrapper Classes**

To make management of players and player-related tasks as smooth, safe and streamlined
as possible, I divided it all into two separate wrapper classes. The first is `CTFPlayer.class`,
and the second is `PlayerState.class`. They are different in a few key ways.

`CTFPlayer` pertains to all high-level operations that are related to the 'data' of that
player, that player's connection, or essentially anything ephemeral that does not pertain
to the world or game state in any way. Example of this are loading and reading a player's
stats from the database, sending packets, sending messages & titles, displaying scoreboards
and saving the player's current scoreboard, etc.

`PlayerState`, on the other hand, is a manager of sorts for all interactions with and operations
pertain to, directly or indirectly, the actual Bukkit `Player` object. This includes
item & inventory management, teleportation, switching gamemodes, health, potion effects,
etc.

Additionally, each of the wrappers makes accessing its counterpart effortless.

**Inventory Profile System**

The Inventory Profile System is a decently rudimentary one, which can be more-or-less
summed up as integrating an enmeshment of various enum templates in order to produce
different 'inventories' for each applicable state of the game. Those inventories have an
array of default items, but first, will comb through the player's selected abilities
in order to find any replacement items from abilities that it should use. If none
are found, the default is selected for the slot.

Additionally, some abilities affect items, but don't actually give you an item.
For example, Juggernaut, which applies Protection I to a player's chestplate, whatever
that chestplate is. This exemplifies why the 'Slot' system is preferred, as it allows
multiple different areas of the code to all edit the same slot, with the resulting item
being a combined result of all item-relevant abilities that affect that slot.

## Text Display & Messaging

**Text-Building Utility**

A custom message/text-building system, 'Text', streamlines the String concatenation process
into a more intuitive factory design, using chaining methods and Java's StringBuilder. ChatColors
are relegated to methods, so no more importing the wrong ChatColor or dealing with requiring workarounds
when combining a color with a format like 'bold' at the beginning of a String.

But, Text so much more beyond that --
Text utilizes a placeholder system so that it can be smoothly used in cases where the data for a specific
part of a message is 'pending' or should be filled in, and is immediately available upon writing the message. Placeholders
come in two forms, either as a custom placeholder which maps to a provided functional interface that then provides
the value real-time, or a more primitive placeholder that is filled using a version of Java's replaceFirst() method
that I created to be usable on a StringBuilder.

Text also seamlessly supports a multi-lined message, and upon being finished, can yield either a String or String array,
depending on whether it contains multiple lines.

**Constant-Based Framework**

Using a Constant-Centric approach, CTF 2.0 has all of its messages, titles *and*
scoreboards standardized as `enum` constants. While each has its differences, they
share the common theme of using `Text` as an easy way to write sometimes lengthy
and tedious strings with numerous `ChatColor` switches.

*Messages*

Messages are the simplest implementation of this. When being defined, they simply accept
a single Text instance as an argument (remember that Text supports multiple lines
though!) Outside of the behavior offered by `Text`, they don't really have much of
their own.

*Titles*

Titles are similar to Messages, except they can take up to two `Text` objects, one
for the Title and one for the Subtitle. Either the Subtitle or the Title (but not both!)
can optionally be left blank, if desired. And, of course, the fades and stay value are
defined as well to control how long the title remains.

**Scoreboards**

Scoreboards a bit more advanced, and have a lot more of their own features to offer.
First of all, each `GameBoard` constant is defined line-by-line, with each Scoreboard
line being distinct (to the point of even having a distinct id). This allows for each
line to be surgically updated by itself, without needing to fully re-send the entire
Scoreboard. The code will also handle the tedious process of assigning a `Score` to 
each line automatically.

Additionally, some lines can be denoted as a `conditionalLine()`, which includes a
functional interface condition. The purpose of this is to allow functionality
where certain lines will exist only for certain players seeing the Scoreboard,
and not others. For example, the Scoreboard for the Lobby displays all Ability
Slots, whether or not they're filled. However, two of four Ability Slots are
locked by default, and of course, players without those slots locked gain
no utility by having their Scoreboard cluttered by them. As such, the lines
displays those slots are conditional.

### GUIs

**Page Support**

CTF 2.0's Custom GUI/Menu System includes support for pages, the required number
of which will be automatically derived from the total number of elements that need
to be displayed.

**Drawability/Backend Visualization**

This GUI system also uses an innovative method of creating new GUIs,
with the intention of making said process as simple and intuitive as possible.
Indeed, it is one that could potentially be done by someone who isn't even well-versed in Java. 

Using a 3d array approach, GUIs can be 'drawn', and any number + choice of characters can be used 
and denoted as placeholders for a certain item, or a pre-made menu button. This allows for 
very easy visualization of GUIs in your IDE without needing to constantly export the plugin and 
reload a test server or just rely on picturing it accurately in your mind.

### Entity Framework 

All Entities in CTF 2.0 are subclasses of `EntityBase<T extends Entity>`,
with the type generic being included in order to ensure type safety and
access to specific behaviors/methods that belong only to a certain subclass of
entity.

**Spawn or Render Support**

`EntityBase<T>` is written with an awareness of the danger of messy/cluttered
entity usage & spawning, as well as the potential consequences of such. As a result,
it tracks if the underlying entity has been spawned or not, *and* if it's been rendered
using packets. It will block any attempts to spawn the entity when it's already rendered
or vise versa, and likewise, it can be smoothly spawned or rendered using methods
in `EntityBase<>.class` itself.

On top of that, `EntityBase<T>` keeps a registry of all currently-rendered 'packet'
entities by entity id (rather than class, for optimized iteration). Primarily, this
is used to enable the registering of specific Packet Events concerning this entity,
as when receiving and reading a server-bound packet, for example `PacketPlayInUseEntity`,
the entity id is the only data that we can realistically use to determine
the entity in question. And because the entity doesn't actually in the world, a search
over all entities in the world won't return anything -- we instead search
this registry.

Note the annotation `@RenderOnly` denotes certain entities as being only able
to be rendered, either due to absolute limitations or the potential unwiseness
of actually spawning them into the world.

**Real-time Updating/Refreshing**

Similar to rendered Game Packets, subclasses of `EntityBase<T>` can be
'refreshed', regardless of whether they've been rendered or have actually been
spawned in the world.

**Holograms**

`Hologram` is a base class that is decently simple. It can display a single line of text,
and can have a block set as its 'hat'. But otherwise, it's purpose is largely to serve
as the building-block upon which more complex and advanced holograms are built.

*Advanced Holograms & Display Nodes*

Currently, the only advanced Hologram is `DynamicHologram`, which was created
with extreme versatility in mind. `DynamicHologram` doesn't itself display any text,
instead it serves as a nexus of sorts for any number of `DisplayNode` instances. These
are a special variety of `Hologram` which are inextricably linked to the `DisplayNode`
for which they were created. Each `DisplayNode` can take a `Vector` offset, giving
control of its position relative to the central position of the `DynamicHologram`.

Each `DisplayNode` displays a line of text, and can also wear/hold items. Likewise,
each node can be independently fetched and interacted with, or all nodes can be wiped
without destroying the central `DynamicHologram`. Best of all, support for Packet Event
interaction with a `DynamicHologram` is assured by the fact that every node's entity id
is set to the id of its host `DynamicHologram`, meaning that any clicks on the general area
of said hologram that might register on a `DisplayNode` instead, will be registered as
hits on the central hologram.

If desired, another version of `DisplayNode` could easily be created that *does* register
hits, perhaps for creating some sort of interactive in-world menu. Likewise, as a result
of the base functionality that Display Nodes inherit from being Holograms and even just
EntityBases, if a player were to be given some way to directly interact with a
`DynamicHologram` in real-time, anything from clicking, to shooting projectiles, to
looking at a `DisplayNode`, or even to sending certain chat messages, a system that
allows for direct, smooth, interactive real-time manipulation of displays could be created.

After all, any changes to the display or position of a CTF 2.0 entity can be all but
instantly shown using entity/packet updating. The only reason nothing of the sort
was implemented in this project is because it was well out of CTF's scope, but
the potential is nearly limitless.

## Other Features

I'll finish up by listing a few of the remaining features CTF 2.0
has, features that are noteworthy but don't really qualify as 
frameworks or systems in any sense, and are pretty brief.

### Connection Pooling

CTF uses a rudimentary Connection Pooling system that centers around a pool of fixed size,
includes smooth connection validation, and will smoothly handle and replace bad connections
in a way that takes advantage of multithreading and won't slow down the 
flow of the game's function.

While I conceptually understand how to go a good deal further than this in the
creation and maintainance of a Connection Pool, I would like to put a good deal
into practice by implementing (not necessarily in this project):
- A more advanced validation system that periodically verifies
the properties of a Connection as opposed to simply accepting any Connection
that is still valid
- Prevention of SQL Injection Attacks
- A monitoring and logging system
- Use of JDBC to encrypt database connections
- Consider a closed ecosystem approach to opening and accessing Connections,
such that only specific elements of the plugin are able to access them, with
the theoretical goal of preventing a compromising attack on the Spigot server instance 
from resulting in the database in turn being compromised

### Packet Event & Listener

CTF 2.0 has a basic implementation of packet interception using a ChannelDuplexHandler
to receive incoming packets. With performance and stability as a primary concern, the
packet is neither replaced nor is its 'passage' (if you will) through the Handler
slowed by burdensome logic. A reference to the packet is delegated to an alternate
thread for reading and consideration/`PacketEvent` triggering to a separate thread.

If the packet is found to be of the correct type, the corresponding `PacketEvent` is triggered
(asynchronously, of course) to which code elsewhere is able to react. In the case
specifically of `PacketPlayInUseEntity`, the obfuscated method 'a()' is first
run. This method takes a World argument and attempts to return an instance of
the clicked Entity if it finds it in the world. If it's a packet entity, it won't
be found, and the code proceeds to use Reflection to access the entity id 
which is the `int` field 'a' in `PacketPlayInUseEntity`. That id is then used
to search through the cache of rendered packet entities in `EntityBase<T>`.

### Rate Limiter

A basic Rate Limiter is implemented, at present, purely for the purpose of limiting
the number of API requests that can made for 'frivolous' reasons so as to not
reach Mojang's rate limit. Frivolous is defined as an operation that is not important for 
the operation of the minigame in any way, such as a player running the */stats* command on 
an unknown player name. Non-frivolous reasons are, currently, just loading skins for NPCs.
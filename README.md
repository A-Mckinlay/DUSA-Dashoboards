# DUSA Dashoboards
Yo, an application to analyse transaction data from YOYO and display it in an easy to interpret way.

## Requirements
- A machine that isn't a potato (x86 or x86_64, in other words, with 2GB+ RAM)
- Java Development Kit 8+ (*not* the consumer Java Runtime Environment!)
- NodeJS Current branch (for JS tooling)
- An IDE to work in (or a lot of patience and using the Gradle wrapper with an editor of your choice)
- [The Spark Bible](http://sparkjava.com/documentation) (see also the
[Mustache docs](https://mustache.github.io/mustache.5.html))

## Setting up in an IDE
### Intellij IDEA Ultimate
(If you don't have a license for it, get one [here](http://www.jetbrains.com/student/) with your university email
address - it's free! The other tools are good too. *Free!*)

1. Open Project
2. Select cloned repository (that's the folder this README is in)
3. ???
4. Profit (and work)

Once IDEA is running, you'll get a error notification about missing required plugins. Click the link in the notification
and installed the required plugins from there. After restarting IDEA, you'll get another notification about linking the
Gradle project. Click the link to import it, uncheck the option about "Seperate Source Sets" and hit Import. If you have
no JDKs defined, cancel out, go to File -> Project Structure and define a JDK, then restart IDEA and retry the import.

### Eclipse
Nope. No idea. Sorry. Searching for "Eclipse" and "Gradle" may give pointers? IDEA is recommended.

## Just let me BUILD!
In a command line/terminal in the directory;
- Windows: `.\gradlew.bat build test`
- Mac/Linux/BSD: `./gradlew build test`

Artifacts will end up in `build/libs` - using the `-all` artifact is recommended, as it packs all dependencies in for
you. No nasty classpath issues!

To skip tests, drop the last `test` section of the command. You **will** need a JDK to run this.

## Other notes
This project uses [Autorouter](https://github.com/Emberwalker/spark-autorouter). To use it, annotate your route methods
(which must be `public static` and don't have void return types) with the HTTP verb annotations, which can be imported
as:
```java
import static io.drakon.spark.autorouter.Routes.*;
```

Javadocs are accessible in IDEA once the Gradle project is imported, and basic usage docs are found on Autorouter's
Github page.

## Comms
Team Trello: https://trello.com/ac41004team9

Team Slack: https://ac41004team9.slack.com/

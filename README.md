# Gameocracy Chat — source project for Minecraft 26.1.2 / NeoForge

Prototype inspired by LuckPerms Chat. Intended to:
- read LuckPerms prefix/suffix metadata;
- add them around a player's name in server chat;
- read `username-color` and `message-color` LuckPerms metadata;
- parse legacy `&` color/style codes.

## Important status
This is a source project and automated build setup, **not a compiled or tested JAR**. It has not been compiled or tested against an actual Minecraft 26.1.2 server. The source may need API adjustments discovered during compilation.

## Build automatically on GitHub
1. Create a GitHub repository and upload the contents of this ZIP.
2. Open the repository's **Actions** tab.
3. Select **Build Gameocracy Chat** and click **Run workflow**.
4. If the build succeeds, open that workflow run and download the `gameocracychat-jar` artifact.
5. Test on a backup copy of your server first.

The workflow uses Java 25 and Gradle on GitHub's runner. It will report any compile errors; those errors must be fixed before a working JAR can be produced.

## Example LuckPerms metadata
```text
/lp group gerant meta setprefix 100 "&4&l[Gérant] "
/lp group joueur meta setprefix 10 "&2[Joueur] "
/lp group gerant meta set username-color "&6"
/lp group joueur meta set username-color "&a"
```

The prototype does not yet implement MiniMessage/hex colors. Ensure the LuckPerms API is available to the mod at runtime in your NeoForge setup.

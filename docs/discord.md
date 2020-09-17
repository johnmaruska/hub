# Discord

Discord bot runs using
[`gizmo385/discord.clj`](https://github.com/gizmo385/discord.clj).

The bot should start when the project initializes

## Major TODO:

- Reloading in the REPL makes extensions get defined again...
- (:author message) doesn't get :roles but (http/get-guild-member ...)
  does?
- some needed functions didn't get helpers made in the http ns. could
  PR them?

## Initial Setup

Local machine needs to have a `settings.json` for `discord.clj`. See
the [Settings section](#settings) below

In addition, the bot needs to be added to a server to check.
Followed along with [thomlom.dev
guide](https://thomlom.dev/create-a-discord-bot-under-15-minutes/) for
this. That guide assumes a JavaScript implementation of discord.js but
we're only concerned with the GUI parts, not the code parts.

## Settings

discord.clj assumes a `settings.json` file located in the directory
`data/settings`. This is hardcoded and cannot be changed, so that's
where it should be.

That file cannot be commited to version control because it contains a
secret token. There are solutions to load in just the token but that's
a problem for later. For now, go to the [Discord Developer
Portal](https://discordapp.com/developers/applications) and pull the
token there.

The settings file should have the following form
```
{
    "token" : "<CLIENT_TOKEN>,
    "prefix" : "!",
    "bot-name" : "<NAME_YOUR_BOT>",
    "extension-folders" : [
        "src/discord/extensions"
    ]
}
```

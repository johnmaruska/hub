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

## handler vs extension vs command

Each of these operates on a different set of messages

### Command

Commands operate on exactly one prefix -- the bot-prefix from
[Settings](#settings) concatenated with the name of the command. The
docs give the following command example

```
(bot/defcommand working
  [client message]
  "Posts the Star Wars Episode 1 'It's working' gif in the channel"
  (bot/say "https://giphy.com/gifs/9K2nFglCAQClO")
```

This command is triggered with a messag starting with `!working` in a
channel with the bot.

### Extension

Extensions are essentially collections of subcommands, like the aws
cli.

```
(bot/defextension deck [client message]
  (:init ...)
  (:shuffle ...)
  (:draw ...)
  (:peek ...))
```

For the above example, each of these commands would trigger like this
```
!deck init
!deck shuffle
!deck draw [n]
!deck peek [n]
```

One unfortunate detail about the extension implementation is that if
you `(get message :contents)` then the resulting string will still
contain the name of the subcommand.

### Handler

Handlers operate on all messages received by the bot. I haven't yet
checked if handlers can stack like middleware or if the extension or
command will still trigger.

## Filling in Missing Calls

Not all API calls have an associated helper message. Luckily the main
function powering those helpers is exposed, so it's fairly easy to
write that helper for your own app (we'll see how easy PRs are after I
try to make one for the helpers I needed).

The function we're looking for is `discord.http/discord-request`.

The function has the below singature, including docstring
```
(defn discord-request
  "General wrapper function for sending a request to one of the pre-defined Discord API endpoints.
   This function calls other helper functions to handle the following:
    - Retrieving the API endpoint to call
    - Formatting the request
    - Sending the API call
    - Deferred retries of API calls in the event of a 429 Rate Limit response

   Arguments:
   endpoint-key: A keyword that maps to a defined endpoint in endpoint-mapping
   auth: Something that implements the Authenticated protocol to auth with the Discord APIs

   Options are passed :key val ... Supported options:

   :json map - An optional JSON body to pass along with post/put/patch/delete request
   :params map - Optional query parameters to pass along with a get request
   :args list - In order (format) arguments to correctly format the endpoint from endpoint-mapping
   :constructor f - A function which is mapped over API responses to create appropriate Records."
  [endpoint-key auth & {:keys [json params args constructor] :or {constructor identity} :as opts}]
  ...)
 ```

The endpoint-key needs to exist in the `endpoint-mapping`, but it
_looks_ like all discord API endpoints are included in that mapping.

Auth is most likely just going to be your discord client which you're
given with your extension/command.

The remainder of your arguments are determined by the route you're
looking to call. As a seq of key-value pairs, not a map, you have to
pass a keyword corresponding to each endpoint parameter,
e.g. this endpoint-mapping `[:get-invite (Route. "/invite/{invite}" :get)]`
would need a call like this

    (http/discord-request :get-invite client :invite invite)

There are four specific keys mentioned in the signature. The keys
`[json params constructor]` do what it says on the tin, very
good. `args` aren't used. The args list is captured as `opts`, so
don't bother passing a list after `:args`.

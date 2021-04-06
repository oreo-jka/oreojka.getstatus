# oreojka.getstatus

Send JK3 server status information to discord.

## Dependencies

You will need to install Clojure: https://clojure.org/guides/getting_started

## Installation

Clone repository onto server or wherever the bot will run.

## Usage

### Config file

In the `resources` directory there is a file `config.edn`

```clojure
{:webhook-url ""
 :servers {}}
```

You will need to add the servers you want to track and the discord webhook you intend to use.

Like this:

```clojure
{:webhook-url "https://some-discord-webhook"
 :servers {"Example Server Name" ["123.123.123.123" 29073]}}
```

The servers key is a map. 

The keys in the map are the server name that will appear in the discord channel with the player count.

The value for each key is a vector where the first element is an IP address string and the second is a port number.

### Running it

```
cd ~/oreojka.getstatus && /usr/local/bin/clojure -M -m oreojka.getstatus
```

I set it up as a cronjob to just run the above command every 5 minutes or so.
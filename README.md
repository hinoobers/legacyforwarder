# legacyforwarder

# What is this project?
This allows you to add a legacy <1.12 server to your velocity proxy, no longer have to search for alternative results or "force" other forwarding protocols.
It allows you to use 1 forwarding protocol for every single server, as every other server supports it already.

# How does it work?
Using a modified Velocity, it tricks it having the necessary packets for this, by setting the minimum version to 1.8, so every version from 1.8 has the necessary packet.
Backend-server-side, it uses a plugin, it also tricks the existence of necessary packets, and pushes its own listener in front of everyone, to ensure compability with ViaVersion.

# How to use it?
From the releases tab, grab the Velocity jar *(velocity was built with the latest commit being `e0db25664fc82eabd9fde5aac22a2311a9765975`)*, replace your existing velocity with the jar, then add the plugin to your legacy backend (1.8) server, configure the secret key in config.yml and restart your velocity & backend server. Make sure forwarding mode in velocity settings is set to modern. To test it's working: Log in, and run /whatismyip, and ensure it does not show your local address.

# Steps to get it working: (basic steps, to see it in action)
*Prerequisites*:
* Velocity-capable server, with a open port so you can connect to it (not necessary if you're testing it on LAN)
* 1.8.8 server, recommended to use **Java 8** (for more info read the 1st step below), that is capable of loading Spigot plugins

*Steps*:
* Setup 1.8 backend server (ensure on backend server `online-mode` is set to false in server.properties, since this is 1.8, and it is quite old, it is recommended to use **Java 8** otherwise you'll get `unable to access address buffer` errors when connecting, however there is a workaround to get it to work with newer java versions, in server.properties set `use-native-transport` to false)
* Install legacyforwarder plugin from releases tab to backend server
* Setup velocity server (grab the velocity jar from releases)
* Switch `player-info-forwarding-mode` to `modern` in `velocity.toml`
* Restart your velocity server or if you already have forwarding.secret in velocity server directory, copy the key from forwarding.secret to plugin's config (plugins/legacyforwarder/config.yml)
* Restart both servers, and connect to your proxy with 1.8.9, or any newer version if you have ViaVersion installed on your backend server as a plugin

# Can i use this in production?
I wouldn't recommend so, it has its flaws, the code can be improved, i just wanted to prove it can be done.
For example, console still sees the wrong UUID, although it replaces the UUID very quickly when it receives the correct data.
I would only use it for legacy test servers on a proxy network.

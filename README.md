# legacyforwarder

# What is this project?
This allows you to add a legacy <1.12 server to your velocity proxy, no longer have to search for alternative results or "force" other forwarding protocols.
It allows you to use 1 forwarding protocol for every single server, as every other server supports it already.

# How does it work?
Using a modified Velocity, it tricks it having the necessary packets for this, by setting the minimum version to 1.8, so every version from 1.8 has the necessary packet.
Backend-server-side, it uses a plugin, it also tricks the existence of necessary packets, and pushes its own listener in front of everyone, to ensure compability with ViaVersion.

# How to use it?
From the releases tab, grab the Velocity jar *(velocity was built with the latest commit being `e0db25664fc82eabd9fde5aac22a2311a9765975`)*, replace your existing velocity with the jar, then add the plugin to your backend server, configure the secret key in config.yml and restart your velocity & backend server. To test it's working: Log in, and run /whatismyip, and ensure it does not show your local address.

# Can i use this in production?
I wouldn't recommend so, it has its flaws, the code can be improved, i just wanted to prove it can be done.
For example, console still sees the wrong UUID, although it replaces the UUID very quickly when it receives the correct data.
I would only use it for legacy test servers on a proxy network.

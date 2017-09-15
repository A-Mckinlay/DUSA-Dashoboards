# Syn
_She who guards the halls._

[Syn](https://github.com/Emberwalker/spark-syn) provides authentication services to Dashoboards. It works by installing
three routes into the active Spark application - a Before filter to catch all requests, and a login GET & POST route.

## Syn in Dashoboards
Our instance of Syn is found in `uk.ac.dundee.ac41004.team9.SecurityManager` if you need to interact with it. The login
rendering is handled in that class as well, and is on the `/login` path.

## Auth Providers
At time of writing, we're using the (very lazy) `MemeticAuthProvider` which just parrots back the last 'created' user to
Syn. This defaults (in SecurityManager) to "test"/"test". Later on, we'll probably need to implement a database-backed
`Syn.AuthProvider` class and set it up instead of the `MemeticAuthProvider`.

## Switch it off! SWITCH IT OFF!
You can disarm Syn by commenting out `syn.route();` in the `init()` method of `SecurityManager`. There might also be a
config flag by the time you read this - check the code in `SecurityManager`.
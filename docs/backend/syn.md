# Syn
_She who guards the halls._

[Syn](https://github.com/Emberwalker/spark-syn) provides authentication services to Dashoboards. It works by installing
three routes into the active Spark application - a Before filter to catch all requests, and a login GET & POST route.

## Syn in Dashoboards
Our instance of Syn is found in `uk.ac.dundee.ac41004.team9.SecurityManager` if you need to interact with it. The login
rendering is handled in that class as well, and is on the `/login` path.

## Auth Providers
There is two `Syn.AuthProvider` implementations in the `SecurityManager` - `MemeticAuthProvider` and
`DatabaseAuthProvider`. If `secUseRealDatabase` is enabled, the database-backed `DatabaseAuthProvider` is used, else the
`MemeticAuthProvider` is used which uses the last given credentials this run - which by default are "test"/"test".

## Switch it off! SWITCH IT OFF!
There's now a config flag to disable Syn, stopping it from ever registering its routes. This might be handy if doing
front-end work and don't want to constantly log back in. To disable, change `secEnable` to `false`.
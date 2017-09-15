# Configuration and You
_How to bend the elements (of Dashoboards backend) to your will._

## What configs are available
Currently the Config class manages the following variables:

- Database
    - `dbHost` (string) - DB server hostname/IP.
    - `dbPort` (int) - DB server port.
    - `dbUser` (string) - DB login username.
    - `dbPass` (string) - DB login password.
    - `dbName` (string) - The database to open in Postgres.
- Security
    - `secEnable` (bool) - Whether or not to activate Syn (the auth system).
    - `secUseRealDatabase` (bool) - Whether to use database entries for users or just the default dummy "test" account.
    
Defaults can be found in the `Config` class.

## Setting a config value
Anything not explicitly changed will use the default values. The `Config` class checks three places, in order of
priority (higher overrides lower):

- Environment variables
- `config.properties` in working directory (usually project root)
- `config.properties` on classpath (compiled in from `src/main/resources`)

Entries in `config.properties` take the form (example data):
```properties
dbHost=127.0.0.1
dbPort=5432
secEnable=true
```

Environment variables take the form `DASHCFG_` followed by the variable name, with an `_` being inserted before each
capital letter. As is convention, environment variables are **all caps**. Examples:
```
DASHCFG_SEC_USE_REAL_DATABASE="true"
DASHCFG_DB_HOST="127.0.0.1"
DASHCFG_DB_PORT="5432"
```

Environment variables can be added to your IDEA run configurations from the Edit Run Configurations screen. You can also
copy your base config and have multiple configs with different environment variables set!
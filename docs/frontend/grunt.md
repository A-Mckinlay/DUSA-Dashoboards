# Grunt and NPM
_Why's this `node_modules` folder here? Why dopes it have 30000 files in it?!_

Because Javascript is permanently inadequate and a pain to use, we've had to include one of the JS worlds favourite
tools, Grunt, to automate a few important jobs. This unfortunately means you'll need to install NodeJS and run a few
commands to get going.

## Installing NodeJS
### Windows
On the [NodeJS homepage](https://nodejs.org/en/) there is two big install buttons for your current platform. You want
the one on the right, marked 'Current' - this is the latest one, not the ancient LTS branch.

### macOS
**Homebrew time!** If you haven't encountered Homebrew, what are you doing with your (dev) life? Install it by following
the instructions on [brew.sh](https://brew.sh/), then from a shell run `brew install node` - it's that easy! Go learn
about Homebrew when you get time - it's super handy.

### Linux
Consult your package manager for what NodeJS your repos have available. If they aren't anywhere near current, you could
try [Linuxbrew](http://linuxbrew.sh/) instead, and run `brew install node` after setup like macOS users.

## Installing Grunt
From a shell, run `npm install -g grunt-cli` to install Grunt's command line in your Node install. After that, in the
Dashoboards project root, run `npm install` to pull down all the packages. Pause a moment to marvel at how much junk it
puts in `node_modules`.

## Get Up and Go
To run a one-off build, which turns all JS files in `src/main/resources/static/js` into a Babel-ised version and a
minified version of the Babel-ised version (as `name.babel.js` and `name.min.js` respectively), just run `grunt` in the
project root.

To have the generated files regenerate automatically when changes are made, run `grunt watch` in a shell and leave it
running.
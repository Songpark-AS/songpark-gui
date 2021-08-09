## Prerequisites

* NodeJS & npm (you can install it using [nvm](https://github.com/nvm-sh/nvm#installing-and-updating))
* iOS or android emulator (or a physical phone) running and connected.

## Setup

Install the node packages by running `npm i`

Setup your emulator for [iOS](https://docs.expo.dev/workflow/ios-simulator/) or [android](https://docs.expo.dev/workflow/android-studio-emulator/) and boot it up.

Follow the instructions from the original repo below on how to setup your IDE.


### NB:

In order to get the app to connect to the MQTT server you need to reverse port `8000` and have the [mqtt server from the platform repo](https://git.inonit.no/inonit/cantavi/songpark-platform/-/tree/dev/mqtt) up and running.
If you're using the android emulator (or phone) run `adb reverse tcp:8000 tcp:8000` after you have the emulator/phone running and connected.
I'm not sure if you need to do something similar with the iOS simulator but [this](https://stackoverflow.com/a/6077929) makes me think it will "just work".

### Misc about the app

When you start the app it'll immediately try to connect to MQTT on port `8000` and if you have not reversed port `8000` and it fails to connect you'll get a warning about an unhandled promise.

As of now it does not attempt to reconnect after a failure.

All of the code that handles mqtt is [here](src/main/example/mqtt.cljs).
The main views are in the [views](src/main/example/views) directory.
And the navigation bar that appears at the bottom of the screen is [here](src/main/example/navbar.cljs)

----

## Using VS Code + Calva

0. Install the [Calva](https://calva.io) extension in VS Code.
1. Open the project in VS Code. Then:
1. Run the Calva command **Start a Project REPL and Connect (aka Jack-in)**
   1. Select the project type `Hello RN Shadow`.
   1. Wait for shadow to build the project.
1. Then **Start build task**. This will start Expo and the Metro
   builder. Wait for it to fire up Expo DevTools in your browser.
   1. Click **Run in web browser**
1. When the app is running the Calva CLJS REPL can be used. Confirm this by evaluating something like: 
   ``` clojure
   (js/alert "Hello world!")
   ```
   (You should see the alert pop up where the app is running.)
1. Hack away!

Of course you should try to fire up the app on all simulators, emulators and phones you have as well. The Expo UI makes this really easy.

## Using Emacs with CIDER

Open Emacs and a bash shell:

1. Run `npx shadow-cljs compile :app` to perform an initial build of the app.
1. In Emacs open one of the files in the project (`deps.edn` is fine)
1. From that buffer, do `cider-jack-in-clojurescript` [C-c M-J] to
   launch a REPL. Follow the series of interactive prompts in the
   minibuffer:
   1. select `shadow-cljs` as the command to launch
   1. select `shadow` as the repl type
   1. select `:app` as the build to connect
   1. and optionally answer `y` or `n` to the final question about
      opening the `shadow-cljs` UI in a browser.
   At this point `shadow-cljs` will be watching the project folder and
   running new builds of the app if any files are changed. You'll also
   have a REPL prompt, *however the REPL doesn't work because it isn't
   connected to anything. The app isn't running yet.*
1. In a shell run `npm run ios` (same as `npx expo start -i`). This starts
   the Metro bundler, perform the bundling, launch the iPhone
   simulator, and transmit the bundled app. Be patient at this step as
   it can take many seconds to complete. When the app is finally
   running expo will display the message:
   
       WebSocket connected!
       REPL init successful
1. Once you see that the REPL is initalized, you can return to Emacs
   and confirm the REPL is connected and functional:
   ``` clojure
   cljs.user> (js/alert "hello world!")
   ```   
   Which should pop-up a modal alert in the simulator, confirming the
   app is running and the REPL is connected end to end.

## Using IntelliJ + Cursive REPL

1. Follow the instructions specified in [Or the Command line](#or-the-command-line).
2. Open up the project in IntelliJ by pressing `Open or Import` and opening the project root directory. 
3. Setup a project SDK by pressing `File > Project Structure`, selecting an SDK under `Project SDK` and pressing `OK`.
4. Select the REPL by pressing `Run > Edit Configurations` and selecting `Clojure REPL > REPL`. 
5. Run the REPL by pressing `Run > Run 'REPL'`. 
6. Run the commands in [Using ClojureScript REPL](#using-clojurescript-repl)

## Or the Command line
```sh
$ npm i
$ npx shadow-cljs watch app
# wait for first compile to finish or expo gets confused 
# on another terminal tab/window:
$ npm start
```
This will run Expo DevTools at http://localhost:19002/

To run the app in browser using expo-web (react-native-web), press `w` in the same terminal after expo devtools is started.
This should open the app automatically on your browser after the web version is built. If it does not open automatically, open http://localhost:19006/ manually on your browser.

Note that you can also run the following instead of `npm start` to run the app in browser:
   ```
   # same as npx expo start --web
   $ npm run web
   
   # or
   
   # same as npx expo start --web-only
   $ npm run web-only
   ```

### Using ClojureScript REPL
Once the app is deployed and opened in phone/simulator/emulator/browser, connect to nrepl and run the following:

```clojure
(shadow/nrepl-select :app)
```

To test the REPL connection:

```clojure
(js/alert "Hello from Repl")
```

### Command line CLJS REPL

Shadow can start a CLJS repl for you, if you prefer to stay at the terminal prompt:

```bash
$ npx shadow-cljs cljs-repl :app
```

## Disabling Expo Fast Refresh

You will need to disable **Fast Refresh** provided by the Expo client, which conflicts with shadow-cljs hot reloading. You really want to use Shadow's, because it is way better and way faster than the Expo stuff is.

For the iOS and Android there is a **Disable Fast Refresh** option in the [development menu](https://docs.expo.io/workflow/debugging/#developer-menu). Sometimes you need to first enble it and then disable it.

For the web app there is, afaik, no way to disable the Live Reload. There used to be a way could block it, mentioned at [https://github.com/thheller/reagent-expo](https://github.com/thheller/reagent-expo), but it doesn't seem to work with newer Expo versions.

### Live Reload, Hot Reload, Fast Refresh...

It's complicated. Expo's Fast Refresh has gone through several changes. First there were only **Live Reload**, which is an old school reload of the full app, albeit automatic. Then came **Hot Reload** which lived side by side with the live reload, but was mutually exclusive. Hot reload is faster and smarter. Presumably it can keep state between reloads in vanilla React Native projects. Both have lately been replaced with **Hot Refresh**. Except for when developing Web apps, when you have the the old **Live Reload**, and can't disable it.

## Production builds

A production build invloves first asking shadow-cljs to build a relase, then to ask Expo to work in Production Mode.

1. Kill the watch and expo tasks.
1. Execute `shadow-cljs release app`
1. Start the expo task (as per above)
   1. Enable Production mode.
   1. Start the app.

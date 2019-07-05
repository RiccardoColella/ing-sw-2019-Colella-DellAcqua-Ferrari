### How to run the applications

#### Starting manually

Once you have built the JARs via the Maven package command, the target folder will contain
the two applications, named:
- client.jar-jar-with-dependencies.jar
- server.jar-jar-with-dependencies.jar

The server JAR contains all the needed dependencies and default configurations. To run it
just open a terminal (with "target" as the current working directory) and type the following:
`java -jar ./server.jar-jar-with-dependencies.jar`

The client JAR contains all the dependencies but JavaFX, that has been marked as "provided"
in the POM file. To add it at runtime, open the terminal (with "target" as the current working directory)
and type one of the following (replacing PATH/TO with the location of javafx on your machine):
1. `java --module-path ./PATH/TO/javafx-sdk-12.0.1/lib --add-modules=javafx.controls,javafx.fxml -jar ./client.jar-jar-with-dependencies.jar`
1. `java --module-path ./PATH/TO/javafx-sdk-12.0.1/lib --add-modules=javafx.controls,javafx.fxml -jar ./client.jar-jar-with-dependencies.jar cli`

The 1. will start a GUI client, while 2. will start a CLI client

#### Starting with our scripts

We made a bunch of scripts to help ourselves during development. Those scripts can be found
in the root of the project and contains the same commands reported above.
We created those scripts for both Windows and Unix-like platforms, the former have a `.bat` extension.  
**They are meant to be executed having the project root as the working directory and the `javafx-sdk-12.0.1` folder
inside the target directory.**

To run the server type:
- `./run_server` (or `./run_server.bat` on Windows)
    
To run the client type:
- `./run_client_gui` (or `./run_client_gui.bat` on Windows) for the GUI      
- `./run_client_cli` (or `./run_client_cli.bat` on Windows) for the CLI


#### Changing the default settings

If you want to change the application behaviour you can edit the configuration files.
On its first start, both the client and the server will create, in the current working directory
of your environment, a config folder that will contain the loaded configuration files.
The files in this folder have precedence over the defaults.
Please note that the configuration files are created at runtime when they are first
needed by the two applications, that means that to have the applications export all
the possible configuration files you will have to start a match, so that all the match
specific files are loaded, otherwise you can always open the JARs as archives (or open the
resources directory in the project folder) and manually copy the configuration files in the "config"
directory.
##### WARNING: manually editing the configuration files may lead to  undefined behaviour or crashing if it's not done correctly, especially in the client where resource file names depend on the server configuration


#### Server configuration parameters

The server main configuration file contains the following parameters:

|Name|Type|Range|Description|
|----|----|-----|-----------|
|maxParallelMatches|int|[1, N]|The maximum amount of parallel matches the server shall handle
|matchStartTimeout|int|[0, N]|The timeout (in milliseconds) the server have to wait before starting a match that satisfies the minimum amount of players required but has't reached the maximum
|clientAnswerTimeout|int|[0, N]|The maximum timeout (in milliseconds) the server shall wait for a client response before considering the client disconnected 
|minClients|int|[1, N]|The minimum number of clients that a match shall contain
|maxClients|int|[minClients,N]|The maximum number of clients that a match shall contain
|rmiPort|int|[1,65535]|The port to use for the RMI registry
|socketPort|int|[1,65535]\\{rmiPort}|The port to use for listening for incoming connection
|rmiHostname|string|any|The value to set to the system property "java.rmi.server.hostname"

All those parameters can be found in the serverConfig.json file, both in the resource folder (defaults) and the config folder that will be created
on first start.
In addition to that, the server can be instantiated with commandline parameters that will have precedence over the ones contained in the JSON file.
To set these parameters from the terminal you have to pass them following the same order they have been reported
in the table above. You can pass only some of them using the terminal, but when you pass one parameter
you must pass all the ones that are positionally before that as  well.


#### Client configuration parameters

The client is more naive, the only parameter that can be passed via the command line is the one indicating
which user interface to start.
To start with a graphical interface you can either pass no parameter or "gui", any other parameter passed will
start the command line interface.

The client main configuration file contains the following parameters:

|Name|Type|Range|Description|
|----|----|-----|-----------|
|rmiPort|int|[1,65535]|The port to use for the RMI registry lookup
|socketPort|int|[1,65535]\\{rmiPort}|The port to use for connecting to the server

All those parameters can be found in the clientConfig.json file, both in the resource folder (defaults) and the config folder that will be created
on first start.
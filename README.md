#Prova Finale Ingegneria del Software 2019
## Gruppo AM49

- ### 10530081 Riccardo Colella ([@RiccardoColella](https://github.com/RiccardoColella))<br>riccardo.colella@mail.polimi.it
- ### 10562233 Carlo Dell'Acqua ([@carlo-dellacqua](https://github.com/carlo-dellacqua))<br>carlo2.dellacqua@mail.polimi.it
- ### 10535197 Adriana Ferrari ([@adrianaferrari](https://github.com/adrianaferrari))<br>adriana.ferrari@mail.polimi.it

| Functionality | State |
|:-----------------------|:------------------------------------:|
| Basic rules | [![RED](https://placehold.it/15/f03c15/f03c15)](#) |
| Complete rules | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Socket | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| RMI | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| GUI | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| CLI | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Multiple games | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#)|
| Persistence | [![RED](https://placehold.it/15/f03c15/f03c15)](#) |
| Domination or Towers modes | [![RED](https://placehold.it/15/f03c15/f03c15)](#) |
| Terminator | [![RED](https://placehold.it/15/f03c15/f03c15)](#) |

<!--
[![RED](https://placehold.it/15/f03c15/f03c15)](#)
[![YELLOW](https://placehold.it/15/ffdd00/ffdd00)](#)
[![GREEN](https://placehold.it/15/44bb44/44bb44)](#)
-->

---

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
They are meant to be executed having the project root as the working directory and the `javafx-sdk-12.0.1` folder
inside the target directory.

To run the server type:
- `./run_server` (or `./run_server.bat` on Windows)
    
To run the client type:
- `./run_client_gui` (or `./run_client_gui.bat` on Windows) for the GUI      
- `./run_client_cli` (or `./run_client_cli.bat` on Windows) for the CLI
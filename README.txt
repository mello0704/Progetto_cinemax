CineMax - cinema monosala
========================

Requisiti
---------
- Java Development Kit (JDK) 11 o successivo
- Eseguire i comandi dalla cartella radice del repository, quella che contiene
  le cartelle src e data.

Compilazione su Windows PowerShell
----------------------------------
New-Item -ItemType Directory -Force bin/classes | Out-Null
javac --release 11 -encoding UTF-8 -d bin/classes (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object FullName)
jar --create --file bin/CineMax.jar --main-class cinemax.Cinemax -C bin/classes .

Avvio
-----
java -jar bin/CineMax.jar

Compilazione su macOS o Linux
-----------------------------
mkdir -p bin/classes
find src -name '*.java' -print0 | xargs -0 javac --release 11 -encoding UTF-8 -d bin/classes
jar --create --file bin/CineMax.jar --main-class cinemax.Cinemax -C bin/classes .

Avvio
-----
java -jar bin/CineMax.jar

I file dati sono letti dalla cartella data relativa alla directory da cui viene
avviato il programma. Per questo motivo avviare il comando java dalla radice
del repository.

Gli utenti già presenti nel file data/Utenti.csv vengono migrati al formato
PBKDF2 con salt al primo avvio dell'applicazione. Le nuove password vengono
memorizzate già in tale formato.

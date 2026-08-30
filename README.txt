================================================================
 TheKnife - Laboratorio Interdisciplinare B, a.a. 2025/2026
 Rabuffetti Riccardo (756625, VA) - Gorla Davide (756140, VA)
 Scarselli Francesco (756661, VA)
================================================================

INDICE
------
1. Requisiti
2. Struttura del repository
3. Creazione del database dbTK
4. Compilazione con Maven
5. Import dei dati draft (michelin_my_maps.csv)
6. Avvio del server (serverTK)
7. Avvio del client (clientTK)
8. Generazione della Javadoc
9. Note su librerie non standard
10. Note e limitazioni note


1. REQUISITI
-------------
- Java JDK 21 o superiore (sviluppato e testato con Temurin 25; il progetto
  compila con --release 21 per la massima compatibilita').
- Apache Maven 3.9+ (oppure usare il Maven Wrapper se presente: ./mvnw).
- PostgreSQL 14+ in esecuzione (locale o remoto), con un utente che abbia
  permesso di creare database e tabelle.
- Connessione di rete TCP tra client e server (anche su una singola
  macchina, usando 127.0.0.1/localhost).


2. STRUTTURA DEL REPOSITORY
----------------------------
  autori.txt         - cognome, nome, matricola, sede degli autori
  README.txt          - questo file
  pom.xml              - build Maven (compilazione, jar, javadoc)
  src/main/java/       - codice sorgente, package "theknife" (con
                          sottopackage theknife.common, theknife.server,
                          theknife.client)
  src/main/resources/
    sql/schema.sql      - script di creazione del database dbTK
    fxml/, css/         - interfaccia grafica JavaFX
  bin/                  - jar eseguibili generati (theknife-server.jar,
                          theknife-client.jar)
  doc/                  - diagrammi ER/UML, manuali, javadoc generata
  lib/                  - (vuota: le dipendenze sono gestite da Maven/
                          scaricate in ~/.m2, nessuna libreria va copiata
                          manualmente)


3. CREAZIONE DEL DATABASE dbTK
--------------------------------
Con psql (o uno strumento equivalente come pgAdmin), connessi a un server
PostgreSQL raggiungibile, eseguire:

    createdb -U <utente> dbtk
    psql -U <utente> -d dbtk -f src/main/resources/sql/schema.sql

Lo script crea tutte le tabelle (utenti, ristoranti, cucine,
ristoranti_cucine, recensioni, preferiti), i vincoli, gli indici e i
trigger applicativi descritti nel Manuale Tecnico.


4. COMPILAZIONE CON MAVEN
---------------------------
Dalla cartella radice del progetto:

    mvn clean package

Il comando compila il codice, esegue i test e produce due jar eseguibili
"fat jar" (con tutte le dipendenze incluse) in target/ e li copia anche in
bin/:

    bin/theknife-server.jar   (Main-Class: theknife.server.ServerTK)
    bin/theknife-client.jar   (Main-Class: theknife.client.LauncherClient)


5. IMPORT DEI DATI DRAFT (michelin_my_maps.csv)
--------------------------------------------------
Il file fornito dal docente (michelin_my_maps.csv) puo' essere importato
nella tabella ristoranti con l'utility ImportCSV, inclusa nel jar del
server (usare -cp per invocarla al posto del Main-Class di default):

    java -cp bin/theknife-server.jar theknife.server.ImportCSV \
         /percorso/a/michelin_my_maps.csv \
         <host_db> <porta_db> <nome_db> <utente_db> <password_db>

Se si omettono gli ultimi 5 parametri, il programma chiede a console le
credenziali del database (come per l'avvio del server). L'import assegna
i ristoranti a un account ristoratore "seed" creato automaticamente
(import@theknife.local): i dati sono un punto di partenza, poi ogni
ristoratore registrato crea i propri ristoranti con aggiungiRistorante().
Le trasformazioni applicate ai dati (fascia di prezzo, nazione/citta',
delivery/prenotazione) sono documentate nei commenti Javadoc della classe
ImportCSV e nel Manuale Tecnico.


6. AVVIO DEL SERVER (serverTK)
---------------------------------
    java -jar bin/theknife-server.jar

All'avvio il server chiede a console le credenziali di accesso al
database (host, porta, nome database, utente, password) e si mette in
ascolto sulla porta 9090 di default, accettando connessioni multiple in
parallelo (un thread per client). E' possibile anche fornire i parametri
come argomenti da riga di comando, utile per script/automazione:

    java -jar bin/theknife-server.jar <host_db> <porta_db> <nome_db> \
         <utente_db> <password_db> [porta_ascolto]

Al primo avvio Windows Defender Firewall potrebbe chiedere di autorizzare
"OpenJDK Platform binary" a comunicare in rete: e' sufficiente consentire
l'accesso per le reti private per il normale funzionamento in LAN/locale.


7. AVVIO DEL CLIENT (clientTK)
---------------------------------
    java -jar bin/theknife-client.jar

Alla partenza il client chiede l'indirizzo (host e porta) del serverTK a
cui connettersi (default: localhost:9090). Da qui e' possibile accedere,
registrarsi, oppure continuare come utente ospite indicando un luogo.
E' anche possibile passare host e porta come argomenti per saltare la
finestra di dialogo:

    java -jar bin/theknife-client.jar localhost 9090


8. GENERAZIONE DELLA JAVADOC
-------------------------------
    mvn javadoc:javadoc

La documentazione viene generata in doc/javadoc/apidocs/index.html.


9. NOTE SU LIBRERIE NON STANDARD
------------------------------------
- org.postgresql:postgresql (driver JDBC per PostgreSQL)
- org.openjfx:javafx-controls / javafx-fxml (interfaccia grafica client)
- org.mindrot:jbcrypt (hashing sicuro delle password, funzione BCrypt)
- org.apache.commons:commons-csv (parsing del file CSV in ImportCSV)
Tutte risolte automaticamente da Maven, nessuna azione manuale richiesta.


10. NOTE E LIMITAZIONI NOTE
-------------------------------
- La logica applicativa (protocollo client/server, DAO, regole di
  business, autorizzazioni per ruolo) e' stata validata con una suite di
  test automatici end-to-end (25 casi funzionali + test di concorrenza con
  30 client simultanei), eseguiti durante lo sviluppo con un'istanza di
  PostgreSQL locale.
- Le schermate dell'interfaccia grafica sono state verificate tramite
  compilazione, avvio effettivo dell'applicazione (nessuna eccezione al
  lancio) e ispezione visiva della prima schermata; si raccomanda comunque
  un giro di test manuale completo dell'interfaccia (login, ricerca,
  recensioni, ecc.) prima della consegna definitiva.

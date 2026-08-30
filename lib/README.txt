Questa cartella e' intenzionalmente vuota: tutte le librerie esterne necessarie
alla compilazione e all'esecuzione (driver JDBC PostgreSQL, JavaFX, jBCrypt,
Apache Commons CSV) sono dichiarate come dipendenze nel file pom.xml e
scaricate automaticamente da Maven (e incluse nei jar eseguibili prodotti in
bin/, che sono "fat jar" auto-contenuti). Non e' quindi necessario copiare
manualmente alcuna libreria in questa cartella.

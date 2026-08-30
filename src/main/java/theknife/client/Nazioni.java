/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.client;

import java.util.List;

/**
 * Elenco delle nazioni del mondo in italiano, usato per popolare i menu a
 * tendina di registrazione e creazione ristorante (invece di dover scrivere
 * il nome in inglese a mano). Include anche alcuni territori/emirati usati
 * dal dataset Michelin come se fossero nazioni a se stanti (es. Hong Kong,
 * Macao, Abu Dhabi, Dubai), per restare coerenti con i dati gia' importati.
 */
public final class Nazioni {

    public static final List<String> ELENCO = List.of(
            "Abu Dhabi", "Afghanistan", "Albania", "Algeria", "Andorra", "Angola",
            "Antigua e Barbuda", "Arabia Saudita", "Argentina", "Armenia", "Australia",
            "Austria", "Azerbaigian", "Bahamas", "Bahrein", "Bangladesh", "Barbados",
            "Belgio", "Belize", "Benin", "Bhutan", "Bielorussia", "Birmania", "Bolivia",
            "Bosnia ed Erzegovina", "Botswana", "Brasile", "Brunei", "Bulgaria",
            "Burkina Faso", "Burundi", "Cambogia", "Camerun", "Canada", "Capo Verde",
            "Ciad", "Cile", "Cina", "Cipro", "Colombia", "Comore", "Corea del Nord",
            "Corea del Sud", "Costa d'Avorio", "Costa Rica", "Croazia", "Cuba",
            "Danimarca", "Dominica", "Dubai", "Ecuador", "Egitto", "El Salvador",
            "Emirati Arabi Uniti", "Eritrea", "Estonia", "Eswatini", "Etiopia", "Figi",
            "Filippine", "Finlandia", "Francia", "Gabon", "Gambia", "Georgia",
            "Germania", "Ghana", "Giamaica", "Giappone", "Gibuti", "Giordania",
            "Grecia", "Grenada", "Guatemala", "Guinea", "Guinea Equatoriale",
            "Guinea-Bissau", "Guyana", "Haiti", "Honduras", "Hong Kong", "India",
            "Indonesia", "Iran", "Iraq", "Irlanda", "Islanda", "Isole Marshall",
            "Isole Salomone", "Israele", "Italia", "Kazakistan", "Kenya",
            "Kirghizistan", "Kiribati", "Kosovo", "Kuwait", "Laos", "Lesotho",
            "Lettonia", "Libano", "Liberia", "Libia", "Liechtenstein", "Lituania",
            "Lussemburgo", "Macao", "Macedonia del Nord", "Madagascar", "Malawi",
            "Malesia", "Maldive", "Mali", "Malta", "Marocco", "Mauritania",
            "Mauritius", "Messico", "Micronesia", "Moldavia", "Monaco", "Mongolia",
            "Montenegro", "Mozambico", "Namibia", "Nauru", "Nepal", "Nicaragua",
            "Niger", "Nigeria", "Norvegia", "Nuova Zelanda", "Oman", "Paesi Bassi",
            "Pakistan", "Palau", "Palestina", "Panama", "Papua Nuova Guinea",
            "Paraguay", "Peru'", "Polonia", "Portogallo", "Qatar", "Regno Unito",
            "Repubblica Ceca", "Repubblica Centrafricana", "Repubblica del Congo",
            "Repubblica Democratica del Congo", "Repubblica Dominicana", "Romania",
            "Ruanda", "Russia", "Saint Kitts e Nevis", "Saint Lucia",
            "Saint Vincent e Grenadine", "Samoa", "San Marino",
            "Sao Tome e Principe", "Senegal", "Serbia", "Seychelles",
            "Sierra Leone", "Singapore", "Siria", "Slovacchia", "Slovenia",
            "Somalia", "Spagna", "Sri Lanka", "Stati Uniti", "Sudafrica", "Sudan",
            "Sudan del Sud", "Suriname", "Svezia", "Svizzera", "Tagikistan",
            "Taiwan", "Tailandia", "Tanzania", "Timor Est", "Togo", "Tonga",
            "Trinidad e Tobago", "Tunisia", "Turchia", "Turkmenistan", "Tuvalu",
            "Ucraina", "Uganda", "Ungheria", "Uruguay", "Uzbekistan", "Vanuatu",
            "Vaticano", "Venezuela", "Vietnam", "Yemen", "Zambia", "Zimbabwe"
    );

    private Nazioni() {
    }
}

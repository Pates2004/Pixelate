# Weryfikacja Pixelate 1.4.3 — 2026-11-13

APK release, versionCode 11, minSdk 28/Android 9, targetSdk 35. Certyfikat SHA-256: `1645b557bf78532e3e82c073316eb4f6fda7f33a457928f111e096e7b6bb8242` (ten sam co poprzednio). SHA-256 końcowego APK: `64B98A3A6E224DD56F916EB301C4D35A25320B3FB8839A34F8E833CFAEA88525`.

## Zgłoszenie i ustalenia

Użytkownik opisał jednorazowy brak nakładki po listopadowym Pixel Drop na fizycznym Pixelu 11 z Androidem 17. Otwarcie Pixelate przywróciło działanie. Brak logów tego zdarzenia uniemożliwia jednoznaczne ustalenie jego przyczyny.

Odtworzono natomiast podobny objaw w kontrolowanym ekranie: na 1.4.2 gotowość drzewa dostępności opóźniona o 1500 ms nie powodowała pokazania przycisku. Otwarcie Activity i powrót przywracały nakładkę. Usługa była połączona, ale odbierała już tylko zdarzenia okna. Baseline sprawdzono po zakończeniu początkowego bindowania, które mogłoby ukryć błąd. Poprawiona wersja odzyskuje widok samodzielnie. Testowano API 35 i 37, a regresję także na API 28.

Znaleziono i poprawiono również:

- Stary Pixel Launcher na API 28 pozostawia ogólny tytuł Pixel Launcher w Ostatnich. Potwierdzono widoczny overview_panel i karty przy tym tytule. Dodano rozpoznawanie jawnego widocznego kontenera w aktywnym oknie launchera. Home i szuflada aplikacji nie pokazują nakładki.
- Akcje przewijania starej karuzeli ponawiane co 135 ms nie pozwalały jej zakończyć ruchu. Ręczne akcje z odstępem 600 ms docierały do CLEAR ALL. Na API 28/29 odstęp zmieniono na 600 ms, zachowując współczesną ścieżkę dla API 30+.
- Activity pokazywała disabled i wyłączony przycisk testu po faktycznym połączeniu usługi, jeżeli stało się to po onResume. Teraz stan aktualizuje callback połączenia, bez okresowego odpytywania.
- Podczas busy opis dostępności pozostawał Clear all przy tekście Clearing. Teraz obie etykiety odpowiadają stanowi.
- Po zmianie języka przycisk był po polsku, ale nazwa okna pozostawała Pixelate controls. Sprawdzono poprawioną nazwę Przyciski Pixelate oraz opisy Wyczyść wszystkie ostatnie aplikacje / Czyszczenie. Dla de-DE wraca angielski wariant zasobów. Test używał systemowego mechanizmu lokalizacji aplikacji; potem przywrócono dziedziczenie języka systemu.

## Macierz emulatorów

| System | Launcher | Czytnik i zakres |
| --- | --- | --- |
| Android 9, API 28, x86_64 | Pixel Launcher 9 | Bez czytnika i TalkBack 15.0.0.639625893; kontrolowane regresje, powroty do Ostatnich, Home i szuflada, prawdziwe czyszczenie |
| Android 15, API 35, x86_64 | Pixel Launcher 15 | Bez czytnika, TalkBack 15 i Jieshuo 20260821; regresje i wielokrotne powroty, status Activity, limity, blokada ekranu, restart procesu, języki |
| Android 17, API 37.1, x86_64, strony 16 KB | Pixel Launcher 17 | Bez czytnika, TalkBack 15 i Jieshuo 20261008; regresje i powroty, restart procesu, prawdziwe czyszczenie |

Android 17 emulator: `CP31.260623.012`, poprawki bezpieczeństwa 2026-07-05. To nie jest identyczny listopadowy firmware telefonu użytkownika. Testy nie dowodzą działania na każdym fizycznym modelu i każdej wersji launchera. Reguła modeli nadal akceptuje fizyczne Google Pixel bez listy generacji; minimum Android 9 nie zostało podniesione. Kontrolowany wyjątek emulatora rozszerzono o rzeczywiste historyczne nazwy Google SDK, bez dopuszczania takich nazw na urządzeniu uznanym za fizyczne.

## Wyniki zachowania

- Opóźnione drzewo, przerwanie feedbacku, zwykły szybki skok, odmowa, brak efektu i brak akcji: zaliczone. Fixture legacy-title również przechodzi.
- Końcowy podpisany APK sprawdzono w osobnych seriach po 3 powroty do Ostatnich z czytnikami na trzech systemach. Podczas badania wykonano też serie 5–12 powrotów oraz warianty bez czytnika. Nie utożsamiano samego enabled z bound.
- Rzeczywiste czyszczenie z końcowego APK: API 28 z TalkBackiem około 3154 ms, API 37 z Jieshuo około 406 ms; niezależna kontrola systemowej listy zadań potwierdziła zero kart aplikacji. Puste Ostatnie nie pokazują przycisku. Są to pojedyncze pomiary emulatorów, nie benchmark ani obietnica czasu na telefonie.
- Podczas dopracowania starego przewijania wykonano 25 żądań utworzenia zadań testowych. Android 9 zachował 18 rekordów łącznie z systemowymi Home. Czyszczenie trwało około 16578 ms i pozostawiło wyłącznie dwa rekordy Home. Nie przedstawiać tego jako testu 25 faktycznie zachowanych kart.
- Restart procesu Pixelate na API 35 i 37 odzyskał nakładkę bez otwierania Activity po około 2,4 i 2,1 s. To zakończenie procesu, nie force-stop aplikacji ani ręczne wyłączenie usługi.
- Limit 1 s na finalnym APK przywrócił przycisk po około 1045 ms. Limit 0 pozostawia operację aktywną; Wstecz anuluje ją bez restartu usługi. Limit zmieniano przez rzeczywiste pole ustawień i przywrócono 20 s.
- Pomiar po uspokojeniu Home na API 35: 0 dodatkowych ticków CPU procesu przez 8 s. Nie jest to pomiar energii i nie oznacza absolutnego zerowego kosztu usługi. Ponowienia są ograniczone do 2,5 s po zdarzeniu, nie stanowią stałej pętli.
- Zgodne klucze 44 tekstów polskich i angielskich; kontrola etykiet i stanu wykonana na działającym APK. Fokus na przycisku potwierdzono osobno po ustabilizowaniu widoku; natychmiastowe await sprawdza dostępność przycisku, nie musi jeszcze widzieć fokusu po jego opóźnieniu.

## Ograniczenia narzędzi i odrzucone próby

Jieshuo 20260821 nie uruchamiał się na ścisłym obrazie 16 KB: libluajava.so miała wyrównanie ELF 4096, powodując UnsatisfiedLinkError. W wydaniu 20261008 ta biblioteka nadal ma takie wyrównanie. Do testu tego czytnika na emulatorze włączono oficjalny tryb zgodności Androida (`bionic.linker.16kb.app_compat.enabled=true`, `pm.16kb.app_compat.disabled=false`). Z tym trybem Jieshuo i Pixelate działały razem. Pixelate nie zawiera bibliotek natywnych i działał na 16 KB również przed włączeniem tego trybu. Nie modyfikowano ani nie podpisywano ponownie APK czytnika. [Dokumentacja Google](https://developer.android.com/guide/practices/page-sizes).

Nie wykonano testu Jieshuo na x86_64 Androida 9. Wyniki współpracy z nim dotyczą API 35 i 37; nie stanowią certyfikacji dowolnej wersji czytnika.

Nieudane próby zachowano zamiast zaliczać je do wyników: szybkie polecenie APP_SWITCH czasami pozostawało w Settings, ekran powitalny TalkBacka zasłaniał fixture, stary dumpsys miał inny format/etykietę, a pierwsza próba edycji ustawienia wyprzedziła otwarcie Activity. Testy poprawiono, by jawnie sprawdzały warunki. Po ukończeniu tutoriala przy włączonym TalkBacku seria API 28 przeszła. Wcześniejszy test interrupt na baseline API 35 sam odzyskiwał nakładkę; nie uznano go za osobno odtworzony błąd produktu.

## Kompilacja i odtwarzanie

Gradle 8.9 / JDK 17 / SDK 35: testDebugUnitTest, lintDebug i assembleRelease — sukces. Cztery testy jednostkowe tożsamości urządzeń, zero błędów; Lint: No issues found. Testy jednostkowe nie zastępują prób usługi na emulatorach. Podpis APK v3 poprawny, bez flagi debuggable. Instalowano aktualizacyjnie na 1.4.2 i kolejne kandydaty 1.4.3.

Źródła fixture i obserwatora są w tests/android-fixture. Skrypty budowania kierują materiały do centralnego Workspace. Bieżący instalator jest w lokalnym installfiles. Surowe logi i stany niezaliczonych prób są w centralnym Workspace projektu, a stan audytu w centralnym Forme. Zidentyfikowane usterki naprawiono; dokładna przyczyna pojedynczego incydentu z fizycznego telefonu pozostaje niepotwierdzona.

> Aktualny instalator jest w lokalnym `installfiles`. Materiały diagnostyczne i wewnętrzne są w `D:\work\Workspace\App closer` i `D:\work\Forme\App closer`, a zbędne archiwalia w `D:\work\Trash\App closer`. Źródła i testy pozostają w repozytorium.

# Pixelate

Pixelate dodaje stały przycisk „Wyczyść wszystko” u dołu widoku Ostatnich aplikacji w Pixel Launcherze. Nie zastępuje launchera ani systemowej karuzeli. Działa wzrokowo bez czytnika, z TalkBackiem oraz z Jieshuo/Commentary Screen Reader. W głównym oknie pokazuje model telefonu wykryty przez Androida.

Wydanie produkcyjne uruchamia się wyłącznie na fizycznych urządzeniach, dla których producent i marka to Google, a model zaczyna się od „Pixel”. Inny telefon otrzymuje ekran o braku obsługi, a usługa dostępności sama się wyłącza. Kontrolowany emulator Google SDK stanowi wyjątek wyłącznie na potrzeby testów.

Po aktywacji przycisku aplikacja próbuje bezpośrednio pokazać systemowy ClearAllButton. Jeśli launcher nie obsługuje tego polecenia lub go nie wykona, Pixelate korzysta z dotychczasowych akcji przewijania karuzeli i gestów zapasowych. Kliknięcie następuje po ponownym odczytaniu widocznego przycisku. Nie wymaga roota, Shizuku ani wtyczki Jieshuo.

Interfejs ma dwa języki: polski jest wybierany wyłącznie dla polskiego języka systemu, a angielski stanowi wariant bazowy dla każdego innego języka.

## Wydanie

- Gotowe APK: installfiles/Pixelate-1.4.3.apk lub [wydanie na GitHubie](https://github.com/Pates2004/Pixelate/releases/tag/v1.4.3)
- Instrukcja instalacji: INSTRUKCJA.md
- Kod usługi: app/src/main/java/pl/pixelate/PixelateAccessibilityService.java
- Logika czyszczenia: app/src/main/java/pl/pixelate/ClearAllController.java
- Wykrywanie Ostatnich: app/src/main/java/pl/pixelate/OverviewDetector.java
- Nakładka: app/src/main/java/pl/pixelate/OverlayController.java

## Co wnosi 1.4.3

- Krótkie, ograniczone ponowienia rozpoznawania przy zmianie okna zapobiegają pominięciu Ostatnich, gdy ich drzewo dostępności pojawia się z opóźnieniem.
- Starszy Pixel Launcher jest rozpoznawany także wtedy, gdy okno Ostatnich nosi ogólną nazwę launchera. Wymagany jest widoczny kontener Ostatnich; sam ekran główny nie wystarcza.
- Na Androidzie 9/10 przewijanie czeka na zakończenie animacji starej karuzeli, zamiast ponawiać ten sam ruch zbyt wcześnie.
- Okno aplikacji odświeża stan po połączeniu lub zakończeniu usługi. Przycisk ma właściwą etykietę „Czyszczenie…”, a zmiana języka odświeża również nazwę okna nakładki.
- Zachowano Androida 9 jako minimum, identyfikację fizycznych Pixeli i podpis aktualizacji. Kontrolowany wyjątek emulatorów obejmuje również ich starsze nazwy.
- Sprawdzenia, zakres i ograniczenia: [weryfikacja 1.4.3](tests/VERIFICATION-1.4.3.md).

## Co wniosła 1.4.2

- Jednorazowa próba bezpośredniego pokazania systemowego przycisku przyspiesza czyszczenie długiej listy aplikacji.
- Odmowa, brak obsługi lub brak efektu uruchamiają dotychczasową metodę przewijania.
- Zachowano anulowanie operacji i ustawiony limit czasu; szybka próba nie dodaje pracy w tle poza czyszczeniem.
- Szczegóły pomiarów i testów awaryjnych: [weryfikacja 1.4.2](tests/VERIFICATION-1.4.2.md).

## Co wniosła 1.4.1

- Przerwanie czyszczenia przyciskiem Wstecz lub wyjściem z Ostatnich zawsze ponownie aktywuje przycisk.
- Dodatkowa kontrola przed pokazaniem nakładki naprawia także rzadki wyścig zdarzeń, który mógł pozostawić przycisk w stanie „Czyszczenie”.
- Wydanie zachowuje sprawdzoną metodę przewijania z 1.4.0.

## Co wniosła wersja 1.4.0

- Wszystkie ustawienia zapisują się automatycznie; przycisk zapisywania nie jest już potrzebny.
- Przycisk w Ostatnich pojawia się szybciej dzięki krótszej stabilizacji i reakcji na zdarzenia.
- Przy długiej karuzeli Pixelate wcześniej przechodzi z natywnych akcji przewijania na gest awaryjny.
- Optymalizacje pozostają zdarzeniowe i nie dodają pracy w tle.

## Co wniosła wersja 1.3.1

- Model telefonu jest pokazany jako pojedyncza informacja w głównym oknie, bez dodatkowej karty ani zakładki.

## Co wniosła wersja 1.3.0

- Dostępna sekcja „Twój telefon” / „Your phone” pokazująca rzeczywisty model zgłaszany przez Androida.
- Publiczne repozytorium projektu i gotowe wydanie APK na GitHubie.

## Co wniosła wersja 1.2

- Ustawienie motywu z wariantami zgodnym z systemem, jasnym i ciemnym.
- Pełne dopasowanie tła, kart, tekstu, kontrolek oraz pasków systemowych.
- Twarda blokada uruchamiania i usługi dostępności na fizycznych telefonach innych niż Google Pixel.

## Co wniosła wersja 1.1

- Opcjonalne przeniesienie fokusu na przycisk, domyślnie włączone dla TalkBacka i Jieshuo.
- Ukrywanie przycisku, gdy Ostatnie nie zawierają żadnej karty.
- Konfigurowalny limit szukania od 0 do 300 sekund; domyślnie 20, a 0 oznacza brak limitu.
- Tryb oszczędny: zdarzenia zmian zawartości są monitorowane tylko na ekranie Ostatnich.
- Automatyczny wybór języka polskiego lub angielskiego zgodnie z językiem systemu.
- Poprawione rozpoznawanie identyfikatorów kart Quickstep.

## Weryfikacja

Wydanie zbudowano dla Android SDK 35 i przetestowano na emulatorze Androida 15 z Pixel Launcherem (com.google.android.apps.nexuslauncher):

- Android Lint: brak problemów;
- podpis APK Signature Scheme v3: poprawny, ten sam klucz co w wersji 1.0;
- zwykłe dotknięcie bez czytnika uruchamia czyszczenie;
- z aktywnym TalkBackiem systemowy fokus dostępności przechodzi do okna Pixelate i obejmuje przycisk;
- z oficjalnym Jieshuo 20260821 systemowy fokus również przechodzi do okna Pixelate, a ramka czytnika obejmuje przycisk;
- po wyłączeniu opcji fokusu TalkBack pozostaje na karcie aplikacji;
- motyw, fokus czytnika i poprawny limit czasu zapisują się bez przycisku, a wartość spoza zakresu nie zastępuje poprzedniego ustawienia;
- w porównawczym teście siedmiu otwarć mediana pojawienia przycisku spadła z 1237 ms w 1.3.1 do 1033 ms w 1.4.0;
- pusta lista nie pokazuje nakładki;
- ręczne usunięcie jedynej karty od razu ukrywa nakładkę;
- test z 10 zadaniami potwierdził przewijanie i czyszczenie;
- przerwanie czyszczenia przyciskiem Wstecz natychmiast po aktywacji oraz w trakcie pierwszego przewinięcia pozostawiło przycisk aktywny przy ponownym wejściu do Ostatnich;
- limit 1 sekundy przerwał szukanie i przywrócił aktywny przycisk;
- poza Ostatnimi usługa nasłuchuje tylko zmian okna; pomiar wykazał 0% CPU, brak alarmów i brak blokady wybudzenia;
- manifest nie zawiera uprawnień do internetu ani WAKE_LOCK.

Powyższe pomiary historyczne dotyczą wcześniejszych wydań. Wersję 1.4.3 sprawdzono również na emulatorach Androida 9 i 17. TalkBack i Jieshuo były sprawdzane osobno; Jieshuo na obrazie 16 KB wymagał systemowego trybu zgodności swojej biblioteki natywnej. Pixelate nie zawiera bibliotek natywnych. Dokładny zakres opisuje raport 1.4.3. Testy emulatorów nie oznaczają sprawdzenia każdego fizycznego Pixela ani identycznego listopadowego firmware telefonu.

Obsługiwany zakres to Google Pixel z Androidem 9 lub nowszym i działającą usługą dostępności. Projekt nie zawęża listy modeli do wybranej generacji. Zachowanie zależy także od wersji launchera.

## Budowanie

Wymagane są JDK 17 i Android SDK 35. W katalogu projektu ustaw JAVA_HOME i ANDROID_HOME, a następnie uruchom:

    .\build_release.ps1

Skrypt wykonuje testy jednostkowe, Lint i kompilację release. Korzysta z Gradle Wrappera 8.9; można wskazać zainstalowanego Gradle 8.9 przez `-GradleExecutable`. Wynik niepodpisany i pliki robocze trafiają do centralnego `Workspace\App closer\build`, a nie do źródeł. `project_materials.ps1` wyznacza ścieżki według kategorii. Podpisany aktualny instalator należy umieścić w lokalnym `installfiles`; folder ten nie jest śledzony w Git.

Wydania aktualizacyjne muszą być podpisane tym samym kluczem co pierwsze APK.

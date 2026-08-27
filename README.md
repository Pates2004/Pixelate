# Pixelate

Pixelate dodaje stały przycisk „Wyczyść wszystko” u dołu widoku Ostatnich aplikacji w Pixel Launcherze. Nie zastępuje launchera ani systemowej karuzeli. Działa wzrokowo bez czytnika, z TalkBackiem oraz z Jieshuo/Commentary Screen Reader. W głównym oknie pokazuje model telefonu wykryty przez Androida.

Wydanie produkcyjne uruchamia się wyłącznie na fizycznych urządzeniach, dla których producent i marka to Google, a model zaczyna się od „Pixel”. Inny telefon otrzymuje ekran o braku obsługi, a usługa dostępności sama się wyłącza. Kontrolowany emulator Google SDK stanowi wyjątek wyłącznie na potrzeby testów.

Po aktywacji przycisku aplikacja korzysta najpierw z natywnych akcji dostępności karuzeli Quickstep. Gdy launcher ich nie udostępnia, wykonuje gesty przesuwania, a na końcu naciska oryginalny ClearAllButton. Nie wymaga roota, Shizuku ani wtyczki Jieshuo.

Interfejs ma dwa języki: polski jest wybierany wyłącznie dla polskiego języka systemu, a angielski stanowi wariant bazowy dla każdego innego języka.

## Wydanie

- Gotowe APK: dist/Pixelate-1.5.0.apk
- Instrukcja instalacji: INSTRUKCJA.md
- Kod usługi: app/src/main/java/pl/pixelate/PixelateAccessibilityService.java
- Logika czyszczenia: app/src/main/java/pl/pixelate/ClearAllController.java
- Wykrywanie Ostatnich: app/src/main/java/pl/pixelate/OverviewDetector.java
- Nakładka: app/src/main/java/pl/pixelate/OverlayController.java

## Co wnosi 1.5.0

- Pixelate sprawdza, czy natywna akcja naprawdę przesunęła karuzelę, zamiast polegać wyłącznie na odpowiedzi launchera.
- Po dwóch akcjach bez postępu automatycznie przechodzi na gest awaryjny; limit 20 akcji pozostaje zabezpieczeniem dla launcherów, które nie ujawniają położenia kart.
- Po geście czeka na systemowe potwierdzenie jego zakończenia i ma dodatkowy watchdog chroniący przed utknięciem.
- Normalna ścieżka, na której karuzela się przesuwa, zachowuje dotychczasową niezawodność i czasy.

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
- test z 10 zadaniami potwierdził 9 kolejnych zmian położenia karuzeli i poprawne wyczyszczenie;
- pomiar tej samej długiej karuzeli dał około 7,3–7,5 s zarówno dla 1.4.0, jak i 1.5.0; nowa logika nie spowalnia prawidłowej ścieżki, a skraca wyłącznie przypadek utknięcia;
- limit 1 sekundy przerwał szukanie i przywrócił aktywny przycisk;
- poza Ostatnimi usługa nasłuchuje tylko zmian okna; pomiar wykazał 0% CPU, brak alarmów i brak blokady wybudzenia;
- manifest nie zawiera uprawnień do internetu ani WAKE_LOCK.

TalkBack i oficjalny Jieshuo zostały sprawdzone osobno. Ostateczne potwierdzenie na konkretnym Pixelu 11 i jego wersji Pixel Launchera należy wykonać po instalacji APK.

## Budowanie

Wymagane są JDK 17 i Android SDK 35. W katalogu projektu ustaw JAVA_HOME i ANDROID_HOME, a następnie uruchom:

    .\gradlew.bat clean lintDebug assembleRelease

Wydania aktualizacyjne muszą być podpisane tym samym kluczem co pierwsze APK.

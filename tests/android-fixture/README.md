# Kontrolowany ekran testowy

Wyłącznie do emulatora. Nie instalować jako aplikacji użytkowej. Pakiet pl.pixelate.testlauncher wystawia drzewo podobne do Ostatnich, aby sprawdzić rzeczywistą usługę Pixelate w kontrolowanych warunkach.

Ustaw JAVA_HOME (JDK 17) i ANDROID_HOME (SDK z platformą i build-tools 35), następnie uruchom `tests/android-fixture/build.ps1`. Diagnostyczny APK i pliki pośrednie trafiają do `D:\work\Workspace\App closer\android-fixture-build`; nie są częścią wydania ani folderu installfiles.

Zainstaluj fixture.apk na emulatorze z włączonym Pixelate i uruchom:

```text
adb shell am start -n pl.pixelate.testlauncher/.FixtureActivity --es mode noop
```

Naciśnij przycisk nakładki Pixelate. Odczytaj zdarzenia przez `adb logcat -s PixelateFixture:I`.

- success: jedno REVEAL, brak SCROLL, CLICK visible=true.
- reject: jedno REVEAL zwracające false, następnie SCROLL i CLICK visible=true.
- noop: jedno REVEAL zwracające true bez skutku, następnie SCROLL i CLICK visible=true.
- unsupported: brak REVEAL, następnie SCROLL i CLICK visible=true.
- stuck: REVEAL bez skutku i nieskuteczne SCROLL; brak CLICK. Użyj do testu limitu czasu oraz Wstecz. Po przerwaniu uruchom success i sprawdź możliwość kliknięcia bez restartu Pixelate.
- delayed: widoczność węzłów jest udostępniana po 1500 ms, a później wysyłana jest tylko zmiana treści. Nakładka powinna pojawić się bez otwierania Pixelate.
- interrupt: po 1500 ms żąda przerwania informacji zwrotnej dostępności; sprawdza odzyskanie nakładki.
- legacy-title: okno ma tytuł Pixel Launcher, ale zawiera widoczny kontener Ostatnich; powinno być rozpoznane i wyczyszczone.

Każdy CLICK zamyka zadanie fixture. Fixture nie usuwa innych aplikacji. W trybie stuck przerwij Wstecz albo zamknij tę aplikację testową przed kolejną próbą.

## Powtarzalne testy usługi

`reliability.ps1 -Serial emulator-5554 -Reader talkback` uruchamia testy po zbudowaniu i zainstalowaniu fixture oraz podpisanego Pixelate. Działa wyłącznie na nazwanych emulatorach. Czytnik musi być zainstalowany i skonfigurowany; zakończ jego ekrany pierwszego uruchomienia przed testem. Obsługiwane warianty: none, talkback, jieshuo.

Skrypt sprawdza faktyczne połączenie usług, późne drzewo, przerwanie feedbacku, szybkie pokazanie przycisku i warianty awaryjne oraz wielokrotne wejście do prawdziwych Ostatnich i brak nakładki na Home. Sprawdza widoczny kontener Ostatnich, aby nie zaliczać nieudanego otwarcia widoku jako błędu nakładki. Przy niepowodzeniu zapisuje stan okien w centralnym Workspace. Przywraca początkowy wybór usług.

`ServiceProbe.java` działa przez shell UiAutomation z FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES. Plik DEX jest w wyjściu kompilacji fixture. Przykład po wysłaniu go do `/data/local/tmp/pixelate-probe.dex`:

```text
adb -s emulator-5554 shell "CLASSPATH=/data/local/tmp/pixelate-probe.dex app_process /system/bin ServiceProbe await"
```

Tryby await, recents, empty i activity służą do obserwacji. clear aktywuje przycisk Pixelate i czeka na zniknięcie nakładki; rzeczywiste usunięcie zadań należy sprawdzić niezależnie w systemie. busy/unlimited przerywają aktywną operację Wstecz, timeout sprawdza ponowne włączenie przycisku, a set-timeout zmienia wartość w otwartym oknie Pixelate przez ACTION_SET_TEXT. click-text pozwala zakończyć ekran pierwszego uruchomienia czytnika bez wyłączania go. targets i scroll-backward/scroll-forward pomagają diagnozować karuzelę; ostatnie dwa przewijają ekran.

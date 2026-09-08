# Kontrolowany ekran testowy

Wyłącznie do emulatora. Nie instalować jako aplikacji użytkowej. Pakiet pl.pixelate.testlauncher wystawia drzewo podobne do Ostatnich, aby sprawdzić rzeczywistą usługę Pixelate w kontrolowanych warunkach.

Ustaw JAVA_HOME (JDK 17) i ANDROID_HOME (SDK z platformą i build-tools 35), następnie uruchom `tests/android-fixture/build.ps1`. Diagnostyczny APK i pliki pośrednie trafiają do `trash/android-fixture-build`; nie są częścią wydania ani folderu installfiles.

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

Każdy CLICK zamyka zadanie fixture. Fixture nie usuwa innych aplikacji. W trybie stuck przerwij Wstecz albo zamknij tę aplikację testową przed kolejną próbą.

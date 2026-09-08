# Weryfikacja Pixelate 1.4.2

2026-09-08. Android 15, emulator Google z Pixel Launcherem versionName 15. Testowano podpisany produkcyjny APK, versionCode 10. Jieshuo+ 20260821 oraz zainstalowany TalkBack, oddzielnie. To testy jednego emulatora, nie gwarancja wyników na wszystkich Pixelach.

## Rzeczywiste czyszczenie

Ten sam zestaw sześciu kart ustawień, pomiar od aktywacji przycisku dostępności do zniknięcia nakładki. Po każdej próbie sprawdzono osobno listę zadań systemu: zero kart aplikacji.

| Wydanie / czytnik | Czas |
| --- | --- |
| 1.4.1 / bez czytnika | 4824 ms |
| 1.4.2 / bez czytnika | 552 ms |
| 1.4.2 / Jieshuo | 539 ms |
| 1.4.2 / TalkBack | 421 ms |

Są to pojedyncze próby, nie statystyczny benchmark. Test aktywacji używa ACTION_CLICK na przycisku Pixelate przy działających usługach czytników. Wcześniejsze odrębne próby fizycznie symulowanych flicków dotyczyły nawigacji Jieshuo, nie tego pomiaru pełnego czyszczenia.

## Anulowanie i limity

- Wstecz natychmiast po aktywacji: przycisk dostępny po ponownym wejściu; ponowne czyszczenie bez restartu usługi, zero pozostałych kart.
- Próba Wstecz po dodatkowych 100 ms z Jieshuo: test oczekujący przycisku do ponowienia nie przeszedł, ponieważ lista zadań była już pusta. Nie zaliczono tej próby jako dowodu anulowania. Wyjątek pochodził z narzędzia testowego, nie Pixelate.
- Aby jednoznacznie sprawdzić trwającą operację, zastosowano ekran fixture w trybie stuck, limit 0 i Jieshuo. Po 1500 ms przycisk nadal był zajęty, Wstecz usunął nakładkę, a kolejny scenariusz success wykonał się bez restartu usługi. W logu nie było kliknięcia w trybie stuck.
- Limit 1 s w trybie stuck: przycisk ponownie aktywny po 1058 ms.
- Puste Ostatnie: brak nakładki.

## Kontrolowane awarie launchera

Osobna aplikacja tests/android-fixture działa jako ekran przypominający Ostatnie. Testowany kod Pixelate nie był podmieniany ani wyposażany w testowe przełączniki. Log PixelateFixture potwierdził:

| Tryb | Żądania pokazania | Przewinięcia | Kliknięcie po ujawnieniu |
| --- | --- | --- | --- |
| success | 1 | 0 | tak |
| reject (akcja zwraca false) | 1 | 1 | tak |
| noop (true bez skutku) | 1 | 1 | tak |
| unsupported (brak akcji) | 0 | 1 | tak |

Fixture nie sprawdza usuwania prawdziwych zadań — do tego służą próby na Pixel Launcherze opisane wyżej. Sprawdza decyzje i powrót do starego przewijania w faktycznej usłudze Pixelate.

## Kompilacja

`gradlew.bat testDebugUnitTest lintDebug assembleRelease`: sukces; 3 istniejące testy jednostkowe, bez błędów; Android Lint: No issues found. Testy jednostkowe dotyczą obsługi urządzeń, nie nowej akcji dostępności. Nową ścieżkę sprawdzono testami emulatora opisanymi powyżej.

Certyfikat SHA-256: 1645b557bf78532e3e82c073316eb4f6fda7f33a457928f111e096e7b6bb8242. Zainstalowano 1.4.2 jako aktualizację 1.4.1.

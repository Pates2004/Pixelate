# Test Jieshuo i szybkiego dotarcia do Clear all

Raport badań poprzedzających wdrożenie 1.4.2. Poniższe stwierdzenia o niezmienionym kodzie dotyczą etapu badań; późniejszą implementację i jej sprawdzenie opisuje [weryfikacja 1.4.2](tests/VERIFICATION-1.4.2.md).

Data: 2026-09-08. Pixelate: 1.4.1, źródła bez zmian.

Środowisko: emulator PixelateTest, Android 15 (API 35), Google Pixel Launcher versionName 15. Jieshuo+ com.nirenr.talkman, wydanie 20260821 pobrane z https://github.com/nirenr/jieshuo/releases/tag/20260821. Kreator skonfigurowany do nawigacji lewo/prawo, przeglądanie dotykowe aktywne. Osobno uruchamiano TalkBack i wariant bez czytnika.

## Gesty

Gesty wygenerowano przez urządzenie wejściowe emulatora. Osobny obserwator UiAutomation działał z FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES. Rejestrował początek/koniec rozpoznawania gestu i zmiany fokusu. Nie należy utożsamiać zwykłego adb input swipe ani zrzutu uiautomator z takim pomiarem.

Bez Pixelate kolejne flicki w lewo prowadziły z karty Settings do Jieshuo+, Settings Battery optimization i mespeak. Pierwsze dwie zmiany fokusu nastąpiły odpowiednio 10 i 8 ms po końcu rozpoznawania gestu. Nie odtworzono opisanego przez użytkownika skoku do systemowego Clear all jednym flickiem.

Z Pixelate fokus przeszedł z karty Settings (597824 ms czasu systemowego) na przycisk pl.pixelate (599067 ms), czyli po 1243 ms. Następny flick w lewo przeniósł fokus na systemowe Select. To rozróżnia systemowy przycisk od nakładki Pixelate.

## Osobna próba ACTION_SHOW_ON_SCREEN

Obserwator odnalazł com.google.android.apps.nexuslauncher:id/clear_all poza ekranem: visible=false, clickable=false, enabled=true. Węzeł udostępniał ACTION_SHOW_ON_SCREEN. Obecny Pixelate odrzuca taki węzeł przez filtr isVisibleToUser().

Wywołanie ACTION_SHOW_ON_SCREEN na tym konkretnym węźle pokazało przycisk bez kolejnych przewinięć kart. Nie wykonywano ACTION_CLICK ani czyszczenia zadań.

Wyniki od wywołania żądania do odświeżenia węzła zgłaszającego visible=true i clickable=true:

- Jieshuo: accepted=true, 19 ms.
- TalkBack: accepted=true, 10 ms.
- Bez czytnika: accepted=true, 19 ms.

Pierwsze podejście bez czytnika nie odnalazło węzła; po jednoznacznym ponownym wejściu do Ostatnich próba powiodła się. Nie zaliczono pierwszego podejścia jako udanego.

To pojedyncze pomiary w trzech konfiguracjach jednego emulatora, a nie statystyka wydajności, czas pełnego zamknięcia aplikacji ani gwarancja zakończenia animacji ekranu. Nie potwierdzają wewnętrznej implementacji Jieshuo ani zachowania konkretnego telefonu użytkownika.

## Wniosek

Warto rozważyć jednorazowe ACTION_SHOW_ON_SCREEN przed dotychczasowym przewijaniem Pixelate. Następnie należy ponownie znaleźć i sprawdzić systemowy przycisk, a dopiero wtedy go kliknąć. Brak węzła, odmowa lub brak postępu powinny uruchamiać dotychczasową metodę. Należy zachować anulowanie przez Wstecz i limit czasu oraz sprawdzić zachowanie przy pustych Ostatnich. Usługa aplikacji korzystałaby z publicznego API dostępności; root był używany wyłącznie do instrumentacji emulatora.

Nie zmieniono źródeł Pixelate i nie opublikowano wydania. Materiały diagnostyczne znajdują się w trash/2026-09-08-jieshuo-tests. Skrypty w tym katalogu są jednorazowymi narzędziami emulatora, nie częścią produktu.

## Uzupełnienie: dokładnie jeden flick (ponowny test)

Ponowny test odtworzył przejście jednym flickiem do systemowego Clear all przy jednej karcie aplikacji. Pixelate pozostawał wyłączony. Nie wywoływano ACTION_SHOW_ON_SCREEN z obserwatora w tych próbach. Używano zdarzeń dotykowych urządzenia wejściowego emulatora, potwierdzonych zdarzeniami TYPE_GESTURE_DETECTION_START/END. Wcześniejszy brak odtworzenia nie dotyczy więc wszystkich konfiguracji.

- Jedna karta Settings, nawigacja góra/dół: jeden flick w górę. Koniec rozpoznawania gestu: 234318 ms; fokus systemowego clear_all: 234845 ms. Źródło: single-task-up.log.
- Jedna karta Settings, nawigacja lewo/prawo: jeden krótki flick w lewo. Koniec rozpoznawania gestu: 307038 ms; fokus systemowego clear_all: 307540 ms. Źródło: single-task-left-fast.log.
- Dwie karty Settings i Contacts, ten sam krótki flick w lewo: fokus przeszedł z Settings na Contacts (334699 ms), nie na clear_all. Źródło: two-tasks-left-fast.log.
- Kilka kart, flick w górę: rozpoznane gesty prowadziły z Settings na Jieshuo+. Próby po 0, 300 i 800 ms dodatkowego oczekiwania po poleceniu otwarcia Ostatnich. Źródło: timing-up.log.
- Próby w lewo po 0, 100, 300 i 800 ms dodatkowego oczekiwania: pierwsze trzy nie zarejestrowały pełnego rozpoznania gestu; nie są dowodem na zachowanie Jieshuo po flicku. Ostatnia przeszła z Settings na Jieshuo+. Źródło: timing-left.log. Opóźnienia są liczone po zakończeniu polecenia ADB, nie względem pierwszej klatki Ostatnich.
- Dłuższy wariant flicka w lewo nie zawsze był rozpoznawany. Skrócony wariant z czterema krokami ruchu rejestrował początek i koniec rozpoznania, więc jego wyniki wykorzystano do porównania jednej i dwóch kart.
- Próby z wpisem preferencji use_old_previous=false oraz true na emulatorze również przeniosły fokus z Settings na Contacts. Wartości zapisano przed ponownym uruchomieniem usługi; nie zweryfikowano w kodzie zamkniętej aplikacji, czy bieżące wydanie nadal korzysta z tego klucza. Źródła: modern-previous.log i legacy-previous.log.

Zarejestrowany cel to com.google.android.apps.nexuslauncher:id/clear_all, a nie pl.pixelate. Przy jednej karcie od końca rozpoznania gestu do fokusu przycisku upłynęło około pół sekundy. Nie należy porównywać tego wprost z wcześniejszym pomiarem ACTION_SHOW_ON_SCREEN: mierzono inne operacje.

Wniosek ogranicza się do badanego emulatora: efekt jednego flicka jest realny i odtworzony w obu trybach przy jednej karcie. Bezpośredni skok ponad wieloma kartami nie został potwierdzony. Nie wyklucza to takiego zachowania na telefonie użytkownika z inną wersją launchera lub konfiguracją Jieshuo. Oryginalne preferencje Jieshuo przywrócono po próbach; kod Pixelate pozostaje bez zmian.

# Pixelate 1.5.0 — instalacja i używanie

## Aktualizacja z wcześniejszej wersji

Zainstaluj Pixelate-1.5.0.apk bez odinstalowywania poprzedniej wersji. Android zachowa ustawienia aplikacji i pozwoli na aktualizację, ponieważ wydania są podpisane tym samym kluczem. Po aktualizacji sprawdź, czy Android pozostawił usługę Pixelate włączoną.

## Pierwsza instalacja

1. Skopiuj Pixelate-1.5.0.apk na telefon, na przykład do folderu Pobrane.
2. Otwórz plik i wybierz Instaluj.
3. Jeśli Android poprosi o zgodę na instalowanie z tego źródła, zezwól aplikacji Pliki, wróć i ponownie wybierz Instaluj.
4. Otwórz Pixelate.

### Gdy Android blokuje usługę jako „ustawienie z ograniczeniami”

1. Otwórz Ustawienia systemowe, następnie Aplikacje i Pixelate.
2. Otwórz menu Więcej opcji, czyli trzy kropki w prawym górnym rogu.
3. Wybierz Zezwól na ustawienia z ograniczeniami i potwierdź kodem lub odciskiem palca.
4. Jeśli tej pozycji nie ma, pomiń ten krok.

## Włączenie usługi

1. W Pixelate wybierz Otwórz ustawienia dostępności.
2. Odszukaj Pixelate na liście pobranych lub zainstalowanych usług.
3. Włącz Używaj usługi Pixelate i zaakceptuj ostrzeżenie Androida.
4. Nie wyłączaj TalkBacka ani Jieshuo/Commentary Screen Reader. Pixelate może działać równocześnie z czytnikiem.
5. Po powrocie stan powinien brzmieć „Usługa działa i jest połączona”.

## Język

Pixelate automatycznie używa polskiego, gdy językiem systemu jest polski. Przy każdym innym języku systemowym używa angielskiego. Nie trzeba niczego ustawiać ręcznie.

## Obsługiwane urządzenia

Pixelate działa wyłącznie na fizycznych urządzeniach Google Pixel. Na innym telefonie pokaże dostępny komunikat o braku obsługi i przycisk zamknięcia. Jeśli ktoś mimo to spróbuje włączyć usługę dostępności, usługa sama się wyłączy i nie będzie pracować w tle. Emulator Google używany do testów jest jedynym wyjątkiem niebędącym fizycznym Pixelem.

W głównym oknie aplikacji pojedyncza informacja „Twój telefon” pokazuje dokładny model zgłaszany przez Androida, na przykład „Twój telefon: Pixel 11”. Nie jest to osobna karta ani zakładka. Przy angielskim języku systemu pojawia się „Your phone: Pixel 11”.

## Używanie

1. Otwórz Ostatnie aplikacje tak jak zwykle.
2. Jeśli istnieją karty aplikacji, na dole nad paskiem gestów pojawi się Wyczyść wszystko. Przy pustej liście przycisk pozostanie ukryty.
3. Z TalkBackiem albo Jieshuo domyślne ustawienie przeniesie fokus bezpośrednio na przycisk po zakończeniu animacji Ostatnich. Wykonaj dwukrotne stuknięcie. Bez czytnika wystarczy zwykłe stuknięcie.
4. Pixelate przewinie karuzelę i uruchomi prawdziwy systemowy przycisk. Karty aplikacji oraz przyciski Screenshot i Select pozostają normalnie widoczne i działają bez zmian.

Przycisk Otwórz Ostatnie i przetestuj na ekranie Pixelate pozwala szybko sprawdzić konfigurację.

## Ustawienia Pixelate

Nie ma przycisku zapisywania. Każda poprawna zmiana zapisuje się automatycznie:

- Motyw aplikacji — zapisuje się i zmienia od razu. „Zgodny z systemem” automatycznie przełącza cały ekran Pixelate między wyglądem jasnym i ciemnym. „Jasny” i „Ciemny” wymuszają wybrany wygląd niezależnie od ustawienia Androida.
- Ustaw fokus czytnika na przycisku — zapisuje się natychmiast po przełączeniu. Domyślnie włączone. Jest przeznaczone dla TalkBacka i Jieshuo/Commentary Screen Reader, ponieważ nakładka jest osobnym oknem dostępności. Wyłącz tę opcję, jeśli korzystasz wzrokowo lub wolisz odnajdywać przycisk dotykiem.
- Limit szukania przycisku — zapisuje się po zakończeniu wpisywania poprawnej wartości. Domyślnie 20 sekund, zakres od 0 do 300. Wartość 0 oznacza brak limitu. Jeśli lista jest pusta, Pixelate rozpozna to przed rozpoczęciem szukania niezależnie od tego ustawienia.

## Bateria

Pixelate nie prowadzi ciągłego skanowania. Poza ekranem Ostatnich reaguje tylko na zmianę aktywnego okna. Dokładniejsze zdarzenia zawartości włącza wyłącznie na czas wyświetlania Ostatnich, dzięki czemu zauważa także ręczne usunięcie ostatniej karty. Nie ma dostępu do internetu, nie ustawia alarmów ani zadań i nie trzyma blokady wybudzenia.

W pamięci pozostaje mała usługa dostępności, aby przycisk mógł pojawić się od razu. Z tego powodu nie obiecuję dosłownego zużycia 0 w każdej statystyce Androida, ale w teście stan jałowy miał 0% CPU, bez alarmów i bez blokady wybudzenia.

## Rozwiązywanie problemów

- Jeśli Pixelate nie występuje na liście usług albo nie daje się włączyć, wykonaj kroki dotyczące ustawień z ograniczeniami.
- Jeśli przycisk nie pojawia się mimo istniejących kart, upewnij się, że domyślnym ekranem głównym jest Pixel Launcher. Wyłącz i ponownie włącz usługę Pixelate, a potem otwórz Ostatnie.
- Jeśli Jieshuo nie przenosi fokusu, sprawdź w Pixelate, czy opcja Ustaw fokus czytnika na przycisku jest włączona, a następnie ponownie otwórz Ostatnie.
- Jeśli po aktualizacji systemu pojawi się komunikat o upływie czasu, podaj wersję Androida i Pixel Launchera. Zmiana struktury launchera może wymagać aktualizacji Pixelate.

## Prywatność i usuwanie

Pixelate działa całkowicie na urządzeniu, nie ma uprawnienia do internetu, nie zapisuje historii i niczego nie wysyła. Dostępność służy do rozpoznania widoku Ostatnich, pokazania przycisku oraz wykonania przewijania i kliknięcia.

Aby wyłączyć aplikację, wyłącz usługę Pixelate w Ustawieniach dostępności. Potem możesz ją normalnie odinstalować.

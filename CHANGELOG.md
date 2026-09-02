# Historia zmian

## 1.4.1 — 2026-09-02

- Naprawiono wyszarzony przycisk po przerwaniu trwającego czyszczenia przyciskiem Wstecz lub opuszczeniem Ostatnich.
- Wszystkie ścieżki anulowania przywracają tekst, wygląd i aktywność przycisku.
- Przed pokazaniem nakładki Pixelate dodatkowo sprawdza, czy operacja czyszczenia naprawdę nadal trwa, co zabezpiecza przed wyścigiem zdarzeń dostępności.
- Ustawiono `versionCode` 9, aby poprawkę można było zainstalować także na urządzeniach, na których wcześniej znalazły się wycofane wersje 1.5.x.

## 1.4.0 — 2026-08-27

- Usunięto przycisk zapisywania; motyw, fokus czytnika i poprawny limit czasu zapisują się automatycznie.
- Skrócono stabilizację ekranu Ostatnich z 450 do 300 ms i reakcję na zdarzenie dostępności ze 140 do 80 ms.
- Przyspieszono przejście z natywnych akcji przewijania na gest awaryjny z 44 do 20 prób.
- Zachowano bezpieczne odstępy po przewijaniu, aby nie pogarszać niezawodności na wolniejszych animacjach Pixel Launchera.

## 1.3.1 — 2026-08-23

- Uproszczono prezentację modelu telefonu do pojedynczej informacji w głównym oknie, bez dodatkowej karty lub sekcji.

## 1.3.0 — 2026-08-23

- Dodano do głównego okna dostępną sekcję pokazującą model telefonu wykryty przez Androida.
- Przygotowano projekt do publikacji w repozytorium GitHub bez lokalnych narzędzi i klucza podpisującego.

## 1.2.0 — 2026-08-23

- Dodano dostępne ustawienie motywu: zgodny z systemem, jasny i ciemny.
- Motyw obejmuje tło, karty, tekst, kontrolki oraz kolory i kontrast pasków systemowych.
- Sprawdzono trwałość ustawienia oraz automatyczną reakcję na zmianę wyglądu Androida.
- Dodano ekran braku obsługi i automatyczne wyłączenie usługi na fizycznych urządzeniach innych niż Google Pixel.

## 1.1.0 — 2026-08-23

- Dodano poprawne przejmowanie fokusu przycisku przez TalkBack i Jieshuo/Commentary Screen Reader oraz przełącznik tej funkcji; oba czytniki przetestowano osobno.
- Przycisk jest ukrywany, gdy lista Ostatnich jest pusta, także po ręcznym usunięciu ostatniej karty.
- Dodano ustawiany limit szukania od 0 do 300 sekund; 0 oznacza brak limitu.
- Ograniczono pracę w tle do zdarzeń zmian okna, a zdarzenia zawartości są włączane tylko w Ostatnich.
- Dodano polski interfejs dla polskiego języka systemu i angielski dla wszystkich pozostałych.
- Poprawiono wykrywanie kart i systemowego przycisku w strukturze Pixel Launchera.

## 1.0.0 — 2026-08-22

- Pierwsze wydanie.

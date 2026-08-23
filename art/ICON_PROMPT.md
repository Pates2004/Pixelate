# Ikona Pixelate

Źródłowy raster został wygenerowany wbudowanym narzędziem OpenAI do generowania obrazów i zapisany jako `art/pixelate-icon-source.png`. Zasób używany przez Androida znajduje się w `app/src/main/res/drawable-xxxhdpi/pixelate_smile.png`; został wyłącznie technicznie przeskalowany metodą nearest-neighbour.

Finalny prompt:

```text
Use case: logo-brand
Asset type: Android adaptive app icon foreground for an accessibility utility named Pixelate
Primary request: an extremely pixelated, friendly smiling face built entirely from large square 8-bit pixels, a literal pixel smile; original design
Subject: one centered chunky square smiley face with two square eyes and a simple smiling mouth, assembled from clearly separated square blocks
Style/medium: crisp retro pixel art, flat colors, strong silhouette, deliberately very low-resolution aesthetic, no anti-aliasing, no gradients, no 3D
Composition/framing: perfectly centered, compact shape, generous transparent padding, all important details inside the central 60 percent safe zone so Android adaptive masks do not crop them
Color palette: electric cyan and vivid violet blocks with near-black facial details and a few white highlight pixels
Constraints: genuinely transparent background; no text; no letters; no border; no phone mockup; no shadows; no watermark; no Pokémon characters, Poké Balls, trademarks, or copied game iconography; recognizable at 48 pixels
```

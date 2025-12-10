# Cultivar Image Filenames Quick Reference

This file provides a quick reference for naming cultivar images. Use this when preparing your image files.

## Image Naming Format

All cultivar images should be named: `cultivar_[name].webp`

Replace the `[name]` part with the suggested filename below.

## Complete List

| # | Cultivar Name | Suggested Filename | Full Image Name |
|---|---------------|-------------------|-----------------|
| 1 | Victory F1 | `victory_f1` | `cultivar_victory_f1.webp` |
| 2 | HOPE F1 | `hope_f1` | `cultivar_hope_f1.webp` |
| 3 | Maganda F1 | `maganda_f1` | `cultivar_maganda_f1.webp` |
| 4 | Malakas F1 | `malakas_f1` | `cultivar_malakas_f1.webp` |
| 5 | Rocky 1 F1 | `rocky_1_f1` | `cultivar_rocky_1_f1.webp` |
| 6 | Improved KS Apollo | `improved_ks_apollo` | `cultivar_improved_ks_apollo.webp` |
| 7 | Improved Pope | `improved_pope` | `cultivar_improved_pope.webp` |
| 8 | Super Pope | `super_pope` | `cultivar_super_pope.webp` |
| 9 | Maguilas | `maguilas` | `cultivar_maguilas.webp` |
| 10 | Maunlad | `maunlad` | `cultivar_maunlad.webp` |
| 11 | Mapalad | `mapalad` | `cultivar_mapalad.webp` |
| 12 | Abiona F1 | `abiona_f1` | `cultivar_abiona_f1.webp` |
| 13 | Akna F1 | `akna_f1` | `cultivar_akna_f1.webp` |
| 14 | Amari F1 | `amari_f1` | `cultivar_amari_f1.webp` |
| 15 | Anita F1 | `anita_f1` | `cultivar_anita_f1.webp` |
| 16 | Colette F1 | `colette_f1` | `cultivar_colette_f1.webp` |
| 17 | Danica F1 | `danica_f1` | `cultivar_danica_f1.webp` |
| 18 | Granger F1 | `granger_f1` | `cultivar_granger_f1.webp` |
| 19 | Janet F1 | `janet_f1` | `cultivar_janet_f1.webp` |
| 20 | Platinum F1 | `platinum_f1` | `cultivar_platinum_f1.webp` |
| 21 | Reina F1 | `reina_f1` | `cultivar_reina_f1.webp` |
| 22 | Renata F1 | `renata_f1` | `cultivar_renata_f1.webp` |
| 23 | Rubellite F1 | `rubellite_f1` | `cultivar_rubellite_f1.webp` |
| 24 | TOM-055 F1 | `tom_055_f1` | `cultivar_tom_055_f1.webp` |
| 25 | TOM-262 OP | `tom_262_op` | `cultivar_tom_262_op.webp` |
| 26 | Dalwangan Tm1 | `dalwangan_tm1` | `cultivar_dalwangan_tm1.webp` |
| 27 | Dalwangan Tm2 | `dalwangan_tm2` | `cultivar_dalwangan_tm2.webp` |
| 28 | NSIC 1999 Tm09 | `nsic_1999_tm09` | `cultivar_nsic_1999_tm09.webp` |
| 29 | Mara | `mara` | `cultivar_mara.webp` |
| 30 | AniMax 1 | `animax_1` | `cultivar_animax_1.webp` |
| 31 | AniMax 2 | `animax_2` | `cultivar_animax_2.webp` |
| 32 | Golden Globe | `golden_globe` | `cultivar_golden_globe.webp` |
| 33 | Maxxime | `maxxime` | `cultivar_maxxime.webp` |

## Placement

Place each image file in all density folders:

```
app/src/main/res/
├── mipmap-mdpi/cultivar_[name].webp
├── mipmap-hdpi/cultivar_[name].webp
├── mipmap-xhdpi/cultivar_[name].webp
├── mipmap-xxhdpi/cultivar_[name].webp
└── mipmap-xxxhdpi/cultivar_[name].webp
```

## Code Mapping

After adding images, update `CultivarImageHelper.java` with mappings like:

```java
put("Victory F1", R.mipmap.cultivar_victory_f1);
put("HOPE F1", R.mipmap.cultivar_hope_f1);
// ... etc for all cultivars
```

**Important:** The cultivar name in quotes must match **exactly** the cultivar name from the list above (including capitalization and spaces).


# Cultivar Image Mappings - Complete Reference

This document shows the complete mapping between cultivar names and their image files.

## ✅ Successfully Mapped (29 cultivars)

| # | Cultivar Name (Exact) | Image Filename | Resource ID |
|---|----------------------|----------------|-------------|
| 1 | Victory F1 | `victory_f1.jpg` | `R.drawable.victory_f1` |
| 2 | HOPE F1 | `hope_f1.jpg` | `R.drawable.hope_f1` |
| 3 | Maganda F1 | `maganda_f1.jpg` | `R.drawable.maganda_f1` |
| 4 | Malakas F1 | `malakas_f1.jpg` | `R.drawable.malakas_f1` |
| 5 | Rocky 1 F1 | `rocky_1_f1.jpg` | `R.drawable.rocky_1_f1` |
| 6 | Improved KS Apollo | `improved_ks_apollo.jpg` | `R.drawable.improved_ks_apollo` |
| 7 | Improved Pope | `improved_pope.jpg` | `R.drawable.improved_pope` |
| 8 | Super Pope | `super_pope.jpg` | `R.drawable.super_pope` |
| 9 | Maguilas | `maguilas.jpg` | `R.drawable.maguilas` |
| 12 | Abiona F1 | `abiona_f1.jpg` | `R.drawable.abiona_f1` |
| 13 | Akna F1 | `akna_f1.jpg` | `R.drawable.akna_f1` |
| 14 | Amari F1 | `amari_f1.jpg` | `R.drawable.amari_f1` |
| 15 | Anita F1 | `anita_f1.jpg` | `R.drawable.anita_f1` |
| 16 | Colette F1 | `colette_f1.jpg` | `R.drawable.colette_f1` |
| 17 | Danica F1 | `danica_f1.jpg` | `R.drawable.danica_f1` |
| 18 | Granger F1 | `granger_f1.jpg` | `R.drawable.granger_f1` |
| 19 | Janet F1 | `janet_f1.jpg` | `R.drawable.janet_f1` |
| 20 | Platinum F1 | `platinum_f1.jpg` | `R.drawable.platinum_f1` |
| 21 | Reina F1 | `reina_f1.jpg` | `R.drawable.reina_f1` |
| 22 | Renata F1 | `renata_f1.jpg` | `R.drawable.renata_f1` |
| 23 | Rubellite F1 | `rubellite_f1.jpg` | `R.drawable.rubellite_f1` |
| 24 | TOM-055 F1 | `tom_055_f1.webp` | `R.drawable.tom_055_f1` |
| 25 | TOM-262 OP | `tom_262_op.jpg` | `R.drawable.tom_262_op` |
| 26 | Dalwangan Tm1 | `dalwangan_tm1.jpg` | `R.drawable.dalwangan_tm1` |
| 27 | Dalwangan Tm2 | `dalwangan_tm2.jpg` | `R.drawable.dalwangan_tm2` |
| 28 | NSIC 1999 Tm09 | `nsic_199_tm09.png` | `R.drawable.nsic_199_tm09` ⚠️ |
| 30 | AniMax 1 | `animax_1.jpg` | `R.drawable.animax_1` |
| 31 | AniMax 2 | `animax_2.jpg` | `R.drawable.animax_2` |
| 32 | Golden Globe | `golden_globe.jpg` | `R.drawable.golden_globe` |

**⚠️ Note:** NSIC 1999 Tm09 image filename is `nsic_199_tm09.png` (has "199" not "1999" in filename)

## ❌ Missing Images (4 cultivars - will use default logo)

| # | Cultivar Name | Status |
|---|---------------|--------|
| 10 | Maunlad | Image file not uploaded |
| 11 | Mapalad | Image file not uploaded |
| 29 | Mara | Image file not uploaded |
| 33 | Maxxime | Image file not uploaded |

These cultivars will display the default logo (`R.mipmap.ic_logo`) until images are added.

## How It Works

1. **Exact Match**: The helper first tries to match the cultivar name exactly
2. **Case-Insensitive Match**: If exact match fails, it tries case-insensitive matching
3. **Default Fallback**: If no match is found, it returns the default logo

## Usage Example

```java
// Get image resource for a cultivar
int imageRes = CultivarImageHelper.getCultivarImageResource("Victory F1");
imageView.setImageResource(imageRes);

// Check if image exists
if (CultivarImageHelper.hasCultivarImage("Victory F1")) {
    // Image is available
}
```

## Image Locations

All cultivar images are located in:
```
app/src/main/res/drawable/
```

## Adding Missing Images

To add the 4 missing cultivar images:

1. Create image files with these exact names (lowercase, underscores):
   - `maunlad.jpg`
   - `mapalad.jpg`
   - `mara.jpg`
   - `maxxime.jpg`

2. Place them in `app/src/main/res/drawable/`

3. Uncomment the corresponding lines in `CultivarImageHelper.java`:
   ```java
   put("Maunlad", R.drawable.maunlad);
   put("Mapalad", R.drawable.mapalad);
   put("Mara", R.drawable.mara);
   put("Maxxime", R.drawable.maxxime);
   ```

4. Rebuild the app


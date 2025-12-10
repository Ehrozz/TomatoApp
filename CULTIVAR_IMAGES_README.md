# Cultivar Images Setup Guide

This document explains how to add cultivar images to the TomatoApp project.

## Overview

The app displays images for different tomato cultivars throughout the interface. These images are managed through the `CultivarImageHelper` utility class.

## Image File Location

Place cultivar images in the following directories:

```
app/src/main/res/
├── mipmap-mdpi/
│   └── cultivar_[name].webp
├── mipmap-hdpi/
│   └── cultivar_[name].webp
├── mipmap-xhdpi/
│   └── cultivar_[name].webp
├── mipmap-xxhdpi/
│   └── cultivar_[name].webp
└── mipmap-xxxhdpi/
    └── cultivar_[name].webp
```

**Note:** You should provide images in all density folders for optimal display quality across different screen sizes.

## Naming Convention

Image files should follow this naming pattern:
- **Format:** `cultivar_[cultivar_name].webp`
- **Replace spaces with underscores**
- **Convert to lowercase**
- **Remove special characters** (keep only letters, numbers, and underscores)
- **Replace hyphens with underscores**

### Examples

| Cultivar Name | Image Filename |
|--------------|----------------|
| Victory F1 | `cultivar_victory_f1.webp` |
| HOPE F1 | `cultivar_hope_f1.webp` |
| Improved KS Apollo | `cultivar_improved_ks_apollo.webp` |
| TOM-055 F1 | `cultivar_tom_055_f1.webp` |
| AniMax 1 | `cultivar_animax_1.webp` |
| NSIC 1999 Tm09 | `cultivar_nsic_1999_tm09.webp` |

## Image Requirements

- **Format:** WebP (recommended) or PNG
- **Recommended Size:** 
  - mdpi: 48x48 dp
  - hdpi: 72x72 dp
  - xhdpi: 96x96 dp
  - xxhdpi: 144x144 dp
  - xxxhdpi: 192x192 dp
- **Aspect Ratio:** Square (1:1)
- **Background:** Transparent (recommended) or white

## Adding Images to the Code

After adding images to the resource folders, update `CultivarImageHelper.java`:

1. Open `app/src/main/java/com/android/tomatoapp/CultivarImageHelper.java`
2. Find the `cultivarImageMap` HashMap
3. Uncomment and add mappings for your cultivars:

```java
put("Victory F1", R.mipmap.cultivar_victory_f1);
put("HOPE F1", R.mipmap.cultivar_hope_f1);
// ... etc
```

**Important:** The cultivar name in the `put()` statement must match **exactly** the cultivar name used in the app data (case-sensitive).

## Available Cultivars

The following 33 cultivars are supported in the app:

1. Victory F1
2. HOPE F1
3. Maganda F1
4. Malakas F1
5. Rocky 1 F1
6. Improved KS Apollo
7. Improved Pope
8. Super Pope
9. Maguilas
10. Maunlad
11. Mapalad
12. Abiona F1
13. Akna F1
14. Amari F1
15. Anita F1
16. Colette F1
17. Danica F1
18. Granger F1
19. Janet F1
20. Platinum F1
21. Reina F1
22. Renata F1
23. Rubellite F1
24. TOM-055 F1
25. TOM-262 OP
26. Dalwangan Tm1
27. Dalwangan Tm2
28. NSIC 1999 Tm09
29. Mara
30. AniMax 1
31. AniMax 2
32. Golden Globe
33. Maxxime

## Quick Start

1. **Prepare your images** in the required sizes and format
2. **Name them** according to the naming convention above
3. **Place them** in all mipmap density folders (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
4. **Update `CultivarImageHelper.java`** to map cultivar names to image resources
5. **Rebuild the app** to include the new images

## Helper Methods

The `CultivarImageHelper` class provides several useful methods:

- `getCultivarImageResource(String cultivarName)` - Gets the image resource for a cultivar
- `hasCultivarImage(String cultivarName)` - Checks if an image exists for a cultivar
- `getSuggestedImageFilename(String cultivarName)` - Suggests a filename for a cultivar
- `getCultivarsWithImages()` - Lists all cultivars that have images

## Default Image

If a cultivar doesn't have a specific image, the app will use the default logo (`R.mipmap.ic_logo`).

## Testing

After adding images:
1. Build and run the app
2. Navigate to Work Program selection or creation
3. Select different cultivars
4. Verify that images display correctly for cultivars with images
5. Verify that the default logo appears for cultivars without images

## Troubleshooting

**Images not showing:**
- Check that filenames match exactly (case-sensitive)
- Verify images are in all density folders
- Ensure `CultivarImageHelper.java` mappings are correct
- Clean and rebuild the project

**Wrong image displayed:**
- Verify the cultivar name in the mapping matches exactly the cultivar name in the data
- Check for typos in the mapping

## Questions?

Refer to the `CultivarImageHelper.java` source code for more details.


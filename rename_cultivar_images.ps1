# PowerShell script to rename cultivar images to valid Android resource names
# Android resource names must be: lowercase letters, numbers, and underscores only

$drawablePath = "app\src\main\res\drawable"

Write-Host "Renaming cultivar images to valid Android resource names..."
Write-Host ""

# Mapping of current filenames to new valid Android resource names
$renameMap = @{
    "Abiona F1.jpg" = "abiona_f1.jpg"
    "Akna f1.jpg" = "akna_f1.jpg"
    "Amari f1.jpg" = "amari_f1.jpg"
    "Animax 1.jpg" = "animax_1.jpg"
    "Animax 2.jpg" = "animax_2.jpg"
    "anita f1.jpg" = "anita_f1.jpg"
    "Colette f1.jpg" = "colette_f1.jpg"
    "Dalwagan Tm2.jpg" = "dalwangan_tm2.jpg"  # Fixing typo: Dalwagan -> Dalwangan
    "Dalwangan Tm1.jpg" = "dalwangan_tm1.jpg"
    "Danica F1.jpg" = "danica_f1.jpg"
    "Golden globe_.jpg" = "golden_globe.jpg"
    "Granger f1.jpg" = "granger_f1.jpg"
    "HOPE F1.jpg" = "hope_f1.jpg"
    "Improved KS Apollo_.jpg" = "improved_ks_apollo.jpg"
    "Improved Pope.jpg" = "improved_pope.jpg"
    "Janet f1.jpg" = "janet_f1.jpg"
    "Maganda F1.jpg" = "maganda_f1.jpg"
    "Maguilas_.jpg" = "maguilas.jpg"
    "Malakas F1.jpg" = "malakas_f1.jpg"
    "Mapalad.jpg" = "mapalad.jpg"
    "Mara.jpg" = "mara.jpg"
    "Maunlad.jpg" = "maunlad.jpg"
    "maxxime.jpg" = "maxxime.jpg"
    "NSIC 199 Tm09.png" = "nsic_199_tm09.png"
    "Platinum f1.jpg" = "platinum_f1.jpg"
    "Reina F1.jpg" = "reina_f1.jpg"
    "Renata f1.jpg" = "renata_f1.jpg"
    "Rocky 1 F1.jpg" = "rocky_1_f1.jpg"
    "Rubellite f1.jpg" = "rubellite_f1.jpg"
    "Super Pope.jpg" = "super_pope.jpg"
    "Tom-055 f1.webp" = "tom_055_f1.webp"
    "Tom-262 Op.jpg" = "tom_262_op.jpg"
    "victory f1.jpg" = "victory_f1.jpg"
}

$renamedCount = 0
$skippedCount = 0

foreach ($oldName in $renameMap.Keys) {
    $oldPath = Join-Path $drawablePath $oldName
    $newName = $renameMap[$oldName]
    $newPath = Join-Path $drawablePath $newName
    
    if (Test-Path $oldPath) {
        if (Test-Path $newPath) {
            Write-Host "SKIP: $newName already exists. Removing old file: $oldName" -ForegroundColor Yellow
            Remove-Item $oldPath -Force
            $skippedCount++
        } else {
            Rename-Item -Path $oldPath -NewName $newName -Force
            Write-Host "Renamed: $oldName -> $newName" -ForegroundColor Green
            $renamedCount++
        }
    } else {
        Write-Host "NOT FOUND: $oldName" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Renaming complete!"
Write-Host "Files renamed: $renamedCount"
Write-Host "Files skipped (already exists): $skippedCount"


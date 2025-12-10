# Testing Checklist for Research Features

**Project**: TomatoApp - Off-Season Tomato Planting Research  
**Date**: Testing Guide  
**Purpose**: Verify all research features are working correctly

---

## ✅ Pre-Testing Setup

- [ ] Clean build: `./gradlew clean assembleDebug`
- [ ] Install app on test device/emulator
- [ ] Ensure device has internet connection (for weather API)
- [ ] Grant location permissions (for weather data)
- [ ] Login with test account

---

## 1. Seasonal Classification Testing

### Test 1.1: Automatic Season Detection
- [ ] Create a work program with planting date in **March** (off-season)
  - Expected: Season should be detected as "Off-Season"
  - Check: `WorkProgramEntity.isOffSeason` should be `true`
  - Check: `WorkProgramEntity.season` should be "Off-Season"
  - Check: `WorkProgramEntity.seasonMonth` should be `3`

- [ ] Create a work program with planting date in **October** (on-season)
  - Expected: Season should be detected as "On-Season"
  - Check: `WorkProgramEntity.isOffSeason` should be `false`
  - Check: `WorkProgramEntity.season` should be "On-Season"
  - Check: `WorkProgramEntity.seasonMonth` should be `10`

- [ ] Create work programs in different months:
  - [ ] January (on-season)
  - [ ] June (off-season)
  - [ ] September (off-season)
  - [ ] December (on-season)

**Verification**: Check Firebase database - season fields should be automatically populated

---

## 2. Weather Data Collection Testing

### Test 2.1: Weather Data Initialization
- [ ] Create a new work program
  - Expected: Weather data should be fetched automatically
  - Check: Open Room database viewer or check logs
  - Check: `WeatherData` entry should be created for the program
  - Check: Weather data should have non-zero values (temperature, etc.)

### Test 2.2: Weather Data Updates
- [ ] Open MainActivity (app start)
  - Expected: Weather data should update for all active programs
  - Check: Logcat for "WeatherDataCollector" messages
  - Check: Weather data entries should have updated timestamps

### Test 2.3: Weather Data Persistence
- [ ] Create multiple work programs
- [ ] Close and reopen app
- [ ] Check: Weather data should persist in Room database
- [ ] Check: Running averages should be calculated correctly

**Verification**: 
- Check Room database: `weather_data` table
- Verify `daysTracked` increments
- Verify running averages are calculated

---

## 3. Yield Tracking Testing

### Test 3.1: Yield Data Input
- [ ] Navigate to Calculator activity
- [ ] Select an existing work program
- [ ] Enter yield data:
  - [ ] Actual Yield (kg/hectare): e.g., `2500`
  - [ ] Total Yield (kg): e.g., `5000`
  - [ ] Harvest Date: e.g., `2024-12-15`
- [ ] Save calculation

**Verification**:
- [ ] Check Firebase: `actualYield`, `totalYield`, `harvestDate` should be saved
- [ ] Check Room database: `WorkProgramEntity` should have yield data
- [ ] Navigate back to Analytics - yield should appear

### Test 3.2: Yield Data Display
- [ ] Go to Analytics screen
- [ ] Check: Yield data should be visible in program details
- [ ] Check: Yield should be included in CSV export

---

## 4. Research Export (CSV) Testing

### Test 4.1: Basic CSV Export
- [ ] Navigate to Analytics screen
- [ ] Click "CSV" export button
- [ ] Check: File should be created in Downloads folder
- [ ] Check: File name should be `Tomato_Research_Data_YYYYMMDD_HHMMSS.csv`
- [ ] Open CSV file in Excel/Google Sheets
- [ ] Verify columns:
  - [ ] Program ID
  - [ ] Cultivar
  - [ ] Planting Date
  - [ ] Season
  - [ ] Is Off-Season
  - [ ] Actual Yield
  - [ ] Total Yield
  - [ ] All financial metrics
  - [ ] Completion rates

### Test 4.2: CSV Export with Weather
- [ ] Navigate to Analytics screen
- [ ] Click "CSV" export button (should use `exportToCsvWithWeather`)
- [ ] Check: File name should include "With_Weather"
- [ ] Open CSV file
- [ ] Verify weather columns:
  - [ ] Avg Temperature
  - [ ] Avg Min/Max Temp
  - [ ] Total Precipitation
  - [ ] Avg Humidity
  - [ ] Days Tracked

### Test 4.3: Filtered CSV Export
- [ ] Apply season filter (e.g., "Off-Season")
- [ ] Apply cultivar filter (e.g., "Victory F1")
- [ ] Click "CSV" export
- [ ] Check: CSV should only contain filtered programs

---

## 5. Seasonal Filters Testing

### Test 5.1: Season Filter in Analytics
- [ ] Navigate to Analytics screen
- [ ] Use season filter spinner:
  - [ ] Select "All Seasons" - should show all programs
  - [ ] Select "On-Season" - should only show on-season programs
  - [ ] Select "Off-Season" - should only show off-season programs

### Test 5.2: Combined Filters
- [ ] Select cultivar filter (e.g., "Victory F1")
- [ ] Select season filter (e.g., "Off-Season")
- [ ] Check: Should only show programs matching both filters
- [ ] Charts should update to reflect filtered data

---

## 6. Season Comparison Dashboard Testing

### Test 6.1: Access Season Comparison
- [ ] Open navigation drawer
- [ ] Click "Season Comparison" menu item
- [ ] Check: `SeasonComparisonActivity` should open
- [ ] Check: Title should be "Season Comparison"

### Test 6.2: Season Comparison Display
- [ ] With programs in both seasons:
  - [ ] Check: Comparison summary should show profit difference
  - [ ] Check: Comparison summary should show yield difference
  - [ ] Check: Bar chart should display both seasons
  - [ ] Check: Individual season summaries should be visible

### Test 6.3: Empty State
- [ ] With no programs:
  - [ ] Check: Empty state message should display
  - [ ] Check: Chart should be hidden

### Test 6.4: Single Season Data
- [ ] With only on-season programs:
  - [ ] Check: Should still display correctly
  - [ ] Check: Off-season metrics should show 0 or N/A

---

## 7. Analytics Manager Testing

### Test 7.1: Seasonal Summaries
- [ ] Create programs in both seasons
- [ ] Check: `summarizeBySeason()` should return summaries for both
- [ ] Check: Summaries should have correct program counts
- [ ] Check: Summaries should calculate averages correctly

### Test 7.2: Season Comparison
- [ ] Call `compareSeasons()` with summaries
- [ ] Check: Should return `SeasonComparison` object
- [ ] Check: Profit difference should be calculated correctly
- [ ] Check: Yield difference should be calculated correctly

---

## 8. Integration Testing

### Test 8.1: End-to-End Workflow
1. [ ] Create work program (March - off-season)
   - [ ] Season auto-detected
   - [ ] Weather data collected
2. [ ] Complete tasks throughout growing season
3. [ ] Enter yield data in Calculator
4. [ ] View in Analytics with season filter
5. [ ] Export to CSV
6. [ ] View in Season Comparison dashboard

### Test 8.2: Data Persistence
- [ ] Create programs with all data
- [ ] Close app completely
- [ ] Reopen app
- [ ] Check: All data should persist
- [ ] Check: Weather data should update on app start

### Test 8.3: Multi-Program Scenario
- [ ] Create 5+ programs:
  - [ ] Mix of on-season and off-season
  - [ ] Different cultivars
  - [ ] Different planting dates
- [ ] Check: Analytics should handle all programs
- [ ] Check: Season comparison should work correctly
- [ ] Check: CSV export should include all programs

---

## 9. Error Handling Testing

### Test 9.1: Network Errors
- [ ] Disable internet connection
- [ ] Create new work program
- [ ] Check: App should handle weather API failure gracefully
- [ ] Check: Program should still be created (without weather initially)

### Test 9.2: Missing Data
- [ ] Create program without yield data
- [ ] Check: Analytics should handle missing yield (show 0 or N/A)
- [ ] Check: CSV export should handle missing data

### Test 9.3: Invalid Dates
- [ ] Try to create program with invalid date
- [ ] Check: App should validate and show error

---

## 10. Performance Testing

### Test 10.1: Large Dataset
- [ ] Create 20+ work programs
- [ ] Check: Analytics should load quickly
- [ ] Check: Season comparison should load quickly
- [ ] Check: CSV export should complete in reasonable time

### Test 10.2: Weather Updates
- [ ] Create 10+ active programs
- [ ] Open app (triggers weather update)
- [ ] Check: Weather updates should complete without blocking UI
- [ ] Check: Should handle API rate limits gracefully

---

## 11. UI/UX Testing

### Test 11.1: Navigation
- [ ] All navigation paths should work:
  - [ ] MainActivity → Analytics
  - [ ] Drawer → Season Comparison
  - [ ] Analytics → CSV Export
  - [ ] Calculator → Yield Input

### Test 11.2: Visual Feedback
- [ ] Loading indicators should show during data fetch
- [ ] Empty states should display when no data
- [ ] Error messages should be user-friendly

### Test 11.3: Charts and Visualizations
- [ ] Bar charts should render correctly
- [ ] Data should be accurate
- [ ] Colors should differentiate seasons

---

## 12. Data Validation Testing

### Test 12.1: Season Classification Accuracy
- [ ] Verify season detection for all months:
  - [ ] Jan, Feb, Oct, Nov, Dec → On-Season
  - [ ] Mar, Apr, May, Jun, Jul, Aug, Sep → Off-Season

### Test 12.2: Weather Data Accuracy
- [ ] Compare weather data with actual weather API response
- [ ] Verify running averages are calculated correctly
- [ ] Check date ranges are tracked correctly

### Test 12.3: CSV Data Accuracy
- [ ] Export CSV
- [ ] Manually verify a few rows match Firebase/Room data
- [ ] Check numeric formatting (decimals, currency)

---

## ✅ Final Verification

### Code Quality
- [ ] No compilation errors
- [ ] No linter warnings (except known ones)
- [ ] All imports are correct
- [ ] All methods are properly implemented

### Documentation
- [ ] `RESEARCH_IMPROVEMENTS.md` is up to date
- [ ] Code comments are clear
- [ ] Method documentation is complete

### Research Readiness
- [ ] All data collection features work
- [ ] All analysis features work
- [ ] All export features work
- [ ] Data is accurate and complete

---

## 🐛 Known Issues / Notes

Document any issues found during testing:

1. 
2. 
3. 

---

## 📊 Test Results Summary

**Date Tested**: _______________  
**Tester**: _______________  
**Build Version**: _______________  

**Results**:
- Total Tests: ___
- Passed: ___
- Failed: ___
- Skipped: ___

**Overall Status**: ⬜ Ready for Deployment  ⬜ Needs Fixes  ⬜ In Progress

---

## Next Steps After Testing

1. Fix any critical bugs found
2. Address performance issues if any
3. Update documentation based on findings
4. Prepare for field testing
5. Deploy to production

---

**Note**: This checklist should be completed before deploying the app for research use. All critical features should pass testing before field deployment.


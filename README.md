# GeoLookup
Converts an address into decimal degrees through Google's GeoCoding API.

## Usage
There are two ways to look up an address. First, you can make a single lookup by using the single address field. Alternatively, you can upload an Excel workbook to make the lookups in a batch. Currently only .xls and .xlsx are supported. If using the batched method, you will be prompted for an input file and then prompted to save a copy of the file with the decimal degrees appended.

### Configuration
All configuration is managed by directly editing settings.ini. 

First, add your API key under the "key" field. You can request one [here](https://developers.google.com/maps/documentation/geocoding/start).

#### Input
If uploading a workbook, the data must be on the first sheet of the workbook, it must have a header row, and the header row must be the first row of the sheet. To configure the input, match the header names of your input file to the corresponding fields in settings.ini. Your input file can have the address split up into parts (street, city, state, zip in individual cells) or the full address can be in one cell. If the latter is true, just use the fullAddressField. If fullAddressField is set, the other input field settings will be ignored unless the fullAddressField header can not be found when the workbook is parsed.

#### Output
When using the workbook upload option, you will be prompted to save the output file when the address lookups are complete. Similar to the input fields, the output field configuration settings correspond to the desired header names in your workbook. **You do not need to add the output field headers to your workbook. They will be added for you.** The output fields are always just appended after the final columns of the sheet. If you'd like the entire decimal degress output to be placed in a single cell, use the fullLocationOutputField setting. Otherwise use latOutputField and lngOutputField to place them in separate cells.

### Error Handling
When using the workbook upload option, if any addresses are unable to be parsed from the input file or return bad API output, they will be reported at the end. Addresses that return more than one location will be considered bad output, even though they return an "OK" status from the API. For each bad address returned, I'd recommend using the single lookup function to determine the cause of the error. The most likely problem is that more than one address was returned. The Google Maps API is pretty aggressive about translating the address into a usable format, so you usually won't get bad address errors. Just fuzzy matches.

### Example
This is an example input file along with a configured settings.ini file, plus the output it will produce

### Input File
| streetName                  | city          | state | zipCode |
|:--------------------------- |:------------- |:----- |:------- |
| 1600 Amphitheatre Parkway   | Mountain View | CA    | 94043   |
| 9606 North MoPac Expressway | Austin        | TX    | 78759   |

### settings.ini
```
[settings]
key = [your key here]

; [Input fields]
fullAddressField = 
streetField = streetName
cityField = city
stateField = state
zipField = zipCode

; [Output fields]
fullLocationOutputField = decimalDegrees
latOutputField = latitude
lngOutputField = longitude
```

### Output File
| streetName                  | city          | state | zipCode | decimalDegrees          | latitude   | longitude    |
|:--------------------------- |:------------- |:----- |:------- | :---------------------- | :--------- | :----------- |
| 1600 Amphitheatre Parkway   | Mountain View | CA    | 94043   | 37.4223582,-122.0844464 | 37.4223582 | -122.0844464 |
| 9606 North MoPac Expressway | Austin        | TX    | 78759   | 30.3861151,-97.7364265  | 30.3861151 | -97.7364265  |


## History
* 1.0.1
  * Fixed a bug causing an invalid error message when performing single lookups
  * Added deployed archive to repo. Will be replaced with a standard release later.
* 1.0.0
  * Initial release

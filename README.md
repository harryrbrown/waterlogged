# Waterlogged

## Download
[![Google_Play_Store_badge_EN svg](https://github.com/user-attachments/assets/15f732f9-54d4-44fa-9d3a-7dbde8691fde)](https://play.google.com/store/apps/details?id=com.hrb116.waterlogged)

## Screenshots

[<img src="https://github.com/user-attachments/assets/cbc37687-f856-4fd1-9c7f-0dbc7d1f1cd2" width="250px">](https://github.com/user-attachments/assets/cbc37687-f856-4fd1-9c7f-0dbc7d1f1cd2)
[<img src="https://github.com/user-attachments/assets/b42a7d3c-5395-40f4-a3bd-4e6e0fe1b490" width="250px">](https://github.com/user-attachments/assets/b42a7d3c-5395-40f4-a3bd-4e6e0fe1b490)
[<img src="https://github.com/user-attachments/assets/3ec67227-d43f-44d5-a485-fa47669554fe" width="250px">](https://github.com/user-attachments/assets/3ec67227-d43f-44d5-a485-fa47669554fe)
[<img src="https://github.com/user-attachments/assets/9c71db4a-d39c-4b58-b37e-1155c2b33133" width="250px">](https://github.com/user-attachments/assets/9c71db4a-d39c-4b58-b37e-1155c2b33133)
[<img src="https://github.com/user-attachments/assets/8c2fbc1d-1e44-4a79-881b-0a2331ce1db1" width="250px">](https://github.com/user-attachments/assets/8c2fbc1d-1e44-4a79-881b-0a2331ce1db1)

## Overview

Waterlogged is a Wear OS Tile that allows quick and easy recording of your water intake to Fitbit, via your watch.

## Features

- Authenticates with Fitbit through your paired device (phone), using OAuth 2.0 PKCE
- Once authenticated, the tile provides three buttons to quickly record your water intake, based upon Fitbit's three default water options - a glass of water (250ml / 8oz), a bottle of water (500ml / 16oz), and a large bottle (750ml / 24oz)
- The preset water amounts are customisable in the main app
- Additionally a progress bar around the outside of the tile indicates your progress towards your daily water goal

## Requirements

- Wear OS device paired to a phone
- Fitbit account

## Installation

Download from [the Google Play Store](https://play.google.com/store/apps/details?id=com.hrb116.waterlogged).

## Usage

1. After installing, go to the Watch app on your smartphone. Open "Tiles", tap "Add Tiles", and find Waterlogged's "Quick add water" tile. 

[<img src="https://github.com/user-attachments/assets/a73d7abf-e290-4f62-8386-34ce4912f9fc" width="500px">](https://github.com/user-attachments/assets/a73d7abf-e290-4f62-8386-34ce4912f9fc)

2. Swipe on your watch to locate your new tile. The first time using the tile, you'll need to authenticate with Fitbit. Follow the steps on the watch and, when prompted, open your phone to complete the web sign-in.
3. After successful authentication, the tile should now present to you the three buttons for quick recording water. You're ready to go!
4. To edit the preset values, open the **app** on your watch (not the tile) and tap "Edit presets".
5. If you change between oz and ml on your Fitbit account, you'll need to resync the user data to have this reflected in the tile. Open the **app** on your watch (not the tile) and tap "Refetch user data" (note that this will clear your presets).

## Privacy and security

The app does not store your Fitbit account information. The only details saved on-device are your access tokens once authenticated with Fitbit. These are required to maintain connection with Fitbit's APIs. They never leave your device.

## Contributions

All contributions are welcome! If you'd like to contribute, please fork the repository and make changes as you'd like. Pull requests are welcomed.

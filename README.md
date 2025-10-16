# PixelCarrierSettings

Enable VoLTE for carriers in unsupported regions on Pixel devices.

Originally this app was made because I want to override specific configurations in one click (5G SA and signal thresholds). As of writing this, using `setImsProvisioningInt` (from [Ims](https://github.com/vvb2060/Ims)'s version 1.0, it doesn't have an UI) seems to be the only way to set VoLTE enabled persistently (explained below in "Override configurations" section), so I updated my app and made an UI for it.

## Usage

### Enable VoLTE

1. Install and start [Shizuku](https://github.com/RikkaApps/Shizuku)
2. Install this app, start it, and grant Shizuku permission
3. Tap on Enable VoLTE
4. Go to system settings - Network & internet - SIMs - select the one you have enabled VoLTE for - turn on VoLTE (or 4G calling, depending on the carrier config)

- You may need to turn on VoLTE in system settings again if you reboot the system after using this method for the first time. Then it should stay on and there is no need to do it again for the same SIM card. Unlike `overrideConfig`, this will not be reset after reboots or system updates.

### Override configurations

From "Config overrides" menu, you can override carrier configurations (the same as what Pixel IMS does, but these are some presets). However, starting with Android 16 QPR2 Beta 3, calling `overrideConfig` with `persistent=true` is no longer possible for non-system apps, so these settings will be reset upon system reboot.

- **Enable VoLTE**: Sets `KEY_CARRIER_VOLTE_AVAILABLE_BOOL` to true, not needed if you use the Enable VoLTE option on the first screen.
- **Enable NR(5G) SA**: Sets `KEY_CARRIER_NR_AVAILABILITIES_INT_ARRAY` to `[1, 2]` which enables both NSA and SA.
- **Enable VoNR(Vo5G)**: Sets `KEY_VONR_ENABLED_BOOL` and `KEY_VONR_SETTING_VISIBILITY_BOOL` to true. VoNR lets the device stay connected to 5G when calling instead of switching to LTE.
- **Enable VoWiFi**: Sets the following options to true.
    * `KEY_CARRIER_WFC_IMS_AVAILABLE_BOOL`
    * `KEY_CARRIER_WFC_SUPPORTS_WIFI_ONLY_BOOL`
    * `KEY_EDITABLE_WFC_MODE_BOOL`
    * `KEY_EDITABLE_WFC_ROAMING_MODE_BOOL`
- **Override 5G signal threshold**: Sets `KEY_5G_NR_SSRSRP_THRESHOLDS_INT_ARRAY` to `[-115, -105, -95, -85]`. This option exists because with AOSP's default values I only get 1 bar 5G signal in my area, but on other devices the signal are full or at least 2 bars (dBm values are the same).

If you need manual/custom overrides, please check out [Pixel IMS](https://github.com/kyujin-cho/pixel-volte-patch).

## References

- [Pixel IMS](https://github.com/kyujin-cho/pixel-volte-patch)
- [Ims](https://github.com/vvb2060/Ims)
- [类原生5G信号差的解决办法](https://www.irvingwu.blog/posts/aosp-5g-signal-strength)
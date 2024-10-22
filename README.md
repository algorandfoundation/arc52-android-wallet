# ARC-0052 Android Wallet Reference Implementation

# Setup

This library uses (and showcases) xHD-Wallet-API Kotlin library for Android.

Crucially, it also relies on `net.java.dev.jna` (@aar for the Android files) in order to have the Android app understand the path to the LibSodium binaries.

## AlgoD

The AlgoD Client parameters have been set to:

```kotlin
private val algoDClient =
        AlgodClient(
                "http://10.0.2.2",
                4001,
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        )
```

If you are running this app in an Android emulator on your computer, e.g. through Android Studio, this will correspond to port 4001 on your computer. If you've spun up a a localnet/sandbox with Algokit it will interact with it.

```
algokit localnet start
```

## Send a Transaction

Note that the default seed `exact remain north lesson program series excess lava material second riot error boss planet brick rotate scrap army riot banner adult fashion casino bamboo` should NOT be used in production.

However, for testing the app, this seed produces the Algorand address `I7V63MENRB7L4K53PQGYRFQFI7ZWXD3N53XIGP5THNNT6BSAYWBFYGX4DE`. Funding it in your environment should allow the app to make a successful transaction and return the transaction id. If you don't fund it the app will simply return fail rather than providing transaction id.

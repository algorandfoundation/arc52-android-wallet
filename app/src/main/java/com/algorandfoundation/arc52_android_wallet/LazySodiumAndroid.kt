/*
 * Copyright (c) Terl Tech Ltd • 04/04/2020, 00:05 • goterl.com
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.goterl.lazysodium

import com.goterl.lazysodium.interfaces.MessageEncoder
import com.goterl.lazysodium.utils.HexMessageEncoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class LazySodiumAndroid(
    sodium: SodiumAndroid?,
    charset: Charset?,
    messageEncoder: MessageEncoder?
) : LazySodium(charset, messageEncoder) {
    private val sodium: SodiumAndroid?

    constructor(sodium: SodiumAndroid?) : this(sodium, StandardCharsets.UTF_8, HexMessageEncoder())
    constructor(sodium: SodiumAndroid?, charset: Charset?) : this(
        sodium,
        charset,
        HexMessageEncoder()
    )

    constructor(sodium: SodiumAndroid?, messageEncoder: MessageEncoder?) : this(
        sodium,
        StandardCharsets.UTF_8,
        messageEncoder
    )

    init {
        this.sodium = sodium
    }

    override fun getSodium(): SodiumAndroid? {
        return sodium
    }
}
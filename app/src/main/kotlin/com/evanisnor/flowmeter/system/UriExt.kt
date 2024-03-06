package com.evanisnor.flowmeter.system

import android.net.Uri
import java.net.URI

fun URI.toAndroidUri() = Uri.parse(toString())

fun Uri.toJavaUri() = URI.create(toString())

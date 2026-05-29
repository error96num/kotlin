/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:OptIn(ExperimentalStdlibApi::class, JsIntrinsic::class)
package kotlin.coroutines.intrinsics

import kotlin.coroutines.Continuation
import kotlin.internal.UsedFromCompilerGeneratedCode

@JsName("$")
@UsedFromCompilerGeneratedCode
internal suspend fun <T> suspendLambdaRun(value: dynamic): T {
    // Please don't change the condition without nice arguments
    // this specific check shows the best benchmarking results across all browsers
    // between different approaches.
    // You can check the micro-benchmark here: https://jsbm.dev/L2qWRbhEQABha
    if (value.constructor === js("Promise")) {
        return await(value)
    } else {
        return jsSuspendValue(value)
    }
}

private val continuationSymbol = Continuation::class.js.asDynamic().Symbol

@UsedFromCompilerGeneratedCode
internal fun <T> orPromise(continuation: dynamic, lambda: dynamic): dynamic {
    if (continuation != VOID && continuation[continuationSymbol]) {
        return lambda()
    } else {
        return promisify<T>(lambda)
    }
}

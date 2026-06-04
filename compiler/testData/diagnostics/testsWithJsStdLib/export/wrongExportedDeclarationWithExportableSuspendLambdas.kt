// RUN_PIPELINE_TILL: FRONTEND
// OPT_IN: kotlin.js.ExperimentalJsExport
// LANGUAGE: +JsAllowExportingSuspendFunctions +JsExportingSuspendLambdas
package foo

@JsExport
val exportedSuspendLambda: suspend () -> String = { "OK" }

@JsExport
fun produceSuspendLambda(): suspend (Int) -> Int = { x -> x * 2 }

@JsExport
suspend fun runLambda(callback: suspend (Int) -> Int): Int = callback(21) * 2

@JsExport
suspend fun runVoidLambda(callback: suspend () -> Unit) {
    callback()
}

@JsExport
fun roundTrip(callback: suspend (Int) -> Int): suspend (Int) -> Int =
    { x -> callback(x) + 1 }

@JsExport
class LambdaHolder(private val base: Int) {
    val multiplier: suspend (Int, Int) -> Int = { x, y -> x * y }

    fun produceAdder(): suspend (Int) -> Int = { x -> x + base }

    suspend fun apply(cb: suspend (Int) -> Int, x: Int): Int = cb(x)
}

@JsExport
fun produceArrayOfSuspendLambdas(): Array<suspend (Int) -> Int> = arrayOf(
    { x -> x + 1 },
    { x -> x * 2 },
)

@JsExport
suspend fun reduceArrayOfSuspendLambdas(lambdas: Array<suspend (Int) -> Int>, start: Int): Int {
    var acc = start
    for (lambda in lambdas) {
        acc = lambda(acc)
    }
    return acc
}

@JsExport
interface InterfaceWithSuspendLambdaProp {
    val handler: suspend (Int) -> String
}

@JsExport
abstract class AbstractClassWithSuspendLambdaProp {
    abstract val handler: suspend (Int) -> String
}

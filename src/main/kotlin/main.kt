
sealed class Command () {
    data class Increment(val i : Int): Command ()
    data class Decrement(val i : Int) : Command ()
}

sealed class Event () {
    data class Incremented(val i : Int): Event ()
    data class Decremented(val i : Int) : Event ()
}



fun main(args: Array<String>) {
    println("Hello World!")
}
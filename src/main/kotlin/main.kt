import akka.actor.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.Behaviors
import akka.cluster.sharding.typed.javadsl.ClusterSharding
import akka.cluster.sharding.typed.javadsl.Entity
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.javadsl.CommandHandler
import akka.persistence.typed.javadsl.EventHandler
import akka.persistence.typed.javadsl.EventSourcedBehavior
import akka.actor.typed.ActorRef

import akka.cluster.sharding.typed.javadsl.EntityTypeKey

sealed class Command () {
    data class Increment(val i : Int): Command ()
    data class Decrement(val i : Int) : Command ()
    // This is to get around lack of parameterless constructors
    // for data classes
    data class Get(val replyTo: ActorRef<State>) : Command ()
}

sealed class Event () {
    data class Incremented(val i : Int): Event ()
    data class Decremented(val i : Int) : Event ()
}

data class State (val amount : Int)

class Counter(persistenceId: PersistenceId) : EventSourcedBehavior<Command, Event, State>(persistenceId) {

    companion object {
        val EntityKey = EntityTypeKey.create(Command::class.java,"Counter")

        private fun create(entityId:String): Behavior<Command> {
            return Behaviors.setup { ctx -> start(Counter(PersistenceId.of(EntityKey.name(),entityId)),ctx) }
        }

        fun init(system: akka.actor.typed.ActorSystem<Any>) {
            ClusterSharding.get(system).init(Entity.of(EntityKey) { ctx -> create(ctx.entityId) })
        }
    }

    override fun emptyState(): State {
        return State(0)
    }

    override fun commandHandler(): CommandHandler<Command, Event, State> {
        return CommandHandler { state, command ->
            when (command) {
                is Command.Increment -> {
                    Effect().persist(Event.Incremented(command.i))
                }
                is Command.Decrement -> {
                    Effect().persist(Event.Decremented(command.i))
                }
                is Command.Get -> {
                    Effect().reply(command.replyTo,state)
                }
            }
        }
    }

    override fun eventHandler(): EventHandler<State, Event> {
        return EventHandler { state, event ->
            when (event) {
                is Event.Incremented -> {
                    val newValue = event.i + state.amount
                    State(newValue)
                }
                is Event.Decremented -> {
                    val newValue = state.amount - event.i
                    State(newValue)
                }
            }
        }
    }
}



fun main(args: Array<String>) {
    val system : akka.actor.typed.ActorSystem<Any> = akka.actor.typed.ActorSystem.create(Behaviors.empty(),"CounterService")
    try {
        Counter.init(system)
        val sharding = ClusterSharding.get(system)
        println("Hello World!")
    } catch (e:Exception) {
        println("Exception occurred")
        println(e.toString())
    }
}
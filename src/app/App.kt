package app

import kotlinx.coroutines.*
import kotlin.browser.window
import react.*

enum class State {
    WAITING,
    LOADING
}

/**
 * I have chosen to clearly separate the logic and render components. This main component
 * is only used to handle the logic. The logic is a basic state machine with 2 states.
 */
val main = functionalComponent<RProps> {
    // These two state hooks handle the state machine and cat url to display
    val (state, setState) = useState { State.LOADING }
    val (catUrl, setCatUrl) = useState { null as String? }

    // This effect hook only needs to trigger on a state change, not the url being set
    useEffect(listOf(state)) {
        // Use coroutine to handle promises in async/await style
        CoroutineScope(Dispatchers.Default).launch {
            when (state) {
                // When we are waiting, delay 2 seconds and move to loading
                State.WAITING -> {
                    delay(2000)
                    setState(State.LOADING)
                }
                // when loading do the fetch, if it succeeds set the cat url state, and always go
                // back to waiting
                State.LOADING -> {
                    try {
                        // Note the use of the await() suspend function in this coroutine
                        val catResponse = window.fetch("https://aws.random.cat/meow").await()
                        val catObject: dynamic = catResponse.json().await()
                        setCatUrl(catObject.file)
                    } finally {
                        setState(State.WAITING)
                    }
                }
            }
        }
    }

    // return the actual view
    catViewer(catUrl)
}

fun RBuilder.app() {
    child(main)
}

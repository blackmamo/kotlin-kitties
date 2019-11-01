import kotlinx.css.*
import styled.*

/**
 * Define the component stylesheet for this app.
 */
object ComponentStyles : StyleSheet("ComponentStyles", isStatic = true) {
    val appHeader by css {
        // Note the type safety here applied to colours and dimensions
        backgroundColor = Color.black
        height = 80.px
        // The type safety here isn't quite as good, could have separate top, bottom &c padding
        padding = 1.px.toString()
        color = Color.white
        textAlign = TextAlign.center
    }

    val image by css {
        textAlign = TextAlign.center
    }
}
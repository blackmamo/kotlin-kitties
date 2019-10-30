package app

import react.dom.*
import styled.*

/**
 * The header for the cat view
 */
internal fun <T : StyledProps> StyledElementBuilder<T>.header() {
    styledDiv {
        css {
            +ComponentStyles.appHeader
        }
        h2 {
            +"Cat show"
        }
    }
}
package app

import react.*
import react.dom.*
import styled.*

/**
 * The cat display component
 */
internal fun <T: StyledProps> StyledElementBuilder<T>.catDisplay(it: String): ReactElement {
    return styledDiv {
        css {
            +ComponentStyles.image
        }
        img(src = it) {}
    }
}
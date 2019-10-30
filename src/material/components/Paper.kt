package material.components

import react.*
import styled.*

@JsModule("@material-ui/core/Paper")
private external val paperModule: dynamic

@Suppress("UnsafeCastFromDynamic")
private val paperComponent: RComponent<MPaperProps, RState> = paperModule.default

interface MPaperProps : StyledProps {
    var component: String
    var elevation: Int
    var square: Boolean
}

/**
 * Inspired by the code from https://github.com/cfnz/muirwik, ideally this and the interface above
 * would be generated from the material-ui typescript definitions
 */
fun RBuilder.mPaper(
        component: String = "div",
        elevation: Int = 2,
        square: Boolean = false,

        className: String? = null,
        handler: StyledHandler<MPaperProps>? = null
): ReactElement = child(
    with(StyledElementBuilder<MPaperProps>(paperComponent)){
        attrs.component = component
        attrs.elevation = elevation
        attrs.square = square
        className?.let { attrs.className = it}
        handler?.invoke(this)
        create()
    }
)
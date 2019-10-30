package app

import material.components.mPaper
import react.RBuilder

/**
 * The view consists of a bit of paper containing the header and if the url is defined, the cats too
 */
internal fun RBuilder.catViewer(catUrl: String?) {
    mPaper(elevation = 10) {
        header()
        catUrl?.let {
            catDisplay(it)
        }
    }
}
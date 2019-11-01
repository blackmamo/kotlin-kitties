# Writing a React & Material UI application in Kotlin - Should you do it?

## Kotlin

I have been writing [Java](https://en.wikipedia.org/wiki/Java_(programming_language) and [Scala](https://www.scala-lang.org/) code for years now, and I recently decided to learn [Kotlin](https://kotlinlang.org/) too. It is often described as the middle ground between the other two languages, but I think that is unfair. It has many of its own unique features too such as [coroutines](https://kotlinlang.org/docs/reference/coroutines/coroutines-guide.html), [delegation](https://kotlinlang.org/docs/reference/delegation.html) and [multi platform](https://kotlinlang.org/docs/reference/multiplatform.html) support (via [Kotlin JS](https://kotlinlang.org/docs/reference/js-overview.html) and [Kotlin Native](https://kotlinlang.org/docs/reference/native-overview.html) which uses [LLVM](https://llvm.org/)).

## Single language full stack apps

With the advent of [Node.js](https://nodejs.org) the idea of writing full stack applications in a single language has become popular. I thought I would try my hand at this using Kotlin & Kotlin JS.

I did write a full stack app, but it turned out a bit too large for a blog post. Wanting to share what I found, I created this github repo for an app that shows a new cat photo every 2 seconds.

https://github.com/blackmamo/kotlin-kitties

![kotlin kitties in action](screenshot.png)

# How I wrote my application

When it comes to writing front end code, I am most familiar with the [React](https://reactjs.org/) framework and [Material UI](https://material-ui.com/) components, so I chose to use these technologies in this project too.

I am a fan of [functional programming](https://en.wikipedia.org/wiki/Functional_programming), which leads me to write React using functional, as opposed to class based, components. This style has become much easier with the advent of [React Hooks](https://reactjs.org/docs/hooks-intro.html). I find hooks, help you to avoid leaking component state into your application state when using e.g. [Redux](https://redux.js.org/introduction/getting-started).

These are the key points on how I approached this app:

 * Use Kotlin React wrappers
 * Functional components
 * Clearly separated logic & render components
 * React hooks for component state
 * Coroutines to code in an async/await style
 * Styled components instead of CSS
 
## Create kotlin react app

In the same vein as create react app, JetBrains have created [create kotlin react app](https://github.com/JetBrains/create-react-kotlin-app). I chose to use this approach to create my application. 

Create kotlin react app will create a project scaffold and use yarn to manage dependencies. It is a very fast way to get up and running and will include all the kotlin react wrappers I mention shortly.

It is worth noting that the alternative approach to this, is to use gradle to build your project. This may be more familiar to existing Kotlin developers, since [gradle](https://gradle.org/), or the [kotlin gradle dsl](https://docs.gradle.org/current/userguide/kotlin_dsl.html) is commonly used to build Kotlin projects.
 
## Using Kotlin wrappers

React is one of the most popular JavaScript frameworks and so it is not surprising to find that [JetBrains](https://www.jetbrains.com/) (who developed Kotlin) have included React support in their [kotlin-wrappers](https://github.com/JetBrains/kotlin-wrappers) project.

The react wrappers for Kotlin allow you to write react code in an idiomatic and  [type safe](https://en.wikipedia.org/wiki/Type_safety) fashion. This gave me a leg up when compared to using the raw JavaScript [interoperability features](https://kotlinlang.org/docs/tutorials/javascript/working-with-javascript.html), and meant I could avoid generating type definitions using [dukat](https://github.com/Kotlin/dukat).

The Kotlin wrappers work nicely with the [Kotlin DSL for HTML](https://github.com/Kotlin/kotlinx.html). These let you construct HTML components very easily e.g. a veery simple page in the dsl might look like:

```kotlin
html {
    body {
        div {
            a("https://kotlinlang.org") {
                +"Main site"
            }
        }
    }
}
```

## Using styled components

Another wrapper that JetBrains have provided is for is [styled components](https://www.styled-components.com/). This [Kotlin Styled](https://github.com/JetBrains/kotlin-wrappers/tree/master/kotlin-styled) wrapper is a way of defining your CSS styles in code. Since that is Kotlin code, it is also type safe code. Note how the typesafety ensures that you provide e.g. dimensions with measurements, and valid enum values where appropriate:

```kotlin
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
```

It also fits in very nicely with the Kotlin DSLs for HTML. Attaching the styles is as simple as this:

```kotlin
/**
 * The header for the cat view
 */
internal fun <T : StyledProps> StyledElementBuilder<T>.header() {
    styledDiv {
        css {
            +ComponentStyles.appHeader // attaching the style here
        }
        h2 {
            +"Cat show"
        }
    }
}
```

## Separating logic from rendering

[App.kt](https://github.com/blackmamo/kotlin-kitties/blob/master/src/app/App.kt) is the most interesting file in the whole project. This component is used to handle all of the logic in the app. It delegates all of the rendering concerns to [View.kt](https://github.com/blackmamo/kotlin-kitties/blob/master/src/app/View.kt).

The logic consists of a two state, state-machine. Toggling between a waiting state and a loading state. Note how the [when expression](https://kotlinlang.org/docs/reference/control-flow.html#when-expression) perfectly allows me to express this.

```kotlin
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
```

### Using coroutines to code in an async/await style

[Promises and futures](https://en.wikipedia.org/wiki/Futures_and_promises) are now fairly ubiquitous concepts across many languages. Initially people would write code using them with callbacks. Whilst powerful, this way of programming ends up looking very scruffy and it is easy to forget to handle errors correctly. Javascript has had [asyc/await](https://developer.mozilla.org/en-US/docs/Learn/JavaScript/Asynchronous/Async_await) for some time now, allowing asynchronous code to be written in the same way as procedural code, and allowing you to use try/catch blocks with them to handle your errors. I've recently enjoyed using this style of coding in Python 3, as it also has [coroutines and tasks](https://docs.python.org/3/library/asyncio-task.html).

This lead me to want to use Kotlin's [coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) to work with JavaScript promises. The documentation for the external [kotlin.js.Promise](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/kotlin.js.-promise/index.html) class shows that it has a [suspend function](https://kotlinlang.org/docs/reference/coroutines/composing-suspending-functions.html) called [await](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/kotlin.js.-promise/await.html) which I could use in a [launch](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/launch.html) coroutine to achieve the same programming style.

My trouble was how to create the coroutine context for the `launch`.

The [GlobalScope](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-global-scope/index.html) documentation states that using GlobalScope is highly discouraged. There are a few Stack Overflow and discuss kotlin articles that try and illustrate how to create a [CoroutineScope](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-scope/index.html), but they were all overly complex.

In the end I came up with the following approach:

```kotlin
    CoroutineScope(Dispatchers.Default).launch {
        ...
    }
```

Having launched the coroutine I could use call `await()` on javascript promises and code in that longed for style.

### Rule #1 of hooks - only call them at the top level

Rule number one of React Hooks is to [only call them from the top level](https://reactjs.org/docs/hooks-rules.html#only-call-hooks-at-the-top-level), not in loops or nested functions. There is a good reason for this. The value of the state hooks is set when the component is rendered, and won't be dynamically updated in such loops or callbacks. You should use the `useEffect` method as a trampoline to respond to state changes.

One way of writing this sort of code might have been to have done this:

```kotlin
    // empty list of hook state means this will only be called when the component is first rendered
    useEffect(listOf()) {
        // Use coroutine to handle promises in async/await style
        CoroutineScope(Dispatchers.Default).launch {
            while(true){
                val catResponse = window.fetch("https://aws.random.cat/meow").await()
                val catObject: dynamic = catResponse.json().await()
                setCatUrl(catObject.file)
                delay(2000)
            }
        }
    }
```

For this very simple application, it would work. The problem is that when the application got more complex, things would break down. Any state that this loop should be modified to read, would have the value it took when the component was first rendered. It would never observe any updates to that state.

This is why making the state machine state explicit with `useState` was a good idea in this case. Whenever the state changes the component is re-rendered and the values accessed by `useState` will be the most recent. In my more complicated application such a mistake caused some significant bugs.

Always think about supplying a list of state variables in the `useEffect` call. You only want to trigger this logic when pertinent state changes.

Please also note here the danger of using a coroutine with `await()` calls. In effect, the lines that follow an `await()` call are executed in the Promise's complete callback. This means that using them is violating the #1 rule of using hooks. In this specific situation it is OK, because the code doesn't attempt to read any state after the await call and so a bad read will never occur. Keep this in mind though whenever using `useEffect`.

## Using Material UI

OK, In this project I use very little of Material UI. I only use the Paper component. In the other larger project I mentioned I was using a lot more, but this is enough for me to illustrate the issues I encountered.

Initially I wasted a lot of time trying to generate kotlin types from [TypeScript](https://www.typescriptlang.org/) type definitions using [ts2kt](https://github.com/Kotlin/ts2kt). Please note that this tool is deprecated and you should not use it.

If you read the main [Kotlin for Javascript](https://kotlinlang.org/docs/reference/js-overview.html) page, it **clearly** states that using [Dukat](https://github.com/Kotlin/dukat) with the [Definitely Typed](http://definitelytyped.org/) type definitions is the preferred way to work.

I tried this.

The problem was that Dukat was unable to generate many of the types I needed from the definitely typed definitions. From the typescript definitions included in MaterialUI itself, I had a little more success, but I still came a cropper. The definitions are low level, and do not work with the Kotlin react wrappers. Those wrappers are the whole reason that using react in kotlin is a pleasure.

I did come across a project [muirwik](https://github.com/cfnz/muirwik), the Material UI React Wrappers in Kotlin. This was a proof of concept project, and hasn't been seen fully through, e.g. there is no npm package published. The whole library was hand rolled, which is impressive, but it does mean that as Material UI moves on, it is an expensive manual process to keep muirwik up to date and it is already out of date.

In this sample project, I ended up rolling my own wrapper for the Paper component, inspired by the muirwik project's own code.

```kotlin
package material.components

import react.*
import styled.*

// JS Module annotation declares the javascript import
@JsModule("@material-ui/core/Paper")
private external val paperModule: dynamic

@Suppress("UnsafeCastFromDynamic")
private val paperComponent: RComponent<MPaperProps, RState> = paperModule.default

// This is the bit that should be possible to auto generate from e.g. Dukat, but since StyledProps
// only exists in the react wrapper code, there is no way for Dukat to correctly use it 
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
```

The key points are:

 * MPaperProps defines the additional props and types of the component
 * Adding [extension functions](https://kotlinlang.org/docs/reference/extensions.html) to the RBuilder enables the mPaper component in the DSL
 * @JSModule annotation and `external` type facilitates the javascript interop

# Conclusions

Writing React using Kotlin was quite a lot of fun. The Kotlin DSLs for styled components and html made it a doddle, and using coroutines to write effect handlers in an async/await style is powerful (if a little dangerous).

The part that lets the whole approach down though, is integrating with other react libraries. Part of the power of using a popular eosystem like React is that you gain access to a lot of other code written by other people.

To gain the full benefit of using Kotlin for frontend development you need the type definitions. [Dukat](https://github.com/Kotlin/dukat) with the [Definitely Typed](http://definitelytyped.org/) doesn't work if you want to use the kotlin react wrappers.

 * Dukat can't correctly generate types for Material UI
 * The types that it does generate are not usable with the react wrappers
 
 What is needed is a way of generating code that is compatible with the react wrappers from the typescript definitions. As I see it there are a number of ways to do this:
 
  * Add an extension mechanism to Dukat, identify react components and auto generate code like that in muirwik
  * Generate type definitions with Dukat, and then run an annotation processor, or Kotlin compiler plugin to generate the react wrapper compatible code
    * Note that annotation processors can't be run on kotlin JS projects and the compiler plugin interface is not stable or supported.
    
If you have an existing team of Kotlin developers and really want to use a single language for front and back end, this is something you can do. Until there are better tools for generating kotlin react wrappper compatible type definitions, you can write your own wrappers for libraries you want to use e.g. MaterialUI and support them yourself. This is a big burden though, and would delay you from adopting any other new technology.

I would recommend that you use the best language for each job, and in the case of front end development, sticking to TypeScript or JavaScript is probably your best bet.

That said, I had a lot of fun writing this application, and the Kotlin JS support is very neat.

 
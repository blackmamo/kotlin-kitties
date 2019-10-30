# My first Kotlin React app

With the advent of Node.js

This project summarises what I learned from implementing my first [React](https://reactjs.org/) app in [Kotlin](https://kotlinlang.org/) using [Kotlin for Javascript](https://kotlinlang.org/docs/reference/js-overview.html) and the [kotlin-wrappers](https://github.com/JetBrains/kotlin-wrappers) project.

In short my favoured combination of approaches are:

 * Use functional components
 * Use hooks
 * Use coroutines to work with promises in an async/await style
 * Use styled components
 * Use Dukat with 

# Getting started with hooks

# Getting started with coroutines and promises

I wanted to use Kotlin's [coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) to work with JavaScript promises in a fashion akin to JavaScript's [asyc/await](https://developer.mozilla.org/en-US/docs/Learn/JavaScript/Asynchronous/Async_await) syntax. The documentation for the external [kotlin.js.Promise](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/kotlin.js.-promise/index.html) class shows that it has a [suspend function](https://kotlinlang.org/docs/reference/coroutines/composing-suspending-functions.html) called [await](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/kotlin.js.-promise/await.html) which I could use in a [launch](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/launch.html) coroutine to achieve the same effect.

My trouble was how to create the coroutine context for the `launch`.

The [GlobalScope](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-global-scope/index.html) documentation states that using GlobalScope is highly discouraged. There are a few Stack Overflow and discuss kotlin articles that try and illustrate how to create a [CoroutineScope](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-scope/index.html), but they were all overly complex.

In the end I came up with the following approach:

{% highlight kotlin %}
    useEffect(emptyList()) {
        CoroutineScope(Dispatchers.Default   ).launch {
        try {
            val urlResponse = window.fetch("gateway.url").await()
            setStatus("Got url response, awaiting content...")

                val url = urlResponse.text().await()
                setStatus("Url is $url, Loading data...")

                setUrl(url)
            } catch (e: dynamic) {
                setStatus("Failed to determine url to load. Error: $e")
            }
        }
    }
{% endhighlight %}


## Use Dukat to generate Kotlin type definitions from TypeScript

Initially I wasted a lot of time trying to generate kotlin types from [TypeScript](https://www.typescriptlang.org/) type definitions using [ts2kt](https://github.com/Kotlin/ts2kt).

If you read the main [Kotlin for Javascript](https://kotlinlang.org/docs/reference/js-overview.html) page, it **clearly** states that using [Dukat](https://github.com/Kotlin/dukat) with the [Definitely Typed](http://definitelytyped.org/) type definitions is the preferred way to work.

## Having type definitions for React components is not enough to use them easily

 
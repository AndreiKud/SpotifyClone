package ru.andreikud.spotifyclone.other

open class Event<out T>(private val data: T) {

    private var isHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (isHandled) {
            return null
        } else {
            isHandled = true
            data
        }
    }

    fun peekContent() = data
}
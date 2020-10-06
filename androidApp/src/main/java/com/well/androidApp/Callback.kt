package com.well.androidApp

interface Callback<R, E> where E: Exception {
    fun onSuccess(result: R)
    fun onCancel()
    fun onError(error: E)
}
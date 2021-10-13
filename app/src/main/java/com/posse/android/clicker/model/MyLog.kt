package com.posse.android.clicker.model

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

object MyLog {

    private val log = PublishSubject.create<String>()

    fun get(): Observable<String> = log

    fun add(string: String) = log.onNext(string)
}
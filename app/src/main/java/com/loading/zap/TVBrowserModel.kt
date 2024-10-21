package com.loading.zap.viewmodels.tv

import com.loading.resources.util.HeaderProvider

interface TvBrowserModel<T> {
    fun isEmpty() : Boolean
    var currentItem: T?
    var nbColumns: Int
    val provider: HeaderProvider
}
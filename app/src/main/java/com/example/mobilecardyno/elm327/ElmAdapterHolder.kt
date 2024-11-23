package com.example.mobilecardyno.elm327

object ElmAdapterHolder {
    var elmAdapter: ElmAdapter? = null

    fun setAdapter(newAdapter: ElmAdapter) { elmAdapter = newAdapter }
    fun getAdapter(): ElmAdapter? { return elmAdapter }
    fun clearAdapter() { elmAdapter = null }
}
package com.taskworld.android.response

/**
 * Created by Kittinun Vantasin on 7/14/15.
 */

interface Response {

    val isSuccessful: Boolean

}

interface ListResponse<T> : Response {

    val items: List<T>

}
package com.taskworld.android.rxmovie.presentation.presenter

import android.view.View
import com.taskworld.android.domain.SignInInteractor
import com.taskworld.android.rxmovie.R
import com.taskworld.android.rxmovie.presentation.viewaction.SignInViewAction
import fuel.util.build
import reactiveandroid.property.MutablePropertyOf
import rx.Observable
import reactiveandroid.scheduler.AndroidSchedulers

/**
 * Created by Kittinun Vantasin on 7/12/15.
 */

class SignInPresenter(override val view: SignInViewAction) : Presenter<SignInViewAction> {

    val email = MutablePropertyOf<CharSequence>("")
    val pass = MutablePropertyOf<CharSequence>("")

    val token = MutablePropertyOf<CharSequence>("")
    val tokenResource = MutablePropertyOf(R.string.empty)

    val signInEnabled = Observable.combineLatest(email.observable, pass.observable) { e, p ->
        isValidEmailPattern(e) && isValidPassword(p)
    }

    val clearVisible = Observable.combineLatest(email.observable, pass.observable) { e, p ->
        if (e.length() == 0 && p.length() == 0) View.GONE else View.VISIBLE
    }

    val interactor = SignInInteractor()

    override fun onStart() {

    }

    override fun onStop() {

    }

    fun requestSignIn() {
        val interactor = build(interactor) {
            username = email.value.toString()
            password = pass.value.toString()
        }

        interactor.invoke().observeOn(AndroidSchedulers.mainThreadScheduler()).subscribe({ response ->
            view.showSignInSuccess()
            token.value = response.requestToken
            tokenResource.value = R.string.movie_db_api_key
        }, { error ->
            view.showSignInFailure("username or password is not valid")
        })
    }

    fun isValidEmailPattern(text: CharSequence) =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
                    .toRegex()
                    .matches(text) or text.toString().equals("kittinunf");

    fun isValidPassword(password: CharSequence) = password.length() >= 6

}
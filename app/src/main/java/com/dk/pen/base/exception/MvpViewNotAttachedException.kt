package com.dk.pen.base.exception

/**
 * Created by andrea on 15/05/16.
 */
class MvpViewNotAttachedException : RuntimeException(
        "Please call Presenter.attachView(MvpView) before requesting data to the Presenter")
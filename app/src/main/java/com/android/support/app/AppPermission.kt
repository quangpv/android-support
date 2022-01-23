package com.android.support.app

import android.support.core.permission.*
import android.support.core.view.ViewScopeOwner
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.atomic.AtomicInteger


interface AppPermissionOwner : ViewScopeOwner {
    val appPermission: AppPermission
        get() = viewScope.getOr("permission") {
            when (this) {
                is Fragment -> AppPermission(this)
                is FragmentActivity -> AppPermission(this)
                else -> error("Not support ${this.javaClass.name}")
            }
        }
}

class AppPermission {
    companion object {
        val PERMISSION_CAMERA = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val PERMISSION_WRITE_STORAGE = arrayOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val PERMISSION_LOCATION = arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        val PERMISSION_CALL_WHATS_APP = arrayOf(
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.CALL_PHONE
        )
    }

    private var mChecker: PermissionAccessible
    private val mNextLocalRequestCode = AtomicInteger()

    constructor(activity: FragmentActivity) {
        mChecker = PermissionAccessibleImpl().apply {
            setDispatcher(ActivityDispatcher(activity))
        }
    }

    constructor(fragment: Fragment) {
        mChecker = PermissionAccessibleImpl().apply {
            setDispatcher(FragmentDispatcher(fragment))
        }
    }

    fun accessCamera(function: () -> Unit): PermissionRequest {
        return mChecker.access(
            mNextLocalRequestCode.getAndIncrement(),
            *PERMISSION_CAMERA,
            onPermission = function
        )
    }

    fun checkWrite(function: (Boolean) -> Unit): PermissionRequest {
        return mChecker.check(
            mNextLocalRequestCode.getAndIncrement(),
            *PERMISSION_WRITE_STORAGE,
            onPermission = function
        )
    }

    fun forceAccessLocation(function: () -> Unit): PermissionRequest {
        return mChecker.forceAccess(
            mNextLocalRequestCode.getAndIncrement(),
            *PERMISSION_LOCATION,
            onPermission = function
        )
    }

    fun accessLocation(function: (Boolean) -> Unit): PermissionRequest {
        return mChecker.check(
            mNextLocalRequestCode.getAndIncrement(),
            *PERMISSION_LOCATION,
            onPermission = function
        )
    }

    fun accessCallWhatApp(function: () -> Unit): PermissionRequest {
        return mChecker.access(
            mNextLocalRequestCode.getAndIncrement(),
            *PERMISSION_CALL_WHATS_APP,
            onPermission = function
        )
    }

    fun accessCall(function: () -> Unit): PermissionRequest {
        return mChecker.access(
            mNextLocalRequestCode.getAndIncrement(),
            android.Manifest.permission.CALL_PHONE,
            onPermission = function
        )
    }

    fun accessReadStorage(function: () -> Unit): PermissionRequest {
        return mChecker.access(
            mNextLocalRequestCode.getAndIncrement(),
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            onPermission = function
        )
    }

    fun accessWriteStorage(function: () -> Unit): PermissionRequest {
        return mChecker.access(
            mNextLocalRequestCode.getAndIncrement(),
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            onPermission = function
        )
    }
}
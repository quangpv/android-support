package com.android.support

import android.os.Bundle
import android.support.di.Inject
import android.support.di.ShareScope
import android.support.di.inject
import android.support.navigation.FragmentResultCallback
import android.support.navigation.findNavigator
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    val idTest by inject<DITest>()
    val idTest1 by inject<DITest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            findNavigator().navigate(LoginFragment::class)
        }
        assert(idTest == idTest1)
    }
}

@Inject(ShareScope.Singleton)
class DITest {

}

abstract class SimpleFragment : Fragment(R.layout.fragment_simple) {

    lateinit var btnClick: Button
    lateinit var txtSimple: TextView
    val idTest by inject<DITest>()
    val idTest1 by inject<DITest>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtSimple = view.findViewById(R.id.txtSimple)
        btnClick = view.findViewById(R.id.btnClick)
        assert(idTest == idTest1)
    }

    fun Button.onClick(text: String, callback: () -> Unit) {
        this.text = text
        setOnClickListener { callback() }
    }

}

class LoginFragment : SimpleFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtSimple.text = "Login"
        btnClick.onClick("Next") {
            findNavigator().navigate(RegisterFragment::class)
        }
    }
}

class RegisterFragment : SimpleFragment(), FragmentResultCallback {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtSimple.text = "Register"
        btnClick.onClick("Next") {
            findNavigator().navigate(ReviewFragment::class)
        }
    }

    override fun onFragmentResult(result: Bundle) {
        txtSimple.text = "Register ${result["text"]}"
    }
}

class ReviewFragment : SimpleFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtSimple.text = "Review"
        btnClick.onClick("Previous") {
            findNavigator().navigateUp(bundleOf("text" to "Review ${Random.nextDouble()}"))
        }
    }
}
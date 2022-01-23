package com.android.support.feature

import android.os.Bundle
import android.support.core.livedata.DistributionLiveData
import android.support.core.livedata.distributeBy
import android.support.core.route.argument
import android.support.core.route.lazyArgument
import android.support.core.view.viewBinding
import android.support.navigation.findNavigator
import android.support.viewmodel.launch
import android.support.viewmodel.viewModel
import android.view.View
import androidx.lifecycle.ViewModel
import com.android.support.R
import com.android.support.app.AppActivity
import com.android.support.app.AppFragment
import com.android.support.databinding.FragmentTestBinding
import com.android.support.datasource.DatasourceProvider
import com.android.support.model.TestEntity
import com.android.support.navigation.Router
import com.android.support.navigation.Routing

class MainNavigationActivity : AppActivity(R.layout.activity_main_navigation) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            Router.navigate(self, argument())
        }
    }

    override fun onBackPressed() {
        if (!findNavigator().navigateUp()) {
            super.onBackPressed()
        }
    }
}

class TestDetailFragment : AppFragment(R.layout.fragment_test) {
    private val binding by viewBinding(FragmentTestBinding::bind)
    private val viewModel by viewModel<VM>()

    private val args by lazyArgument<Routing.TestDetail>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.test.bind {
            with(binding.lItemTest) {
                txtId.text = it.id
                txtName.text = it.name
                txtStatus.text = it.status
            }
        }
        viewModel.setId(args.id)
    }

    class VM(private val datasourceProvider: DatasourceProvider) : ViewModel() {
        val datasource = datasourceProvider.testDao

        val test = DistributionLiveData<TestEntity>()

        fun setId(id: String) = launch {
            datasource.getById(id).distributeBy(test)
        }
    }
}
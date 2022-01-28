package com.android.support.repo.random

import android.support.di.Inject
import com.android.support.datasource.local.RandomLocalSource
import com.android.support.helper.Validator
import com.android.support.model.entity.RandomEntity
import com.android.support.model.request.EditRandomRequest

@Inject
class EditRandomRepo(
    private val validator: Validator,
    private val randomLocalSource: RandomLocalSource,
) {
    suspend operator fun invoke(request: EditRandomRequest) {
        with(validator) {
            checkName(request.name)
            checkStatus(request.status)
            checkFolderId(request.folderId)
        }
        randomLocalSource.saveAll(listOf(
            RandomEntity(
                request.id,
                request.name,
                request.status
            )
        ), request.folderId)
        randomLocalSource.invalidate()
    }
}
package com.infinity.drive.data.mapper

import com.infinity.drive.data.local.entity.ExclusionEntity
import com.infinity.drive.domain.model.Exclusion

fun ExclusionEntity.toDomain(): Exclusion = Exclusion(
    id = id,
    type = type,
    value = value,
    enabled = enabled,
    createdAt = createdAt
)

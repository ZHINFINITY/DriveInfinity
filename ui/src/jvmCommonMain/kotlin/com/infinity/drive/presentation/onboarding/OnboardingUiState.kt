package com.infinity.drive.presentation.onboarding

import com.infinity.drive.presentation.common.UiText

import com.infinity.drive.core.telegram.CodeDeliveryChannel
import com.infinity.drive.domain.model.Country
import com.infinity.drive.domain.model.DriveChannel

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val working: Boolean = false,
    val error: UiText? = null,
    val codePhoneNumber: String = "",
    val codeChannel: CodeDeliveryChannel = CodeDeliveryChannel.TELEGRAM_APP,
    val codeLength: Int? = null,
    val passwordHint: String? = null,
    val registrationRequired: Boolean = false,
    val qrLink: String? = null,
    val qrMode: Boolean = false,
    val countries: List<Country> = emptyList(),
    val countryLoadState: CountryLoadState = CountryLoadState.LOADING,
    val selectedCountry: Country? = null,
    val channels: List<DriveChannel> = emptyList(),
    val selectedChatId: Long? = null,
    val channelCreated: Boolean = false,
    val backupDcim: Boolean = true,
    val backupPictures: Boolean = true,
    val backupMovies: Boolean = false,
    val autoBackupEnabled: Boolean = true,
    val wifiOnly: Boolean = false,
    val finishing: Boolean = false
)

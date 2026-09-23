package com.infinity.drive.presentation.di

import com.infinity.drive.presentation.AppViewModel
import com.infinity.drive.presentation.channels.ChannelsViewModel
import com.infinity.drive.presentation.collection.CollectionViewModel
import com.infinity.drive.presentation.files.FilesViewModel
import com.infinity.drive.presentation.gallery.GalleryViewModel
import com.infinity.drive.presentation.home.HomeViewModel
import com.infinity.drive.presentation.onboarding.OnboardingViewModel
import com.infinity.drive.presentation.note.NoteEditorViewModel
import com.infinity.drive.presentation.preview.PreviewContentResolver
import com.infinity.drive.presentation.preview.PreviewViewModel
import com.infinity.drive.presentation.proxy.ProxyViewModel
import com.infinity.drive.presentation.search.SearchViewModel
import com.infinity.drive.presentation.settings.ExclusionsViewModel
import com.infinity.drive.presentation.settings.SettingsViewModel
import com.infinity.drive.presentation.transfers.TransfersViewModel
import com.infinity.drive.presentation.trash.TrashViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedUiModule = module {
    singleOf(::PreviewContentResolver)
    viewModelOf(::AppViewModel)
    viewModelOf(::PreviewViewModel)
    viewModelOf(::ChannelsViewModel)
    viewModelOf(::CollectionViewModel)
    viewModelOf(::FilesViewModel)
    viewModelOf(::GalleryViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::NoteEditorViewModel)
    viewModelOf(::ProxyViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ExclusionsViewModel)
    viewModelOf(::TransfersViewModel)
    viewModelOf(::TrashViewModel)
}

package com.shop.itemservice.config.di

import com.shop.itemservice.repository.ItemRepository
import com.shop.itemservice.repository.InMemoryItemRepository
import com.shop.itemservice.service.ItemService
import org.koin.dsl.module
import io.ktor.server.config.ApplicationConfig

fun itemModule(config: ApplicationConfig) = module {
    single<ItemRepository> { InMemoryItemRepository() }
    single { ItemService(get()) }
}

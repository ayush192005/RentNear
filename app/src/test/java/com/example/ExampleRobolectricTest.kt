package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entities.Favorite
import com.example.data.model.Property
import com.example.data.model.PropertyStatus
import com.example.data.model.PropertyType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("RentNear", appName)
    }

    @Test
    fun `property and favorite CRUD operations succeed`() = runBlocking {
        val propertyDao = db.propertyDao()
        val favoriteDao = db.favoriteDao()

        // 1. CREATE Property
        val testProperty = Property(
            id = "test-prop-1",
            ownerId = "owner-1",
            title = "Test 2 BHK Flat",
            rent = 12000,
            areaSqft = 900,
            bedrooms = 2,
            bathrooms = 2,
            address = "Sector 1",
            city = "Kalka",
            contactPhone = "+91 98765 43210",
            whatsappNumber = "919876543210",
            propertyType = PropertyType.FLAT,
            status = PropertyStatus.AVAILABLE
        )
        propertyDao.insertProperty(testProperty)

        // 2. READ Property
        val loaded = propertyDao.getPropertyById("test-prop-1")
        assertNotNull(loaded)
        assertEquals("Test 2 BHK Flat", loaded?.title)
        assertEquals(12000, loaded?.rent)

        val propertiesList = propertyDao.getAllProperties().first()
        assertEquals(1, propertiesList.size)

        // 3. CREATE & READ Favorite
        val testFavorite = Favorite(userId = "user-1", propertyId = "test-prop-1")
        favoriteDao.addFavorite(testFavorite)

        val isFav = favoriteDao.isFavoriteFlow("user-1", "test-prop-1").first()
        assertTrue(isFav)

        val favProps = favoriteDao.getFavoritePropertiesFlow("user-1").first()
        assertEquals(1, favProps.size)
        assertEquals("test-prop-1", favProps[0].id)

        // 4. UPDATE Property
        val updated = testProperty.copy(rent = 13500)
        propertyDao.updateProperty(updated)
        val reloaded = propertyDao.getPropertyById("test-prop-1")
        assertEquals(13500, reloaded?.rent)

        // 5. DELETE Favorite & Property
        favoriteDao.removeFavorite("user-1", "test-prop-1")
        val isFavAfterDelete = favoriteDao.isFavoriteFlow("user-1", "test-prop-1").first()
        assertEquals(false, isFavAfterDelete)

        propertyDao.deletePropertyById("test-prop-1")
        val deleted = propertyDao.getPropertyById("test-prop-1")
        assertNull(deleted)
    }

    @Test
    fun `database module provides singleton and daos`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbInstance = com.example.di.DatabaseModule.provideDatabase(context)
        assertNotNull(dbInstance)

        val propDao = com.example.di.DatabaseModule.providePropertyDao(dbInstance)
        assertNotNull(propDao)

        val favDao = com.example.di.DatabaseModule.provideFavoriteDao(dbInstance)
        assertNotNull(favDao)

        val userDao = com.example.di.DatabaseModule.provideUserDao(dbInstance)
        assertNotNull(userDao)
    }

    @Test
    fun `remote dto mapping and retrofit service initialization succeed`() {
        // 1. Verify Retrofit service creation
        val apiService = com.example.data.remote.SupabaseNetworkClient.createService("https://example.supabase.co")
        assertNotNull(apiService)

        // 2. Verify PropertyDto conversion
        val domainProp = Property(
            id = "test-prop-dto",
            ownerId = "owner-dto",
            title = "DTO Test Apartment",
            rent = 15000,
            areaSqft = 850,
            address = "Street 10",
            city = "Panchkula"
        )
        val dto = com.example.data.remote.dto.PropertyDto.fromDomain(domainProp)
        assertEquals("test-prop-dto", dto.id)
        assertEquals("DTO Test Apartment", dto.title)
        assertEquals(15000, dto.rent)

        val restoredDomain = dto.toDomain()
        assertEquals(domainProp.id, restoredDomain.id)
        assertEquals(domainProp.title, restoredDomain.title)
        assertEquals(domainProp.rent, restoredDomain.rent)

        // 3. Verify FavoriteDto conversion
        val domainFav = Favorite(userId = "user-abc", propertyId = "prop-xyz")
        val favDto = com.example.data.remote.dto.FavoriteDto.fromDomain(domainFav)
        assertEquals("user-abc", favDto.userId)
        assertEquals("prop-xyz", favDto.propertyId)

        val restoredFav = favDto.toDomain()
        assertEquals(domainFav.userId, restoredFav.userId)
        assertEquals(domainFav.propertyId, restoredFav.propertyId)
    }

    @Test
    fun `property repository single source of truth caching and observables work`() = runBlocking {
        val repo = com.example.data.repository.PropertyRepository(db)

        // Saving new property writes directly to Room Single Source of Truth
        val newProp = Property(
            id = "ssot-prop-1",
            ownerId = "owner-1",
            title = "SSOT Property",
            rent = 18000,
            areaSqft = 1100,
            address = "Main Road",
            city = "Chandigarh"
        )
        repo.saveProperty(newProp)

        val cached = repo.getPropertyById("ssot-prop-1")
        assertNotNull(cached)
        assertEquals("SSOT Property", cached?.title)

        // Toggle favorite via repository updates Room
        repo.toggleFavorite("user-1", "ssot-prop-1", currentIsFav = false)
        val favIds = repo.getFavoritePropertyIdsFlow("user-1").first()
        assertTrue(favIds.contains("ssot-prop-1"))
    }

    @Test
    fun `main activity launches without crashing`() {
        org.robolectric.Robolectric.buildActivity(MainActivity::class.java).use { controller ->
            controller.create().start().resume()
            assertNotNull(controller.get())
        }
    }

    @Test
    fun `navigation routes are correctly formatted`() {
        assertEquals("home", RentNearRoutes.HOME)
        assertEquals("search", RentNearRoutes.SEARCH)
        assertEquals("property_details/{propertyId}", RentNearRoutes.PROPERTY_DETAILS)
        assertEquals("property_details/123", RentNearRoutes.propertyDetails("123"))
        assertEquals("add_edit_property", RentNearRoutes.addProperty())
        assertEquals("add_edit_property?propertyId=123", RentNearRoutes.editProperty("123"))
    }

    @Test
    fun `property detail screen route and repo lookup succeed`() = runBlocking {
        val repo = com.example.data.repository.PropertyRepository(db)
        val newProp = Property(
            id = "detail-test-prop",
            ownerId = "owner-test",
            title = "Test House",
            rent = 12000,
            areaSqft = 800,
            contactPhone = "+919876543210",
            whatsappNumber = "+919876543210",
            address = "Test Street",
            city = "Kalka"
        )
        repo.saveProperty(newProp)
        val detailProp = repo.getPropertyByIdFlow(newProp.id).first()
        assertNotNull(detailProp)
        assertEquals(newProp.id, detailProp?.id)
        assertNotNull(detailProp?.contactPhone)
        assertNotNull(detailProp?.whatsappNumber)
    }

    @Test
    fun `currency utils formats integer and double rent correctly`() {
        val formattedInt = com.example.ui.util.CurrencyUtils.formatRent(12500)
        assertTrue(formattedInt.contains("12,500") || formattedInt.contains("12500"))

        val formattedZero = com.example.ui.util.CurrencyUtils.formatRent(0)
        assertTrue(formattedZero.contains("0"))
    }

    @Test
    fun `auth repository login signup and logout update login state correctly`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val authRepo = com.example.data.repository.AuthRepository(db, context)

        // Sign up new user
        val signupResult = authRepo.signup(
            email = "tester@rentnear.in",
            password = "testpassword123",
            fullName = "Test User",
            phone = "+919876543210",
            role = com.example.data.model.UserRole.TENANT
        )
        assertTrue(signupResult.isSuccess)
        assertTrue(authRepo.isUserLoggedIn.value)
        assertEquals("Test User", authRepo.currentUser.value.fullName)

        // Logout
        authRepo.logout()
        kotlinx.coroutines.delay(100) // allow coroutine update
        org.junit.Assert.assertFalse(authRepo.isUserLoggedIn.value)

        // Login again
        val loginResult = authRepo.login("tester@rentnear.in", "testpassword123")
        assertTrue(loginResult.isSuccess)
        assertTrue(authRepo.isUserLoggedIn.value)
    }

    @Test
    fun `property photo manager saves camera bitmap successfully`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val bitmap = android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888)
        val savedUri = com.example.ui.util.PropertyPhotoManager.saveCameraBitmap(context, bitmap)
        assertNotNull(savedUri)
        assertTrue(savedUri!!.startsWith("file://"))
    }
}

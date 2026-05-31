package com.hotguy.workshopmanagement.domain.usecase

import com.hotguy.workshopmanagement.domain.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests unitarios para [UserRole].
 *
 * Se prueban las conversiones desde el claim JWT del backend y los
 * casos límite (prefijo desconocido, valor vacío, null implícito).
 */
class UserRoleTest {

    // ── fromJwtClaim ──────────────────────────────────────────────────────────

    @Test
    fun `fromJwtClaim with ROLE_ADMIN prefix returns ADMIN`() {
        assertEquals(UserRole.ADMIN, UserRole.fromJwtClaim("ROLE_ADMIN"))
    }

    @Test
    fun `fromJwtClaim with ROLE_MECHANIC prefix returns MECHANIC`() {
        assertEquals(UserRole.MECHANIC, UserRole.fromJwtClaim("ROLE_MECHANIC"))
    }

    @Test
    fun `fromJwtClaim with ROLE_CLIENT prefix returns CLIENT`() {
        assertEquals(UserRole.CLIENT, UserRole.fromJwtClaim("ROLE_CLIENT"))
    }

    @Test
    fun `fromJwtClaim without ROLE_ prefix still resolves correctly`() {
        // El prefijo se elimina con removePrefix — si no está presente,
        // el string queda igual y el match por nombre funciona igualmente
        assertEquals(UserRole.ADMIN,    UserRole.fromJwtClaim("ADMIN"))
        assertEquals(UserRole.MECHANIC, UserRole.fromJwtClaim("MECHANIC"))
        assertEquals(UserRole.CLIENT,   UserRole.fromJwtClaim("CLIENT"))
    }

    @Test
    fun `fromJwtClaim with unknown value returns null`() {
        assertNull(UserRole.fromJwtClaim("ROLE_SUPERUSER"))
        assertNull(UserRole.fromJwtClaim(""))
        assertNull(UserRole.fromJwtClaim("ROLE_"))
    }

    @Test
    fun `fromJwtClaim is case sensitive`() {
        // Spring Security devuelve siempre en mayúsculas; si llegara en
        // minúsculas no debería resolverse
        assertNull(UserRole.fromJwtClaim("ROLE_admin"))
        assertNull(UserRole.fromJwtClaim("role_admin"))
    }

    // ── Enum values ───────────────────────────────────────────────────────────

    @Test
    fun `UserRole has exactly three values`() {
        assertEquals(3, UserRole.entries.size)
    }

    @Test
    fun `UserRole entries contain ADMIN, MECHANIC and CLIENT`() {
        val names = UserRole.entries.map { it.name }
        assert(names.contains("ADMIN"))
        assert(names.contains("MECHANIC"))
        assert(names.contains("CLIENT"))
    }
}

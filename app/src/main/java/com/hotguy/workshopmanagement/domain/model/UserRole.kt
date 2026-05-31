package com.hotguy.workshopmanagement.domain.model

/**
 * Roles de usuario del sistema, tal como los define el backend.
 *
 * Este enum vive en la capa de dominio porque las reglas de negocio
 * (qué puede hacer cada rol) se evalúan aquí, independientemente de
 * cómo se transporten en la red (como string en el JWT) o cómo se
 * muestren en la UI (como texto localizado).
 *
 * El backend devuelve el rol con el prefijo "ROLE_" en el JWT claim
 * (convención de Spring Security). El mapper en la capa de datos
 * elimina ese prefijo al convertir el string al enum.
 */
enum class UserRole {

    /**
     * Administrador del sistema.
     * Acceso completo: gestión de usuarios, clientes, vehículos,
     * mecánicos, tareas y cobros.
     */
    ADMIN,

    /**
     * Mecánico del taller.
     * Puede gestionar clientes, vehículos y tareas, pero no puede
     * gestionar usuarios ni marcar tareas como pagadas.
     */
    MECHANIC,

    /**
     * Cliente del taller.
     * Solo puede consultar sus propios vehículos y el historial
     * de tareas asociadas a ellos. Acceso de solo lectura.
     */
    CLIENT;

    companion object {
        /**
         * Convierte el string que devuelve el backend en el enum correspondiente.
         * Elimina el prefijo "ROLE_" que añade Spring Security al claim del JWT.
         *
         * @param value Cadena del JWT, ej. "ROLE_ADMIN" o "ROLE_CLIENT".
         * @return El [UserRole] correspondiente, o null si no se reconoce el valor.
         */
        fun fromJwtClaim(value: String): UserRole? {
            val normalized = value.removePrefix("ROLE_")
            return entries.firstOrNull { it.name == normalized }
        }
    }
}

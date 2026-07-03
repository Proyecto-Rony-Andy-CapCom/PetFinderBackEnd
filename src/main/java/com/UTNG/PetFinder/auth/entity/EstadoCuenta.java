package com.UTNG.PetFinder.auth.entity;

/**
 * NOTA: igual que TipoCuenta, ajusta estos valores para que coincidan
 * EXACTAMENTE con los definidos en tu CREATE TYPE estado_cuenta AS ENUM (...).
 */
public enum EstadoCuenta {
    activa,
    suspendida,
    inactiva,
    eliminada
}
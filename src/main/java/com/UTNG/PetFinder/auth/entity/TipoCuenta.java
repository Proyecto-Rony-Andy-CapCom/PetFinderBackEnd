package com.UTNG.PetFinder.auth.entity;

/**
 * NOTA: el SQL no define los valores del tipo ENUM `tipo_cuenta` en Postgres.
 * Ajusta estos valores para que coincidan EXACTAMENTE (mismo texto y orden
 * no importa, pero el texto sí) con los definidos en tu CREATE TYPE tipo_cuenta
 * AS ENUM (...).
 */
public enum TipoCuenta {
    ciudadano,
    refugio,
    clinica,
    administrador
}
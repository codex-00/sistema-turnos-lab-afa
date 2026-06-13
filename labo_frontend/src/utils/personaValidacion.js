export const soloNumeros = (valor) => (valor || '').toString().replace(/\D/g, '');

export const soloLetras = (valor) =>
    (valor || '')
        .toString()
        .replace(/[^\p{L} '\-]/gu, '')
        .replace(/\s+/g, ' ');

export const textoBasico = (valor) =>
    (valor || '')
        .toString()
        .replace(/[^\p{L}\d .,'\-#/]/gu, '')
        .replace(/\s+/g, ' ');

export const nombreValido = (valor) => /^[\p{L}]+(?:[ '\-][\p{L}]+)*$/u.test((valor || '').trim()) && (valor || '').trim().length >= 2;
export const dniValido = (valor) => /^\d{7,10}$/.test((valor || '').trim());
export const telefonoValido = (valor) => /^\d{6,15}$/.test((valor || '').trim());
export const emailValido = (valor) => /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test((valor || '').trim());
export const direccionValida = (valor) => /^[\p{L}\d .,'\-#/]{3,120}$/u.test((valor || '').trim());

export const limpiarPersona = (persona) => ({
    ...persona,
    nombre: soloLetras(persona?.nombre).trim(),
    apellido: soloLetras(persona?.apellido).trim(),
    dni: soloNumeros(persona?.dni),
    telefono: soloNumeros(persona?.telefono),
    email: (persona?.email || '').toString().trim().toLowerCase(),
    direccion: textoBasico(persona?.direccion).trim()
});

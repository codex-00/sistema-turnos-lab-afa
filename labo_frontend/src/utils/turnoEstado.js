export const normalizeTurnoEstado = (estado) => (estado || 'PENDIENTE').toString().trim().toUpperCase();

export const isTurnoRealizado = (turno) => normalizeTurnoEstado(turno?.estado) === 'REALIZADO';
export const isTurnoCancelado = (turno) => normalizeTurnoEstado(turno?.estado) === 'CANCELADO';

export const isTurnoPerdido = (turno) => {
    const estado = normalizeTurnoEstado(turno?.estado);
    if (estado === 'REALIZADO' || estado === 'CANCELADO') {
        return false;
    }
    if (!turno?.fechaDeTurno) {
        return false;
    }

    const fechaTurno = new Date(turno.fechaDeTurno);
    if (Number.isNaN(fechaTurno.getTime())) {
        return false;
    }

    return fechaTurno < new Date();
};

export const isTurnoReprogramable = (turno) => !isTurnoRealizado(turno) && !isTurnoCancelado(turno) && !isTurnoPerdido(turno);

export const resolveTurnoEstado = (turno) => {
    if (isTurnoPerdido(turno)) {
        return 'PERDIDO';
    }

    const estado = normalizeTurnoEstado(turno?.estado);
    return estado;
};

export const getTurnoEstadoSeverity = (estado) => {
    switch (normalizeTurnoEstado(estado)) {
        case 'CANCELADO':
            return 'danger';
        case 'REPROGRAMADO':
            return 'warn';
        case 'PENDIENTE':
            return 'warn';
        case 'REALIZADO':
            return 'success';
        case 'PERDIDO':
            return 'secondary';
        default:
            return 'info';
    }
};

export const getTurnoEstadoTagClass = (estado) => {
    switch (normalizeTurnoEstado(estado)) {
        case 'CANCELADO':
            return '!bg-red-100 !text-red-700';
        case 'REPROGRAMADO':
            return '!bg-yellow-100 !text-yellow-800';
        case 'PENDIENTE':
            return '!bg-orange-100 !text-orange-800';
        case 'REALIZADO':
            return '!bg-green-100 !text-green-700';
        case 'PERDIDO':
            return '!bg-gray-200 !text-gray-700';
        default:
            return '!bg-slate-100 !text-slate-700';
    }
};

export const getTurnoEstadoClass = (estado) => {
    switch (normalizeTurnoEstado(estado)) {
        case 'CANCELADO':
            return 'bg-red-100 text-red-700 border-red-200';
        case 'REPROGRAMADO':
            return 'bg-yellow-100 text-yellow-800 border-yellow-200';
        case 'PENDIENTE':
            return 'bg-orange-100 text-orange-800 border-orange-200';
        case 'REALIZADO':
            return 'bg-green-100 text-green-700 border-green-200';
        case 'PERDIDO':
            return 'bg-gray-200 text-gray-700 border-gray-300';
        default:
            return 'bg-slate-100 text-slate-700 border-slate-200';
    }
};

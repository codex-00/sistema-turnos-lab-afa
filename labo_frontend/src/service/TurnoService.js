import axios from 'axios';

// const getToken = () => {
//     const auth = JSON.parse(localStorage.getItem("auth"));
//     return auth?.token || null
// }

const api = axios.create({
    baseURL: `http://localhost:8081`,
    headers: {
        accept: 'application/json'
    }
});

export default class TurnoService {
    // async findAllFake() {
    //     const res = await fetch(contextPath + 'demo/data/plan-desarrollo-institucional.json');
    //     return await res.json();
    // }

    getTurnos() {
    return api.get('/turnos')
        .then(response => response.data);
    }

    // http://localhost:8081/turnos
    async findAll() {
        try {
            // const config = {
            //     headers: {
            //         Authorization: 'Bearer ' + getToken()
            //     }
            // };

            const { data } = await api.get('/turnos'); // , config
            console.log(data);

            return data;
        } catch (error) {
            if (error.response?.data) throw error.response.data;
            throw error;
        }
    }

    // http://localhost:8081/turnos?medico=1
    async findAllByMedico({idMedico}) {
        try {
            // const config = {
            //     headers: {
            //         Authorization: 'Bearer ' + getToken()
            //     }
            // };

            const { data } = await api.get(`/turnos?medico=${idMedico}`); // , config
            console.log(data);

            return data;
        } catch (error) {
            if (error.response?.data) throw error.response.data;
            throw error;
        }
    }

        // http://localhost:8081/turnos?paciente=1
    async findAllByPaciente({idPaciente}) {
        try {
            // const config = {
            //     headers: {
            //         Authorization: 'Bearer ' + getToken()
            //     }
            // };

            const { data } = await api.get(`/turnos?paciente=${idPaciente}`); // , config
            console.log(data);

            return data;
        } catch (error) {
            if (error.response?.data) throw error.response.data;
            throw error;
        }
    }

    // async findOne(id) {
    //     try {
    //         const config = {
    //             headers: {
    //                 Authorization: 'Bearer ' + getToken()
    //             }
    //         };

    //         const { data } = await api.get(`/medicos/${id}`, config);
    //         return data;
    //     } catch (error) {
    //         if (error.response.data) throw error.response.data;
    //         throw error;
    //     }
    // }

    async create({ fechaDeTurno, medico, paciente}) {
        // fechaRegistro
        try {
            const { data } = await api.post('/turnos', {
                fechaDeTurno,
                // fechaRegistro,
                medico,
                paciente
            });
            // config
            console.log(data);

            return data;
        } catch (error) {
            if (error.response?.data) throw error.response.data;
            throw error;
        }
    }

    async update(
        id,
        {
            fechaHora,
            medico,
            paciente
            // fechaRegistro,
        }
    ) {
        // const config = {
        //     headers: {
        //         Authorization: 'Bearer ' + getToken()
        //     }
        // };
        return await api
            .put(
                `/medicos/${id}`,
                {
                    fechaHora,
                    medico,
                    paciente
                    // fechaRegistro,
                }
                // config
            )
            .then(({ data }) => data)
            .catch((error) => {
                if (error.response?.data) throw error.response.data;
                throw error;
            });
    }

    // async remove(id) {
    //     const config = {
    //         headers: {
    //             Authorization: 'Bearer ' + getToken()
    //         }
    //     };

    //     return await api
    //         .delete(`/plan-desarrollo-institucional/${id}`, config)
    //         .then(() => true)
    //         .catch((error) => {
    //             throw error.response.data;
    //         });
    // }
}

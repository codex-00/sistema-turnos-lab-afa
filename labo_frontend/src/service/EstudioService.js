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

export default class EstudioService {
    // async findAllFake() {
    //     const res = await fetch(contextPath + 'demo/data/plan-desarrollo-institucional.json');
    //     return await res.json();
    // }

    async findAll({}) {
        try {

            const { data } = await api.get('/estudios');
            console.log(data);

            return data;
        } catch (error) {
            if (error.response.data) throw error.response.data;
            throw error;
        }
    }

    // http://localhost:8081/estudios?paciente=1
    async findAllByPaciente({idPaciente}) {
        try {
            const { data } = await api.get(`/estudios?paciente=${idPaciente}`); 
            console.log(data);

            return data;
        } catch (error) {
            if (error.response.data) throw error.response.data;
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

    async create({ paciente, nombre, descripcion }) {
        // fechaRegistro
        try {
            // const config = {
            //     headers: {
            //         Authorization: 'Bearer ' + getToken()
            //     }
            // };

            const { data } = await api.post('/estudios', {
                paciente,
                nombre,
                descripcion
            });
            // config
            console.log(data);

            return data;
        } catch (error) {
            if (error.response.data) throw error.response.data;
            throw error;
        }
    }

    async update(
        id,
        {
            paciente,
            nombre,
            descripcion

        }
    ) {
        // const config = {
        //     headers: {
        //         Authorization: 'Bearer ' + getToken()
        //     }
        // };
        return await api
            .put(
                `/estudios/${id}`,
                {
                    paciente,
                    nombre,
                    descripcion
                }
                // config
            )
            .then(({ data }) => data)
            .catch((error) => {
                throw error.response.data;
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

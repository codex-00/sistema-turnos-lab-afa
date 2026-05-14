package proyecto_laboS.labo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import proyecto_laboS.labo.model.Test;
import proyecto_laboS.labo.repository.TestRepository;


@Service
public class TestServiceImpl {

    private final TestRepository testRepository;

    public TestServiceImpl(TestRepository testRepository) {
        this.testRepository = testRepository;
    }
       
    public Test guardar(Test test) {
        return testRepository.save(test);
    }   
    
}

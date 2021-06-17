package one.digitalinnovation.personapi.services;

import lombok.AllArgsConstructor;
import one.digitalinnovation.personapi.dto.request.PersonDTO;
import one.digitalinnovation.personapi.dto.response.MessageResponseDTO;
import one.digitalinnovation.personapi.entities.Person;
import one.digitalinnovation.personapi.dto.mapper.PersonMapper;
import one.digitalinnovation.personapi.repositories.PersonRepository;
import one.digitalinnovation.personapi.services.exceptions.DataIntegrityException;
import one.digitalinnovation.personapi.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PersonService {

    private final PersonRepository personRepository;

    private final PersonMapper personMapper= PersonMapper.INSTANCE;

    public PersonDTO findById(Long id){
        Person person = verifyIfExists(id);
        return personMapper.toDTO(person);
    }

    @Transactional(readOnly = true)
    public Page<PersonDTO> findPage(Pageable pageable){
        Page<Person> pagePerson = personRepository.findAll(pageable);
        return pagePerson.map(personMapper::toDTO);

    }

    @PostMapping
    public MessageResponseDTO createPerson(PersonDTO personDTO){
        Person personToSave = personMapper.toModel(personDTO);
        Person savedPerson = personRepository.save(personToSave);
        return createMessageResponse(savedPerson.getId(),"Created person with ID ");
    }

    public MessageResponseDTO updateById(Long id, PersonDTO personDTO){
        verifyIfExists(id);
        Person personToUpdate = personMapper.toModel(personDTO);
        Person updatedPerson = personRepository.save(personToUpdate);
        return createMessageResponse(updatedPerson.getId(), "Updated person with ID ");
    }

    public void delete(Long id){
        verifyIfExists(id);
        try {
            personRepository.deleteById(id);
        }catch(DataIntegrityViolationException e) {
            String msg = "Failed when trying to delete person with ID %d";
            throw new DataIntegrityException(String.format(msg, id));
        }
    }

    private Person verifyIfExists(Long id){
        return personRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Person not found!"));
    }

    private MessageResponseDTO createMessageResponse(Long id, String message) {
        return MessageResponseDTO
                .builder()
                .message(message + id)
                .build();
    }
}
